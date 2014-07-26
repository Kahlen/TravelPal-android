package com.kahlen.travelpal.utilities;

import android.accounts.AccountManager;
import android.content.Context;

import com.kahlen.travelpal.account.AccountMainActivity;

public class AccountUtils {

	public static String getUserid( Context context ) {
		return AccountManager.get(context).getAccountsByType(AccountMainActivity.TRAVELPAL_ACCOUNT_TYPE)[0].name;
	}
	
	public static boolean isLoggedin( Context context ) {
		return AccountManager.get(context).getAccountsByType(AccountMainActivity.TRAVELPAL_ACCOUNT_TYPE).length > 0;
	}
}
