package cz.nkp.differ.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window;

import cz.nkp.differ.DifferApplication;

/**
 * Contains GUI Macros to simplify both code readability and future code modifications. These methods are
 * common actions for GUI components.If target is null this method will fail with an error
 * that will end the application session.
 * @author Joshua Mabrey
 * Jun 12, 2012
 */
public class GUIMacros {
	static Logger LOGGER = Logger.getLogger(GUIMacros.class);
	
	/**
	 * Returns a Button.ClickListener that will open the Window passed to the method whenever the component
	 * the listener is attached to fires the ButtonClick event.
	 * @param target
	 * @return
	 */
	@SuppressWarnings("serial")
	public static final Button.ClickListener createWindowOpenButtonListener(final Window target){
		return new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				GeneralMacros.errorIfContainsNull(target);
				DifferApplication.getCurrentApplication().getMainWindow().addWindow(target);		
			}
		};
	}

	private static final Map<String, Object> CloseVariableMap = new HashMap<String,Object>(1);
	
	static
	{
		CloseVariableMap.put("close", true);
	}
	
	/**
	 * Returns a Button.ClickListener that will close the Window passed to the method whenever the component
	 * the listener is attached to fires the ButtonClick event. If target is null this method will fail with an error
	 * that will end the application session.
	 * @param target
	 * @return
	 */
	@SuppressWarnings("serial")
	public static final Button.ClickListener createWindowCloseButtonListener(final Window target){
		return new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				GeneralMacros.errorIfContainsNull(target);
				target.changeVariables(null, CloseVariableMap);//Workaround. May change in future releases of Vaadin.
				//Generally windows can't be closed by easy function call b/c the call is package local. However
				//this map insertion is the same way currently used by the window to close itself. 
			}
		};
	}
}
