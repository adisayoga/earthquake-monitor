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

import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.adisayoga.earthquake.R;
import com.adisayoga.earthquake.dto.EarthquakeTheme;
import com.adisayoga.earthquake.dto.LocationType;
import com.adisayoga.earthquake.dto.Unit;
import com.adisayoga.earthquake.utils.LocationUtils;

/**
 * Class setter dan getter preference.
 * 
 * @author Adi Sayoga
 */
public class Prefs {

	// Display
	public static final String MIN_MAG = "min_mag";
	public static final String AUTO_UPDATE = "auto_update";
	public static final String INTERVAL = "interval";
	public static final String MAX_AGE = "max_age";
	public static final String UNIT = "unit";
	public static final String THEME = "theme";
	
	// Lokasi
	public static final String LOC_DETECT = "loc_detect";
	public static final String LOC_MANUAL = "loc_manual";
	public static final String LOC_MANUAL_LATITUDE = "loc_manual_latitude";
	public static final String LOC_MANUAL_LONGITUDE = "loc_manual_longitude";
	
	// Notifikasi
	public static final String RANGE = "range";
	
	public static final String NOTIFY = "notify";
	public static final String NOTIFY_SEND = "notify_send";
	public static final String NOTIFY_MIN_MAG = "notify_min_mag";
	public static final String NOTIFY_ALERT = "notify_alert";
	public static final String NOTIFY_ALERT_SOUND = "notify_alert_sound";
	public static final String NOTIFY_FLASH = "notify_flash";
	public static final String NOTIFY_VIBRATE = "notify_vibrate";
	
	public static final String FACEBOOK = "facebook";
	public static final String FACEBOOK_SEND = "facebook_send";
	public static final String FACEBOOK_MIN_MAG = "facebook_min_mag";
	
	public static final String TWITTER = "twitter";
	public static final String TWITTER_SEND = "twitter_send";
	public static final String TWITTER_MIN_MAG = "twitter_min_mag";
	public static final String TWITTER_TEMPLATE = "twitter_template";
	public static final String TWITTER_ACCESS_TOKEN = "twitter_access_token";
	public static final String TWITTER_ACCESS_TOKEN_SECRET = "twitter_access_token_secret";
	
	public static final String SOCIAL_CONNECT = "social_connect";
	public static final String CONTACT = "contact";
	
	public static final String MAIL = "mail";
	public static final String MAIL_SEND = "mail_send";
	public static final String MAIL_MIN_MAG = "mail_min_mag";
	public static final String MAIL_CONNECT = "mail_connect";
	public static final String MAIL_LOGIN = "mail_login";
	public static final String MAIL_HOST = "mail_host";
	public static final String MAIL_PORT = "mail_port";
	public static final String MAIL_SPORT = "mail_sport";
	public static final String MAIL_USERNAME = "mail_username";
	public static final String MAIL_PASS = "mail_pass";
	public static final String MAIL_TEMPLATE = "mail_template";
	public static final String MAIL_TEMPLATE_DETAIL = "mail_template_detail";

	public static final String SMS = "sms";
	public static final String SMS_SEND = "sms_send";
	public static final String SMS_MIN_MAG = "sms_min_mag";
	public static final String SMS_TEMPLATE = "sms_template";
	public static final String SMS_TEMPLATE_DETAIL = "sms_template_detail";
	
	// Lainnya
	public static final String BOOT_START = "boot_start";
	public static final String DIALOG = "dialog";
	public static final String DIALOG_STARTUP = "dialog_startup";
	public static final String DIALOG_INTERVAL = "dialog_interval";
	public static final String LAST_UPDATE = "last_update";
	public static final String ZOOM_TO_FIT = "zoom_to_fit";
	
	// Tags
	public static final String TPL_DETAILS = "{details}";
	public static final String TPL_REGION = "{region}";
	public static final String TPL_MAGNITUDE = "{magnitude}";
	public static final String TPL_DATE = "{date}";
	public static final String TPL_LOCATION = "{location}";
	public static final String TPL_DEPTH = "{depth}";
	public static final String TPL_DISTANCE = "{distance}";
	public static final String TPL_COUNT = "{count}";
	
	// Default value
	private static final String DEF_MIN_MAG = "3";
	private static final String DEF_MIN_MAG_NOTIFY_REG = "5";
	private static final String DEF_MIN_MAG_NOTIFY_GLOBAL = "6";
	private static final String DEF_MIN_MAG_SMS_REG = "7";
	private static final String DEF_MIN_MAG_SMS_GLOBAL = "8";
	private static final String DEF_MIN_MAG_FACEBOOK_REG = "6";
	private static final String DEF_MIN_MAG_FACEBOOK_GLOBAL = "7";
	private static final String DEF_MIN_MAG_TWITTER_REG = "6";
	private static final String DEF_MIN_MAG_TWITTER_GLOBAL = "7";
	private static final String DEF_MIN_MAG_MAIL_REG = "6";
	private static final String DEF_MIN_MAG_MAIL_GLOBAL = "7";
	
	private static Prefs instance = null;
	private static SharedPreferences prefs;
	
	private Prefs() {
	}
	
	public static Prefs getInstance(Context context) {
		if (instance == null) {
			instance = new Prefs();
			Prefs.prefs = PreferenceManager.getDefaultSharedPreferences(context);
		}
		return instance;
	}
	
	/**
	 * Mendapatkan SharedPreferences
	 * 
	 * @return SharedPreferences
	 */
	public SharedPreferences getSharedPrefs() {
		return prefs;
	}
	
	/* *********************************************************************** *
	 * Getter/Setter
	 * *********************************************************************** */
	
	/* ----------------------------------------------------------------------- *
	 * Display
	 * ----------------------------------------------------------------------- */
	
	/**
	 * Mendapatkan nilai maginitudo minimal gempa bumi yang akan ditampilkan.
	 *
	 * @return Magnitodo minimal
	 */
	public float getMinMagnitude() {
		return Float.parseFloat(prefs.getString(MIN_MAG, DEF_MIN_MAG));
	}
	
	/**
	 * Menyimpan nilai magnitudo minimal gempa bumi yang akan ditampilkan 
	 * ke preference.
	 * 
	 * @param value Magnitudo minimal
	 */
	public void setMinMagnitude(float value) {
		prefs.edit().putString(MIN_MAG, Float.toString(value))
				.commit();
	}

	/**
	 * Mendapatkan apakah gempa bumi dicek otomatis secara periodik atau tidak.
	 * 
	 * @return True jika dicek secara periodik
	 */
	public boolean isAutoUpdate() {
		return prefs.getBoolean(AUTO_UPDATE, false);
	}

	/**
	 * Menentukan apakah gempa bumi dicek otomatis secara periodik atau tidak.
	 * 
	 * @param value True jika dicek secara periodik
	 */
	public void setAutoUpdate(boolean value) {
		prefs.edit().putBoolean(AUTO_UPDATE, value).commit();
	}
	
	/**
	 * Mendapatkan seberapa sering gempa bumi baru dicek secara periodik.
	 * 
	 * @return Interval dalam milisecond
	 */
	public long getInterval() {
		String interval = prefs.getString(INTERVAL, Long.toString(
				AlarmManager.INTERVAL_FIFTEEN_MINUTES));
		return Long.parseLong(interval);
	}

	/**
	 * Menyimpan seberapa sering gempa bumi baru dicek secara periodik.
	 * 
	 * @param value Interval dalam milisecond
	 */
	public void setInterval(int value) {
		prefs.edit().putString(INTERVAL, Integer.toString(value)).commit();
	}
	
	/**
	 * Mendapatkan berapa hari data akan disimpan dalam database. Gempa bumi
	 * lebih lama dari ini akan dihapus.
	 * 
	 * @return Hari dalam milisecond
	 */
	public long getMaxAge() {
		// dikali 1 hari (dalam milisecond)
		long age = Integer.parseInt(prefs.getString(MAX_AGE, "30"));
		return age * AlarmManager.INTERVAL_DAY;
	}
	
	/**
	 * Menyimpan berapa hari data akan disimpan dalam database. Gempa bumi
	 * lebih lama dari ini akan dihapus.
	 * 
	 * @param value Hari dalam milisecond
	 */
	public void setMaxAge(long value) {
		// disimpan dalam satuan hari
		prefs.edit().putString(MAX_AGE, Long.toString(value / 
				AlarmManager.INTERVAL_DAY)).commit();
	}
	
	/**
	 * Mendapatkan unit yang digunakan ketika menampilkan jarak.
	 * 
	 * @return Unit dalam Metric/US
	 */
	public Unit getUnit() {
		Unit unit = Unit.valueOf(prefs.getString(UNIT, Unit.METRIC
				.toString()));
		return unit;
	}
	
	/**
	 * Menyimpan unit yang digunakan ketika menampilkan jarak.
	 * 
	 * @param value Unit dalam Metric/US
	 */
	public void setUnit(Unit value) {
		prefs.edit().putString(UNIT, value.toString()).commit();
	}
	
	/**
	 * Mendapatkan tema yang digunakan pada aplikasi ini.
	 * 
	 * @return Tema yang digunakan
	 */
	public EarthquakeTheme getTheme() {
		return EarthquakeTheme.valueOf(prefs.getString(THEME, 
				EarthquakeTheme.LIGHT.toString()));
	}
	
	/**
	 * Menyimpan tema yang digunakan pada aplikasi ini.
	 * 
	 * @param value Tema yang digunakan
	 */
	public void setTheme(EarthquakeTheme value) {
		prefs.edit().putString(THEME, value.toString()).commit();
	}
	
	/* ----------------------------------------------------------------------- *
	 * Lokasi
	 * ----------------------------------------------------------------------- */
	
	/**
	 * Mendapatkan apakah lokasi dapat dideteksi atau tidak.
	 * 
	 * @return True jika lokasi dapat dideteksi
	 */
	public boolean isDetectLocation() {
		return prefs.getBoolean(LOC_DETECT, true);
	}
	
	/**
	 * Menentukan apakah lokasi dapat dideteksi atau tidak.
	 * 
	 * @param value True jika dapat dideteksi
	 */
	public void setDetectLocation(boolean value) {
		prefs.edit().putBoolean(LOC_DETECT, value).commit();
	}
	
	/**
	 * Mendapatkan koordinat lokasi yang diinputkan secara manual.
	 * 
	 * @return Koordinat lokasi manual
	 */
	public Location getManualLocation() {
		Location location = new Location(LocationUtils.CONSTRUCT_PROVIDER);
		location.setLatitude(getManualLatitude());
		location.setLongitude(getManualLongitude());
		return location;
	}
	
	/**
	 * Mendapatkan lokasi latitude yang diinputkan manual.
	 * 
	 * @return Latitude
	 */
	public double getManualLatitude() {
		String latitudeString = prefs.getString(LOC_MANUAL_LATITUDE, "0");
		double latitude = Double.parseDouble(latitudeString);
		return latitude;
	}
	
	/**
	 * Mendapatkan lokasi longitude yang diinputkan manual.
	 * 
	 * @return Longitude
	 */
	public double getManualLongitude() {
		String longitudeString = prefs.getString(LOC_MANUAL_LONGITUDE, "0");
		double longitude = Double.parseDouble(longitudeString);
		return longitude;
	}
	
	/**
	 * Menyimpan koordinat lokasi yang diinputkan secara manual.
	 * 
	 * @param value Koordinat lokasi manual
	 */
	public void setManualLocation(Location value) {
		Double latitude = Double.MIN_VALUE;
		Double longitude = Double.MIN_VALUE;
		
		if (value != null) {
			latitude = value.getLatitude();
			longitude = value.getLongitude();
		}
		
		setManualLocation(latitude, longitude);
	}
	
	/**
	 * Menyimpan koordinat lokasi yang diinputkan secara manual.
	 * 
	 * @param latitude Latitude
	 * @param longitude Longitude
	 */
	public void setManualLocation(Double latitude, Double longitude) {
		Editor editor = prefs.edit();
		editor.putString(LOC_MANUAL_LATITUDE, Double.toString(latitude));
		editor.putString(LOC_MANUAL_LONGITUDE, Double.toString(longitude));
		editor.commit();
	}
	
	/**
	 * Mendapatkan alamat lokasi yang diinputkan secara manual.
	 * 
	 * @return Alamat lokasi manual
	 */
	public String getManualLocationAddress() {
		return prefs.getString(LOC_MANUAL, "");
	}
	
	/**
	 * Menyimpan alamat lokasi yang diinputkan secara manual.
	 * 
	 * @param value Alamat lokasi manual
	 */
	public void setManualLocactionAddress(String value) {
		prefs.edit().putString(LOC_MANUAL, value).commit();
	}
	
	/* ----------------------------------------------------------------------- *
	 * Notifikasi
	 * ----------------------------------------------------------------------- */
	
	/**
	 * Mendapatkan berapa jarak gempa dapat dikategorikan sebagai wilayah regional.
	 * 
	 * @return Jarak dalam meter
	 */
	public int getRange() {
		String range = prefs.getString(RANGE, "250000");
		return Integer.parseInt(range);
	}
	
	/**
	 * Menyimpan jarak gempa yang dikategorikan sebagai wilayah regional.
	 * 
	 * @param value Jarak dalam meter
	 */
	public void setRange(int value) {
		prefs.edit().putString(RANGE, Integer.toString(value)).commit();
	}
	
	/**
	 * Mendapatkan apakah akan mengirim pemberitahuan saat terjadi gempa atau tidak.
	 * 
	 * @return True jika notifikasi diaktifkan
	 */
	public boolean isNotifySend() {
		return prefs.getBoolean(NOTIFY_SEND, true);
	}
	
	/**
	 * Menyimpan apakah pemberitahuan dikirim saat terjadi gempa atau tidak.
	 * 
	 * @param value True untuk mengaktifkan pemberitahuan, untuk tidak mengaktifkan
	 */
	public void setNotifySend(boolean value) {
		prefs.edit().putBoolean(NOTIFY_SEND, value).commit();
	}
	
	/**
	 * Mendapatkan minimal magnitudo pemberitahuan saat terjadi gempa.
	 * 
	 * @param locationType Tipe lokasi, REGIONAL atau GLOBAL
	 * @return Minimal magnitudo pemberitahuan
	 */
	public float getNotifyMinMagnitude(LocationType locationType) {
		String defValue = (locationType == LocationType.REGIONAL) 
				? DEF_MIN_MAG_NOTIFY_REG : DEF_MIN_MAG_NOTIFY_GLOBAL;
		String magnitude = prefs.getString(NOTIFY_MIN_MAG + "_" + locationType.value, 
				defValue);
		return Float.parseFloat(magnitude);
	}
	
	/**
	 * Menyimpan minimal magnitudo pemberitahuan.
	 * 
	 * @param locationType Tipe lokasi, REGIONAL atau GLOBAL
	 * @param value Minimal magnitudo pemberitahuan
	 */
	public void setNotifyMinMagnitude(LocationType locationType, float value) {
		prefs.edit().putString(NOTIFY_MIN_MAG + "_" + locationType.value, 
				Float.toString(value)).commit();
	}
	
	/**
	 * Mendapatkan apakah membunyikan suara alert saat pemberithauan atau tidak.
	 * 
	 * @return True jika terdapat suara, false jika tidak
	 */
	public boolean isNotifyAlert() {
		return prefs.getBoolean(NOTIFY_ALERT, true);
	}
	
	/**
	 * Menyimpan apakah membunyikan suara alert saat pemberitahuan atau tidak.
	 * 
	 * @param value True jika membunyikan suara, false jika tidak
	 */
	public void setNotifyAlert(boolean value) {
		prefs.edit().putBoolean(NOTIFY_ALERT, value).commit();
	}
	
	/**
	 * Mendapatkan suara alert saat pemberitahuan. Suara alert harus diaktifkan
	 * terlebih dahulu. lihat {@link #isNotifyAlert()}
	 * 
	 * @return Suara alert
	 */
	public Uri getNotifyAlertSound() {
		String uriString = prefs.getString(NOTIFY_ALERT_SOUND, "");
		
		if (uriString == null || uriString.length() == 0) {
			if (isNotifyAlert()) {
				Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager
						.TYPE_NOTIFICATION);
				setNotifyAlertSound(defaultUri);
				return defaultUri;
			} else {
				return null;
			}
		}
		return Uri.parse(uriString);
	}

	/**
	 * Menyimpan suara alert notifikasi.
	 * 
	 * @param value Uri suara alert
	 */
	public void setNotifyAlertSound(Uri value) {
		prefs.edit().putString(NOTIFY_ALERT_SOUND, value.toString()).commit();
	}
	
	/**
	 * Mendapatkan apakah layar berkedip saat pemberitahuan.
	 * 
	 * @return True jika berkedip, false jika tidak
	 */
	public boolean isNotifyFlash() {
		return prefs.getBoolean(NOTIFY_FLASH, true);
	}

	/**
	 * Menentukan apakah layar berkedip saat pemberitahuan atau tidak.
	 * 
	 * @param value True jika berkedip, false jika tidak
	 */
	public void setNotifyFlash(boolean value) {
		prefs.edit().putBoolean(NOTIFY_FLASH, value).commit();
	}

	/**
	 * Mendapatkan apakah bergetar saat pemberitahuan atau tidak.
	 * 
	 * @return True jika bergetar, false jika tidak
	 */
	public boolean isNotifyVibrate() {
		return prefs.getBoolean(NOTIFY_VIBRATE, true);
	}

	/**
	 * Menentukan apakah bergetar saat pemberitahuan atau tidak.
	 * 
	 * @param value True jika bergetar, false jika tidak
	 */
	public void setNotifyVibrate(boolean value) {
		prefs.edit().putBoolean(NOTIFY_VIBRATE, value).commit();
	}
	
	/* ----------------------------------------------------------------------- *
	 * Facebook
	 * ----------------------------------------------------------------------- */
	
	/**
	 * Mendapatkan apakah akan memposting ke Facebook ketika terjadi gempa atau tidak.
	 * 
	 * @return True jika posting, false jika tidak
	 */
	public boolean isFacebookSend() {
		return prefs.getBoolean(FACEBOOK_SEND, false);
	}
	
	/**
	 * Menentukan apakah akan memposting ke Facebook ketika terjadi gempa atau tidak.
	 * 
	 * @param value True jika posting, false jika tidak
	 */
	public void setFacebookSend(boolean value) {
		prefs.edit().putBoolean(FACEBOOK_SEND, value).commit();
	}

	/**
	 * Mendapatkan minimal magnitudo share ke Facebook jika terjadi gempa.
	 * 
	 * @param locationType Tipe lokasi, REGIONAL atau GLOBAL
	 * @return Minimal magnitudo share
	 */
	public float getFacebookMinMagnitude(LocationType locationType) {
		String defValue = (locationType == LocationType.REGIONAL) 
				? DEF_MIN_MAG_FACEBOOK_REG : DEF_MIN_MAG_FACEBOOK_GLOBAL;
		String magnitude = prefs.getString(FACEBOOK_MIN_MAG + "_" 
				+ locationType.value, defValue);
		return Float.parseFloat(magnitude);
	}

	/**
	 * Menyimpan minimal magnitudo share ke Facebook jika terjadi gempa.
	 * 
	 * @param locationType Tipe lokasi, REGIONAL atau GLOBAL
	 * @param value Minimal magnitudo share
	 */
	public void setFacebookMinMagnitude(LocationType locationType, float value) {
		prefs.edit().putString(FACEBOOK_MIN_MAG + "_" + locationType.value, 
				Float.toString(value)).commit();
	}

	/* ----------------------------------------------------------------------- *
	 * Twitter
	 * ----------------------------------------------------------------------- */
	
	/**
	 * Mendapatkan apakah akan memposting ke Twitter ketika terjadi gempa atau tidak.
	 * 
	 * @return True jika posting, false jika tidak
	 */
	public boolean isTwitterSend() {
		return prefs.getBoolean(TWITTER_SEND, false);
	}
	
	/**
	 * Menentukan apakah akan memposting ke Twitter ketika terjadi gempa atau tidak.
	 * 
	 * @param value True jika posting, false jika tidak
	 */
	public void setTwitterSend(boolean value) {
		prefs.edit().putBoolean(TWITTER_SEND, value).commit();
	}

	/**
	 * Mendapatkan minimal magnitudo share ke Twitter jika terjadi gempa.
	 * 
	 * @param locationType Tipe lokasi, REGIONAL atau GLOBAL
	 * @return Minimal magnitudo share
	 */
	public float getTwitterMinMagnitude(LocationType locationType) {
		String defValue = (locationType == LocationType.REGIONAL) 
				? DEF_MIN_MAG_TWITTER_REG : DEF_MIN_MAG_TWITTER_GLOBAL;
		String magnitude = prefs.getString(TWITTER_MIN_MAG + "_" 
				+ locationType.value, defValue);
		return Float.parseFloat(magnitude);
	}

	/**
	 * Menyimpan minimal magnitudo share ke Twitter jika terjadi gempa.
	 * 
	 * @param locationType Tipe lokasi, REGIONAL atau GLOBAL
	 * @param value Minimal magnitudo share
	 */
	public void setTwitterMinMagnitude(LocationType locationType, float value) {
		prefs.edit().putString(TWITTER_MIN_MAG + "_" + locationType.value, 
				Float.toString(value)).commit();
	}

	/**
	 * Mendapatkan template Twitter untuk dikirim jika terjadi gempa, jika belum
	 * diatur sebelumnya akan digunakan template Twitter default.
	 * 
	 * @param context Context
	 * @return Template Twitter
	 */
	public String getTwitterTemplate(Context context) {
		return prefs.getString(TWITTER_TEMPLATE, context.getString(
				R.string.tpl_def_twitter));
	}

	/**
	 * Menyimpan detail template Twitter untuk dikirim jika terjadi gempa.
	 * 
	 * @param value Template Twitter
	 */
	public void setTwitterTemplate(String value) {
		prefs.edit().putString(TWITTER_TEMPLATE, value).commit();
	}

	/**
	 * Mendapatkan akses token user.
	 * 
	 * @return Akses token
	 */
	public String getTwitterAccessToken() {
		return prefs.getString(TWITTER_ACCESS_TOKEN, "");
	}
	
	/**
	 * Menyimpan akses token user.
	 * 
	 * @param token Akses token
	 * @param secret Akses token rahasia
	 */
	public void setTwitterToken(String token, String secret) {
		Editor editor = prefs.edit();
		editor.putString(TWITTER_ACCESS_TOKEN, token);
		editor.putString(TWITTER_ACCESS_TOKEN_SECRET, secret);
		editor.commit();
	}
	
	/**
	 * Mendapatkan akses token user.
	 * 
	 * @return Array, index 0: akses token, index 1: akses token rahasia
	 */
	public String[] getTwitterToken() {
		String token = prefs.getString(TWITTER_ACCESS_TOKEN, "");
		String secret = prefs.getString(TWITTER_ACCESS_TOKEN_SECRET, "");
		return new String[] { token, secret };
	}
	
	/* ----------------------------------------------------------------------- *
	 * Email
	 * ----------------------------------------------------------------------- */
	
	/**
	 * Mendapatkan apakah akan mengirim email ketika terjadi gempa atau tidak.
	 * 
	 * @return True jika mengirim email, false jika tidak
	 */
	public boolean isMailSend() {
		return prefs.getBoolean(MAIL_SEND, false);
	}
	
	/**
	 * Menentukan apakah akan mengirim email ketika terjadi gempa atau tidak.
	 * 
	 * @param value True jika mengirim email, false jika tidak
	 */
	public void setMailSend(boolean value) {
		prefs.edit().putBoolean(MAIL_SEND, value).commit();
	}

	/**
	 * Mendapatkan minimal magnitudo mengirim email jika terjadi gempa.
	 * 
	 * @param locationType Tipe lokasi, REGIONAL atau GLOBAL
	 * @return Minimal magnitudo mengirim email
	 */
	public float getMailMinMagnitude(LocationType locationType) {
		String defValue = (locationType == LocationType.REGIONAL) 
				? DEF_MIN_MAG_MAIL_REG : DEF_MIN_MAG_MAIL_GLOBAL;
		String magnitude = prefs.getString(MAIL_MIN_MAG + "_" 
				+ locationType.value, defValue);
		return Float.parseFloat(magnitude);
	}

	/**
	 * Menyimpan minimal magnitudo mengirim email jika terjadi gempa.
	 * 
	 * @param locationType Tipe lokasi, REGIONAL atau GLOBAL
	 * @param value Minimal magnitudo mengirim email
	 */
	public void setMailMinMagnitude(LocationType locationType, float value) {
		prefs.edit().putString(MAIL_MIN_MAG + "_" + locationType.value, 
				Float.toString(value)).commit();
	}

	/**
	 * Mendapatkan SMTP server email.
	 * 
	 * @return SMTP server email
	 */
	public String getMailHost() {
		return prefs.getString(MAIL_HOST, "");
	}
	
	/**
	 * Menyimpan SMTP server email.
	 * 
	 * @param value SMTP server
	 */
	public void setMailHost(String value) {
		prefs.edit().putString(MAIL_HOST, value).commit();
	}

	/**
	 * Mendapatkan SMTP port email.
	 * 
	 * @return SMTP port email
	 */
	public String getMailPort() {
		return prefs.getString(MAIL_PORT, "");
	}
	
	/**
	 * Menyimpan SMPT port email.
	 * 
	 * @param value SMTP port email
	 */
	public void setMailPort(String value) {
		prefs.edit().putString(MAIL_PORT, value).commit();
	}

	/**
	 * Mendapatkan socket factory port email.
	 * 
	 * @return Socket factory port email
	 */
	public String getMailSPort() {
		return prefs.getString(MAIL_SPORT, "");
	}
	
	/**
	 * Menyimpan socket factory port email.
	 * 
	 * @param value Socket factory port email
	 */
	public void setMailSPort(String value) {
		prefs.edit().putString(MAIL_SPORT, value).commit();
	}
	
	/**
	 * Mendapatkan username email.
	 * 
	 * @return Username email
	 */
	public String getMailUsername() {
		return prefs.getString(MAIL_USERNAME, "");
	}
	
	/**
	 * Menyimpan username email.
	 * 
	 * @param value Username
	 */
	public void setMailUsername(String value) {
		prefs.edit().putString(MAIL_USERNAME, value).commit();
	}
	
	/**
	 * Mendapatkan password email.
	 * 
	 * @return Password email
	 */
	public String getMailPass() {
		return prefs.getString(MAIL_PASS, "");
	}
	
	/**
	 * Menyimpan username email.
	 * 
	 * @param value Username
	 */
	public void setMailPass(String value) {
		prefs.edit().putString(MAIL_PASS, value).commit();
	}

	/**
	 * Mendapatkan template email untuk dikirim jika terjadi gempa, jika belum
	 * diatur sebelumnya akan digunakan template email default.
	 * 
	 * @param context Context
	 * @return Template email
	 */
	public String getMailTemplate(Context context) {
		return prefs.getString(MAIL_TEMPLATE, context.getString(
				R.string.tpl_def_mail));
	}

	/**
	 * Menyimpan detail template email untuk dikirim jika terjadi gempa.
	 * 
	 * @param value Template email
	 */
	public void setMailTemplate(String value) {
		prefs.edit().putString(MAIL_TEMPLATE, value).commit();
	}

	/**
	 * Mendapatkan detail template email untuk dikirim jika terjadi gempa, 
	 * jika belum diatur sebelumnya akan digunakan template email default.
	 * 
	 * @param context Context
	 * @return Template email
	 */
	public String getMailTemplateDetail(Context context) {
		return prefs.getString(MAIL_TEMPLATE_DETAIL, context.getString(
				R.string.tpl_def_mail_detail));
	}

	/**
	 * Menyimpan template email untuk dikirim jika terjadi gempa.
	 * 
	 * @param value Template email
	 */
	public void setMailTemplateDetail(String value) {
		prefs.edit().putString(MAIL_TEMPLATE_DETAIL, value).commit();
	}

	/* ----------------------------------------------------------------------- *
	 * SMS
	 * ----------------------------------------------------------------------- */
	
	/**
	 * Mendapatkan apakah akan mengirim pesan SMS ketika terjadi gempa atau tidak.
	 * 
	 * @return True jika mengirim SMS, false jika tidak
	 */
	public boolean isSmsSend() {
		return prefs.getBoolean(SMS_SEND, false);
	}

	/**
	 * Menentukan apakah akan mengirim pesan SMS ketika terjadi gempa atau tidak.
	 * 
	 * @param value True jika mengirim SMS, false jika tidak
	 */
	public void setSmsSend(boolean value) {
		prefs.edit().putBoolean(SMS_SEND, value).commit();
	}
	
	/**
	 * Mendapatkan minimal magnitudo mengirim pesan SMS jika terjadi gempa.
	 * 
	 * @param locationType Tipe lokasi, REGIONAL atau GLOBAL
	 * @return Minimal magnitudo mengirim SMS
	 */
	public float getSmsMinMagnitude(LocationType locationType) {
		String defValue = (locationType == LocationType.REGIONAL) 
				? DEF_MIN_MAG_SMS_REG : DEF_MIN_MAG_SMS_GLOBAL;
		String magnitude = prefs.getString(SMS_MIN_MAG + "_" + locationType.value, 
				defValue);
		return Float.parseFloat(magnitude);
	}

	/**
	 * Menyimpan minimal magnitudo mengirim pesan SMS jika terjadi gempa.
	 * 
	 * @param locationType Tipe lokasi, REGIONAL atau GLOBAL
	 * @param value Minimal magnitudo mengirim pesan
	 */
	public void setSmsMinMagnitude(LocationType locationType, float value) {
		prefs.edit().putString(SMS_MIN_MAG + "_" + locationType.value, 
				Float.toString(value)).commit();
	}

	/**
	 * Mendapatkan template pesan SMS untuk dikirim jika terjadi gempa, jika belum
	 * diatur sebelumnya akan digunakan template sms default.
	 * 
	 * @param context Context
	 * @return Template SMS
	 */
	public String getSmsTemplate(Context context) {
		return prefs.getString(SMS_TEMPLATE, context.getString(
				R.string.tpl_def_sms));
	}

	/**
	 * Menyimpan detail template pesan SMS untuk dikirim jika terjadi gempa.
	 * 
	 * @param value Template SMS
	 */
	public void setSmsTemplate(String value) {
		prefs.edit().putString(SMS_TEMPLATE, value).commit();
	}

	/**
	 * Mendapatkan detail template pesan SMS untuk dikirim jika terjadi gempa, 
	 * jika belum diatur sebelumnya akan digunakan template sms default.
	 * 
	 * @param context Context
	 * @return Template SMS
	 */
	public String getSmsTemplateDetail(Context context) {
		return prefs.getString(SMS_TEMPLATE_DETAIL, context.getString(
				R.string.tpl_def_sms_detail));
	}

	/**
	 * Menyimpan template pesan SMS untuk dikirim jika terjadi gempa.
	 * 
	 * @param value Template SMS
	 */
	public void setSmsTemplateDetail(String value) {
		prefs.edit().putString(SMS_TEMPLATE_DETAIL, value).commit();
	}
	
	/* ----------------------------------------------------------------------- *
	 * Others
	 * ----------------------------------------------------------------------- */

	/**
	 * Mendapatkan apakah service di-start secara otomatis saat sistem boot.
	 * 
	 * @return True jika service di-start saat boot
	 */
	public boolean isBootStart() {
		return prefs.getBoolean(BOOT_START, true);
	}

	/**
	 * Menentukan apakah service di-start secara otomatis saat sistem boot.
	 * 
	 * @param value True jika service di-start saat boot
	 */
	public void setBootStart(boolean value) {
		prefs.edit().putBoolean(BOOT_START, value).commit();
	}
	
	/**
	 * Mendapatkan apakah dialog ditampilkan atau tidak.
	 * 
	 * @param warnId ID dari dialog
	 * @return True jika dialog ditampilkan, false jika tidak
	 */
	public boolean isDialogShown(String warnId) {
		return prefs.getBoolean(warnId, true);
	}
	
	/**
	 * Menentukan apakah dialog ditampilkan atau tidak.
	 * 
	 * @param key ID dari dialog
	 * @param value True jika dialog ditampilkan, false jika tidak
	 */
	public void setDialogShown(String key, boolean value) {
		prefs.edit().putBoolean(key, value).commit();
	}
	
	/**
	 * Mendapatkan terakhir kali data di-update. (dalam milisecond).
	 * 
	 * @return Waktu terakhir di-update dalam milisecond
	 */
	public long getLastUpdate() {
		return prefs.getLong(LAST_UPDATE, 0);
	}

	/**
	 * Menyimpan terakhir kali data di-update. (dalam milisecond).
	 * 
	 * @param value Waktu terakhir kali di-update dalam milisecond
	 */
	public void setLastUpdate(long value) {
		prefs.edit().putLong(LAST_UPDATE, value).commit();
	}
	
	/**
	 * Mendapatkan apakah layer pada peta digunakan atau tidak.
	 * 
	 * @param key Key layer yang digunakan
	 * @return True jika digunakan, false jika tidak
	 */
	public boolean isLayerUsed(String key) {
		return prefs.getBoolean(key, false);
	}
	
	/**
	 * Menentukan apakah layer pada peta digunakan atau tidak.
	 * 
	 * @param key Key layer yang digunakan
	 * @param value True jika digunakan, false jika tidak
	 */
	public void setLayerUsed(String key, boolean value) {
		prefs.edit().putBoolean(key, value).commit();
	}
}
