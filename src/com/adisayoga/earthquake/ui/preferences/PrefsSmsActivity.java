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

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.adisayoga.earthquake.R;
import com.adisayoga.earthquake.ui.SmsTemplateActivity;
import com.adisayoga.earthquake.wrapper.Prefs;

/**
 * Activity class preference mengirim SMS.
 * 
 * @author Adi Sayoga
 */
public class PrefsSmsActivity extends PreferenceActivity 
	implements OnPreferenceClickListener {

	public static String TAG = "PrefsSMSActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		
		Prefs prefs = Prefs.getInstance(this);
		setTheme(prefs.getTheme().resId);
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.prefs_sms);
		
		Preference smsTemplate = findPreference(Prefs.SMS_TEMPLATE);
		smsTemplate.setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		String key = preference.getKey();
		Log.d(TAG, "key=" + key);
		
		if (key.equals(Prefs.SMS_TEMPLATE)) {
			Intent templateIntent = new Intent(this, SmsTemplateActivity.class);
			startActivity(templateIntent);
			return true;
		}
		
		return false;
	}

}
