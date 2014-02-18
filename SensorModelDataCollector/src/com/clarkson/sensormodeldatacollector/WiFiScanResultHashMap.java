package com.clarkson.sensormodeldatacollector;

import java.util.Collection;
import java.util.HashMap;

public class WiFiScanResultHashMap 
{
	HashMap<String, WiFiScanResult> mWifiScanResultHashMap = new HashMap<String, WiFiScanResult>();
	public WiFiScanResultHashMap()
	{
		
	}
	public WiFiScanResultHashMap(
			HashMap<String, WiFiScanResult> mWifiScanResultHashMap) {
		super();
		this.mWifiScanResultHashMap = mWifiScanResultHashMap;
	}

	public void addItem(WiFiScanResult latestScanResult)
	{		
		String ssid_key = latestScanResult.getSSID();
		if(mWifiScanResultHashMap.containsKey(ssid_key))
		{
			WiFiScanResult scan_result = mWifiScanResultHashMap.get(ssid_key);
			scan_result.setRSS(latestScanResult.getLatestTimestampedRSS());
			mWifiScanResultHashMap.put(ssid_key, scan_result);
		}//if
		else
		{
			mWifiScanResultHashMap.put(ssid_key, latestScanResult);
		}
	}//addItem
	
	public void clear()
	{
		mWifiScanResultHashMap.clear();
	}//clear
	
	public int size()
	{
		return mWifiScanResultHashMap.size();
	}
	
	public Collection<WiFiScanResult> values()
	{
		return mWifiScanResultHashMap.values();
	}

}//WiFiScanResultHashMap
