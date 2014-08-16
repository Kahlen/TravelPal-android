package com.kahlen.travelpal.chat;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kahlen.travelpal.MainFragment;
import com.kahlen.travelpal.R;
import com.kahlen.travelpal.mqtt.MQTTConfiguration;
import com.kahlen.travelpal.utilities.AccountUtils;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
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
		mListView.setOnItemLongClickListener( new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				FriendModel f = (FriendModel) parent.getItemAtPosition(position);
				Log.d("kahlen", f.id + " is long pressed");
				final boolean addFriend = !f.isFriend;
				f.isFriend = !f.isFriend;
				mAdapter.notifyDataSetChanged();
				final JSONObject requestBody = new JSONObject();
				try {
					requestBody.put("id", AccountUtils.getUserid(mContext));
					requestBody.put("friend", f.id);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						try {		 
							//instantiates httpclient to make request
						    DefaultHttpClient httpclient = new DefaultHttpClient();

						    //url with the post data
						    String path = MQTTConfiguration.ADD_FRIEND_URI;
						    if ( !addFriend )
						    	path = MQTTConfiguration.REMOVE_FRIEND_URI;
						    HttpPost httpost = new HttpPost(path);

						    //convert parameters into JSON object
						    Log.d("kahlen", "requestBody = " + requestBody);

						    //passes the results to a string builder/entity
						    StringEntity se = new StringEntity(requestBody.toString());

						    //sets the post request as the resulting string
						    httpost.setEntity(se);
						    //sets a request header so the page receving the request
						    //will know what to do with it
						    httpost.setHeader("Accept", "application/json");
						    httpost.setHeader("Content-type", "application/json");

						    //Handles what is returned from the page 
						    HttpResponse response = httpclient.execute(httpost);
						    int status = response.getStatusLine().getStatusCode();
						    Log.d("kahlen", "add/remove friend status = " + status);
						    if ( status == HttpStatus.SC_OK || status == HttpStatus.SC_CREATED ) {
						    	Log.d("kahlen", "add/remove friend ok");
						    }
				 
				        } catch (Exception e) {
				            e.printStackTrace();
				        }
						return null;
					}
					
				}.execute(null, null, null);
				return false;
			}
			
		});
	}
	
	private void getFriendsFromServer() {
		FindFriendsTask task = new FindFriendsTask( mCallback );
		task.execute( AccountUtils.getUserid(mContext) );
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
