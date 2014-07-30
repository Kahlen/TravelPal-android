package com.kahlen.travelpal.mytrip;

import com.kahlen.travelpal.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MyTripAdapter extends ArrayAdapter<MyTripModel> {
	
	private Context mContext;

	public MyTripAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		mContext = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MyTripModel trip = getItem(position);
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if ( convertView == null )
			convertView = inflater.inflate( R.layout.mytrip_list_item , parent, false );
		
		TextView destinationTxt = (TextView) convertView.findViewById(R.id.mytrip_destination);
		destinationTxt.setText( trip.destination );
		TextView dateTxt = (TextView) convertView.findViewById(R.id.mytrip_date);
		dateTxt.setText( trip.startDate + " ~ " + trip.endDate );
		
		return convertView;
	}

}
