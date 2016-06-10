package com.bignerdranch.android.randomrestaurants;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.bignerdranch.android.models.Restaurant;
import com.bignerdranch.android.models.RestaurantLab;

import java.util.List;
import java.util.UUID;

// Thanks to Big Nerd Ranch Guide
public class RandomRestaurantPagerActivity extends AppCompatActivity {

    private final String LOG_TAG = RandomRestaurantPagerActivity.class.getSimpleName();
    public static final String EXTRA_RESTAURANT_ID = "com.bignerdranch.android.randomrestaurants.resId";
    private ViewPager mViewPager;
    private List<Restaurant> mRestaurants;

    public static Intent newIntent(Context packageContext, UUID restraurantId) {
        Intent intent = new Intent(packageContext, RandomRestaurantPagerActivity.class);
        intent.putExtra(EXTRA_RESTAURANT_ID, restraurantId);
        return intent;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_pager);

        UUID restaurantId = (UUID) getIntent().getSerializableExtra(EXTRA_RESTAURANT_ID);
        mRestaurants = RestaurantLab.get(this).getRestaurants();

        FragmentManager fragmentManager = getSupportFragmentManager();

        mViewPager = (ViewPager) findViewById(R.id.activity_restaurant_pager_view_pager);
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                Restaurant restaurant = mRestaurants.get(position);
                return RestaurantFragment.newInstance(restaurant.getId(), "random");
            }

            @Override
            public int getCount() {
                return mRestaurants.size();
            }
        });

        for (int i = 0; i < mRestaurants.size(); i++) {
            Restaurant restaurant = mRestaurants.get(i);
            if (restaurant.getId().equals(restaurantId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }

        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //maybe do this in the future.
//        getMenuInflater().inflate(R.menu.favorites, menu);
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
//        if (id == R.id.favorites) {
//            //navigate back to MainActivit the parent.
//            NavUtils.navigateUpFromSameTask(this);
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }
}
