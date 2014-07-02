package com.kahlen.travelpal.mqtt;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class MQTTService extends Service {
	
	public static String ACTION_CONNECT = "com.kahlen.travelpal.connect";
	public static String ACTION_DISCONNECT = "com.kahlen.travelpal.disconnect";
	public static String ACTION_SUBSCRIBE = "com.kahlen.travelpal.subscribe";

	private MQTTClientController mController;
	private final IBinder mBinder = new MQTTServiceBinder();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mController = MQTTClientController.getInstance( getApplicationContext() );
		
		String action = intent.getAction();
		if ( ACTION_CONNECT.equals(action) ) {
			mController.connectMQTTServer();
		} else if ( ACTION_DISCONNECT.equals(action) ) {
			mController.disconnectMQTTServer();
			stopSelf();
		} else if ( ACTION_SUBSCRIBE.equals(action) ) {
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
	
	public class MQTTServiceBinder extends Binder {
		public MQTTService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MQTTService.this;
        }
    }

}
