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
 
package com.adisayoga.earthquake.ui.preferences;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.ContextThemeWrapper;

import com.adisayoga.earthquake.R;
import com.adisayoga.earthquake.dto.Unit;
import com.adisayoga.earthquake.receivers.RefreshReceiver;
import com.adisayoga.earthquake.ui.ContactActivity;
import com.adisayoga.earthquake.ui.ManualLocationActivity;
import com.adisayoga.earthquake.ui.SocialConnectActivity;
import com.adisayoga.earthquake.wrapper.Prefs;
import com.adisayoga.earthquake.wrapper.WarnDialogBuilder;

/**
 * Activity class preference main.
 * 
 * @author Adi Sayoga
 */
public class PrefsActivity extends PreferenceActivity implements 
		OnSharedPreferenceChangeListener, OnPreferenceClickListener {
	
	private static final String TAG = "PrefsActivity";
	
	// Request
	private static final int LOC_MANUAL_REQUEST = 1;
	private static final int NOTIFY_REQUEST = 2;
	private static final int SMS_REQUEST = 3;
	private static final int FACEBOOK_REQUEST = 4;
	private static final int TWITTER_REQUEST = 5;
	private static final int MAIL_REQUEST = 6;
	private static final int DIALOG_REQUEST = 7;
	
	// Preference result
	public static final int RESULT_CANCELED = 0;
	public static final int RESULT_REFRESH = 1;
	public static final int RESULT_RESTART = 2;
	
	private int prefsResult = RESULT_CANCELED;
	private Prefs prefs;
	private SharedPreferences sharedPrefs;
	private ListPreference rangePref;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		
		prefs = Prefs.getInstance(this);
		setTheme(prefs.getTheme().resId);
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.prefs);
		sharedPrefs = getPreferenceScreen().getSharedPreferences();
		
		rangePref = (ListPreference) findPreference(Prefs.RANGE);
		
		// Set listener
		ListPreference intervalPref = (ListPreference) findPreference(Prefs.INTERVAL);
		intervalPref.setOnPreferenceClickListener(this);
		Preference locManualPref = findPreference(Prefs.LOC_MANUAL);
		locManualPref.setOnPreferenceClickListener(this);
		
		Preference notifyPref = findPreference(Prefs.NOTIFY);
		notifyPref.setOnPreferenceClickListener(this);
		Preference smsPref = findPreference(Prefs.SMS);
		smsPref.setOnPreferenceClickListener(this);
		Preference facebookPref = findPreference(Prefs.FACEBOOK);
		facebookPref.setOnPreferenceClickListener(this);
		Preference twitterPref = findPreference(Prefs.TWITTER);
		twitterPref.setOnPreferenceClickListener(this);
		Preference mailPref = findPreference(Prefs.MAIL);
		mailPref.setOnPreferenceClickListener(this);
		
		Preference socialPrefs = findPreference(Prefs.SOCIAL_CONNECT);
		socialPrefs.setOnPreferenceClickListener(this);
		Preference contactPrefs = findPreference(Prefs.CONTACT);
		contactPrefs.setOnPreferenceClickListener(this);
		
		Preference dialogPref = findPreference(Prefs.DIALOG);
		dialogPref.setOnPreferenceClickListener(this);
		
		setResult(RESULT_CANCELED);
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		
		setRangeEntries();
		sharedPrefs.registerOnSharedPreferenceChangeListener(this);
	}
	
	/**
	 * Set entri range berdasarkan unit
	 */
	private void setRangeEntries() {
		if (prefs.getUnit() == Unit.METRIC) {
			rangePref.setEntries(R.array.range_metric_entries);
		} else {
			rangePref.setEntries(R.array.range_us_entries);
		}
	}
	
	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
		sharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		String key = preference.getKey();
		Log.d(TAG, "key=" + key);
		
		if (key.equals(Prefs.INTERVAL)) {
			// Peringatan interval terlalu sering akan membebani perangkat
			if (!prefs.isDialogShown(Prefs.DIALOG_INTERVAL)) return false;
			
			ContextThemeWrapper dialogContext = new ContextThemeWrapper(
					preference.getContext(), android.R.style.Theme_Dialog);
			
			AlertDialog.Builder builder = new WarnDialogBuilder(dialogContext, 
					Prefs.DIALOG_INTERVAL);
			builder.setTitle(R.string.dialog_warn_interval_title);
			builder.setMessage(R.string.dialog_warn_interval_msg);
			
			AlertDialog dialog = builder.create();
			dialog.show();
			
			return true;
		
		} else if (key.equals(Prefs.LOC_MANUAL)) {
			Intent intent = new Intent(this, ManualLocationActivity.class);
    		startActivityForResult(intent, LOC_MANUAL_REQUEST);
    		
		} else if (key.equals(Prefs.NOTIFY)) {
			Intent intent = new Intent(this, PrefsNotifyActivity.class);
    		startActivityForResult(intent, NOTIFY_REQUEST);
    		
		} else if (key.equals(Prefs.SMS)) {
			Intent intent = new Intent(this, PrefsSmsActivity.class);
    		startActivityForResult(intent, SMS_REQUEST);
    		
		} else if (key.equals(Prefs.FACEBOOK)) {
			Intent intent = new Intent(this, PrefsFacebookActivity.class);
    		startActivityForResult(intent, FACEBOOK_REQUEST);

		} else if (key.equals(Prefs.TWITTER)) {
			Intent intent = new Intent(this, PrefsTwitterActivity.class);
    		startActivityForResult(intent, TWITTER_REQUEST);
    		
		} else if (key.equals(Prefs.MAIL)){
			Intent intent = new Intent(this, PrefsMailActivity.class);
			startActivityForResult(intent, MAIL_REQUEST);

		} else if (key.equals(Prefs.SOCIAL_CONNECT)) {
			Intent socialIntent = new Intent(this, SocialConnectActivity.class);
			startActivity(socialIntent);
			return true;
			
		} else if (key.equals(Prefs.CONTACT)) {
			Intent contactIntent = new Intent(this, ContactActivity.class);
			startActivity(contactIntent);
			return true;
			
		} else if (key.equals(Prefs.DIALOG)) {
			Intent intent = new Intent(this, PrefsDialogActivity.class);
    		startActivityForResult(intent, DIALOG_REQUEST);
		}
		
		return false;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "Activity result: request=" + requestCode + ", result=" + resultCode);
		prefsResult |= resultCode;
		setResult(prefsResult);
		
		switch(requestCode) {
		case LOC_MANUAL_REQUEST:
			break;
		case NOTIFY_REQUEST:
			break;
		case SMS_REQUEST:
			break;
		case FACEBOOK_REQUEST:
			break;
		case TWITTER_REQUEST:
			break;
		case DIALOG_REQUEST:
			break;
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.d(TAG, "Preference berubah: " + key);
		
		if (key.equals(Prefs.THEME)) {
			prefsResult |= RESULT_RESTART;
			
		} else if (key.equals(Prefs.MIN_MAG) || key.equals(Prefs.MAX_AGE)) {
			prefsResult |= RESULT_REFRESH;
		
		} else if (key.equals(Prefs.UNIT)) {
			setRangeEntries();
			prefsResult |= RESULT_RESTART;
		
		} else if (key.equals(Prefs.INTERVAL)) {
			sendBroadcast(new Intent(RefreshReceiver.SCHEDULE, null, this, 
					RefreshReceiver.class));
		
		} else if (key.equals(Prefs.AUTO_UPDATE)) {
			if (prefs.isAutoUpdate()) {
				sendBroadcast(new Intent(RefreshReceiver.SCHEDULE, null, this, 
						RefreshReceiver.class));
			} else {
				sendBroadcast(new Intent(RefreshReceiver.CANCEL, null, this, 
						RefreshReceiver.class));
			}
		}
		
		setResult(prefsResult);
	}
	
}
