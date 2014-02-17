package com.clarkson.sensormodeldatacollector;    
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;    
import java.util.HashSet;
import java.util.List;    

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
import android.util.Log;   
import android.view.View;    
import android.view.View.OnClickListener;    
import android.widget.AdapterView;    
import android.widget.Button;    
import android.widget.ListView;    
import android.widget.SimpleAdapter;    
import android.widget.TextView;    
import android.widget.Toast;

public class WiFiScanActivity extends Activity implements OnClickListener
 {      
    WifiManager wifi_manger;       
    ListView lv;
    TextView textStatus;
    Button buttonScan;
    int size = 0;
    List<ScanResult> results;
    String ITEM_KEY = "key";
    ArrayList<HashMap<String, String>> arraylist = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, WiFiScanResult>> mScanResultHash = new ArrayList<HashMap<String, WiFiScanResult>>();
    ArrayList<WiFiScanResult> mScanResultsArray = new ArrayList<WiFiScanResult>();
    SimpleAdapter adapter;
    WiFiScanResultAdapter mAdapter;

    /* Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.wifi_scan_layout);
        Log.d(getLocalClassName(), "Step 3");
        buttonScan = (Button) findViewById(R.id.start_scan_button);
        buttonScan.setOnClickListener(this);
        lv = (ListView)findViewById(R.id.AP_list);
        /******** Take some data in Arraylist ( WiFiScanResult Array ) ***********/
        
        Resources res =getResources();
        wifi_manger = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifi_manger.isWifiEnabled() == false)
        {
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
            wifi_manger.setWifiEnabled(true);
        }  
        setWiFiScanListData();
        mAdapter = new WiFiScanResultAdapter(this, mScanResultsArray, res);
        //this.adapter = new SimpleAdapter(WiFiScanActivity.this, arraylist, android.R.layout.simple_list_item_2, new String[] { ITEM_KEY }, new int[] { R.id.AP_list});
        //lv.setAdapter(this.adapter);
        lv.setAdapter(mAdapter);

        registerReceiver(new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context c, Intent intent) 
            {
               results = wifi_manger.getScanResults();
               size = results.size();
               Log.d(getLocalClassName(), "Step 4");
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
        //arraylist.clear();
    	mScanResultsArray.clear();
        wifi_manger.startScan();

        Toast.makeText(this, "Scanning...." + size, Toast.LENGTH_SHORT).show();
        try 
        {
            size = size - 1;
            while (size >= 0) 
            {   
                //HashMap<String, String> item = new HashMap<String, String>();              	
                WiFiScanResult scan_result_item = new WiFiScanResult();
                //item.put(ITEM_KEY, results.get(size).SSID + "  " + results.get(size).capabilities);
                scan_result_item.setSSID(results.get(size).SSID);
                scan_result_item.setRSS(results.get(size).level);
                scan_result_item.setMac_address(results.get(size).BSSID);
                scan_result_item.setFrequency(results.get(size).frequency);                
                Log.d(getLocalClassName(), "STEP 5A: " + scan_result_item.printMe());
                //arraylist.add(item);
                mScanResultsArray.add(scan_result_item);
                HashSet<WiFiScanResult> duplicate_filter = new HashSet<WiFiScanResult>();
                duplicate_filter.addAll(mScanResultsArray);
                mScanResultsArray.clear();
                mScanResultsArray.addAll(duplicate_filter);
                Collections.sort(mScanResultsArray);
                
                size--;
                //adapter.notifyDataSetChanged();
                mAdapter.notifyDataSetChanged();
                Log.d(getLocalClassName(), "STEP 5A: (Size= " + mScanResultsArray.size());
            } 
        }
        catch (Exception e)
        { 
        	Log.d(getLocalClassName(), "Step 4A: Errors");
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