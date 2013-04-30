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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Helper class untuk membuka, membuat, dan mengelola version control database
 * 
 * @author Adi Sayoga
 */
public class DatabaseHelper extends SQLiteOpenHelper {
	
	private static final String TAG = "DatabaseHelper";
	
	public static final String DATABASE_NAME = "earthquake.db";
	public static final int DATABASE_VERSION = 1;

	public DatabaseHelper(Context context, CursorFactory factory) {
		super(context, DATABASE_NAME, factory, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// Tabel gempa bumi
		db.execSQL("CREATE TABLE " + EarthquakeColumns.TABLE_NAME + " ("
				+ EarthquakeColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ EarthquakeColumns.SRC + " TEXT, "
				+ EarthquakeColumns.EQID + " TEXT, "
				+ EarthquakeColumns.VERSION + " TEXT, "
				
				+ EarthquakeColumns.DATE + " INTEGER, "
				+ EarthquakeColumns.LATITUDE + " FLOAT, "
				+ EarthquakeColumns.LONGITUDE + " FLOAT, "
				+ EarthquakeColumns.MAGNITUDE + " FLOAT, "
				+ EarthquakeColumns.DEPTH + " FLOAT, "
				+ EarthquakeColumns.NST + " INTEGER, "
				+ EarthquakeColumns.REGION + " TEXT);");
		
		// Tabel contact
		db.execSQL("CREATE TABLE " + ContactColumns.TABLE_NAME + " ("
				+ ContactColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ ContactColumns.NAME + " TEXT, "
				+ ContactColumns.PHONE_NUMBER + " TEXT, "
				+ ContactColumns.MAIL + " TEXT);");
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Meng-upgrade database dari versi " + oldVersion + " ke " 
				+ newVersion + ", yang akan menghapus semua data lama");
		
		// Hapus semua tabel
		db.execSQL("DROP TABLE IF EXISTS " + EarthquakeColumns.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + ContactColumns.TABLE_NAME);
		
		// Ciptakan tabel baru
		onCreate(db);
	}

}
