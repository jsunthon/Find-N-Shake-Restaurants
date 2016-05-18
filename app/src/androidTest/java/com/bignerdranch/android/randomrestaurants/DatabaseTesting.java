package com.bignerdranch.android.randomrestaurants;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.bignerdranch.android.data.Contract;
import com.bignerdranch.android.data.RestaurantDbHelper;

import java.util.Map;
import java.util.Set;

/**
 * Created by steveshim on 5/17/16.
 */
public class DatabaseTesting extends AndroidTestCase {

    Context context;
    private final String LOG_TAG = "Data Test";

    @Override
    protected void setUp() throws Exception{
        super.setUp();
        context = getContext();
    }

    public void testInsertData(){
        RestaurantDbHelper helper = new RestaurantDbHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(Contract.LocationEntry.COLUMN_CITY_NAME, "Glendale");
        cv.put(Contract.LocationEntry.COLUMN_LOCATION_SETTING, "Home");
        cv.put(Contract.LocationEntry.COLUMN_COORD_LAT, "" + 90);
        cv.put(Contract.LocationEntry.COLUMN_COORD_LONG, "" + 90);

        Cursor cursor = null;

        try{
            long locId = db.insert(Contract.LocationEntry.TABLE_NAME, null, cv);

            assertTrue(locId != -1);

            cursor = db.query(Contract.LocationEntry.TABLE_NAME, null, null, null, null, null, null);

            assertTrue("No records", cursor.moveToFirst());

            assertTrue(validateCurrentRecord("Records do not match", cursor, cv));


        } catch (Exception e){
            Log.e(LOG_TAG, e.getMessage());
        } finally{
            db.delete(Contract.LocationEntry.TABLE_NAME, null, null);
            //db.delete(Contract.RestaurantEntry.TABLE_NAME, null, null);

            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    static boolean validateCurrentRecord(String errorMessage, Cursor c, ContentValues expected){
        Set<Map.Entry<String, Object>> valueSet = expected.valueSet();
        for (Map.Entry<String, Object> entry : valueSet){
            String column = entry.getKey();
            int idx = c.getColumnIndex(column);
            if (idx == -1) return false;
            String expectedValue = entry.getValue().toString();
            String retrievedValue = c.getString(idx);
            if(!retrievedValue.equals(expectedValue)) return false;
        }
        return true;
    }

}
