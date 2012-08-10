package cz.nkp.differ.plugins;

import java.io.File;

import org.apache.log4j.Logger;

import com.vaadin.Application;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Window;

import cz.nkp.differ.plugins.compare.io.ImageFileAnalysisContainer;

public class ComparePluginInterface implements DifferPluginInterface{

	public static Logger LOGGER = Logger.getRootLogger();
	
	private ImageFileAnalysisContainer iFAC1,iFAC2;
	private static Application application = null;
	private PluginPollingThread currentThread;
	
	@Override
	public String getName() {
		return "Compare";
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
		if(file.length != 2){
			LOGGER.warn("Invalid number of files given to Compare Plugin." +
					" Should have given 2 files, gave " + file.length + " files");
			return;
		}
		iFAC1 = new ImageFileAnalysisContainer(file[0],this);
		iFAC2 = new ImageFileAnalysisContainer(file[1],this);
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
		if(iFAC1 == null || iFAC2 == null){
			showSeriousError("Cannot call getPluginDisplayComponent before setting plugin file inputs successfully");
		}
		
		HorizontalLayout layout = new HorizontalLayout();
		c.setCompleted(10);
		LOGGER.trace("Getting first component");
		layout.addComponent(iFAC1.getComponent());
		
		c.setCompleted(40);
		LOGGER.trace("Getting second component");
		layout.addComponent(iFAC2.getComponent());
		
		c.setCompleted(70);
		
		String[] hashes = {
			iFAC1.processor.getImageMD5(),	
			iFAC2.processor.getImageMD5()
		};
		
		ImageFileAnalysisContainer iFAC3 = ImageFileAnalysisContainer.getCombinationContainer(iFAC1, iFAC2,hashes);
		layout.addComponent(iFAC3.getComponent());
		
		c.setCompleted(100);
		return layout;
	}

	@SuppressWarnings("static-access")
	@Override
	public void setLogger(Logger logger) {
		LOGGER = logger.getLogger(ComparePluginInterface.class);
	}

	@Override
	public void setApplication(Application application) {
		this.application = application;		
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
