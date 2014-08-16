package com.kahlen.travelpal.mytrip;

public interface MyTripListener {
	void go2TripContent(MyTripModel model);
	void go2TripContent(MyTripModel model, String sharedLink);
}
