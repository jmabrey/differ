package cz.nkp.differ.gui.components;

import java.io.File;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

import cz.nkp.differ.gui.tabs.DifferProgramTab;
import cz.nkp.differ.gui.windows.ProfileCreationWindow;
import cz.nkp.differ.gui.windows.UploadFilesWindow;
import cz.nkp.differ.util.GUIMacros;

public class DifferProgramTabButtonPanel extends CustomComponent{

	private HelpTooltip uploadButtonHelp,createProfilesButtonHelp;
	private Button uploadFilesButton, createProfilesButton, logoutButton, compareButton;
	private DifferProgramTab parent;
	
	
	public DifferProgramTabButtonPanel(DifferProgramTab parent){
		this.parent = parent;		
		this.setCompositionRoot(createDifferProgramTabButtonPanel());
	}
	
	private Panel createDifferProgramTabButtonPanel(){
		Panel panel = new Panel();
		panel.addComponent(createDifferProgramTabButtonLayout());
		panel.setHeight("100%");
		return panel;
	}
	
	private Layout createDifferProgramTabButtonLayout(){
		VerticalLayout buttonPanelRoot = new VerticalLayout();
		
		uploadFilesButton = new Button("Upload Files");
		uploadFilesButton.addListener(GUIMacros.createWindowOpenButtonListener(new UploadFilesWindow()));
		
		createProfilesButton = new Button("Create New Profile");	
		createProfilesButton.addListener(GUIMacros.createWindowOpenButtonListener(new ProfileCreationWindow()));
		
		compareButton = new Button("Compare");
		compareButton.addListener(compareButtonClickListener);
		
		logoutButton = new Button("Logout");
		logoutButton.addListener(logoutButtonClickListener);
		
		buttonPanelRoot.addComponent(GUIMacros.bindTooltipToComponent(uploadFilesButton, "Upload Files", "Use this function to upload new image files"));
		buttonPanelRoot.addComponent(GUIMacros.bindTooltipToComponent(createProfilesButton, "Create Profile", "Create a new image processing profile"));
		buttonPanelRoot.addComponent(compareButton);
		buttonPanelRoot.addComponent(logoutButton);
		
		return buttonPanelRoot;
	}
	
	private final Button.ClickListener logoutButtonClickListener = new Button.ClickListener() {
		
		@Override
		public void buttonClick(ClickEvent event) {
			parent.setLoggedOutView();				
		}
	};
	
	private final Button.ClickListener compareButtonClickListener = new Button.ClickListener() {
		
		@Override
		public void buttonClick(ClickEvent event) {
			File[] files = parent.getSelectedFiles();
			if(files.length > 1){
				parent.addComponent(new PluginCompareComponent(files[0], files[1]));			
			}
		}
	};
	
}
