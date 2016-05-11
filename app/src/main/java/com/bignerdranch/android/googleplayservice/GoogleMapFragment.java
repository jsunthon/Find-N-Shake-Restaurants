package com.bignerdranch.android.googleplayservice;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.android.randomrestaurants.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by jsunthon on 5/11/2016.
 */
public class GoogleMapFragment extends SupportMapFragment {
    private final String LOG_TAG = this.getClass().getSimpleName();
    private double latitude;
    private double longitude;

    private GoogleApiClient mClient;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View v = inflater.inflate(R.layout.activity_google_map, container, false);
//        return v;
//    }

//    vprivate class Search
}
