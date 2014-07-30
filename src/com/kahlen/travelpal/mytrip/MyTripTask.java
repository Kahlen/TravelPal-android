package com.kahlen.travelpal.mytrip;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.kahlen.travelpal.mqtt.MQTTConfiguration;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class MyTripTask extends AsyncTask<String, Void, JSONObject> {
	
	private Context mContext;
	private MyTripCallback mCallback;
	
	public MyTripTask( Context context, MyTripCallback callback ) {
		mContext = context;
		mCallback = callback;
	}

	@Override
	protected JSONObject doInBackground(String... params) {
		Log.d("kahlen", "MyTripTask with id: " + params[0]);
		try {		 
            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            
            String path = MQTTConfiguration.GET_MY_TRIPS_URI + "?user=" + params[0];
            HttpGet httpGet = new HttpGet(path);
 
            // make GET request to the given URL
            HttpResponse response = httpclient.execute(httpGet);
 
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            StringBuilder builder = new StringBuilder();
            for (String line = null; (line = reader.readLine()) != null;) {
                builder.append(line).append("\n");
            }
            
            JSONObject result = new JSONObject(builder.toString());
            Log.d("kahlen", "MyTripTask result: " + result);
            
            return result;
 
        } catch (Exception e) {
            e.printStackTrace();
        }
		return null;
	}

	@Override
	protected void onPostExecute(JSONObject result) {
		mCallback.mytripListResult(result);
	}
	

}
