package cz.nkp.differ.gui.components;

import java.io.File;

import org.apache.log4j.Logger;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;

import cz.nkp.differ.plugins.DifferPluginInterface;
import cz.nkp.differ.plugins.PluginComponentReadyCallback;
import cz.nkp.differ.util.GUIMacros;
import cz.nkp.differ.util.GeneralMacros;

public class PluginDisplayComponent extends CustomComponent{
	
	private static final long serialVersionUID = -5172306282663506101L;
	private Logger LOGGER = Logger.getLogger(PluginDisplayComponent.class);
		
	public PluginDisplayComponent(DifferPluginInterface d, File image1,File image2){
		super();
		this.setCompositionRoot(createPluginCompareComponent(d,image1,image2));
		
	}
	
	private Layout createPluginCompareComponent(DifferPluginInterface d,File image1,File image2){
		HorizontalLayout layout = new HorizontalLayout();
		
		if(GeneralMacros.containsNull(d,image1,image2)){
			LOGGER.error("Null passed to createPluginCompareComponent");
			layout.addComponent(GUIMacros.ErrorLabel);
			return layout;
		}
		
		layout.addComponent(new PluginDisplayPanel(d,new File[]{image1,image2}));
		return layout;
	}	
}

class PluginDisplayPanel extends VerticalLayout implements PluginComponentReadyCallback{
	private static final long serialVersionUID = -4597810967107465071L;
	private ProgressIndicator progress = new ProgressIndicator();
	
	static Logger LOGGER = Logger.getLogger(PluginDisplayPanel.class);
	
	public PluginDisplayPanel(DifferPluginInterface d, File[] files){
		d.addFiles(files);
		d.setPluginDisplayComponentCallback(this);
		synchronized(progress){
			progress.setIndeterminate(false);
			progress.setImmediate(true);
			progress.setPollingInterval(750);
			progress.setCaption("Loading plugin...");
			progress.setValue(0f);
			this.addComponent(progress);
		}
	}
	
	@Override
	public void ready(Component c) {
		this.removeAllComponents();
		this.addComponent(c);		
	}

	@Override
	public void setCompleted(int percentage) {
		synchronized(progress){
			progress.setValue(((float)(percentage))/100f);
		}
	}
}
