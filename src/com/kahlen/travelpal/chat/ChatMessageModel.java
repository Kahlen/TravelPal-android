package com.kahlen.travelpal.chat;

public class ChatMessageModel {
	public String message;
	public boolean me;
	public String senderId; 
	
	public ChatMessageModel() {
		message = "";
		me = true;
	}
	
	public ChatMessageModel( String msg ) {
		message = msg;
		me = true;
	}
	
	public ChatMessageModel( String msg, boolean m, String id ) {
		message = msg;
		me = m;
		senderId = id;
	}
	
}
