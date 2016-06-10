package com.bignerdranch.android.randomrestaurants;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Created by jsunthon on 6/9/2016.
 */
public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        ActivityCompat.OnRequestPermissionsResultCallback {
    ViewPager mViewPager;
    TabLayout mTabLayout;
    private GoogleApiClient mGoogleApiClient;
    private final String LOG_TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.v(LOG_TAG, " onCreate called");
        buildGoogleApiClient();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
        else
            Log.v(LOG_TAG, "Not Connected");
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            Location mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLocation != null) {
                SharedPreferences sharedPrefs = getSharedPreferences("location_prefs", MODE_PRIVATE);
                SharedPreferences.Editor sharedEdit = sharedPrefs.edit();
                sharedEdit.putString("mLatitude", String.valueOf(mLocation.getLatitude()));
                sharedEdit.putString("mLongitude", String.valueOf(mLocation.getLongitude()));
                sharedEdit.commit();
                Log.v(LOG_TAG, "The Current Latitude is :" + mLocation.getLatitude()
                        + "and Current Longitude is :" + mLocation.getLongitude());
                setUpGUI();
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 200: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onConnected(null);
                } else {
                    setUpGUI();
                    Log.v(LOG_TAG,"Access Location Permission Denied");
                }
            }
        }
    }

    private void setUpGUI() {
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);

        //http://www.androidwarriors.com/2015/10/tablayout-with-viewpager-android.html
        FragmentManager manager = getSupportFragmentManager();
        MainPagerAdapter mainPagerAdapter = new MainPagerAdapter(manager, getApplicationContext());

        //hook up the pager adapater with the pager itself.
        mViewPager.setAdapter(mainPagerAdapter);

        // hook up the tablayout with the pager
        mTabLayout.setupWithViewPager(mViewPager);

        // adding functionality to tab and viewpager to manage each other when a page is changed or when a tab is selected
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        mTabLayout.setTabsFromPagerAdapter(mainPagerAdapter);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v(LOG_TAG, "Connection Failed");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v(LOG_TAG, "Connection Suspended");
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
}
