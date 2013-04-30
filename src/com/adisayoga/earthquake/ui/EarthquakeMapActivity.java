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

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.adisayoga.earthquake.R;
import com.adisayoga.earthquake.dto.EarthquakeDTO;
import com.adisayoga.earthquake.models.EarthquakeModel;
import com.adisayoga.earthquake.providers.EarthquakeColumns;
import com.adisayoga.earthquake.receivers.EarthquakeReceiver;
import com.adisayoga.earthquake.receivers.RefreshReceiver;
import com.adisayoga.earthquake.ui.preferences.PrefsActivity;
import com.adisayoga.earthquake.utils.BaseLocationListener;
import com.adisayoga.earthquake.utils.LocationFinder;
import com.adisayoga.earthquake.wrapper.Prefs;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

/**
 * Activity peta gempa.
 * 
 * @author Adi Sayoga
 */
public class EarthquakeMapActivity extends MapActivity {

	private static final String TAG = "EarthquakeMapActivity";
	private static final int SHOW_PREFS_REQUEST = 1;

	private static final String LAYER_SATTELITE_KEY = "map_layer_sattelite";
	private static final String LAYER_STREET_VIEW_KEY = "map_layer_street_view";
	private static final String LAYER_TRAFFIC_KEY = "map_layer_traffic";
	
	private static final int LAYER_SATTELITE_INDEX = 0;
	private static final int LAYER_STREET_VIEW_INDEX = 1;
	private static final int LAYER_TRAFFIC_INDEX = 2;
	
	private static Prefs prefs;
	private boolean firstStart = true;
	private boolean centerLocationWhenAvailable = false;
	
	private EarthquakeReceiver quakeReceiver;
	private List<EarthquakeDTO> quakes = new ArrayList<EarthquakeDTO>();
	
	private LocationManager locationManager;
	private LocationFinder locationFinder;
	private Location location;

	private ImageButton refreshButton;
	private ImageButton myLocationButton;
	private MapView mapView;
	private MapController mapController;
	private final boolean[] layers = new boolean[3];
	
	private List<Overlay> overlays;
	private List<EarthquakeOverlay> quakeOverlays;
	
	private LinearLayout locationLayout = null;
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		Log.d(TAG, "onWindowFocusChanged");
		super.onWindowFocusChanged(hasFocus);
		
		if (firstStart) {
			// Animasi tidak dapat dimulai pada onCreate, pilihan terbaik adalah pada
			// onWindowFocusChaged ini, dan ini haya dijalankan sekali saja.
			firstStart = false;
			Toast.makeText(this, R.string.refreshing, Toast.LENGTH_SHORT).show();
			animateRefreshButton(true);
			
			showLocationMarker(location);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		prefs = Prefs.getInstance(this);
		setTheme(prefs.getTheme().resId);
		setContentView(R.layout.map);
		
		quakeReceiver = new EarthquakeReceiver(handler);
		
		// Mendapatkan lokasi saat ini
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	locationFinder = new LocationFinder(this);
		location = getCurrentLocation();
		
		// Action Bar
    	setupActionBarListener();
    	
    	// Inisialisasi mapView
    	initializeMapView();
    	
		// Load data gempa dari provider, kemudian tampilkan pada peta
		quakeOverlays = new ArrayList<EarthquakeOverlay>();
    	updateQuakes(false);
        refreshData();
        
		// Mendapatkan item data gempa dari intent untuk ditampilkan di tengah-tengah.
		centerQuake();
	}

	/**
	 * Setup listener ActionBar.
	 */
    private void setupActionBarListener() {
    	refreshButton = (ImageButton) findViewById(R.id.refresh);
    	refreshButton.setOnClickListener(actionBarListener);
    	myLocationButton = (ImageButton) findViewById(R.id.location);
    	myLocationButton.setOnClickListener(actionBarListener);
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
	 * Inisiasi MapView.
	 */
	private void initializeMapView() {
		mapView = (MapView) findViewById(R.id.quake_mapview);
		mapView.setBuiltInZoomControls(true);
		
		boolean satelliteShown = prefs.isLayerUsed(LAYER_SATTELITE_KEY);
		layers[LAYER_SATTELITE_INDEX] = satelliteShown;
		mapView.setSatellite(satelliteShown);
		
		boolean streetViewShown = prefs.isLayerUsed(LAYER_STREET_VIEW_KEY);
		layers[LAYER_STREET_VIEW_INDEX] = streetViewShown;
		mapView.setStreetView(streetViewShown);
		
		boolean trafficShown = prefs.isLayerUsed(LAYER_TRAFFIC_KEY);
		layers[LAYER_TRAFFIC_INDEX] = trafficShown;
		mapView.setTraffic(trafficShown);
		
		mapController = mapView.getController();
		overlays = mapView.getOverlays();
	}

    /**
     * Buat tombol refresh berputar, sebagai tanda sedang ada proses pada background.
     * 
     * @param animate True jika animasi aktif, false jika tidak
     */
    private void animateRefreshButton(final boolean animate) {
    	handler.post(new Runnable() {
    		@Override
			public void run() {
    			AnimationDrawable anim = (AnimationDrawable) refreshButton.getDrawable();
    			if (animate) {
    				anim.start(); 
    			} else {
    				anim.stop();
    			}
    		}
    	});
    }
    
    /**
     * Menampilkan lokasi user saat ini (animasi).
     * 
     * @param Location Lokasi saat ini
     */
    private void showLocationMarker(Location Location) {
    	if (location == null) return;
    	GeoPoint point = new GeoPoint((int) (location.getLatitude() * 1E6), 
				(int) (location.getLongitude() * 1E6));
    	
        if (locationLayout == null) {
        	locationLayout = (LinearLayout) View.inflate(this, 
        			R.layout.location_marker, null);
        	ImageView marker = (ImageView) locationLayout.findViewById(R.id.marker);
            AnimationDrawable markerImage = (AnimationDrawable) marker.getDrawable();
            mapView.addView(locationLayout, 0);
            markerImage.start();
        }
        
        MapView.LayoutParams params = new MapView.LayoutParams(
        		ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams
        		.WRAP_CONTENT, point, MapView.LayoutParams.CENTER);
        locationLayout.setLayoutParams(params);
    }
    
    /**
     * Load data dari provider kemudian update overlay. (pada background thread)
     * 
     * @param finishLoading Menandakan proses loading selesai, animasi tombol refresh
     *        akan dihentikan
     */
	private void updateQuakes(final boolean finishLoading) {
		new AsyncTask<Boolean, Void, Void>() {
			private boolean finishLoading;
			
			@Override
			protected Void doInBackground(Boolean... params) {
				this.finishLoading = params[0];
				
				// Load data dari provider, urutkan berdasarkan tanggal ascending,
				// karena pada ovelay list yang paling akhir yang akan ditampilkan 
				// paling depan pada MapView
    			EarthquakeModel table = new EarthquakeModel(EarthquakeMapActivity.this);
    			quakes = table.getMatchQuakes(prefs.getMinMagnitude(), prefs.getMaxAge(), 
    					EarthquakeColumns.DATE + " ASC");
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {
				setupQuakeOverlay();
		    	if (finishLoading) animateRefreshButton(false);
			}
		}.execute(finishLoading);
	}
	
	/**
	 * Setup overlay gempa.
	 */
	private void setupQuakeOverlay() {
		// Kosongkan quakeOverlays dari overlays
		for (Overlay overlay : quakeOverlays) {
			overlays.remove(overlay);
		}
		quakeOverlays.clear();
		
		// Tambahkan quakeOverlay baru yang sesuai dengan database
		for (EarthquakeDTO quake : quakes) {
			Drawable quakeDrawable = getResources().getDrawable(R.drawable.one);
			EarthquakeOverlay quakeOverlay = new EarthquakeOverlay(quakeDrawable, 
					mapView, location);
			quakeOverlay.addQuake(quake);
			
			quakeOverlays.add(quakeOverlay);
			overlays.add(quakeOverlay);
		}
		
		mapView.invalidate();
	}
	
	/**
	 * Tampilkan gempa di tengah-tengah. Data gempa didapat dari extra pada intent.
	 */
	private void centerQuake() {
		EarthquakeDTO quake = null;
		Intent intent = getIntent();
		
		if (intent.hasExtra(EarthquakeColumns.TABLE_NAME)) {
			quake = (EarthquakeDTO) intent.getExtras().get(EarthquakeColumns.TABLE_NAME);
		}
		
		if (quake != null) {
			zoomToMagnitude(quake.magnitude);
			mapController.animateTo(quake.getPoint());
		}
	}

	/**
	 * Zoom peta sesuai dengan magnitudo gempa.
	 * 
	 * @param magnitude Magnitudo gempa
	 */
	private void zoomToMagnitude(float magnitude) {
		int zoom;
		
		if (magnitude < 2) {
			zoom = 11;
		} else if (magnitude < 3) {
			zoom = 10;
		} else if (magnitude < 4) {
			zoom = 9;
		} else if (magnitude < 5) {
			zoom = 8;
		} else if (magnitude < 6) {
			zoom = 7;
		} else {
			zoom = 6;
		}
		
		mapController.setZoom(zoom);
	}
	
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		
		registerReceiver(quakeReceiver, new IntentFilter(EarthquakeReceiver.NEW_QUAKE_FOUND));
    	registerReceiver(quakeReceiver, new IntentFilter(EarthquakeReceiver.NO_NEW_QUAKE));
    	registerReceiver(quakeReceiver, new IntentFilter(EarthquakeReceiver.NETWORK_ERROR));
    	
		if (prefs.isDetectLocation()) {
    		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, 
    				locationListener);
    	}
	}
	
	/**
	 * Kirim broadcast agar data direfresh.
	 */
	private void refreshData() {
		sendBroadcast(new Intent(RefreshReceiver.REFRESH, null, this, 
				RefreshReceiver.class));
	}
	
	@Override
	protected void onPause() {
    	Log.d(TAG, "onPause");
    	super.onPause();
    	unregisterReceiver(quakeReceiver);
    	locationManager.removeUpdates(locationListener);
    	animateRefreshButton(false);
    }
	
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	MenuInflater inflater = new MenuInflater(this);
    	inflater.inflate(R.menu.map_menu, menu);
    	return true;
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	super.onOptionsItemSelected(item);
    	Intent intent;
    	
    	switch (item.getItemId()) {
    	case R.id.layers:
    		AlertDialog.Builder layersBuilder = new AlertDialog.Builder(this);
    		layersBuilder.setTitle(R.string.layers);
    		layersBuilder.setMultiChoiceItems(R.array.layers, 
    				new boolean[] { 
    					layers[LAYER_SATTELITE_INDEX], 
    					layers[LAYER_STREET_VIEW_INDEX], 
    					layers[LAYER_TRAFFIC_INDEX]
    				}, 
    				new DialogInterface.OnMultiChoiceClickListener() {
    			
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					layers[which] = isChecked;
					switch (which) {
					case LAYER_SATTELITE_INDEX:
						mapView.setSatellite(isChecked);
				    	prefs.setLayerUsed(LAYER_SATTELITE_KEY, isChecked);
						break;
					case LAYER_STREET_VIEW_INDEX:
						mapView.setStreetView(isChecked);
						prefs.setLayerUsed(LAYER_STREET_VIEW_KEY, isChecked);
						break;
					case LAYER_TRAFFIC_INDEX:
						mapView.setTraffic(isChecked);
						prefs.setLayerUsed(LAYER_TRAFFIC_KEY, isChecked);
						break;
					}
				}
			});
    		layersBuilder.show();
    		break;
    		
    	case R.id.preferences:
    		intent = new Intent(this, PrefsActivity.class);
    		startActivityForResult(intent, SHOW_PREFS_REQUEST);
    		return true;
    	
		case R.id.help:
			intent = new Intent(this, HelpActivity.class);
			startActivity(intent);
			return true;
    	}
    	
    	return false;
    }

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	Log.d(TAG, "Activity result: request=" + requestCode + ", result=" + resultCode);
    	
    	switch (requestCode) {
    	case SHOW_PREFS_REQUEST:
    		if ((resultCode & PrefsActivity.RESULT_RESTART) == PrefsActivity.RESULT_RESTART) {
    			finish();
    			startActivity(new Intent(this, getClass()));
    			return;
    		}
    		if ((resultCode & PrefsActivity.RESULT_REFRESH) == PrefsActivity.RESULT_REFRESH) {
    			animateRefreshButton(true);
    			refreshData();
    		}
    		break;
    	}
    }
    
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	/**
	 * Handler dari refresh data.
	 */
	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			Log.d(TAG, "Handler: message.what=" + message.what);
			
			switch (message.what) {
			case EarthquakeReceiver.NEW_QUAKE_FOUND_WHAT:
				// Terdapat gempa baru, load data dari provider
				updateQuakes(false);
				
			case EarthquakeReceiver.NO_NEW_QUAKE_WHAT:
				// Tidak terdapat data, hanya menghentikan animasi refresh
				animateRefreshButton(false);
				break;
				
			case EarthquakeReceiver.NETWORK_ERROR_WHAT:
				// Perlihatkan dialog network error
				animateRefreshButton(false);
				Toast.makeText(EarthquakeMapActivity.this, 
						R.string.dialog_network_error_title, Toast.LENGTH_LONG).show();
				break;
			}
		}
	};
	
	/**
	 * Listener lokasi.
	 */
	private final LocationListener locationListener = new BaseLocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			Log.d(TAG, "Lokasi berubah, Lat: " + location.getLatitude() + ", Lng: " 
					+ location.getLongitude());
			EarthquakeMapActivity.this.location = location;
			
			// Update location pada location overlay
			//setupLocationOverlay();
			showLocationMarker(location);
			
			// Update location pada earthquake overlay
			for (EarthquakeOverlay quakeOverlay : quakeOverlays) {
				quakeOverlay.setLocation(location);
			}
			
			// Tampilkan lokasi saat ini di tengah-tengah
			if (centerLocationWhenAvailable) {
				centerLocationWhenAvailable = false;
				
				int latitude = (int) (location.getLatitude() * 1E6);
				int longitude = (int) (location.getLongitude() * 1E6);
				GeoPoint point = new GeoPoint(latitude, longitude);
				mapController.animateTo(point);
			}
		}
	};
	
	/**
	 * Listener untuk ActionBar
	 */
	private final View.OnClickListener actionBarListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.refresh:
				animateRefreshButton(true);
	    		refreshData();
	    		break;
	    		
	    	case R.id.location:
	    		if (prefs.isDetectLocation()) {
		    		location = locationFinder.getLastLocation(LocationFinder.MAX_DISTANCE, 
		    				System.currentTimeMillis() + prefs.getInterval());
		    		if (location != null) {
		    			int latitude = (int) (location.getLatitude() * 1E6);
						int longitude = (int) (location.getLongitude() * 1E6);
						GeoPoint point = new GeoPoint(latitude, longitude);
		    			mapController.animateTo(point);
		    		} else {
		        		centerLocationWhenAvailable = true;
		    			Toast.makeText(getApplicationContext(), R.string.waiting_location, 
		    					Toast.LENGTH_SHORT).show();
		    		}
	    		} else {
	    			if (location != null) {
	    				int latitude = (int) (location.getLatitude() * 1E6);
						int longitude = (int) (location.getLongitude() * 1E6);
	    				GeoPoint point = new GeoPoint(latitude, longitude);
		    			mapController.animateTo(point);
	    			} else {
	    				Toast.makeText(getApplicationContext(), R.string.unknown_location, 
	    						Toast.LENGTH_SHORT).show();
	    			}
	    		}
	    		break;
			}
		}
	};
	
}
