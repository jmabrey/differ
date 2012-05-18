package cz.nkp.differ.io;

import java.io.File;
import java.io.IOException;

import javax.jcr.Repository;

import cz.nkp.differ.data.beans.UserBean;

public class DifferFileStore {
	private static DifferFileStore _internal;
	
	
	public static final DifferFileStore getDifferFileStore(){
		if(_internal == null){
			_internal = new DifferFileStore();
		}
		return _internal;
	}
	
	
	private DifferFileStore(){
		getAccessToJCRImplementation();
	}
	
	private static final void getAccessToJCRImplementation(){
		
	}
	
	
	public void storeFile(UserBean owner, File f) throws IOException{
		if(isValidFile(f)){
			
		}
		else throw new IOException("The provided file " + f.getName() + " was deemed invalid.");
	}
	
	private static boolean isValidFile(final File f){
		if(f == null){
			return false;
		}
		else if(f.isDirectory()){
			return false;
		}
		else{//This checks the validity of the file name, and throws the IOException if the files name is invalid
			try {
				f.getCanonicalPath();
			} catch (IOException e) {
				return false;
			}
		}
		
		return true;
	}
}
