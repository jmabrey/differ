package cz.nkp.differ;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Security;
import java.util.Locale;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.vaadin.terminal.gwt.server.WebApplicationContext;

import cz.nkp.differ.gui.windows.MainDifferWindow;
import cz.nkp.differ.io.DatabaseManager;
import eu.livotov.tpt.TPTApplication;

/**
 * The main Application instance, responsible for setting global settings, such as locale, theme, and the root window for the GUI.
 * This class also allows any code to fetch the current Application instance in a thread-safe way.
 * @author Joshua Mabrey
 * Mar 30, 2012
 */

@SuppressWarnings("serial")
public class DifferApplication extends TPTApplication{
	
	/*
	 * We dont need an X server running on a display to do graphics operations. May be slower on some machines.
	 * TODO: examine a switching option for this setting
	 */
	static
	{
		System.setProperty("java.awt.headless", "true");
	}
	
	/**
	 * Called by the server to run the application and begin the session
	 */
	@Override
	public void applicationInit() {
		//Setup Apache Log4j Configuration
		BasicConfigurator.configure();
		
		//BouncyCastle Setup
		Security.addProvider(new BouncyCastleProvider());
		
		setTheme(DIFFER_THEME_NAME);//Set to custom differ theme
		LOGGER.trace("Loaded Vaadin theme: " + DIFFER_THEME_NAME);
		
		//Get Application Context
		WebApplicationContext context = (WebApplicationContext) getContext();
		
		//Set Context Locale to Browser Locale
		Locale locale = context.getBrowser().getLocale();
		setLocale(locale);
		LOGGER.trace("Session Locale: " + locale.getDisplayName());
		
		//Add this as a listener to the context transaction event pump
		context.addTransactionListener(this);
		
		DatabaseManager.loadDatabase();
		
		//Load Differ Properties into JVM
		File differProps = new File(getHomeDirectory(),"differ.properties");
		if(differProps.exists() && differProps.canRead()){
			FileInputStream propStream =  null;
			try {
				propStream = new FileInputStream(differProps);
				System.getProperties().load(propStream);
				LOGGER.info("Loaded differ.properties");
			} catch (IOException e) {
				LOGGER.error("Unable to load differ.properties!",e);
			} finally{
				if(propStream != null){
					try {
						propStream.close();
					} catch (IOException e) {
						LOGGER.error("Unable to close differ.properties file stream.",e);
					}
				}
			}
		}
		
		//Setup Apache Log4j Configuration for file logging if the property is set in differ props
		if(System.getProperty("differ.logging.file").equalsIgnoreCase("true")){
			
			String fileLocation = System.getProperty("differ.logging.file.location");
			if(fileLocation == null){
				//Create a logging file in the logs directory that is names by the current nanotime
				fileLocation = new File(new File(getHomeDirectory(),"logs"),"" + System.nanoTime()).toString();
			}
			
			File loggingFile = new File(fileLocation);
			if(loggingFile.exists()){
				LOGGER.warn("differ.logging.file.location is an invalid location");
			}
			else{
				try {
					BasicConfigurator.configure(new FileAppender(new PatternLayout(PatternLayout.DEFAULT_CONVERSION_PATTERN),loggingFile.getAbsolutePath()));
				} catch (IOException e) {
					LOGGER.error("Unable to create logging file",e);
				}
			}
		}
		
		MainDifferWindow mainWindow = new MainDifferWindow();
		mainWindow.setSizeUndefined();
		setMainWindow(mainWindow);
	}
	
	@Override
	public void firstApplicationStartup() {}
		
    public static File getHomeDirectory(){
    	if(differHome == null){
    		differHome = System.getProperty("user.home");
    		differHome += File.separatorChar + ".differ";
    		LOGGER.trace("Differ Home Directory: " + differHome);
    		
    		//If the home directory doesnt exist create it
    		File differHomeFile = new File(differHome);
    		if(!differHomeFile.exists()){
    			differHomeFile.mkdir();
    		}
    		
    		//Same with the plugin subdirectory
			File differHomeFilePluginDirectory = new File(differHomeFile,"plugins");
			if(!differHomeFilePluginDirectory.exists()){
				differHomeFilePluginDirectory.mkdir();
    		}
			
			//Same with users subdirectory
			File differHomeFileUsersDirectory = new File(differHomeFile,"users");
			if(!differHomeFileUsersDirectory.exists()){
				differHomeFileUsersDirectory.mkdir();
    		}
			
			//Same with logs subdirectory
			File differHomeLogsDirectory = new File(differHomeFile,"logs");
			if(!differHomeLogsDirectory.exists()){
				differHomeLogsDirectory.mkdir();
    		}
			
    	}
    	
    	File homeDir = new File(differHome);
    	
    	if(!homeDir.exists()){
    		LOGGER.error("Differ home directory unable to be created at " + homeDir.getAbsolutePath());
    	}
    	
    	return homeDir;
    }
    
    public float getScreenWidth(){
    	return getMainWindow().getWidth();
    }
    
    private static String differHome;
 
	private static final String DIFFER_THEME_NAME = "differ";
	
	private static Logger LOGGER = Logger.getLogger(DifferApplication.class);
	
}
