package com.kahlen.travelpal.chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.kahlen.travelpal.mqtt.MQTTConfiguration;

import android.os.AsyncTask;
import android.util.Log;

public class FindFriendsTask extends AsyncTask<String, Void, JSONObject>{
	
	private FindFriendsCallback mCallback;
	
	public FindFriendsTask( FindFriendsCallback callback ) {
		mCallback = callback;
	}

	@Override
	protected JSONObject doInBackground(String... params) {
		Log.d("kahlen", "FindFriends with id: " + params[0]);
		try {		 
            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            
            String path = MQTTConfiguration.FIND_FRIENDS_URI + "?id=" + params[0];
            HttpGet httpGet = new HttpGet(path);
 
            // make GET request to the given URL
            HttpResponse response = httpclient.execute(httpGet);
 
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            StringBuilder builder = new StringBuilder();
            for (String line = null; (line = reader.readLine()) != null;) {
                builder.append(line).append("\n");
            }
            
            JSONObject result = new JSONObject(builder.toString());
            Log.d("kahlen", "FindFriends result: " + result);
            
            return result;
 
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		return null;
	}

	@Override
	protected void onPostExecute(JSONObject result) {
		mCallback.getFriendResult(result);
	}
	

}
