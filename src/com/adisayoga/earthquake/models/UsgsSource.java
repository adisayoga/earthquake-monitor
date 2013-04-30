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
 
package com.adisayoga.earthquake.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlarmManager;
import android.net.Uri;
import android.util.Log;

import com.adisayoga.earthquake.dto.EarthquakeDTO;
import com.adisayoga.earthquake.utils.TimeUtils;

/**
 * Class untuk komunikasi data dengan USGS.
 * 
 * @author Adi Sayoga
 */
public class UsgsSource {
	
	private static final String TAG = "UsgsSource";

	public static final String FEED = "http://earthquake.usgs.gov/earthquakes/catalogs/";
	public static final String DETAIL = "http://earthquake.usgs.gov/earthquakes/recenteqsww/Quakes/";
	public static final String GLOBE = "http://earthquake.usgs.gov/images/globes/";
	/*public static final String FEED = "http://10.0.2.2/earthquake/";
	public static final String DETAIL = "http://10.0.2.2/earthquake/details/";
	public static final String GLOBE = "http://10.0.2.2/earthquake/globes/";*/
	
	private static final Pattern PATTERN = Pattern.compile( 
	      // src____ eqid___ ver____ datetime________ lat____ lon____ mag_____ 
			"([^,]+),([^,]+),([^,]+),\"([^\"]+) UTC\",([^,]+),([^,]+),([^,]+),"
		  // depth__ nst________ region______
		  + "([^,]+),\\s?([^,]+),\"([^\"]+)\"");

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEEE, MMMM d, yyyy HH:mm:ss", Locale.US);
	
	private UsgsSource() {
	}
	
	/**
	 * Membaca data feed dari usgs.
	 * 
	 * @param lastUpdate Terakhir kali data diambil
	 * @param minMagnitude Magnitudo minimal
	 * @return ArrayList dari data gempa
	 * @throws IOException Jika tidak dapat connect ke server
	 */
	public static List<EarthquakeDTO> read(long lastUpdate, float minMagnitude) 
			throws IOException {
		Log.i(TAG, "Mendapatkan data dari server...");
		
		BufferedReader reader = null;
		List<EarthquakeDTO> quakes = new ArrayList<EarthquakeDTO>();
		long interval = System.currentTimeMillis() - lastUpdate;
		
		try {
			URL url = getUrl(minMagnitude, interval);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(30 * 1000); // 30 detik
			connection.setReadTimeout(30 * 1000);    // 30 detik
			
			if (connection.getResponseCode() != 200) { 
				// Koneksi gagal
				Log.e(TAG, "Koneksi gagal, response code: " + connection
						.getResponseCode());
				return null;
			}
			
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),
					"ISO-8859-1"), 8192);
			String line;
			while ((line = reader.readLine()) != null) {
				try {
					EarthquakeDTO quake = getQuake(line);
					if (quake != null) quakes.add(quake);
				} catch (Exception e) {
					Log.e(TAG, "Error parse data dari baris: " + line, e);
				}
			}
			
			Log.d(TAG, "Selesai mengambil data: " + quakes.size() + " items");
			return quakes;
			
		} catch (IOException e) {
			Log.e(TAG, "Gagal mendapatkan data dari server: " + e.getMessage());
			throw e;
			
		} finally {
			try { if (reader != null) reader.close(); } catch(IOException e) {}
		}
	}

	/**
	 * Mendapatkan url feed USGS berdasarkan minimal magnitudo dan interval refresh.
	 * 
	 * @param minMagnitude Minimal magnitudo
	 * @param interval Interval refresh
	 * @return Url feed USGS
	 * @throws MalformedURLException
	 */
	private static URL getUrl(float minMagnitude, long interval) 
			throws MalformedURLException {
		String filename;
		if (interval <= AlarmManager.INTERVAL_HOUR) { // 1 jam kurang
			if (minMagnitude < 1) { 
				filename = "eqs1hour-M0.txt";
			} else {
				filename = "eqs1hour-M1.txt";
			}
		} else if (interval <= AlarmManager.INTERVAL_DAY) { // 1 hari kurang
			if (minMagnitude < 1) { 
				filename = "eqs1day-M0.txt";
			} else if (minMagnitude < 2.5) { 
				filename = "eqs1day-M1.txt";
			} else {
				filename = "eqs1day-M2.5.txt";
			}
		} else { // 1 hari keatas
			if (minMagnitude < 5) {
				filename = "eqs7day-M2.5.txt";
			} else if (minMagnitude < 7) { 
				filename = "eqs7day-M5.txt";
			} else {
				filename = "eqs7day-M7.txt";
			}
		}
		
		URL url = new URL(FEED + filename);
		Log.d(TAG, "Magnitude=" + minMagnitude + " interval=" + interval 
				+ ", url dipilih: " + url.toString());
		return url;
	}
	
	/**
	 * Parse data baris string ke objek EarthquakeDTO.
	 * 
	 * @param line Baris data sumber
	 * @param lastUpdate Terakhir kali diupdate
	 * @return Objek EarthquakeDTO
	 * @throws NumberFormatException
	 * @throws ParseException
	 */
	private static EarthquakeDTO getQuake(String line) 
			throws NumberFormatException, ParseException {
		Matcher matcher = PATTERN.matcher(line);
		if (!matcher.matches()) return null;
		
		String source = matcher.group(1);
		String eqid = matcher.group(2);
		String version = matcher.group(3);
		
		double latitude = Double.parseDouble(matcher.group(5));
		double longitude = Double.parseDouble(matcher.group(6));
		long time = TimeUtils.parseDate(matcher.group(4), DATE_FORMAT).getTime();
		
		float magnitude = Float.parseFloat(matcher.group(7));
		float depth = Float.parseFloat(matcher.group(8)) * 1000;
		int nst = Integer.parseInt(matcher.group(9));
		String region = matcher.group(10);
		
		return new EarthquakeDTO(0, source, eqid, version, time, latitude, longitude, 
				magnitude, depth, nst, region);
	}

	/**
	 * Mendapatkan alamat URL untuk gambar globe.
	 * 
	 * @param quake Data gempa
	 * @return URL globe atau null jika terdapat kesalahan
	 */
	public static URL getGlobeURL(EarthquakeDTO quake) {
		// Bulatkan (step 5)
		int latitude = (int) (Math.round(quake.latitude / 5) * 5);
		int longitude = (int) (Math.round(quake.longitude / 5) * 5);
		String imageName = latitude + "_" + longitude + ".jpg";
		
		try {
			URL url = new URL(UsgsSource.GLOBE + imageName);
			Log.d(TAG, "getImageUri: latitude=" + quake.latitude + ", longitude="
					+ quake.longitude + ", image=" + url.toString());
			return url;
			
		} catch (MalformedURLException e) {
			Log.e(TAG, "Error generate url, " + e.getMessage());
			return null;
		}
	}
	
	/**
	 * Mendapatkan alamat uri detail gempa pada USGS.
	 * 
	 * @param quake Data gempa
	 * @return Uri detail gempa pada USGS
	 */
	public static Uri getExternalUri(EarthquakeDTO quake) {
		return Uri.parse(UsgsSource.DETAIL + quake.source + quake.eqid + ".php");
	}
}
