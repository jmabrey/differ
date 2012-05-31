package cz.nkp.differ.plugins;

import javax.jcr.Binary;

import org.slf4j.Logger;

import com.vaadin.ui.Component;

public interface DifferPlugin {
	
	/**
	 * Takes a JCR Binary stream resource and processes it through the plugin implementation. The plugin 
	 * must return a Vaadin component, and is free to format that component however the plugin wishes.
	 * Generally the plugin should minimize the space of the plugin's returned data. The Differ main
	 * application is not guaranteed to protect the component returned by the plugin, and may add, delete,
	 * or resize components returned. Plugins SHOULD NOT RETURN NULL if they receive a null input Binary,
	 * instead they should return a component indicating an error, and configure the getErrorState() method
	 * to return appropriately. Any custom plugin will receive it's own layout physically separated from
	 * the default custom plugins.
	 * @param fileBinary
	 * @return
	 */
	Component getDataRepresentation(Binary fileBinary);
	
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
