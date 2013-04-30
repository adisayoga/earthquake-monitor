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


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Content provider untuk tabel kontak.
 * 
 * @author Adi Sayoga
 */
public class ContactProvider extends ContentProvider {
	
	private static final String TAG = "ContactProvider";
	public static final String AUTHORITY = "com.adisayoga.provider.contact";
	
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY 
			+ "/contacts");
	
	private SQLiteDatabase db;
	
	// Buat konstanta untuk membedakan URI request
	private static final int CONTACTS = 1;
	private static final int CONTACT_ID = 2;
	
	private static final UriMatcher uriMatcher;
	
	// Alokasi objek UriMatcher, jika URI diakhiri dengan 'contacts' berarti
	// request untuk semua data, dan 'contacts' dengan diikuti '/[rowID] akan
	// mewakili satu baris data.
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, "contacts", CONTACTS);
		uriMatcher.addURI(AUTHORITY, "contacts/#", CONTACT_ID);
	}
	
	@Override
	public boolean onCreate() {
		Context context = getContext();
		DatabaseHelper dbHelper = new DatabaseHelper(context, null);
		try {
			db = dbHelper.getWritableDatabase();
		} catch (SQLException e) {
			db = null;
			Log.e(TAG, "Error membuka database, " + e.getMessage());
		}
		return (db == null) ? false : true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, 
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(ContactColumns.TABLE_NAME);
		
		// Jika terdapat baris query, batasi result set tergantung row yang dilewatkan
		switch (uriMatcher.match(uri)) {
		case CONTACT_ID:
			builder.appendWhere(ContactColumns._ID + " = " 
					+ uri.getPathSegments().get(1));
			break;
		default:
			break;
		}
		
		// Jika sort order tidak ditentukan, urutkan berdasarkan nama
		if (TextUtils.isEmpty(sortOrder)) sortOrder = ContactColumns.NAME;
		
		// Terapkan query ke database
		Cursor cursor = builder.query(db, projection, selection, selectionArgs, 
				null, null, sortOrder);

		// Register contexts ContentResolver untuk diberitahukan jika cursor result
		// set berubah.
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		
		// Return cursor hasil query
		//Log.d(TAG, "query: " + uri + ", result count=" + cursor.getCount());
		return cursor;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// Insert baris baru, akan mengembalikan no baris jika sukses
		long rowId = db.insert(ContactColumns.TABLE_NAME, "nullhack", values);
		
		// Mengembalikan URI ke baris yang baru saja diinsert saat sukses
		if (rowId > 0) {
			Uri newUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(uri, null);
			//Log.d(TAG, "insert: " + uri + ", newUri=" + newUri);
			return newUri;
		}
		throw new SQLException("Gagal insert baris ke " + uri);
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection, 
			String[] selectionArgs) {
		int count;
		switch (uriMatcher.match(uri)) {
		case CONTACTS:
			count = db.update(ContactColumns.TABLE_NAME, values, selection, 
					selectionArgs);
			break;
			
		case CONTACT_ID:
			String segment = uri.getPathSegments().get(1);
			count = db.update(ContactColumns.TABLE_NAME, values, ContactColumns._ID 
					+ " = " + segment + (!TextUtils.isEmpty(selection) ? " AND (" 
					+ selection + ")" : ""), selectionArgs);
			break;
			
		default:
			throw new IllegalArgumentException("URI tidak didukung: " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		//Log.d(TAG, "update: " + uri + ", result count=" + count);
		return count;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count;
		
		switch (uriMatcher.match(uri)) {
		case CONTACTS:
			count = db.delete(ContactColumns.TABLE_NAME, selection, selectionArgs);
			break;
			
		case CONTACT_ID:
			String segment = uri.getPathSegments().get(1);
			count = db.delete(ContactColumns.TABLE_NAME, ContactColumns._ID
					+ " = " + segment + (!TextUtils.isEmpty(selection) ? " AND ("
					+ selection + ")" : ""), selectionArgs);
			break;
			
		default:
			throw new IllegalArgumentException("URI tidak didukung: " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		//Log.d(TAG, "delete: " + uri + ", result count=" + count);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case CONTACTS:
			return "vnd.android.cursor.dir/vnd.adisayoga.earthquake";
		case CONTACT_ID:
			return "vnd.android.cursor.item/vnd.adisayoga.earthquake";
		default:
			throw new IllegalArgumentException("URI tidak didukung: " + uri);
		}
	}

}
