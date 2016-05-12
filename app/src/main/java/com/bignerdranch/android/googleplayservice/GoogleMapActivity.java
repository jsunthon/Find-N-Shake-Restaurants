package com.bignerdranch.android.googleplayservice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.bignerdranch.android.randomrestaurants.R;
import com.bignerdranch.android.randomrestaurants.SettingsActivity;

import java.util.UUID;

/**
 * Created by jsunthon on 5/11/2016.
 */
public class GoogleMapActivity extends AppCompatActivity {

    public static final String EXTRA_LAT =
            "com.bignerdranch.android.randomrestaurants.lat";

    public static final String EXTRA_LON =
            "com.bignerdranch.android.randomrestaurants.lon";

    public static final String EXTRA_RESTAURANT_NAME =
            "com.bignerdranch.android.randomrestaurants.restaurantname";

    public static final String EXTRA_RESTAURANT_ADDRESS =
            "com.bignerdranch.android.randomrestaurants.restaurantaddress";

    public static final String EXTRA_RESTAURANT_PHONE =
            "com.bignerdranch.android.randomrestaurants.restaurantphone";

    public static final String EXTRA_RESTAURANT_RATING =
            "com.bignerdranch.android.randomrestaurants.restaurantrating";


    public static Intent newIntent(Context packageContext,
                                   double lat, double lon, String restaurantName,
                                   String restaurantAddress, String restaurantPhone,
                                   double restaurantRating) {
        Intent intent = new Intent(packageContext, GoogleMapActivity.class);
        intent.putExtra(EXTRA_LAT, lat);
        intent.putExtra(EXTRA_LON, lon);
        intent.putExtra(EXTRA_RESTAURANT_NAME, restaurantName);
        intent.putExtra(EXTRA_RESTAURANT_ADDRESS, restaurantAddress);
        intent.putExtra(EXTRA_RESTAURANT_PHONE, restaurantPhone);
        intent.putExtra(EXTRA_RESTAURANT_RATING, restaurantRating);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            fragment = new GoogleMapFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
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
}
