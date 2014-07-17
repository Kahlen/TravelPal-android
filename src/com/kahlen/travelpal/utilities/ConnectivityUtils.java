package com.kahlen.travelpal.utilities;

import com.kahlen.travelpal.mqtt.MQTTService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class ConnectivityUtils {

	public static boolean isConnectionOn( Context context ) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo netInfo = cm.getActiveNetworkInfo();
		    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
		        return true;
		    }
		    return false;
	}
	
	public static class IntenetStateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent i) {
			if ( isConnectionOn( context ) ) {
				Log.d("kahlen", "internet state change, reconnect MQTT");
				// start service
				Intent intent = new Intent(context, MQTTService.class);
				intent.setAction( MQTTService.ACTION_CONNECT );
		        context.startService(intent);
			}
			
		}
		
	}
	
}
