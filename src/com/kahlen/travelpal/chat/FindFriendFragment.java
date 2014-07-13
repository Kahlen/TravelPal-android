package com.kahlen.travelpal.chat;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kahlen.travelpal.MyApplication;
import com.kahlen.travelpal.R;
import com.kahlen.travelpal.user.UserInfo;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class FindFriendFragment extends Fragment implements FindFriendsCallback {
	
	private Context mContext;
	private FindFriendAdapter mAdapter;
	private ListView mListView;
	private FindFriendsCallback mCallback = this;
	private View mRootView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		mRootView = inflater.inflate(R.layout.activity_friends, container, false);
	     // argument <-> bundle
//	     int i = getArguments().getInt(ARG_PLANET_NUMBER);
	     String title = getResources().getStringArray(R.array.activity_titles)[3];
	     getActivity().setTitle(title);
	     
	     // get friends from server
	     initListView();
		 getFriendsFromServer();
	     
	     return mRootView;
	}
	
	private void initListView() {
		mListView = (ListView) mRootView.findViewById( R.id.friends_content );
		mAdapter = new FindFriendAdapter( mContext, R.layout.friends_list_item );
		mListView.setAdapter( mAdapter );
	}
	
	private void getFriendsFromServer() {
		FindFriendsTask task = new FindFriendsTask( mCallback );
		task.execute( UserInfo.getUserId() );
	}

	@Override
	public void getFriendResult(JSONObject json) {
		// put friends on ListView
		try {
			Log.d("kahlen", "friends result: " + json);
			JSONArray friends = json.getJSONArray("friends");
			for ( int i = 0; i < friends.length(); i++ ) {
				JSONObject fo = friends.getJSONObject(i);
				FriendModel f = new FriendModel( fo.getString("_id"), fo.getBoolean("isFriend") );
				mAdapter.add(f);
				mAdapter.notifyDataSetChanged();
			}
			
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
	}
	
}
