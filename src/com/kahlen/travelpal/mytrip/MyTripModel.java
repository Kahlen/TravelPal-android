package com.kahlen.travelpal.mytrip;

import java.util.ArrayList;

public class MyTripModel {
	
	public String id;
	public String destination;
	public String startDate;
	public String endDate;
	public ArrayList<String> partners;
	
	public MyTripModel(String i, String d, String s, String e, ArrayList<String> p) {
		id = i;
		destination = d;
		startDate = s;
		endDate = e;
		partners = p;
	}
	
	public boolean hasPartners() {
		return (partners != null) && (partners.size() != 0);
	}
}
