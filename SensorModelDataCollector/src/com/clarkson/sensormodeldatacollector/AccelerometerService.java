package com.clarkson.sensormodeldatacollector;
import java.util.List;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

import com.clarkson.sensormodeldatacollector.util.SystemUiHider;

/**
 * Created by jlicata on 3/22/2015.
 */
public class AccelerometerService extends Service implements SensorEventListener {
    final static String MY_ACTION = "MY_ACTION";
    private TextView output;
    private String reading;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private long mLastUpdateTimeMilliseconds = 0;
    static final String LOG_TAG = "AccelerometerService";
    Intent intent = new Intent("com.clarkson.sensormodeldatacollector.AccelerometerService.MY_ACTION");

    @Override
    //public void onStartCommand() {
    public void onCreate() {
        Log.d(LOG_TAG, "onStartCommand");
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if(mAccelerometer != null)
        {
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }//onCreate

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        mSensorManager.unregisterListener(this);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long current_time = SystemClock.uptimeMillis();
        if(current_time - mLastUpdateTimeMilliseconds > 999) {
            Log.d(LOG_TAG, "onSensorChanged");
            StringBuilder builder = new StringBuilder();
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                builder.append(x);
                builder.append(",");
                builder.append(y);
                builder.append(",");
                builder.append(z);
                builder.append("\n");
                Log.d("AccelerometerService", "Accelerometer readings: X: " + x + " Y: " + y + " Z: " + z);
            }//if

            reading = builder.toString();

            //Send back reading to Activity
            intent.putExtra("measurement", reading);
            intent.putExtra("timestamp", Long.toString(SystemClock.uptimeMillis()));
            sendBroadcast(intent);
            mLastUpdateTimeMilliseconds = current_time;
        }
    }
}
