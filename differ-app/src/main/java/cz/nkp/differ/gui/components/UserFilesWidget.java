package cz.nkp.differ.gui.components;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
import cz.nkp.differ.user.UserDataController;
import cz.nkp.differ.util.GUIMacros;
import cz.nkp.differ.util.GeneralMacros;

public class UserFilesWidget extends CustomComponent{
	
	private static final long serialVersionUID = 4241885952194067796L;

	private static Logger LOGGER = Logger.getLogger(UserFilesWidget.class);
	
	private static List<UserFilesWidget> userFileWidgets= new ArrayList<UserFilesWidget>();
	
	private Table userFilesTable; 
	private File selected,userDir;	
	private String userDirName;
	
	
	public UserFilesWidget(){
		userFileWidgets.add(this);
		setCompositionRoot(createUserFilesWidget(FileManager.getUserDirectory(UserDataController.getInstance().getLoggedInUser())));
	}
	
	private Layout createUserFilesWidget(File userDir){
		VerticalLayout layout = new VerticalLayout();
		
		if(GeneralMacros.containsNull(userDir) || !userDir.isDirectory()){
			LOGGER.warn("Invalid User Directory!");
			layout.addComponent(GUIMacros.ErrorLabel);
			return layout;
		}

		this.userDirName = userDir.getName();		
		this.userDir = userDir;
		
		userFilesTable = new Table();
		
		userFilesTable.addContainerProperty("subfolder", String.class, "Error","Folder",null,null);
		userFilesTable.addContainerProperty("filename", String.class, "Error", "File", null, null);
		userFilesTable.addContainerProperty("extension", String.class, "Error", "Extension", null, null);
		userFilesTable.addContainerProperty("created_date", String.class, "Error", "Created", null, null);
		userFilesTable.addContainerProperty("file_size", String.class, "Error", "Size", null, null);
		
		userFilesTable.addListener(new Property.ValueChangeListener() {
		    private static final long serialVersionUID = -3804836111826229951L;

			public void valueChange(ValueChangeEvent event) {
		         selected = (File) userFilesTable.getValue();
		    }
		});
		
		
		userFilesTable.setSizeUndefined();
		userFilesTable.setPageLength(10);

		refreshFilesFromFileSystem();
		
		userFilesTable.setSelectable(true);
		userFilesTable.setImmediate(true);
		userFilesTable.setNullSelectionAllowed(false);
		
		
		layout.addComponent(userFilesTable);
		layout.setMargin(false, true, false, true);
		layout.setSizeUndefined();	
		
		return layout;
	}
	
	public static void refreshFiles(){
		for(UserFilesWidget u: userFileWidgets){
			u.refreshFilesFromFileSystem();
		}
	}
	
	private synchronized void refreshFilesFromFileSystem(){
		LOGGER.trace("Refreshing from filesystem");
		Collection<File> files = FileUtils.listFiles(userDir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		this.userFilesTable.removeAllItems();
		
		for(File file: files){
			try {
				addFile(file);
			} catch (IOException e) {
				LOGGER.warn("Unable to load file: " + file.getAbsolutePath());
			}
		}	
		
		userFilesTable.setValue(userFilesTable.firstItemId());
	}
	
	private void addFile(File f) throws IOException{
		if(GeneralMacros.containsNull(f,userFilesTable)){
			return;
		}		
		
		UserFileWidgetTableEntry entry = new UserFileWidgetTableEntry();
		entry.setFile(f, userDirName);		
		entry.addToTable(userFilesTable);
	}
	
	public synchronized File getSelectedFile(){
		return selected;
	}
	
}

class UserFileWidgetTableEntry{
	public void addToTable(Table t){
		if(GeneralMacros.containsNull(t, ID)){
			return;
		}
		t.addItem(new Object[]{subfolder,filename,extension,created,file_size}, ID);
	}
	
	public void setFile(File f, String userDirName) throws IOException{
		if(GeneralMacros.containsNull(f,userDirName)){
			Logger.getLogger(getClass()).warn("Null values passed to UserFileWidget Entry");
			return;
		}
		String fileNameFull = f.getName();
		BasicFileAttributes attr = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
		
		subfolder = f.getParentFile().getName();
		if(subfolder.equals(userDirName)){
			subfolder = null; //lets hide the ugly hex folder name from the user
		}
		
		filename = FilenameUtils.removeExtension(fileNameFull);		
		extension = FilenameUtils.getExtension(fileNameFull).toUpperCase(Locale.ENGLISH);
		//Intended to always be English since other locales might improperly capitalize
				
		created = DateFormat.getDateTimeInstance(DateFormat.DEFAULT,DateFormat.DEFAULT,
				DifferApplication.getCurrentApplication().getLocale()).format(
						new Date(attr.creationTime().toMillis()));//Format the creation time for the locale
		
		file_size = FileUtils.byteCountToDisplaySize(attr.size());
		
		ID = f;		
	}
	
	private String subfolder,filename,extension,created,file_size;
	private File ID;
}