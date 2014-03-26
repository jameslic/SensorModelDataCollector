package com.clarkson.sensormodeldatacollector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
//import android.widget.Button;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * An example full-screen activity 
 */
public class MainMenu extends Activity {
    

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    //private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    //private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    //private SystemUiHider mSystemUiHider;
	private CheckBox mConnectToServerCheckBox;
	private EditText mWifiScanIntervalTextField;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_menu);
        mConnectToServerCheckBox = (CheckBox) findViewById(R.id.connectToServerCheckBox);
        mWifiScanIntervalTextField = (EditText)findViewById(R.id.timer_milliseconds);
        
Log.d(getLocalClassName(), "Step 1");
        
        
    }

    
    public void wifiScan(View view)
    {
    	Log.d(getLocalClassName(), "Step 2");
    	//Intent intent = new Intent(getApplicationContext(), apc.examples.AboutActivity.class); 
    	Intent intent = new Intent(getApplicationContext(), com.clarkson.sensormodeldatacollector.WiFiScanActivity.class);
    	intent.putExtra("mConnectToServer", mConnectToServerCheckBox.isChecked());
    	intent.putExtra("mWifiScanIntervalMilliseconds", Long.getLong(mWifiScanIntervalTextField.getText().toString()));
    	startActivity(intent);
    }
}
