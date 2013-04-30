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

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.adisayoga.earthquake.R;
import com.adisayoga.earthquake.dto.ContactDTO;
import com.adisayoga.earthquake.models.ContactModel;
import com.adisayoga.earthquake.wrapper.EarthquakeSms;
import com.adisayoga.earthquake.wrapper.Prefs;

/**
 * Activity mengirim SMS.
 * <p>
 * TODO phonesText buat autoComplete, saat memilih kontak, tampilkan nama pada
 * actionBar
 * 
 * @author Adi Sayoga
 */
public class SmsSendActivity extends Activity {

	private static final String TAG = "SmsSendActivity";
	private static final int PICK_CONTACT_REQUEST = 1;
	public static final String MESSAGE_EXTRA = "message";
	
	private static Prefs prefs;
	private String message;
	
	private EditText phonesText;
	private EditText messageText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		prefs = Prefs.getInstance(this);
		setTheme(prefs.getTheme().resId);
		setContentView(R.layout.sms_send);
		
		phonesText = (EditText) findViewById(R.id.to);
		messageText = (EditText) findViewById(R.id.message);
		
		// Tampilkan pesan dari extra
		Intent intent = getIntent();
		if (intent.hasExtra(MESSAGE_EXTRA)) {
			message = intent.getStringExtra(MESSAGE_EXTRA);
			messageText.setText(message);
		}
		
		setListeners();
	}
	
	/**
	 * Set listener untuk masing-masing control.
	 */
	private void setListeners() {
		ImageButton picContact = (ImageButton) findViewById(R.id.pick_contact);
		picContact.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intentContactPick = new Intent(Intent.ACTION_PICK, 
						ContactsContract.Contacts.CONTENT_URI); 
				startActivityForResult(intentContactPick, PICK_CONTACT_REQUEST);
			}
		});
		
		Button sendButton = (Button) findViewById(R.id.send);
		sendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendSms();
			}
		});
		
		Button cancelButton = (Button) findViewById(R.id.cancel);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.d(TAG, "RequestCode = " + requestCode + ", resultCode=" + resultCode);
    	switch (requestCode) {
    	// Mengambil data dari daftar kontak
    	case PICK_CONTACT_REQUEST:
    		if (data != null && resultCode == Activity.RESULT_OK) {
    			pickContact(data);
    		}
	    	break;
    	}
    }

    /**
     * Mengambil data kontak dari daftar kontak telepon.
     * 
     * @param data Data
     */
    private void pickContact(Intent data) {
    	Log.i(TAG, data.getDataString());
		ContactModel contactTable = new ContactModel(this);
		ContactDTO contact = contactTable.getSystemContact(data.getData());
		if (contact == null) {
			Toast.makeText(this, R.string.no_contact, Toast.LENGTH_SHORT).show();
			return;
		}
		
		addPhone(contact.phoneNumber);
    }
    
    /**
     * Tambahkan nomor telepon ke phonesText.
     * 
     * @param phone Nomor telepon
     */
    private void addPhone(String phone) {
    	String phones = phonesText.getText().toString().trim();
    	if (!phones.equals("")) {
	    	if (!phones.substring(phones.length() - 1, phones.length()).equals(",")) {
	    		phones += ",";
	    	}
	    	phones += " ";
    	}
    	phones += phone + ", ";
    	phonesText.setText(phones);
    	phonesText.setSelection(phones.length());
    }
    
	/**
	 * Mengirim pesan sms.
	 */
	private void sendSms() {
		String phones = phonesText.getText().toString();
		String message = messageText.getText().toString();
		
		// Validasi
		if (phones.equals("")) {
			Toast.makeText(SmsSendActivity.this, R.string.phone_empty, 
					Toast.LENGTH_SHORT).show();
			return;
		} else if (message.equals("")) {
			Toast.makeText(SmsSendActivity.this, R.string.message_empty, 
					Toast.LENGTH_SHORT).show();
			return;
		}
		
		// Kirim pesan (async)
		new AsyncTask<String, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(String... args) {
				
				String[] phones = args[0].split(",");
				String message = args[1];
				
				EarthquakeSms sms = new EarthquakeSms(SmsSendActivity.this);
				List<String> phonesSent = sms.sendTextMessage(phones, message, 
						EarthquakeSms.SPLIT_SMS_MESSAGE);
				final boolean hasSent = phonesSent != null && phonesSent.size() > 0;
				return hasSent;
			}

			@Override
			protected void onPostExecute(Boolean hasSent) {
				if (hasSent)
					Toast.makeText(getApplicationContext(), R.string.sms_sent, 
							Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(getApplicationContext(), R.string.sms_fail, 
							Toast.LENGTH_SHORT).show();
			}
		}.execute(phones, message);
		
		finish();
	}
}
