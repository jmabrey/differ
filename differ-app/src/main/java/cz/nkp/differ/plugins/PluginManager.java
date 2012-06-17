package cz.nkp.differ.plugins;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;


import cz.nkp.differ.DifferApplication;
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
	

	private static List<DifferPluginInterface> pluginClassesWrapped;
	private static List<DifferPluginInterface> pluginClassesUnwrapped;
	
	private static Map<String,Integer> pluginVersionMap;
	private static Map<String,DifferPluginInterface> pluginClassMap;
	
	private PluginManager(){
		pluginClassesWrapped = new ArrayList<DifferPluginInterface>();
		pluginClassesUnwrapped = new ArrayList<DifferPluginInterface>();
		pluginVersionMap = new HashMap<String,Integer>();
		pluginClassMap = new HashMap<String,DifferPluginInterface>();
	}
	
	public static final PluginManager getInstance(){
		if(_instance == null){
			_instance = new PluginManager();
			_instance.load();
		}
		
		return _instance;
	}
	
	public DifferPluginInterface[] getPlugins(){
		return pluginClassesWrapped.toArray(new DifferPluginInterface[0]);
	}
	
	public void load(){
		
		List<File> pluginSearchLocations = new ArrayList<File>();
		
		String directory = System.getProperty("cz.differ.plugins.directory");
		if(directory != null){
			//TODO:implement classpath style semicolon separation for multiple directories
			pluginSearchLocations.add(new File(directory));
		}
		
		File baseDirPlugins = new File(DifferApplication.getHomeDirectory() + File.separator + "plugins");
		
		pluginSearchLocations.add(baseDirPlugins);
		
		for(File dir : pluginSearchLocations){
			if(!dir.isDirectory()){
				continue;//Only examine it if it is a directory
			}
			LOGGER.trace("Attempting to load plugins from directory at: " + dir.getAbsolutePath());
			for(File file : dir.listFiles()){
				if(!file.isDirectory() && file.canRead()){
					LOGGER.trace("Found potential plugin at: " + file.getAbsolutePath());
					addFile(file);//add every file that isn't a directory in the plugins folder
				}
			}
		}
		
		//Wrap all the plugins in security wrappers
		for(Object o: pluginClassesUnwrapped){
			pluginClassesWrapped.add((DifferPluginInterface) o);
		}
		//Now we get rid of the references to the insecure classes to prevent later access
		pluginClassesUnwrapped.clear();
		pluginClassesUnwrapped = null;
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
		String pluginInterfaceImplementationClassName = pluginInterfaceImplementationClass.getName();
		Integer pluginInterfaceImplementationVersion = (Integer) pluginDescriptorClass.getField("VERSION").get(new Object());
		
		//Version Check
		if(pluginVersionMap.containsKey(pluginInterfaceImplementationClassName)){
			int loadedVersion = pluginVersionMap.get(pluginInterfaceImplementationClassName);
			if(loadedVersion < pluginInterfaceImplementationVersion){
				
				//we need to unload the current outdated version and load the new one
				Object outdatedPlugin = pluginClassMap.get(pluginInterfaceImplementationClassName);
				pluginClassesUnwrapped.remove(outdatedPlugin);
				
				//Updating the version number to the new version
				pluginClassMap.remove(pluginInterfaceImplementationClassName);
				pluginVersionMap.remove(pluginInterfaceImplementationClassName);
				pluginVersionMap.put(pluginInterfaceImplementationClassName, pluginInterfaceImplementationVersion);
			}
			else{
				LOGGER.info("Avoided loading outdated plugin from " + jarFile.getAbsolutePath());
				return;//No need to load an outdated plugin
			}
		}else{//New plugin (by class name evaluation)
			pluginVersionMap.put(pluginInterfaceImplementationClassName, pluginInterfaceImplementationVersion);
		}
		
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
			pluginClassesUnwrapped.add((DifferPluginInterface) pluginInterfaceImplObject);
			pluginClassMap.put(pluginInterfaceImplementationClassName, (DifferPluginInterface) pluginInterfaceImplObject);
		}
		
	}	
}
