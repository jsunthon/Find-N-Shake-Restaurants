package com.bignerdranch.android.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RestaurantDbHelper extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "restaurants.db";

    public RestaurantDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        final String SQL_CREATE_RESTAURANT_TABLE = "CREATE TABLE " + Contract.RestaurantEntry.TABLE_NAME + " ( " +
                Contract.RestaurantEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Contract.RestaurantEntry.COLUMN_CAT_KEY + " INTEGER NOT NULL, " +
                Contract.RestaurantEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                Contract.RestaurantEntry.COLUMN_PHONE + " TEXT NOT NULL, " +
                Contract.RestaurantEntry.COLUMN_ADDRESS + " TEXT NOT NULL, " +
                Contract.RestaurantEntry.COLUMN_RATING + " REAL NOT NULL, " +
                Contract.RestaurantEntry.COLUMN_RATING_IMAGE + " TEXT NOT NULL, " +
                Contract.RestaurantEntry.COLUMN_IMAGE_URL + " TEXT NOT NULL, " +
                Contract.RestaurantEntry.COLUMN_SNIPPET_IMAGE + " TEXT NOT NULL, " +
                Contract.RestaurantEntry.COLUMN_CATEGORIES + " TEXT NOT NULL, " +
                Contract.RestaurantEntry.COLUMN_LATITUDE + " REAL NOT NULL, " +
                Contract.RestaurantEntry.COLUMN_LONGITUDE + " REAL NOT NULL, " +

                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + Contract.RestaurantEntry.COLUMN_LOC_KEY + ") REFERENCES " +
                Contract.LocationEntry.TABLE_NAME + " (" + Contract.LocationEntry._ID + "), " +

                // To assure the application have just one restaurant entry per random query
                // per location, it's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + Contract.RestaurantEntry.COLUMN_CAT_KEY + ", " +
                Contract.RestaurantEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + Contract.LocationEntry.TABLE_NAME + " (" +
                Contract.LocationEntry._ID + " INTEGER PRIMARY KEY," +
                Contract.LocationEntry.COLUMN_LOCATION_SETTING + " TEXT UNIQUE NOT NULL, " +
                Contract.LocationEntry.COLUMN_CITY_NAME + " TEXT NOT NULL, " +
                Contract.LocationEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
                Contract.LocationEntry.COLUMN_COORD_LONG + " REAL NOT NULL " +
                " );";

        //db.execSQL(SQL_CREATE_RESTAURANT_TABLE);
        db.execSQL(SQL_CREATE_LOCATION_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1){
        db.execSQL("DROP TABLE IF EXISTS " + Contract.LocationEntry.TABLE_NAME + ";");
        db.execSQL("DROP TABLE IF EXISTS " + Contract.RestaurantEntry.TABLE_NAME + ";");
        onCreate(db);
    }

}
