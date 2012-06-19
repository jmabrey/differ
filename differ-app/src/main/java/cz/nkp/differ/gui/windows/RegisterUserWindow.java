package cz.nkp.differ.gui.windows;

import org.apache.log4j.Logger;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import cz.nkp.differ.DifferApplication;
import cz.nkp.differ.gui.components.CaptchaComponent;
import cz.nkp.differ.gui.tabs.DifferProgramTab;
import cz.nkp.differ.user.UserDataController;
import cz.nkp.differ.user.UserDataController.UserRegisterResult;
import cz.nkp.differ.util.GUIHelperFunctions;

import eu.livotov.tpt.gui.widgets.TPTCaptcha;

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
		
		HorizontalLayout buttonLayout = new HorizontalLayout();
		
		Button register = new Button("Register");
		register.addListener(this);
		buttonLayout.addComponent(register);
		
		Button close = new Button("Close");
		close.addListener(GUIHelperFunctions.createWindowCloseButtonListener(this));
		buttonLayout.addComponent(close);
		
		addComponent(buttonLayout);
	}
	
	
	TextField nameField,captchaField;
	PasswordField passField;
	CaptchaComponent captcha;
	
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
		
		captcha = new CaptchaComponent();
		layout.addComponent(captcha);
		
		captchaField = new TextField("Verification");
		layout.addComponent(captchaField);
		
		return layout;
	}

	@Override
	public void buttonClick(ClickEvent event) {
		
		String captchaValue = (String) captchaField.getValue();
		if(!captchaValue.equals(captcha.getCaptchaCode())){
			DifferApplication.getCurrentApplication().getMainWindow().showNotification("Captcha Problem","<br/>You did not enter the correct captcha.",Window.Notification.TYPE_WARNING_MESSAGE);
			return;
		}
		
		String nameValue = (String) nameField.getValue();
		String passValue = (String) passField.getValue();
		if(nameValue != null && passValue != null){
			UserRegisterResult registerResult = UserDataController.getInstance().addUser(nameValue, passValue);
			switch(registerResult){
			case USER_CREATION_SUCCESS:
				this.close();
				break;
			case DATABASE_ERROR:
				DifferApplication.getCurrentApplication().getMainWindow().showNotification("Database Error","<br/>The database encountered an error.",Window.Notification.TYPE_ERROR_MESSAGE);
				break;
			case USER_ALREADY_EXISTS:
			case USER_CREATION_FAIL:	
				DifferApplication.getCurrentApplication().getMainWindow().showNotification("Registration Problem","<br/>The username or password is invalid.",Window.Notification.TYPE_WARNING_MESSAGE);
				break;
			default:
				Logger.getLogger(DifferProgramTab.class).error("Need to add case to switch statement!");
				break;
		}
		}//TODO:password strength checking, closing dialog etc
	}
		
}