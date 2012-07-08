package cz.nkp.differ.gui.tabs;

import java.io.File;

import org.apache.log4j.Logger;

import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.LoginForm.LoginEvent;
import com.vaadin.ui.LoginForm.LoginListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import cz.nkp.differ.DifferApplication;
import cz.nkp.differ.gui.components.DifferProgramTabButtonPanel;
import cz.nkp.differ.gui.components.LoginRegisterComponent;
import cz.nkp.differ.gui.components.UserFilesWidget;
import cz.nkp.differ.user.UserDataController;
import cz.nkp.differ.util.GeneralMacros;

/**
 * The main application view.
 * @author Joshua Mabrey
 * Mar 30, 2012
 */
@SuppressWarnings("serial")
public class DifferProgramTab extends HorizontalLayout implements LoginListener{
	
	private CustomComponent loginPanel;	
	private Layout loggedInView,loggedOutView,customViewWrapper;
	private Button customLayoutBackButton;
	private UserFilesWidget fileSelector1,fileSelector2;
	
	private final DifferProgramTab this_internal = this;
	//Used by button listener to reference DifferProgramTab object indirectly
	
	
	public DifferProgramTab(){
		setLoggedOutView();//Start the program logged out
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
		
		if(loggedInView == null){
			loggedInView = new HorizontalLayout();
			fileSelector1 = new UserFilesWidget(); 
			fileSelector2 = new UserFilesWidget(); 
			loggedInView.addComponent(fileSelector1);
			loggedInView.addComponent(fileSelector2);
			loggedInView.addComponent(new DifferProgramTabButtonPanel(this));
			loggedInView.setSizeUndefined();
		}
		
		this.removeAllComponents();
		this.addComponent(loggedInView);
		this.setSizeUndefined();
	}
	
	public void setLoggedOutView(){
		
		if(loggedOutView == null){
			loggedOutView = new HorizontalLayout();
			loginPanel = new LoginRegisterComponent(this);
			loggedOutView.addComponent(loginPanel);
		}

		this.removeAllComponents();
		this.addComponent(loggedOutView);
		this.setSizeUndefined();
	}
	
	public void setCustomView(Layout layout){
		if(customViewWrapper == null){
			customViewWrapper = new VerticalLayout();
			customLayoutBackButton = new Button("Back");
			customLayoutBackButton.addListener(customViewWrapperBackButtonListener);
		}
		
		customViewWrapper.removeAllComponents();
		customViewWrapper.addComponent(customLayoutBackButton);
		customViewWrapper.addComponent(layout);
		customViewWrapper.setSizeUndefined();
		
		this.removeAllComponents();
		this.addComponent(customViewWrapper);
		this.setSizeUndefined();
	}
	
	public File[] getSelectedFiles(){
		if(GeneralMacros.containsNull(fileSelector1,fileSelector2)){
			return null;
		}
		return new File[]{fileSelector1.getSelectedFile(),fileSelector2.getSelectedFile()};
	}
	
	private Button.ClickListener customViewWrapperBackButtonListener = new Button.ClickListener() {
		
		@Override
		public void buttonClick(ClickEvent event) {
			this_internal.removeAllComponents();
			this_internal.setLoggedInView();
		}
	};
}
