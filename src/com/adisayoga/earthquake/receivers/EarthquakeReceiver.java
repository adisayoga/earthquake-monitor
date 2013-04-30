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
import android.os.Handler;
import android.util.Log;

/**
 * Receiver ada/tidaknya data gempa baru.
 * 
 * @author Adi Sayoga
 */
public class EarthquakeReceiver extends BroadcastReceiver {
	
	private static final String TAG = "EarthquakeReceiver";
	
	public static final String NEW_QUAKE_FOUND = "new_quake_found";
	public static final String NO_NEW_QUAKE = "no_new_quake";
	public static final String NETWORK_ERROR = "network_error";
	
	public static final int NEW_QUAKE_FOUND_WHAT = 1;
	public static final int NO_NEW_QUAKE_WHAT = 2;
	public static final int NETWORK_ERROR_WHAT = 3;
	
	private final Handler handler;
	
	public EarthquakeReceiver(Handler handler) {
		this.handler = handler;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.d(TAG, "action=" + action);
		
		if (action.equals(NEW_QUAKE_FOUND)) {
			handler.sendEmptyMessage(NEW_QUAKE_FOUND_WHAT);
		} else if (action.equals(NO_NEW_QUAKE)) {
			handler.sendEmptyMessage(NO_NEW_QUAKE_WHAT);
		} else if (action.equals(NETWORK_ERROR)) {
			handler.sendEmptyMessage(NETWORK_ERROR_WHAT);
		}
	}

}
