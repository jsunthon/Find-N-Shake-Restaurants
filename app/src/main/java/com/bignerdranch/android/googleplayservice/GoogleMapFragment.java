package com.bignerdranch.android.googleplayservice;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bignerdranch.android.randomrestaurants.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by jsunthon on 5/11/2016.
 */
public class GoogleMapFragment extends SupportMapFragment {
    private final String LOG_TAG = this.getClass().getSimpleName();
    private double latitude;
    private double longitude;
    private String restaurantName;
    private String restaurantAddress;
    private String restaurantPhone;
    private double restaurantRating;
    private GoogleMap mMap;
    private GoogleApiClient mClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        latitude = getActivity().getIntent()
                .getDoubleExtra(GoogleMapActivity.EXTRA_LAT, 0.00);
        longitude = getActivity().getIntent()
                .getDoubleExtra(GoogleMapActivity.EXTRA_LON, 0.00);
        restaurantName = getActivity().getIntent()
                .getStringExtra(GoogleMapActivity.EXTRA_RESTAURANT_NAME);
        restaurantAddress = getActivity().getIntent()
                .getStringExtra(GoogleMapActivity.EXTRA_RESTAURANT_ADDRESS);
        restaurantPhone = getActivity().getIntent()
                .getStringExtra(GoogleMapActivity.EXTRA_RESTAURANT_PHONE);
        restaurantRating = getActivity().getIntent()
                .getDoubleExtra(GoogleMapActivity.EXTRA_RESTAURANT_RATING, 0.00);

        Log.v(LOG_TAG, "Got latitude: " + latitude);
        Log.v(LOG_TAG, "Got longitude: " + longitude);
        Log.v(LOG_TAG, "Got name: " + restaurantName);
        Log.v(LOG_TAG, "Got addr: " + restaurantAddress);
        Log.v(LOG_TAG, "Got phone: " + restaurantPhone);
        Log.v(LOG_TAG, "Got rating: " + restaurantRating);
        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        Log.v(LOG_TAG, "Map has loaded. Calling updateUI...");
                        Toast.makeText(getContext(), "Going to " + restaurantName + "....", Toast.LENGTH_SHORT).show();
                        updateUI();
                    }
                });
            }
        });
    }

    private void updateUI() {
        if (mMap == null) {
            return;
        }
        LatLng itemPoint = new LatLng(latitude, longitude);
        Log.v(LOG_TAG, "lat, long in updateUI: " + itemPoint.latitude + " longitude: " + itemPoint.longitude);
//        LatLng myPoint = new LatLng(
//                mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        MarkerOptions restaurantMarker = new MarkerOptions()
                .position(itemPoint);
        restaurantMarker.title(restaurantName);
        String description = generateRestaurantDesc();
        restaurantMarker.snippet(description);
        mMap.clear();
        mMap.addMarker(restaurantMarker);
//        LatLngBounds bounds = new LatLngBounds.Builder()
//                .include(itemPoint)
//                .build();
//        int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(itemPoint,15);
        mMap.animateCamera(update);
    }

    private String generateRestaurantDesc() {
        return restaurantAddress + "\n" + restaurantPhone + "\n" + "Rating: " + restaurantRating;
    }

}
