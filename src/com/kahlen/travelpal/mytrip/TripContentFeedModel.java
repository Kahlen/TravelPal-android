package com.kahlen.travelpal.mytrip;

import java.util.ArrayList;

import com.kahlen.travelpal.account.UserModel;

public class TripContentFeedModel {
	public UserModel user;
	public String feed;
	public String timestamp;
	public ArrayList<TripContentCommentModel> comments;
	
	public TripContentFeedModel( UserModel u, String f, String t, ArrayList<TripContentCommentModel> c ) {
		user = u;
		feed = f;
		timestamp = t;
		comments = c;
		if ( comments == null ) {
			comments = new ArrayList<TripContentCommentModel>();
		}
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