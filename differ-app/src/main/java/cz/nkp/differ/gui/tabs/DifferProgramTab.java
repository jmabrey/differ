package cz.nkp.differ.gui.tabs;

import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.LoginForm.LoginEvent;
import com.vaadin.ui.LoginForm.LoginListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;

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
		CustomComponent loginPanel = new LoginRegisterComponent(this);
		addComponent(loginPanel);
	}
	
	@Override
	public void onLogin(LoginEvent event) {
		if(UserDataController.getInstance().isValidUserInfo(event.getLoginParameter("username"), event.getLoginParameter("password"))){
			setLoggedInView();	
		}else{
			//TODO:tell user info entered is incorrect
		}
	}
	
	private void setLoggedInView(){
		this.removeAllComponents();
		addComponent(getButtonPanel());
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
		
		buttonPanelRoot.addComponent(uploadFilesButton);
		buttonPanelRoot.addComponent(uploadButtonHelp);
		buttonPanelRoot.addComponent(createProfilesButton);
		buttonPanelRoot.addComponent(createProfilesButtonHelp);
		
		return buttonPanelRoot;
	}
		
	HelpTooltip uploadButtonHelp,createProfilesButtonHelp;
	Button uploadFilesButton, createProfilesButton;
}
