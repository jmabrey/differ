package cz.nkp.differ.plugins;

import java.security.Permission;

import org.apache.log4j.Logger;

/**
 * The SecurityManager to be enforced while running untrusted plugin code.
 * @author Joshua Mabrey
 * Jun 10, 2012
 */
public class PluginSecurityManager extends SecurityManager{
	
      private static Logger LOGGER = Logger.getLogger(PluginSecurityManager.class);
            
      public PluginSecurityManager(){
    	  super();
      }
      
      @Override
      public void checkPermission(Permission permission){
    	  return;
      }         
      
	  @Override
	  public void checkExit(int status) {
		  throwLoggedException();
	  }
	  
      private static void throwLoggedException(){
    	  SecurityException se = new SecurityException("A plugin attempted an unauthorized action.");
    	  LOGGER.error("PluginSecurityManager stopped an unauthorized action.",se);
    	  throw se;
      }   
}
