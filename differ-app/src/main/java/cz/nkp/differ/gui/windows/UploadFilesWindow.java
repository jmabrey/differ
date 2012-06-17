package cz.nkp.differ.gui.windows;

import java.io.File;

import org.vaadin.easyuploads.MultiFileUpload;

import com.vaadin.ui.Window;

import cz.nkp.differ.DifferApplication;

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
                String msg = fileName + " uploaded. Saved to temp file "
                        + file.getAbsolutePath() + " (size " + length
                        + " bytes)";
                DifferApplication.getCurrentApplication().getMainWindow().showNotification(msg);
            }
        };


		addComponent(upload);
	}
	
	MultiFileUpload upload;
}
