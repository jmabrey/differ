package cz.nkp.differ.util;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window;

import cz.nkp.differ.DifferApplication;

public class GUIHelperFunctions {
	
	@SuppressWarnings("serial")
	public static final Button.ClickListener createWindowOpenButtonListener(final Window target){
		return new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				GeneralHelperFunctions.errorIfContainsNull(target);
				DifferApplication.getInstance().getMainWindow().addWindow(target);		
			}
		};
	}

	private static final Map<String, Object> CloseVariableMap = new HashMap<String,Object>(1);
	
	static
	{
		CloseVariableMap.put("close", true);
	}
	
	@SuppressWarnings("serial")
	public static final Button.ClickListener createWindowCloseButtonListener(final Window target){
		return new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				GeneralHelperFunctions.errorIfContainsNull(target);
				target.changeVariables(null, CloseVariableMap);
			}
		};
	}
}
