package com.kahlen.travelpal;

import org.apache.http.HttpStatus;
import org.json.JSONObject;

import com.kahlen.travelpal.user.UserLoginCallback;
import com.kahlen.travelpal.user.UserLoginTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements UserLoginCallback {
	
	UserLoginCallback mCallback = this;
	Context mContext = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView( R.layout.activity_login );
		setLogInBtn();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MyApplication.activityResumed();
	}

	@Override
	protected void onPause() {
		super.onPause();
		MyApplication.activityPaused();
	}
	
	private void setLogInBtn() {
		Button loginBtn = (Button) findViewById( R.id.login_btn );
		loginBtn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Log.d("kahlen", "login button clicked");
				// check if both username and password are not empty
				EditText userEditTxt = (EditText) findViewById(R.id.user_id_edit_txt);
				EditText passwordEditTxt = (EditText) findViewById(R.id.user_password_edit_txt);
				String username = userEditTxt.getText().toString().replaceAll("\\s+","");
				String password = passwordEditTxt.getText().toString().replaceAll("\\s+","");
				if ( username.isEmpty() || password.isEmpty() ) {
					Toast.makeText(mContext, "username or password is wrong", Toast.LENGTH_LONG).show();
				} else {
					try {
						Log.d("kahlen", "start to log in");
						JSONObject params = new JSONObject();
						params.put("_id", username);
						params.put("password", password);
						params.put("name", "");
						UserLoginTask task = new UserLoginTask( mCallback );
						task.execute(params);
					} catch ( Exception e ) {
						e.printStackTrace();
					}
					
				}

			}
			
		});
	}

	@Override
	public void loginResult(int statusCode) {
		if ( statusCode == HttpStatus.SC_OK ) {
			Log.d("kahlen", "login successfully");
			// start next activity
			Intent intent = new Intent( mContext, DrawerActivity.class );
			startActivity( intent );
			finish();
		} else if ( statusCode == HttpStatus.SC_NOT_FOUND ){
			Toast.makeText(mContext, "username or password is wrong", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(mContext, "server error: " + statusCode, Toast.LENGTH_LONG).show();
		}
		
	}

}
