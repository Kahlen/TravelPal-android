package com.kahlen.travelpal.mqtt;

public class MQTTConfiguration {
	public static String BROKER_URL = "tcp://broker.mqttdashboard.com:1883";
	
	// server http request
	final public static String SERVER_URL = "http://localhost:9000";
	final public static String LOGIN_URI = SERVER_URL + "/login";
	final public static String CREATE_ACCOUNT_URI = SERVER_URL + "/create";
	final public static String FIND_FRIENDS_URI = SERVER_URL + "/searchf";
	final public static String GET_CHAT_HISTORY = SERVER_URL + "/history";
	final public static String FIND_FRIENDS_WITH_RELATION_URI = SERVER_URL + "/searchff";
	final public static String ADD_ITINERARY_URI = SERVER_URL + "/additinerary";
	final public static String GET_MY_TRIPS_URI = SERVER_URL + "/itinerary";
	final public static String GET_TRIP_CONTENT_URI = SERVER_URL + "/getitineraryjson";
	final public static String COMMENT_ITINERARY = SERVER_URL + "/itinerarycomment";
	final public static String CREATE_FEED_ITINERARY = SERVER_URL + "/updateitinerary";
	final public static String ADD_FRIEND_URI = SERVER_URL + "/addf";
	final public static String REMOVE_FRIEND_URI = SERVER_URL + "/removef";
}
