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

import java.util.ArrayList;
import java.util.List;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;
/**
 * Class bantuan untuk mengirim pesan SMS.
 * 
 * @author Adi Sayoga
 */
public class EarthquakeSms {
	
	private static final String TAG = "EarthquakeSMS";
	
	public static final String SENT_SMS = "com.adisayoga.earthquake.SENT_SMS";
	public static final String RECEIPENT = "receipent";
	public static boolean SPLIT_SMS_MESSAGE = true;
	
	private final Context context;
	
	public EarthquakeSms(Context context) {
		this.context = context;
	}
	
	/**
	 * Mengirim SMS ke banyak nomor sekaligus.
	 * 
	 * @param phones Daftar nomor telepon tujuan
	 * @param message Pesan yang akan dikirim. Untuk men-generate pesan lihat 
	 *        {@link #getMessage(List)}
	 * @param SplitMessage Apakah pesan yang dikirim akan dipecah atau tidak
	 */
	public List<String> sendTextMessage(String phones[], String message, 
			boolean splitMessage) {
		if (phones == null || phones.length == 0) return null;
		
		List<String> phonesSent = new ArrayList<String>();
		for (String phone : phones) {
			if (sendTextMessage(phone, message, splitMessage)) {
				phonesSent.add(phone);
			}
		}
		return phonesSent;
	}
	
	/**
	 * Mengirim pesan SMS.
	 * 
	 * @param contact Data kontak
	 * @param message Pesan
	 * @param splitMessage True jika pesan dipecah, false jika tidak
	 * @return
	 */
	public boolean sendTextMessage(String to, String message, 
			boolean splitMessage) {
			
		try {
			Intent intent = new Intent(SENT_SMS);
			intent.putExtra(RECEIPENT, to);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 
					0, intent, 0);
			
			SmsManager sms = SmsManager.getDefault();
			if (splitMessage) {
				List<String> messages = sms.divideMessage(message);
				for (String toSend : messages) {
					sms.sendTextMessage(to, null, toSend, pendingIntent, null);
				}
			} else {
				sms.sendTextMessage(to, null, message, pendingIntent, null);
			}
			
			// TODO Return pesan berhasil dikirim... Sepertinya bukan disini!
			Log.d(TAG, "Pesan telah dikirim ke: " + to);
			Log.i(TAG, message);
			return true;
			
		} catch (Exception e) {
			Log.e(TAG, "Error mengirim sms: " + e.getMessage(), e);
			return false;
		}
	}
	
}
