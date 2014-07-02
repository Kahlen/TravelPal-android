package com.kahlen.travelpal.chat;

import com.kahlen.travelpal.MyApplication;
import com.kahlen.travelpal.R;
import com.kahlen.travelpal.mqtt.MQTTActivityCallBack;
import com.kahlen.travelpal.mqtt.MQTTClientController;
import com.kahlen.travelpal.mqtt.MQTTService;
import com.kahlen.travelpal.mqtt.MQTTService.MQTTServiceBinder;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ChatActivity extends Activity implements MQTTActivityCallBack {
	
	private Context mContext;
	private MQTTService mService;
	private boolean mServiceBound = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mContext = getApplicationContext();
		setContentView(R.layout.activity_chat);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		MyApplication.activityResumed();
		
		final MQTTClientController controller = MQTTClientController.getInstance(mContext);
		
		Button subscribeBtn = (Button) findViewById( R.id.main_subscribe_btn );
		subscribeBtn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, MQTTService.class);
				intent.setAction( MQTTService.ACTION_SUBSCRIBE );
		        mContext.startService(intent);				
			}
			
		});
		
		final Button connectBtn = (Button) findViewById( R.id.main_connect_btn );
		if ( controller.isConnected() ) {
			connectBtn.setText( R.string.disconnect );
		} else {
			connectBtn.setText( R.string.connect );
		}
		
		
		connectBtn.setOnClickListener( new OnClickListener () {

			@Override
			public void onClick(View arg0) {
				if ( controller.isConnected() ) {
					// disconnect
					
					// stop service
					Intent intent = new Intent(mContext, MQTTService.class);
					intent.setAction( MQTTService.ACTION_DISCONNECT );
			        mContext.startService(intent);
			        
			        connectBtn.setText( R.string.disconnect );
					
				} else {
					// connect
					
					// start service
					Intent intent = new Intent(mContext, MQTTService.class);
					intent.setAction( MQTTService.ACTION_CONNECT );
			        mContext.startService(intent);
			        
			        connectBtn.setText( R.string.connect );
			        
				}
				
			}
			
		});
		
		Button publishBtn = (Button) findViewById( R.id.main_publish_btn );
		publishBtn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				EditText editText = (EditText) findViewById( R.id.main_publish_txt );
				String publishTxt = editText.getText().toString();
				controller.publishOnTopic("hello", publishTxt);
			}
			
		});
		
		Button checkBtn = (Button) findViewById( R.id.main_check_connection_btn );
		checkBtn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				TextView checkTxt = (TextView) findViewById( R.id.main_check_connection_txt );
				if ( controller.isConnected() )
					checkTxt.setText( R.string.connected );
				else
					checkTxt.setText( R.string.not_connected );
			}
			
		});
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		MyApplication.activityPaused();
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
        	MQTTServiceBinder binder = (MQTTServiceBinder) service;
            mService = (MQTTService) binder.getService();
            mServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        	mServiceBound = false;
        }
    };

    @Override
	protected void onStart() {
		super.onStart();
		
		// bind service
		Intent intent = new Intent(mContext, MQTTService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		// unbind service
		if ( mServiceBound ) {
            unbindService(mConnection);
            mServiceBound = false;
        }
	}

	@Override
	public void messageReceived( String message ) {
		TextView receivedTextView = (TextView) findViewById( R.id.main_push_content );
		receivedTextView.append(message + "\n");
	}

}
