package com.clarkson.sensormodeldatacollector;

import java.util.LinkedList;
import java.util.Queue;

public class WiFiScanResult implements Comparable<Object>{
	    String mSSID;
	    Queue<TimestampedRSS> mTimestampedRSSValues = new LinkedList<TimestampedRSS>();
	    int mLastAddedTimestamp;
	    //int mReceivedSignalStrength_dBm;
	    int frequency_MHz;
	    String channel;
	    String mac_address;
	    
	    public WiFiScanResult()
	    {
	    }

	    @Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((mSSID == null) ? 0 : mSSID.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			WiFiScanResult other = (WiFiScanResult) obj;
			if (mSSID == null) {
				if (other.mSSID != null)
					return false;
			} else if (!mSSID.equals(other.mSSID))
				return false;
			return true;
		}

		public String getSSID() {
	        return mSSID;
	    }

	    public void setSSID(String name) {
	        this.mSSID = name;
	    }

	    public int getRSS() 
	    {
	    	int returned_RSS = 0;
	    	if(mTimestampedRSSValues.size() > 0)
	    	{
	    		returned_RSS = mTimestampedRSSValues.element().mReceivedSignalStrength_dBm;
	    	}//if
	        return returned_RSS;
	    }//getRSS
	    
	    public TimestampedRSS getLatestTimestampedRSS()
	    {
	    	return mTimestampedRSSValues.element();
	    }
	    
	    public Queue<TimestampedRSS> getAllRSS()
	    {
	    	return mTimestampedRSSValues;
	    }

	    public void setRSS(int rss, long timestamp) {
	    	mTimestampedRSSValues.add(new TimestampedRSS(rss, timestamp));
	        //this.mReceivedSignalStrength_dBm = rss;
	    }
	    
	    public void setRSS(TimestampedRSS newRSSEntry) {
	    	mTimestampedRSSValues.add(newRSSEntry);
	        //this.mReceivedSignalStrength_dBm = rss;
	    }

		public int getFrequency() {
			return frequency_MHz;
		}

		public void setFrequency(int frequency) {
			this.frequency_MHz = frequency;
		}

		public String getChannel() {
			return channel;
		}

		public void setChannel(String channel) {
			this.channel = channel;
		}

		public String getMac_address() {
			return mac_address;
		}

		public void setMac_address(String mac_address) {
			this.mac_address = mac_address;
		}

		public String printMe()
		{
			return "SSID: "+getSSID()+" RSS: "+getRSS()+ "dBM";
		}

		@Override
		public int compareTo(Object anotherScanResult) {
			    if (!(anotherScanResult instanceof WiFiScanResult))
			      throw new ClassCastException("A WiFiScanResult object expected.");
			    int another_scan_result_RSS = ((WiFiScanResult) anotherScanResult).getRSS();  
			    return another_scan_result_RSS - this.getRSS();    
		}
}
