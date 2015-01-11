package com.clarkson.sensormodeldatacollector;    
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;    
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;    
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;    
import android.content.DialogInterface;
import android.content.Intent;     
import android.content.IntentFilter;    
import android.content.res.Resources;
import android.net.wifi.ScanResult;    
import android.net.wifi.WifiConfiguration;   
import android.net.wifi.WifiManager;    
import android.os.Bundle;    
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;   
import android.view.View;    
import android.view.View.OnClickListener;    
import android.widget.AdapterView;    
import android.widget.Button;    
import android.widget.CheckBox;
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
	Bundle mMainMenuExtras;
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
	AlertDialog.Builder mWriteToFileDialog;
	AlertDialog.Builder mResetScanResultsDialog;
	AlertDialog.Builder mServerConnectionDialog;

	public int mCurrentlySelectedItemIndex = -1;
	public String mCurrentlySelectedItemRSSString = "";

	final int NUMBER_DEFAULT_SCAN_ITEMS = 2;
	private final String mDefaultServerURL = "http://192.168.1.6:9999/SensorModelServletProject/SensorModelServlet";
	//Historical hashmap to continue to collect RSS values for APs we encounter in multiple scans
	WiFiScanResultHashMap mScanResultsHashMap = new WiFiScanResultHashMap();
	//Indicator for weather scan is running or not
	public boolean mScanRunning = false;
	public boolean mResetScanResults = false;
    public boolean mReceiverRegistered = false;
	//The list view object gives a handle to the list that appears to the user on the Wi-Fi activity page
	ListView mScanResultsListView;
	//The button object provides a handle to the Start/Stop scan button that appears to the user on the Wi-Fi activity page
	Button mStartStopScanButton;
    //The button object provides a handle to the one shot scan button that appears to the user on the Wi-Fi activity page
    Button mOneShotScanButton;
	//Wifi Scan Timer Task Members
	//Wifi scheduled timer task
	TimerTask mTimerTask;
	//Wifi scan timer
	Timer mScanTimer = new Timer();
	//Defined handler for the wifi scan timer task
	private Handler mScanTimerHandler = new Handler();
	BroadcastReceiver mBroadcastReceiver;
	public long mMillisecondsSinceBoot = 0;
	public long mWifiScanIntervalMilliseconds = 5000; //Default 5 seconds
    public boolean mSSIDFilterEnabled = false;
    public String mSSIDFilter = "CiscoLinksys";

	public boolean mWriteToCSVFile = false;
	
	public boolean mConnectToServer = false; //Default to not connecting to server
	public enum ServerStatus{
		SENSOR_MODEL_SERVER_CONNECTED(4), SENSOR_MODEL_SERVER_DISCONNECTED(0);

		 private ServerStatus(int connection_status){
		    this.connection_status = connection_status;
		  }

		  private int connection_status;

		  public int getStatus(){
		    return this.connection_status;
		  }

		  public String toString(){
		    return String.valueOf(this.connection_status);
		  }
		}
	public ServerStatus mServerConnectionCode;
	

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
        mOneShotScanButton = (Button) findViewById(R.id.one_shot_scan_button);
        mOneShotScanButton.setOnClickListener(this);
		
		mScanResultsListView = (ListView)findViewById(R.id.AP_list);

		mMainMenuExtras = getIntent().getExtras();
		if(mMainMenuExtras != null)
		{
			mConnectToServer = mMainMenuExtras.getBoolean("mConnectToServer");
			mWifiScanIntervalMilliseconds = 1000*mMainMenuExtras.getLong("mWifiScanIntervalSeconds");
            this.mSSIDFilter = mMainMenuExtras.getString("mSSIDFilter");
            this.mSSIDFilterEnabled = mMainMenuExtras.getBoolean("mSSIDFilterEnabled");
		}//if

		Resources application_resources =getResources();
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (mWifiManager.isWifiEnabled() == false)
		{
			Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
			mWifiManager.setWifiEnabled(true);
		}//if  
		//Initialize the list view with default data
		setWiFiScanListData();
		mWifiScanResultAdapter = new WiFiScanResultAdapter(this, mScanResultsArrayList, application_resources);
		mScanResultsListView.setAdapter(mWifiScanResultAdapter);

		buildAlertDialogs();
		
		checkServerConnection();
	}//onCreate

	public void checkServerConnection()
	{
		if(mConnectToServer == true)
		{
			mServerConnectionDialog.show();
		}//if
		else
		{
			//Do nothing
		}//else
	}//checkServerConnection
	
	/** 
	 * Called when the WiFi scan activity exits back to the main menu
	 */   
	@Override
	protected void onStop()
	{
		Log.d(getLocalClassName(), "OnStop, unregistering receiver");
		unregisterReceiver(mBroadcastReceiver);
		super.onStop();
	}//onStop

	/** 
	 * Creates all Dialogs that appear during user interaction with Wifi Scan Activity
	 */   
	private void buildAlertDialogs()
	{
		//Set up our write to file dialog
		mWriteToFileDialog = new AlertDialog.Builder(
				WiFiScanActivity.this);

		// Setting Dialog Title
		mWriteToFileDialog.setTitle("Confirm Write To File");

		// Setting Dialog Message
		mWriteToFileDialog.setMessage("Would you like to capture this AP's scan results in a CSV file?");

		// Setting Icon to Dialog
		mWriteToFileDialog.setIcon(R.drawable.new_document);

		// Setting Positive "Yes" button
		mWriteToFileDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// Write your code here to execute after dialog
				mWriteToCSVFile = true;

				Toast.makeText(getApplicationContext(),
						"Writing to SD Card...", Toast.LENGTH_SHORT)
						.show();
				if(writeAPDataToCSVFile() == true)
				{
					//Let user know file written successfully
					Toast.makeText(getApplicationContext(),
							"File written successfully!", Toast.LENGTH_SHORT)
							.show();
				}//if
				else
				{
					Toast.makeText(getApplicationContext(),
							"Writing file to SD card failed!", Toast.LENGTH_SHORT)
							.show();
				}//else
			}//onClick
		});//setPositiveButton
		// Setting Negative "NO" Button
		mWriteToFileDialog.setNegativeButton("NO",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// Write your code here to execute after dialog
				mWriteToCSVFile = false;
				/*Toast.makeText(getApplicationContext(),
                                "You clicked on NO", Toast.LENGTH_SHORT)
                                .show();*/
				dialog.cancel();
			}//onClick
		});//setNegativeButton
		
		//Set up our reset scan results dialog
		mResetScanResultsDialog = new AlertDialog.Builder(
				WiFiScanActivity.this);

		// Setting Dialog Title
		mResetScanResultsDialog.setTitle("Reset all scan results?");

		// Setting Dialog Message
		mResetScanResultsDialog.setMessage("Would you like reset the scan counter and collected AP data?");

		// Setting Icon to Dialog
		mResetScanResultsDialog.setIcon(R.drawable.ic_delete);

		// Setting Positive "Yes" button
		mResetScanResultsDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// Write your code here to execute after dialog
				mResetScanResults = true;

				Toast.makeText(getApplicationContext(),
						"Resetting scan results...", Toast.LENGTH_SHORT)
						.show();
				resetScanResults();
				
				 try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					//Let user know file written successfully
					Toast.makeText(getApplicationContext(),
							"Scan results reset!", Toast.LENGTH_SHORT)
							.show();				
			}//onClick
		});//setPositiveButton
		// Setting Negative "NO" Button
		mResetScanResultsDialog.setNegativeButton("NO",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// Write your code here to execute after dialog
				mResetScanResults = false;
				/*Toast.makeText(getApplicationContext(),
                                "You clicked on NO", Toast.LENGTH_SHORT)
                                .show();*/
				dialog.cancel();
			}//onClick
		});//setNegativeButton
		
		//Set up our server connection dialog
		mServerConnectionDialog = new AlertDialog.Builder(
				WiFiScanActivity.this);

		// Setting Dialog Title
		mServerConnectionDialog.setTitle("Server Connection");

		// Setting Dialog Message
		mServerConnectionDialog.setMessage("Click OK to check server connection");

		// Setting Icon to Dialog
		mServerConnectionDialog.setIcon(R.drawable.ic_launcher);

		// Setting Positive "Yes" button
		mServerConnectionDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// Write your code here to execute after dialog

				Toast.makeText(getApplicationContext(),
						"Testing server connection...", Toast.LENGTH_SHORT)
						.show();
				if(testServerConnection() == true)
				{
					//Let user know file written successfully
					Toast.makeText(getApplicationContext(),
							"Server connected successfully", Toast.LENGTH_SHORT)
							.show();
				}//if
				else
				{
					Toast.makeText(getApplicationContext(),
							"Problem connecting to server, please check your settings!", Toast.LENGTH_SHORT)
							.show();
				}//else
			}//onClick
		});//setPositiveButton
		// Setting Negative "Cancel" Button
		mServerConnectionDialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// Write your code here to execute after dialog

				/*Toast.makeText(getApplicationContext(),
                                "You clicked on NO", Toast.LENGTH_SHORT)
                                .show();*/
				dialog.cancel();
			}//onClick
		});//setNegativeButton
		
	}//buildAlertDialogs
	
	/** 
	 * Tests the apps ability to connect to the Servlet
	 */   
	boolean testServerConnection()
	{
		boolean server_status = false;
		Random test_code_generator = new Random();
		int test_code_number = test_code_generator.nextInt(10); //Generate random number between 0 and 10
        int test_code_response = 0;
		try{
        	//108.26.49.81
            URL url = new URL(mDefaultServerURL);
            //URL url = new URL("http://108.26.49.81:9999/SensorModelServletProject/SensorModelServlet");
            URLConnection sensor_model_servlet = url.openConnection();
            
            
            //inputString = URLEncoder.encode(inputString, "UTF-8");
            
            String test_code_input_string = String.valueOf(test_code_number);
            Log.d(getLocalClassName(), "Test code input: " + test_code_input_string);

            sensor_model_servlet.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(sensor_model_servlet.getOutputStream());
            out.write(test_code_input_string);
            out.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(sensor_model_servlet.getInputStream()));

            String returnString="";
            
            mServerConnectionCode = ServerStatus.SENSOR_MODEL_SERVER_DISCONNECTED;
            while ((returnString = in.readLine()) != null) 
            {
            	Log.d("returnString", returnString);
            	Log.d("enum value connected: ", ServerStatus.SENSOR_MODEL_SERVER_CONNECTED.toString());
            	test_code_response = Integer.valueOf(returnString);
            }
            in.close();

            }
			catch(Exception e)
            {
                Log.d("Exception",e.toString());
            }

		if(test_code_response == test_code_number*2 && test_code_response <= 20)
        {
        	server_status = true;  
        	Log.d(getLocalClassName(), "Test code response: " + String.valueOf(test_code_response));
        }  
		return server_status;
	}
	
	/** 
	 * Called when the activity is first created
	 * 
	 * This method initializes the wifi scan result list view with default values to orient 
	 * the user to the displayed data format
	 */    
	public void setWiFiScanListData()
	{         
		for (int i = 0; i < NUMBER_DEFAULT_SCAN_ITEMS; i++) 
		{
			Log.d(getLocalClassName(), "Filling default item#"+i);
			final WiFiScanResult default_wifi_scan_result = new WiFiScanResult();

			//Set the wifi scan result members
			
			Log.d(getLocalClassName(), "SystemClock: "+mMillisecondsSinceBoot);
			default_wifi_scan_result.setSSID("Test SSID#"+i);
			default_wifi_scan_result.setRSS(i, mMillisecondsSinceBoot);
			//dummy_item.setFrequency(i*1000);

			//Add default item to the array list
			mScanResultsArrayList.add( default_wifi_scan_result );
		}//for

	}//setWiFiScanListData

	/** 
	 * Called when a button on the WiFiScanActivity view is pressed
	 *
     * Decides which method to call based on the buton type pressed
	 * @param view - the view shed for the current activity
	 */  
	public void onClick(View view) 
	{
		Log.d(getLocalClassName(), "Step 5");
        switch (view.getId())
        {
            case R.id.one_shot_scan_button:
                onOneShotScanClick();
                break;
            case R.id.start_scan_button:
                onStartScanningClick();
                break;
            default:
                onStartScanningClick();
                break;
        }//switch
	}//onClick

    public void registerWifiScanReceiver()
    {
        registerReceiver(mBroadcastReceiver = new BroadcastReceiver()
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
        mReceiverRegistered = true;
    }

    public void unregisterWifiScanReceiver()
    {
        unregisterReceiver(mBroadcastReceiver);
        mReceiverRegistered = false;
    }//unregisterWifiScanReceiver

    /**
     * Called when the Start/Stop scan button is pressed
     *
     * This method clears out any previous scan results and schedules a new wifi scan timer task
     * If the scan is already running, then this method halts the timer task and resets the scan counter
     * If the one shot scan button is clicked, scan results are cleared out and a single scan is performed
     */
    public void onStartScanningClick()
    {
        if(mStartStopScanButton.getText().toString().equals(getResources().getString(R.string.perform_scan)))
        {
            if(mNumberOfScansCounter == 0)
            {
                //Set time to normalize scan results to if this is the first scan
                mMillisecondsSinceBoot = SystemClock.uptimeMillis();
            }//if
            mScanResultsArrayList.clear();
            mWifiScanResultAdapter.disableItemsSelection();
            Log.d(getLocalClassName(), "Step 5.1");
            //startTime = SystemClock.uptimeMillis();
            mStartStopScanButton.setText(R.string.stop_scan);
            mScanRunning = true;
            if(mReceiverRegistered == false) {
                registerWifiScanReceiver();
            }//if
            scheduleWifiScanTimerTask(false);
        }//if
        else
        {
            mWifiScanResultAdapter.enableItemsSelection();
            Log.d(getLocalClassName(), "Step 5.2");
            //Halt the wifi scan timer task
            mTimerTask.cancel();
            mScanRunning = false;
            //Set the Scan button text back to performing the scan
            mStartStopScanButton.setText(R.string.perform_scan);
            mResetScanResultsDialog.show();
            if(mReceiverRegistered) {
                unregisterWifiScanReceiver();
            }//if
        }//else
    }//onStartScanningClick

    public void onOneShotScanClick()
    {
        Log.d(getLocalClassName(), "Step 5(one shot)");

        if(mScanRunning == true)
        {
            mWifiScanResultAdapter.enableItemsSelection();
            Log.d(getLocalClassName(), "Step 5.2");
            //Halt the wifi scan timer task
            mTimerTask.cancel();
            mScanRunning = false;
            //Set the Scan button text back to performing the scan
            mStartStopScanButton.setText(R.string.perform_scan);
            mResetScanResultsDialog.show();
        }//if
        mMillisecondsSinceBoot = SystemClock.uptimeMillis();
        mWifiScanResultAdapter.disableItemsSelection();
        Log.d(getLocalClassName(), "Step 5.1");
        mScanRunning = true;
        if(mReceiverRegistered == false)
        {
            registerWifiScanReceiver();
        }//if
        scheduleWifiScanTimerTask(true);
        unregisterWifiScanReceiver();
        mScanRunning = false;
    }//onClick

	/** 
	 * Called when user requests after a stop scan
	 * 
	 * Resets all necessary parameters involved with scan management
	 */  
	public void resetScanResults()
	{
		//Reset the scan counter
		mNumberOfScansCounter = 0;
		//Reset the time of reference clock
		//Clear out the associated scan result arrays
		mScanResultsArrayList.clear();                                
		mScanResultHash.clear();
		mWifiManagerScanResults.clear();
		mScanResultsHashMap.clear();
		
		mWifiScanResultAdapter.notifyDataSetChanged();
	}//resetScanResults
	
	/** 
	 * Called to schedule a wifi scan timer task
	 * 
	 * This method initializes the wifi scan timer task and calls the performScan method.
	 */  
	public void scheduleWifiScanTimerTask(boolean oneShot)
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
        if(oneShot == true)
        {
            mScanTimer.schedule(mTimerTask, 500);
            try {
                synchronized (this) {
                    wait(1002);
                }
            } catch (InterruptedException e) {
                Log.d(getLocalClassName(), "Waiting didnt work!!");
                e.printStackTrace();
            }
        }//if
        else
        {
            mScanTimer.schedule(mTimerTask, 500, 5000);
        }//else

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
				//Set the RSS (signal strength level) and timestamp normalized to program start time
				scan_result_item.setRSS(mWifiManagerScanResults.get(mWifiManagerScanResultsCount).level, SystemClock.uptimeMillis()-mMillisecondsSinceBoot);
				//Set the BSSID (Mac Address)
				scan_result_item.setMac_address(mWifiManagerScanResults.get(mWifiManagerScanResultsCount).BSSID);
				//Set the frequency
				scan_result_item.setFrequency(mWifiManagerScanResults.get(mWifiManagerScanResultsCount).frequency);                
				//Log the scan result fields
				Log.d(getLocalClassName(), "STEP 5B: " + scan_result_item.printMe());
				//Add the scan result item to the results Array List only if matches SSID filter
                if(this.mSSIDFilterEnabled == true) {
                    Log.d(getLocalClassName(), "SSID: " + scan_result_item.getSSID());
                    Log.d(getLocalClassName(), "SSID Filter: " + this.mSSIDFilter + "*");
                    if (scan_result_item.getSSID().matches(this.mSSIDFilter + "(.*)")) {
                        mScanResultsHashMap.addItem(scan_result_item);
                    }//if
                }//if
                else
                {
                    mScanResultsHashMap.addItem(scan_result_item);
                }//else
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
	public void onItemClick(int itemPosition)
	{
		Log.d(getLocalClassName(), "Step 10: OnItemClick");
		if(mScanRunning == false && mScanResultsArrayList.isEmpty() == false)
		{
			WiFiScanResult tempValues = ( WiFiScanResult ) mScanResultsArrayList.get(itemPosition);
			Queue<TimestampedRSS> all_rss_values = tempValues.getAllRSS();
			String rss_string = "";
			Iterator<TimestampedRSS> rss_iterator = all_rss_values.iterator();

			while (rss_iterator.hasNext() == true)
			{
				Log.d(getLocalClassName(), "Step 10A: RSS Strings Assembled");
				rss_string = rss_string + (rss_iterator.next().toString()) + ",";        	
			}
			setItemClickAttributes(itemPosition, rss_string);
			// SHOW ALERT 
			// Showing Alert Dialog
			mWriteToFileDialog.show();


			Toast.makeText(this,
					""+tempValues.getSSID()
					+ "\nRSS: " + rss_string
					+"\nFreq: "+tempValues.getFrequency()
					+ "\nChan: "+tempValues.getChannel()
					+ "\nMAC: " + tempValues.getMac_address(),
					Toast.LENGTH_LONG)
					.show();
		}//if
	}//onItemClick

	void setItemClickAttributes(int index, String rssString)
	{
		mCurrentlySelectedItemIndex = index;
		mCurrentlySelectedItemRSSString = rssString;
	}

	@SuppressLint("SdCardPath")
	boolean writeAPDataToCSVFile()
	{
		boolean file_written_successfully = false;
		WiFiScanResult selected_AP_scan_result_data = ( WiFiScanResult ) mScanResultsArrayList.get(mCurrentlySelectedItemIndex);
		if(mWriteToCSVFile == true && Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED_READ_ONLY)        
		{
			try {
				String output_string="SSID,Channel,Frequency,MAC,RSS,Timestamp,(1:N)\n";
				Log.d(getLocalClassName(), "Step 11A: Logging to CSV file");

				//File output_folder = new File(Environment.getExternalStorageDirectory() + File.separator + "SensorModelDataCollector");
                File new_file = getFilesDir();
                boolean folder_success = true;
				if (!new_file.exists()) {
					//folder_success = output_folder.mkdir();
                    folder_success |= new_file.mkdir();
				}

				if(folder_success == true)
				{
					String output_filename = selected_AP_scan_result_data.mSSID+"_"+selected_AP_scan_result_data.getLatestTimestampedRSS().mTimestamp+".csv";

					//File output_file = new File(output_folder, output_filename);
                    File output_file = new File(new_file, output_filename);
					if (output_file.exists() == false)
					{
						output_file.createNewFile();
                        //output_file2.createNewFile();
					}//if - file doesn't exist, create it


					if(output_file.canWrite() == true)
					{
						FileWriter output_file_writer = new FileWriter(output_file);

						output_string += selected_AP_scan_result_data.mSSID + ",";
						output_string += selected_AP_scan_result_data.channel + ",";
						output_string += selected_AP_scan_result_data.frequency_MHz + ",";
						output_string += selected_AP_scan_result_data.getMac_address() + ",";
						Queue<TimestampedRSS> all_rss_values = selected_AP_scan_result_data.getAllRSS();
						String rss_string = "";
						Iterator<TimestampedRSS> rss_iterator = all_rss_values.iterator();

						while (rss_iterator.hasNext() == true)
						{
							Log.d(getLocalClassName(), "Step 10A: RSS Strings Assembled");
							rss_string = rss_string + rss_iterator.next().rssTimestampPair() + ",";        	
						}
						output_string += rss_string + "\n";

						Log.d(getLocalClassName(), "Step 10C: "+output_string);
						output_file_writer.append(output_string);
						output_file_writer.flush();
						output_file_writer.close();
						file_written_successfully = true;
					}
					else
					{
						Log.d(getLocalClassName(), "Step 10C: No file write access permission");
					}
				}
			}
			catch (IOException ioe) 
			{ioe.printStackTrace();}
		}    

		else
		{
			Log.d(getLocalClassName(), "Step 11B: mWriteToCSVFile set to false or media not mounted for write");
		}
		return file_written_successfully;
	}//writeAPDataToCSVFile

}