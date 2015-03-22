package com.clarkson.sensormodeldatacollector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by jlicata on 3/22/2015.
 */
public class AccelerometerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String measurement = intent.getStringExtra("measurement");
        String timestamp = intent.getStringExtra("timestamp");
        long timestamp_value = Long.parseLong(timestamp);
        //mAccelerometerReadings.add(timestamp + "," + measurement);
        //context.openOrCreateDatabase()
        Log.d("TEST", timestamp + "," + measurement);
    }//onReceive
}
