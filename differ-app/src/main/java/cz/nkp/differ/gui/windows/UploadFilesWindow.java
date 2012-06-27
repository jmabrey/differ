package cz.nkp.differ.gui.windows;

import java.io.File;

import org.vaadin.easyuploads.MultiFileUpload;

import com.vaadin.ui.Window;

import cz.nkp.differ.DifferApplication;
import cz.nkp.differ.io.FileManager;
import cz.nkp.differ.user.UserDataController;

@SuppressWarnings("serial")
public class UploadFilesWindow extends Window {
	public UploadFilesWindow(){
		setCaption("Upload Files");
		setModal(true);
		setDraggable(false);
		setResizable(false); 
		center();
		setWidth("25%");
		upload = new MultiFileUpload() {
            @Override
            protected void handleFile(File file, String fileName,
                    String mimeType, long length) {
                FileManager.addFile(UserDataController.getInstance().getLoggedInUser(), file, fileName);
            }
        };


		addComponent(upload);
	}
	
	MultiFileUpload upload;
}
