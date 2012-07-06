package cz.nkp.differ.gui.tabs;

import java.io.File;

import org.apache.log4j.Logger;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.LoginForm.LoginEvent;
import com.vaadin.ui.LoginForm.LoginListener;
import com.vaadin.ui.Window;

import cz.nkp.differ.DifferApplication;
import cz.nkp.differ.gui.components.DifferProgramTabButtonPanel;
import cz.nkp.differ.gui.components.LoginRegisterComponent;
import cz.nkp.differ.gui.components.UserFilesWidget;
import cz.nkp.differ.user.UserDataController;

/**
 * The main application view.
 * @author Joshua Mabrey
 * Mar 30, 2012
 */
@SuppressWarnings("serial")
public class DifferProgramTab extends HorizontalLayout implements LoginListener{
	
	CustomComponent loginPanel;
	UserFilesWidget[] widgets;
	
	public DifferProgramTab(){
		setLoggedOutView();//Start the program logged out
		widgets = new UserFilesWidget[2];
		widgets[0] = new UserFilesWidget(); 
		widgets[1] = new UserFilesWidget(); 
	}
	
	@Override
	public void onLogin(LoginEvent event) { 
		UserDataController.UserLoginResult loginResult = UserDataController.UserLoginResult.DATABASE_ERROR;
		
		loginResult = UserDataController.getInstance().attemptLogin(event.getLoginParameter("username"), event.getLoginParameter("password"));
		
		if(loginResult == UserDataController.UserLoginResult.USER_LOGIN_SUCCESS){
			setLoggedInView();	
		}else{
			switch(loginResult){
				case DATABASE_ERROR:
					DifferApplication.getCurrentApplication().getMainWindow().showNotification("Database Error","<br/>The database encountered an error.",Window.Notification.TYPE_ERROR_MESSAGE);
					break;
				case USER_LOGIN_FAIL:
				case USER_DOES_NOT_EXIST:	
					DifferApplication.getCurrentApplication().getMainWindow().showNotification("Login Problem","<br/>The username or password is invalid.",Window.Notification.TYPE_WARNING_MESSAGE);
					break;
				default:
					Logger.getLogger(DifferProgramTab.class).error("Need to add case to switch statement!");
					break;
			}
		}
	}
	
	private void setLoggedInView(){
		this.removeAllComponents();
		addComponent(widgets[0]);
		addComponent(widgets[1]);
		addComponent(new DifferProgramTabButtonPanel(this));
		this.setSizeUndefined();
	}
	
	public void setLoggedOutView(){
		this.removeAllComponents();
		if(loginPanel == null){
			loginPanel = new LoginRegisterComponent(this);
		}
		addComponent(loginPanel);
		this.setSizeUndefined();
	}
	
	public File[] getSelectedFiles(){
		File[] files = new File[2];
		
		files[0] = widgets[0].getSelectedFile();
		files[1] = widgets[1].getSelectedFile();
		
		return files;
	}
}
