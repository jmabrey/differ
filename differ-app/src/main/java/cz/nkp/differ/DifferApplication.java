package cz.nkp.differ;

import java.io.File;
import java.security.Security;
import java.util.Locale;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.vaadin.Application;
import com.vaadin.service.ApplicationContext;
import com.vaadin.terminal.gwt.server.WebApplicationContext;

import cz.nkp.differ.gui.windows.MainDifferWindow;
import cz.nkp.differ.io.DatabaseManager;
import cz.nkp.differ.plugins.PluginManager;

/**
 * The main Application instance, responsible for setting global settings, such as locale, theme, and the root window for the GUI.
 * This class also allows any code to fetch the current Application instance in a thread-safe way.
 * @author Joshua Mabrey
 * Mar 30, 2012
 */

@SuppressWarnings("serial")
public class DifferApplication extends Application implements ApplicationContext.TransactionListener{
	
	/**
	 * Called by the server to run the application and begin the session
	 */
	@Override
	public void init() {
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
	
	public void transactionStart ( Application application, Object o )
    {
        if ( application == DifferApplication.this )
        {
            currentApplication.set ( this );
        }
    }

    public void transactionEnd ( Application application, Object o )
    {
        if ( application == DifferApplication.this )
        {
            currentApplication.set ( null );
            currentApplication.remove ();
        }
    }
    
    public static DifferApplication getInstance()
    {
        return currentApplication.get ();
    }
	
    public static String getHomeDirectory(){
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
    	return differHome;
    }
    
    private static String differHome;
    
	/**
	 * Global handle to allow any class to access the current Application by using the getter
	 */
	private static final ThreadLocal<DifferApplication> currentApplication = new ThreadLocal<DifferApplication>();
 
	private static final String DIFFER_THEME_NAME = "differ";
	
	private static Logger LOGGER = Logger.getLogger(DifferApplication.class);
	
}
