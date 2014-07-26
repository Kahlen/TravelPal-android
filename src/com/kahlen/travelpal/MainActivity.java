package com.kahlen.travelpal;

import org.apache.http.HttpStatus;
import org.json.JSONObject;

import com.kahlen.travelpal.account.CreateAccountActivity;
import com.kahlen.travelpal.account.LoginAccountActivity;
import com.kahlen.travelpal.account.UserAccountCallback;
import com.kahlen.travelpal.account.UserAccountTask;

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

public class MainActivity extends Activity implements UserAccountCallback {
	
	UserAccountCallback mCallback = this;
	Context mContext = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView( R.layout.activity_account );
		setBtns();
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
	
	private void setBtns() {
		Button createBtn = (Button) findViewById( R.id.main_create_account_btn );
		createBtn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent( mContext, CreateAccountActivity.class );
				startActivity( intent );
				finish();
			}
			
		});
		
		Button loginBtn = (Button) findViewById( R.id.main_login_btn );
		loginBtn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent( mContext, LoginAccountActivity.class );
				startActivity( intent );
				finish();
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
