package cz.nkp.differ.plugins;

import java.io.File;

import org.apache.log4j.Logger;
import com.vaadin.ui.Component;

public class DROIDPluginInterface implements DifferPluginInterface{

	@Override
	public String getName() {
		return "DROID Plugin";
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
		return null;
	}

	@Override
	public void setLogger(Logger logger) {
		// TODO Auto-generated method stub
		
	}
	
}
