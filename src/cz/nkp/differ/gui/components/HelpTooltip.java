package cz.nkp.differ.gui.components;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;

/**
 * A component that places a help icon and displays a message when that icon is hovered over.
 * @author Joshua Mabrey
 * Mar 30, 2012
 */
@SuppressWarnings("serial")
public class HelpTooltip extends CustomComponent{
	
	private static Embedded icon_hover_hotspot;
	
	/**
	 * Creates a <code>HelpTooltip</code> with the specified message as its help message
	 * @param message
	 */
	public HelpTooltip(String message){
		this.setCompositionRoot(createHelpTooltip(message));
	}
	
	/**
	 * Changes the message currently used to the message given
	 * @param message
	 */
	public void setMessage(String message){
		icon_hover_hotspot.setDescription(message);
	}
	
	/**
	 * Creates and returns the component. This class breaks the pattern used by the other custom components by having a class-scope variable,
	 * but this method is still used to reduce pattern differences between component code.
	 * @param message
	 * @return
	 */
	private Embedded createHelpTooltip(String message){
		icon_hover_hotspot = new Embedded("", new ThemeResource("img/help_tooltip_icon.png"));
		setMessage(message);
		icon_hover_hotspot.setSizeUndefined();
		return icon_hover_hotspot;
	}
}
