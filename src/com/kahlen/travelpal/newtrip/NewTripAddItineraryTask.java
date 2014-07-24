package com.kahlen.travelpal.newtrip;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.kahlen.travelpal.mqtt.MQTTConfiguration;

import android.os.AsyncTask;
import android.util.Log;

public class NewTripAddItineraryTask extends AsyncTask<JSONObject, Void, Boolean> {
	
	private NewTripAddItineraryCallback mCallback;
	
	public NewTripAddItineraryTask( NewTripAddItineraryCallback callback ) {
		mCallback = callback;
	}

	@Override
	protected Boolean doInBackground(JSONObject... params) {
		JSONObject requestBody = params[0];
		
		try {
			//instantiates httpclient to make request
		    DefaultHttpClient httpclient = new DefaultHttpClient();

		    //url with the post data
		    String path = MQTTConfiguration.ADD_ITINERARY_URI;
		    HttpPost httpost = new HttpPost(path);

		    //convert parameters into JSON object
		    Log.d("kahlen", "requestBody = " + requestBody);

		    //passes the results to a string builder/entity
		    StringEntity se = new StringEntity(requestBody.toString());

		    //sets the post request as the resulting string
		    httpost.setEntity(se);
		    //sets a request header so the page receving the request
		    //will know what to do with it
		    httpost.setHeader("Accept", "application/json");
		    httpost.setHeader("Content-type", "application/json");

		    //Handles what is returned from the page 
		    HttpResponse response = httpclient.execute(httpost);
		    int status = response.getStatusLine().getStatusCode();
		    Log.d("kahlen", "log in status = " + status);
		    if ( status == HttpStatus.SC_OK || status == HttpStatus.SC_CREATED ) {
		    	return true;
		    }
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		mCallback.addItineraryResult(result);
	}

}
