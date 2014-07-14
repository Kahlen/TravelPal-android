package com.kahlen.travelpal.chat;

import org.json.JSONObject;

public interface ChatHistoryCallback {
	void getChatHistoryResult( JSONObject history );
}
