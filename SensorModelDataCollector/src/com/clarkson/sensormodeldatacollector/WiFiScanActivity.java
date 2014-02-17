package com.clarkson.sensormodeldatacollector;    
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;    
import java.util.HashSet;
import java.util.List;    
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;    
import android.content.Intent;     
import android.content.IntentFilter;    
import android.content.res.Resources;
import android.net.wifi.ScanResult;    
import android.net.wifi.WifiConfiguration;   
import android.net.wifi.WifiManager;    
import android.os.Bundle;    
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;   
import android.view.View;    
import android.view.View.OnClickListener;    
import android.widget.AdapterView;    
import android.widget.Button;    
import android.widget.EditText;
import android.widget.ListView;    
import android.widget.SimpleAdapter;    
import android.widget.TextView;    
import android.widget.Toast;

public class WiFiScanActivity extends Activity implements OnClickListener
 {      
    WifiManager mWifiManager;       
    ListView mScanResultsListView;
    Button mStartStopScanButton;
    EditText mUserScanIntervalInput;
    int size = 0;
    List<ScanResult> mWifiManagerScanResults;
    String ITEM_KEY = "key";
    ArrayList<HashMap<String, String>> arraylist = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, WiFiScanResult>> mScanResultHash = new ArrayList<HashMap<String, WiFiScanResult>>();
    ArrayList<WiFiScanResult> mScanResultsArray = new ArrayList<WiFiScanResult>();
    WiFiScanResultAdapter mWifiScanResultAdapter;
    public int mNumberOfScansCounter = 0;
    
    //Wifi Scan Timer Task Members
    TimerTask mTimerTask;
    Timer mScanTimer = new Timer();
    private Handler mScanTimerHandler = new Handler();

    /* Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.wifi_scan_layout);
        Log.d(getLocalClassName(), "Step 3");
        mStartStopScanButton = (Button) findViewById(R.id.start_scan_button);
        mStartStopScanButton.setOnClickListener(this);
        mUserScanIntervalInput = (EditText)findViewById(R.id.timer_milliseconds);

        
        mScanResultsListView = (ListView)findViewById(R.id.AP_list);
        /******** Take some data in Arraylist ( WiFiScanResult Array ) ***********/
        
        Resources res =getResources();
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager.isWifiEnabled() == false)
        {
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
            mWifiManager.setWifiEnabled(true);
        }  
        setWiFiScanListData();
        mWifiScanResultAdapter = new WiFiScanResultAdapter(this, mScanResultsArray, res);
        mScanResultsListView.setAdapter(mWifiScanResultAdapter);

        registerReceiver(new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context c, Intent intent) 
            {
               mWifiManagerScanResults = mWifiManager.getScanResults();
               size = mWifiManagerScanResults.size();
               Log.d(getLocalClassName(), "Step 4: HandleScanResults");
               HandleScanResults();
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));            
    }

    /****** Function to set data in ArrayList *************/
    public void setWiFiScanListData()
    {
         
        for (int i = 0; i < 2; i++) {
        	Log.d(getLocalClassName(), "Filling dummy item#"+i);
            final WiFiScanResult dummy_item = new WiFiScanResult();
                 
              /******* Firstly take data in model object ******/
            dummy_item.setSSID("Test SSID#"+i);
            dummy_item.setRSS(i);
            //dummy_item.setFrequency(i*1000);
                
            /******** Take Model Object in ArrayList **********/
            mScanResultsArray.add( dummy_item );
        }
         
    }
    
    public void onClick(View view) 
    {
    	Log.d(getLocalClassName(), "Step 5");
    	
    	mScanResultsArray.clear();
    	if(mStartStopScanButton.getText().toString().equals(getResources().getString(R.string.perform_scan)))
    	{
    		Log.d(getLocalClassName(), "Step 5.1");
    	   //startTime = SystemClock.uptimeMillis();
    	   mStartStopScanButton.setText(R.string.stop_scan);
    	   doTimerTask();  	   
    	}//if
    	else
    	{
    		Log.d(getLocalClassName(), "Step 5.2");
    		mTimerTask.cancel();
    		mStartStopScanButton.setText(R.string.perform_scan);
    		mNumberOfScansCounter = 0;
    	}
    }//onClick
    
    public void doTimerTask(){
    	 
    	mTimerTask = new TimerTask() {
    	        public void run() {
    	        	mScanTimerHandler.post(new Runnable() {
    	                        public void run() {
    	                        	PerformScan();
    	                        	Log.d("TIMER", "TimerTask run");
    	                        }
    	               });
    	        }};
 
            // public void schedule (TimerTask task, long delay, long period) 
    	        
    	        //mScanTimer.schedule(mTimerTask, 500, Integer.parseInt(scanText.getText().toString()));
    	        mScanTimer.schedule(mTimerTask, 500, 5000);
 
    	 }
    
    
    public void PerformScan()
    {
    	mWifiManager.startScan();
    }
    
    public void HandleScanResults()
    {    	
        Log.d(getLocalClassName(), "Step 4");
        mNumberOfScansCounter++;
        Toast.makeText(this, "Scanning...Scan #"+mNumberOfScansCounter, Toast.LENGTH_SHORT).show();
        Log.d(getLocalClassName(), "STEP 5A: scan #"+mNumberOfScansCounter);
        try 
        {
            size = size - 1;
            while (size >= 0) 
            {   
            	//Need to utilize hash functionality and update RSS/keep history
                //HashMap<String, String> item = new HashMap<String, String>();              	
                WiFiScanResult scan_result_item = new WiFiScanResult();
                //item.put(ITEM_KEY, results.get(size).SSID + "  " + results.get(size).capabilities);
                scan_result_item.setSSID(mWifiManagerScanResults.get(size).SSID);
                scan_result_item.setRSS(mWifiManagerScanResults.get(size).level);
                scan_result_item.setMac_address(mWifiManagerScanResults.get(size).BSSID);
                scan_result_item.setFrequency(mWifiManagerScanResults.get(size).frequency);                
                Log.d(getLocalClassName(), "STEP 5B: " + scan_result_item.printMe());
                //arraylist.add(item);
                mScanResultsArray.add(scan_result_item);
                HashSet<WiFiScanResult> duplicate_filter = new HashSet<WiFiScanResult>();
                duplicate_filter.addAll(mScanResultsArray);
                mScanResultsArray.clear();
                mScanResultsArray.addAll(duplicate_filter);
                Collections.sort(mScanResultsArray);
                
                size--;
                mWifiScanResultAdapter.notifyDataSetChanged();
                Log.d(getLocalClassName(), "Step 6.2: (Size= " + mScanResultsArray.size());
            } 
        }
        catch (Exception e)
        { 
        	Log.d(getLocalClassName(), "Step 6.2A: Errors");
        }           
    }
    
    /*****************  This function used by WiFiScanResultAdapter ****************/
    public void onItemClick(int mPosition)
    {
        WiFiScanResult tempValues = ( WiFiScanResult ) mScanResultsArray.get(mPosition);


       // SHOW ALERT                  

        Toast.makeText(this,
                ""+tempValues.getSSID()
                  +"\nRSS: "+tempValues.getRSS()
                  +"\nFreq: "+tempValues.getFrequency()
                  + "\nChan: "+tempValues.getChannel()
                  + "\nMAC: " + tempValues.getMac_address(),
                Toast.LENGTH_LONG)
        .show();
    }
}