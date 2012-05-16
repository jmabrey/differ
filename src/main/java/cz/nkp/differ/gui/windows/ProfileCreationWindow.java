package cz.nkp.differ.gui.windows;

import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Select;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import cz.nkp.differ.gui.components.XYResolutionField;
import cz.nkp.differ.util.GUIHelperFunctions;

@SuppressWarnings("serial")
public class ProfileCreationWindow extends Window{
	
	public ProfileCreationWindow(){
		setCaption("Create Profile");
		setModal(true);
		setDraggable(false);
		setResizable(false); 
		center();
		setWidth(335,Window.UNITS_PIXELS);
		VerticalLayout windowLayout = new VerticalLayout();
		windowLayout.setSpacing(true);
				
		windowLayout.addComponent(createProfileCreationWindowForm());
		
		HorizontalLayout layout = new HorizontalLayout();
		Button create = new Button("Create");
		windowLayout.addComponent(create);
		
		Button close = new Button("Close");
		windowLayout.addComponent(close);
		close.addListener(GUIHelperFunctions.createWindowCloseButtonListener(this));
        
		windowLayout.addComponent(layout);
		
        addComponent(windowLayout);
	}
	
	private Layout createProfileCreationWindowForm(){
		VerticalLayout layout = new VerticalLayout();
		
		TextField nameField = new TextField("Profile Name");
		layout.addComponent(nameField);
		
		TextField cLevelsField = new TextField("Levels");
		cLevelsField.addValidator(new IntegerValidator("Levels must be a whole integer"));
		layout.addComponent(cLevelsField);
		
		TextField cLayersField = new TextField("Quality Layers");
		cLayersField.addValidator(new IntegerValidator("Quality Layers must be a whole integer"));
		layout.addComponent(cLayersField);

		Select Ckernels = new Select("Ckernels");
		Ckernels.addItem("Daubechies Biorthogonal Spline Filter");
		Ckernels.addItem("Le Gall Spline Filter");
		Ckernels.setNullSelectionAllowed(false);
		Ckernels.setNewItemsAllowed(false);
		Ckernels.setValue("Daubechies Biorthogonal Spline Filter");
		layout.addComponent(Ckernels);
		
		XYResolutionField Cblk = new XYResolutionField("Cblk");
		Cblk.setValues(4,8,16,32,64,128,256,512,1024);
		Cblk.setDefaultXValue(256);
		Cblk.setDefaultYValue(256);
		layout.addComponent(Cblk);
				
		Select Corder = new Select("Corder");
		Corder.addItem("Layer Resolution Component Position");
		Corder.addItem("Resolution Layer Component Position");
		Corder.addItem("Resolution Position Component Layer");
		Corder.addItem("Position Component Resolution Layer");
		Corder.addItem("Component Position Resolution Layer");
		Corder.setNullSelectionAllowed(false);
		Corder.setNewItemsAllowed(false);
		Corder.setValue("Layer Resolution Component Position");
		layout.addComponent(Corder);
		
		Select Creversible = new Select("Creversible");
		Creversible.addItem("Yes");
		Creversible.addItem("No");
		Creversible.setNullSelectionAllowed(false);
		Creversible.setNewItemsAllowed(false);
		Creversible.setValue("Yes");
		layout.addComponent(Creversible);
		
		Select usePrecints = new Select("Use Precints");
		usePrecints.addItem("Yes");
		usePrecints.addItem("No");
		usePrecints.setNullSelectionAllowed(false);
		usePrecints.setNewItemsAllowed(false);
		usePrecints.setValue("Yes");
		layout.addComponent(usePrecints);
		
		Select packetHeaderStart = new Select("Packet Header [Start]");
		packetHeaderStart.addItem("Yes");
		packetHeaderStart.addItem("No");
		packetHeaderStart.setNullSelectionAllowed(false);
		packetHeaderStart.setNewItemsAllowed(false);
		packetHeaderStart.setValue("Yes");
		layout.addComponent(packetHeaderStart);
		
		Select packetHeaderEnd = new Select("Packet Header [End]");
		packetHeaderEnd.addItem("Yes");
		packetHeaderEnd.addItem("No");
		packetHeaderEnd.setNullSelectionAllowed(false);
		packetHeaderEnd.setNewItemsAllowed(false);
		packetHeaderEnd.setValue("Yes");
		layout.addComponent(packetHeaderEnd);
				
        return layout;
	}
}
