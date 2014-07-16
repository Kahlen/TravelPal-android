package com.kahlen.travelpal;

import com.kahlen.travelpal.chat.ChatFragment;
import com.kahlen.travelpal.chat.FindFriendFragment;
import com.kahlen.travelpal.mqtt.MQTTActivityCallBack;
import com.kahlen.travelpal.mqtt.MQTTCallBack;
import com.kahlen.travelpal.mqtt.MQTTClientController;
import com.kahlen.travelpal.mqtt.MQTTConfiguration;
import com.kahlen.travelpal.mqtt.MQTTErrorCallBack;
import com.kahlen.travelpal.mqtt.MQTTService;
import com.kahlen.travelpal.user.UserInfo;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class DrawerActivity extends Activity implements FindFriendFragment.FindFriendListener, MQTTActivityCallBack, MQTTErrorCallBack {
	
	private Context mContext;
	
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mPlanetTitles;
    
    MQTTClientController mController = MQTTClientController.getInstance( mContext );

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mTitle = mDrawerTitle = getTitle();
		mContext = getApplicationContext();
		setContentView(R.layout.activity_root);

		mPlanetTitles = getResources().getStringArray(R.array.activity_titles);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mPlanetTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		
     // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        Intent intent = getIntent();
		boolean fromNewMessage = intent.getBooleanExtra("new_message", false);
		if ( fromNewMessage ) {
			// get new message when in
			String topic = intent.getStringExtra("topic");
			String friendId = topic.split("/")[1];
			friendIdSelected(friendId);
		} else if (savedInstanceState == null) {
            selectItem(0);
        }
        
	}

	@Override
	protected void onResume() {
		super.onResume();
		MyApplication.activityResumed();
		
		// connect to MQTT
		connect2Mqtt();
	}

	@Override
	protected void onPause() {
		super.onPause();
		MyApplication.activityPaused();
	}


    @Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	// drawer, slide in menu
    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
    	Log.d("kahlen", "select item: " + position);
    	if ( position == 3 ) {
    		// update the main content by replacing fragments
            Fragment fragment = new FindFriendFragment();
            Bundle args = new Bundle();
            args.putInt(MainFragment.ARG_PLANET_NUMBER, position);
            fragment.setArguments(args);

            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            setTitle(mPlanetTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerList);
            return;
    	}
    	
        // update the main content by replacing fragments
        Fragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putInt(MainFragment.ARG_PLANET_NUMBER, position);
        fragment.setArguments(args);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mPlanetTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
        getActionBar().setTitle(mTitle);
	}
	
	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    // --- callback from FindFriendFragment ---
	@Override
	public void friendIdSelected(String id) {
		// click event from FindFriendFragment
		// open chat fragment
        Fragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ChatFragment.ARG_CHAT_FRIEND_ID, id);
        fragment.setArguments(args);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
	}
	
	// --- MQTT ---
	private void connect2Mqtt() {
		if ( !mController.isConnected() ) {
			mController.registerCallback( new MQTTCallBack(mContext, this) );
			// start service
			Intent intent = new Intent(mContext, MQTTService.class);
			intent.setAction( MQTTService.ACTION_CONNECT_N_SUBSCRIBE );
			// subscribe to everything sent to this user
			// topic = me/#
			intent.putExtra( MQTTService.INTENT_EXTRA_SUBSCRIBE_TOPIC, UserInfo.getUserId() + "/#" );
	        mContext.startService(intent);	
		}
	}
	
	@Override
	public void messageReceived(String topic, String message) {
		Log.d("kahlen", "--- messageReceived in DrawerActivity ---");
		ChatFragment chatFrg = (ChatFragment) getFragmentManager().findFragmentById(R.id.content_frame);
		chatFrg.messageReceived(topic, message);
	}
	
	@Override
	public void mqttFail(String errorMsg) {
		Toast.makeText(mContext, errorMsg, Toast.LENGTH_LONG).show();
	}
	
	// --- menu ---
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		
		switch( item.getItemId() ) {
			case R.id.menu_connect:
				if ( mController.isConnected() ) {
					// disconnect
					
					// stop service
					Intent intent = new Intent(mContext, MQTTService.class);
					intent.setAction( MQTTService.ACTION_DISCONNECT );
			        mContext.startService(intent);
				} else {
					// connect
					
//					registerListener();
					// start service
					Intent intent = new Intent(mContext, MQTTService.class);
					intent.setAction( MQTTService.ACTION_CONNECT );
			        mContext.startService(intent);	
			        
				}
				
				break;
			case R.id.menu_subscribe:
				// show popup input dialog
				if ( mController.isConnected() ) {
					// use Activity.this for AlertDialog to avoid exception
					// alertdialog Unable to add window -- token null is not for an application
					AlertDialog.Builder alert = new AlertDialog.Builder( DrawerActivity.this );
					alert.setTitle( R.string.dialog_title_subscribe );
					alert.setMessage( R.string.dialog_message_subscribe );
					final EditText input = new EditText( mContext );
					alert.setView(input);
					alert.setPositiveButton( R.string.ok , new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							String topic = input.getText().toString();
							// subscribe
							Intent intent = new Intent(mContext, MQTTService.class);
							intent.setAction( MQTTService.ACTION_SUBSCRIBE );
							intent.putExtra( MQTTService.INTENT_EXTRA_SUBSCRIBE_TOPIC, topic );
					        mContext.startService(intent);	
						}
					});
					alert.show();
				} else {
					Toast.makeText(mContext, R.string.not_connected, Toast.LENGTH_LONG).show();
				}
				
				break;
			case R.id.menu_check_connection:
				// check connection, show toast message
				if ( mController.isConnected() )
					Toast.makeText(mContext, R.string.connected, Toast.LENGTH_LONG).show();
				else
					Toast.makeText(mContext, R.string.not_connected, Toast.LENGTH_LONG).show();
				break;
			case R.id.menu_server:
				AlertDialog.Builder alert2 = new AlertDialog.Builder( DrawerActivity.this );
				alert2.setTitle( R.string.server );
				alert2.setMessage( R.string.dialog_message_server );
				final EditText input2 = new EditText( mContext );
				alert2.setView(input2);
				alert2.setPositiveButton( R.string.ok , new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String server = input2.getText().toString();
						// subscribe
						MQTTConfiguration.BROKER_URL = server;
					}
				});
				alert2.show();
				break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

}
