package com.bignerdranch.android.randomrestaurants;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.bignerdranch.android.data.FavoritesDbHelper;
import com.bignerdranch.android.googleplayservice.GoogleMapActivity;
import com.bignerdranch.android.models.Restaurant;
import com.bignerdranch.android.models.RestaurantLab;
import java.util.UUID;

// with help from Big Nerd Ranch Guide
public class RestaurantFragment extends Fragment {

    private Restaurant mRestaurant;
    private final String LOG_TAG = getClass().getSimpleName();
    private static final String RESTAURANT_SHARE_HASHTAG = " #RestaurantFinderApp ";
    private static final String DIALOG_IMG = "DialogImg";
    private static final String ARG_RESTAURANT_ID = "restaurant_id";
    private static final String ARG_RESTAURANT_TYPE = "restaurant_type";
    private Button mShowMap;
    private Button mShowDirections;
    private ImageView mMainImgView;
    private ImageView mSnippetImgView;
    private Button mFavorite;
    private FavoritesDbHelper db;

    private ShareActionProvider mShareActionProvider;

    public RestaurantFragment() {
        setHasOptionsMenu(true);
    }

    public static RestaurantFragment newInstance(UUID restaurantId, String type) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_RESTAURANT_ID, restaurantId);
        args.putString(ARG_RESTAURANT_TYPE, type);
        RestaurantFragment fragment = new RestaurantFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID restaurantId = (UUID) getArguments().getSerializable(ARG_RESTAURANT_ID);
        String restaurantType = getArguments().getString(ARG_RESTAURANT_TYPE);

        if (restaurantType.equals("random")) {
            mRestaurant = RestaurantLab.get(getActivity()).getRestaurant(restaurantId);
        } else if (restaurantType.equals("favorite")) {
            mRestaurant = RestaurantLab.get(getActivity()).getFavoriteRestaurant(restaurantId);
        }
        db = new FavoritesDbHelper(this.getContext());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_fragment, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_item_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareRestaurantIntent());
        } else
            Log.d(LOG_TAG, "Share Action Provider is null");
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            getActivity().setTitle(mRestaurant.getName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView;
        if(!db.restaurantExists(mRestaurant)) {
            rootView = inflater.inflate(R.layout.fragment_restaurant, container, false);
            mFavorite = (Button) rootView.findViewById(R.id.favorite);
            mFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (db.insertData(mRestaurant))
                        Log.v(LOG_TAG, "Inserted");
                    Intent intent = getActivity().getIntent();
                    getActivity().finish();
                    startActivity(intent);
                }
            });
        } else{
            rootView = inflater.inflate(R.layout.fragment_restaurant_favorited, container, false);
            mFavorite = (Button) rootView.findViewById(R.id.favorite);
            mFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (db.removeData(mRestaurant) != 0)
                        Log.v(LOG_TAG, "Removed");
                    Intent intent = getActivity().getIntent();
                    getActivity().finish();
                    startActivity(intent);
                }
            });
        }

        mShowMap = (Button) rootView.findViewById(R.id.google_map_btn);
        mShowDirections = (Button) rootView.findViewById(R.id.google_map_directions);


        SharedPreferences sharedPrefs = getActivity().getSharedPreferences("location_prefs", 0);
        Double mCurrentLatitude = Double.valueOf(sharedPrefs.getString("mLatitude", "0"));
        Double mCurrentLongitude = Double.valueOf(sharedPrefs.getString("mLongitude", "0"));

        if (mCurrentLatitude != 0.00 && mCurrentLongitude != 0.00) {
            mShowDirections.setVisibility(View.VISIBLE);
            mShowDirections.setOnClickListener(new View.OnClickListener() {
                                                   @Override
                                                   public void onClick(View v) {
                                                       SharedPreferences sharedPrefs = getActivity().getSharedPreferences("location_prefs", 0);
                                                       Double mCurrentLatitude = Double.valueOf(sharedPrefs.getString("mLatitude", " "));
                                                       Double mCurrentLongitude = Double.valueOf(sharedPrefs.getString("mLongitude", " "));

                                                       String url = "http://maps.google.com/maps?" +
                                                               "saddr=" + mCurrentLatitude + "," + mCurrentLongitude + "" +
                                                               "&daddr=" + mRestaurant.getLatitude() + "," + mRestaurant.getLongitude() + "&mode=driving";
                                                       Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                                       startActivity(mapIntent);
                                                   }
                                               }
            );
        } else {
            mShowDirections.setVisibility(View.INVISIBLE);
        }

        if (mRestaurant.getLatitude() == 0.00 && mRestaurant.getLongitude() == 0.00) {
            mShowMap.setText(mRestaurant.getName() + " coordinate location and map not available");
        } else {
            mShowMap.setText("Map Location");
            mShowMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = GoogleMapActivity.newIntent(getActivity(), mRestaurant.getLatitude(),
                            mRestaurant.getLongitude(), mRestaurant.getName()
                            , mRestaurant.getAddress(), mRestaurant.getPhone(),
                            mRestaurant.getRating());
                    startActivity(intent);
                }
            });

        }



        ((TextView) rootView.findViewById(R.id.restaurant_name_detail_text)).setText(mRestaurant.getName());
        ((TextView) rootView.findViewById(R.id.restaurant_address_detail_text)).setText(mRestaurant.getAddress());

        if (mRestaurant.getPhone() != "123") {
            ((TextView) rootView.findViewById(R.id.restaurant_phone_detail_text)).setText(mRestaurant.getPhone());
        } else {
            ((TextView) rootView.findViewById(R.id.restaurant_phone_detail_text)).setText("Phone number not available.");
        }

        mMainImgView = (ImageView) rootView.findViewById(R.id.main_img_view);
        mMainImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                ImageViewFragment imageViewFragment = ImageViewFragment.newInstance(mRestaurant.getName() + " Food Image", mRestaurant.getImageUrl());
                imageViewFragment.show(manager, DIALOG_IMG);
            }
        });

        mSnippetImgView = (ImageView) rootView.findViewById(R.id.snippet_img_view);
        mSnippetImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                ImageViewFragment imageViewFragment = ImageViewFragment.newInstance(mRestaurant.getName() + " Snippet Image", mRestaurant.getSnippetImageUrl());
                imageViewFragment.show(manager, DIALOG_IMG);
            }
        });

        new DownloadImageTask(mMainImgView)
                .execute(mRestaurant.getImageUrl());
        new DownloadImageTask((ImageView) rootView.findViewById(R.id.snippet_img_view))
                .execute(mRestaurant.getSnippetImageUrl());
        new DownloadImageTask((ImageView) rootView.findViewById(R.id.rating_img_view))
                .execute(mRestaurant.getRatingUrl());
        return rootView;
    }

    private Intent createShareRestaurantIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mRestaurant.getName()
                + ",  "
                + mRestaurant.getAddress()
                + RESTAURANT_SHARE_HASHTAG);
        return shareIntent;
    }
}
