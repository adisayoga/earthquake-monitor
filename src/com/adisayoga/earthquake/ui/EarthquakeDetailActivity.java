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

import java.net.URL;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.adisayoga.earthquake.R;
import com.adisayoga.earthquake.dto.EarthquakeDTO;
import com.adisayoga.earthquake.dto.EarthquakeTheme;
import com.adisayoga.earthquake.dto.Unit;
import com.adisayoga.earthquake.models.UsgsSource;
import com.adisayoga.earthquake.providers.EarthquakeColumns;
import com.adisayoga.earthquake.utils.BaseLocationListener;
import com.adisayoga.earthquake.utils.BitmapUtils;
import com.adisayoga.earthquake.utils.LocationFinder;
import com.adisayoga.earthquake.utils.LocationUtils;
import com.adisayoga.earthquake.utils.TimeUtils;
import com.adisayoga.earthquake.wrapper.EarthquakeFacebook;
import com.adisayoga.earthquake.wrapper.EarthquakeTemplate;
import com.adisayoga.earthquake.wrapper.EarthquakeTwitter;
import com.adisayoga.earthquake.wrapper.Prefs;

/**
 * Activity detail gempa.
 * 
 * @author Adi Sayoga
 */
public class EarthquakeDetailActivity extends Activity {
	
	private static final String TAG = "EarthquakeDetailsActivity";
	private static Prefs prefs;
	
	private EarthquakeFacebook facebook;
	private EarthquakeTwitter twitter;
	private EarthquakeDTO quake = null;
	private LocationManager locationManager;
	private LocationFinder locationFinder;
	private Location location;
	
	private TextView titleTextView;
	private ImageView imageView;
	private ImageView imageMaskView;
	private ProgressBar imageProgress;
	private TextView distanceTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		facebook = new EarthquakeFacebook(this);
		twitter = new EarthquakeTwitter(this);
		prefs = Prefs.getInstance(this);
		EarthquakeTheme theme = prefs.getTheme();
		setTheme(theme.resId);
		setContentView(R.layout.earthquake_detail);
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	locationFinder = new LocationFinder(this);
    	location = getCurrentLocation();
    	
    	titleTextView = (TextView) findViewById(R.id.title);
    	titleTextView.setSelected(true);
		imageView = (ImageView) findViewById(R.id.map);
		imageMaskView = (ImageView) findViewById(R.id.map_mask);
		imageProgress = (ProgressBar) findViewById(R.id.map_progress);
		distanceTextView = (TextView) findViewById(R.id.distance);
		
		setListeners();
		
		Intent intent = getIntent();
		if (intent.hasExtra(EarthquakeColumns.TABLE_NAME)) {
			quake = (EarthquakeDTO) intent.getExtras().get(EarthquakeColumns.TABLE_NAME);
			bindView(quake, theme);
		}
	}
	
	/**
	 * Setup click listener untuk button.
	 */
	private void setListeners() {
		ImageButton showMapButton = (ImageButton) findViewById(R.id.show_map);
		showMapButton.setOnClickListener(buttonListener);
		ImageButton usgsButton = (ImageButton) findViewById(R.id.usgs_detail);
		usgsButton.setOnClickListener(buttonListener);
		
		ImageButton facebookButton = (ImageButton) findViewById(R.id.share_to_facebook);
		facebookButton.setOnClickListener(buttonListener);
		ImageButton twitterButton = (ImageButton) findViewById(R.id.share_to_twitter);
		twitterButton.setOnClickListener(buttonListener);
		ImageButton mailButton = (ImageButton) findViewById(R.id.send_mail);
		mailButton.setOnClickListener(buttonListener);
		ImageButton sendSmsButton = (ImageButton) findViewById(R.id.send_sms);
		sendSmsButton.setOnClickListener(buttonListener);
		
		ImageButton shareButton = (ImageButton) findViewById(R.id.share_others);
		shareButton.setOnClickListener(buttonListener);
	}

	/**
	 * Mendapatkan lokasi saat ini sesuai dengan preference. Jika dapat mendeteksi
	 * lokasi akan digunakan gps, atau jika tidak lokasi manual yang digunakan.
	 * 
	 * @return Lokasi
	 */
	private Location getCurrentLocation() {
    	locationFinder.setChangedLocationListener(locationListener);
    	Location location;
    	
    	if (prefs.isDetectLocation()) {
	    	location = locationFinder.getLastLocation(LocationFinder.MAX_DISTANCE, 
	    			System.currentTimeMillis() + prefs.getInterval());
    	} else {
    		location = prefs.getManualLocation();
    	}
    	
    	return location;
	}
	
	/**
	 * Menampilkan detail gempa bumi ke view.
	 * 
	 * @param quake Data gempa
	 * @param theme Tema yang digunakan
	 */
	private void bindView(EarthquakeDTO quake, EarthquakeTheme theme) {
		if (quake == null) return;
		
		titleTextView.setText(quake.region);
		TextView regionTextView = (TextView) findViewById(R.id.region);
		regionTextView.setText(quake.region);
		
		TextView locationTextView = (TextView) findViewById(R.id.location);
		locationTextView.setText(LocationUtils.getInstance(this).formatLocation(
				quake.latitude, quake.longitude, false));
		TextView magnitudeTextView = (TextView) findViewById(R.id.magnitude);
		magnitudeTextView.setText(Float.toString(quake.magnitude) + " " + 
				getString(R.string.sr));
		TextView dateTextView = (TextView) findViewById(R.id.date);
		dateTextView.setText(TimeUtils.getInstance(this).toHumanReadable(quake.time));
		
		Unit unit = prefs.getUnit();
		float distance = 0;
		if (location != null) distance = quake.getLocation().distanceTo(location);
		distanceTextView.setText(unit.formatNumber(distance, EarthquakeDTO
				.FRACTION_DISTANCE));
		TextView depthTextView = (TextView) findViewById(R.id.depth);
		depthTextView.setText(unit.formatNumber(quake.depth, EarthquakeDTO
				.FRACTION_DEPTH));
		
		loadGlobe(UsgsSource.getGlobeURL(quake));
	}
	
	/**
	 * Load gambar globe dari server USGS. (dalam thread berbeda)
	 * 
	 * @param url URL gambar
	 */
	private void loadGlobe(final URL url) {
		if (url == null) return;
		
		new AsyncTask<URL, Void, Bitmap>() {
			@Override
			protected void onPreExecute() {
				imageProgress.setVisibility(View.VISIBLE);
			}

			@Override
			protected Bitmap doInBackground(URL... params) {
				Bitmap bitmap = BitmapUtils.getImage(url);
				if (bitmap == null) return null;
				
				return BitmapUtils.getRoundedCorner(bitmap, bitmap.getWidth() / 2);
			}
			
			@Override
			protected void onPostExecute(Bitmap globe) {
				if (globe != null) {
					imageView.setImageBitmap(globe);
					imageMaskView.setVisibility(View.VISIBLE);
				} else {
					// Gambar tidak dapat diambil, tampilkan pesan error. 
					// ImageView biarkan dengan gambar defaultnya
					Toast.makeText(EarthquakeDetailActivity.this, 
							R.string.dialog_network_error_title, Toast.LENGTH_SHORT)
							.show();
				}
				imageProgress.setVisibility(View.GONE);
			}
		}.execute(url);
	}
	
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		
		if (prefs.isDetectLocation()) {
    		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, 
    				locationListener);
    	}
	}
	
	@Override
	protected void onPause() {
    	Log.d(TAG, "onPause");
    	super.onPause();
    	locationManager.removeUpdates(locationListener);
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "Activity result: request=" + requestCode + ", result=" + resultCode);
    	facebook.getFacebook().authorizeCallback(requestCode, resultCode, data);
    	twitter.authorizeCallback(requestCode, resultCode, data);
	}
	
	/**
	 * Perlihatkan peta gempa bumi.
	 * 
	 * @param quake Data gempa
	 */
    private void showMap(EarthquakeDTO quake) {
    	Intent intent = new Intent(this, EarthquakeMapActivity.class);
    	if (quake != null) intent.putExtra(EarthquakeColumns.TABLE_NAME, quake);
    	startActivity(intent);
    }

    /**
     * Buka browser dan tampilkan informasi gempa ini di website USGS.
     * 
     * @param uri Alamat uri gempa
     */
    private void showBrowser(Uri uri) {
    	Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(browserIntent);
    }

    /**
     * Share ke Facebook.
     * 
     * @param quake Data gempa
     */
    private void shareToFacebook(EarthquakeDTO quake) {
		facebook.postQuake(this, quake, location);
    }

    /**
     * Share gempa ini ke Twitter.
     * 
     * @param quake Quake
     */
    private void shareToTwitter(EarthquakeDTO quake) {
    	twitter.postQuake(this, quake, location);
    }

    /**
     * Tampilkan activity untuk mengirim email.
     * 
     * @param quake Data gempa
     */
    private void sendMail(EarthquakeDTO quake) {
    	Context context = this;
    	String message = EarthquakeTemplate.getInstance(context).getMessage(
    			prefs.getMailTemplate(context), prefs.getMailTemplateDetail(context), 
    			quake, location);
    	Intent intent = new Intent(this, MailSendActivity.class);
    	intent.putExtra(MailSendActivity.MESSAGE_EXTRA, message);
    	startActivity(intent);
    }

    /**
     * Tampilkan activity untuk mengirim pesan sms.
     * 
     * @param quake Quake
     */
    private void sendSms(EarthquakeDTO quake) {
    	Context context = this;
    	String message = EarthquakeTemplate.getInstance(context).getMessage(
    			prefs.getSmsTemplate(context), prefs.getSmsTemplateDetail(context), 
    			quake, location);
    			
    	Intent intent = new Intent(this, SmsSendActivity.class);
    	intent.putExtra(SmsSendActivity.MESSAGE_EXTRA, message);
    	startActivity(intent);
    }

    /**
     * Sharing menggunakan Intent.ACTION_SEND.
     * TODO Template yang digunakan?
     * 
     * @param quake Data gempa
     */
    private void shareOthers(EarthquakeDTO quake) {
    	Context context = this;
    	String message = EarthquakeTemplate.getInstance(context).getMessage(
    			prefs.getMailTemplate(context), prefs.getMailTemplateDetail(context), 
    			quake, location);
    	Intent intent = new Intent();
    	intent.setAction(Intent.ACTION_SEND);
    	intent.setType("text/plain");
    	intent.putExtra(Intent.EXTRA_TEXT, message);
    	intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
    	startActivity(Intent.createChooser(intent, getString(R.string.share)
    			+ " " + quake.region));
    }
    
    /**
     * Listener lokasi.
     */
	private final LocationListener locationListener = new BaseLocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			Log.d(TAG, "Lokasi berubah, Lat: " + location.getLatitude() + ", Lng: " 
					+ location.getLongitude());
			
			float distance = 0;
			if (location != null) distance = quake.getLocation().distanceTo(location);
			distanceTextView.setText(prefs.getUnit().formatNumber(distance, 
					EarthquakeDTO.FRACTION_DISTANCE));
			
			EarthquakeDetailActivity.this.location = location;
		}
	};
	
	/**
	 * Listener tombol.
	 */
	private final View.OnClickListener buttonListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.show_map: // Tampilkan pada peta
				showMap(quake);
				break;
			case R.id.usgs_detail: // Buka browser ke alamat USGS detail gempa
				Uri detailUri = UsgsSource.getExternalUri(quake);
				showBrowser(detailUri);
				break;
			case R.id.share_to_facebook: // Share ke Facebook
				shareToFacebook(quake);
				break;
			case R.id.share_to_twitter: // Share ke Twitter
				shareToTwitter(quake);
				break;
			case R.id.send_mail: // Kirim email
				sendMail(quake);
				break;
			case R.id.send_sms: // Kirim pesan
				sendSms(quake);
				break;
			case R.id.share_others: // Share lainnya
				shareOthers(quake);
				break;
			}	
		}
	};
}
