package cz.nkp.differ.gui.windows;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Layout;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import cz.nkp.differ.user.UserDataController;
import cz.nkp.differ.util.GUIHelperFunctions;

@SuppressWarnings("serial")
public class RegisterUserWindow extends Window implements ClickListener{
	public RegisterUserWindow(){
		setCaption("Register User");
		setModal(true);
		setDraggable(false);
		setResizable(false); 
		center();
		setWidth("25%");
		
		addComponent(createRegisterUserWindow());
		
		Button register = new Button("Register");
		register.addListener(this);
		addComponent(register);
		
		Button close = new Button("Close");
		close.addListener(GUIHelperFunctions.createWindowCloseButtonListener(this));
		
        addComponent(close); 
	}
	
	
	TextField nameField;
	PasswordField passField;
	/**
	 * 
	 * @return
	 */
	private Layout createRegisterUserWindow(){
		
		VerticalLayout layout = new VerticalLayout();
		
		nameField = new TextField("Username");
		layout.addComponent(nameField);
		
		passField = new PasswordField("Password");
		layout.addComponent(passField);
		
		return layout;
	}

	@Override
	public void buttonClick(ClickEvent event) {
		String nameValue = (String) nameField.getValue();
		String passValue = (String) passField.getValue();
		if(nameValue != null && passValue != null){
			UserDataController.getInstance().addUser(nameValue, passValue);
		}//TODO:password strength checking, closing dialog etc
	}
		
}