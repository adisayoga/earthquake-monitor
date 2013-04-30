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
 
package com.adisayoga.earthquake.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.adisayoga.earthquake.dto.EarthquakeDTO;
import com.adisayoga.earthquake.providers.EarthquakeColumns;
import com.adisayoga.earthquake.ui.EarthquakeDetailActivity;

/**
 * Receiver saat notifikasi diklik.
 * 
 * @author Adi Sayoga
 */
public class NotificationClickReceiver extends BroadcastReceiver {
	
	private static final String TAG = "NotificationClickReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		EarthquakeDTO quake = null;
		// Ambil data gempa di extra
		if (intent.hasExtra(EarthquakeColumns.TABLE_NAME)) {
			quake = (EarthquakeDTO) intent.getExtras().get(EarthquakeColumns.TABLE_NAME);
		}
		if (quake != null) {
			// Terdapat data, tampilkan detail gempa
			Intent detailIntent = new Intent(context, EarthquakeDetailActivity.class);
			detailIntent.putExtra(EarthquakeColumns.TABLE_NAME, quake);
			detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
					            | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			context.startActivity(detailIntent);
		} else {
			// Data gempa tidak ada, tidak ada yang perlu dilakukan
			Log.w(TAG, "Tidak terdapat data gempa bumi");
		}
	}
}
