package com.adisayoga.earthquake.utils;

import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

/**
 * Kerangka class dasar untuk LocationListener.
 * 
 * @author Adi Sayoga
 */
public abstract class BaseLocationListener implements LocationListener {

	private static final String TAG = "BaseLocationListener";
	
	@Override
	public void onProviderDisabled(String provider) {
		Log.d(TAG, "onProviderDisabled, provider=" + provider);
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d(TAG, "onProviderEnabled, provider=" + provider);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d(TAG, "onProviderEnabled, provider=" + provider + ", status=" + status);
	}

}
