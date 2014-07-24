package com.kahlen.travelpal.newtrip;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kahlen.travelpal.chat.FriendModel;
import com.kahlen.travelpal.mqtt.MQTTConfiguration;

import android.os.AsyncTask;
import android.util.Log;

public class NewTripFriendsTask extends AsyncTask<String, Void, ArrayList<FriendModel>>{
	
	private NewTripFriendsCallback mCallback;
	
	public NewTripFriendsTask( NewTripFriendsCallback callback ) {
		mCallback = callback;
	}

	@Override
	protected ArrayList<FriendModel> doInBackground(String... params) {
		Log.d("kahlen", "NewTripFriendsTask with id: " + params[0]);
		try {		 
            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            
            String path = MQTTConfiguration.FIND_FRIENDS_WITH_RELATION_URI + "?id=" + params[0];
            HttpGet httpGet = new HttpGet(path);
 
            // make GET request to the given URL
            HttpResponse response = httpclient.execute(httpGet);
 
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            StringBuilder builder = new StringBuilder();
            for (String line = null; (line = reader.readLine()) != null;) {
                builder.append(line).append("\n");
            }
            
            JSONObject result = new JSONObject(builder.toString());
            Log.d("kahlen", "NewTripFriendsTask result: " + result);
            JSONArray friends = result.getJSONArray( "friends" );
            ArrayList<FriendModel> fResult = new ArrayList<FriendModel>();
            for ( int i = 0; i < friends.length(); i++ ) {
            	JSONObject f = friends.getJSONObject(i);
            	fResult.add( new FriendModel(f.getString("_id"), f.getString("name"), false) );
            }
            
            return fResult;
 
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		return null;
	}

	@Override
	protected void onPostExecute(ArrayList<FriendModel> result) {
		mCallback.getFriendsResult(result);
	}

}
