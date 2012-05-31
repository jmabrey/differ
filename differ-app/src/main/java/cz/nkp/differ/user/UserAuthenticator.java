package cz.nkp.differ.user;

/**
 * Class for application wide user authentication.
 * @author Joshua Mabrey
 * May 4, 2012
 */
public class UserAuthenticator{

	public static UserAuthenticator getInstance(){
		if(_instance == null){
			_instance = new UserAuthenticator();
		}
		
		return _instance;
	}
	
	/**
	 * Checks the supplied username and password against the user database.
	 * 
	 * TODO:Implement
	 * @param username
	 * @param password
	 * @return
	 */
	public boolean validateUserInfo(String username, String password){
		if(username.equals("test") && password.equals("test")){
			return true;
		}
		return false;
	}
	
	
	
	private static UserAuthenticator _instance = null;
}