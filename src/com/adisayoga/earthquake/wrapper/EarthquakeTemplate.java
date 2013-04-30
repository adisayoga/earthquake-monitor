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

package com.adisayoga.earthquake.wrapper;

import java.util.List;

import android.content.Context;
import android.location.Location;

import com.adisayoga.earthquake.R;
import com.adisayoga.earthquake.dto.EarthquakeDTO;
import com.adisayoga.earthquake.dto.Unit;
import com.adisayoga.earthquake.utils.LocationUtils;
import com.adisayoga.earthquake.utils.TimeUtils;

/**
 * Utility template pesan.
 * TODO Tambahkan geocoder, darimana pesan ini.
 * 
 * @author Adi Sayoga
 */
public class EarthquakeTemplate {
	
	private static Prefs prefs;
	private static EarthquakeTemplate instance = null;
	private static Context context;
	
	private EarthquakeTemplate() {	
	}
	
	public static EarthquakeTemplate getInstance(Context context) {
		EarthquakeTemplate.context = context;
		
		if (instance == null) {
			instance = new EarthquakeTemplate();
			prefs = Prefs.getInstance(context);
		}
		return instance;
	}


	/**
	 * Mendapatkan teks polos dengan banyak detail.
	 * 
	 * @param template Template utama
	 * @param templateDetails Detail template
	 * @param quakes Daftar gempa
	 * @param location Lokasi saat ini
	 * @return Pesan
	 */
	public String getMessage(String template, String templateDetails, 
			List<EarthquakeDTO> quakes, Location location) {
		StringBuilder details = new StringBuilder();
		
		// detail pesan
		for (EarthquakeDTO quake : quakes) {
			int distance = -1;
			if (location != null) distance = (int) location.distanceTo(quake.getLocation());
			details.append(getDetailMessage(templateDetails, quake, distance)).append("\n");
		}
		
		String message = template.replace(Prefs.TPL_DETAILS, details.toString());
		return message;
	}
	
	/**
	 * Mendapatkan teks polos.
	 * 
	 * @param template Template utama
	 * @param templateDetails Detail template
	 * @param quake Data gempa
	 * @param location Lokasi saat ini
	 * @return Pesan
	 */
	public String getMessage(String template, String templateDetails, 
			EarthquakeDTO quake, Location location) {
		float distance = -1;
		if (location != null) distance = location.distanceTo(quake.getLocation());
		String details = getDetailMessage(templateDetails, quake, distance);
		String message = template.replace(Prefs.TPL_DETAILS, details);
		return message;
	}
	
	/**
	 * Mendapatkan detail pesan.
	 * 
	 * @param templateDetails Detail template
	 * @param quake Data gempa
	 * @param distance Jarak gempa dengan lokasi user
	 * @return Detail pesan
	 */
	public String getDetailMessage(String templateDetails, EarthquakeDTO quake, 
			float distance) {
		String dateString = TimeUtils.getInstance(context).toHumanReadable(quake.time);
		String magnitudeString = Float.toString(quake.magnitude);
		String region = quake.region;
		String locationString = LocationUtils.getInstance(context).formatLocation(
				quake.latitude, quake.longitude, false, false);
		
		Unit unit = prefs.getUnit();
		String depthString = unit.formatNumber(quake.depth, EarthquakeDTO
    			.FRACTION_DEPTH);
		
		String distanceString = (distance != -1) 
				? unit.formatNumber(distance, EarthquakeDTO
		    			.FRACTION_DEPTH)
				: (String) context.getString(R.string.unknown_location);
		
		templateDetails = templateDetails.replace(Prefs.TPL_DATE, dateString);
		templateDetails = templateDetails.replace(Prefs.TPL_MAGNITUDE, magnitudeString);
		templateDetails = templateDetails.replace(Prefs.TPL_REGION, region);
		templateDetails = templateDetails.replace(Prefs.TPL_LOCATION, locationString);
		templateDetails = templateDetails.replace(Prefs.TPL_DEPTH, depthString);
		templateDetails = templateDetails.replace(Prefs.TPL_DISTANCE, distanceString);
		
		return templateDetails;
	}
	
}
