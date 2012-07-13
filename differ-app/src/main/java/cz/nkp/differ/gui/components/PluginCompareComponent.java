package cz.nkp.differ.gui.components;

import java.io.File;

import org.apache.log4j.Logger;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

import cz.nkp.differ.plugins.DifferPluginInterface;
import cz.nkp.differ.plugins.PluginManager;
import cz.nkp.differ.util.GUIMacros;
import cz.nkp.differ.util.GeneralMacros;

public class PluginCompareComponent extends CustomComponent{
	
	private static final long serialVersionUID = -5172306282663506101L;
	private Logger LOGGER = Logger.getLogger(PluginCompareComponent.class);
		
	public PluginCompareComponent(File image1,File image2){
		super();
		DifferPluginInterface[] plugins = PluginManager.getInstance().getPlugins();
		//TODO:file verification
		
		this.setCompositionRoot(createPluginCompareComponent(plugins,image1,image2));
		
	}
	
	private Layout createPluginCompareComponent(DifferPluginInterface[] plugins,File image1,File image2){
		HorizontalLayout layout = new HorizontalLayout();
		
		if(GeneralMacros.containsNull((Object[])plugins,image1,image2)){
			LOGGER.error("Null passed to createPluginCompareComponent");
			layout.addComponent(GUIMacros.ErrorLabel);
			return layout;
		}
		
		layout.addComponent(new PluginComparePanel(plugins,new File[]{image1,image2}));
		return layout;
	}
	
	
}

class PluginComparePanel extends CustomComponent{
	private static final long serialVersionUID = -4597810967107465071L;

	static Logger LOGGER = Logger.getLogger(PluginComparePanel.class);
	
	public PluginComparePanel(DifferPluginInterface[] dfi, File[] files){
		Component[] components = new Component[dfi.length];
		
		for(int i = 0; i < dfi.length; i++){
			DifferPluginInterface d = dfi[i];
			if(GeneralMacros.containsNull(d)){
				LOGGER.error("Null plugin found!");
				components[i] = GUIMacros.ErrorLabel;
			}else{
				d.addFiles(files);
				components[i] = getPluginComponent(d);
			}
		}
		
		this.setCompositionRoot(createPluginComparePanel(components));
	}
	
	private Layout createPluginComparePanel(Component[] pluginComponents){
		VerticalLayout layout = new VerticalLayout();
		
		if(GeneralMacros.containsNull((Object)pluginComponents)){
			LOGGER.error("Component list is null.");
			layout.addComponent(GUIMacros.ErrorLabel);
			return layout;
		}
		
		for(Component c: pluginComponents){
			if(GeneralMacros.containsNull(c)){
				LOGGER.warn("Null component found.");
				layout.addComponent(GUIMacros.ErrorLabel);
			}
			else layout.addComponent(c);
		}
		
		layout.setMargin(false, true, true, true);
		
		return layout;		
	}
	
	private Component getPluginComponent(DifferPluginInterface target){
		if(GeneralMacros.containsNull(target)){
			LOGGER.error("Plugin given to PluginComparePanel is null!");
		}
		
		Component pluginComponent;
		try{
			pluginComponent = target.getPluginDisplayComponent();
		}catch(Exception e){
			LOGGER.warn("Plugin " + target.getName() + " failed",e);
			return GUIMacros.ErrorLabel;
		}
		
		if(GeneralMacros.containsNull(pluginComponent)){
			LOGGER.warn("Plugin " + target.getName() + " returned a null Component!");
			return GUIMacros.ErrorLabel;
		}
		
		return pluginComponent;
	}
}
