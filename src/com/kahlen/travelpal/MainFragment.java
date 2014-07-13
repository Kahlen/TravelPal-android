package com.kahlen.travelpal;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainFragment extends Fragment {
	public static final String ARG_PLANET_NUMBER = "planet_number";

    public MainFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_main, container, false);
        // argument <-> bundle
        int i = getArguments().getInt(ARG_PLANET_NUMBER);
        String title = getResources().getStringArray(R.array.activity_titles)[i];

        TextView txtView = (TextView) rootView.findViewById(R.id.main_text);
        txtView.setText( "main page string" );
        getActivity().setTitle(title);
        return rootView;
    }
}
