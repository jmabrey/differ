package cz.nkp.differ.gui.components;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Layout;
import com.vaadin.ui.LoginForm;
import com.vaadin.ui.LoginForm.LoginListener;
import cz.nkp.differ.gui.windows.RegisterUserWindow;
import cz.nkp.differ.util.GUIMacros;

/**
 * Basic login form wrapper created so that adding customization to the form later would be much easier.
 * @author Joshua Mabrey
 * May 4, 2012
 */
@SuppressWarnings("serial")
public class LoginRegisterComponent extends CustomComponent{
	
	public LoginRegisterComponent(LoginListener parent){
		setCompositionRoot(createUserLoginForm(parent));
	}
	
	private Layout createUserLoginForm(LoginListener parent){
		AbsoluteLayout layout = new AbsoluteLayout();
		layout.setWidth("400px");
		layout.setHeight("200px");
		LoginForm loginForm = new LoginForm();
		loginForm.addListener(parent);
		layout.addComponent(loginForm,"left: 0px; top: 0px;");
		Button registerButton = new Button("Register");
		registerButton.addListener(GUIMacros.createWindowOpenButtonListener(new RegisterUserWindow()));
		layout.addComponent(registerButton,"left: 65px; top: 80px;");
		return layout;
	}
}
