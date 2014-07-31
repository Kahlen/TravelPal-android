package com.kahlen.travelpal.mytrip;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.kahlen.travelpal.mqtt.MQTTConfiguration;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class TripContentCommentTask extends AsyncTask<JSONObject, Void, Void>{
	
	private Context mContext;
	
	public TripContentCommentTask( Context context ) {
		mContext = context;
	}

	@Override
	protected Void doInBackground(JSONObject... params) {
		JSONObject requestBody = params[0];
		
		try {
			//instantiates httpclient to make request
		    DefaultHttpClient httpclient = new DefaultHttpClient();

		    //url with the post data
		    String path = MQTTConfiguration.COMMENT_ITINERARY;
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
		    Log.d("kahlen", "send comment status = " + status);
		    if ( status == HttpStatus.SC_OK || status == HttpStatus.SC_CREATED ) {
		    	Log.d("kahlen", "send comment ok");
		    }
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		return null;
	}

}
