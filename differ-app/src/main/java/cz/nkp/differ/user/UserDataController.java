package cz.nkp.differ.user;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.log4j.Logger;

import cz.nkp.differ.io.DatabaseManager;
import cz.nkp.differ.util.GeneralHelperFunctions;

/**
 * Class for application wide user authentication.
 * @author Joshua Mabrey
 * May 4, 2012
 */
public class UserDataController{
	

	private static Logger LOGGER = Logger.getLogger(UserDataController.class);
	private static UserDataController _instance = null;	
	
	private static final String PASSWORD_HASH_ALGORITHM_NAME = "SHA-1";
	private static final int PASSWORD_HASH_ITERATIONS = 10000;
		
	/*
	 * SQL Queries
	 */
	private static final String QUERY_USER_INFORMATION = "SELECT password_hash, password_salt FROM users WHERE username = ?";
	private static final String ADD_USER_INFORMATION = "INSERT INTO USERS(username,password_hash,password_salt) VALUES(?,?,?)";
	
	
	
	private PreparedStatement queryUserInformationStatement;
	private PreparedStatement addUserStatement;
	
	private static MessageDigest passwordHashDigest;
	
	private UserDataController(){
		try {
			passwordHashDigest = MessageDigest.getInstance(PASSWORD_HASH_ALGORITHM_NAME);
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error("Unable to create MessageDigest. Algorithm: " + PASSWORD_HASH_ALGORITHM_NAME);
		}
	}
	
	public static UserDataController getInstance(){
		if(_instance == null){
			_instance = new UserDataController();
		}
		
		return _instance;
	}
	
	/**
	 * Checks the supplied username and password against the user database.
	 * 
	 * TODO:Implement
	 * @param username
	 * @param userSuppliedPassword
	 * @return
	 */
	public boolean isValidUserInfo(String username, String userSuppliedPassword){
		
		final boolean USER_INVALID = false;
		final boolean USER_VALID = true;
		
		if(!DatabaseManager.isLoaded()){
			LOGGER.error("Database is unloaded. User verification failed.");
			return USER_INVALID;
		}//Database loaded
		
		if(GeneralHelperFunctions.containsNull(queryUserInformationStatement)){
			queryUserInformationStatement = DatabaseManager.getInstance().getStatement(QUERY_USER_INFORMATION);
			if(GeneralHelperFunctions.containsNull(queryUserInformationStatement)){
				return USER_INVALID;//We can't determine validity without a database query!
			}
		}//Prep statement loaded
		
		ResultSet userInfo;
		boolean hasResult = false;//fail on error
		try {
			queryUserInformationStatement.clearParameters();
			queryUserInformationStatement.setString(1, username);
			userInfo = queryUserInformationStatement.executeQuery();
			hasResult = userInfo.next();
		} catch (SQLException e) {
			LOGGER.error("Error executing prepared statement against username: " + username + ". User verification failed.");
			return USER_INVALID;
		}//ResultSet from query created		
		
		if(!hasResult){
			//No results, meaning no users with that username. verify failed.
			LOGGER.info("No users were found for username: " + username);
			return USER_INVALID;
		}
		
		//At least one result was returned! Excellent!
		String dbSuppliedSalt, dbSuppliedPassword;
		try {
			dbSuppliedPassword = userInfo.getString(1);//Col 1 is password_hash
			dbSuppliedSalt = userInfo.getString(2);//Col 2 is password_salt
					
		} catch (SQLException e) {
			LOGGER.error("Unable to extract result String from password hash in ResultSet. User verification failed.");
			return USER_INVALID;
		}
		
		if(Arrays.equals(new BigInteger(dbSuppliedPassword).toByteArray(),getHashedPassword(userSuppliedPassword.toCharArray(), dbSuppliedSalt.getBytes())) && dbSuppliedPassword != null){
			//Valid user
			return USER_VALID;
		}
		
		return USER_INVALID;//If all else fails, end the method fail-safe			
	}
	
	public void addUser(String username, String passwordPlaintext){
		if(GeneralHelperFunctions.containsNull(queryUserInformationStatement)){
			addUserStatement = DatabaseManager.getInstance().getStatement(ADD_USER_INFORMATION);
			if(GeneralHelperFunctions.containsNull(addUserStatement)){
				return;
			}
		}//Prep statement loaded
		
		
		String salt = getPasswordSalt();
		String hashedPassword = new BigInteger(getHashedPassword(passwordPlaintext.toCharArray(),salt.getBytes())).toString();

		try {
			addUserStatement.clearParameters();
			addUserStatement.setString(1,username);
			addUserStatement.setString(2, hashedPassword);		
			addUserStatement.setString(3, salt);
			addUserStatement.executeUpdate();
		} catch (SQLException e) {
			LOGGER.error("Unable to create/execute the add user prepared statement.",e);
		}		
	}
	
	
	private static final String getPasswordSalt(){
		try {
			SecureRandom saltGen = SecureRandom.getInstance("SHA1PRNG");
			byte[] salt = new byte[64];
			saltGen.nextBytes(salt);
			return new BigInteger(salt).toString();
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error("Unable to find SHA1PRNG provider to generate salts.");
			//TODO:Implement fallback
		}
		
		return null;
		
	}
	
	/**
	 * Returns a byte array representing the hash of the given salted password.
	 * @param saltedPassword
	 * @return
	 */
	private static final byte[] getHashedPassword(char[] plaintextPassword, byte[] salt){
		if(GeneralHelperFunctions.containsNull(passwordHashDigest,salt,plaintextPassword)){
			LOGGER.warn("Failed to hash password becuase of null arguments.");
			return null;//No way to create password, so give them nothing.
		}
				
		try {
			KeySpec spec = new PBEKeySpec(plaintextPassword, salt, 2048, 160);
			SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			return f.generateSecret(spec).getEncoded();
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error("Unable to perform hash because algorithm is missing.",e);
		} catch (InvalidKeySpecException e) {
			LOGGER.error("Unable to perform hash.",e);
		}

		return null;
	}
}