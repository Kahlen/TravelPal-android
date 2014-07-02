package com.kahlen.travelpal.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import android.content.Context;
import android.widget.Toast;

public class MQTTClientController implements MQTTTaskHandler {

	private static MqttClient mClient;
	private static Context mContext;
	private static MQTTClientController mController = null;
	
	private MQTTClientController( Context context ) {
		mContext = context;
		try {
//			MqttDefaultFilePersistence persistence = new MqttDefaultFilePersistence("/kahlen");
			mClient = new MqttClient(MQTTConfiguration.BROKER_URL, MQTTConfiguration.CLIENT_ID, new MemoryPersistence());
		} catch (MqttException e) {
			Toast.makeText(mContext, "Something went wrong!" + e.getMessage(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}

	}
	
	public static MQTTClientController getInstance( Context context ) {
		if ( mController == null ) {
			mController = new MQTTClientController( context );
		}
		
		return mController;
	}
	
	public boolean isConnected() {
		return mClient.isConnected(); 
	}
	
	public void connectMQTTServer( MQTTActivityCallBack callback ) {
		// use AsyncTask to avoid ANR
		MQTTAsyncTask task = new MQTTAsyncTask( mContext, mClient, this );
		task.execute( new Object[]{MQTTAsyncTask.TaskType.connect, callback} );
	}
	
	public void connectMQTTServer() {
		connectMQTTServer( null );
	}
	
	public void disconnectMQTTServer() {		
		MQTTAsyncTask task = new MQTTAsyncTask( mContext, mClient, this );
		task.execute( new Object[]{MQTTAsyncTask.TaskType.disconnect} );
	}
	
	public void subscribeTopic( String topic ) {
		MQTTAsyncTask task = new MQTTAsyncTask( mContext, mClient, this );
		task.execute( new Object[]{MQTTAsyncTask.TaskType.subscribe, topic} );
	}
	
	// default QoS is 1
	public void publishOnTopic( String topic, String message ) {
		publishOnTopic( topic, message, 2 );
	}
	
	public void publishOnTopic( String topic, String message, int qos ) {
		if ( !mClient.isConnected() ) {
			// client is not connected
			Toast.makeText(mContext, "Client is not connected", Toast.LENGTH_LONG).show();
			return;
		}
		
		MQTTAsyncTask task = new MQTTAsyncTask( mContext, mClient, this );
		task.execute( new Object[]{MQTTAsyncTask.TaskType.publish, topic, message, qos} );
	}
	
	public void registerCallback( MQTTCallBack callback ) {
		mClient.setCallback( callback );
	}

	@Override
	public void connectionFail( Exception e ) {
		if ( e != null ) {
			Toast.makeText(mContext, "Something went wrong!" + e.getMessage(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}
	
}
