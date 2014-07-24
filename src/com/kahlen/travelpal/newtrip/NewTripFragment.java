package com.kahlen.travelpal.newtrip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.kahlen.travelpal.MainFragment;
import com.kahlen.travelpal.R;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import android.app.DatePickerDialog;

public class NewTripFragment extends Fragment {
	
	final public static String NEW_TRIP_DESTINATION = "destination";
	final public static String NEW_TRIP_START_DATE = "start_date";
	final public static String NEW_TRIP_END_DATE = "end_date";
	final public static String NEW_TRIP_FRIENDS_SELECTED = "friends_selected";
	
	private Context mContext;
	private NewTripListener activityCallback;
	static private View mRootView;
	static private Button startDateBtn;
	static private Button endDateBtn;
	static private DatePickerDialog dpdS;
	static private DatePickerDialog dpdE;
	static private SimpleDateFormat formatter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mContext = getActivity();
		mRootView = inflater.inflate(R.layout.activity_newtrip, container, false);
		
		formatter = new SimpleDateFormat("dd/MM/yyy");
	     // argument <-> bundle
		Bundle data = getArguments();
	    int i = data.getInt(MainFragment.DRAWER_SELECTED_POSITION);
	    
	    String prevDestination = data.getString( NEW_TRIP_DESTINATION, "" );
	    if ( !prevDestination.isEmpty() ) {
	    	EditText des = (EditText) mRootView.findViewById(R.id.newtrip_destination_input);
	    	des.setText(prevDestination);
	    }
	    String prevStartDate = data.getString( NEW_TRIP_START_DATE, "" );
	    if ( prevStartDate.isEmpty() ) {
	    	// use current time
	    	setStartPickDateBtn( Calendar.getInstance() );
	    } else {
	    	// use previously chosen date
			try {
				Date startD = (Date) formatter.parse( prevStartDate );
				Calendar c = Calendar.getInstance();
		    	c.setTime(startD);
		    	setStartPickDateBtn( c );
			} catch (ParseException e) {
				e.printStackTrace();
				setStartPickDateBtn( Calendar.getInstance() );
			}	    	
	    }
	    
	    String prevEndDate = data.getString( NEW_TRIP_END_DATE, "" );
	    if ( prevEndDate.isEmpty() ) {
	    	// use current time
	    	setEndPickDateBtn( Calendar.getInstance() );
	    } else {
	    	// use previously chosen date
			try {
				Date startE = (Date) formatter.parse( prevEndDate );
				Calendar c = Calendar.getInstance();
		    	c.setTime(startE);
		    	setEndPickDateBtn( c );
			} catch (ParseException e) {
				e.printStackTrace();
				setEndPickDateBtn( Calendar.getInstance() );
			}
	    	
	    }
	    
	    String title = getResources().getStringArray(R.array.activity_titles)[i];
	    getActivity().setTitle(title);

	    setFooter();
	     
	    return mRootView;		
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			activityCallback = (NewTripListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnHeadlineSelectedListener");
        }
	}


	private void setFooter() {
		Button nextBtn = (Button) mRootView.findViewById( R.id.newtrip_footer_next );
		nextBtn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Bundle data = getArguments();
				String destination = getDestination();
				if ( destination == null || destination.matches("\\s+") ) {
					// destination is empty
					Toast.makeText(mContext, "Please input destination", Toast.LENGTH_LONG).show();
				} else {
					data.putString(NEW_TRIP_DESTINATION, destination);
					data.putString(NEW_TRIP_START_DATE, getStartDate());
					data.putString(NEW_TRIP_END_DATE, getEndDate());
					activityCallback.destinationDateDone(data);
				}
			}
			
		});
	}
	
	private String getDestination() {
		EditText des = (EditText) mRootView.findViewById(R.id.newtrip_destination_input);
		return des.getText().toString();
	}
	
	private String getStartDate() {
		DatePicker startPicker = dpdS.getDatePicker();
		return getFormattedDate(startPicker.getYear(), startPicker.getMonth(), startPicker.getDayOfMonth());
	}
	
	private String getEndDate() {
		DatePicker endPicker = dpdE.getDatePicker();
		return getFormattedDate(endPicker.getYear(), endPicker.getMonth(), endPicker.getDayOfMonth());
	}
		
	private void setStartPickDateBtn( Calendar c ) {
		startDateBtn = (Button) mRootView.findViewById( R.id.newtrip_start_date_btn );
		dpdS = new DatePickerDialog(mContext, new DatePickerFragmentStart(), c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		dpdS.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
		startDateBtn.setText( getFormattedDate(c) );
		
		startDateBtn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				dpdS.show();
			}
		});
	}
	
	private void setEndPickDateBtn( Calendar c ) {
		endDateBtn = (Button) mRootView.findViewById( R.id.newtrip_end_date_btn );
		dpdE = new DatePickerDialog(mContext, new DatePickerFragmentEnd(), c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		dpdE.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
		endDateBtn.setText( getFormattedDate(c) );
		
		endDateBtn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				dpdE.show();
			}
		});
	}
	
	static private String getFormattedDate( DatePicker p ) {
		return p.getDayOfMonth() + "/" + (p.getMonth()+1) + "/" + p.getYear();
	}
	
	static private String getFormattedDate( Calendar c ) {
		return c.get(Calendar.DAY_OF_MONTH) + "/" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.YEAR);
	}
	
	static private String getFormattedDate( int year, int month, int day ) {
		return day + "/" + (month+1) + "/" + year;
	}
	
	public static class DatePickerFragmentStart extends DialogFragment implements DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);
			
			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}
		
		public void onDateSet(DatePicker view, int year, int month, int day) {
			// show date when date is selected
			startDateBtn.setText( getFormattedDate(year, month, day) );
			// change end date
			
			try {
				DatePicker endDatePicker = dpdE.getDatePicker();
				Date startDate = (Date) formatter.parse( getFormattedDate(year, month, day) );
				Date endDate = (Date) formatter.parse( getFormattedDate(endDatePicker) );
				dpdE.getDatePicker().setMinDate(startDate.getTime());
				if ( endDate.getTime() < startDate.getTime() ) {
					dpdE.updateDate(year, month, day);
					endDateBtn.setText( getFormattedDate(year, month, day) );
				}
			} catch (ParseException e) {
				e.printStackTrace();
			} 
		}
	}
	
	public static class DatePickerFragmentEnd extends DialogFragment implements DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);
			
			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}
		
		public void onDateSet(DatePicker view, int year, int month, int day) {
			// show date when date is selected
			endDateBtn.setText( getFormattedDate(year, month, day) );
			
			try {
				DatePicker startDatePicker = dpdS.getDatePicker();
				Date endDate = (Date) formatter.parse( getFormattedDate(year, month, day) );
				Date startDate = (Date) formatter.parse( getFormattedDate(startDatePicker) );
				dpdS.getDatePicker().setMinDate(endDate.getTime());
				if ( endDate.getTime() < startDate.getTime() ) {
					dpdS.updateDate(year, month, day);
					startDateBtn.setText( getFormattedDate(year, month, day) );
				}
			} catch (ParseException e) {
				e.printStackTrace();
			} 
		}
	}

	
	
}
