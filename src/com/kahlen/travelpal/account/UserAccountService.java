package com.kahlen.travelpal.account;


import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class UserAccountService extends Service {

	UserAccountAuthenticator mAuthenticator;

	@Override
	public void onCreate() {
		Log.d("kahlen", "UserAccountService onCreate");
		super.onCreate();
		mAuthenticator = new UserAccountAuthenticator(this);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mAuthenticator.getIBinder();
	}

	 public class UserAccountAuthenticator extends AbstractAccountAuthenticator {

		 Context mContext;
		 
		public UserAccountAuthenticator(Context context) {
			super(context);
			mContext = context;
		}

		@Override
		public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
			Intent intent = new Intent(mContext, AccountMainActivity.class);
	        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
	        intent.putExtra(AccountManager.KEY_AUTHENTICATOR_TYPES, authTokenType);
	        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

	        final Bundle bundle = new Bundle();
	        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
	        return bundle;
		}

		@Override
		public Bundle confirmCredentials(AccountAuthenticatorResponse arg0,
				Account arg1, Bundle arg2) throws NetworkErrorException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Bundle editProperties(AccountAuthenticatorResponse arg0,
				String arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
	        // Extract the username and password from the Account Manager, and ask
	        // the server for an appropriate AuthToken.
	        AccountManager am = AccountManager.get(mContext);
	        String password = am.getPassword(account);

	        // Lets give another try to authenticate the user
	        if ( password == null || password.isEmpty() ) {
	        	Intent intent = new Intent(mContext, AccountMainActivity.class);
	            intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, account.name);
	            intent.putExtra(AccountManager.KEY_AUTHENTICATOR_TYPES, authTokenType);
	            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
	            final Bundle bundle = new Bundle();
	            bundle.putParcelable(AccountManager.KEY_INTENT, intent);
	            return bundle;
	        }

	        Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, password);
            return result;
		}

		@Override
		public String getAuthTokenLabel(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Bundle hasFeatures(AccountAuthenticatorResponse arg0,
				Account arg1, String[] arg2) throws NetworkErrorException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Bundle updateCredentials(AccountAuthenticatorResponse arg0,
				Account arg1, String arg2, Bundle arg3)
				throws NetworkErrorException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Bundle getAccountRemovalAllowed( AccountAuthenticatorResponse response, Account account) throws NetworkErrorException {
			Bundle result = new Bundle();
		    result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true);
		    return result;
		}
		 
	 }
}
