package cz.nkp.differ.plugins;

import java.io.File;

import org.apache.log4j.Logger;

import com.sun.jna.Native;
import com.vaadin.data.Item;
import com.vaadin.ui.Form;

import cz.nkp.differ.JNAPluginExample.Kernel32;

public class JNAPluginExamplePluginInterface implements DifferPluginInterface{

	@Override
	public String getName() {
		Kernel32 lib = (Kernel32) Native.loadLibrary ("kernel32",
                Kernel32.class);
		
		Kernel32.SYSTEMTIME time = new Kernel32.SYSTEMTIME ();
	    lib.GetLocalTime (time);
	    
		return "JNA Example Program TIME from JNA: " + time.wHour + ":" + time.wMinute;
	}
	
	@Override
	public void addFiles(File... file) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getMinimumNumberOfImagesRequired() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Form getPluginSettingsFormBean() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPluginSettingsFormItem(Item i) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Throwable getErrorState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLogger(Logger logger) {
		// TODO Auto-generated method stub
		
	}
	
}
