package com.kahlen.travelpal.gcm;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.kahlen.travelpal.mqtt.MQTTConfiguration;
import com.kahlen.travelpal.utilities.AccountUtils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class GCMController {
	
	final private static String SENDER_ID = "sender_id";
	final public static String SHARED_PREF_FILE_GCM = "GCM_DATA";
	final public static String SHARED_PREF_KEY_REGISTRATION_ID = "registration_id";
	
	private static GoogleCloudMessaging gcm;
	private static String regid;
	
	public static void registerIfNeed( Context context ) {
//		if ( checkPlayServices( (Activity)context ) ) {
			gcm = GoogleCloudMessaging.getInstance(context);
			regid = getRegistrationId(context);
			if (regid.isEmpty()) {
                registerInBackground( context );
            }
//		}
	}
	
	private static String getRegistrationId( Context context ) {
	    final SharedPreferences prefs = getGCMPreferences(context);
	    String registrationId = prefs.getString(SHARED_PREF_KEY_REGISTRATION_ID, "");
	    if (registrationId.isEmpty()) {
	        Log.i("kahlen", "Registration not found.");
	        return "";
	    }
	    return registrationId;
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private static SharedPreferences getGCMPreferences( Context context ) {
	    // This sample app persists the registration ID in shared preferences, but
	    // how you store the regID in your app is up to you.
	    return context.getSharedPreferences(SHARED_PREF_FILE_GCM, Context.MODE_PRIVATE);
	}
	
	private static boolean checkPlayServices( Activity context ) {
	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
	    if (resultCode != ConnectionResult.SUCCESS) {
	        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	            GooglePlayServicesUtil.getErrorDialog(resultCode, context, 9000).show();
	        } else {
	            Log.i("kahlen", "This device is not supported.");
	        }
	        return false;
	    }
	    return true;
	}
	
	private static void registerInBackground( final Context context ) {
	    new AsyncTask<Void, Void, Void>() {
	        @Override
	        protected Void doInBackground(Void... params) {
	            try {
	                if (gcm == null) {
	                    gcm = GoogleCloudMessaging.getInstance(context);
	                }
	                regid = gcm.register(SENDER_ID);

	                // You should send the registration ID to your server over HTTP,
	                // so it can use GCM/HTTP or CCS to send messages to your app.
	                // The request to your server should be authenticated if your app
	                // is using accounts.
	                
	                //instantiates httpclient to make request
	    		    DefaultHttpClient httpclient = new DefaultHttpClient();

	    		    //url with the post data
	    		    String path = MQTTConfiguration.REGISTER_GCM;
	    		    HttpPost httpost = new HttpPost(path);
	    		    JSONObject requestBody = new JSONObject();
	    		    requestBody.put("_id", AccountUtils.getUserid(context));
	    		    requestBody.put("registrationId", regid);

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
	    		    Log.d("kahlen", "send registration id status = " + status);
	    		    if ( status == HttpStatus.SC_OK || status == HttpStatus.SC_CREATED ) {
	    		    	Log.d("kahlen", "send registration id ok");
	    		    }
	    		    
	    		    

	                // For this demo: we don't need to send it because the device
	                // will send upstream messages to a server that echo back the
	                // message using the 'from' address in the message.

	                // Persist the regID - no need to register again.
	                storeRegistrationId(context, regid);
	            } catch (Exception ex) {
	                ex.printStackTrace();
	                // If there is an error, don't just keep trying to register.
	                // Require the user to click a button again, or perform
	                // exponential back-off.
	            }
				return null;
	            
	        }

	    }.execute(null, null, null);
	}
	
	private static void storeRegistrationId(Context context, String regId) {
	    final SharedPreferences prefs = getGCMPreferences(context);
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.putString(SHARED_PREF_KEY_REGISTRATION_ID, regId);
	    editor.commit();
	}
	
}
