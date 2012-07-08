package cz.nkp.differ.plugins;

import java.io.File;

import org.apache.log4j.Logger;

import com.sun.jna.Native;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

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
	public int getDesiredPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Component getPluginDisplayComponent() {
		// TODO Auto-generated method stub
		return new Label(getName());
	}

	@Override
	public void setLogger(Logger logger) {
		// TODO Auto-generated method stub
		
	}
	
}
