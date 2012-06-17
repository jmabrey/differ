package cz.nkp.differ.gui.tabs;

import org.apache.log4j.Logger;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.LoginForm.LoginEvent;
import com.vaadin.ui.LoginForm.LoginListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Runo;

import cz.nkp.differ.DifferApplication;
import cz.nkp.differ.gui.components.HelpTooltip;
import cz.nkp.differ.gui.components.LoginRegisterComponent;
import cz.nkp.differ.gui.windows.ProfileCreationWindow;
import cz.nkp.differ.gui.windows.UploadFilesWindow;
import cz.nkp.differ.user.UserDataController;
import cz.nkp.differ.util.GUIHelperFunctions;

/**
 * The main application view.
 * @author Joshua Mabrey
 * Mar 30, 2012
 */
@SuppressWarnings("serial")
public class DifferProgramTab extends VerticalLayout implements LoginListener{
	
	public DifferProgramTab(){
		setLoggedOutView();//Start the program logged out
	}
	
	@Override
	public void onLogin(LoginEvent event) { 
		UserDataController.UserLoginResult loginResult = UserDataController.UserLoginResult.DATABASE_ERROR;
		loginResult = UserDataController.getInstance().isValidUserInfo(event.getLoginParameter("username"), event.getLoginParameter("password"));
		
		if(loginResult == UserDataController.UserLoginResult.USER_LOGIN_SUCCESS){
			setLoggedInView();	
		}else{
			switch(loginResult){
				case DATABASE_ERROR:
					DifferApplication.getInstance().getMainWindow().showNotification("Database Error","<br/>The database encountered an error.",Window.Notification.TYPE_ERROR_MESSAGE);
					break;
				case USER_LOGIN_FAIL:
				case USER_DOES_NOT_EXIST:	
					DifferApplication.getInstance().getMainWindow().showNotification("Login Problem","<br/>The username or password is invalid.",Window.Notification.TYPE_WARNING_MESSAGE);
					break;
				default:
					Logger.getLogger(DifferProgramTab.class).error("Need to add case to switch statement!");
					break;
			}
		}
	}
	
	private void setLoggedInView(){
		this.removeAllComponents();
		addComponent(getButtonPanel());
	}
	
	private void setLoggedOutView(){
		this.removeAllComponents();
		if(loginPanel == null){
			loginPanel = new LoginRegisterComponent(this);
		}
		addComponent(loginPanel);
	}
	
	private Layout getButtonPanel(){
		HorizontalLayout buttonPanelRoot = new HorizontalLayout();
		
		uploadFilesButton = new Button("Upload Files");
		uploadFilesButton.addStyleName(Runo.BUTTON_SMALL);
		uploadFilesButton.addListener(GUIHelperFunctions.createWindowOpenButtonListener(new UploadFilesWindow()));
		
		uploadButtonHelp = new HelpTooltip("This is an example!");
		
		createProfilesButton = new Button("Create New Profile");
		createProfilesButton.addStyleName(Runo.BUTTON_SMALL);		
		createProfilesButton.addListener(GUIHelperFunctions.createWindowOpenButtonListener(new ProfileCreationWindow()));
		
		createProfilesButtonHelp = new HelpTooltip("This is an example!");
		
		logoutButton = new Button("Logout");
		logoutButton.addStyleName(Runo.BUTTON_SMALL);
		logoutButton.addListener(logoutButtonClickListener);
		
		buttonPanelRoot.addComponent(uploadFilesButton);
		buttonPanelRoot.addComponent(uploadButtonHelp);
		buttonPanelRoot.addComponent(createProfilesButton);
		buttonPanelRoot.addComponent(createProfilesButtonHelp);
		buttonPanelRoot.addComponent(logoutButton);
		
		return buttonPanelRoot;
	}
	
	private final Button.ClickListener logoutButtonClickListener = new Button.ClickListener() {
		
		@Override
		public void buttonClick(ClickEvent event) {
			setLoggedOutView();				
		}
	};
	
	CustomComponent loginPanel;
	HelpTooltip uploadButtonHelp,createProfilesButtonHelp;
	Button uploadFilesButton, createProfilesButton, logoutButton;
}
