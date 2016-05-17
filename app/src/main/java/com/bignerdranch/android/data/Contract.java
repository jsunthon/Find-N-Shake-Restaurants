package com.bignerdranch.android.data;

import android.provider.BaseColumns;

/**
 * Created by SteveShim on 5/16/2016.
 */
public class Contract {
    public static final class LocationEntry implements BaseColumns{
        public static final String TABLE_NAME = "location";

        public static final String COLUMN_LOCATION_SETTING = "location_setting";

        public static final String COLUMN_CITY_NAME = "city_name";

        public static final String COLUMN_COORD_LAT = "coord_lat";
        public static final String COLUMN_COORD_LONG = "coord_long";
    }

    public static final class RestaurantEntry implements BaseColumns{
        public static final String TABLE_NAME = "restaurant";

        //Foreign key location table
        public static final String COLUMN_LOC_KEY = "location_id";

        //Category that was randomly queried
        public static final String COLUMN_CAT_KEY = "query";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PHONE = "phone";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_IMAGE_URL = "image_url";
        public static final String COLUMN_SNIPPET_IMAGE = "snippet_image";
        public static final String COLUMN_RATING_IMAGE = "rating_image";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_CATEGORIES = "categories";
    }

}
