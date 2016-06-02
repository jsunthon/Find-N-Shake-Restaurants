package com.bignerdranch.android.randomrestaurants;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import com.bignerdranch.android.googleplayservice.GoogleMapActivity;
import com.bignerdranch.android.models.Restaurant;
import com.bignerdranch.android.models.RestaurantLab;

import java.io.InputStream;
import java.util.UUID;

/**
 * A placeholder fragment containing a simple view.
 */
public class RestaurantFragment extends Fragment {

    private Restaurant restaurant;
    private final String LOG_TAG = getClass().getSimpleName();
    private Button mShowMap;
    private Button mShowDirections;
    private ShareActionProvider mShareActionProvider;
    private static final String RESTAURANT_SHARE_HASHTAG = " #RestaurantFinderApp ";
    public RestaurantFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_fragment, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_item_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if(mShareActionProvider != null)
        {
            mShareActionProvider.setShareIntent(createShareRestaurantIntent());
        }
        else
            Log.d(LOG_TAG, "Share Action Provider is null");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_restaurant, container, false);

        final UUID restaurantId = (UUID) getActivity().getIntent().getSerializableExtra(RestaurantActivity.EXTRA_RESTAURANT_ID);
        restaurant = RestaurantLab.get(getActivity()).getRestaurant(restaurantId);

        mShowMap = (Button) rootView.findViewById(R.id.google_map_btn);

        mShowDirections = (Button) rootView.findViewById(R.id.google_map_directions);

        if (restaurant.getLatitude() == 0.00 && restaurant.getLongitude() == 0.00) {
            mShowMap.setText(restaurant.getName() + " coordinate location and map not available");
        } else {
            mShowMap.setText("Find " + restaurant.getName());
            mShowMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = GoogleMapActivity.newIntent(getActivity(), restaurant.getLatitude(),
                            restaurant.getLongitude(), restaurant.getName()
                            , restaurant.getAddress(), restaurant.getPhone(),
                            restaurant.getRating());
                    startActivity(intent);
                }
            });
        }

        if(restaurant.getLatitude() == 0.0 && restaurant.getLongitude() == 0.0)
        {
            mShowDirections.setText(restaurant.getName() + "coordinate location and map not available");
        }
        else
        {
            mShowDirections.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences sharedPrefs = getActivity().getSharedPreferences("location_prefs", 0);
                    Double mCurrentLatitude = Double.valueOf(sharedPrefs.getString("mLatitude", ""));
                    Double mCurrentLongitude = Double.valueOf(sharedPrefs.getString("mLongitude", ""));

                    String url = "http://maps.google.com/maps?" +
                            "saddr=" + mCurrentLatitude + "," + mCurrentLongitude + "" +
                            "&daddr=" + restaurant.getLatitude() + "," + restaurant.getLongitude() + "&mode=driving";
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(mapIntent);
                }

                }
            );

        }

        ((TextView) rootView.findViewById(R.id.restaurant_name_detail_text)).setText(restaurant.getName());
        ((TextView) rootView.findViewById(R.id.restaurant_address_detail_text)).setText(restaurant.getAddress());

        if (restaurant.getPhone() != "123") {
            ((TextView) rootView.findViewById(R.id.restaurant_phone_detail_text)).setText(restaurant.getPhone());
        } else {
            ((TextView) rootView.findViewById(R.id.restaurant_phone_detail_text)).setText("Phone number not available.");
        }
        new DownloadImageTask((ImageView) rootView.findViewById(R.id.main_img_view))
                .execute(restaurant.getImageUrl());
        new DownloadImageTask((ImageView) rootView.findViewById(R.id.snippet_img_view))
                .execute(restaurant.getSnippetImageUrl());
        new DownloadImageTask((ImageView) rootView.findViewById(R.id.rating_img_view))
                .execute(restaurant.getRatingUrl());
        return rootView;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Couldnt process or download the image...");
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    private Intent createShareRestaurantIntent()
    {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, restaurant.getName()
                +",  "
                + restaurant.getAddress()
                + RESTAURANT_SHARE_HASHTAG);
        return shareIntent;
    }


}
