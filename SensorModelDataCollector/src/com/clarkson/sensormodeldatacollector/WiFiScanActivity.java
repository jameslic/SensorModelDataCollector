package com.clarkson.sensormodeldatacollector;    
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;    
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;    
import java.util.Queue;
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

/**
* Wi-Fi Scan Activity.
* 
* <P>Controls starting and stopping the wi-fi scan activity for data collection
*  
* <P>Note that a timer activity thread is used to schedule the repeating Wi-Fi scans.
*  
* @author James Licata
* @version 1.0
*/
public class WiFiScanActivity extends Activity implements OnClickListener
 {      
	//The wifi manager provides the system level API calls to the wifi hardware
    WifiManager mWifiManager;
    //Member variable to hold the AP count for a given scan
    int mWifiManagerScanResultsCount = 0;
    //List member variable to store wifi scan results
    List<ScanResult> mWifiManagerScanResults;
    //Placeholder value for hash key for a wifi scan result
    String ITEM_KEY = "key";
    //Placeholder array list using hash keys for wifi scan results
    ArrayList<HashMap<String, WiFiScanResult>> mScanResultHash = new ArrayList<HashMap<String, WiFiScanResult>>();
    //Arraylist to store wifi scan results
    ArrayList<WiFiScanResult> mScanResultsArrayList = new ArrayList<WiFiScanResult>();
    //Custom Android Adapter for updating list views with wifi scan results
    WiFiScanResultAdapter mWifiScanResultAdapter;
    //Public member variable tracking the current number of scans count
    public int mNumberOfScansCounter = 0;    
    
    final int NUMBER_DEFAULT_SCAN_ITEMS = 2;
    //Historical hashmap to continue to collect RSS values for APs we encounter in multiple scans
    WiFiScanResultHashMap mScanResultsHashMap = new WiFiScanResultHashMap();
    
    //The list view object gives a handle to the list that appears to the user on the Wi-Fi activity page
    ListView mScanResultsListView;
    //The button object provides a handle to the Start/Stop scan button that appears to the user on the Wi-Fi activity page
    Button mStartStopScanButton;
    //The Edit Text object provides a handle to the value entered by the user for the scan interval on the configuration/main activity page
    EditText mUserScanIntervalInput;
    //Wifi Scan Timer Task Members
    //Wifi scheduled timer task
    TimerTask mTimerTask;
    //Wifi scan timer
    Timer mScanTimer = new Timer();
    //Defined handler for the wifi scan timer task
    private Handler mScanTimerHandler = new Handler();

    /** 
     * Called when the activity is first created
     * 
     * This method is basically the "constructor" for the activity.
     * @param savedInstanceState - top level android application instance
     */
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
        
        
        Resources res =getResources();
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager.isWifiEnabled() == false)
        {
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
            mWifiManager.setWifiEnabled(true);
        }//if  
        //Initialize the list view with default data
        setWiFiScanListData();
        mWifiScanResultAdapter = new WiFiScanResultAdapter(this, mScanResultsArrayList, res);
        mScanResultsListView.setAdapter(mWifiScanResultAdapter);

        registerReceiver(new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context c, Intent intent) 
            {
               mWifiManagerScanResults = mWifiManager.getScanResults();
               mWifiManagerScanResultsCount = mWifiManagerScanResults.size();
               Log.d(getLocalClassName(), "Step 4: HandleScanResults");
               handleScanResults();
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));            
    }

    /** 
     * Called when the activity is first created
     * 
     * This method initializes the wifi scan result list view with default values to orient 
     * the user to the displayed data format
     */    
    public void setWiFiScanListData()
    {         
    	long milliseconds_since_boot = 0;
        for (int i = 0; i < NUMBER_DEFAULT_SCAN_ITEMS; i++) 
        {
        	Log.d(getLocalClassName(), "Filling default item#"+i);
            final WiFiScanResult default_wifi_scan_result = new WiFiScanResult();
                 
            //Set the wifi scan result members
            
            milliseconds_since_boot = SystemClock.uptimeMillis();
            Log.d(getLocalClassName(), "SystemClock: "+milliseconds_since_boot);
            default_wifi_scan_result.setSSID("Test SSID#"+i);
            default_wifi_scan_result.setRSS(i, milliseconds_since_boot);
            //dummy_item.setFrequency(i*1000);
                
            //Add default item to the array list
            mScanResultsArrayList.add( default_wifi_scan_result );
        }//for
         
    }//setWiFiScanListData
    
    /** 
     * Called when the Start/Stop scan button is pressed
     * 
     * This method clears out any previous scan results and schedules a new wifi scan timer task
     * If the scan is already running, then this method halts the timer task and resets the scan counter
     * @param view - the view shed for the current activity
     */  
    public void onClick(View view) 
    {
    	Log.d(getLocalClassName(), "Step 5");
    	
    	
    	if(mStartStopScanButton.getText().toString().equals(getResources().getString(R.string.perform_scan)))
    	{
    		mScanResultsArrayList.clear();
    		mWifiScanResultAdapter.disableItemsSelection();
    		Log.d(getLocalClassName(), "Step 5.1");
    	   //startTime = SystemClock.uptimeMillis();
    	   mStartStopScanButton.setText(R.string.stop_scan);
    	   scheduleWifiScanTimerTask();  	   
    	}//if
    	else
    	{
    		mWifiScanResultAdapter.enableItemsSelection();
    		Log.d(getLocalClassName(), "Step 5.2");
    		//Halt the wifi scan timer task
    		mTimerTask.cancel();
    		//Set the Scan button text back to performing the scan
    		mStartStopScanButton.setText(R.string.perform_scan);
    		//Reset the scan counter
    		mNumberOfScansCounter = 0;
    		
    	}//else
    }//onClick
    
    /** 
     * Called to schedule a wifi scan timer task
     * 
     * This method initializes the wifi scan timer task and calls the performScan method.
     */  
    public void scheduleWifiScanTimerTask()
    {    	 
    	mTimerTask = new TimerTask() 
    	{
    	    public void run() 
    	    {
    	      	mScanTimerHandler.post(new Runnable() 
    	      	{
    	            public void run() 
    	            {
    	                performScan();
    	                Log.d("TIMER", "TimerTask run");
    	            }//run
    	        });//Runnable
    	}};//TimerTask
 
        // public void schedule (TimerTask task, long delay, long period) 
    	//mScanTimer.schedule(mTimerTask, 500, Integer.parseInt(scanText.getText().toString()));
    	mScanTimer.schedule(mTimerTask, 500, 5000);
 
    }//scheduleWifiScanTimerTask
    
    /** 
     * Called to start a wifi scan
     * 
     * This method utilizes the wifi manager to start a wifi scan
     */  
    public void performScan()
    {
    	mWifiManager.startScan();
    }//performScan
    
    /** 
     * Called from within the registered system broadcast receiver 
     * to handle a completed wifi scan's results
     * 
     * This method iterates through the wifi AP scan results and creates WiFiScanResult objects.
     * These objects are added to a HashSet, duplicates are removed, and added to the array list
     * that is connected to the Activity list view through our custom adapter.
     */  
    public void handleScanResults()
    {    	
        Log.d(getLocalClassName(), "Step 4");
        //Increase the scan counter
        mNumberOfScansCounter++;
        //Alert user what the current scan number is so he/she knows wifi scans are continuing to complete successfully
        Toast.makeText(this, "Scanning...Scan #"+mNumberOfScansCounter, Toast.LENGTH_SHORT).show();
        //Log our current scan count
        Log.d(getLocalClassName(), "STEP 5A: scan #"+mNumberOfScansCounter);
        try 
        {
        	//Scan results are 0 indexed, so subtract one for our while
            mWifiManagerScanResultsCount = mWifiManagerScanResultsCount - 1;
            //Loop through the scan results and add them to our array list
            while (mWifiManagerScanResultsCount >= 0) 
            {   
            	//Need to utilize hash functionality and update RSS/keep history
                //HashMap<String, String> item = new HashMap<String, String>();              	
                WiFiScanResult scan_result_item = new WiFiScanResult();
                //item.put(ITEM_KEY, results.get(size).SSID + "  " + results.get(size).capabilities);
                //Set the SSID (Name) for the scan result
                scan_result_item.setSSID(mWifiManagerScanResults.get(mWifiManagerScanResultsCount).SSID);
                //Set the RSS (signal strength level)
                scan_result_item.setRSS(mWifiManagerScanResults.get(mWifiManagerScanResultsCount).level, SystemClock.uptimeMillis());
                //Set the BSSID (Mac Address)
                scan_result_item.setMac_address(mWifiManagerScanResults.get(mWifiManagerScanResultsCount).BSSID);
                //Set the frequency
                scan_result_item.setFrequency(mWifiManagerScanResults.get(mWifiManagerScanResultsCount).frequency);
                //Log the scan result fields
                Log.d(getLocalClassName(), "STEP 5B: " + scan_result_item.printMe());
                //Add the scan result item to the results Array List
                mScanResultsHashMap.addItem(scan_result_item);
                //mScanResultsArrayList.add(scan_result_item);
                //Use a hash set to remove duplicates
                //HashSet<WiFiScanResult> duplicate_filter = new HashSet<WiFiScanResult>();
                //duplicate_filter.addAll(mScanResultsArrayList);
                mScanResultsArrayList.clear();                                
                mScanResultsArrayList.addAll(mScanResultsHashMap.values());
                Collections.sort(mScanResultsArrayList);
                
                mWifiManagerScanResultsCount--;
                mWifiScanResultAdapter.notifyDataSetChanged();
                Log.d(getLocalClassName(), "Step 6.2: (Size= " + mScanResultsArrayList.size());
            }//while 
        }//try
        catch (Exception e)
        { 
        	Log.d(getLocalClassName(), "Step 6.2A: Errors");
        }//catch           
    }//handleScanResults
    
    /*****************  This function used by WiFiScanResultAdapter ****************/
    public void onItemClick(int mPosition)
    {
    	Log.d(getLocalClassName(), "Step 10: OnItemClick");
        WiFiScanResult tempValues = ( WiFiScanResult ) mScanResultsArrayList.get(mPosition);

       // SHOW ALERT                  
        Queue<TimestampedRSS> all_rss_values = tempValues.getAllRSS();
        String rss_string = "\nRSS: ";
        Iterator<TimestampedRSS> rss_iterator = all_rss_values.iterator();

        while (rss_iterator.hasNext() == true)
        {
        	Log.d(getLocalClassName(), "Step 10A: RSS Strings Assembled");
        	rss_string = rss_string + (rss_iterator.next().toString()) + ", ";        	
        }
        /*try {
            String TestString="";

            FileOutputStream fOut = openFileOutput(filename, MODE_WORLD_READABLE);

            OutputStreamWriter osw = new OutputStreamWriter(fOut); 

               // Write the string to the file
             for( i=1; i<total_row; i++)
                {

                    for( j=1; j<total_col; j++)
                    {
                        TestString+=table[i][j].getText().toString();        // to pass in every widget a context of activity (necessary) 
                        TestString += " ,";
                    }
                     TestString+="\n";
                }
             Log.v("the string is",TestString);
             osw.write(TestString);
             osw.flush();
             osw.close();
            }
            catch (IOException ioe) 
              {ioe.printStackTrace();}*/
        Toast.makeText(this,
                ""+tempValues.getSSID()
                  + rss_string
                  +"\nFreq: "+tempValues.getFrequency()
                  + "\nChan: "+tempValues.getChannel()
                  + "\nMAC: " + tempValues.getMac_address(),
                Toast.LENGTH_LONG)
        .show();
    }
}