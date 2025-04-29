package models.Utils;

import java.util.HashMap;

public class SessionManager {
	private static SessionManager instance;
	private HashMap<String, String> userData;

	public static SessionManager getInstance() {
		if (instance == null) {
			instance = new SessionManager();
		}
		return instance;
	}

	public HashMap<String, String> getUserData() {
		return this.userData;
	}

	public void setUserData(HashMap<String, String> userData) {
		this.userData = userData;
	}
}
