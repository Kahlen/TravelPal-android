package com.kahlen.travelpal.chat;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kahlen.travelpal.DrawerActivity;
import com.kahlen.travelpal.R;
import com.kahlen.travelpal.account.UserInfo;
import com.kahlen.travelpal.mqtt.MQTTActivityCallBack;
import com.kahlen.travelpal.mqtt.MQTTService;
import com.kahlen.travelpal.mqtt.MQTTServiceDelegate;

import android.os.Bundle;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class ChatFragment extends Fragment implements MQTTActivityCallBack, ChatHistoryCallback {
	
	private Context mContext;
	private ListView mListView;
	private ChatAdapter mAdapter;
	private View mRootView;
	final public static String ARG_CHAT_FRIEND_ID = "chat_friend_id";
	private String chatFriendId;
	private String topic2Publish;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("kahlen", "create ChatFragment");
		mContext = getActivity().getApplicationContext();
		mRootView = inflater.inflate(R.layout.activity_chat, container, false);
	     // argument <-> bundle
		chatFriendId = getArguments().getString(ARG_CHAT_FRIEND_ID);
		
		ChatHistoryTask task = new ChatHistoryTask( this );
		task.execute( UserInfo.getUserId(), chatFriendId );
		
		topic2Publish = chatFriendId + "/" + UserInfo.getUserId();
	    String title = getResources().getStringArray(R.array.activity_titles)[3];
	    getActivity().setTitle(title);
	     
	    setupContent();
	    registerListener();
	     
	    return mRootView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		unregisterListener();
	}



	protected void setupContent() {
		super.onResume();

		Button publishBtn = (Button) mRootView.findViewById( R.id.chat_send_btn );
		publishBtn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				EditText editText = (EditText) mRootView.findViewById( R.id.chat_msg_txt );
				String publishTxt = editText.getText().toString();
				editText.setText("");
				// TODO: publish topic
				// start service
				Intent intent = new Intent(mContext, MQTTService.class);
				intent.setAction( MQTTService.ACTION_PUBLISH );
				intent.putExtra( MQTTService.INTENT_EXTRA_PUBLISH_TOPIC , topic2Publish);
				intent.putExtra( MQTTService.INTENT_EXTRA_PUBLISH_MESSAGE , publishTxt);
		        mContext.startService(intent);

				ChatMessageModel msg = new ChatMessageModel( publishTxt );
				mAdapter.add( msg );
				mAdapter.notifyDataSetChanged();
			}
			
		});

		initListView();
	}
	
	private void initListView() {
		mListView = (ListView) mRootView.findViewById( R.id.chat_content );
		mAdapter = new ChatAdapter( mContext, R.layout.chat_message_list_item );
		mListView.setAdapter( mAdapter );
	}
	
	public void registerListener() {
		MQTTServiceDelegate.registerActivityCallback( this );
	}
	
	public void unregisterListener() {
		MQTTServiceDelegate.unregisterActivityCallback( this );
	}

	@Override
	public void messageReceived( DrawerActivity.MQTTNotificationType notificationType, String topic, String message ) {
		// the message is not chat
		if ( notificationType != DrawerActivity.MQTTNotificationType.newMessage )
			return;
		
		final ChatMessageModel msg = new ChatMessageModel();
		msg.message = message;
		Log.d("kahlen", "receive topic: " + topic);
		
		// TODO: define topic and sender id
		if ( topic.startsWith( UserInfo.getUserId() ) ) {
			msg.me = false;
			msg.senderId = topic.split("/")[1];
		} else {
			msg.me = true;
			msg.senderId = UserInfo.getUserId();
		}
		
		Log.d("kahlen", "new message --- " + message);
		getActivity().runOnUiThread(new Runnable() {
		     @Override
		     public void run() {
		    	mAdapter.add( msg );
		 		mAdapter.notifyDataSetChanged();
		 		// ListView scroll down
				mListView.setSelection( mAdapter.getCount() -1 );
		    }
		});

	}

	@Override
	public void getChatHistoryResult(JSONObject history) {
		Log.d("kahlen", "getChatHistoryResult: " + history);
		try {
			JSONArray historyMsg = history.getJSONArray( "history" );
			for ( int i = 0; i < historyMsg.length(); i++ ) {
				JSONObject msg = historyMsg.getJSONObject(i);
				String from =  msg.getString("from");
				if ( UserInfo.getUserId().equals( from )) {
					// from me
					addMyMessage( msg.getString("message") );
				} else {
					// from friend
					addFriendMessage( from, msg.getString("message") );
				}
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	private void addFriendMessage( String friendId, String message ) {
		final ChatMessageModel oldMsg = new ChatMessageModel( message, false, friendId );
		getActivity().runOnUiThread(new Runnable() {
		     @Override
		     public void run() {
		    	mAdapter.add( oldMsg );
		 		mAdapter.notifyDataSetChanged();
		 		// ListView scroll down
				mListView.setSelection( mAdapter.getCount() -1 );
		    }
		});
	}
	
	private void addMyMessage( String message ) {
		final ChatMessageModel oldMsg = new ChatMessageModel( message, true, UserInfo.getUserId() );
		getActivity().runOnUiThread(new Runnable() {
		     @Override
		     public void run() {
		    	mAdapter.add( oldMsg );
		 		mAdapter.notifyDataSetChanged();
		 		// ListView scroll down
				mListView.setSelection( mAdapter.getCount() -1 );
		    }
		});
	}

}
