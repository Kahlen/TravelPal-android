package com.kahlen.travelpal.utilities;

import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kahlen.travelpal.account.AccountMainActivity;
import com.kahlen.travelpal.mqtt.MQTTService;

public class AccountUtils {

	public static String getUserid( Context context ) {
		return AccountManager.get(context).getAccountsByType(AccountMainActivity.TRAVELPAL_ACCOUNT_TYPE)[0].name;
	}
	
	public static boolean isLoggedin( Context context ) {
		return AccountManager.get(context).getAccountsByType(AccountMainActivity.TRAVELPAL_ACCOUNT_TYPE).length > 0;
	}
	
	public static class AccountStateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent i) {
			if ( AccountManager.LOGIN_ACCOUNTS_CHANGED_ACTION.equals( i.getAction() ) )  {
				boolean accountExists = isLoggedin(context);
				if ( !MQTTService.isServiceRunning && accountExists ) {
					// start service
					Intent intent = new Intent(context, MQTTService.class);
					intent.setAction( MQTTService.ACTION_CONNECT_N_SUBSCRIBE );
					// subscribe to everything sent to this user
					// topic = me/#
					intent.putExtra( MQTTService.INTENT_EXTRA_SUBSCRIBE_TOPIC, AccountUtils.getUserid(context) + "/#" );
			        context.startService(intent);
				} else if ( MQTTService.isServiceRunning && !accountExists ) {
					// disconnect MQTT and stop service
					Intent intent = new Intent(context, MQTTService.class);
					intent.setAction( MQTTService.ACTION_DISCONNECT );
			        context.startService(intent);
				}
			}
			
		}
		
	}
}
