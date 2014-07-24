package com.kahlen.travelpal.mqtt;

import java.util.ArrayList;

import com.kahlen.travelpal.DrawerActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MQTTServiceDelegate {
	
	public final static String ACTION_MESSAGE_ARRIVED = "com.kahlen.travelpal.MESSAGE_ARRIVED";
	public final static String ACTION_CONNECTION_ERROR = "com.kahlen.travelpal.CONNECTION_ERROR";
	public final static String INTENT_EXTRA_RECEIVED_NOTIFICATIO_TYPE = "com.kahlen.travelpal.RECEIVED_NOTIFICATION_TYPE";
	public final static String INTENT_EXTRA_RECEIVED_TOPIC = "com.kahlen.travelpal.RECEIVED_TOPIC";
	public final static String INTENT_EXTRA_RECEIVED_MESSAGE = "com.kahlen.travelpal.RECEIVED_MESSAGE";
	public final static String INTENT_EXTRA_ERROR_MESSAGE = "com.kahlen.travelpal.ERROR_MESSAGE";

	private static ArrayList<MQTTActivityCallBack> mActivityCallbacks = new ArrayList<MQTTActivityCallBack>();
	private static ArrayList<MQTTErrorCallBack> mErrorCallbacks = new ArrayList<MQTTErrorCallBack>();
	
	public static void registerActivityCallback( MQTTActivityCallBack callback ) {
		mActivityCallbacks.add(callback);
	}
	
	public static void unregisterActivityCallback( MQTTActivityCallBack callback ) {
		if ( mActivityCallbacks.contains(callback) )  {
			mActivityCallbacks.remove(callback);
		}
	}
	
	public static void registerErrorCallback( MQTTErrorCallBack callback ) {
		mErrorCallbacks.add(callback);
	}
	
	public static void unregisterErrorCallback( MQTTErrorCallBack callback ) {
		if ( mErrorCallbacks.contains(callback) )  {
			mErrorCallbacks.remove(callback);
		}
	}
	
	public static class MessageReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {			
			DrawerActivity.MQTTNotificationType notificationType = DrawerActivity.MQTTNotificationType.values()[intent.getIntExtra(INTENT_EXTRA_RECEIVED_NOTIFICATIO_TYPE, DrawerActivity.MQTTNotificationType.unknown.ordinal())];
			String topic = intent.getStringExtra( INTENT_EXTRA_RECEIVED_TOPIC );
			String message = intent.getStringExtra( INTENT_EXTRA_RECEIVED_MESSAGE );
			Log.d("kahlen", "MessageReceiver onReceive: ( " + topic + ", " + message + " )");
			
			for ( MQTTActivityCallBack callback: mActivityCallbacks ) {
				callback.messageReceived(notificationType, topic, message);
			}
			
		}
	}
	
	public static class ConnectionErrorReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {			
			String errorMsg = intent.getStringExtra( INTENT_EXTRA_ERROR_MESSAGE );
			Log.d("kahlen", "ConnectionErrorReceiver onReceive: " + errorMsg );
			
			for ( MQTTErrorCallBack callback: mErrorCallbacks ) {
				callback.mqttFail(errorMsg);
			}
			
		}
	}
	
}
