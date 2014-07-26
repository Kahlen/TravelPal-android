package com.kahlen.travelpal;

import com.kahlen.travelpal.account.AccountMainActivity;
import com.kahlen.travelpal.utilities.AccountUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if ( AccountUtils.isLoggedin(this) ) {
			Intent intent = new Intent( this, DrawerActivity.class );
			startActivity( intent );
			finish();
		} else {
			Intent intent = new Intent( this, AccountMainActivity.class );
			startActivity( intent );
			finish();
		}
	}

	
}
