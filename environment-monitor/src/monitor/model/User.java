package monitor.model;

import java.util.HashMap;
import java.util.Map;

import monitor.implementation.MonitorRuntimeException;

public class User {
	
	private final UserKey key;
	private String otherPassword;
	private static Map<UserKey, User> existingUsers = new HashMap<UserKey, User>();
	
	private User(UserKey key) {
		this.key = key;
	}
	
	public static User getInstance(String host, String name, String password, String otherPassword) {
		UserKey key = new UserKey(host, password, name);
		User user = existingUsers.get(key);
		if (user != null && password != null && !user.getPassword().equals(password)) {
			throw new MonitorRuntimeException("found User '" + user.getName() + "' for host '" + host + "' but it was added with a different password.");
			//throw new MonitorRuntimeException(String.format("found %s on host %s with password %s but the password requested was %s", user.getName(), host, user.getPassword(), password));
		}
		if (user == null) {
			user = new User(key);
			user.otherPassword = otherPassword;
			existingUsers.put(key, user);
		}
		return user;
	}
	
	public String getName() {
		return key.name;
	}

	public String getPassword() {
		return key.password;
	}

	public String getOtherPassword() {
		return otherPassword;
	}	
	
	@Override
	public String toString() {
		return key.name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}
	
}
