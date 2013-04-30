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
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.adisayoga.earthquake.R;
import com.adisayoga.earthquake.ui.preferences.PrefsActivity;
import com.adisayoga.earthquake.utils.LocationUtils;
import com.adisayoga.earthquake.wrapper.Prefs;

/**
 * Activity untuk menentukan lokasi yang diinputkan manual.
 * 
 * @author Adi Sayoga
 */
public class ManualLocationActivity extends Activity {

	private static final String TAG = "ManualLocationActivity";
	private static Prefs prefs;
	
	private RadioButton addressCheckbox;
	private EditText addressText;
	private RadioButton locationCheckbox;
	private EditText latitudeText;
	private EditText longitudeText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        
        prefs = Prefs.getInstance(this);
        setTheme(prefs.getTheme().resId);
        setContentView(R.layout.manual_location);
        
        addressCheckbox = (RadioButton) findViewById(R.id.use_address);
        addressText = (EditText) findViewById(R.id.address);
        addressText.setText(prefs.getManualLocationAddress());
        
        locationCheckbox = (RadioButton) findViewById(R.id.use_location);
        latitudeText = (EditText) findViewById(R.id.latitude);
        latitudeText.setText(Double.toString(prefs.getManualLatitude()));
        longitudeText = (EditText) findViewById(R.id.longitude);
        longitudeText.setText(Double.toString(prefs.getManualLongitude()));
        
        setListeners();
        setResult(PrefsActivity.RESULT_CANCELED);
	}
	
	/**
	 * Set listener untuk masing-masing view
	 */
	private void setListeners() {
		addressCheckbox.setOnClickListener(checkboxListener);
		locationCheckbox.setOnClickListener(checkboxListener);
        
		// Tombol update
		Button updateButton = (Button) findViewById(R.id.update);
        updateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateLocation();
			}
		});
        
        // Tombol kembali
        Button backButton = (Button) findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	/**
	 * Update lokasi
	 */
	private void updateLocation() {
		Toast.makeText(ManualLocationActivity.this, R.string.waiting_location, 
				Toast.LENGTH_SHORT).show();
		
		// Lokasi berdasarkan nama alamat
		if (addressCheckbox.isChecked()) {
			String addressName = addressText.getText().toString();
			
			// Simpan ke preference, untuk memastikan alamat tersimpan walaupun 
			// getFromLocationName gagal mendapatkan alamat
			prefs.setManualLocactionAddress(addressName);
			setResult(PrefsActivity.RESULT_RESTART);
			
			getFromLocationName(addressName);
			
		// Lokasi berdasarkan latitude dan longitude
		} else if (locationCheckbox.isChecked()) {
			try {
				String latitudeString = latitudeText.getText().toString();
				String longitudeString = longitudeText.getText().toString();
				double latitude = Float.parseFloat(latitudeString);
				double longitude = Float.parseFloat(longitudeString);
				
				// Simpan ke preference, untuk memastikan latitude dan longitude
				// tersimpan walaupun getFromLocation gagal mendapatkan alamat
				prefs.setManualLocation(latitude, longitude);
				setResult(PrefsActivity.RESULT_RESTART);
				
				getFromLocation(latitude, longitude);
				
			} catch (NumberFormatException e) {
				showUnknownLocation();
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}
	
	/**
	 * Mendapatkan lokasi alamat berdasarkan nama alamat. (pada thread berbeada)
	 * 
	 * @param addressName Nama alamat
	 */
	private void getFromLocationName(String addressName) {
		new AsyncTask<String, Void, List<Address>>() {
			
			@Override
			protected List<Address> doInBackground(String... params) {
				try {
					Geocoder geocoder = new Geocoder(getApplicationContext(), 
							Locale.getDefault());
					List<Address> addresses = geocoder.getFromLocationName(
							params[0], 5);
					return addresses;
					
				} catch (Exception e) {
					return null;
				}
			}

			@Override
			protected void onPostExecute(List<Address> result) {
				updateView(result);
			}
		}.execute(addressName);
	}
	
	/**
	 * Mendapatkan alamat berdasarkan lokasi alamat. (pada thread berbeada)
	 * 
	 * @param latitude Latitude
	 * @param longitude Longitude
	 */
	private void getFromLocation(double latitude, double longitude) {
		new AsyncTask<Double, Void, List<Address>>() {
			
			@Override
			protected List<Address> doInBackground(Double... params) {
				try {
					Geocoder geocoder = new Geocoder(ManualLocationActivity.this, 
							Locale.getDefault());
					List<Address> addresses = geocoder.getFromLocation(params[0], 
							params[1], 5);
					return addresses;
					
				} catch (Exception e) {
					return null;
				}
			}

			@Override
			protected void onPostExecute(List<Address> result) {
				updateView(result);
			}
		}.execute(latitude, longitude);
	}
	
	/**
	 * Update lokasi.
	 * 
	 * @param addresses Alamat
	 */
	private void updateView(List<Address> addresses) {
		if (addresses == null || addresses.size() == 0) {
			showUnknownLocation();
			return;
		}
		
		if (addresses.size() == 1) {
			// Hanya terdapat satu alamat, langsung update lokasi
			saveLocation(addresses.get(0));
		} else {
			// Terdapat alamt lebih dari satu, tampilkan dialog pilihan alamat
			showAddressDialog(addresses);
		}
	}
	
	/**
	 * Simpan lokasi di preference.
	 * 
	 * @param address Alamat
	 */
	private void saveLocation(Address address) {
		String addressName = LocationUtils.getAddressLine(address);
		double latitude = address.getLatitude();
		double longitude = address.getLongitude();
		
		// Simpan ke preference
		prefs.setManualLocactionAddress(addressName);
		prefs.setManualLocation(latitude, longitude);
		
		// Tampilkan pada UI
		addressText.setText(addressName);
		latitudeText.setText(Double.toString(latitude));
		longitudeText.setText(Double.toString(longitude));
		
		Toast.makeText(this, addressName, Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * Menampilkan dialog pilihan alamat
	 * 
	 * @param addresses List address
	 */
	public void showAddressDialog(final List<Address> addresses) {
		AddressAdapter adapter = new AddressAdapter(this, R.layout.address_details, 
				addresses);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.select_location);
		
		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Address address = addresses.get(which);
				saveLocation(address);
			}
		});
		builder.show();
	}
	
	/**
	 * Menampilkan pesan lokasi tidak diketahui. (dijalankan pada UI thread)
	 */
	private void showUnknownLocation() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(ManualLocationActivity.this, 
						R.string.unknown_location, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	/**
	 * Enable/disable textbox sesuai dengan checkbox yang mana yang diklik.
	 */
	private final View.OnClickListener checkboxListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.use_address:
				locationCheckbox.setChecked(false);
				
				addressText.setEnabled(true);
				latitudeText.setEnabled(false);
				longitudeText.setEnabled(false);
				
				addressText.requestFocus();
				break;
			case R.id.use_location:
				addressCheckbox.setChecked(false);
				
				addressText.setEnabled(false);
				latitudeText.setEnabled(true);
				longitudeText.setEnabled(true);
				
				latitudeText.requestFocus();
				break;
			}
		}
	};
	
}
