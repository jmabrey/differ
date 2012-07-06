package cz.nkp.differ.gui.components;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.log4j.Logger;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import cz.nkp.differ.DifferApplication;
import cz.nkp.differ.io.FileManager;
import cz.nkp.differ.util.GeneralMacros;

public class UserFilesWidget extends CustomComponent{
	
	private Table userFilesTable; 
	private File selected;
	
	private static Logger LOGGER = Logger.getLogger(UserFilesWidget.class);
	
	public UserFilesWidget(){
		setCompositionRoot(createUserFilesWidget(FileManager.getUsersDirectory()));
	}
	
	private Layout createUserFilesWidget(File userDir){
		if(GeneralMacros.containsNull(userDir)){
			return null;
		}
		
		if(!userDir.isDirectory()){
			return null;
		}
		
		VerticalLayout layout = new VerticalLayout();
		
		userFilesTable = new Table();
		
		userFilesTable.addContainerProperty("filename", String.class, "Error", "File", null, null);
		userFilesTable.addContainerProperty("extension", String.class, "Error", "Extension", null, null);
		userFilesTable.addContainerProperty("upload_date", String.class, null, "Uploaded", null, null);
		userFilesTable.addContainerProperty("file_size", String.class, null, "Size", null, null);
		
		userFilesTable.setSelectable(true);
		userFilesTable.setNullSelectionAllowed(false);
		
		userFilesTable.addListener(new Property.ValueChangeListener() {
		    public void valueChange(ValueChangeEvent event) {
		         selected = (File) userFilesTable.getValue();
		    }
		});
		
		userFilesTable.setSizeUndefined();
		
		Collection<File> files = FileUtils.listFiles(userDir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		
		for(File file: files){
			try {
				addFile(file);
			} catch (IOException e) {
				LOGGER.warn("Unable to load file: " + file.getAbsolutePath());
			}
		}
		
		layout.addComponent(userFilesTable);
		layout.setMargin(false, true, false, true);
		layout.setSizeUndefined();
		return layout;
	}
	
	private void addFile(File f) throws IOException{
		if(GeneralMacros.containsNull(f,userFilesTable)){
			return;
		}
		
		String fileNameFull = f.getName();
		BasicFileAttributes attr = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
		
		String filename = FilenameUtils.removeExtension(fileNameFull);		
		String extension = FilenameUtils.getExtension(fileNameFull);
		
		String uploaded = DateFormat.getDateInstance(DateFormat.FULL,
				DifferApplication.getCurrentApplication().getLocale()).format(
						new Date(attr.creationTime().toMillis()));//Format the creation time for the locale
		
		String file_size = FileUtils.byteCountToDisplaySize(attr.size());
		
		userFilesTable.addItem(new Object[]{
				filename,extension,uploaded,file_size
		},f);
	}
	
	public File getSelectedFile(){
		return selected;
	}
	
}
