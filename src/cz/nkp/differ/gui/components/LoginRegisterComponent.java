package cz.nkp.differ.gui.components;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Layout;
import com.vaadin.ui.LoginForm;
import com.vaadin.ui.LoginForm.LoginListener;
import com.vaadin.ui.VerticalLayout;

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
		VerticalLayout layout = new VerticalLayout();
		LoginForm loginForm = new LoginForm();
		loginForm.addListener(parent);
		layout.addComponent(loginForm);
		return layout;
	}
}
