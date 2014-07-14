package com.kahlen.travelpal.chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.kahlen.travelpal.mqtt.MQTTConfiguration;

import android.os.AsyncTask;
import android.util.Log;

public class ChatHistoryTask extends AsyncTask<String, Void, JSONObject> {
	
	private ChatHistoryCallback mCallback;
	
	public ChatHistoryTask( ChatHistoryCallback callback ) {
		mCallback = callback;
	}
	

	@Override
	protected JSONObject doInBackground(String... params) {
		try {		 
			StringBuilder usersParamBuilder = new StringBuilder();
			for ( String u: params )
				usersParamBuilder.append(u + ",");
			String usersParam = usersParamBuilder.substring(0, usersParamBuilder.length()-1);
			Log.d("kahlen", "get chat history with users: " + usersParam);
			
            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            
            String path = MQTTConfiguration.GET_CHAT_HISTORY + "?users=" + usersParam;
            HttpGet httpGet = new HttpGet(path);
 
            // make GET request to the given URL
            HttpResponse response = httpclient.execute(httpGet);
 
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            StringBuilder builder = new StringBuilder();
            for (String line = null; (line = reader.readLine()) != null;) {
                builder.append(line).append("\n");
            }
            
            JSONObject result = new JSONObject(builder.toString());
            Log.d("kahlen", "ChatHistory result: " + result);
            
            return result;
 
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		return null;
	}

	@Override
	protected void onPostExecute(JSONObject result) {
		mCallback.getChatHistoryResult(result);
	}

}
