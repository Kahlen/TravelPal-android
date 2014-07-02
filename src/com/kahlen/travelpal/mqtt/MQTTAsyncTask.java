package com.kahlen.travelpal.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class MQTTAsyncTask extends AsyncTask<Object, Void, Exception> {
	
	public static enum TaskType { connect, disconnect, subscribe, publish };
	
	private Context mContext;
	private MqttClient mClient;
	private MQTTTaskHandler mHandler;
	
	public MQTTAsyncTask( Context context, MqttClient client, MQTTTaskHandler handler ) {
		mContext = context;
		mClient = client;
		mHandler = handler;
	}

	@Override
	protected Exception doInBackground(Object... params) {
		TaskType type = (TaskType) params[0];
		Log.d("kahlen", "task type = " + type);
		switch ( type ) {
			case connect:
				
				try {
					MQTTActivityCallBack callback = (MQTTActivityCallBack) params[1];
					MqttConnectOptions options = new MqttConnectOptions();
					// set clean session to false so that when reconnected, it gets messages happen when the connection was lost
					options.setCleanSession( false );
					options.setKeepAliveInterval(30);
					mClient.setCallback(new MQTTCallBack(mContext, callback));
					mClient.connect( options );
		        } catch (MqttException e) {
		            e.printStackTrace();
		            return e;
		        } catch (Exception e) {
					e.printStackTrace();
					return e;
				}
				
				break;
				
			case disconnect:
				
				try {
					if ( mClient.isConnected() )
						mClient.disconnect();
		        } catch (MqttException e) {
		            e.printStackTrace();
		            return e;
		        }
				
				break;
			
			case subscribe:
				
				try {
					String topic = (String) params[1];
					mClient.unsubscribe(topic);
					mClient.subscribe(topic, 2);
				} catch (MqttException e) {
		            e.printStackTrace();
		            return e;
				} catch (Exception e) {
					e.printStackTrace();
					return e;
				}
				
				break;
				
			case publish:

				try {
					String topic = (String) params[1];
					String message = (String) params[2];
					int qos = (Integer) params[3];
					
					MqttTopic mTopic = mClient.getTopic(topic);
					MqttMessage mMessage = new MqttMessage(message.getBytes());
					mMessage.setQos(qos);
					
					mTopic.publish(mMessage);
				} catch (MqttPersistenceException e) {
					e.printStackTrace();
					return e;
				} catch (MqttException e) {
					e.printStackTrace();
					return e;
				} catch (Exception e) {
					e.printStackTrace();
					return e;
				}
				
				break;
		}
		return null;
	}

	@Override
	protected void onPostExecute(Exception result) {
		super.onPostExecute( result );
		if ( result != null )
			mHandler.connectionFail( result );
	}

}
