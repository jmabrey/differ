package cz.nkp.differ.plugins;

import java.io.File;

import org.apache.log4j.Logger;
import com.vaadin.data.Item;
import com.vaadin.ui.Form;

public class DROIDPluginInterface implements DifferPluginInterface{

	@Override
	public String getName() {
		return "Differ Plugin";
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
