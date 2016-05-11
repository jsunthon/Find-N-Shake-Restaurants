package com.bignerdranch.android.randomrestaurants;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

    public RestaurantFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_restaurant, container, false);

        UUID restaurantId = (UUID) getActivity().getIntent().getSerializableExtra(RestaurantActivity.EXTRA_RESTAURANT_ID);
        restaurant = RestaurantLab.get(getActivity()).getRestaurant(restaurantId);

        ((TextView) rootView.findViewById(R.id.restaurant_name_detail_text)).setText(restaurant.getName());
        ((TextView) rootView.findViewById(R.id.restaurant_address_detail_text)).setText(restaurant.getAddress());
        ((TextView) rootView.findViewById(R.id.restaurant_phone_detail_text)).setText(restaurant.getPhone());
        ((TextView) rootView.findViewById(R.id.restaurant_rating_detail_text)).setText(String.valueOf(restaurant.getRating()));
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
}
