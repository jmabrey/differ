package cz.nkp.differ.user;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.log4j.Logger;

import cz.nkp.differ.io.DatabaseManager;
import cz.nkp.differ.util.GeneralMacros;

/**
 * Class for application wide user authentication.
 * @author Joshua Mabrey
 * May 4, 2012
 */
public class UserDataController{
	

	private static Logger LOGGER = Logger.getLogger(UserDataController.class);
	private static UserDataController _instance = null;	
	
	private static final String PASSWORD_HASH_ALGORITHM_NAME = "SHA-1";
		
	/*
	 * SQL Queries
	 */
	private static final String QUERY_USER_INFORMATION = "SELECT password_hash, password_salt FROM users WHERE username = ?";
	private static final String ADD_USER_INFORMATION = "INSERT INTO USERS(username,password_hash,password_salt) VALUES(?,?,?)";
	
	public static enum UserLoginResult{
		DATABASE_ERROR,
		USER_DOES_NOT_EXIST,
		USER_LOGIN_FAIL,
		USER_LOGIN_SUCCESS
	};
	
	public static enum UserRegisterResult{
		DATABASE_ERROR,
		USER_ALREADY_EXISTS,
		USER_CREATION_SUCCESS,
		USER_CREATION_FAIL
	};
	
	private PreparedStatement queryUserInformationStatement;
	private PreparedStatement addUserStatement;
	
	private static MessageDigest passwordHashDigest;
	
	private static String currentUser = null;
	
	
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
	public synchronized UserLoginResult attemptLogin(String username, String userSuppliedPassword){
		
		if(!DatabaseManager.isLoaded()){
			LOGGER.error("Database is unloaded. User verification failed.");
			return UserLoginResult.DATABASE_ERROR;
		}//Database loaded
		
		
		if(GeneralMacros.containsNull(queryUserInformationStatement)){
			queryUserInformationStatement = DatabaseManager.getInstance().getStatement(QUERY_USER_INFORMATION);
			if(GeneralMacros.containsNull(queryUserInformationStatement)){
				return UserLoginResult.DATABASE_ERROR;//We can't determine validity without a database query!
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
			return UserLoginResult.DATABASE_ERROR;
		}//ResultSet from query created		
		
		if(!hasResult){
			//No results, meaning no users with that username. verify failed.
			LOGGER.trace("No users were found for username: " + username);
			return UserLoginResult.USER_DOES_NOT_EXIST;
		}
		
		//At least one result was returned! Excellent!
		String dbSuppliedSalt, dbSuppliedPasswordHash;
		try {
			dbSuppliedPasswordHash = userInfo.getString(1);//Col 1 is password_hash
			dbSuppliedSalt = userInfo.getString(2);//Col 2 is password_salt		
		} catch (SQLException e) {
			LOGGER.error("Unable to extract result String from password hash in ResultSet. User verification failed.");
			return UserLoginResult.DATABASE_ERROR;
		}
		
		String userSuppliedPasswordHash = getHashedPassword(userSuppliedPassword.toCharArray(), dbSuppliedSalt.getBytes());
		
		if(dbSuppliedPasswordHash.equals(userSuppliedPasswordHash)){
			//Valid user
				currentUser = username;
				LOGGER.trace("Logged in user: " + currentUser);
			return UserLoginResult.USER_LOGIN_SUCCESS;
		}
		
		return UserLoginResult.USER_LOGIN_FAIL;//If all else fails, end the method fail-safe			
	}
	
	public synchronized String getLoggedInUser(){
			if(currentUser == null){
				LOGGER.warn("No user logged in!");
			}
			return currentUser;
	}
	
	public synchronized UserRegisterResult registerUser(String username, String passwordPlaintext){
		if(GeneralMacros.containsNull(username,passwordPlaintext)){
			LOGGER.debug("Null username or password!");
			return UserRegisterResult.DATABASE_ERROR;
		}
		
		if(GeneralMacros.containsNull(addUserStatement)){
			addUserStatement = DatabaseManager.getInstance().getStatement(ADD_USER_INFORMATION);
			if(GeneralMacros.containsNull(addUserStatement)){
				return UserRegisterResult.DATABASE_ERROR;
			}
		}//Prep statement loaded
		
		
		String salt = getPasswordSalt();
		String hashedPassword = getHashedPassword(passwordPlaintext.toCharArray(),salt.getBytes());
		
		if(GeneralMacros.containsNull(salt,hashedPassword)){
			LOGGER.debug("Null Salt or hashed password!");
			return UserRegisterResult.DATABASE_ERROR;
		}
		try {
			addUserStatement.clearParameters();
			addUserStatement.setString(1,username);
			addUserStatement.setString(2, hashedPassword);		
			addUserStatement.setString(3, salt);
			addUserStatement.executeUpdate();
		} catch (SQLException e) {
			if(e.getSQLState().equals("23505")){
				//Duplicate unique key (aka duplicate username)
				return UserRegisterResult.USER_ALREADY_EXISTS;
			}
			//We reach this it means the error is more deviant
			LOGGER.error("Unable to create/execute the add user prepared statement.",e);
			return UserRegisterResult.DATABASE_ERROR;
		}		
		
		return UserRegisterResult.USER_CREATION_SUCCESS;
	}
	
	
	private static final String getPasswordSalt(){
		try {
			SecureRandom saltGen = SecureRandom.getInstance("SHA1PRNG");
			byte[] salt = new byte[64];
			saltGen.nextBytes(salt);
			return StringUtils.newStringUtf8(salt);
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
	private static final String getHashedPassword(char[] plaintextPassword, byte[] salt){
		if(GeneralMacros.containsNull(passwordHashDigest,salt,plaintextPassword)){
			LOGGER.warn("Failed to hash password becuase of null arguments.");
			return null;//No way to create password, so give them nothing.
		}
				
		try {
			KeySpec spec = new PBEKeySpec(plaintextPassword, salt, 2048, 160);
			SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] bytes = f.generateSecret(spec).getEncoded();
			return StringUtils.newStringUtf8(bytes);
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error("Unable to perform hash because algorithm is missing.",e);
		} catch (InvalidKeySpecException e) {
			LOGGER.error("Unable to perform hash.",e);
		}

		return null;
	}
}