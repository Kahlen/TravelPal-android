package com.kahlen.travelpal.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.kahlen.travelpal.DrawerActivity;
import com.kahlen.travelpal.MyApplication;
import com.kahlen.travelpal.R;
import com.kahlen.travelpal.user.UserInfo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class MQTTService extends Service implements MqttCallback, MQTTTaskHandler {
	
	public static String ACTION_CONNECT = "com.kahlen.travelpal.connect";
	public static String ACTION_DISCONNECT = "com.kahlen.travelpal.disconnect";
	public static String ACTION_SUBSCRIBE = "com.kahlen.travelpal.subscribe";
	public static String ACTION_CONNECT_N_SUBSCRIBE = "com.kahlen.travelpal.connectnsubscribe";
	public static String ACTION_PUBLISH = "com.kahlen.travelpal.publish";
	
	public static String INTENT_EXTRA_SUBSCRIBE_TOPIC = "subscribe_topic";
	public static String INTENT_EXTRA_PUBLISH_TOPIC = "publish_topic";
	public static String INTENT_EXTRA_PUBLISH_MESSAGE = "publish_message";

	private Context mContext = this;
	private final IBinder mBinder = new MQTTServiceBinder();
		
	private static MqttClient mClient;

	@Override
	public void onCreate() {
		super.onCreate();
		if ( mClient == null ) {
			try {
				mClient = new MqttClient(MQTTConfiguration.BROKER_URL, UserInfo.getUserId(), new MemoryPersistence());
			} catch (MqttException e) {
				// broadcast message
				broadcastErrorMessage( e.getMessage() );

				e.printStackTrace();
			} catch (Exception e) {
				// TODO: UserInfo.getUserId() null
				// other errors
				e.printStackTrace();
				Log.d("kahlen", "MQTTService error, stop service!");
				stopSelf();
			}
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {		
		String action = intent.getAction();
		Log.d("kahlen", "MQTTService action: " + action);
		if ( ACTION_CONNECT.equals(action) ) {
			connectMQTTServer();
		} else if ( ACTION_DISCONNECT.equals(action) ) {
			disconnectMQTTServer();
			stopSelf();
		} else if ( ACTION_SUBSCRIBE.equals(action) ) {
			String topic = intent.getStringExtra( INTENT_EXTRA_SUBSCRIBE_TOPIC );
			if ( topic != null )
				subscribeTopic(topic);
		} else if ( ACTION_CONNECT_N_SUBSCRIBE.equals(action) ) {
			connectMQTTServer();
			String topic = intent.getStringExtra( INTENT_EXTRA_SUBSCRIBE_TOPIC );
			if ( topic != null )
				subscribeTopic(topic);
		} else if ( ACTION_PUBLISH.equals(action) ) {
			String topic = intent.getStringExtra( INTENT_EXTRA_PUBLISH_TOPIC );
			String message = intent.getStringExtra( INTENT_EXTRA_PUBLISH_MESSAGE );
			publishOnTopic( topic, message );
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
		disconnectMQTTServer();
	}
	
	public class MQTTServiceBinder extends Binder {
		public MQTTService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MQTTService.this;
        }
    }
	
	private void broadcastErrorMessage( String errorMsg ) {
		Intent broadcastIntent = new Intent();
        broadcastIntent.setAction( MQTTServiceDelegate.ACTION_CONNECTION_ERROR );
        broadcastIntent.putExtra(MQTTServiceDelegate.INTENT_EXTRA_ERROR_MESSAGE, errorMsg );
        sendBroadcast(broadcastIntent); 
	}
	
	// --- MQTT functions ---
	public void connectMQTTServer() {
		// use AsyncTask to avoid ANR
		if ( !mClient.isConnected() ) {
			MQTTAsyncTask task = new MQTTAsyncTask( mContext, mClient, this );
			task.execute( new Object[]{MQTTAsyncTask.TaskType.connect, this} );
		}
	}
	
	public void disconnectMQTTServer() {		
		if ( mClient.isConnected() ) {
			MQTTAsyncTask task = new MQTTAsyncTask( mContext, mClient, this );
			task.execute( new Object[]{MQTTAsyncTask.TaskType.disconnect} );
		}
	}
	
	public void subscribeTopic( String topic ) {
		MQTTAsyncTask task = new MQTTAsyncTask( mContext, mClient, this );
		task.execute( new Object[]{MQTTAsyncTask.TaskType.subscribe, topic} );
	}
	
	public void publishOnTopic( String topic, String message ) {
		publishOnTopic( topic, message, 2 );
	}
	
	public void publishOnTopic( String topic, String message, int qos ) {
		if ( !isConnected() ) {
			// client is not connected
			broadcastErrorMessage( "Client is not connected" );
			return;
		}
		
		MQTTAsyncTask task = new MQTTAsyncTask( mContext, mClient, this );
		task.execute( new Object[]{MQTTAsyncTask.TaskType.publish, topic, message, qos} );
	}
	
	public boolean isConnected() {
		return mClient.isConnected(); 
	}
	
	// --- MQTT connect callback ---
	@Override
	public void connectionFail( Exception e ) {
		if ( e != null ) {
			e.printStackTrace();
			broadcastErrorMessage( e.getMessage() );
		}
	}
	
	// --- MQTT call back ---
	@Override
	public void connectionLost(Throwable arg0) {
		Log.d("kahlen", "Connection lost");
		arg0.printStackTrace();		
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		Log.d("kahlen", "Delivery complete");
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		String msg = new String(message.getPayload(), "UTF-8");
		Log.d("kahlen", "receive new message on topic (" + topic + "): " + msg);
		
		// if app is not at foreground, create notification
		if ( !MyApplication.isActivityVisible() ) {
			Intent resultIntent = new Intent(mContext, DrawerActivity.class);
			resultIntent.putExtra("new_message", true);
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
		
		// broadcast message
		Intent broadcastIntent = new Intent();
        broadcastIntent.setAction( MQTTServiceDelegate.ACTION_MESSAGE_ARRIVED );
        broadcastIntent.putExtra(MQTTServiceDelegate.INTENT_EXTRA_RECEIVED_TOPIC, topic);
        broadcastIntent.putExtra(MQTTServiceDelegate.INTENT_EXTRA_RECEIVED_MESSAGE , msg);
        sendBroadcast(broadcastIntent); 
			
	}

}
