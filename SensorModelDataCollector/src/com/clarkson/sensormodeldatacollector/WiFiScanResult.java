package com.clarkson.sensormodeldatacollector;

import java.util.LinkedList;
import java.util.Queue;

public class WiFiScanResult implements Comparable<Object>{
	    String mSSID;
	    Queue<TimestampedRSS> mTimestampedRSSValues = new LinkedList<TimestampedRSS>();
	    long mLastAddedTimestamp;
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
		}//equals

		public String getSSID() {
	        return mSSID;
	    }//getSSID

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

        public int getLatestRSS()
        {
            int returned_RSS = 0;
            long latest_timestamp = 0;
            if(mTimestampedRSSValues.size() > 0)
            {
                returned_RSS = mTimestampedRSSValues.element().getReceivedSignalStrength();
                latest_timestamp = mTimestampedRSSValues.element().getTimestamp();
                for( TimestampedRSS timestampedRSS : mTimestampedRSSValues)
                {
                    if(latest_timestamp < timestampedRSS.getTimestamp())
                    {
                        returned_RSS = timestampedRSS.getReceivedSignalStrength();
                        latest_timestamp = timestampedRSS.getTimestamp();
                    }//if
                }//for
            }//if

            return returned_RSS;
        }//getLatestRSS
	    
	    public Queue<TimestampedRSS> getAllRSS()
	    {
	    	return mTimestampedRSSValues;
	    }

	    public void setRSS(int rss, long timestamp, String localTime) {
	    	mTimestampedRSSValues.add(new TimestampedRSS(rss, timestamp, localTime));
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
			if (frequency_MHz >= 2412 && frequency_MHz <= 2484) 
		    {
		        channel = String.valueOf((frequency_MHz - 2412) / 5 + 1);
		    } 
		    else if (frequency_MHz >= 5170 && frequency_MHz <= 5825) 
		    {
		        channel = String.valueOf((frequency_MHz - 5170) / 5 + 34);
		    } 
		    else 
		    {
		        channel = null;
		    }
		}

		public String getChannel() 
		{
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
