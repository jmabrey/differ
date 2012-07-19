package cz.nkp.differ.plugins.compare.io;

import org.apache.log4j.Logger;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Runo;

import cz.nkp.differ.plugins.ComparePluginInterface;

public class ScrollableImagePanel extends CustomComponent{
	
	private static Logger LOGGER = Logger.getRootLogger();
	
	public ScrollableImagePanel(Component scaledImage, final Component fullImage,int scale) throws ScrollableImagePanelException{
		LOGGER = ComparePluginInterface.LOGGER;
		setCompositionRoot(getScrollableImagePanel(scaledImage,fullImage,scale));
	}
	
	public static class ScrollableImagePanelException extends Exception{
		public ScrollableImagePanelException(String string){
			super(string);
			LOGGER.warn(string);
		}
	}
	
	private static Layout getScrollableImagePanel(Component scaledImage,final Component fullImage, int scaleFactor) throws ScrollableImagePanelException{
		final Panel scrollPanel = new Panel();
		scrollPanel.addStyleName(Runo.PANEL_LIGHT);
		scrollPanel.setWidth(scaleFactor, Component.UNITS_PIXELS);
		scrollPanel.setHeight(scaleFactor, Component.UNITS_PIXELS);
		scrollPanel.setScrollable(true);
		
		HorizontalLayout scrollButtons = new HorizontalLayout();
		
		scrollButtons.addComponent(getScrollUpButton(scrollPanel));
		scrollButtons.addComponent(getScrollDownButton(scrollPanel));
		scrollButtons.addComponent(getImageLargerButton(fullImage));
				
		scrollPanel.addComponent(scaledImage);
		
		VerticalLayout component = new VerticalLayout();
		component.addComponent(scrollPanel);
		component.addComponent(scrollButtons);
		
		return component;
	}
	
	
	private static final Button getScrollDownButton(final Panel target) throws ScrollableImagePanelException{
		if(target == null){
			throw new ScrollableImagePanelException("Scroll panel target was null");
		}
		
		Button scrollDown = new Button("▼");
		scrollDown.addStyleName(Runo.BUTTON_SMALL);
		
		scrollDown.addListener(new Button.ClickListener() {
		    public void buttonClick(ClickEvent event) {
		        int scrollPos = target.getScrollTop();
		        if (scrollPos > target.getHeight())
		            scrollPos = (int) target.getHeight();
		        target.setScrollTop(scrollPos + 250);
		    }
		});
		
		scrollDown.setImmediate(true);
		
		return scrollDown;
	}
	
	private static final Button getScrollUpButton(final Panel target) throws ScrollableImagePanelException{
		if(target == null){
			throw new ScrollableImagePanelException("Scroll panel target was null");
		}
		
		Button scrollUp = new Button("▲");
		scrollUp.addStyleName(Runo.BUTTON_SMALL);
		
		scrollUp.addListener(new Button.ClickListener() {
		    public void buttonClick(ClickEvent event) {
		        int scrollPos = target.getScrollTop() - 250;
		        if (scrollPos < 0)
		            scrollPos = 0;
		        target.setScrollTop(scrollPos);
		    }
		});
		
		scrollUp.setImmediate(true);
		
		return scrollUp;
	}
	
	private static final Button getImageLargerButton(final Component fullImage) throws ScrollableImagePanelException{
		if(fullImage == null){
			throw new ScrollableImagePanelException("Scroll full image was null");
		}
		
		Button fullSizeButton = new Button("Larger");
		
		fullSizeButton.addListener(new Button.ClickListener() {
		    public void buttonClick(ClickEvent event) {
		        ComparePluginInterface.getApplication().getMainWindow().addWindow(new FullSizeImageWindow(fullImage));
		    }
		});
		

		fullSizeButton.setImmediate(true);
		
		return fullSizeButton;
	}
}

class FullSizeImageWindow extends Window{
	public FullSizeImageWindow(Component fullImage){
		setCaption("Image Display");
		setModal(true);
		setDraggable(false);
		setResizable(false); 
		center();
		setWidth("800px");
		setHeight("100%");
		
		Panel imagePanel = new Panel();
		imagePanel.addComponent(fullImage);
		imagePanel.setScrollable(true);
		imagePanel.addStyleName(Runo.PANEL_LIGHT);
		imagePanel.setSizeFull();
		
		addComponent(imagePanel);
	}
}
