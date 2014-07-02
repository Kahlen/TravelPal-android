package com.kahlen.travelpal.chat;


import com.kahlen.travelpal.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ChatAdapter extends ArrayAdapter<ChatMessageModel> {

	Context mContext;
	
	public ChatAdapter(Context context, int resource ) {
		super(context, resource );
		mContext = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ChatMessageModel msg = getItem(position);
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if ( convertView == null )
			convertView = inflater.inflate( R.layout.chat_message_list_item , parent, false );
		
		if ( msg.me ) {
			convertView.setBackgroundColor( mContext.getResources().getColor( android.R.color.background_light ) );
		} else {
			convertView.setBackgroundColor( mContext.getResources().getColor( android.R.color.background_dark ) );
		}
		TextView textView = (TextView) convertView.findViewById( R.id.chat_item_txt );
		textView.setText( msg.message );
		
		return convertView;
	}


}
