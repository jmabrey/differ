package cz.nkp.differ.plugins;

import java.io.File;

import com.vaadin.ui.Component;

import org.apache.log4j.Logger;

/**
 * Interface exposed by Differ Plugin Implementations. The PluginManager searches for jars in the plugin folder,
 * and any appropriately configured jar that identifies an implementation of this interface is considered a plugin.
 * @author Joshua Mabrey
 * Jun 7, 2012
 */
public interface DifferPluginInterface{
	
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
	 * Return the plugins desired display positioning as a non-negative integer. Larger numbers are positioned further down the queue of plugins
	 * when the application prepares them for display.  If the response is negative then the positioning is Integer.MAX_VALE;
	 * @return
	 */
	int getDesiredPosition();
	
	/**
	 * Return a Component that will be bound by the differ runtime into the application.
	 * @return
	 */
	Component getPluginDisplayComponent();
	
	/**
	 * Called by the Differ runtime to set the plugins logger instance. The plugin SHOULD ONLY use this logger
	 * to log any output data.
	 * @param logger
	 */
	void setLogger(Logger logger);
}