package com.kahlen.travelpal.mytrip;

import com.kahlen.travelpal.account.UserModel;

public class TripContentCommentModel {
	public UserModel user;
	public String comment;
	public String timestamp;
	
	public TripContentCommentModel( UserModel u, String c, String t ) {
		user = u;
		comment = c;
		timestamp = t;
	}
}
