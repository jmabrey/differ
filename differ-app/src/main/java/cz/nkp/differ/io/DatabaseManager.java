package cz.nkp.differ.io;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class DatabaseManager {
	private static Logger LOGGER = Logger.getLogger(DatabaseManager.class);
	
	private static final String DERBY_EMBEDDED_DRIVER_NAME = "org.apache.derby.jdbc.EmbeddedDriver";
	private static final String DERBY_CONNECTION_URL = "jdbc:derby:differDB;";
	private static final String DERBY_CONNECTION_CREATE_DB_URL = DERBY_CONNECTION_URL + "create=true";
	private static final String DERBY_SHUTDOWN_URL = "jdbc:derby:differDB;shutdown=true";
	
	private static Connection dbConnection;
	
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
			try {
				dbConnection = DriverManager.getConnection(DERBY_CONNECTION_CREATE_DB_URL);
			} catch (SQLException e1) {
				LOGGER.fatal("Unable to load or create database!");
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
}
