package com.kahlen.travelpal.mqtt;

import com.kahlen.travelpal.DrawerActivity;

public interface MQTTActivityCallBack {
	void messageReceived( DrawerActivity.MQTTNotificationType notificationType, String topic, String message );
}
