package com.kahlen.travelpal.user;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.kahlen.travelpal.mqtt.MQTTConfiguration;

import android.os.AsyncTask;
import android.util.Log;

public class UserLoginTask extends AsyncTask<JSONObject, Void, Integer> {
	
	private UserLoginCallback mCallback;
	
	public UserLoginTask( UserLoginCallback callback ) {
		mCallback = callback;
	}

	@Override
	protected Integer doInBackground(JSONObject... params) {
		try {
			//instantiates httpclient to make request
		    DefaultHttpClient httpclient = new DefaultHttpClient();

		    //url with the post data
		    String path = MQTTConfiguration.LOGIN_URI;
		    HttpPost httpost = new HttpPost(path);

		    //convert parameters into JSON object
		    JSONObject holder = params[0];
		    Log.d("kahlen", "login params = " + holder);

		    //passes the results to a string builder/entity
		    StringEntity se = new StringEntity(holder.toString());

		    //sets the post request as the resulting string
		    httpost.setEntity(se);
		    //sets a request header so the page receving the request
		    //will know what to do with it
		    httpost.setHeader("Accept", "application/json");
		    httpost.setHeader("Content-type", "application/json");

		    //Handles what is returned from the page 
		    HttpResponse response = httpclient.execute(httpost);
		    int status = response.getStatusLine().getStatusCode();
		    if ( status == 200 ) {
		    	UserInfo.setUser( holder.getString("_id") );
		    }
		    Log.d("kahlen", "log in status = " + status);
		    
		    return status;
		    
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
		return HttpStatus.SC_INTERNAL_SERVER_ERROR;		
	}

	@Override
	protected void onPostExecute(Integer result) {
		mCallback.loginResult( result );
	}
	

}
