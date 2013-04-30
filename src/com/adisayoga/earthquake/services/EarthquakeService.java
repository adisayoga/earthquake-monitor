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
 
package com.adisayoga.earthquake.services;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.adisayoga.earthquake.R;
import com.adisayoga.earthquake.dto.EarthquakeDTO;
import com.adisayoga.earthquake.dto.LocationType;
import com.adisayoga.earthquake.models.ContactModel;
import com.adisayoga.earthquake.models.EarthquakeModel;
import com.adisayoga.earthquake.models.UsgsSource;
import com.adisayoga.earthquake.providers.EarthquakeColumns;
import com.adisayoga.earthquake.providers.EarthquakeProvider;
import com.adisayoga.earthquake.receivers.EarthquakeReceiver;
import com.adisayoga.earthquake.utils.BaseLocationListener;
import com.adisayoga.earthquake.utils.LocationFinder;
import com.adisayoga.earthquake.wrapper.EarthquakeFacebook;
import com.adisayoga.earthquake.wrapper.EarthquakeMail;
import com.adisayoga.earthquake.wrapper.EarthquakeNotification;
import com.adisayoga.earthquake.wrapper.EarthquakeSms;
import com.adisayoga.earthquake.wrapper.EarthquakeTemplate;
import com.adisayoga.earthquake.wrapper.EarthquakeTwitter;
import com.adisayoga.earthquake.wrapper.Prefs;

/**
 * Service untuk mengecek data gempa baru, dan juga untuk notifikasi, share
 * ke jejaring sosial (facebook dan twitter), sms, dan email.
 * 
 * @author Adi Sayoga
 */
public class EarthquakeService extends IntentService {

	private static final String TAG = "EarthquakeService";
	private static final String NAME = "EarthquakeService";
	private static Location location;
	
	private Prefs prefs;
	private EarthquakeFacebook facebook;
	private EarthquakeTwitter twitter;
	private EarthquakeMail mail;
	private LocationFinder locationFinder;
	private final Handler handler = new Handler();
	
	public EarthquakeService() {
		super(NAME);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		prefs = Prefs.getInstance(this);
		facebook = new EarthquakeFacebook(this);
		twitter = new EarthquakeTwitter(this);
		mail = new EarthquakeMail(this);
		
		// Menentukan lokasi kita saat ini
		locationFinder = new LocationFinder(this);
		location = getLocation();
	}

	/**
	 * Mendapatkan lokasi (deteksi atau manual) sesuai dengan preference. 
	 * Jika dapat mendeteksi lokasi akan digunakan lokasi terakhir didapat, 
	 * atau jika tidak lokasi manual yang digunakan.
	 * 
	 * @return Lokasi
	 */
	private Location getLocation() {
    	locationFinder.setChangedLocationListener(locationListener);
    	
    	Location location;
    	if (prefs.isDetectLocation()) {
    		// Mendapatkan lokasi melalui GPS akan memerlukan waktu, jadi disini 
    		// kita akan mendapatkan lokasi user terakhir.
	    	location = locationFinder.getLastLocation(LocationFinder.MAX_DISTANCE, 
	    			System.currentTimeMillis() + prefs.getInterval());
    	} else {
    		location = prefs.getManualLocation();
    	}
    	
    	return location;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// Hapus data lama
		deleteOldQuakes(prefs.getMaxAge());
		
		try {
			// Mendapatkan data dari USGS
			long lastUpdate = prefs.getLastUpdate();
			float minMagnitude = prefs.getMinMagnitude();
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss");
			Log.i(TAG, "Merefresh data... Last update=" + sdf.format(lastUpdate));
			List<EarthquakeDTO> quakes = UsgsSource.read(lastUpdate, minMagnitude);
			// Kita sudah selesai mendapatkan data, simpan terakhir kali diupdate
			prefs.setLastUpdate(System.currentTimeMillis());
			
			if (quakes != null && quakes.size() > 0) {
				// Terdapat data pada server, filter data ini sehingga data yang
				// didapat merupakan data yang benar-benar baru
				Log.d(TAG, "Data pada server: " + quakes.size() + " items");
				quakes = getNewQuakes(quakes);
			}
			
			if (quakes != null && quakes.size() > 0) {
				// Terdapat data, simpan ke provider, beritahukan ada gempa baru 
				// (jika sesuai dengan minimal magnitudo pengaturan), dan kirim 
				// broadcast terdapat data baru
				Log.d(TAG, "Terdapat data baru: " + quakes.size() + " items");
				addNewQuakes(quakes);
				notifyNewQuake(quakes);
				sendBroadcast(new Intent(EarthquakeReceiver.NEW_QUAKE_FOUND));
			} else {
				// Tidak ada data baru, kirim broadcast tidak ada data baru
				Log.d(TAG, "Tidak ada data yang perlu di-update");
				sendBroadcast(new Intent(EarthquakeReceiver.NO_NEW_QUAKE));
			}
			
		} catch (IOException e) {
			// Terdapat error, kirim broadcast jaringan error
			Log.w(TAG, "Gagal mendapatkan data dari server");
			sendBroadcast(new Intent(EarthquakeReceiver.NETWORK_ERROR));
		}
	}
	
	/**
	 * Menghapus data gempa yang lebih lama dari age yang ditentukan.
	 * 
	 * @param age Umur dalam milisecond
	 * @return Jumlah data yang dihapus
	 */
	private int deleteOldQuakes(long age) {
		EarthquakeModel table = new EarthquakeModel(this);
		return table.deleteQuakes(age);
	}
	
	/**
	 * Mendapatkan data yang tidak ada pada provider.
	 * 
	 * @param quakes Data gempa
	 * @return Data baru
	 */
	private List<EarthquakeDTO> getNewQuakes(List<EarthquakeDTO> quakes) {
		List<EarthquakeDTO> newQuakes = new ArrayList<EarthquakeDTO>();
		
		ContentResolver resolver = getContentResolver();
		String selection = EarthquakeColumns.DATE + " = ?";
		
		for (EarthquakeDTO quake : quakes) {
			String[] selectionArgs = new String[] { Long.toString(quake.time) };
			
			Cursor cursor = resolver.query(EarthquakeProvider.CONTENT_URI, null, 
					selection, selectionArgs, null);
			if (cursor.getCount() == 0) newQuakes.add(quake);
			cursor.close();
		}
		
		return newQuakes;
	}
	
	/**
	 * Menambahkan data gempa baru ke provider.
	 * 
	 * @param quakes List data gempa
	 * @return List gempa yang ditambahkan dengan id tabelnya
	 */
	private List<EarthquakeDTO> addNewQuakes(List<EarthquakeDTO> quakes) {
		Log.d(TAG, "Menyimpan data... ");
		ContentResolver resolver = getContentResolver();
		
		for (EarthquakeDTO quake : quakes) {
			// Simpan ke provider
			ContentValues values = new ContentValues();
			values.put(EarthquakeColumns.SRC, quake.source);
			values.put(EarthquakeColumns.EQID, quake.eqid);
			values.put(EarthquakeColumns.VERSION, quake.version);
			
			values.put(EarthquakeColumns.DATE, quake.time);
			values.put(EarthquakeColumns.LATITUDE, quake.latitude);
			values.put(EarthquakeColumns.LONGITUDE, quake.longitude);
			values.put(EarthquakeColumns.MAGNITUDE, quake.magnitude);
			values.put(EarthquakeColumns.DEPTH, quake.depth);
			values.put(EarthquakeColumns.NST, quake.nst);
			values.put(EarthquakeColumns.REGION, quake.region);
			
			Uri uri = resolver.insert(EarthquakeProvider.CONTENT_URI, values);
			
			// Mendapatkan id dari data yang baru saja diinsert
			long segment = Long.parseLong(uri.getPathSegments().get(1));
			quake.id = segment;
		}
		
		return quakes;
	}
	
	/**
	 * Memberitahukan bahwa terdapat gempa baru (notifikasi, kirim SMS, share ke 
	 * Facebook).
	 * <p>
	 * Disini dilakukan filter yang sesuai dengan minimal magnitudo pengaturan untuk
	 * masing-masing pemberitahuan.
	 * 
	 * @param quakes List gempa
	 */
	private void notifyNewQuake(List<EarthquakeDTO> quakes) {
		if (quakes == null || quakes.size() == 0) return;
		
		// Load preference
		boolean isNotify = prefs.isNotifySend();
		boolean isSmsSend = prefs.isSmsSend();
		boolean isFacebookSend = prefs.isFacebookSend();
		boolean isTwitterSend = prefs.isTwitterSend();
		boolean isMailSend = prefs.isMailSend();
		
		float notifyMinMagReg = prefs.getNotifyMinMagnitude(LocationType.REGIONAL);
		float notifyMinMagGlobal = prefs.getNotifyMinMagnitude(LocationType.GLOBAL);
		float facebookMinMagReg = prefs.getFacebookMinMagnitude(LocationType.REGIONAL);
		float facebookMinMagGlobal = prefs.getFacebookMinMagnitude(LocationType.GLOBAL);
		float twitterMinMagReg = prefs.getTwitterMinMagnitude(LocationType.REGIONAL);
		float twitterMinMagGlobal = prefs.getTwitterMinMagnitude(LocationType.GLOBAL);
		float mailMinMagReg = prefs.getMailMinMagnitude(LocationType.REGIONAL);
		float mailMinMagGlobal = prefs.getMailMinMagnitude(LocationType.GLOBAL);
		float smsMinMagReg = prefs.getSmsMinMagnitude(LocationType.REGIONAL);
		float smsMinMagGlobal = prefs.getSmsMinMagnitude(LocationType.GLOBAL);
		
		float prefMinMag = prefs.getMinMagnitude();
		int prefRange = prefs.getRange();
		
		// Variable untuk menentukan data gempa yang paling besar dan diprioritaskan
		// untuk lokasi regional
		float lastMagReg = 0;
		float lastMagGlobal = 0;
		int quakeCount = 0;
		
		// Variable untuk menyimpan data
		EarthquakeDTO quakeNotify = null;
		List<EarthquakeDTO> quakesFacebook = new ArrayList<EarthquakeDTO>();
		List<EarthquakeDTO> quakesTwitter = new ArrayList<EarthquakeDTO>();
		List<EarthquakeDTO> quakesMail = new ArrayList<EarthquakeDTO>();
		List<EarthquakeDTO> quakesSms = new ArrayList<EarthquakeDTO>();
		
		for (EarthquakeDTO quake : quakes) {
			float magnitude = quake.magnitude;
			if (magnitude < prefMinMag) continue;
			
			// Jarak dari masing-masing gempa berada pada range regional atau tidak
			boolean inRange = false;
			if (location != null) {
				float distance = quake.getLocation().distanceTo(location);
				inRange = distance <= prefRange;
			}
			
			// Notifikasi status bar, diprioritaskan untuk regional
			if (isNotify) {
				if (inRange && notifyMinMagReg <= magnitude) {
					quakeCount++;
					if (lastMagReg < magnitude) {
						quakeNotify = quake;
						lastMagReg = magnitude;
					}
				} else if (notifyMinMagGlobal <= magnitude) {
					quakeCount++;
					if (lastMagGlobal < magnitude) {
						// Jika belum ada notifikasi regional maka ini yang dipakai, 
						// jika tidak maka biarkan yang regional
						if (lastMagReg == 0) quakeNotify = quake;
						lastMagGlobal = magnitude;
					}
				}
			}
			
			// Share ke Facebook
			if (isFacebookSend) addQuakeNotify(quakesFacebook, quake, inRange, magnitude, 
					facebookMinMagReg, facebookMinMagGlobal);
			
			// Share ke Twitter
			if (isTwitterSend) addQuakeNotify(quakesTwitter, quake, inRange, magnitude, 
					twitterMinMagReg, twitterMinMagGlobal);
			
			// Kirim email
			if (isMailSend) addQuakeNotify(quakesMail, quake, inRange, magnitude, 
					mailMinMagReg, mailMinMagGlobal);

			// SMS
			if (isSmsSend) addQuakeNotify(quakesSms, quake, inRange, magnitude, 
					smsMinMagReg, smsMinMagGlobal);
			
		}
		
		// Tampilkan notifikasi, dan/atau share
		if (quakeCount > 0) sendNotification(quakeNotify, quakeCount);
		if (quakesFacebook != null && quakesFacebook.size() > 0) shareToFacebook(quakesFacebook);
		if (quakesTwitter != null && quakesTwitter.size() > 0) shareToTwitter(quakesTwitter);
		if (quakesMail != null && quakesMail.size() > 0) sendMail(quakesMail);
		if (quakesSms != null && quakesSms.size() > 0) sendSms(quakesSms);
	}
	
	/**
	 * Tambahkan data gempa dengan filter minimal magnitudo regional dan global.
	 * 
	 * @param toAdd Data gempa yang ditambahkan
	 * @param quake Data gempa sumber
	 * @param inRange True untuk dalam jarak regional, false untuk global
	 * @param magnitude Magnitudo gempa
	 * @param minMagReg Minimal magnitudo regional
	 * @param minMagGlobal Minimal magnitudo global
	 */
	private void addQuakeNotify(List<EarthquakeDTO> toAdd, EarthquakeDTO quake, 
			boolean inRange, float magnitude, float minMagReg, float minMagGlobal) {
		if ((inRange && minMagReg <= magnitude) || (minMagGlobal <= magnitude)) {
			toAdd.add(quake);
		}
	}
	
	/**
	 * Kirim notifikasi.
	 * 
	 * @param quake Data gempa
	 * @param quakeCount Jumlah gempa
	 */
	private void sendNotification(EarthquakeDTO quake, int quakeCount) {
		Log.d(TAG, "Mengirim notifikasi...");
		
		boolean isFlash = prefs.isNotifyFlash();
		boolean isAlert = prefs.isNotifyAlert();
		Uri alertSound = prefs.getNotifyAlertSound();
		boolean isVibrate = prefs.isNotifyVibrate();
		
		EarthquakeNotification notifier = new EarthquakeNotification(
				this, quake, quakeCount, isAlert, alertSound, isFlash, isVibrate);
		notifier.alert();
	}
	
	/** 
	 * Share ke Facebook. Dilakukan hanya jika sudah login. 
	 * <p>
	 * Sebagai catatan, service berjalan pada background, sehingga kita tidak perlu 
	 * menampilkan dialog login, karena itu mungkin akan menjengkelkan user.
	 * 
	 * @param quakes Data gempa
	 */
	private void shareToFacebook(List<EarthquakeDTO> quakes) {
		if (!facebook.isSessionValid()) return;
		Log.d(TAG, "Share ke Facebook...");
		
		// Post setiap list quake
		boolean postSent = false;
		for (EarthquakeDTO quake : quakes) {
			Bundle params = facebook.genereateParams(quake, null, location);
			postSent |= facebook.postMessage(params);
		}
		
		showMessage((postSent) ? R.string.facebook_post_sent 
				: R.string.facebook_post_fail);
	}

	/** 
	 * Share ke Twitter. Dilakukan hanya jika sudah login. 
	 * <p>
	 * Sebagai catatan, service berjalan pada background, sehingga kita tidak perlu 
	 * menampilkan dialog login, karena itu mungkin akan menjengkelkan user.
	 * <p>
	 * Pesan hanya akan dikirim sekali saja, tidak dapat dikirim berulang-ulang
	 * untuk setiap data gempa karena akan terdapat error update limit.
	 * Twitte juga dibatasi 140 karakter, jadi pesan yang dikirim akan dipotong.
	 * <p>
	 * TODO: Ada cara lebih baik?
	 * <p>
	 * 
	 * @param quakes Data gempa
	 */
	private void shareToTwitter(final List<EarthquakeDTO> quakes) {
		Log.d(TAG, "Share ke Twitter...");
		
		twitter.login(null, new EarthquakeTwitter.AuthListener() {
			@Override
			public void onAuthComplete() {
				String message = "";
				for (EarthquakeDTO quake : quakes) {
					if (message != "") message += ", ";
					message += twitter.getPostMessage(quake, location);
				}
				// Message maksimal 140 karakter
				message = message.substring(0, 137) + "...";
				boolean postSent = twitter.postMessage(message);
				
				showMessage((postSent) ? R.string.twitter_post_sent 
						: R.string.twitter_post_fail);
			}

			@Override
			public void onAuthFail() {
				showMessage(R.string.twitter_post_fail);
			}
		});
	}
	
	/**
	 * Kirim pesan ke email.
	 * 
	 * @param quakes Data gempa
	 */
	private void sendMail(List<EarthquakeDTO> quakes) {
		try {
			Log.d(TAG, "Mengirim Email...");
			
			Context context = this;
			mail.setFrom(prefs.getMailUsername());
			ContactModel table = new ContactModel(this);
			String[] mails = table.getMails();
			mail.setTo(mails);
			mail.setSubject(context.getString(R.string.app_name));
			String message = EarthquakeTemplate.getInstance(context).getMessage(
	    			prefs.getMailTemplate(context), 
	    			prefs.getMailTemplateDetail(context), 
	    			quakes, location);
			mail.setBody(message);
			boolean mailSent = mail.send();
			
			showMessage((mailSent) ? R.string.mail_sent : R.string.mail_fail);
			
		} catch (Exception e) {
			showMessage(R.string.mail_fail);
			Log.e(TAG, e.getMessage(), e);
		}
	}

	/**
	 * Kirim pesan SMS.
	 * 
	 * @param quakes Data gempa
	 */
	private void sendSms(List<EarthquakeDTO> quakes) {
		Log.d(TAG, "Mengirim sms...");
		Context context = this;
		String message = EarthquakeTemplate.getInstance(context).getMessage(
    			prefs.getSmsTemplate(context), prefs.getSmsTemplateDetail(context), 
    			quakes, location);
		
		ContactModel table = new ContactModel(this);
		String[] phones = table.getPhones();
		EarthquakeSms sms = new EarthquakeSms(this);
		List<String> phonesSent = sms.sendTextMessage(phones, message, 
				EarthquakeSms.SPLIT_SMS_MESSAGE);
		
		boolean smsSent = phonesSent != null && phonesSent.size() > 0;
		showMessage((smsSent) ? R.string.sms_sent : R.string.sms_fail);
	}
	
	/**
	 * Tampilkan pesan menggunakan Toast.
	 * 
	 * @param resId Resource id
	 */
	private void showMessage(final int resId) {
		// Tampilkan pesan pada UI thread. 
		// Toast tidak dapat berjalan pada bacground thread
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(EarthquakeService.this, resId, Toast.LENGTH_SHORT);
			}
		});
	}
	
	private final LocationListener locationListener = new BaseLocationListener() {
		@Override
		public void onLocationChanged(Location newLocation) {
			location = newLocation;
		}
	};
}
