package com.kahlen.travelpal.mqtt;

public interface MQTTActivityCallBack {
	void messageReceived( String topic, String message );
}
