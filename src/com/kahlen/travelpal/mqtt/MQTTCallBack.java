package com.kahlen.travelpal.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.kahlen.travelpal.MainActivity;
import com.kahlen.travelpal.MyApplication;
import com.kahlen.travelpal.R;
import com.kahlen.travelpal.utilities.ConnectivityUtils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class MQTTCallBack implements MqttCallback {
	
	private Context mContext;
	private MQTTActivityCallBack mActivityCallBack;
	
	public MQTTCallBack( Context context, MQTTActivityCallBack callback ) {
		mContext = context;
		mActivityCallBack = callback;
	}

	@Override
	public void connectionLost(Throwable arg0) {
		Log.d("kahlen", "Connection lost");
		arg0.printStackTrace();
		
//		if ( !ConnectivityUtils.isConnectionOn(mContext) ) {
			/*final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                	Log.d("kahlen", "receive event");
                	if ( ConnectivityUtils.isConnectionOn(mContext) ) {
                		Log.d("kahlen", "internet is connected");
                		MQTTClientController controller = MQTTClientController.getInstance(mContext);
                		controller.connectMQTTServer();
                	}
                }
            };

            Log.d("kahlen", "register receiver");
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            mContext.registerReceiver(broadcastReceiver, intentFilter);*/
		}
		
//	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		Log.d("kahlen", "Delivery complete");
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		Log.d("kahlen", "receive new message on topic (" + topic + "): " + new String(message.getPayload(), "UTF-8"));
		
		if ( MyApplication.isActivityVisible() ) {
			if ( mActivityCallBack != null ) {
				mActivityCallBack.messageReceived( new String(message.getPayload(), "UTF-8") );
			} else {
				Log.d("kahlen", "Message arrived: " + new String(message.getPayload(), "UTF-8") );
			}
		} else {
			Intent resultIntent = new Intent(mContext, MainActivity.class);
			resultIntent.putExtra("topic", topic);
			resultIntent.putExtra("message", new String(message.getPayload(), "UTF-8"));
			PendingIntent resultPendingIntent =
				    PendingIntent.getActivity(
				    mContext,
				    0,
				    resultIntent,
				    PendingIntent.FLAG_UPDATE_CURRENT
				);
			
			NotificationManager mNotifyMgr = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
			NotificationCompat.Builder mBuilder =
				    new NotificationCompat.Builder(mContext)
				    .setSmallIcon(R.drawable.ic_launcher)
				    .setAutoCancel(true)
				    .setContentTitle("TravelPal")
				    .setContentText("new message received!")
				    .setContentIntent(resultPendingIntent);
			mNotifyMgr.notify(789, mBuilder.build());	
		}
			
	}

}
