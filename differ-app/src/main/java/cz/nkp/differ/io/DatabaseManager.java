package cz.nkp.differ.io;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.derby.drda.NetworkServerControl;
import org.apache.log4j.Logger;

import cz.nkp.differ.DifferApplication;
import cz.nkp.differ.util.GeneralMacros;
 
public class DatabaseManager {
	
	private static Logger LOGGER = Logger.getLogger(DatabaseManager.class);
	private static DatabaseManager _instance;
	private static NetworkServerControl serverControl = null;
	
	private static final String DERBY_EMBEDDED_DRIVER_NAME = "org.apache.derby.jdbc.ClientDriver";
	private static final String DERBY_DATABASE_NAME = "differDB";
	private static final int DERBY_PORT = 1527;
	private static final String DERBY_CONNECTION_URL = "jdbc:derby://localhost:" + DERBY_PORT + "/"+ DERBY_DATABASE_NAME +";";
	private static final String DERBY_CONNECTION_CREATE_DB_URL = DERBY_CONNECTION_URL + "create=true";
	private static final String DERBY_SHUTDOWN_URL = DERBY_CONNECTION_URL + "shutdown=true";
	
	private static final String[] DATABASE_TABLE_NAMES = {
		"users"
	};//List of default table names
	
	private static final String[] DATABASE_TABLE_PARAMETERS = {
		"username varchar(255) NOT NULL, password_hash varchar(255) NOT NULL,password_salt varchar(255) NOT NULL,UNIQUE(username)"
	};//List of default table rows in SQL format of (***) where *** is this variables content
	
	static{//Checks to make sure the DATABASE_TABLE vars are same size, since they correspond
		if(DATABASE_TABLE_NAMES.length != DATABASE_TABLE_PARAMETERS.length){
			Exception e = new RuntimeException("Invalid database manager variables have caused a fatal error.");
			LOGGER.fatal("The DATABASE_TABLE_NAMES and DATABASE_TABLE_PAREMETERS arrays MUST be the same size!",e);
			System.exit(-1);//Very unusual, but the server cannot be allowed to run
		}
	}
	
	private static Connection dbConnection;
	
	private DatabaseManager(){
		try {
			serverControl = new NetworkServerControl(InetAddress.getByName("localhost"),DERBY_PORT);
		} catch (UnknownHostException e) {
			LOGGER.error("Unable to start derby network server!",e);
		} catch (Exception e) {
			LOGGER.error("Unable to start derby network server!",e);
		}
	}
	
	public static final DatabaseManager getInstance(){
		if(_instance == null){
			_instance = new DatabaseManager();
			_instance.load();
		}
		
		return _instance;
	}
	
	/**
	 * Syntatic Sugar
	 */
	public static final void loadDatabase(){
		getInstance();
	}
	
	
	private void load(){
		
		//If the user hasn't preconfigured the derby location, set it to our default location
		if(System.getProperty("derby.system.home") == null){
			System.setProperty("derby.system.home", DifferApplication.getHomeDirectory().getAbsolutePath());
		}	
		
		startNetworkDatabase();
		
		try {
			LOGGER.trace("Loading database.");
			Class.forName(DERBY_EMBEDDED_DRIVER_NAME).newInstance();
		} catch (InstantiationException e) {
			LOGGER.error("Unable to get database driver by name: " + DERBY_EMBEDDED_DRIVER_NAME,e);
		} catch (IllegalAccessException e) {
			LOGGER.error("Unable to get database driver by name: " + DERBY_EMBEDDED_DRIVER_NAME,e);
		} catch (ClassNotFoundException e) {
			LOGGER.error("Unable to get database driver by name: " + DERBY_EMBEDDED_DRIVER_NAME,e);
		}
		
		try {
			LOGGER.trace("Connecting to database.");
			dbConnection = DriverManager.getConnection(DERBY_CONNECTION_URL);
		} catch (SQLException e) {
			LOGGER.info("Unable to open database connection. Attempting to create database.");
			dbConnection = createDefaultDatabase();
		}
	}
	
	private static final void startNetworkDatabase(){
		if(GeneralMacros.containsNull(serverControl)){
			LOGGER.error("Unable to start network database because database handle is null");
			return;
		}
		try {
			serverControl.ping();
			return;
		} catch (Exception e) {
			// This exception means the database isn't started
			try {
				serverControl.start(null);//started
			} catch (Exception e1) {
				LOGGER.error("Starting network database failed.",e);
			}
		}
	}
	
	/**
	 * Returns true if the database is loaded.
	 * @return
	 */
	public static final boolean isLoaded(){
		if(dbConnection == null){
			return false;
		}
		try {
			return dbConnection.isValid(5);
		} catch (SQLException e) {
			return false;
		}
	}
		
	private static final Connection createDefaultDatabase(){
		Connection conn;
		
		try {
			conn = DriverManager.getConnection(DERBY_CONNECTION_CREATE_DB_URL);
			LOGGER.info("Database created");
		} catch (SQLException e) {
			LOGGER.fatal("Unable to load or create database! Now passing null Connection.",e);
			return null;
		}

		createDefaultDatabaseDefaultTables(conn);
		
		return conn;
		
	}
	
	private static final void createDefaultDatabaseDefaultTables(Connection conn){
		GeneralMacros.errorIfContainsNull(conn);//What can we do if we get nothing?
		
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
		if(!isLoaded()){
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
