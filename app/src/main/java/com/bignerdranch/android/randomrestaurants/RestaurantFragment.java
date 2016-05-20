package com.bignerdranch.android.randomrestaurants;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
    public RestaurantFragment() {
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

        Intent intent = getActivity().getIntent();
        Bundle bundle =  intent.getExtras();



        if(restaurant.getLatitude() == 0.0 && restaurant.getLongitude() == 0.0)
        {
            mShowDirections.setText(restaurant.getName() + "coordinate location and map not available");
        }
        else
        {
            mShowDirections.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = "http://maps.google.com/maps?" +
                            "saddr="+restaurant.getLatitude()+","+restaurant.getLongitude()+"" +
                            "&daddr="+restaurant.getLatitude()+","+restaurant.getLongitude()+"&mode=driving";
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW);

                }
            });
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
}
