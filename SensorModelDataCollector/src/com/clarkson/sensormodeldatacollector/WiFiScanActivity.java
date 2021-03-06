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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;    
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;    
import android.content.DialogInterface;
import android.content.Intent;     
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;   
import android.net.wifi.WifiManager;    
import android.os.Bundle;    
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;    
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;    
import android.widget.SimpleAdapter;    
import android.widget.TextView;    
import android.widget.Toast;
import android.database.sqlite.SQLiteDatabase;

//Custom data base entries
import com.clarkson.sensormodeldatacollector.data.AccelerometerDataReaderContract;
import com.clarkson.sensormodeldatacollector.data.AccelerometerDataReaderDbHelper;
import com.clarkson.sensormodeldatacollector.data.AccelerometerDataReaderContract.AccelerometerDataEntry;

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
	SensorManager mSensorManager;
	Sensor mMagnetometerSensor;
	Bundle mMainMenuExtras;

    private SketchView sketchView = null;
    private Orientation magnetometerAnalyzer = null;
    private ProgramState programState = null;
    private MenuManager menuManager = null;
    private Storage storage = null;
    private Movement movement = null;

    //AccelerometerReceiver myReceiver=null;
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

    ArrayList<String> mAccelerometerReadings = new ArrayList<String>();

	public int mCurrentlySelectedItemIndex = -1;
	public String mCurrentlySelectedItemRSSString = "";
    //Intent mAccelerometerIntent;
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

    //Database for Accelerometer Data
    //AccelerometerDataReaderDbHelper mDbHelper;

    public WiFiScanActivity()
    {
    }//WifiScanActivity

    protected void onPause()
    {
        super.onPause();
        //mSensorManager.unregisterListener(this);
        //stopService(mAccelerometerIntent);
        //if (myReceiver != null)unregisterReceiver(myReceiver);
    }//onPause

    protected void onResume()
    {
        super.onResume();
        //mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        //Make sensor delay 1 second
        //int sensor_delay_microseconds = 1000 * 1000;
        //mSensorManager.registerListener(this, mAccelerometer, sensor_delay_microseconds);
        //myReceiver = new AccelerometerReceiver();
        //myReceiver.setMillisecondsSinceBoot(mMillisecondsSinceBoot);
        //IntentFilter intentFilter = new IntentFilter();
        //intentFilter.addAction(AccelerometerService.MY_ACTION);
        //startService(mAccelerometerIntent);
        //registerReceiver(myReceiver, intentFilter);
    }//onResume

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
		}//ServerStatus
    //Server connection status enum
	public ServerStatus mServerConnectionCode;

    public Bitmap getBitmapResource(int index)
    {
        try {
            final int addressOffset = 0x7f020000;
            Bitmap bit = BitmapFactory.decodeResource(this.getResources(), addressOffset + index);
            return bit;
        } catch (Exception e) {
            return null;
        }
    }

    private void setupSketchView() {
        // creating view
        if (sketchView == null)
            sketchView = new SketchView(getApplicationContext());
        // setting view fields
        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point displaySize = new Point();

        // Our application only supports drawing to portrait canvas orientation
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            sketchView.setBeginDrawing(true);
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // get display size for aspect ratio
        try {
            display.getSize(displaySize);
        } catch (java.lang.NoSuchMethodError ex) { // Older device
            displaySize.x = display.getWidth();
            displaySize.y = display.getHeight();

            // account for orientation change
            int min = Math.min(displaySize.x, displaySize.y);
            int max = Math.max(displaySize.x, displaySize.y);
            displaySize = new Point(min, max);
            displaySize.y += displaySize.x * 0.16; // fix for status bar height in older API
        }

        float aspectRatio = (float)displaySize.y / (float)displaySize.x;

        sketchView.setAspectRatio(aspectRatio);
        sketchView.setBitmapArrow(getBitmapResource(0));

        //setContentView(sketchView);
    }

    private void beginCalibrationStateMonitoring() {

        new Thread() {
            public void run() {
                while (true) {
                    try {
                        programState.checkCalibrationStateChange();
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }.start();

    }

    private void beginMovementStateMonitoring() {

        new Thread() {
            public void run() {
                while (true) {
                    try {
                        programState.checkMovementState();
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }.start();

    }

    private void beginExportPathBitmapMonitoring() {

        new Thread() {
            public void run() {
                while (true) {
                    try {
                        programState.checkExportBitmapState();
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }.start();

    }

    private void setupUiInteraction() {

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        sketchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    float[] coords = {event.getX(), event.getY()};

                    if (menuManager.getIsMenuVisible())
                        menuManager.menuProcessEvents(coords);
                    else {
                        if (programState.getProgramState() != ProgramState.State.STATE_CALIBRATION &&
                                programState.getProgramState() != ProgramState.State.STATE_EXPORT_MOVEMENT_PATH
                                )
                            programState.setProgramState(ProgramState.State.STATE_PAUSE_WITH_MENU);
                    }

                    sketchView.invalidate();
                }
                return true;
            }
        });

    }

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
        //mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        //mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		mStartStopScanButton = (Button) findViewById(R.id.start_scan_button);
		mStartStopScanButton.setOnClickListener(this);
		
		mScanResultsListView = (ListView)findViewById(R.id.AP_list);

		mMainMenuExtras = getIntent().getExtras();
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		mMagnetometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		if(mMagnetometerSensor == null)
		{
			Toast.makeText(getApplicationContext(), "Magnetometer unavailable", Toast.LENGTH_LONG).show();
		}//if
        setupSketchView();
        magnetometerAnalyzer = new Orientation(sketchView);
        mSensorManager.registerListener(magnetometerAnalyzer, mMagnetometerSensor, SensorManager.SENSOR_DELAY_FASTEST);

        storage = new Storage(magnetometerAnalyzer, this);
        storage.loadCalibrationData();

        movement = new Movement(magnetometerAnalyzer);
        sketchView.setMovementObject(movement);

        programState = new ProgramState(sketchView, magnetometerAnalyzer, storage, movement);

        menuManager = new MenuManager(programState);
        menuManager.setBitmapMenuArrow(getBitmapResource(2));
        menuManager.setBitmapMenuButton(getBitmapResource(3));


        sketchView.setMenuManager(menuManager);

        programState.setMenuManager(menuManager);
        programState.setProgramState(ProgramState.State.STATE_PAUSE_WITH_MENU);

        setupUiInteraction();

        sketchView.invalidate();

        beginCalibrationStateMonitoring();

        beginMovementStateMonitoring();

        beginExportPathBitmapMonitoring();

		if(mMainMenuExtras != null)
		{
			mConnectToServer = mMainMenuExtras.getBoolean("mConnectToServer");
            int wifi_scan_interval_seconds = Integer.parseInt(mMainMenuExtras.getString("mWifiScanIntervalSeconds"));
			mWifiScanIntervalMilliseconds = 1000*wifi_scan_interval_seconds;
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
        mMillisecondsSinceBoot = SystemClock.uptimeMillis();
        //Start service
        //mAccelerometerIntent = new Intent(this, com.clarkson.sensormodeldatacollector.AccelerometerService.class);
        //mDbHelper = new AccelerometerDataReaderDbHelper(this);
        //Log.d( getLocalClassName(), "onCreate/startService" );

        buildAlertDialogs();
		
		checkServerConnection();
	}//onCreate

    /*
    Checks the server connection
     */
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
		Log.d(getLocalClassName(), "OnStop");
		//unregisterReceiver(mBroadcastReceiver);
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
			default_wifi_scan_result.setRSS(i, mMillisecondsSinceBoot, getCurrentTimeString());
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
            case R.id.start_scan_button:
                onStartScanningClick();
                break;
            default:
                onStartScanningClick();
                break;
        }//switch
	}//onClick

    /*
    Registers the wifi scan broadcast receiver
     */
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
    }//registerWifiScanReceiver

    /*
    Unregisters the wifi scan broadcast receiver
     */
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
                //mMillisecondsSinceBoot = SystemClock.uptimeMillis();
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
            scheduleWifiScanTimerTask();
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

        mScanTimer.schedule(mTimerTask, 500, mWifiScanIntervalMilliseconds);
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
		Log.d(getLocalClassName(), "STEP 5A: scan #" + mNumberOfScansCounter);
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
				scan_result_item.setRSS(mWifiManagerScanResults.get(mWifiManagerScanResultsCount).level, SystemClock.uptimeMillis()-mMillisecondsSinceBoot, getCurrentTimeString());
				//Set the BSSID (Mac Address)
				scan_result_item.setMac_address(mWifiManagerScanResults.get(mWifiManagerScanResultsCount).BSSID);
				//Set the frequency
				scan_result_item.setFrequency(mWifiManagerScanResults.get(mWifiManagerScanResultsCount).frequency);
				//Log the scan result fields
				//Log.d(getLocalClassName(), "STEP 5B: " + scan_result_item.printMe());
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

    /*
    Sets member variables that hold the currently selected items attributes
     */
	void setItemClickAttributes(int index, String rssString)
	{
		mCurrentlySelectedItemIndex = index;
		mCurrentlySelectedItemRSSString = rssString;
	}

    /*
    Writes access point collected data to CSV files
     */
	@SuppressLint("SdCardPath")
	boolean writeAPDataToCSVFile()
	{
		boolean file_written_successfully = false;
		WiFiScanResult selected_AP_scan_result_data = ( WiFiScanResult ) mScanResultsArrayList.get(mCurrentlySelectedItemIndex);
		if(mWriteToCSVFile == true && Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED_READ_ONLY)        
		{
			try
            {
				String column_header_string="SSID,Channel,Frequency,MAC,RSS,Timestamp,Local Time\n";
				Log.d(getLocalClassName(), "Step 11A: Logging to CSV file");

				//File output_folder = new File(Environment.getExternalStorageDirectory() + File.separator + "SensorModelDataCollector");
                //File new_file = new File(getFilesDir() + File.separator + "SensorModelDataCollector");
                File new_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "SensorModelDataCollector");
                boolean folder_success = true;
				if (!new_file.exists())
                {
					//folder_success = output_folder.mkdir();
                    folder_success |= new_file.mkdir();
				}//if

				if(folder_success == true)
				{

					String output_filename = selected_AP_scan_result_data.mSSID+"_"+selected_AP_scan_result_data.getLatestTimestampedRSS().mTimestamp+"_"+ getCurrentDateString() + ".csv";

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
                        output_file_writer.append(column_header_string);
						String data_row_output_string = selected_AP_scan_result_data.mSSID + ",";
                        data_row_output_string += selected_AP_scan_result_data.channel + ",";
                        data_row_output_string += selected_AP_scan_result_data.frequency_MHz + ",";
                        data_row_output_string += selected_AP_scan_result_data.getMac_address() + ",";
						Queue<TimestampedRSS> all_rss_values = selected_AP_scan_result_data.getAllRSS();
						String rss_string = "";
						Iterator<TimestampedRSS> rss_iterator = all_rss_values.iterator();

						while (rss_iterator.hasNext() == true)
						{
							Log.d(getLocalClassName(), "Step 10A: RSS Strings Assembled");
							rss_string = rss_iterator.next().rssTimestampPair();
							output_file_writer.append(data_row_output_string + rss_string + "\n");
						}//while
						output_file_writer.flush();
						output_file_writer.close();
						file_written_successfully = true;
					}//if
					else
					{
						Log.d(getLocalClassName(), "Step 10C: No file write access permission");
					}//else
				}//if
			}//try
			catch (IOException ioe) 
			{ioe.printStackTrace();}
		}//if
		else
		{
			Log.d(getLocalClassName(), "Step 11B: mWriteToCSVFile set to false or media not mounted for write");
		}//else
        //writeAccelerometerDataToFile();
		return file_written_successfully;
	}//writeAPDataToCSVFile


    private static String getCurrentDateString()
    {
        Date current_date = new Date();
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");

        String current_date_string = sdf1.format(current_date) + "_" + getCurrentTimeString();
        return current_date_string;
    }//getCurrentDateString

    private static String getCurrentTimeString()
    {
        Date current_date = new Date();
        SimpleDateFormat time_format = new SimpleDateFormat("HHmmss");
        return time_format.format(current_date);
    }//getCurrentTimeString

    /*
    Writes accelerometer data to file
     */
   /* public void writeAccelerometerDataToFile()
    {
        if(mAccelerometerReadings.isEmpty() == false) {
            try {
                String output_string = "X,Y,Z,Timestamp,(1:N)\n";

                //File output_folder = new File(Environment.getExternalStorageDirectory() + File.separator + "SensorModelDataCollector");
                //File new_file = new File(getFilesDir() + File.separator + "SensorModelDataCollector");
                File new_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "SensorModelDataCollector");
                boolean folder_success = true;
                if (!new_file.exists()) {
                    //folder_success = output_folder.mkdir();
                    folder_success |= new_file.mkdir();
                }

                if (folder_success == true) {
                    String output_filename = "AccelerometerReadings" + "_" + (SystemClock.uptimeMillis() - mMillisecondsSinceBoot) + ".csv";

                    //File output_file = new File(output_folder, output_filename);
                    File output_file = new File(new_file, output_filename);
                    if (output_file.exists() == false) {
                        output_file.createNewFile();
                        //output_file2.createNewFile();
                    }//if - file doesn't exist, create it


                    if (output_file.canWrite() == true)
                    {
                        FileWriter output_file_writer = new FileWriter(output_file);
                        output_file_writer.append(output_string);
                        //Open up the data base and read off the entries
                        SQLiteDatabase db = mDbHelper.getReadableDatabase();

                        // Define a projection that specifies which columns from the database
                        // you will actually use after this query.
                        String[] projection = {
                                AccelerometerDataEntry._ID,
                                AccelerometerDataEntry.COLUMN_NAME_TIMESTAMP,
                                AccelerometerDataEntry.COLUMN_NAME_X_VALUE,
                                AccelerometerDataEntry.COLUMN_NAME_Y_VALUE,
                                AccelerometerDataEntry.COLUMN_NAME_Z_VALUE
                        };

                        // How you want the results sorted in the resulting Cursor
                        String sortOrder =
                                AccelerometerDataEntry.COLUMN_NAME_TIMESTAMP + " DESC";

                        Cursor query_results = db.query(
                                AccelerometerDataEntry.TABLE_NAME,  // The table to query
                                projection,                               // The columns to return
                                "*",                                // The columns for the WHERE clause
                                null,                            // The values for the WHERE clause
                                null,                                     // don't group the rows
                                null,                                     // don't filter by row groups
                                sortOrder                                 // The sort order
                        );
                        query_results.moveToFirst();
                        if(query_results.getCount() > 0)
                        {
                            for (int i = 0; i < query_results.getCount(); ++i) {
                                long entry_id = query_results.getLong(query_results.getColumnIndexOrThrow(AccelerometerDataEntry._ID));
                                String timestamp = query_results.getString(query_results.getColumnIndexOrThrow(AccelerometerDataEntry.COLUMN_NAME_TIMESTAMP));
                                String x_value = query_results.getString(query_results.getColumnIndexOrThrow(AccelerometerDataEntry.COLUMN_NAME_X_VALUE));
                                String y_value = query_results.getString(query_results.getColumnIndexOrThrow(AccelerometerDataEntry.COLUMN_NAME_Y_VALUE));
                                String z_value = query_results.getString(query_results.getColumnIndexOrThrow(AccelerometerDataEntry.COLUMN_NAME_Z_VALUE));
                                String accelerometerReading = entry_id + "," + timestamp + "," + x_value + "," + y_value + "," + z_value + "\n";
                                output_file_writer.append(accelerometerReading);
                            }//for
                            output_file_writer.flush();
                            output_file_writer.close();
                            //Delete everything from the database
                            db.delete(AccelerometerDataEntry.TABLE_NAME, "*", null);
                        }//if
                        db.close();
                    }//if
                }//if
            }//try
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }//if
    }*/

}