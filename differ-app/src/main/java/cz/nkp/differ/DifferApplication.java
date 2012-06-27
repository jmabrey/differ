package cz.nkp.differ;

import java.io.File;
import java.security.Security;
import java.util.Locale;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.vaadin.terminal.gwt.server.WebApplicationContext;

import cz.nkp.differ.gui.windows.MainDifferWindow;
import cz.nkp.differ.io.DatabaseManager;
import cz.nkp.differ.plugins.PluginManager;
import eu.livotov.tpt.TPTApplication;

/**
 * The main Application instance, responsible for setting global settings, such as locale, theme, and the root window for the GUI.
 * This class also allows any code to fetch the current Application instance in a thread-safe way.
 * @author Joshua Mabrey
 * Mar 30, 2012
 */

@SuppressWarnings("serial")
public class DifferApplication extends TPTApplication{
	
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
		LOGGER.debug("Session Locale: " + locale.getDisplayName());
		
		//Add this as a listener to the context transaction event pump
		context.addTransactionListener(this);
		
		DatabaseManager.getInstance();//Attempts to load/create an embedded database
		
		PluginManager.getInstance();//Attempts to find and dynamically load all plugins
		
		MainDifferWindow mainWindow = new MainDifferWindow();
		setMainWindow(mainWindow);
	}
	
	@Override
	public void firstApplicationStartup() {
		// TODO Auto-generated method stub
		
	}
		
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
    	}
    	
    	File homeDir = new File(differHome);
    	
    	if(!homeDir.exists()){
    		LOGGER.error("Differ home directory unable to be created at " + homeDir.getAbsolutePath());
    	}
    	
    	return homeDir;
    }
    
    private static String differHome;
 
	private static final String DIFFER_THEME_NAME = "differ";
	
	private static Logger LOGGER = Logger.getLogger(DifferApplication.class);
	
}
