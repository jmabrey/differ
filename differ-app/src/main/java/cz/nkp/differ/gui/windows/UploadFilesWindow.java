package cz.nkp.differ.gui.windows;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.vaadin.terminal.FileResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class UploadFilesWindow extends Window implements Upload.SucceededListener,Upload.FailedListener,Upload.Receiver{
	public UploadFilesWindow(){
		setCaption("Upload Files");
		setModal(true);
		setDraggable(false);
		setResizable(false); 
		center();
		setWidth("25%");
		upload = new Upload("Upload the file here",this);
		addComponent(upload);
		upload.addListener((Upload.SucceededListener) this);
        upload.addListener((Upload.FailedListener) this);
	}
	
	
	private Upload upload;
	private Panel imagePanel;
	File  file;         // File to write to.
	
	@Override
	public OutputStream receiveUpload(String filename, String mimeType) {
		FileOutputStream fos = null; // Output stream to write to
        file = new File("/tmp/uploads/" + filename);
        try {
            // Open the file for writing.
            fos = new FileOutputStream(file);
        } catch (final java.io.FileNotFoundException e) {
            // Error while opening the file. Not reported here.
            e.printStackTrace();
            return null;
        }

        return fos; // Return the output stream to write to
	}

	// This is called if the upload is finished.
    public void uploadSucceeded(Upload.SucceededEvent event) {
        // Log the upload on screen.
        addComponent(new Label("File " + event.getFilename()
                + " of type '" + event.getMIMEType()
                + "' uploaded."));
        
        // Display the uploaded file in the image panel.
        final FileResource imageResource =
                new FileResource(file, getApplication());
        imagePanel.removeAllComponents();
        imagePanel.addComponent(new Embedded("", imageResource));
    }

    // This is called if the upload fails.
    public void uploadFailed(Upload.FailedEvent event) {
        // Log the failure on screen.
        	addComponent(new Label("Uploading "
                + event.getFilename() + " of type '"
                + event.getMIMEType() + "' failed."));
    }
}
