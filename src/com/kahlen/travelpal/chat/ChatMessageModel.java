package com.kahlen.travelpal.chat;

import com.kahlen.travelpal.mqtt.MQTTConfiguration;
import com.kahlen.travelpal.user.UserInfo;

public class ChatMessageModel {
	public String message;
	public boolean me;
	// TODO: define sender id
	public String senderId; 
	
	public ChatMessageModel() {
		message = "";
		me = true;
		senderId = UserInfo.getUserId();
	}
	
	public ChatMessageModel( String msg ) {
		message = msg;
		me = true;
		senderId = UserInfo.getUserId();
	}
	
	public ChatMessageModel( String msg, boolean m, String id ) {
		message = msg;
		me = m;
		senderId = id;
	}
	
}
