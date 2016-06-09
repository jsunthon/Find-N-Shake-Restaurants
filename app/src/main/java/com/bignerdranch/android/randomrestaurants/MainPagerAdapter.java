package com.bignerdranch.android.randomrestaurants;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

// thanks to http://www.androidwarriors.com/2015/10/tablayout-with-viewpager-android.html
public class MainPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;
    private final int PAGE_COUNT = 2;

    public MainPagerAdapter(FragmentManager fm, Context context){
        super(fm);
        mContext = context;
    }
    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    public Fragment getItem(int position){
        switch (position){
            case 0:
                return new RestaurantsListFragment();
            case 1:
                return new FavoriteListFragment();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position){
        switch(position){
            case 0:
                return mContext.getString(R.string.random_page_title);
            case 1:
                return mContext.getString(R.string.favorite_page_title);
            default:
                return null;
        }
    }
}
