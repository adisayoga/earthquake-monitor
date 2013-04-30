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
 
package com.adisayoga.earthquake.ui;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.adisayoga.earthquake.R;
import com.adisayoga.earthquake.dto.ContactDTO;
import com.adisayoga.earthquake.providers.ContactColumns;
import com.adisayoga.earthquake.providers.ContactProvider;
import com.adisayoga.earthquake.wrapper.Prefs;

/**
 * Activity tambah/ubah/hapus data kontak.
 * 
 * @author Adi Sayoga
 */
public class ContactEditActivity extends Activity {

	private static final String TAG = "ContactEditActivity";
	private static Prefs prefs;
	
	private EditText nameText;
	private EditText phoneText;
	private EditText mailText;
	
	private long id = -1;
	private boolean needSave = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		prefs = Prefs.getInstance(this);
		setTheme(prefs.getTheme().resId);
		setContentView(R.layout.contact_edit);
		
		nameText = (EditText) findViewById(R.id.name);
		phoneText = (EditText) findViewById(R.id.phone_number);
		mailText = (EditText) findViewById(R.id.mail_address);
		
		Intent intent = getIntent();
		if (intent.hasExtra(ContactColumns.TABLE_NAME)) {
			ContactDTO contact = (ContactDTO) intent.getExtras().get(
					ContactColumns.TABLE_NAME);
			id = contact.id;
			bindView(contact);
		}
		setListeners();
		setResult(Activity.RESULT_OK);
	}
	
	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
		if (needSave) updateContact();
	}
	
	/**
	 * Menampilkan daftar kontak ke view
	 */
	private void bindView(ContactDTO contact) {
		if (contact == null) return;
		
		nameText.setText(contact.name);
		phoneText.setText(contact.phoneNumber);
		mailText.setText(contact.mail);
	}

	/**
	 * Set listener tombol
	 */
	private void setListeners() {
		// Tombol update
		Button updateButton = (Button) findViewById(R.id.update);
		updateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (updateContact()) {
					needSave = false;
					finish();
				}
			}
		});
		
		// Tombol kembali
		Button cancelButton = (Button) findViewById(R.id.cancel);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				needSave = false;
				if (id == -1) {
					setResult(Activity.RESULT_CANCELED);
				} else {
					deleteContact();
				}
				finish();
			}
		});
	}
	
	/**
	 * Update data kontak, tambah baru jika belum ada data.
	 * 
	 * @return True jika berhasil di-update, false sebaliknya
	 */
	private boolean updateContact() {
		String name = nameText.getText().toString();
		String phone = phoneText.getText().toString();
		String mail = mailText.getText().toString();
		if (name.equals("") || (phone.equals("") && mail.equals(""))) {
			return false;
		}
		
		ContentValues values = new ContentValues();
		values.put(ContactColumns.NAME, name);
		values.put(ContactColumns.PHONE_NUMBER, phone);
		values.put(ContactColumns.MAIL, mail);
		ContentResolver resolver = getContentResolver();
		if (id == -1) {
			// Jika id belum ditentukan, maka tambah kontak baru
			Uri uri = resolver.insert(ContactProvider.CONTENT_URI, values);
			id = Long.parseLong(uri.getPathSegments().get(1));
			Log.d(TAG, "Contact created...");
		} else {
			// Update data kontak
			resolver.update(ContactProvider.CONTENT_URI, values, ContactColumns._ID
					+ " = ?", new String[] { "" + id });
			Log.d(TAG, "Contact updated...");
		}
		return true;
	}
	
	/**
	 * Menghapus daftar kontak.
	 */
	private void deleteContact() {
		ContentResolver resolver = getContentResolver();
		resolver.delete(ContactProvider.CONTENT_URI, ContactColumns._ID + " = ?",
				new String[] { "" + id });
		Log.d(TAG, "Contact deleted...");
	}
}
