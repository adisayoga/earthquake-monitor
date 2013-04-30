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
 
package com.adisayoga.earthquake.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.adisayoga.earthquake.R;

/**
 * Utility untuk memformat waktu.
 * 
 * @author Adi Sayoga
 */
public class TimeUtils {
	
	private static final String TAG = "TimeUtils";

	private static final int ZONE_OFFSET = Calendar.getInstance().get(Calendar.ZONE_OFFSET);
	private static final int DST_OFFSET = Calendar.getInstance().get(Calendar.DST_OFFSET);
	
	private static final long ONE_SECOND = 1000;
	private static final long ONE_MINUTE = ONE_SECOND * 60;
	private static final long ONE_HOUR = ONE_MINUTE * 60;
	private static final long ONE_DAY = ONE_HOUR * 24;
	private static final long ONE_MONTH = ONE_DAY * 30;
	private static final long ONE_YEAR = ONE_DAY * 365;
	
	private static TimeUtils instance = null;
	
	private static String justNow;
	private static String minuteAgo;
    private static String minutesAgo;
    private static String hourAgo;
    private static String hoursAgo;
    private static String yesterday;
    private static String daysAgo;
    private static String monthAgo;
    private static String monthsAgo;

    private static String timeFormat;
    private static String timeDayFormat;
    private static String dateShortFormat;
    private static String datetimeFormat;
    private static String datetimeYearFormat;
    
	private TimeUtils() {
	}

	public static final TimeUtils getInstance(Context context) {
		if (instance == null) {
			instance = new TimeUtils();
			
			Resources resources = context.getResources();
			justNow = resources.getString(R.string.tpl_just_now);
			minuteAgo = resources.getString(R.string.tpl_minute_ago);
			minutesAgo = resources.getString(R.string.tpl_minutes_ago);
			hourAgo = resources.getString(R.string.tpl_hour_ago);
			hoursAgo = resources.getString(R.string.tpl_hours_ago);
			yesterday = resources.getString(R.string.tpl_yesterday);
			daysAgo = resources.getString(R.string.tpl_days_ago);
			monthAgo = resources.getString(R.string.tpl_month_ago);
			monthsAgo = resources.getString(R.string.tpl_months_ago);
			
			timeFormat = resources.getString(R.string.format_time);
			timeDayFormat = resources.getString(R.string.format_time_day);
			datetimeFormat = resources.getString(R.string.format_datetime);
			dateShortFormat = resources.getString(R.string.format_date_short);
			datetimeYearFormat = resources.getString(R.string.format_datetime_year);
		}
		
		return instance;
	}
	
	/**
	 * Konversi waktu (dalam milisecond) ke dalam format yang mudah dibaca 
	 * manusia.
	 * <pre>
	 * contoh:
	 *   * Baru saja
	 *   * 2 menit lalu, 10:00 AM
	 *   * 5 jam lalu, 10:00 AM
	 *   * Kemarin, 10:00 AM
	 *   * 3 hari lalu: Rabu, 10:00 AM
	 *   * 4 bulan lalu: Rabu, 22-Apr 10:00 AM
	 *   * Rabu, 22-Apr-2011 10:00 AM
	 * </pre>
	 * 
	 * @param time Waktu (dalam timestamp) yang diformat
	 * @return Format string yang mudah dibaca manusia
	 * 
	 * @see {@link #toHumanReadableShort(long)}
	 */
	public String toHumanReadable(long time) {
		long duration = System.currentTimeMillis() - time;
		if (duration < ONE_MINUTE) return justNow;
		
		SimpleDateFormat sdf;
		String dateString = "";
		
		if (duration < ONE_HOUR) {
			sdf = new SimpleDateFormat(timeFormat);
			
			long minutes = duration / ONE_MINUTE;
			dateString = String.format((minutes <= 1) ? minuteAgo : minutesAgo, minutes) + ", ";
		} 
		else if (duration < ONE_DAY) {
			sdf = new SimpleDateFormat(timeFormat);
			
			long hours = duration / ONE_HOUR;
			dateString = String.format((hours <= 1) ? hourAgo : hoursAgo, hours) + ", ";
		} 
		else if (duration < ONE_MONTH) {
			long days = duration / ONE_DAY;
			if (days <= 1) {
				sdf = new SimpleDateFormat(timeFormat);
				dateString = yesterday + " ";
			} else if (days <= 3) {
				sdf = new SimpleDateFormat(timeDayFormat);
				dateString = String.format(daysAgo, days) + ", ";
			} else {
				sdf = new SimpleDateFormat(datetimeFormat);
				dateString = String.format(daysAgo, days) + ", ";
			}
		}
		else if (duration < ONE_YEAR) {
			sdf = new SimpleDateFormat(datetimeFormat);
			
			long months = duration / ONE_MONTH;
			if (months <= 1) {
				dateString = monthAgo + ", ";
			} else {
				dateString = String.format(monthsAgo, months) + ", ";
			}
		} 
		else {
			sdf = new SimpleDateFormat(datetimeYearFormat);
		}
		
		dateString += sdf.format(new Date(time));
		return dateString;
	}
	
	/**
	 * Konversi waktu ke dalam format yang biasa dibaca manusia (singkat)
	 * 
	 * @param time Waktu (dalam timestamp) yang diformat
	 * @return Format string yang mudah dibaca manusia
	 * 
	 * @see {@link #toHumanReadable(long)}
	 */
	public String toHumanReadableShort(long time) {
		long duration = System.currentTimeMillis() - time;
		if (duration < ONE_MINUTE) return justNow;
		
		SimpleDateFormat sdf;
		Date date = new Date(time);
		String dateString = "";
		
		if (duration < ONE_DAY) {
			sdf = new SimpleDateFormat(timeFormat);
			dateString = sdf.format(date);
		}
		else if (duration < ONE_DAY * 2) {
			dateString = yesterday;
		}
		else {
			sdf = new SimpleDateFormat(dateShortFormat);
			dateString = sdf.format(date);
		}
		
		return dateString;
	}
	
	/**
	 * Parse string ke dalam date.
	 * 
	 * @param value Nilai dalam string
	 * @param format Format
	 * 
	 * @return Date
	 * @throws ParseException
	 */
	public static Date parseDate(String value, SimpleDateFormat format) throws ParseException {
		try {
			Date date = format.parse(value);
			date = new Date(date.getTime() + ZONE_OFFSET + DST_OFFSET);
			return date;
		}
		catch (ParseException e) {
			Log.e(TAG, "Tidak dapat mem-parse tanggal dari string: " + value);
			throw e;
		}
	}
}
