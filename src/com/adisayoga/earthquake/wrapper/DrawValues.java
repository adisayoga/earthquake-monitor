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
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Typeface;

import com.adisayoga.earthquake.dto.EarthquakeTheme;

/**
 * Class helper berisi objek Paint digunakan untuk menggambar titik gempa
 * pada peta.
 * 
 * @author Adi Sayoga
 */
public class DrawValues {
	private static final long AGE = AlarmManager.INTERVAL_DAY * 3;
	private static final float MIN_MAGNITUDE = 4;
	
	public static final Paint markPaint = new Paint();
	public static final Paint markGlowPaint = new Paint();
	public static final Paint markOutlinePaint = new Paint();
	public static final Paint feelPaint = new Paint();
	public static final Paint feelOutlinePaint = new Paint();
	
	public static final Paint textPaint = new Paint();
	public static final Paint textOutlinePaint = new Paint();
	
	public static String text = "";
	public static int markRadius = 0;
	public static int dmgMeters = 0;
	
	static {
		markPaint.setAntiAlias(true);
		
		markGlowPaint.setColor(Color.BLACK);
		markGlowPaint.setAntiAlias(true);
		markGlowPaint.setStyle(Paint.Style.STROKE);
		markGlowPaint.setStrokeWidth(1);
		
		markOutlinePaint.setColor(Color.WHITE);
		markOutlinePaint.setAntiAlias(true);
		markOutlinePaint.setStyle(Paint.Style.STROKE);
		markOutlinePaint.setStrokeWidth(1);
		
		feelPaint.setAntiAlias(true);
		
		feelOutlinePaint.setAntiAlias(true);
		feelOutlinePaint.setStyle(Paint.Style.STROKE);
		feelOutlinePaint.setStrokeWidth(1);
		
		textPaint.setAntiAlias(true);
		textPaint.setColor(0xffffa500);
		textPaint.setTextAlign(Align.CENTER);
		textPaint.setTypeface(Typeface.DEFAULT_BOLD);
		
		textOutlinePaint.setAntiAlias(true);
		textOutlinePaint.setColor(Color.BLACK);
		textOutlinePaint.setTextAlign(Align.CENTER);
		textOutlinePaint.setTypeface(Typeface.DEFAULT_BOLD);
		textOutlinePaint.setStyle(Style.STROKE);
		textOutlinePaint.setStrokeWidth(3);
	}
	
	/**
	 * Atur paint dan teks.
	 * 
	 * @param theme Tema
	 * @param textSize Ukuran teks
	 * @param magnitude Magnitudo gempa
	 * @param time Waktu terjadinya gempa
	 */
	public static void prepare(EarthquakeTheme theme, int textSize, float magnitude, 
			long time) {
		textPaint.setTextSize(textSize);
		textOutlinePaint.setTextSize(textSize);
		
		setColor(theme.getQuakeColor(magnitude));
		setMagnitude(magnitude);
		setAlpha(time);
		setText(magnitude, time);
	}
	
	/**
	 * Set warna paint.
	 * 
	 * @param color Warna
	 */
	private static void setColor(int color) {
		markPaint.setColor(color);
		feelPaint.setColor(color);
		feelOutlinePaint.setColor(color);
	}
	
	/**
	 * Set radius magnitudo
	 * 
	 * @param magnitude Magnitudo
	 */
	private static void setMagnitude(float magnitude) {
		markRadius = (int) (magnitude * 2);
		dmgMeters = (int) Math.max(magnitude * 10, Math.pow(magnitude, 3)) * 1000;
	}
	
	/**
	 * Set alpha, Gempa yang lebih lama akan lebih transparan
	 * 
	 * @param time Waktu tejadinya gempa
	 */
	private static void setAlpha(long time) {
		float alpha = calculateAlpha(time, AGE);
		int defaultAlpha = (int) ((alpha * 205 / 255) + 50); // 50-255 -> 0-205 + 50
		int markAlpha = (int) ((alpha * 90 / 255) + 10);     // 10-100 -> 0-90 + 10
		int feelAlpha = (int) ((alpha * 40 / 255) + 10);     // 10-50  -> 0-40 + 10
		
		markPaint.setAlpha(markAlpha);
		markGlowPaint.setAlpha(defaultAlpha);
		markOutlinePaint.setAlpha(defaultAlpha);
		feelPaint.setAlpha(feelAlpha);
		feelOutlinePaint.setAlpha(defaultAlpha);
	}
	
	/**
	 * Perhitungan alpha berdasarkan waktu terjadinya gempa.
	 * 
	 * @param time Waktu terjadinya gempa
	 * @param age Umur minimal pengaturan
	 * @return Alpha
	 */
	private static float calculateAlpha(long time, long age) {
		long delta = System.currentTimeMillis() - time;
		if (delta > age) return 0;
		
		return 255f - ((float) delta / (float) age * 255f);
	}
	
	/**
	 * Set teks yang akan ditampilkan, Pada magnitudo dan waktu tertentu, teks 
	 * akan bernilai kosong.
	 * 
	 * @param magnitude Mangitudo
	 * @param time Waktu terjadinya gempa
	 */
	private static void setText(float magnitude, long time) {
		text = (magnitude >= MIN_MAGNITUDE && System.currentTimeMillis() - time <= AGE)
				? "M" + magnitude : "";
	}
}
