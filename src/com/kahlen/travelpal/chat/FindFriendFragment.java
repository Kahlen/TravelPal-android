package com.kahlen.travelpal.chat;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kahlen.travelpal.MainFragment;
import com.kahlen.travelpal.R;
import com.kahlen.travelpal.account.UserInfo;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class FindFriendFragment extends Fragment implements FindFriendsCallback {
	
	private Context mContext;
	private FindFriendAdapter mAdapter;
	private ListView mListView;
	private FindFriendsCallback mCallback = this;
	private View mRootView;
	private FindFriendListener activityCallback;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		mRootView = inflater.inflate(R.layout.activity_friends, container, false);
	     // argument <-> bundle
	     int i = getArguments().getInt(MainFragment.DRAWER_SELECTED_POSITION);
	     String title = getResources().getStringArray(R.array.activity_titles)[i];
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
		mListView.setOnItemClickListener( new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				FriendModel f = (FriendModel) arg0.getItemAtPosition(arg2);
				Log.d("kahlen", f.id + " is clicked");
				// notify activity to change fragment
				activityCallback.friendIdSelected( f.id );
			}
			
		});
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
				if ( fo.has("name") )
					f.name = fo.getString("name");
				mAdapter.add(f);
				mAdapter.notifyDataSetChanged();
			}
			
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
	}
	
	// --- activity communication ---	
	public interface FindFriendListener {
		public void friendIdSelected( String id );
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			activityCallback = (FindFriendListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnHeadlineSelectedListener");
        }
		
	}
	
}
