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

import com.adisayoga.earthquake.wrapper.Prefs;

/**
 * Receiver pada saat sistem selesai boot.
 * 
 * @author Adi Sayoga
 */
public class BootReceiver extends BroadcastReceiver {

	private static final String TAG = "BootReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Prefs prefs = Prefs.getInstance(context);
		// Abaikan jika preference disetting tidak start saat boot
		if (!prefs.isBootStart()) return;
		
		// Abaikan jika tidak update otomatis
		if (!prefs.isAutoUpdate()) return;
		
		Log.d(TAG, "Schedule service saat boot");
		context.sendBroadcast(new Intent(RefreshReceiver.SCHEDULE, null, context, 
				RefreshReceiver.class));
	}

}
