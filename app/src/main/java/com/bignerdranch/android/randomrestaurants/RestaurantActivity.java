package com.bignerdranch.android.randomrestaurants;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.bignerdranch.android.models.Restaurant;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.UUID;

public class RestaurantActivity extends AppCompatActivity
                                implements GoogleApiClient.OnConnectionFailedListener,
                                GoogleApiClient.ConnectionCallbacks,
                                ActivityCompat.OnRequestPermissionsResultCallback {
    private List<Restaurant> mRestaurants;
    private final String LOG_TAG = RestaurantActivity.class.getSimpleName();
    public static final String EXTRA_RESTAURANT_ID =
            "com.bignerdranch.android.randomrestaurants.resId";
    private GoogleApiClient mGoogleApiClient;

    public static Intent newIntent(Context packageContext, UUID restaurantId) {
        Intent intent = new Intent(packageContext, RestaurantActivity.class);
        intent.putExtra(EXTRA_RESTAURANT_ID, restaurantId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new RestaurantFragment())
                    .commit();
        }

        buildGoogleApiClient();

        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
        else
            Log.v(LOG_TAG, "Not Connected");

        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Location mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            if (mLocation != null) {
                SharedPreferences sharedPrefs = getSharedPreferences("location_prefs", MODE_PRIVATE);
                SharedPreferences.Editor sharedEdit = sharedPrefs.edit();
                sharedEdit.putString("mLatitude", String.valueOf(mLocation.getLatitude()));
                sharedEdit.putString("mLongitude", String.valueOf(mLocation.getLongitude()));
                sharedEdit.commit();
                Log.v(LOG_TAG, "The Current Latitude is :" + mLocation.getLatitude()
                        + "and Current Longitude is :" + mLocation.getLongitude());
            } else {
                Toast.makeText(this, "Access Location permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case 200: {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    onConnected(null);
                }
            }
        }
    }

    @Override
    public void onConnectionFailed (@NonNull ConnectionResult connectionResult){
        Log.v(LOG_TAG,"Connection Failed");
    }

    @Override
    public void onConnectionSuspended ( int i){
            Log.v(LOG_TAG,"Connection Suspended");
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
}
