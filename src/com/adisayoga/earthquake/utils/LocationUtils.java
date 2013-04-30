/*
 * Copyright 2011 Adi Sayoga.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package com.adisayoga.earthquake.utils;

import android.content.Context;
import android.location.Address;

import com.adisayoga.earthquake.R;

/**
 * Utility untuk memformat lokasi dalam bentuk string.
 * 
 * @author Adi Sayoga
 */
public class LocationUtils {
	
	public static final String CONSTRUCT_PROVIDER = "construct_provider";
	
	private static LocationUtils instance = null;
	
	private static String dirNorth;
	private static String dirSouth;
	private static String dirEast;
	private static String dirWest;
	
	private LocationUtils() {
	}
	
	public static LocationUtils getInstance(Context context) {
		if (instance == null) {
			instance = new LocationUtils();
			
			dirNorth = context.getString(R.string.dir_north);
			dirSouth = context.getString(R.string.dir_south);
			dirEast = context.getString(R.string.dir_east);
			dirWest = context.getString(R.string.dir_west);
		}
		return instance;
	}

	/**
	 * Memformat lokasi yang dapat dibaca manusia.
	 * 
	 * @param dms Menggunakan format (date, minute, socond) atau tidak
	 * @param latitude Lokasi latitude
	 * @param longitude Lokasi longitude
	 * @return Format dalam string
	 */
	public String formatLocation(double latitude, double longitude, boolean dms) {
		return formatLocation(latitude, longitude, dms, true);
	}
	
	/**
	 * Memformat lokasi yang dapat dibaca manusia.
	 * 
	 * @param dms Menggunakan format (date, minute, socond) atau tidak
	 * @param latitude Lokasi latitude
	 * @param longitude Lokasi longitude
	 * @param showDegreeSign Memperlihatkan tanda degree atau tidak
	 * @return Format dalam string
	 */
	public String formatLocation(double latitude, double longitude, boolean dms, 
			boolean showDegreeSign) {
		
		if (dms) {
			return dd2dms(latitude, true, showDegreeSign) + ", " 
					+ dd2dms(longitude, false, showDegreeSign);
		} else {
			int latSign = (latitude < 0) ? -1 : 1;
			int lngSign = (longitude < 0) ? -1 : 1;
			String latDir = (latSign == 1) ? dirNorth : dirSouth;
			String lngDir = (lngSign == 1) ? dirEast : dirWest;
			
			String degree = (showDegreeSign) ? "\u00b0" : "";
			return (latitude * latSign) + degree + latDir + ", " + (longitude * lngSign) 
					+ degree + lngDir;
		}
	}
	
	private String dd2dms(double value, boolean isLatitude, boolean showDegreeSign) {
		int sign = (value < 0) ? -1 : 1;
		String dir = (isLatitude) ? ((sign == 1) ? dirNorth : dirSouth) 
				: ((sign == 1) ? dirEast : dirWest);
		
		int degree = Math.abs((int) value);
		double minuteDouble = (Math.abs(value) - degree) * 60;
		int minute = Math.abs((int) minuteDouble);
		double second = Math.round((minuteDouble - minute) * 60 * 1000000) / 1000000;
		String degreeSign = (showDegreeSign) ? "\u00b0 " : "deg ";
		return (degree * sign) + degreeSign + minute + "' " + second + "\"" + dir;
	}

	/**
	 * Mendapatkan nama alamat.
	 * 
	 * @param address Address
	 * @return nama alamat
	 */
	public static String getAddressLine(Address address) {
		if (address == null) return "";
		
		String addressLine = "";
		for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
			if (addressLine != "") addressLine += ", ";
			addressLine += address.getAddressLine(i);
		}
		return addressLine;
	}
	
}
