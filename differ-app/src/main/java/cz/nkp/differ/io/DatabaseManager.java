package cz.nkp.differ.io;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import cz.nkp.differ.util.GeneralHelperFunctions;

public class DatabaseManager {
	
	private static Logger LOGGER = Logger.getLogger(DatabaseManager.class);
	private static DatabaseManager _instance;
	
	private static final String DERBY_EMBEDDED_DRIVER_NAME = "org.apache.derby.jdbc.EmbeddedDriver";
	private static final String DERBY_CONNECTION_URL = "jdbc:derby:differDB;";
	private static final String DERBY_CONNECTION_CREATE_DB_URL = DERBY_CONNECTION_URL + "create=true";
	private static final String DERBY_SHUTDOWN_URL = "jdbc:derby:differDB;shutdown=true";
	
	private static final String[] DATABASE_TABLE_NAMES = {"users"};//List of default table names
	private static final String[] DATABASE_TABLE_PARAMETERS = {"username varchar(255) NOT NULL, password_hash varchar(255) NOT NULL,password_salt varchar(255) NOT NULL,UNIQUE(username)"};//List of default table rows in SQL format of (***) where *** is this variables content
	
	static{//Checks to make sure the DATABASE_TABLE vars are same size, since they correspond
		if(DATABASE_TABLE_NAMES.length != DATABASE_TABLE_PARAMETERS.length){
			LOGGER.fatal("The DATABASE_TABLE_NAMES and DATABASE_TABLE_PAREMETERS arrays MUST be the same size!");
			throw new RuntimeException("Invalid database manager variables have caused a fatal error.");
		}
	}
	
	private static Connection dbConnection;
	
	private DatabaseManager(){}
	
	public static final DatabaseManager getInstance(){
		if(_instance == null){
			_instance = new DatabaseManager();
			_instance.load();
		}
		
		return _instance;
	}
	
	
	public void load(){
		
		try {
			LOGGER.debug("Loading database.");
			Class.forName(DERBY_EMBEDDED_DRIVER_NAME).newInstance();
		} catch (InstantiationException e) {
			LOGGER.error("Unable to get database driver by name: " + DERBY_EMBEDDED_DRIVER_NAME,e);
		} catch (IllegalAccessException e) {
			LOGGER.error("Unable to get database driver by name: " + DERBY_EMBEDDED_DRIVER_NAME,e);
		} catch (ClassNotFoundException e) {
			LOGGER.error("Unable to get database driver by name: " + DERBY_EMBEDDED_DRIVER_NAME,e);
		}
		
		try {
			LOGGER.debug("Connecting to database.");
			dbConnection = DriverManager.getConnection(DERBY_CONNECTION_URL);
		} catch (SQLException e) {
			LOGGER.info("Unable to open database connection. Attempting to create database",e);
			dbConnection = createDefaultDatabase();
		}
	}
	
	/**
	 * Returns true if the database is loaded.
	 * @return
	 */
	public static final boolean isLoaded(){
		return dbConnection != null;
	}
	
	private static final Connection createDefaultDatabase(){
		Connection conn;
		
		try {
			conn = DriverManager.getConnection(DERBY_CONNECTION_CREATE_DB_URL);
		} catch (SQLException e1) {
			LOGGER.fatal("Unable to load or create database! Now passing null Connection.");
			return null;
		}

		createDefaultDatabaseDefaultTables(conn);

		return conn;
		
	}
	
	private static final void createDefaultDatabaseDefaultTables(Connection conn){
		GeneralHelperFunctions.errorIfContainsNull(conn);//What can we do if we get nothing?
		
		for(int pos = 0; pos < DATABASE_TABLE_NAMES.length &&
				DATABASE_TABLE_NAMES.length == DATABASE_TABLE_PARAMETERS.length;pos++){
			//For each table name and parameters, while the table name list is the same length as the parameters list
			Statement s;
			try {
				s = conn.createStatement();
				s.executeUpdate("CREATE TABLE " + DATABASE_TABLE_NAMES[pos] + "(" + DATABASE_TABLE_PARAMETERS[pos] +")" );
			} catch (SQLException e) {
				LOGGER.fatal("Unable to create a default table. Table name: " + DATABASE_TABLE_NAMES[pos]);
			}			
		}
		
	}
	
	public void shutdown(){
		if(dbConnection == null){
			LOGGER.warn("Ignored shutdown request on database because it is not loaded.");
			return;
		}
		try {
			LOGGER.debug("Shutting down database.");
			DriverManager.getConnection(DERBY_SHUTDOWN_URL);
			dbConnection = null;
		} catch (SQLException e) {
			LOGGER.error("Unable to shutdown database connection. Next startup will run recovery code",e);
		}
	}
	
	public PreparedStatement getStatement(String sql){
		if(!isLoaded()){
			LOGGER.error("Attempted to prepare statement on null database connection!");
			return null;
		}
		try {
			return dbConnection.prepareStatement(sql);
		} catch (SQLException e) {
			LOGGER.warn("Unable to create prepared statement. Statement sql: " + sql,e);
			return null;
		}
	}
}
