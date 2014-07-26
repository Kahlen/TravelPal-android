package com.kahlen.travelpal.account;


public class UserModel {
	
	final public static String USER_NAME = "userName";

	public String id;
	public String password;
	public String name;
	public String type = AccountMainActivity.TRAVELPAL_ACCOUNT_TYPE;
	
	public UserModel( String i, String pw, String n) {
		id = i;
		password = pw;
		name = n;
	}
	
	public UserModel( String i, String pw ) {
		id = i;
		password = pw;
	}
	
}
