package com.kahlen.travelpal.mytrip;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.kahlen.travelpal.mqtt.MQTTConfiguration;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class TripContentTask extends AsyncTask<String, Void, JSONObject> {
	
	private Context mContext;
	private TripContentCallback mCallback;
	
	public TripContentTask( Context context, TripContentCallback callback ) {
		mContext = context;
		mCallback = callback;
	}

	@Override
	protected JSONObject doInBackground(String... params) {
		Log.d("kahlen", "TripContentTask with id: " + params[0]);
		try {		 
            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            
            String path = MQTTConfiguration.GET_TRIP_CONTENT_URI + "?iid=" + params[0];
            HttpGet httpGet = new HttpGet(path);
 
            // make GET request to the given URL
            HttpResponse response = httpclient.execute(httpGet);
            
            // if no content, return null directly
            if ( response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT ) {
            	Log.d("kahlen", "this itinerary doesn't have any feed");
            	return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            StringBuilder builder = new StringBuilder();
            for (String line = null; (line = reader.readLine()) != null;) {
                builder.append(line).append("\n");
            }
            
            JSONObject result = new JSONObject(builder.toString());
            Log.d("kahlen", "TripContentTask result: " + result);
            
            return result;
 
        } catch (Exception e) {
            e.printStackTrace();
        }
		return null;
	}

	@Override
	protected void onPostExecute(JSONObject result) {
		mCallback.tripContentResult(result);
	}

}
