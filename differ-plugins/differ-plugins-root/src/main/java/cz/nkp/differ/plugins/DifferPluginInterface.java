package cz.nkp.differ.plugins;

import java.io.File;

import com.vaadin.Application;
import com.vaadin.ui.Component;

import org.apache.log4j.Logger;

/**
 * Interface exposed by Differ Plugin Implementations. The PluginManager searches for jars in the plugin folder,
 * and any appropriately configured jar that identifies an implementation of this interface is considered a plugin.
 * @author Joshua Mabrey
 * Jun 7, 2012
 */
public interface DifferPluginInterface{
	
	public enum PluginType{
		ImageProcessing
	}
	
	
	
	PluginType getType();
	/**
	 * Return the name of the plugin.
	 * @return
	 */
	String getName();
	
	/**
	 * Add any number of files. These method must erase all previous file handles from memory.
	 * @return
	 */
	void addFiles(File... file);
	
	/**
	 * Set the callback that should be notified of plugin progress and completion
	 * @return
	 */
	void setPluginDisplayComponentCallback(PluginComponentReadyCallback c);
		
	/**
	 * Called by the Differ runtime to set the plugins logger instance. The plugin SHOULD ONLY use this logger
	 * to log any output data.
	 * @param logger
	 */
	void setLogger(Logger logger);
	
	/**
	 * Called by the Differ runtime to set the plugins application instance.
	 * @param logger
	 */
	void setApplication(Application application);
}