package com.kahlen.travelpal.mytrip;

import java.util.ArrayList;

import com.kahlen.travelpal.account.UserModel;

public class TripContentFeedModel {
	public UserModel user;
	public String feed;
	public long timestamp;
	public ArrayList<TripContentCommentModel> comments;
	
	public TripContentFeedModel( UserModel u, String f, long t, ArrayList<TripContentCommentModel> c ) {
		user = u;
		feed = f;
		timestamp = t;
		comments = c;
	}
	
	public boolean hasComments() {
		return (comments != null) && (comments.size() != 0);
	}
	
	public int commentCount() {
		if ( hasComments() )
			return comments.size();
		
		return 0;
	}
}