package com.kahlen.travelpal.newtrip;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kahlen.travelpal.MainFragment;
import com.kahlen.travelpal.R;
import com.kahlen.travelpal.chat.FriendModel;
import com.kahlen.travelpal.user.UserInfo;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class NewTripFriendsFragment extends Fragment implements NewTripFriendsCallback, NewTripAddItineraryCallback {
		
	private Context mContext;
	private View mRootView;
	private ListView mListView;
	private NewTripFriendsAdapter mAdapter;
	private NewTripListener activityCallback;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContext = getActivity();
		mRootView = inflater.inflate(R.layout.activity_newtrip_friends, container, false);
		// argument <-> bundle
		Bundle data = getArguments();
		int i = data.getInt(MainFragment.DRAWER_SELECTED_POSITION);
		String title = getResources().getStringArray(R.array.activity_titles)[i];
		getActivity().setTitle(title);

		setDestinationData( data );
		setFooter( data );
		initListView();
		getFriendsFromServer();
		
		return mRootView;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			activityCallback = (NewTripListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnHeadlineSelectedListener");
        }
	}
	
	private void setBackKeyEvent() {
		mRootView.setOnKeyListener( new OnKeyListener() {

			@Override
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				if( arg1 == KeyEvent.KEYCODE_BACK ) {
					backKeyPressed();
					return true;
				}
				return false;
			}
			
		});
	}
	
	private void setDestinationData( Bundle data ) {
		TextView destination = (TextView) mRootView.findViewById(R.id.newtrip_friends_destination);
		destination.setText( data.getString(NewTripFragment.NEW_TRIP_DESTINATION) );
		
		TextView startDate = (TextView) mRootView.findViewById(R.id.newtrip_friends_start_date_from_date);
		startDate.setText( data.getString(NewTripFragment.NEW_TRIP_START_DATE) );
		
		TextView endDate = (TextView) mRootView.findViewById(R.id.newtrip_friends_end_date_to_date);
		endDate.setText( data.getString(NewTripFragment.NEW_TRIP_END_DATE) );
	}
	
	private void setFooter( final Bundle data ) {
		Button backBtn = (Button) mRootView.findViewById( R.id.newtrip_friends_footer_left );
		backBtn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				backKeyPressed();
			}
			
		});
		
		Button okBtn = (Button) mRootView.findViewById( R.id.newtrip_friends_footer_right );
		okBtn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				addItinerary();
				// back to main page
				activityCallback.finishNewTrip(getArguments());
			}
			
		});
	}
	
	private ArrayList<String> getSelectedFriendsId() {
		ArrayList<String> selectedFriends = new ArrayList<String>();
		for ( int i = 0; i < mAdapter.getCount(); i++ ) {
			FriendModel f = mAdapter.getItem(i);
			if ( f.isFriend )
				selectedFriends.add( f.id );
		}
		return selectedFriends;
	}
	
	public void backKeyPressed() {
		ArrayList<String> selectedFriends = getSelectedFriendsId();
		Bundle data = getArguments();
		if ( !selectedFriends.isEmpty() )
			data.putStringArrayList(NewTripFragment.NEW_TRIP_FRIENDS_SELECTED, selectedFriends);
		activityCallback.backToNewTrip(data);
	}
	
	private void getFriendsFromServer() {
		NewTripFriendsTask task = new NewTripFriendsTask( this );
		task.execute( UserInfo.getUserId() );
	}
	
	private void addItinerary() {
		Bundle data = getArguments();
		JSONObject requestBody = new JSONObject();
		try {
			requestBody.put( "user" , UserInfo.getUserId());
			requestBody.put("destination", data.getString(NewTripFragment.NEW_TRIP_DESTINATION));
			requestBody.put("start", data.getString(NewTripFragment.NEW_TRIP_START_DATE));
			requestBody.put("end", data.getString(NewTripFragment.NEW_TRIP_END_DATE));
			ArrayList<String> selectedFriends = getSelectedFriendsId();
			if ( !selectedFriends.isEmpty() ) {
				requestBody.put("partners", new JSONArray(selectedFriends));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NewTripAddItineraryTask task = new NewTripAddItineraryTask( this );
		task.execute( requestBody );
	}
	
	private void initListView() {
		mListView = (ListView) mRootView.findViewById( R.id.newtrip_friends_list );
		mAdapter = new NewTripFriendsAdapter( mContext, R.layout.friends_list_item );
		mListView.setAdapter( mAdapter );
		mListView.setOnItemClickListener( new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				FriendModel f = (FriendModel) arg0.getItemAtPosition(arg2);
				Log.d("kahlen", f.id + " is clicked");
				f.isFriend = !f.isFriend;
				mAdapter.notifyDataSetChanged();
			}
			
		});
	}

	@Override
	public void getFriendsResult(ArrayList<FriendModel> friends) {
		Bundle data = getArguments();
		if ( data.containsKey( NewTripFragment.NEW_TRIP_FRIENDS_SELECTED ) ) {
			ArrayList<String> selectedFriends = data.getStringArrayList(NewTripFragment.NEW_TRIP_FRIENDS_SELECTED);
			for ( FriendModel f: friends ) {
				if ( selectedFriends.contains( f.id ) ) {
					f.isFriend = true;
				}
			}
		}
		mAdapter.addAll(friends);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void addItineraryResult(boolean successul) {
		if ( !successul ) {
			Toast.makeText(mContext, "Add itinerary failed!", Toast.LENGTH_LONG).show();
		}
		
	}

}
