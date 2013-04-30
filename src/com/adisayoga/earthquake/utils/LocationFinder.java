package com.adisayoga.earthquake.utils;

import java.util.List;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/**
 * This class let's yout find the "best" (most accurate and timely) previously
 * detected location using whatever providers are available.
 * <p>
 * Where a timely/accurate provious location is not detected it will
 * return the newest location (where one exists) and setup a one-off location
 * update to find the current location.
 * <p>
 * Based on: <a href="http://android-developers.blogspot.com/2011/06/deep-dive-into-location.html">
 * A Deep Dive Into Location</a>
 */
public class LocationFinder {
	
	private static final String TAG = "LocationFinder";
	
	/** The default search radius when searching for places enarby. */
	public static final int DEFAULT_RADIUS = 150;
	
	/** The maximum distance the user should travel between location updates. */
	public static final int MAX_DISTANCE = DEFAULT_RADIUS / 2;
	
	/** The maximum time that should pass before the user gets a location update */
	//public static final long MAX_TIME = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
	
	private LocationListener locationListener;
	private final LocationManager locationManager;
	private final Criteria criteria;
	private final Context context;
	
	public LocationFinder(Context context) {
		this.context = context;
		locationManager = (LocationManager) context.getSystemService(
				Context.LOCATION_SERVICE);
		criteria = new Criteria();
		
		/* 
		 * Coarse accuracy is specified here to get the fastest possible result.
		 * The calling activity will likely (or have already) request ongoing
		 * updates using the Fine location provider.
		 */
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
	}

	/**
	 * Returns the most accourate and timely previously detected location.
	 * Where the last result is beyond the specified maximum distance or
	 * latency a one-off location update is returned via the {@link LocationListener}
	 * specified in {@link setChangedLocationListener}.
	 * 
	 * @param minDistance Minimum distance before we require a location update
	 * @param minTime Minimum time require between location updates
	 * @return The most accurate and/or timely previously detected location
	 */
	public Location getLastLocation(int minDistance, long minTime) {
		Location bestResult = null;
		float bestAccuracy = Float.MAX_VALUE;
		long bestTime = Long.MAX_VALUE;
		
		/*
		 * Iterate through all the providers on the system, keeping note
		 * of the most accurate result within the acceptable time limit.
		 * If no result is found within maxTime, return the newest Location.
		 */
		List<String> matchingProviders = locationManager.getAllProviders();
		for (String provider : matchingProviders) {
			Location location = locationManager.getLastKnownLocation(provider);
			if (location == null) continue;
			
			float accuracy = location.getAccuracy();
			long time = location.getTime();
			
			if (time < minTime && accuracy < bestAccuracy) {
				bestResult = location;
				bestAccuracy = accuracy;
				bestTime = time;
			} else if (bestAccuracy == Float.MAX_VALUE 
					&& time < minTime && time < bestTime) {
				bestResult = location;
				bestTime = time;
			}
		}
		
		/*
		 * If the best result is beyond the allowed time limit, or the 
		 * accuracy of the best result is wider then the acceptable maximum
		 * distance, request a single update.
		 * This check simply implements the same conditions we set when
		 * requesting regular location update every [minTime] and [minDistance].
		 */
		if (locationListener != null 
				&& (bestTime > minTime || bestAccuracy > minDistance)) {
			
			String provider = locationManager.getBestProvider(criteria, true);
			if (provider != null) {
				locationManager.requestLocationUpdates(provider, 0, 0, 
						singleUpdateListener, context.getMainLooper());
			}
		}
		
		return bestResult;
	}
	
	/**
	 * Set the {@link LocationListener} that may receiver a one-shot current
	 * location update.
	 * @param locationListener LocationListener
	 */
	public void setChangedLocationListener(LocationListener locationListener) {
		this.locationListener = locationListener;
	}
	
	/**
	 * Cancel the one-shot current location update.
	 */
	public void cancel() {
		locationManager.removeUpdates(singleUpdateListener);
	}
	
	/**
	 * This one-off {@link LocationListener} simply listens for a single
	 * update before unregistering itself.
	 * <p>
	 * The one-off location update is returned via the {@link LocationListener}
	 * specified in {@link setChangedLocationListener}.
	 */
	private final LocationListener singleUpdateListener = new LocationListener() {
		
		@Override
		public void onLocationChanged(Location location) {
			Log.d(TAG, "Single location update received: " + location.getLatitude() 
					+ ", " + location.getLongitude());
			
			if (locationListener != null && location != null) {
				locationListener.onLocationChanged(location);
			}
			locationManager.removeUpdates(singleUpdateListener);
		}
		
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
		
		@Override
		public void onProviderEnabled(String provider) {
		}
		
		@Override
		public void onProviderDisabled(String provider) {
		}
	};

}
