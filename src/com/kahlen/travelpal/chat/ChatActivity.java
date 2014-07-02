package com.kahlen.travelpal.chat;

import com.kahlen.travelpal.MyApplication;
import com.kahlen.travelpal.R;
import com.kahlen.travelpal.mqtt.MQTTActivityCallBack;
import com.kahlen.travelpal.mqtt.MQTTCallBack;
import com.kahlen.travelpal.mqtt.MQTTClientController;
import com.kahlen.travelpal.mqtt.MQTTConfiguration;
import com.kahlen.travelpal.mqtt.MQTTService;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ChatActivity extends Activity implements MQTTActivityCallBack {
	
	private Context mContext;
	private MQTTClientController mController;
	private ListView mListView;
	private ChatAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mContext = getApplicationContext();
		mController = MQTTClientController.getInstance( mContext );
		setContentView( R.layout.activity_chat );
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch( item.getItemId() ) {
			case R.id.menu_connect:
				if ( mController.isConnected() ) {
					// disconnect
					
					// stop service
					Intent intent = new Intent(mContext, MQTTService.class);
					intent.setAction( MQTTService.ACTION_DISCONNECT );
			        mContext.startService(intent);
				} else {
					// connect
					
					registerListener();
					// start service
					Intent intent = new Intent(mContext, MQTTService.class);
					intent.setAction( MQTTService.ACTION_CONNECT );
			        mContext.startService(intent);	
			        
				}
				
				break;
			case R.id.menu_subscribe:
				// show popup input dialog
				if ( mController.isConnected() ) {
					// use Activity.this for AlertDialog to avoid exception
					// alertdialog Unable to add window -- token null is not for an application
					AlertDialog.Builder alert = new AlertDialog.Builder( ChatActivity.this );
					alert.setTitle( R.string.dialog_title_subscribe );
					alert.setMessage( R.string.dialog_message_subscribe );
					final EditText input = new EditText( mContext );
					alert.setView(input);
					alert.setPositiveButton( R.string.ok , new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							String topic = input.getText().toString();
							// subscribe
							Intent intent = new Intent(mContext, MQTTService.class);
							intent.setAction( MQTTService.ACTION_SUBSCRIBE );
							intent.putExtra( MQTTService.INTENT_EXTRA_SUBSCRIBE_TOPIC, topic );
					        mContext.startService(intent);	
						}
					});
					alert.show();
				} else {
					Toast.makeText(mContext, R.string.not_connected, Toast.LENGTH_LONG).show();
				}
				
				break;
			case R.id.menu_check_connection:
				// check connection, show toast message
				if ( mController.isConnected() )
					Toast.makeText(mContext, R.string.connected, Toast.LENGTH_LONG).show();
				else
					Toast.makeText(mContext, R.string.not_connected, Toast.LENGTH_LONG).show();
				break;
			case R.id.menu_server:
				AlertDialog.Builder alert2 = new AlertDialog.Builder( ChatActivity.this );
				alert2.setTitle( R.string.server );
				alert2.setMessage( R.string.dialog_message_server );
				final EditText input2 = new EditText( mContext );
				alert2.setView(input2);
				alert2.setPositiveButton( R.string.ok , new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String server = input2.getText().toString();
						// subscribe
						MQTTConfiguration.BROKER_URL = server;
					}
				});
				alert2.show();
				break;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MyApplication.activityResumed();

		Button publishBtn = (Button) findViewById( R.id.chat_send_btn );
		publishBtn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				EditText editText = (EditText) findViewById( R.id.chat_msg_txt );
				String publishTxt = editText.getText().toString();
				// TODO: publish topic
				mController.publishOnTopic("hello", publishTxt);
				ChatMessageModel msg = new ChatMessageModel( publishTxt );
				mAdapter.add( msg );
				mAdapter.notifyDataSetChanged();
			}
			
		});

		initListView();
	}

	@Override
	protected void onPause() {
		super.onPause();
		MyApplication.activityPaused();
	}

    @Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	
	private void initListView() {
		mListView = (ListView) findViewById( R.id.chat_content );
		mAdapter = new ChatAdapter( mContext, R.layout.chat_message_list_item );
		mListView.setAdapter( mAdapter );
	}
	
	public void registerListener() {
		mController.registerCallback( new MQTTCallBack(this, this) );
	}
	
	public void unregisterListener( MQTTActivityCallBack callback ) {
		mController.registerCallback( new MQTTCallBack(this, null) );
	}

	@Override
	public void messageReceived( String topic, String message ) {
		final ChatMessageModel msg = new ChatMessageModel();
		msg.message = message;
		
		// TODO: define topic and sender id
		if ( MQTTConfiguration.CLIENT_ID.equals( topic ) ) {
			msg.me = true;
			msg.senderId = MQTTConfiguration.CLIENT_ID;
		} else {
			msg.senderId = topic;
		}
		
		Log.d("kahlen", "new message --- " + message);
		runOnUiThread(new Runnable() {
		     @Override
		     public void run() {
		    	mAdapter.add( msg );
		 		mAdapter.notifyDataSetChanged();
		 		// ListView scroll down
				mListView.setSelection( mAdapter.getCount() -1 );
		    }
		});

	}




}
