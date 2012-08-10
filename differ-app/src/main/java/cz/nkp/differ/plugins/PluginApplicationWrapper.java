package cz.nkp.differ.plugins;

import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.vaadin.Application;
import com.vaadin.service.ApplicationContext;
import com.vaadin.terminal.ApplicationResource;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.Terminal;
import com.vaadin.ui.Window;

public class PluginApplicationWrapper extends Application{

	private static SecurityException se = new SecurityException("A plugin attempted an unauthorized action involving the Application handle.");
	private static Logger LOGGER = Logger.getLogger(PluginSecurityWrapper.class);
		
	private Application parent;
	
	public PluginApplicationWrapper(){
		throwError("Default Constructor");
	}
	
	public PluginApplicationWrapper(Application a){
		parent = a;
	}
	
	private static void throwError(String name){
		LOGGER.warn("PluginApplicationWrapper stopped an unauthorized action in "+ name +"()!",se);
		throw se;
	}
	
	public Locale getLocale() {
        return parent.getLocale();
    }
	
	public void addResource(ApplicationResource resource) {
    	parent.addResource(resource);
    }
	
	@Deprecated
    public String getRelativeLocation(ApplicationResource resource) {
    	return parent.getRelativeLocation(resource);
    }
	
	public Window getMainWindow() {
    	return parent.getMainWindow();
    }
	

    public ApplicationContext getContext() {
    	return parent.getContext();
    }

	
	public Window getWindow(String name) {
		 throwError("getWindow");
		 return null;
	}

    public void addWindow(Window window) throws IllegalArgumentException,
            NullPointerException {
    	throwError("addWindow");
    }

    private void fireWindowAttachEvent(Window window) {
    	throwError("fireWindowAttachEvent");
    }

    public void removeWindow(Window window) {
    	throwError("removeWindow");
    }

    private void fireWindowDetachEvent(Window window) {
    	throwError("fireWindowDetachEvent");
    }

    public Object getUser() {
    	throwError("getUser");
		return null;
    }

    public void setUser(Object user) {
    	throwError("setUser");
    }
    public URL getURL() {
    	throwError("getURL");
    	return null;
    }

    public void close() {
    	throwError("close");
    }
    public void start(URL applicationUrl, Properties applicationProperties,
            ApplicationContext context) {
    	throwError("start");
    }

    public boolean isRunning() {
    	throwError("isRunning");
    	return false;
    }

    public Collection<Window> getWindows() {
    	throwError("getWindows");
    	return null;
    }

    public void init() {
    	throwError("init");
	}

    public String getTheme() {
    	throwError("getTheme");
    	return null;
    }

    public void setTheme(String theme) {
    	throwError("setTheme");
    }

    public void setMainWindow(Window mainWindow) {
    	throwError("setMainWindow");
    }

   
    public Enumeration<?> getPropertyNames() {
    	throwError("getPropertyNames");
    	return null;
    }

   
    public String getProperty(String name) {
    	throwError("getProperty");
    	return null;
    }
   
    public void removeResource(ApplicationResource resource) {
    	throwError("removeResource");
    }

    @Deprecated
    public DownloadStream handleURI(URL context, String relativeUri) {
    	throwError("handleURI");
    	return null;
    }    

    public void setLocale(Locale locale) {
    	throwError("setLocale");
    }
    
    public void addListener(WindowAttachListener listener) {
    	throwError("addListener");
    }
    
    public void removeListener(WindowAttachListener listener) {
    	throwError("removeListener");
    }
    
    public String getLogoutURL() {
    	throwError("getLogoutURL");
    	return null;
    }
    public void setLogoutURL(String logoutURL) {
    	throwError("setLogoutURL");
    }

    public static SystemMessages getSystemMessages() {
    	throwError("getSystemMessages");
    	return null;
    }

    public void terminalError(Terminal.ErrorEvent event) {
    	throwError("terminalError");
    }

    public String getVersion() {
    	throwError("getVersion");
    	return null;
    }
    public Terminal.ErrorListener getErrorHandler() {
    	throwError("getErrorHandler");
    	return null;
    }

    public void setErrorHandler(Terminal.ErrorListener errorHandler) {
    	throwError("setErrorHandler");
    }
}
