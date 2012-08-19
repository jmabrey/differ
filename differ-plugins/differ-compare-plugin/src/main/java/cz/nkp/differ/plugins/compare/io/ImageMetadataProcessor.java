package cz.nkp.differ.plugins.compare.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import cz.nkp.differ.plugins.ComparePluginInterface;
import cz.nkp.differ.plugins.compare.io.FileLoader.FileType;
import cz.nkp.differ.plugins.tools.CommandHelper;

public class ImageMetadataProcessor {
	private static Logger LOGGER = ComparePluginInterface.LOGGER;
		
	private File file = null;
	private FileLoader.FileType fileType = FileLoader.FileType.OTHER;
	
	private Label kduLabel;
	
	public ImageMetadataProcessor(File file, FileLoader.FileType type){
		this.file = file;
		this.fileType = type;
		
		kduLabel = new Label("KDU: Generating...");
	}
	
	private static CommandHelper.CommandInfo getJHoveCommand(File imageFile) throws IOException{
		//jhove.app.location //home//xrosecky//jhove//bin//JhoveApp.jar
		//jhove.conf.location //home//xrosecky//jhove//conf//jhove.conf
		
		if(imageFile == null || !imageFile.exists()){
			throw new IOException("Either the image file is null or non-existant!");
		}
		
		String javaHome = System.getProperty("java.home");
		String jhoveAppLoc = System.getProperty("jhove.app.location");
		
		String jhoveHome =  System.getProperty("user.home") + "/.differ/resources/jhove";
		
		if(jhoveAppLoc == null){
			jhoveAppLoc = jhoveHome + "/JhoveApp.jar"; 
		}
		
		String jhoveConfLoc = System.getProperty("jhove.conf.location");
		if(jhoveConfLoc == null){
			jhoveConfLoc = jhoveHome +  "/jhove.conf"; 
		}
		
		CommandHelper.CommandInfo info = new CommandHelper.CommandInfo();
		
		ArrayList<String> commands = new ArrayList<String>();
		
		commands.add("java");
		commands.add("-jar");
		commands.add(jhoveAppLoc);
		commands.add("-h");
		commands.add("xml");
		commands.add(imageFile.getCanonicalPath());
		commands.add("-c");
		commands.add(jhoveConfLoc);
		
		info.workingDir = javaHome + File.separator +"bin" + File.separator;
		info.commands = commands.toArray(new String[0]);
		
		LOGGER.trace("JHove Command: " + Arrays.toString(info.commands));
		
		return info;
	}
	
	private static CommandHelper.CommandInfo getKDUCommand(File imageFile) throws IOException{
		
		if(imageFile == null || !imageFile.exists()){
			throw new IOException("Either the image file is null or non-existant!");
		}
		
		
		
		String kduHome =  System.getProperty("user.home") + "/.differ/resources/kdu";
		
		//Determine OS
		String os_name = System.getProperty("os.name").toLowerCase();
		String kdu_binary_name;
		
		if(os_name.indexOf("nix") >= 0 || os_name.indexOf("nux") >= 0){
			kduHome += "/linux";
			kdu_binary_name = "kdu_expand";
		}
		else if(os_name.indexOf("win") >= 0){
			kduHome += "/windows";
			kdu_binary_name = "kdu_expand";
		}
		else{
			throw new IOException("Cannot determine OS type for kdu_expand command");
		}
		
		
		
		String kduAppLoc = System.getProperty("kdu_expand.app.location");
		
		if(kduAppLoc == null){
			kduAppLoc = kduHome;
		}
		
		CommandHelper.CommandInfo info = new CommandHelper.CommandInfo();
		
		ArrayList<String> commands = new ArrayList<String>();
		
		commands.add(kdu_binary_name);
		commands.add("-record");
		commands.add("-quiet");
		commands.add("/dev/stdout");
		commands.add("-i");
		commands.add(imageFile.getCanonicalPath());
		
		info.workingDir = kduAppLoc;
		info.commands = commands.toArray(new String[0]);
		
		LOGGER.trace("kdu_expand Command: " + Arrays.toString(info.commands));		
		
		return info;
	}
	
	public Component getMetadata(){
		CommandHelper.CommandInfo kduExpand = null,jhove= null;
		try {
			kduExpand = getKDUCommand(file);
			jhove = getJHoveCommand(file);
		} catch (IOException e) {
			LOGGER.error("Unable to generate metadata commands", e);
			return new Label("Unable to generate metadata");
		}		
		
		VerticalLayout layout = new VerticalLayout();
		
		
		
		String[] jhoveTags = new String[]{"status","format"};
		
		if(fileType == FileType.JPEG2000){
			jhoveTags = new String[]{"status","format","size","mimeType","mix:compressionScheme","mix:imageWidth","mix:imageHeight","mix:bitsPerSampleValue","mix:samplesPerPixel"};
		}
		
		
		XmlTableCallback jhoveCallback = new XmlTableCallback(LOGGER,jhoveTags);
		CommandHelper jhoveCommand = new CommandHelper("JHOVE",jhove, jhoveCallback,LOGGER);	
		layout.addComponent(jhoveCallback.getTable());
		jhoveCommand.start();			
		
		/*if(fileType == FileType.JPEG2000){
			CommandHelper kduCommand = new CommandHelper("KDU",kduExpand,new labelCaptionCallback(kduLabel), LOGGER);
			kduCommand.start();
			layout.addComponent(kduLabel);	
		}*/
		
		return layout;
	}
}
