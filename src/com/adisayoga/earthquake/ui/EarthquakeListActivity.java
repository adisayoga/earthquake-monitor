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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.adisayoga.earthquake.wrapper.EarthquakeFacebook;
import com.adisayoga.earthquake.wrapper.EarthquakeTemplate;
import com.adisayoga.earthquake.wrapper.EarthquakeTwitter;
import com.adisayoga.earthquake.wrapper.Prefs;
import com.adisayoga.earthquake.wrapper.WarnDialogBuilder;

/**
 * Activity daftar gempa.
 * 
 * @author Adi Sayoga
 */
public class EarthquakeListActivity extends Activity {
	
	private static final String TAG = "EarthquakeListActivity";
	private static final int SHOW_PREFS_REQUEST = 1;
	
	// Dialog
	private static final int NETWORK_ERROR_DIALOG = 1;
	private static final int STARTUP_DIALOG = 2;
	
	private static Prefs prefs;
	
	private AlertDialog networkErrorDialog;
	private AlertDialog startupDialog;
	
	private boolean firstStart = true;
	
	private EarthquakeReceiver listReceiver;
	private EarthquakeFacebook facebook;
	private EarthquakeTwitter twitter;
	
	private final List<EarthquakeDTO> quakes = new ArrayList<EarthquakeDTO>();
	private EarthquakeListAdapter adapter;

	private ImageButton refreshButton;
	private ImageButton showMapButton;
	private ListView list;
	private LinearLayout noQuake;
	
	private LocationManager locationManager;
	private LocationFinder locationFinder;
	private Location location;

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
			
	        // Startup dialog
	        if (prefs.isDialogShown(Prefs.DIALOG_STARTUP)) showDialog(STARTUP_DIALOG);
		}
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        prefs = Prefs.getInstance(this);
        facebook = new EarthquakeFacebook(this);
        twitter = new EarthquakeTwitter(this);
        
        listReceiver = new EarthquakeReceiver(handler);
        setTheme(prefs.getTheme().resId);
        setContentView(R.layout.earthquake_list);
        
        // TEST: Hapus data gempa
        // EarthquakeModel table = new EarthquakeModel(this);
        // table.deleteQuakes(-1);
        
        // TEST: Reset last update
        // prefs.setLastUpdate(System.currentTimeMillis() - AlarmManager
        //		.INTERVAL_DAY * 30);
        
		// Mendapatkan lokasi saat ini
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	locationFinder = new LocationFinder(this);
		location = getCurrentLocation();
		
    	setupActionBarListener();
        setupListAdapter();
    	
        // Load data dari database saat di-create, saat onResume, akan diperbaharui 
        // setelah mendapatkan data dari server.
        noQuake = (LinearLayout) findViewById(R.id.no_quake);
        updateQuakes(false);
        refreshData();
    }
    
    /**
     * Setup listener action bar
     */
    private void setupActionBarListener() {
    	refreshButton = (ImageButton) findViewById(R.id.refresh);
    	refreshButton.setOnClickListener(actionBarListener);
    	showMapButton = (ImageButton) findViewById(R.id.show_map);
    	showMapButton.setOnClickListener(actionBarListener);
    }
    
    /**
     * Setup list adapter dan listener list
     */
    private void setupListAdapter() {
    	adapter = new EarthquakeListAdapter(this, R.layout.earthquake_list_item, 
        		location, quakes, quickActionListener);
        list = (ListView) findViewById(R.id.listview);
        list.setAdapter(adapter);
        list.setOnItemClickListener(itemClickListener);
        list.setOnItemLongClickListener(itemLongClickListener);
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
     * Buat tombol refresh berputar, sebagai tanda sedang ada proses pada background.
     * 
     * @param animate True jika animasi aktif, false jika tidak
     */
    private void animateRefreshButton(boolean animate) {
		AnimationDrawable anim = (AnimationDrawable) refreshButton.getDrawable();
		if (animate) {
			anim.start(); 
		} else {
			anim.stop();
		}
    }

    /**
     * Load data dari provider kemudian update list. (pada background thread)
     * 
     * @param finishLoading Menandakan proses loading selesai, animasi tombol refresh
     *        akan dihentikan
     */
    private void updateQuakes(boolean finishLoading) {
    	new AsyncTask<Boolean, Void, List<EarthquakeDTO>>() {
    		private boolean finishLoading;
    		
			@Override
			protected List<EarthquakeDTO> doInBackground(Boolean... params) {
				this.finishLoading = params[0];
				
    			// Load data dari provider
    			EarthquakeModel table = new EarthquakeModel(EarthquakeListActivity.this);
    			List<EarthquakeDTO> newQuakes = table.getMatchQuakes(
    					prefs.getMinMagnitude(), prefs.getMaxAge());
    			
				return newQuakes;
			}
    		
			@Override
			protected void onPostExecute(List<EarthquakeDTO> newQuakes) {
    			// Tambahkan ke quakes
				quakes.clear();
    			if (newQuakes != null) {
    				for (EarthquakeDTO quake : newQuakes) {
    					quakes.add(quake);
    				}
    			}
    			
    			// Perlihatkan atau sembunyikan view
				if (quakes.size() > 0) {
		    		Log.d(TAG, "Terdapat data, memperlihatkan list");
		    		noQuake.setVisibility(View.GONE);
		    		list.setVisibility(View.VISIBLE);
		    	} else {
		    		Log.d(TAG, "Tidak terdapat data, menyembunyikan list");
		    		noQuake.setVisibility(View.VISIBLE);
		    		list.setVisibility(View.GONE);
		    	}
				adapter.notifyDataSetChanged();
		    	if (finishLoading) animateRefreshButton(false);
			}
    	}.execute(finishLoading);
    }
    
    @Override
	protected void onResume() {
    	Log.d(TAG, "onResume");
    	super.onResume();
    	
    	registerReceiver(listReceiver, new IntentFilter(EarthquakeReceiver.NEW_QUAKE_FOUND));
    	registerReceiver(listReceiver, new IntentFilter(EarthquakeReceiver.NO_NEW_QUAKE));
    	registerReceiver(listReceiver, new IntentFilter(EarthquakeReceiver.NETWORK_ERROR));
    	
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
    	unregisterReceiver(listReceiver);
    	locationManager.removeUpdates(locationListener);
    	animateRefreshButton(false);
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	MenuInflater inflater = new MenuInflater(this);
    	inflater.inflate(R.menu.list_menu, menu);
    	return true;
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	super.onOptionsItemSelected(item);
    	Intent intent;
    	
    	switch (item.getItemId()) {
    	case R.id.contact:
    		intent = new Intent(this, ContactActivity.class);
    		startActivity(intent);
    		return true;
    		
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
    	facebook.getFacebook().authorizeCallback(requestCode, resultCode, data);
    	
    	Log.d(TAG, "RequestCode = " + requestCode);
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
	public Dialog onCreateDialog(int id) {
    	switch (id) {
		case NETWORK_ERROR_DIALOG:
			if (networkErrorDialog == null) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setIcon(android.R.drawable.ic_dialog_alert);
				builder.setTitle(R.string.dialog_network_error_title);
				builder.setMessage(R.string.dialog_network_error_msg);
				builder.setNeutralButton(android.R.string.ok, new DialogInterface
						.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dismissDialog(NETWORK_ERROR_DIALOG);
					}
				});
				networkErrorDialog = builder.create();
			}
			return networkErrorDialog;
			
		case STARTUP_DIALOG:
			if (startupDialog == null) {
				ContextThemeWrapper dialogContext = new ContextThemeWrapper(this, 
						android.R.style.Theme_Dialog);
				
				AlertDialog.Builder builder = new WarnDialogBuilder(dialogContext, 
						Prefs.DIALOG_STARTUP);
				builder.setTitle(R.string.dialog_startup_title);
				builder.setIcon(android.R.drawable.ic_dialog_info);
				builder.setMessage(R.string.dialog_startup_msg);
				
				startupDialog = builder.create();
			}
			return startupDialog;
    	}
    	
    	return null;
    }
    
	/**
	 * Start activity detail gempa bumi.
	 * 
	 * @param quake Quake
	 */
    private void showQuakeDetails(EarthquakeDTO quake) {
		Intent intent = new Intent(this, EarthquakeDetailActivity.class);
		if (quake != null) intent.putExtra(EarthquakeColumns.TABLE_NAME, quake);
		startActivity(intent);
    }
    
    /**
     * Perlihatkan peta gempa bumi.
     * 
     * @param quake Quake
     */
    private void showQuakeMap(EarthquakeDTO quake) {
    	Intent intent = new Intent(this, EarthquakeMapActivity.class);
    	if (quake != null) intent.putExtra(EarthquakeColumns.TABLE_NAME, quake);
    	startActivity(intent);
    }
    
    /**
     * Share gempa ini ke Facebook.
     * 
     * @param quake Quake
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
     * Handler dari refresh data.
     */
	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			Log.d(TAG, "Handler: message.what=" + message.what);
			
			switch (message.what) {
			case EarthquakeReceiver.NEW_QUAKE_FOUND_WHAT:
				// Terdapat gempa baru, load data dari provider
				updateQuakes(true);
				break;
				
			case EarthquakeReceiver.NO_NEW_QUAKE_WHAT:
				// Tidak terdapat data, hanya menghentikan animasi refresh
				animateRefreshButton(false);
				break;
				
			case EarthquakeReceiver.NETWORK_ERROR_WHAT:
				// Perlihatkan dialog network error
				animateRefreshButton(false);
				showDialog(NETWORK_ERROR_DIALOG);
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
			
			adapter.setLocation(location);
			adapter.notifyDataSetChanged();
		}
	};
	
	/**
	 * Listener untuk tombol pada ActionBar
	 */
	private final View.OnClickListener actionBarListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.refresh:
				animateRefreshButton(true);
	    		refreshData();
	    		break;
	    	case R.id.show_map:
	    		showQuakeMap(null);
	    		break;
			}
		}
	};
	
	/**
	 * Listener saat item list diklik.
	 */
	private final AdapterView.OnItemClickListener itemClickListener 
			= new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// Tampilkan detail gempa bumi
			EarthquakeDTO quake = quakes.get(position);
			showQuakeDetails(quake);
		}
	};
	
	/**
	 * Listener untuk menampilkan context menu.
	 */
	private final AdapterView.OnItemLongClickListener itemLongClickListener
			= new AdapterView.OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			final EarthquakeDTO selectedQuake = quakes.get(position);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(EarthquakeListActivity.this);
			builder.setTitle(selectedQuake.region);
			
			builder.setItems(R.array.list_options, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0: // Tampilkan detail gempa bumi
						showQuakeDetails(selectedQuake);
						break;
					case 1: // Tampilkan pada peta
						showQuakeMap(selectedQuake);
						break;
					case 2: // Share ke Facebook
						shareToFacebook(selectedQuake);
						break;
					case 3: // Share ke Twitter
						shareToTwitter(selectedQuake);
						break;
					case 4: // Kirim email
						sendMail(selectedQuake);
						break;
					case 5: // Kirim sms
						sendSms(selectedQuake);
						break;
					case 6: // Share lainnya
						shareOthers(selectedQuake);
						break;
					}
				}
			});
			builder.show();
			return true;
		}
	};
	
	/**
	 * Listener untuk QuickAction.
	 */
	private final EarthquakeListAdapter.OnQuickActionListener quickActionListener 
			= new EarthquakeListAdapter.OnQuickActionListener() {
		@Override
		public void onItemClick(int quakePos, int quickActionPos) {
			EarthquakeDTO selectedQuake = quakes.get(quakePos);
			
			switch (quickActionPos) {
			case 0: // Share ke Facebook
				shareToFacebook(selectedQuake);
				break;
			case 1: // Share ke Twitter
				shareToTwitter(selectedQuake);
				break;
			case 2: // Kirim email
				sendMail(selectedQuake);
				break;
			case 3: // Kirim sms
				sendSms(selectedQuake);
				break;
			case 4: // Share lainnya
				shareOthers(selectedQuake);
				break;
			}
		}
	};
}