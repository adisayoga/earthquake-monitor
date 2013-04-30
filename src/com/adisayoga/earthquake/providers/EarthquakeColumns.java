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
 
package com.adisayoga.earthquake.providers;

import android.provider.BaseColumns;

/**
 * Berisi konstanta daftar nama field dan field index untuk tabel earthquake.
 * 
 * @author Adi Sayoga
 */
public class EarthquakeColumns {

	public static final String TABLE_NAME = "earthquake";
	
	// Nama kolom
	public static final String _ID = BaseColumns._ID;
	
	public static final String SRC = "src";
	public static final String EQID = "eqid";
	public static final String VERSION = "version";
	
	public static final String DATE = "date";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String MAGNITUDE = "magnitude";
	public static final String DEPTH = "depth";
	public static final String NST = "nst";
	public static final String REGION = "region";
	
	// Kolom index
	public static final int _ID_INDEX = 0;
	public static final int SRC_INDEX = 1;
	public static final int EQID_INDEX = 2;
	public static final int VERSION_INDEX = 3;
	
	public static final int DATE_INDEX = 4;
	public static final int LATITUDE_INDEX = 5;
	public static final int LONGITUDE_INDEX = 6;
	public static final int MAGNITUDE_INDEX = 7;
	public static final int DEPTH_INDEX = 8;
	public static final int NST_INDEX = 9;
	public static final int REGION_INDEX = 10;
}
