package com.kahlen.travelpal.mytrip;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.kahlen.travelpal.R;
import com.kahlen.travelpal.utilities.AccountUtils;

import android.app.Dialog;
import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class TripContentAdapter extends ArrayAdapter<TripContentFeedModel> {

	private Context mContext;
	private String iid;
	
	public TripContentAdapter(Context context, int resource) {
		super(context, resource);
		mContext = context;
	}
	
	public void setIid( String i ) {
		iid = i;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final TripContentFeedModel contentModel = getItem(position);
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if ( convertView == null )
			convertView = inflater.inflate( R.layout.trip_content_list_item , parent, false );
		
		TextView feedUserTxt = (TextView) convertView.findViewById(R.id.trip_content_feed_user);
		feedUserTxt.setText( contentModel.user.name + " (" + contentModel.user.id + ")" );
		
		TextView feedFeedTxt = (TextView) convertView.findViewById(R.id.trip_content_feed);
		if ( contentModel.feed.startsWith("http://") || contentModel.feed.startsWith("https://") ) {
			// make text a link
			feedFeedTxt.setText(Html.fromHtml("<a href=\"" + contentModel.feed + "\">" + contentModel.feed + "</a>"));
			feedFeedTxt.setMovementMethod(LinkMovementMethod.getInstance());
		} else {
			// not a link
			feedFeedTxt.setText( contentModel.feed );
		}
		
		TextView feedTimetxt = (TextView) convertView.findViewById( R.id.trip_content_feed_time );
		feedTimetxt.setText( contentModel.timestamp );
		
		TextView seeCommentTxt = (TextView) convertView.findViewById(R.id.trip_content_comment_count_txt);
		if ( contentModel.hasComments() ) {
			seeCommentTxt.setText( mContext.getResources().getString( R.string.see_comment ) + " :" + contentModel.commentCount() );
		} else {
			seeCommentTxt.setText( mContext.getResources().getString( R.string.see_comment ) );
		}
		
		seeCommentTxt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// create a dialog to show comments
				final Dialog commentDialog = new Dialog( mContext );
				commentDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				commentDialog.setContentView( R.layout.trip_content_comment_dialog );
				ListView commentListView = (ListView) commentDialog.findViewById( R.id.trip_content_comment_listview );
				final TripCommentAdapter commentAdapter = new TripCommentAdapter( mContext, R.layout.trip_content_comment_list_item );
				commentListView.setAdapter(commentAdapter);
				if ( contentModel.hasComments() ) {
					commentAdapter.addAll( contentModel.comments );
					commentAdapter.notifyDataSetChanged();
				}
				Button commentBtn = (Button) commentDialog.findViewById( R.id.trip_content_comment_btn );
				commentBtn.setOnClickListener( new OnClickListener() {

					@Override
					public void onClick(View v) {
						EditText editText = (EditText) commentDialog.findViewById( R.id.trip_content_comment_input );
						String commentInputTxt = editText.getText().toString();
						editText.setText("");

						String commetTime = getCurrentCommentTimestamp();
						Log.d("kahlen", "send comment: " + commentInputTxt + ", at " + commetTime);
						TripContentCommentModel newComment = new TripContentCommentModel( AccountUtils.getUserModel(mContext), commentInputTxt, commetTime );
						commentAdapter.add(newComment);
						commentAdapter.notifyDataSetChanged();
						// send comment to server
						sendComment2Server(newComment, position);
						contentModel.comments.add(newComment);
						notifyDataSetChanged();
					}
					
				});
				commentDialog.show();
				Window window = commentDialog.getWindow();
				window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				LinearLayout commentInputArea = (LinearLayout) commentDialog.findViewById( R.id.trip_content_comment_area );
				LinearLayout.LayoutParams commentInputAreaLayout = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				commentInputArea.setLayoutParams(commentInputAreaLayout);
			}
			
		});
		
		return convertView;
	}
	
	private String getCurrentCommentTimestamp() {
		SimpleDateFormat dataFormatter = new SimpleDateFormat("h:m:s dd M yyy"); //2:26:13 30 Jun 2014
		String commetTime = dataFormatter.format(new Date());
		String[] tmp = commetTime.split(" ");
		String month = "Jan";
		switch ( Integer.parseInt(tmp[tmp.length-2]) ) {
			case 1:
				month = "Jan";
				break;
			case 2:
				month = "Feb";
				break;
			case 3:
				month = "Mar";
				break;
			case 4:
				month = "Apr";
				break;
			case 5:
				month = "May";
				break;
			case 6:
				month = "Jun";
				break;
			case 7:
				month = "Jul";
				break;
			case 8:
				month = "Aug";
				break;
			case 9:
				month = "Sep";
				break;
			case 10:
				month = "Oct";
				break;
			case 11:
				month = "Nov";
				break;
			case 12:
				month = "Dec";
				break;	
		}
		tmp[tmp.length-2] = month;
		StringBuilder result = new StringBuilder();
		for ( String s: tmp ) {
			result.append( s + " " );
		}
		return result.substring(0, result.length()-1).toString();
	}
	
	private void sendComment2Server( TripContentCommentModel comment, int index ) {
		JSONObject requestBody = new JSONObject();
		try {
			requestBody.put("_id", iid);
			requestBody.put("index", getCount()-index-1);
			JSONObject dataRequestBody = new JSONObject();
			dataRequestBody.put("user", AccountUtils.getUserJson(mContext));
			dataRequestBody.put("comment", comment.comment);
			dataRequestBody.put("timestamp", comment.timestamp);
			requestBody.put("data", dataRequestBody);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TripContentCommentTask task = new TripContentCommentTask( mContext );
		task.execute(requestBody);
	}
	
	class TripCommentAdapter extends ArrayAdapter<TripContentCommentModel> {

		public TripCommentAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TripContentCommentModel commentModel = getItem(position);
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			if ( convertView == null )
				convertView = inflater.inflate( R.layout.trip_content_comment_list_item , parent, false );
			
			TextView commentTxt = (TextView) convertView.findViewById( R.id.trip_content_comment_txt );
			commentTxt.setText( commentModel.user.name + " (" + commentModel.user.id + "): " + commentModel.comment );
			
			TextView commentTimeTxt = (TextView) convertView.findViewById( R.id.trip_content_comment_time_txt );
			commentTimeTxt.setText( commentModel.timestamp );
			
			return convertView;
		}
		
	}

}
