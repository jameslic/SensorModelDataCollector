package com.clarkson.sensormodeldatacollector;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.clarkson.sensormodeldatacollector.data.AccelerometerDataReaderContract.AccelerometerDataEntry;

import com.clarkson.sensormodeldatacollector.data.AccelerometerDataReaderDbHelper;

/**
 * Created by jlicata on 3/22/2015.
 */
public class AccelerometerReceiver extends BroadcastReceiver {
    //Database for Accelerometer Data
    AccelerometerDataReaderDbHelper mDbHelper;
    @Override
    public void onReceive(Context context, Intent intent) {
        String measurement = intent.getStringExtra("measurement");
        String timestamp = intent.getStringExtra("timestamp");
        long timestamp_value = Long.parseLong(timestamp);
        //mAccelerometerReadings.add(timestamp + "," + measurement);
        //context.openOrCreateDatabase()
        mDbHelper = new AccelerometerDataReaderDbHelper(context);
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(AccelerometerDataEntry.COLUMN_NAME_TIMESTAMP, timestamp);
        values.put(AccelerometerDataEntry.COLUMN_NAME_X_VALUE, timestamp);
        values.put(AccelerometerDataEntry.COLUMN_NAME_Y_VALUE, timestamp);
        // Insert the new row, returning the primary key value of the new row
        long newRowId = 0;
        newRowId = db.insert(
                AccelerometerDataEntry.TABLE_NAME,
                null,
                values);
        //If a row ID is returned, success
        if(newRowId > 0)
        {
            Log.d("TEST", timestamp + "," + measurement);
        }//if
        db.close();
    }//onReceive
}//AccelerometerReceiver
