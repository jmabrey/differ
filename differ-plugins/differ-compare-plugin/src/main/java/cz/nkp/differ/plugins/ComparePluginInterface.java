package cz.nkp.differ.plugins;

import java.io.File;
import java.io.IOException;

import org.apache.commons.imaging.ImageReadException;
import org.apache.log4j.Logger;

import com.vaadin.Application;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

import cz.nkp.differ.plugins.compare.io.ImageFileAnalysisContainer;

public class ComparePluginInterface implements DifferPluginInterface{

	public static Logger LOGGER = Logger.getRootLogger();
	private ImageFileAnalysisContainer iFAC1,iFAC2;
	private static Application application = null;
	
	@Override
	public String getName() {
		return "Compare Plugin";
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
	public Component getPluginDisplayComponent() {
		if(iFAC1 == null || iFAC2 == null){
			LOGGER.error("Cannot call getPluginDisplayComponent before setting plugin file inputs successfully");
			return new Label("There was an error.");
		}
				
		try {
			HorizontalLayout layout = new HorizontalLayout();
			
			layout.addComponent(iFAC1.getComponent());
			layout.addComponent(iFAC2.getComponent());
			
			ImageFileAnalysisContainer iFAC3 = ImageFileAnalysisContainer.getCombinationImage(iFAC1, iFAC2);
			layout.addComponent(iFAC3.getComponent());
			return layout;
		} catch (ImageReadException e) {
			LOGGER.error("Unable to create component! ",e);
			return new Label("There was an error.");
		} catch (IOException e) {
			LOGGER.error("Unable to create component! ",e);
			return new Label("There was an error.");
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
