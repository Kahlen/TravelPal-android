package com.kahlen.travelpal.chat;

public class FriendModel {

	public String id = "";
	String name = "";
	public boolean isFriend = false;
	
	public FriendModel( String userid, String n, boolean f ) {
		id = userid;
		name = n;
		isFriend = f;
	}
	
	public FriendModel( String userid, boolean f ) {
		id = userid;
		isFriend = f;
	}
	
}
