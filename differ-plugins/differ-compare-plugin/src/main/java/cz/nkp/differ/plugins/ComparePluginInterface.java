package cz.nkp.differ.plugins;

import java.io.File;
import java.io.IOException;

import org.apache.commons.imaging.ImageReadException;
import org.apache.log4j.Logger;

import com.vaadin.Application;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

import cz.nkp.differ.plugins.compare.io.ImageFileAnalysisContainer;

public class ComparePluginInterface implements DifferPluginInterface{

	public static Logger LOGGER = Logger.getRootLogger();
	private ImageFileAnalysisContainer iFAC1,iFAC2;
	private static Application application = null;
	private Thread currentThread;
	
	@Override
	public String getName() {
		return "Compare Plugin";
	}
	
	public DifferPluginInterface.PluginType getType(){
		return DifferPluginInterface.PluginType.ImageProcessing;
	}
	
	@Override
	public void addFiles(File... file) {
		if(file.length != 2){
			LOGGER.warn("Invalid number of files given to Compare Plugin." +
					" Should have given 2 files, gave " + file.length + " files");
			return;
		}
		try {
			iFAC1 = new ImageFileAnalysisContainer(file[0]);
			iFAC2 = new ImageFileAnalysisContainer(file[1]);
		} catch (IOException e) {
			LOGGER.error("Unable to process files: " + e);
		}
	}
	
	@Override
	public void setPluginDisplayComponentCallback(final PluginComponentReadyCallback c) {
		
		if(currentThread != null){
			currentThread.interrupt();
		}
		
		Thread thread = new Thread(){

			@Override
			public void run() {
				boolean continueRunning = true;
				while(!this.isInterrupted()){
					try{
						Component comp;
						comp = getPluginDisplayComponent(c);
						if(comp!= null){
							c.ready(comp);
							break;
						}
						wait(1000);
					}catch(InterruptedException e){}
				}
				LOGGER.info("Thread stopped");
			}			
		};
		
		currentThread = thread;
		currentThread.start();
	}

	private Component getPluginDisplayComponent(PluginComponentReadyCallback c) {
		if(iFAC1 == null || iFAC2 == null){
			LOGGER.error("Cannot call getPluginDisplayComponent before setting plugin file inputs successfully");
			return null;
		}
				
		try {
			HorizontalLayout layout = new HorizontalLayout();
			c.setCompleted(10);
			LOGGER.info("Getting first component");
			layout.addComponent(iFAC1.getComponent());
			c.setCompleted(40);
			LOGGER.info("Getting second component");
			layout.addComponent(iFAC2.getComponent());
			c.setCompleted(70);
			//ImageFileAnalysisContainer iFAC3 = ImageFileAnalysisContainer.getCombinationImageFileAnalysis(iFAC1, iFAC2);
			//layout.addComponent(iFAC3.getComponent());
			c.setCompleted(100);
			return layout;
		} catch (ImageReadException e) {
			LOGGER.error("Unable to create component! ",e);
			c.setCompleted(0);
			return null;
		} catch (IOException e) {
			LOGGER.error("Unable to create component! ",e);
			c.setCompleted(0);
			return null;
		}
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
	
	public static Application getApplication(){
		if(application == null){
			LOGGER.error("You must provide the plugin with an application instance!");
		}
		
		return application;
	}
}
