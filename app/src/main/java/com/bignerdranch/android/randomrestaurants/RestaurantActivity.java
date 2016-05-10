package com.bignerdranch.android.randomrestaurants;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.bignerdranch.android.models.Restaurant;

import java.util.List;
import java.util.UUID;

public class RestaurantActivity extends AppCompatActivity {

    private List<Restaurant> mRestaurants;

    public static final String EXTRA_RESTAURANT_ID =
            "com.bignerdranch.android.randomrestaurants.resId";

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
}
