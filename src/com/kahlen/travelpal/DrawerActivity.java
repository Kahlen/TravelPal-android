package com.kahlen.travelpal;

import com.kahlen.travelpal.chat.ChatFragment;
import com.kahlen.travelpal.chat.FindFriendFragment;
import com.kahlen.travelpal.mqtt.MQTTActivityCallBack;
import com.kahlen.travelpal.mqtt.MQTTConfiguration;
import com.kahlen.travelpal.mqtt.MQTTErrorCallBack;
import com.kahlen.travelpal.mqtt.MQTTService;
import com.kahlen.travelpal.mqtt.MQTTServiceDelegate;
import com.kahlen.travelpal.newtrip.NewTripFriendsFragment;
import com.kahlen.travelpal.newtrip.NewTripFragment;
import com.kahlen.travelpal.newtrip.NewTripListener;
import com.kahlen.travelpal.utilities.AccountUtils;

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

public class DrawerActivity extends Activity implements FindFriendFragment.FindFriendListener, NewTripListener, MQTTActivityCallBack, MQTTErrorCallBack {
	
	public static enum DrawerType { home, mytrip, newtrip, friends, me };
	public static enum MQTTNotificationType { newChat, addItinerary, updateItinerary, unknown };
	final public static String INTENT_EXTRA_MQTT_NOTIFICATION_TYPE = "mqtt_notification_type";
	final public static String INTENT_EXTRA_TOPIC = "topic";
	final public static String INTENT_EXTRA_MESSAGE = "message";
	
	private Context mContext;
	
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mPlanetTitles;
    
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
		int mqttNotificationType = intent.getIntExtra(INTENT_EXTRA_MQTT_NOTIFICATION_TYPE, MQTTNotificationType.unknown.ordinal());
		Log.d("kahlen", "from notification type: " + MQTTNotificationType.values()[mqttNotificationType] );
		switch ( MQTTNotificationType.values()[mqttNotificationType] ) {
			case newChat:
				// get new message when in
				String topic = intent.getStringExtra("topic");
				String friendId = topic.split("/")[1];
				friendIdSelected(friendId);
				break;
			case addItinerary:
			case updateItinerary:
			case unknown:
				selectItem(DrawerType.home);
				break;
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
		MQTTServiceDelegate.unregisterActivityCallback(this);
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
            selectItem(DrawerType.values()[position]);
        }
    }

    private void selectItem(DrawerType position) {
    	Log.d("kahlen", "select item: " + position);
    	
    	FragmentManager fragmentManager = getFragmentManager();
    	Bundle args = new Bundle();
        args.putInt(MainFragment.DRAWER_SELECTED_POSITION, position.ordinal());
        
    	int positionInt = position.ordinal();
    	switch ( position ) {
    		case home:
    		case mytrip:
    		case me:
    			// update the main content by replacing fragments
    			Fragment mainFragment = new MainFragment();
    			mainFragment.setArguments(args);
    	        fragmentManager.beginTransaction().replace(R.id.content_frame, mainFragment ).commit();

    	        // update selected item and title, then close the drawer
    	        mDrawerList.setItemChecked(positionInt, true);
    	        setTitle(mPlanetTitles[positionInt]);
    	        mDrawerLayout.closeDrawer(mDrawerList);
    	        break;
    		case newtrip:
    			Fragment newtrip = new NewTripFragment();
    			newtrip.setArguments(args);
    			fragmentManager.beginTransaction().replace(R.id.content_frame, newtrip).commit();
    			// update selected item and title, then close the drawer
                mDrawerList.setItemChecked(positionInt, true);
                setTitle(mPlanetTitles[positionInt]);
                mDrawerLayout.closeDrawer(mDrawerList);
    			break;
    		case friends:
    			// update the main content by replacing fragments     
    			FindFriendFragment findFriendFragment = new FindFriendFragment();
    			findFriendFragment.setArguments(args);
                fragmentManager.beginTransaction().replace(R.id.content_frame, findFriendFragment).commit();
                // update selected item and title, then close the drawer
                mDrawerList.setItemChecked(positionInt, true);
                setTitle(mPlanetTitles[positionInt]);
                mDrawerLayout.closeDrawer(mDrawerList);
    			break;
    	}
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
        // add FindFriendFragment to stack, so when back key is pressed on ChatFragment, it will go back to FindFriendFragment
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack("FindFriendFragment").commit();
	}
	
	// --- callback from NewTripFragment/NewTripFriendsFragment ---
	@Override
	public void destinationDateDone(Bundle args) {
		Fragment fragment = new NewTripFriendsFragment();
		args.putInt(MainFragment.DRAWER_SELECTED_POSITION, DrawerType.newtrip.ordinal());
		fragment.setArguments(args);
		
		FragmentManager fragmentManager = getFragmentManager();
        // add NewTripFragment to stack, so when back key is pressed on NewTripFriendsFragment, it will go back to NewTripFragment
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack("NewTripFragment").commit();
	}
	
	@Override
	public void backToNewTrip( Bundle args ) {
		Fragment fragment = new NewTripFragment();
		args.putInt(MainFragment.DRAWER_SELECTED_POSITION, DrawerType.newtrip.ordinal());
		fragment.setArguments(args);
		
		FragmentManager fragmentManager = getFragmentManager();
		// pop NewTripFragment, so when clicking back key on NewTripFriendsFragment, it doesn't go back to NewTripFragment
		fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        // don't add NewTripFriendsFragment to stack
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();	
	}

	@Override
	public void finishNewTrip( Bundle args ) {
		// pop NewTripFragment, so when clicking back key on main page, it doesn't go back to NewTripFragment
		getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		// go back to home page
		selectItem(DrawerType.home);
	}
	
	// --- MQTT ---
	private void connect2Mqtt() {
		MQTTServiceDelegate.registerActivityCallback( this );
		// start service
		Intent intent = new Intent(mContext, MQTTService.class);
		intent.setAction( MQTTService.ACTION_CONNECT_N_SUBSCRIBE );
		// subscribe to everything sent to this user
		// topic = me/#
		intent.putExtra( MQTTService.INTENT_EXTRA_SUBSCRIBE_TOPIC, AccountUtils.getUserid(mContext) + "/#" );
        mContext.startService(intent);
	}
	
	@Override
	public void messageReceived( MQTTNotificationType notificationType, String topic, String message) {
		Log.d("kahlen", "--- messageReceived in DrawerActivity ---");
		// TODO: what to do in DrawerActivity?
//		ChatFragment chatFrg = (ChatFragment) getFragmentManager().findFragmentById(R.id.content_frame);
//		chatFrg.messageReceived(topic, message);
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
				// start service
				Intent intent = new Intent(mContext, MQTTService.class);
				intent.setAction( MQTTService.ACTION_CONNECT );
		        mContext.startService(intent);	
				
				break;
			case R.id.menu_subscribe:
				// show popup input dialog
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
				
				break;
			case R.id.menu_check_connection:
				// check connection, show toast message
//				if ( mController.isConnected() )
//					Toast.makeText(mContext, R.string.connected, Toast.LENGTH_LONG).show();
//				else
//					Toast.makeText(mContext, R.string.not_connected, Toast.LENGTH_LONG).show();
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
