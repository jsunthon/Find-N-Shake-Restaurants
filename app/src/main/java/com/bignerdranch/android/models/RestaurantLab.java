package com.bignerdranch.android.models;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by jsunthon on 5/10/2016.
 */
public class RestaurantLab {
    private static RestaurantLab sRestaurantLab;
    private List<Restaurant> mRestaurants;
    private List<Restaurant> mFavoriteRestaurants;

    public static RestaurantLab get(Context context) {
        if (sRestaurantLab == null) {
            sRestaurantLab = new RestaurantLab(context);
        }
        return sRestaurantLab; //return an instance of this class
    }

    private RestaurantLab(Context context) {
        mRestaurants = new ArrayList<>();
        mFavoriteRestaurants = new ArrayList<>();
    }

    public void addRestaurant(Restaurant r) {
        mRestaurants.add(r);
    }

    public void addFavoriteRestaurant(Restaurant r) {
        mFavoriteRestaurants.add(r);
    }

    public Restaurant getRestaurant(UUID id) {
        for (Restaurant restaurant: mRestaurants) {
            if (restaurant.getId().equals(id)) {
                return restaurant;
            }
        }
        return null;
    }

    public Restaurant getFavoriteRestaurant(UUID id) {
        for (Restaurant restaurant: mFavoriteRestaurants) {
            if (restaurant.getId().equals(id)) {
                return restaurant;
            }
        }
        return null;
    }

    public List<Restaurant> getRestaurants() {
        return this.mRestaurants;
    }

    public List<Restaurant> getFavoriteRestaurants() {
        return this.mFavoriteRestaurants;
    }

    public void resetRestaurants() {
        mRestaurants = new ArrayList<>();
    }

    public void resetFavoriteRestaurants() {
        mFavoriteRestaurants = new ArrayList<>();
    }
}
