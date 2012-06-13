package cz.nkp.differ.plugins;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


import cz.nkp.differ.plugins.DifferPluginInterface;
import cz.nkp.differ.util.GeneralHelperFunctions;

/**
 * Finds loads and maintains plugins throughout the session lifecycle.
 * @author Joshua Mabrey
 * Jun 10, 2012
 */
public class PluginManager {
	
	private static Logger LOGGER = Logger.getLogger(PluginManager.class);
	private static PluginManager _instance;
	
	private PluginManager(){
	}
	
	public static final PluginManager getInstance(){
		if(_instance == null){
			_instance = new PluginManager();
			_instance.load();
		}
		
		return _instance;
	}
	
	public DifferPluginInterface[] getPlugins(){
		return pluginClasses.toArray(new DifferPluginInterface[0]);
	}
	
	public void load(){
		String directory = System.getProperty("cz.differ.plugins.directory");
		if(directory == null){
			directory = "./plugins";//if the directory isn't manually specified then subdirectory plugins is default
		}
		
		File f = new File(directory);
		
		LOGGER.trace("Attempting to load plugins from directory at: " + f.getAbsolutePath());
		
		if(!f.canRead()){
			LOGGER.error("Unable to read from plugin directory!");
			return;
		}
		
		for(File file : f.listFiles()){
			if(!file.isDirectory())
			LOGGER.trace("Found potential plugin at: " + file.getAbsolutePath());
			addFile(file);//add every file that isn't a directory in the plugins folder
		}
	}
	
	private void addFile(File f){
		if(f.isDirectory()){
			LOGGER.trace("Plugin " + f.getAbsolutePath() + " is a directory and cannot be loaded as a plugin. Loading Aborted");
			return;
		}
		if(!f.getName().toLowerCase().endsWith(".jar") && !f.getName().toLowerCase().endsWith(".war") ){
			LOGGER.warn("Plugin " + f.getAbsolutePath() + " does not end with jar or war file extension! Loading Aborted");
			return;
		}
		
		try {
			loadPluginClassFromFile(f);
		} catch (MalformedURLException e) {
			LOGGER.error("Plugin " + f.getAbsolutePath() + " has failed loading! Loading Aborted.",e);
		} catch (ClassNotFoundException e) {
			LOGGER.error("Plugin " + f.getAbsolutePath() + " has failed loading! Loading Aborted.",e);
		} catch (IllegalArgumentException e) {
			LOGGER.error("Plugin " + f.getAbsolutePath() + " has failed loading! Loading Aborted.",e);
		} catch (IllegalAccessException e) {
			LOGGER.error("Plugin " + f.getAbsolutePath() + " has failed loading! Loading Aborted.",e);
		} catch (NoSuchFieldException e) {
			LOGGER.error("Plugin " + f.getAbsolutePath() + " has failed loading! Loading Aborted.",e);
		} catch (SecurityException e) {
			LOGGER.error("Plugin " + f.getAbsolutePath() + " has failed loading! Loading Aborted.",e);
		}
	}
	
	private static List<DifferPluginInterface> pluginClasses = new ArrayList<DifferPluginInterface>();
		
	/**
	 * Attempts to dynamically load a class from a plugin file. May fail for any number of reasons.
	 * @param jarFile
	 * @throws MalformedURLException
	 * @throws ClassNotFoundException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 */
	private void loadPluginClassFromFile(File jarFile) throws MalformedURLException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{
		GeneralHelperFunctions.errorIfContainsNull(jarFile);
		URLClassLoader child = new URLClassLoader (new URL[]{jarFile.toURI().toURL()}, this.getClass().getClassLoader());
		//TODO:comment this method
		Class<?> pluginDescriptorClass = Class.forName ("cz.nkp.differ.plugins.PluginDescriptor", true, child);

		Class<?> pluginInterfaceImplementationClass = (Class<?>) pluginDescriptorClass.getField("PLUGIN_CLASS").get(new Object());
		
		Object pluginInterfaceImplObject;
		
		try {
			pluginInterfaceImplObject = pluginInterfaceImplementationClass.newInstance();
		} catch (InstantiationException e) {
			LOGGER.error("Unable to create object from PluginDescriptor class. Loading Aborted.");
			return;
		}
		
		if(!DifferPluginInterface.class.isInstance(pluginInterfaceImplObject)){
			LOGGER.warn("Plugin " + jarFile.getAbsolutePath() + " does not point to a valid interface implementation! Loading Aborted.");
		}
		else{
			DifferPluginInterface pluginInterfaceImpl = new PluginSecurityWrapper((DifferPluginInterface)pluginInterfaceImplObject);
			pluginClasses.add(pluginInterfaceImpl);
			LOGGER.trace(pluginInterfaceImpl.getName() + " loaded successfully from " +  jarFile.getAbsolutePath());
		}
		
	}	
}
