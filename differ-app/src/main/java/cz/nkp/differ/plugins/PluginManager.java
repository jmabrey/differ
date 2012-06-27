package cz.nkp.differ.plugins;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cz.nkp.differ.DifferApplication;
import cz.nkp.differ.util.GeneralMacros;

/**
 * Finds loads and maintains plugins throughout the session lifecycle.
 * @author Joshua Mabrey
 * Jun 10, 2012
 */
public class PluginManager {
	
	private static Logger LOGGER = Logger.getLogger(PluginManager.class);
	private static Logger PLUGIN_LOGGER = Logger.getLogger("Plugin Logger");
	private static PluginManager _instance;
	

	private static List<PluginSecurityWrapper> pluginClassesWrapped;
	private static List<DifferPluginInterface> pluginClassesUnwrapped;
	
	private static Map<String,Integer> pluginVersionMap;
	private static Map<String,DifferPluginInterface> pluginClassMap;
	
	private PluginManager(){
		pluginClassesWrapped = new ArrayList<PluginSecurityWrapper>();
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
	
	/**
	 * Syntatic sugar for loading plugins
	 */	
	public static final void loadPlugins(){
		getInstance();
	}
	
	public DifferPluginInterface[] getPlugins(){
		//Collections.sort(pluginClassesWrapped, new DifferPluginInterfaceComparator());
		return pluginClassesWrapped.toArray(new DifferPluginInterface[0]);
	}
	
	private void load(){		
		for(File dir : getPluginSearchLocations()){
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
			pluginClassesWrapped.add(new PluginSecurityWrapper((DifferPluginInterface) o));
		}
		
		//Now we get rid of the references to the insecure classes to prevent later access
		pluginClassesUnwrapped.clear();
		pluginClassesUnwrapped = null;
		
		setLoggers();
	}
	
	private static final File[] getPluginSearchLocations(){
		List<File> pluginSearchLocations = new ArrayList<File>();
		
		String directory = System.getProperty("cz.differ.plugins.directory");
		if(directory != null){
			//TODO:implement classpath style semicolon separation for multiple directories
			pluginSearchLocations.add(new File(directory));
		}
		
		File baseDirPlugins = new File(DifferApplication.getHomeDirectory() + File.separator + "plugins");
		
		pluginSearchLocations.add(baseDirPlugins);
		
		return pluginSearchLocations.toArray(new File[0]);
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
		GeneralMacros.errorIfContainsNull(jarFile);
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
			pluginVersionMap.put(pluginInterfaceImplementationClassName, pluginInterfaceImplementationVersion);
			pluginClassMap.put(pluginInterfaceImplementationClassName, (DifferPluginInterface) pluginInterfaceImplObject);
		}
	}	
	
	@SuppressWarnings("static-access")
	/**
	 * Sets the plugin loggers. Static Access suppressed to inherit logger from Plugin Logger root.
	 */
	private static void setLoggers(){
		
		for(DifferPluginInterface dfi : pluginClassesWrapped){
			dfi.setLogger(PLUGIN_LOGGER.getLogger(dfi.getName()));
		}
	}
}

class DifferPluginInterfaceComparator implements Comparator<DifferPluginInterface>{

	@Override
	public int compare(DifferPluginInterface d1, DifferPluginInterface d2) {
				
		if(d1.getDesiredPosition() < d2.getDesiredPosition()){
			//d1 comes first
			return 1;
		}
		else if(d2.getDesiredPosition() < d1.getDesiredPosition()){
			//d2 comes first
			return -1;
		}
		else return 0;//equal
	}
	
}
