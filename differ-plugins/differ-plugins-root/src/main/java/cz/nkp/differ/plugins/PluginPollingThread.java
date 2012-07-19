package cz.nkp.differ.plugins;

import java.io.IOException;

import com.vaadin.ui.Component;

public class PluginPollingThread extends Thread{
	
	private volatile Component comp;
	private volatile PluginComponentReadyCallback callback;
	private volatile DifferPluginInterface plugin;
	
	public PluginPollingThread(final DifferPluginInterface plugin, final PluginComponentReadyCallback callback) throws IOException{
		if(plugin == null || callback == null){
			throw new IOException("Plugin or callback null!");
		}
	}
	
	volatile boolean continueRun = true;
	
	public void run() {
		while(continueRun){
			
			if(checkCallback()){
				continueRun = false;
			}else{
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					continueRun = false;
				}
			}
			
		}
		
	}
	
	private synchronized final boolean checkCallback(){
		if(comp != null){
			return true;// Empty cycles if we have finished
		}
		
		comp = plugin.getPluginDisplayComponent(callback);
		if(comp != null){
			callback.ready(comp);
			return true;
		}
		
		return false;
	}
	
}