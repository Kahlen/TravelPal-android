package com.kahlen.travelpal.mqtt;

import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MQTTService extends Service {
	
	public static String ACTION_CONNECT = "com.kahlen.travelpal.connect";
	public static String ACTION_DISCONNECT = "com.kahlen.travelpal.disconnect";
	public static String ACTION_SUBSCRIBE = "com.kahlen.travelpal.subscribe";
	
	public static String INTENT_EXTRA_SUBSCRIBE_TOPIC = "subscribe_topic";

	private MQTTClientController mController;
	private final IBinder mBinder = new MQTTServiceBinder();
	
	private ArrayList<MQTTActivityCallBack> callbacks = new ArrayList<MQTTActivityCallBack>();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mController = MQTTClientController.getInstance( getApplicationContext() );
		
		String action = intent.getAction();
		Log.d("kahlen", "MQTTService action: " + action);
		if ( ACTION_CONNECT.equals(action) ) {
			mController.connectMQTTServer();
		} else if ( ACTION_DISCONNECT.equals(action) ) {
			mController.disconnectMQTTServer();
			stopSelf();
		} else if ( ACTION_SUBSCRIBE.equals(action) ) {
			String topic = intent.getStringExtra( INTENT_EXTRA_SUBSCRIBE_TOPIC );
			if ( topic != null )
				mController.subscribeTopic(topic);
			else
				mController.subscribeTopic("hello");
		}

		return Service.START_REDELIVER_INTENT;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if ( mController == null )
			mController = MQTTClientController.getInstance( getApplicationContext() );
		mController.disconnectMQTTServer();
	}
	
	public void registerListener( MQTTActivityCallBack callback ) {
		Log.d("kahlen", "registerListener");
		if ( mController == null )
			mController = MQTTClientController.getInstance( getApplicationContext() );
		
		callbacks.add( callback );
		mController.registerCallback( new MQTTCallBack(this, callback) );
	}
	
	public void unregisterListener( MQTTActivityCallBack callback ) {
		if ( mController == null )
			mController = MQTTClientController.getInstance( getApplicationContext() );
		
		callbacks.remove( callback );
		mController.registerCallback( new MQTTCallBack(this, null) );
	}
	
	public class MQTTServiceBinder extends Binder {
		public MQTTService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MQTTService.this;
        }
    }

}
