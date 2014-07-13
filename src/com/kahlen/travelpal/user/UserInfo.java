package com.kahlen.travelpal.user;

public class UserInfo {
	
	private static User user;
	
	private static class User {
		public String id = "";
		public String name = "";
	}

	public static void setUser( String i ) {
		User u = new User();
		u.id = i;
		user = u;
	}
	
	public static void setUser( String i, String n ) {
		User u = new User();
		u.id = i;
		u.name = n;
		user = u;
	}
	
	public static String getUserId() {
		return user.id;
	}
	
	public static String getUserName() {
		return user.name;
	}
	
}
