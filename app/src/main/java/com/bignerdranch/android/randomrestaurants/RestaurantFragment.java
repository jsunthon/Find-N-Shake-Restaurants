package com.bignerdranch.android.randomrestaurants;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bignerdranch.android.models.Restaurant;
import com.bignerdranch.android.models.RestaurantLab;

import java.util.UUID;

/**
 * A placeholder fragment containing a simple view.
 */
public class RestaurantFragment extends Fragment {

    private Restaurant restaurant;

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

        return rootView;
    }
}
