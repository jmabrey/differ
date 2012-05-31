package cz.nkp.differ.gui.windows;

import com.vaadin.ui.Button;
import com.vaadin.ui.Window;

import cz.nkp.differ.util.GUIHelperFunctions;

@SuppressWarnings("serial")
public class RegisterUserWindow extends Window{
	public RegisterUserWindow(){
		setCaption("Register User");
		setModal(true);
		setDraggable(false);
		setResizable(false); 
		center();
		setWidth("25%");
		Button close = new Button("Register");
		close.addListener(GUIHelperFunctions.createWindowCloseButtonListener(this));
        addComponent(close); 
	}
		
}