package com.kahlen.travelpal.newtrip;

import android.os.Bundle;

public interface NewTripListener {
	void destinationDateDone( Bundle args );
	void backToNewTrip( Bundle args );
	void finishNewTrip( Bundle args );
}
