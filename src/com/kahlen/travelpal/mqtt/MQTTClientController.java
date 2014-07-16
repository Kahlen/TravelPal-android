package com.kahlen.travelpal.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.kahlen.travelpal.user.UserInfo;

import android.content.Context;

public class MQTTClientController implements MQTTTaskHandler {

	private static MqttClient mClient;
	private static Context mContext;
	private static MQTTClientController mController = null;
	private static MQTTErrorCallBack mErrorCallback;
	
	private MQTTClientController( Context context ) {
		mContext = context;
		try {
//			MqttDefaultFilePersistence persistence = new MqttDefaultFilePersistence("/kahlen");
			mClient = new MqttClient(MQTTConfiguration.BROKER_URL, UserInfo.getUserId(), new MemoryPersistence());
		} catch (MqttException e) {
			if ( mErrorCallback != null ) {
				mErrorCallback.mqttFail( "Something went wrong!" + e.getMessage() );
			}
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
			mErrorCallback.mqttFail( "Client is not connected" );
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
			e.printStackTrace();
			if ( mErrorCallback != null ) {
				mErrorCallback.mqttFail( "Something went wrong!" + e.getMessage() );
			}
		}
	}
	
	public void setErrorCallback( MQTTErrorCallBack callback ) {
		mErrorCallback = callback;
	}
	
}
