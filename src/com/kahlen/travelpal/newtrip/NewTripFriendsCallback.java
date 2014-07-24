package com.kahlen.travelpal.newtrip;

import java.util.ArrayList;

import com.kahlen.travelpal.chat.FriendModel;

public interface NewTripFriendsCallback {
	void getFriendsResult( ArrayList<FriendModel> friends );
}
