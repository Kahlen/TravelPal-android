package com.kahlen.travelpal.utilities;

import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kahlen.travelpal.account.AccountMainActivity;
import com.kahlen.travelpal.account.UserModel;
import com.kahlen.travelpal.mqtt.MQTTService;

public class AccountUtils {

	public static String getUserid( Context context ) {
		return AccountManager.get(context).getAccountsByType(AccountMainActivity.TRAVELPAL_ACCOUNT_TYPE)[0].name;
	}
	
	public static String getUserName( Context context ) {
		Account account = AccountManager.get(context).getAccountsByType(AccountMainActivity.TRAVELPAL_ACCOUNT_TYPE)[0];
		return AccountManager.get(context).getUserData(account, AccountMainActivity.ACCOUNT_USER_DATA_USER_NAME);
	}
	
	public static UserModel getUserModel( Context context ) {
		Account account = AccountManager.get(context).getAccountsByType(AccountMainActivity.TRAVELPAL_ACCOUNT_TYPE)[0];
		String id = account.name;
		String password = AccountManager.get(context).getPassword(account);
		String name = AccountManager.get(context).getUserData(account, AccountMainActivity.ACCOUNT_USER_DATA_USER_NAME);
		return new UserModel( id, password, name );
	}
	
	public static JSONObject getUserJson( Context context ) {
		UserModel model = getUserModel( context );
		JSONObject result = new JSONObject();
		try {
			result.put("_id", model.id);
			result.put("password", model.password);
			result.put("name", model.name);
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		return result;
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
