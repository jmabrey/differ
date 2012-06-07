package cz.nkp.differ.plugins;

import java.io.File;

import com.vaadin.data.Item;
import com.vaadin.ui.Form;
import org.apache.log4j.Logger;

/**
 * Interface exposed by Differ Plugin Implementations. The PluginManager searches for jars in the plugin folder,
 * and any appropriately configured jar that identifies an implementation of this interface is considered a plugin.
 * @author Joshua Mabrey
 * Jun 7, 2012
 */
public interface DifferPluginInterface {
	
	/**
	 * Return the name of the plugin.
	 * @return
	 */
	String getName();
	
	/**
	 * Add any number of files. Will set ErrorState if invalid or null files are given.
	 * @return
	 */
	void addFiles(File... file);
	
	/**
	 * Return the minimum number of images required for this plugin. A comparison plugin for instance, might require
	 * a minimum of two images.
	 * @return
	 */
	int getMinimumNumberOfImagesRequired();
	
	/**
	 * Return a Form that will be bound by the differ runtime into a plugin settings dialog.
	 * @return
	 */
	Form getPluginSettingsFormBean();
	
	/**
	 * The callback implementation of the plugin settings dialog
	 * @param i
	 */
	void setPluginSettingsFormItem(Item i);
	
	
	/**
	 * Should return null if no errors occur. The plugin should swallow all errors to a log obtained by the runtime
	 * call of setLogger(). The Differ runtime will call this method after fetching the service and after calling
	 * getDataRepresentation(). If an error occurs the Differ runtime will handle the error without any
	 * guarantees. Under NO CIRCUMSTANCE should a plugin attempt to close the current runtime.
	 * 
	 * @return
	 */
	Throwable getErrorState();
	
	/**
	 * Called by the Differ runtime to set the plugins logger instance. The plugin SHOULD ONLY use this logger
	 * to log any output data, and the plugin's code should be modified if needed to make this possible.
	 * @param logger
	 */
	void setLogger(Logger logger);
}
