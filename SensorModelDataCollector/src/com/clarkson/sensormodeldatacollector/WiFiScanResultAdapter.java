package com.clarkson.sensormodeldatacollector;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

	/********* WiFiScanResultAdapter class extends with BaseAdapter and implements with OnClickListener ************/
	public class WiFiScanResultAdapter extends BaseAdapter implements OnClickListener {
	          
	         /*********** Declare Used Variables *********/
	         private Activity activity;
	         private ArrayList<WiFiScanResult> data;
	         private static LayoutInflater inflater=null;
	         public Resources res;
	         WiFiScanResult tempValues=null;
	         int i=0;
	          public boolean mAreItemsSelectable = false;
	         /*************  CustomAdapter Constructor *****************/
	         public WiFiScanResultAdapter(Activity a, ArrayList<WiFiScanResult> d,Resources resLocal) {
	              
	                /********** Take passed values **********/
	                 activity = a;
	                 data=d;
	                 res = resLocal;
	              
	                 /***********  Layout inflator to call external xml layout () ***********/
	                  inflater = ( LayoutInflater )activity.
	                                              getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	              
	         }
	      
	         public void enableItemsSelection()
	         {
	        	 mAreItemsSelectable = true;
	         }
	         
	         public void disableItemsSelection()
	         {
	        	 mAreItemsSelectable = false;
	         }
	         
	         /******** What is the size of Passed Arraylist Size ************/
	         public int getCount() {
	              
	             if(data.size()<=0)
	                 return 1;
	             return data.size();
	         }
	      
	         public Object getItem(int position) {
	             return position;
	         }
	      
	         public long getItemId(int position) {
	             return position;
	         }
	          
	         
	         @Override
	         public boolean areAllItemsEnabled()
	                         {

	         return mAreItemsSelectable;

	         }
	         
	         /********* Create a holder Class to contain inflated xml file elements *********/
	         public static class ViewHolder{
	              
	             public TextView text_rss;
	             //public TextView text_freq;
	             public TextView text_ssid;
	      
	         }
	      
	         /****** Depends upon data size called for each row , Create each ListView row *****/
	         public View getView(int position, View convertView, ViewGroup parent) {
	              
	             View vi = convertView;
	             ViewHolder holder;
	              
	             if(convertView==null){
	                  
	                 /****** Inflate tabitem.xml file for each row ( Defined below ) *******/
	                 vi = inflater.inflate(R.layout.tabitem, null);
	                  
	                 /****** View Holder Object to contain tabitem.xml file elements ******/
	 
	                 holder = new ViewHolder();
	                 holder.text_ssid = (TextView) vi.findViewById(R.id.text_ssid);
	                 holder.text_rss=(TextView)vi.findViewById(R.id.text_rss);
	                  
	                /************  Set holder with LayoutInflater ************/
	                 vi.setTag( holder );
	             }
	             else 
	                 holder=(ViewHolder)vi.getTag();
	              
	             if(data.size()<=0)
	             {
	                 holder.text_ssid.setText("No Data");
	                  
	             }
	             else
	             {
	                 /***** Get each Model object from Arraylist ********/
	                 tempValues=null;
	                 tempValues = ( WiFiScanResult ) data.get( position );
	                  
	                 /************  Set Model values in Holder elements ***********/
	 
	                  holder.text_ssid.setText( "SSID: " + tempValues.getSSID());
	                  holder.text_rss.setText( "RSS: " +tempValues.getLatestRSS() + " dBm" );
	                   
	                  /******** Set Item Click Listener for LayoutInflater for each row *******/
	 
	                  vi.setOnClickListener(new OnItemClickListener( position ));
	             }
	             return vi;
	         }
	          
	         @Override
	         public void onClick(View v) {
	                 Log.v("CustomAdapter", "=====Row button clicked=====");
	         }
	          
	         /********* Called when Item click in ListView ************/
	         private class OnItemClickListener  implements OnClickListener{           
	             private int mPosition;
	              
	             OnItemClickListener(int position){
	                  mPosition = position;
	             }
	              
	             @Override
	             public void onClick(View arg0) {
	 
	        
	               WiFiScanActivity sct = (WiFiScanActivity)activity;
	 
	              /****  Call  onItemClick Method inside CustomListViewAndroidExample Class ( See Below )****/
	 
	                 sct.onItemClick(mPosition);
	             }               
	         }   
	     }
