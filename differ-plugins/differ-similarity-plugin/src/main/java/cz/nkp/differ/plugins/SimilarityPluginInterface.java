package cz.nkp.differ.plugins;

import java.io.File;

import org.apache.log4j.Logger;

import com.vaadin.Application;
import com.vaadin.ui.Component;
import com.vaadin.ui.Window;


public class SimilarityPluginInterface implements DifferPluginInterface{

	public static Logger LOGGER = Logger.getRootLogger();
	
	private static Application application = null;
	private PluginPollingThread currentThread;
	
	@Override
	public String getName() {
		return "Similarity";
	}
	
	public DifferPluginInterface.PluginType getType(){
		return DifferPluginInterface.PluginType.ImageProcessing;
	}
	
	public static void showSeriousError(String message){
		Window.Notification errorNotif = new Window.Notification("Plugin Error", 
				"A runtime error has occured while executing a plugin. Plugin operation halted. Message: " + message, 
				Window.Notification.TYPE_ERROR_MESSAGE);
		
		application.getMainWindow().showNotification(errorNotif);
	}
	
	@Override
	public void addFiles(File... file) {
		if(file.length != 1){
			LOGGER.warn("Invalid number of files given to Similarity Plugin." +
					" Should have given 1 files, gave " + file.length + " files");
			return;
		}
	}
	
	@Override
	public void setPluginDisplayComponentCallback(final PluginComponentReadyCallback c) {
		try {
			currentThread = new PluginPollingThread(this,c);
			currentThread.start();
		} catch (Exception e) {
			showSeriousError(e.getLocalizedMessage());
		}
	}

	public Component getPluginDisplayComponent(PluginComponentReadyCallback c){
		
		c.setCompleted(10);
		
		c.setCompleted(40);
		
		c.setCompleted(70);
		
		c.setCompleted(100);
		
		return null;
	}

	@SuppressWarnings("static-access")
	@Override
	public void setLogger(Logger logger) {
		LOGGER = logger.getLogger(SimilarityPluginInterface.class);
	}

	@Override
	public void setApplication(Application application) {
		SimilarityPluginInterface.application = application;		
	}

	@Override
	public Logger getLogger() {
		if(LOGGER == null){
			try {
				showSeriousError("Plugin must be provided with an application instance!");
			} catch (Exception e) {
				return null;
			}
		}
		
		return LOGGER;
	}
	
	public Application getApplication() {
		if(application == null){
			try {
				showSeriousError("Plugin must be provided with an application instance!");
			} catch (Exception e) {
				return null;
			}
		}
		
		return application;
	}
}
