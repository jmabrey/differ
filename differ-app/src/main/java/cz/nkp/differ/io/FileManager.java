package cz.nkp.differ.io;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import cz.nkp.differ.DifferApplication;
import cz.nkp.differ.gui.components.UserFilesWidget;
import cz.nkp.differ.util.GeneralMacros;

public class FileManager {
	private static Logger LOGGER = Logger.getLogger(FileManager.class);
	
	private static File homeDir;
	
	public enum FileAddResult{
		FILE_ADDED,
		FILE_ALREADY_EXISTS,
		FILE_NAME_INVALID,
		FILE_ADDED_FAILURE
	}
		
	public static final FileAddResult addFile(String username,File file,String fileName){
		File userDir = getUserDirectory(username);
		if(GeneralMacros.containsNull(username,file,userDir)){
			//Most probable reason is invalid username
			return FileAddResult.FILE_ADDED_FAILURE;
		}
		
		if(file.isDirectory() || !file.exists()){
			return FileAddResult.FILE_ADDED_FAILURE;
		}
		
		try {
			if(FileUtils.directoryContains(userDir, file)){
				return FileAddResult.FILE_ALREADY_EXISTS;
			}
		} catch (IOException e1) {
			return FileAddResult.FILE_ADDED_FAILURE;
		}
		
		try {
			FileUtils.copyFileToDirectory(file,userDir,true);

			File tempFileInUserDir = new File(userDir,file.getName());
			File renamedFileInUserDir = new File(userDir,fileName);
			
			FileUtils.moveFile(tempFileInUserDir, renamedFileInUserDir);
			
			FileUtils.deleteQuietly(tempFileInUserDir);
			
			//Lets have the File Table UI components refresh
			UserFilesWidget.refreshFiles();
			
			return FileAddResult.FILE_ADDED;
		} catch (IOException e) {
			return FileAddResult.FILE_ADDED_FAILURE;
		}
	}
	
	public static final File getRootUsersDirectory(){
		if(GeneralMacros.containsNull(homeDir)){
			homeDir = DifferApplication.getHomeDirectory();
		}
		
		File usersDir = new File(homeDir,"users");
		
		if(!usersDir.exists()){
			usersDir.mkdir();
		}
		
		return usersDir;		
	}
	
	public static final File getUserDirectory(String username){
		if(GeneralMacros.containsNull(username)){
			return null;
		}
		
		String usernameHash = DigestUtils.md5Hex(username);
		
		File userDir = new File(getRootUsersDirectory(),usernameHash);
		
		if(!userDir.exists()){
			userDir.mkdir();
		}
		
		return userDir;
	}
	
	public static final boolean isValidFileLength(long length){
		return true;
	}
}


