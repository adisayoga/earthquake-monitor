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

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.adisayoga.earthquake.dto.EarthquakeDTO;
import com.adisayoga.earthquake.providers.EarthquakeColumns;
import com.adisayoga.earthquake.providers.EarthquakeProvider;

/**
 * Class yang digunakan untuk memudahkan mendapatkan data dari provider daftar
 * gempa.
 * 
 * @author Adi Sayoga
 */
public class EarthquakeModel {
	
	private static final String TAG = "EarthquakeModel";
	
	private final Context context;
	
	public EarthquakeModel(Context context) {
		this.context = context;
	}
	
	/**
	 * Mendapatkan data gempa berdasarkan minMagnitude dan maxAge.
	 * 
	 * @param minMagnitude Mangitudo minimal
	 * @param maxAge Maksimum umur yang akan ditampilkan
	 * @param sortOder Sort order
	 * @return List gempa
	 */
	public List<EarthquakeDTO> getMatchQuakes(float minMagnitude, long maxAge, 
			String sortOrder) {
		Log.i(TAG, "Mengambil data dari provider...");
    	
    	String where = EarthquakeColumns.MAGNITUDE + " >= " + minMagnitude 
    			+ " AND " + EarthquakeColumns.DATE + " >= " + maxAge;
    	if (sortOrder == null || sortOrder.equals("")) {
    		sortOrder = EarthquakeColumns.DATE + " DESC";	
    	}
    	
		ContentResolver resolver = context.getContentResolver();
    	Cursor cursor = resolver.query(EarthquakeProvider.CONTENT_URI, null, where, 
    			null, sortOrder);
    	
    	List<EarthquakeDTO> quakes = new ArrayList<EarthquakeDTO>();
		while (cursor.moveToNext()) {
    		long id = cursor.getLong(EarthquakeColumns._ID_INDEX);
    		String source = cursor.getString(EarthquakeColumns.SRC_INDEX);
    		String eqid = cursor.getString(EarthquakeColumns.EQID_INDEX);
    		String version = cursor.getString(EarthquakeColumns.VERSION_INDEX);
    		
    		long time = cursor.getLong(EarthquakeColumns.DATE_INDEX);
    		double latitude = cursor.getDouble(EarthquakeColumns.LATITUDE_INDEX);
    		double longitude = cursor.getDouble(EarthquakeColumns.LONGITUDE_INDEX);
    		float magnitude = cursor.getFloat(EarthquakeColumns.MAGNITUDE_INDEX);
    		
    		float depth = cursor.getFloat(EarthquakeColumns.DEPTH_INDEX);
    		int nst = cursor.getInt(EarthquakeColumns.NST_INDEX);
    		String region = cursor.getString(EarthquakeColumns.REGION_INDEX);
    		
    		// Tambahkan ke arraylist
    		EarthquakeDTO quake = new EarthquakeDTO(id, source, eqid, version, 
    				time, latitude, longitude, magnitude, depth, nst, region);
    		quakes.add(quake);
    	}
    	cursor.close();
    	
    	Log.d(TAG, "Selesai mengambil data, " + cursor.getCount() + " items");
    	return quakes;
	}
	
	/**
	 * Mendapatkan data gempa berdasarkan minMagnitude dan maxAge dengan default
	 * sort order.
	 * 
	 * @param minMagnitude Mangitudo minimal
	 * @param maxAge Maksimum umur yang akan ditampilkan
	 * @return List gempa
	 */
	public List<EarthquakeDTO> getMatchQuakes(float minMagnitude, long maxAge) {
		return getMatchQuakes(minMagnitude, maxAge, null);
	}
	
	/**
	 * Menghapus data gempa yang lebih lama dari age yang ditentukan. Jika age -1
	 * artinya menghapus semua data.
	 * 
	 * @param age Umur dalam milisecond
	 * @return Jumlah data yang dihapus
	 */
	public int deleteQuakes(long age) {
		Log.d(TAG, "Menghapus data...");
		
		ContentResolver resolver = context.getContentResolver();
		String where = null;
		String[] whereArgs = null;
		if (age != -1) {
			where = EarthquakeColumns.DATE + " < ?";
			long limit = System.currentTimeMillis() - age;
			whereArgs = new String[] { Long.toString(limit) };
		}
		int count = resolver.delete(EarthquakeProvider.CONTENT_URI, where, whereArgs);
		
		Log.d(TAG, "Data lama dihapus " + count + " items");
		return count;
	}
	
}
