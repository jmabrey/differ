package cz.nkp.differ.plugins.compare.io;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import cz.nkp.differ.plugins.ComparePluginInterface;

public class ImageMetadataProcessor {
	private static Logger LOGGER = ComparePluginInterface.LOGGER;
		
	private File file = null;
	private FileLoader.FileType fileType = FileLoader.FileType.OTHER;
	
	public ImageMetadataProcessor(File file, FileLoader.FileType type){
		this.file = file;
		this.fileType = type;
	}
	
	private static String getJHoveCommand(File imageFile) throws IOException{
		//jhove.app.location //home//xrosecky//jhove//bin//JhoveApp.jar
		//jhove.conf.location //home//xrosecky//jhove//conf//jhove.conf
		
		if(imageFile == null || !imageFile.exists()){
			throw new IOException("Either the image file is null or non-existant!");
		}
		
		String javaHome = System.getProperty("java.home");
		String jhoveAppLoc = System.getProperty("jhove.app.location");
		String jhoveConfLoc = System.getProperty("jhove.conf.location");
		
		if(javaHome == null || jhoveAppLoc == null || jhoveConfLoc == null){
			throw new IOException("Unable to form Jhove command for metadata extraction." +
					" Make sure that jhove.app.location and jhove.conf.location are set correctly");
		}
		
		String command = javaHome + File.separator + "bin" + File.separator + "java -jar " + jhoveAppLoc + " -h xml "+ 
							imageFile.getCanonicalPath() +" -c " + jhoveConfLoc;
		
		LOGGER.trace("JHove Command: " + command);
		
		return command;
	}
	
	private static String getKDUCommand(File imageFile) throws IOException{
		
		if(imageFile == null || !imageFile.exists()){
			throw new IOException("Either the image file is null or non-existant!");
		}
		
		String kduAppLoc = System.getProperty("kdu_expand.app.location");
		
		if(kduAppLoc == null){
			throw new IOException("Unable to form kdu_expand command for metadata extraction." +
					" Make sure that kduExpand.app.location is set correctly");
		}
		String command = kduAppLoc + " -record -quiet /dev/stdout  -i " + imageFile.getCanonicalPath();
		
		LOGGER.trace("kdu_expand Command: " + command);
		
		return command;
	}
	
	public Component getMetadata(){
		String kduExpandString = null, jhoveString= null;
		try {
			kduExpandString = getKDUCommand(file);
			jhoveString = getJHoveCommand(file);
		} catch (IOException e) {
			LOGGER.error("Unable to generate metadata", e);
			return new Label("Unable to generate metadata");
		}		
		
		VerticalLayout layout = new VerticalLayout();
		CommandHelper kduCommand = new CommandHelper(kduExpandString);
		CommandHelper jhoveCommand = new CommandHelper(jhoveString);

		kduCommand.start();
		jhoveCommand.start();
		
		try {
			layout.addComponent(new Label("KDU: " + kduCommand.getMessage()));
		} catch (IOException e) {
			LOGGER.error("Unable to generate kdu_expand metadata", e);
			layout.addComponent(new Label("kdu error"));
		}
		
		try {
			layout.addComponent(new Label("JHOVE: " + jhoveCommand.getMessage()));
		} catch (IOException e) {
			LOGGER.error("Unable to generate jhove metadata", e);
			layout.addComponent(new Label("Jhove error"));
		}		
		
		return layout;
	}
}
