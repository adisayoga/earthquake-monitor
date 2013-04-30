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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.adisayoga.earthquake.services.EarthquakeService;
import com.adisayoga.earthquake.wrapper.Prefs;

/**
 * Receiver untuk memulai/menjadwalkan refresh otomatis, atau untuk 
 * membatalkannya.
 * 
 * @author Adi Sayoga
 */
public class RefreshReceiver extends BroadcastReceiver {
	
	private static final String TAG = "RefreshReceiver";

	public static final String REFRESH = "refresh";
	public static final String SCHEDULE = "schedule";
	public static final String CANCEL = "cancel";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.d(TAG, "action=" + action);
		
		if (action.equals(REFRESH)) {
			context.startService(new Intent(context, EarthquakeService.class));
			
		} else if (action.equals(SCHEDULE)) {
			long interval = Prefs.getInstance(context).getInterval();
			schedule(context, interval);
		
		} else if (action.equals(CANCEL)) {
			cancel(context);
		}
	}
	
	/**
	 * Jadwalkan refresh otomatis.
	 * 
	 * @param context Context
	 * @param interval Interval refresh dalam milisecond
	 */
	private void schedule(Context context, long interval) {
		AlarmManager manager = (AlarmManager) context.getSystemService(
				Context.ALARM_SERVICE);
		Intent intent = new Intent(REFRESH, null, context, RefreshReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 
				PendingIntent.FLAG_UPDATE_CURRENT);
		
		// Cancel kemudian schedule
		manager.cancel(pendingIntent);
		manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 
				SystemClock.elapsedRealtime() + interval, interval, pendingIntent);
	}
	
	/**
	 * Batalkan refresh otomatis.
	 * 
	 * @param context Context
	 */
	private void cancel(Context context) {
		AlarmManager manager = (AlarmManager) context.getSystemService(
				Context.ALARM_SERVICE);
		Intent intent = new Intent(REFRESH, null, context, RefreshReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		manager.cancel(pendingIntent);
		
		context.stopService(new Intent(context, EarthquakeService.class));
	}

}
