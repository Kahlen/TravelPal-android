package com.kahlen.travelpal.account;

import org.apache.http.HttpStatus;
import org.json.JSONObject;

import com.kahlen.travelpal.DrawerActivity;
import com.kahlen.travelpal.MyApplication;
import com.kahlen.travelpal.R;

import android.accounts.Account;
import android.accounts.AccountManager;
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

public class LoginAccountActivity extends Activity implements UserAccountCallback {
	
	UserAccountCallback mCallback = this;
	Context mContext = this;
	UserModel mUser;
	boolean isFromSettings = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d("kahlen", "--- LoginAccountActivity onCreate");
		
		isFromSettings = getIntent().getBooleanExtra(AccountMainActivity.INTENT_EXTRA_FROM_SETTINGS, false);

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
						mUser = new UserModel(username, password);
						UserAccountTask task = new UserAccountTask( mCallback, UserAccountTask.TaskType.login );
						task.execute(params);
					} catch ( Exception e ) {
						e.printStackTrace();
					}
					
				}

			}
			
		});
	}

	@Override
	public void loginResult(JSONObject result) {
		try {
			int statusCode = result.getInt("statusCode");
			
			if ( statusCode == HttpStatus.SC_OK ) {
				Log.d("kahlen", "login successfully");
				Bundle accountBundle = new Bundle();
				accountBundle.putString(UserModel.USER_NAME, mUser.name);
				accountBundle.putString(AccountManager.KEY_ACCOUNT_NAME, mUser.id);
				accountBundle.putString(AccountManager.KEY_ACCOUNT_TYPE, mUser.type);
				accountBundle.putString(AccountManager.KEY_AUTHTOKEN, mUser.password);
				
				String userAccountName = result.getString("name");
				accountBundle.putString(AccountMainActivity.ACCOUNT_USER_DATA_USER_NAME, userAccountName);
				mUser.name = userAccountName;
				
				Account account = new Account(mUser.id, mUser.type);
				AccountManager am = AccountManager.get(mContext);
				am.addAccountExplicitly(account, mUser.password, accountBundle);
				am.setAuthToken(account, mUser.type, mUser.password);
				am.setPassword(account, mUser.password);
				
				if ( isFromSettings ) {
					// return account info to MainActivity
					Intent intent = new Intent();
					intent.putExtras(accountBundle);
					setResult( RESULT_OK, intent );
				} else {
					// start next activity
					Intent intent = new Intent( mContext, DrawerActivity.class );
					startActivity( intent );
				}
				
				finish();
			} else if ( statusCode == HttpStatus.SC_NOT_FOUND ){
				Toast.makeText(mContext, "username or password is wrong", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(mContext, "server error: " + statusCode, Toast.LENGTH_LONG).show();
			}
			
		} catch (Exception e) {
			Toast.makeText(mContext, "there's something wrong", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}

	@Override
	public void onBackPressed() {
		// return to login/create page
		Intent intent = new Intent( mContext, AccountMainActivity.class );
		startActivity( intent );
		finish();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("kahlen", "--- LoginAccountActivity onDestroy");
	}
}
