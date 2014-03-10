package com.clarkson.sensormodeldatacollector;

public class TimestampedRSS implements Comparable<Object> 
{
	 public int mReceivedSignalStrength_dBm;
    public long mTimestamp;
   
    public TimestampedRSS()
    {
    	mReceivedSignalStrength_dBm = 0;
    	mTimestamp = 0;
    }
	public TimestampedRSS(int mReceivedSignalStrength_dBm, long mTimestamp) {
		super();
		this.mTimestamp = mTimestamp;
		this.mReceivedSignalStrength_dBm = mReceivedSignalStrength_dBm;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + mReceivedSignalStrength_dBm;
		result = prime * result + (int) (mTimestamp ^ (mTimestamp >>> 32));
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
		TimestampedRSS other = (TimestampedRSS) obj;
		if (mTimestamp != other.mTimestamp)
			return false;
		return true;
	}
	public long getTimestamp() {
		return mTimestamp;
	}
	public void setTimestamp(long mTimestamp) {
		this.mTimestamp = mTimestamp;
	}
	public int getReceivedSignalStrength() {
		return mReceivedSignalStrength_dBm;
	}
	public void setReceivedSignalStrength(int mReceivedSignalStrength) {
		this.mReceivedSignalStrength_dBm = mReceivedSignalStrength;
	}
	
	@Override
	public int compareTo(Object anotherTimestampedRSS) {
		    if (!(anotherTimestampedRSS instanceof TimestampedRSS))
		      throw new ClassCastException("A TimestampedRSS object expected.");
		    long another_timestamped_rss = ((TimestampedRSS) anotherTimestampedRSS).getTimestamp();  
		    return (int)(another_timestamped_rss - this.mTimestamp);    
	}
	
	@Override 
	public String toString()
	{
		return String.valueOf(mReceivedSignalStrength_dBm);
	}	
}
