package com.kahlen.travelpal.mytrip;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kahlen.travelpal.MainFragment;
import com.kahlen.travelpal.R;
import com.kahlen.travelpal.newtrip.NewTripListener;
import com.kahlen.travelpal.utilities.AccountUtils;

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

public class MyTripFragment extends Fragment implements MyTripCallback {
	
	public final static String ARGS_SHARED_LINK_URL = "shared_link_url";
	
	private Context mContext;
	private View mRootView;
	private ListView mListView;
	private MyTripAdapter mAdapter;
	private MyTripListener activityCallback;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mContext = getActivity();
		mRootView = inflater.inflate(R.layout.activity_mytrip, container, false);
		
		Bundle data = getArguments();
	    int i = data.getInt(MainFragment.DRAWER_SELECTED_POSITION);
	    String title = getResources().getStringArray(R.array.activity_titles)[i];
	    getActivity().setTitle(title);
	    
	    initListView();
	    getMyTripsFromServer();
	    
	    return mRootView;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			activityCallback = (MyTripListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnHeadlineSelectedListener");
        }
	}
	
	private void initListView() {
		mListView = (ListView) mRootView.findViewById( R.id.mytrip_listview );
		mAdapter = new MyTripAdapter( mContext, R.layout.mytrip_list_item );
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener( new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				MyTripModel tripModel = (MyTripModel) arg0.getItemAtPosition(arg2);
				activityCallback.go2TripContent(tripModel, getArguments().getString(ARGS_SHARED_LINK_URL, null));
				getArguments().putString(ARGS_SHARED_LINK_URL, null);
			}
			
		});
	}
	
	private void getMyTripsFromServer() {
		MyTripTask task = new MyTripTask( mContext, this );
		task.execute( AccountUtils.getUserid(mContext) );
	}

	@Override
	public void mytripListResult(JSONObject result) {
		try {
			Log.d("kahlen", "get my trip from server: " + result);
			JSONArray trips = result.getJSONArray("itineraries");
			for ( int i = 0; i < trips.length(); i++ ) {
				JSONObject tr = trips.getJSONObject(i);
				ArrayList<String> partners = null;
				if ( tr.has("partners") ) {
					partners = new ArrayList<String>();
					JSONArray p  = tr.getJSONArray("partners");
					for ( int j = 0; j < p.length(); j++ )
						partners.add(p.getString(j));
				}
				
				MyTripModel model = new MyTripModel( tr.getString("_id"), tr.getString("destination"), tr.getString("start"), tr.getString("end"), partners );
				mAdapter.add(model);
				mAdapter.notifyDataSetChanged();
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
	}

}
