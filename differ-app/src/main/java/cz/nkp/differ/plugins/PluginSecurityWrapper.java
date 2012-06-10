package cz.nkp.differ.plugins;

import java.io.File;

import org.apache.log4j.Logger;

import com.vaadin.data.Item;
import com.vaadin.ui.Form;

/**
 * Wrapper for server-side handles to DifferPluginInterface's allowing safer execution of untrusted plugin code
 * by using a PluginSecurityManager to enforce the security model.
 * @author Joshua Mabrey
 * Jun 10, 2012
 */
public class PluginSecurityWrapper implements DifferPluginInterface{

	private DifferPluginInterface child;
	private PluginSecurityManager securityManager;
	private SecurityManager defaultSecurityManager;
	private static Logger LOGGER = Logger.getLogger(PluginSecurityWrapper.class);
	
	public PluginSecurityWrapper(DifferPluginInterface d){
		if(d instanceof PluginSecurityWrapper){
			LOGGER.warn("Double wrapping of PluginSecurityWrappers is occuring. This is runnable but inefficient");
		}
		child = d;
		securityManager = new PluginSecurityManager();
		defaultSecurityManager = System.getSecurityManager();
	}
	
	private void setSecurityMode(boolean enhanced){
		try{
			if(enhanced){
				System.setSecurityManager(securityManager);
			}else if(defaultSecurityManager != null){
				System.setSecurityManager(defaultSecurityManager);
			}
		}catch(SecurityException e){
			LOGGER.error("Unable to modify system SecurityManager. Untrusted code may run without safeguards.");
		}
	}
	
	@Override
	public String getName() {
		setSecurityMode(true);
		String child_response = "";
		try{
			 child_response = child.getName();
		}catch(SecurityException se){
			LOGGER.info("GetName executed disallowed code",se);
		}
		setSecurityMode(false);
		return child_response;
	}

	@Override
	public void addFiles(File... file) {
		setSecurityMode(true);
		try{
			child.addFiles(file);
		}catch(SecurityException se){
			LOGGER.info(child.getName() + " executed disallowed code",se);
		}		
		setSecurityMode(false);		
	}

	@Override
	public int getMinimumNumberOfImagesRequired() {
		setSecurityMode(true);
		int child_response = 0;
		try{
			child_response = child.getMinimumNumberOfImagesRequired();
		}catch(SecurityException se){
			LOGGER.info(child.getName() + " executed disallowed code",se);
		}
		 
		setSecurityMode(false);
		return child_response;
	}

	@Override
	public Form getPluginSettingsFormBean() {
		setSecurityMode(true);
		Form child_response = null;
		try{
			child_response = child.getPluginSettingsFormBean();
		}catch(SecurityException se){
			LOGGER.info(child.getName() + " executed disallowed code",se);
		}
		setSecurityMode(false);
		return child_response;
	}

	@Override
	public void setPluginSettingsFormItem(Item i) {
		setSecurityMode(true);
		try{
			child.setPluginSettingsFormItem(i);
		}catch(SecurityException se){
			LOGGER.info(child.getName() + " executed disallowed code",se);
		}
		setSecurityMode(false);	
	}

	@Override
	public Throwable getErrorState() {
		setSecurityMode(true);
		Throwable child_response = null;
		try{
			child_response = child.getErrorState();
		}catch(SecurityException se){
			LOGGER.info(child.getName() + " executed disallowed code",se);
		}
		setSecurityMode(false);
		return child_response;
	}

	@Override
	public void setLogger(Logger logger) {
		setSecurityMode(true);
		try{
			child.setLogger(logger);
		}catch(SecurityException se){
			LOGGER.info(child.getName() + " executed disallowed code",se);
		}		
		setSecurityMode(false);	
	}

}
