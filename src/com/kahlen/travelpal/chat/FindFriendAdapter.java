package com.kahlen.travelpal.chat;

import com.kahlen.travelpal.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FindFriendAdapter extends ArrayAdapter<FriendModel> {
	
	Context mContext;

	public FindFriendAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		mContext = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		FriendModel friend = getItem(position);
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if ( convertView == null )
			convertView = inflater.inflate( R.layout.friends_list_item , parent, false );
		
		TextView useridTxt = (TextView) convertView.findViewById( R.id.friend_name );
		useridTxt.setText( friend.id );
		if ( friend.isFriend ) {
			useridTxt.setBackgroundColor( mContext.getResources().getColor(R.color.red) );
		} else {
			useridTxt.setBackgroundColor( mContext.getResources().getColor(R.color.blue) );
		}
		
		return convertView;
	}
	
	

}
