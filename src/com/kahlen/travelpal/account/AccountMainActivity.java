package com.kahlen.travelpal.account;

import com.kahlen.travelpal.MyApplication;
import com.kahlen.travelpal.R;

import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AccountMainActivity extends AccountAuthenticatorActivity {
	
	final public static String TRAVELPAL_ACCOUNT_TYPE = "com.kahlen.travelpal";
	final public static String ACCOUNT_USER_DATA_USER_NAME = "userName";
	
	final public static String INTENT_EXTRA_FROM_SETTINGS = "from_settings";
	final public static int ACTIVITY_CREATE_REQUEST_CODE = 123;
	final public static int ACTIVITY_LOGIN_REQUEST_CODE = 456;
	
	Context mContext = this;
	boolean isFromSettings = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		isFromSettings = intent.hasExtra( AccountManager.KEY_ACCOUNT_TYPE );
		
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
				if ( isFromSettings ) {
					Intent intent = getIntent();
					intent.setClass( mContext, CreateAccountActivity.class );
					intent.putExtra(INTENT_EXTRA_FROM_SETTINGS, isFromSettings);
					startActivityForResult( intent, ACTIVITY_CREATE_REQUEST_CODE );
				} else {
					// from app itself
					Intent intent = new Intent(mContext, CreateAccountActivity.class);
					startActivity( intent );
					finish();
				}
				
			}
			
		});
		
		Button loginBtn = (Button) findViewById( R.id.main_login_btn );
		loginBtn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if ( isFromSettings ) {
					Intent intent = getIntent();
					intent.setClass( mContext, LoginAccountActivity.class );
					intent.putExtra(INTENT_EXTRA_FROM_SETTINGS, isFromSettings);
					startActivityForResult( intent, ACTIVITY_LOGIN_REQUEST_CODE );
				} else {
					// from app itself
					Intent intent = new Intent( mContext, LoginAccountActivity.class );
					startActivity( intent );
					finish();
				}
			}
			
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("kahlen", "requestCode: " + requestCode + ", resultCode: " + resultCode);
		if ( resultCode == RESULT_OK ) {
			setAccountAuthenticatorResult(data.getExtras());
		}
		
        finish();
	}


}
