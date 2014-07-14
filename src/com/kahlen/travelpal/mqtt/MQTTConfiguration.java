package com.kahlen.travelpal.mqtt;

public class MQTTConfiguration {
	public static String BROKER_URL = "tcp://broker.mqttdashboard.com:1883";
//	public static final String CLIENT_ID = android.os.Build.SERIAL;
	
	// server http request
	public static String SERVER_URL = "http://localhost:9000";
	public static String LOGIN_URI = SERVER_URL + "/login";
	public static String FIND_FRIENDS_URI = SERVER_URL + "/searchf";
	public static String GET_CHAT_HISTORY = SERVER_URL + "/history";
}
