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

package com.adisayoga.earthquake.wrapper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;

import com.adisayoga.earthquake.R;
import com.adisayoga.earthquake.dto.EarthquakeDTO;
import com.adisayoga.earthquake.providers.EarthquakeColumns;
import com.adisayoga.earthquake.receivers.NotificationClickReceiver;

/**
 * Class untuk notifikasi gempa.
 * 
 * @author Adi Sayoga
 */
public class EarthquakeNotification {
	private static final String TAG = "EarthquakeNotification";
	private static final int ALERT_ID = 1;
	
	private final Context context;
	private final EarthquakeDTO quake;
	private final int quakeCount;
	
	private final boolean isAlert;
	private final Uri alertSound;
	private final boolean isFlash;
	private final boolean isVibrate;
	
	public EarthquakeNotification(Context context, EarthquakeDTO quake, int quakeCount,
			boolean isAlert, Uri alertSound, boolean isFlash, boolean isVibrate) {
		
		this.context = context;
		this.quake = quake;
		this.quakeCount = quakeCount;
		
		this.isAlert = isAlert;
		this.alertSound = alertSound;
		this.isFlash = isFlash;
		this.isVibrate = isVibrate;
	}
	
	/**
	 * Tampilkan notifikasi.
	 */
	public void alert() {
		if (quake == null) return;
		
		// TODO Ganti icon status bar yang sesuai
		int icon = android.R.drawable.stat_sys_warning;
		CharSequence tickerText = getTickerText();
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, tickerText, when);

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		if (quakeCount > 1) notification.number = quakeCount;
		
		// Alert sound
		if (isAlert) notification.sound = alertSound;
		
		// Flash
		if (isFlash) {
			notification.flags |= Notification.FLAG_SHOW_LIGHTS;
			notification.ledOffMS = 250;
			notification.ledOnMS = 500;
			notification.ledARGB = Color.parseColor("#ff0000");
		}
		
		// Getar sesuai dengan kekuatan gempa
		if (isVibrate) {
			double vibrateLength = 100 * Math.exp(0.53 * quake.magnitude);
			long[] vibrate = new long[] { 0, 100, 100, (long) vibrateLength };
			notification.vibrate = vibrate;
		}
		
		CharSequence contentTitle = context.getText(R.string.new_quake_title);
		CharSequence contentText = getContentText();
		
		Intent intent = new Intent(context, NotificationClickReceiver.class);
		intent.putExtra(EarthquakeColumns.TABLE_NAME, quake);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		
		notification.setLatestEventInfo(context, contentTitle, contentText, pendingIntent);
		NotificationManager manager = (NotificationManager) context.getSystemService(
				Context.NOTIFICATION_SERVICE);
		manager.notify(ALERT_ID, notification);
		
		Log.i(TAG, "Alert sent: " + contentText);
	}
	
	/**
	 * Mendapatkan teks yang akan ditampilkan pada status bar saat notifikasi
	 * pertama kali aktif.
	 * 
	 * @return Ticker teks
	 */
	private CharSequence getTickerText() {
		String message = (String) context.getText(R.string.tpl_new_quake_ticker);
		message = message.replace(Prefs.TPL_MAGNITUDE, Float.toString(quake.magnitude));
		message = message.replace(Prefs.TPL_REGION, quake.region);
		return message;
	}
	
	/**
	 * Teks yang akan ditampilkan saat entry diperluas.
	 * 
	 * @return Teks
	 */
	private CharSequence getContentText() {
		String message;
		if (quakeCount > 1) {
			message = (String) context.getText(R.string.tpl_new_quake_contents);
		} else {
			message = (String) context.getText(R.string.tpl_new_quake_content);
		}
		message = message.replace(Prefs.TPL_MAGNITUDE, Float.toString(quake.magnitude));
		message = message.replace(Prefs.TPL_REGION, quake.region);
		message = message.replace(Prefs.TPL_COUNT, Integer.toString(quakeCount - 1));
		
		return message;
	}

	/**
	 * Batalkan notifikasi.
	 */
	public void cancel() {
		NotificationManager manager = (NotificationManager) context.getSystemService(
				Context.NOTIFICATION_SERVICE);
		manager.cancel(ALERT_ID);
	}
	
}
