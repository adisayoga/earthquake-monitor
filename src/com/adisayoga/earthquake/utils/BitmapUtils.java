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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * Class utility yang berhubungan dengan bitmap.
 * 
 * @author Adi Sayoga
 */
public class BitmapUtils {
	
	private static final String TAG = "BitmapUtils";	
	
	/**
	 * Download gambar dari URL.
	 * 
	 * @param url Url gambar
	 * @return Gambar bitmal
	 */
	public static Bitmap getImage(URL url) {
		try {
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(30 * 1000); // 30 detik
			connection.setReadTimeout(30 * 1000);    // 30 detik
			
			if (connection.getResponseCode() != 200) { 
				// Koneksi gagal
				Log.e(TAG, "Koneksi ke " + url.toString() + " gagal, response code: " 
						+ connection.getResponseCode());
				return null;
			}
			
			InputStream is = (InputStream) connection.getContent();
			Drawable drawable = Drawable.createFromStream(is, "src");
			Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
			return bitmap;
			
		} catch (MalformedURLException e) {
			Log.e(TAG, "Gagal membaca gambar " + e.getMessage(), e);
			return null;
			
		} catch (IOException e) {
			Log.e(TAG, "Gagal membaca gambar " + e.getMessage(), e);
			return null;
		}
	}
	
	/**
	 * Buat sisi pojok bulat.
	 * 
	 * @param bitmap Bitmap
	 * @param pixels Pixel
	 * @return Bitmap yang pada sisi pojoknya sudah bulat
	 */
	public static Bitmap getRoundedCorner(Bitmap bitmap, int pixels) {
		if (bitmap == null) return null;
		
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff000000;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        
        return output;
    }
	
}
