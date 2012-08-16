package cz.nkp.differ.plugins.compare.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import cz.nkp.differ.plugins.CommandHelper;
import cz.nkp.differ.plugins.ComparePluginInterface;

public class ImageMetadataProcessor {
	private static Logger LOGGER = ComparePluginInterface.LOGGER;
		
	private File file = null;
	private FileLoader.FileType fileType = FileLoader.FileType.OTHER;
	
	public ImageMetadataProcessor(File file, FileLoader.FileType type){
		this.file = file;
		this.fileType = type;
	}
	
	private static CommandHelper.CommandInfo getJHoveCommand(File imageFile) throws IOException{
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
		
		CommandHelper.CommandInfo info = new CommandHelper.CommandInfo();
		
		ArrayList<String> commands = new ArrayList<String>();
		
		commands.add("java");
		commands.add("-jar " + jhoveAppLoc);
		commands.add("-h xml");
		commands.add(imageFile.getCanonicalPath());
		commands.add("-c jhoveConfLoc");
		
		info.workingDir = javaHome + File.separator +"bin" + File.separator;
		info.commands = commands.toArray(new String[0]);
		
		LOGGER.info("JHove Command: " + Arrays.toString(info.commands));
		
		return info;
	}
	
	private static CommandHelper.CommandInfo getKDUCommand(File imageFile) throws IOException{
		
		if(imageFile == null || !imageFile.exists()){
			throw new IOException("Either the image file is null or non-existant!");
		}
		
		String kduAppLoc = System.getProperty("kdu_expand.app.location");
		
		if(kduAppLoc == null){
			throw new IOException("Unable to form kdu_expand command for metadata extraction." +
					" Make sure that kduExpand.app.location is set correctly");
		}
		
		CommandHelper.CommandInfo info = new CommandHelper.CommandInfo();
		
		ArrayList<String> commands = new ArrayList<String>();
		
		commands.add("kdu_expand");
		commands.add("-record");
		commands.add("-quiet");
		commands.add("/dev/stdout");
		commands.add("-i " + imageFile.getCanonicalPath());
		
		info.workingDir = kduAppLoc;
		info.commands = commands.toArray(new String[0]);
		
		LOGGER.info("kdu_expand Command: " + Arrays.toString(info.commands));		
		
		return info;
	}
	
	public Component getMetadata(){
		CommandHelper.CommandInfo kduExpand = null,jhove= null;
		try {
			kduExpand = getKDUCommand(file);
			jhove = getJHoveCommand(file);
		} catch (IOException e) {
			LOGGER.error("Unable to generate metadata", e);
			return new Label("Unable to generate metadata");
		}		
		
		VerticalLayout layout = new VerticalLayout();
		CommandHelper kduCommand = new CommandHelper(kduExpand,LOGGER);
		CommandHelper jhoveCommand = new CommandHelper(jhove,LOGGER);

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
