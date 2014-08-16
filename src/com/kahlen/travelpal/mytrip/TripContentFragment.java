package com.kahlen.travelpal.mytrip;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kahlen.travelpal.MainFragment;
import com.kahlen.travelpal.R;
import com.kahlen.travelpal.account.UserModel;
import com.kahlen.travelpal.utilities.AccountUtils;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class TripContentFragment extends Fragment implements TripContentCallback {
	
	final public static String TRIP_CONTENT_DESTINATION = "trip_content_destination";
	final public static String TRIP_CONTENT_IID = "trip_content_iid";
	final public static String TRIP_CONTENT_TRAVEL_TIME = "trip_content_travel_time";
	
	private Context mContext;
	private View mRootView;
	private ListView mListView;
	private TripContentAdapter mAdapter;
	private String mIid;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mContext = getActivity();
		mRootView = inflater.inflate(R.layout.activity_trip_content, container, false);
		
		Bundle data = getArguments();
//	    int i = data.getInt(MainFragment.DRAWER_SELECTED_POSITION);
//	    String title = getResources().getStringArray(R.array.activity_titles)[i];
//	    getActivity().setTitle(title);
	    getActivity().getActionBar().setTitle(data.getString(TRIP_CONTENT_DESTINATION));
	    getActivity().getActionBar().setSubtitle(data.getString(TRIP_CONTENT_TRAVEL_TIME));
	    
	    setCreateFeedBtn();
	    initListView();
	    mIid = data.getString(TRIP_CONTENT_IID);
	    getTripContentFromServer( mIid );
		
		return mRootView;
	}
	
	private void setCreateFeedBtn() {
		Button feedBtn = (Button) mRootView.findViewById(R.id.trip_content_create_btn);
		feedBtn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				final Dialog feedDialog = new Dialog( mContext );
				feedDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				feedDialog.setContentView( R.layout.trip_content_feed_dialog );
				Button feedBtn = (Button) feedDialog.findViewById( R.id.trip_content_feed_btn );
				feedBtn.setOnClickListener( new OnClickListener() {

					@Override
					public void onClick(View v) {
						EditText editText = (EditText) feedDialog.findViewById( R.id.trip_content_feed_input );
						String feedInputTxt = editText.getText().toString();
						editText.setText("");
						
						long feedTime = System.currentTimeMillis();
						JSONObject requestBody = new JSONObject();
						try {
							requestBody.put("_id", mIid);
							JSONObject dataRequestBody = new JSONObject();
							dataRequestBody.put("user", AccountUtils.getUserJson(mContext));
							dataRequestBody.put("feed", feedInputTxt);
							dataRequestBody.put("timestamp", feedTime);
							requestBody.put("data", dataRequestBody);
							TripContentFeedTask task = new TripContentFeedTask( mContext );
							task.execute(requestBody);
						} catch ( Exception e ) {
							e.printStackTrace();
						}
						
						TripContentFeedModel newFeed = new TripContentFeedModel( AccountUtils.getUserModel(mContext), feedInputTxt, feedTime, null );
						mAdapter.insert(newFeed, 0);
						feedDialog.dismiss();
					}
					
				});
				feedDialog.show();
				Window window = feedDialog.getWindow();
				window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			}
			
		});
	}
	
	private void initListView() {
		mListView = (ListView) mRootView.findViewById(R.id.trip_content_listview);
		mAdapter = new TripContentAdapter( mContext, R.layout.trip_content_list_item );
		mListView.setAdapter(mAdapter);
	}
	
	private void getTripContentFromServer(String iid) {
		TripContentTask task = new TripContentTask( mContext, this );
		task.execute(iid);
	}

	@Override
	public void tripContentResult(JSONObject result) {
		if ( result == null )
			return;
		Log.d("kahlen", "trip content result: " + result);
		try {
			mAdapter.setIid( result.getString("_id") );
			if ( result.has("data") ) {
				JSONArray data = result.getJSONArray("data");
				for ( int i = 0; i < data.length(); i++ ) {
					JSONObject feedData = data.getJSONObject(i);
					JSONObject feedUserData = feedData.getJSONObject("user");
					UserModel feedUser = new UserModel( feedUserData.getString("_id"), feedUserData.getString("password"), feedUserData.getString("name") );
					String feedFeed = feedData.getString("feed");
					long feedTimestamp = feedData.getLong("timestamp");
					
					ArrayList<TripContentCommentModel> commentModels = null;
					if ( feedData.has("comments") ) {
						JSONArray comments = feedData.getJSONArray("comments");
						commentModels = new ArrayList<TripContentCommentModel>();
						for ( int j = 0; j < comments.length(); j++ ) {
							JSONObject commentData = comments.getJSONObject(j);
							JSONObject commentUserData = commentData.getJSONObject("user");
							UserModel commentUser = new UserModel( commentUserData.getString("_id"), commentUserData.getString("password"), commentUserData.getString("name") );
							String commentComment = commentData.getString("comment");
							String commentTimestamp = commentData.getString("timestamp");
							TripContentCommentModel commentModel = new TripContentCommentModel( commentUser, commentComment, commentTimestamp );
							commentModels.add(commentModel);
						}
					}
					TripContentFeedModel feedModel = new TripContentFeedModel( feedUser, feedFeed, feedTimestamp, commentModels );
					mAdapter.add(feedModel);
					mAdapter.notifyDataSetChanged();
				}
			}

		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		getActivity().getActionBar().setSubtitle(null);
	}

}
