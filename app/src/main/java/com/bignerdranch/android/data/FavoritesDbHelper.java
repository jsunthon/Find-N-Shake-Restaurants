package com.bignerdranch.android.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.bignerdranch.android.models.Restaurant;

public class FavoritesDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "favorites.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "favorites";
    private static final String _ID = "id";
    private static final String RES_ID = "uuid";
    private static final String RES_NAME = "name";
    private static final String RES_PHONE = "phone";
    private static final String RES_RATING = "rating";
    private static final String RES_ADDRESS = "address";
    private static final String RES_IMAGE_URL = "image_url";
    private static final String RES_SNIPPET_URL = "snippet_url";
    private static final String RES_RATING_URL = "rating_url";
    private static final String RES_LAT = "latitude";
    private static final String RES_LONG = "longitude";
    private static final String RES_CAT = "categories";

    public FavoritesDbHelper(Context context){
        super (context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                RES_ID + " TEXT," +
                RES_NAME + " TEXT, " +
                RES_PHONE + " TEXT, " +
                RES_RATING + " REAL, " +
                RES_ADDRESS + " TEXT, " +
                RES_IMAGE_URL + " TEXT, " +
                RES_SNIPPET_URL + " TEXT, " +
                RES_RATING_URL + " TEXT, " +
                RES_LAT + " REAL, " +
                RES_LONG + " REAL, " +
                RES_CAT + " TEXT " +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME + ";");
        onCreate(db);
    }

    public boolean insertData(Restaurant r){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(RES_ID, r.getId().toString());
        cv.put(RES_NAME, r.getName());
        cv.put(RES_PHONE, r.getPhone());
        cv.put(RES_RATING, r.getRating());
        cv.put(RES_ADDRESS, r.getAddress());
        cv.put(RES_IMAGE_URL, r.getImageUrl());
        cv.put(RES_SNIPPET_URL, r.getSnippetImageUrl());
        cv.put(RES_RATING_URL, r.getRatingUrl());
        cv.put(RES_LAT, r.getLatitude());
        cv.put(RES_LONG, r.getLongitude());
        cv.put(RES_CAT, r.getCategories());

        long inserted = db.insert(TABLE_NAME, null, cv);
        return (inserted != -1);
    }

    public Cursor getAllData(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor result = db.rawQuery("SELECT * FROM " + TABLE_NAME + ";", null);
        return result;
    }

    public boolean restaurantExists(Restaurant r){
        boolean result = true;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + RES_PHONE + " LIKE '%" + r.getPhone() + "%';", null);
        if (c == null || c.getCount() <= 0)
            result = false;
        c.close();
        return result;
    }

    public int removeData(Restaurant r){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, RES_PHONE + " LIKE '%" + r.getPhone() + "%';", null);
    }
}
