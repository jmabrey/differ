package cz.nkp.differ;

import com.vaadin.Application;
import com.vaadin.service.ApplicationContext;
import com.vaadin.terminal.gwt.server.WebApplicationContext;

import cz.nkp.differ.gui.windows.MainDifferWindow;

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
		setTheme("differ");
		//Get Application Context
		WebApplicationContext context = (WebApplicationContext) getContext();
		//Set Context Locale to Browser Locale
		setLocale(context.getBrowser().getLocale());
		
		//Add this as a listener to the context transaction event pump
		context.addTransactionListener(this);
		
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
	
	/**
	 * Global handle to allow any class to access the current Application by using the getter
	 */
	private static final ThreadLocal<DifferApplication> currentApplication = new ThreadLocal<DifferApplication>();
 
}
