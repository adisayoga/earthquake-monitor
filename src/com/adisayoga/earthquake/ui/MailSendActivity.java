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
 
package com.adisayoga.earthquake.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.adisayoga.earthquake.R;
import com.adisayoga.earthquake.wrapper.EarthquakeMail;
import com.adisayoga.earthquake.wrapper.Prefs;

/**
 * Activity mengirim email.
 * 
 * @author Adi Sayoga
 */
public class MailSendActivity extends Activity {
	
	private static final String TAG = "MailActivity";
	public static final String MESSAGE_EXTRA = "message";
	
	private static Prefs prefs;
	private String message;
	
	private EditText toText;
	private EditText subjectText;
	private EditText messageText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        prefs = Prefs.getInstance(this);
        setTheme(prefs.getTheme().resId);
		setContentView(R.layout.mail_send);
		
		toText = (EditText) findViewById(R.id.to);
		subjectText = (EditText) findViewById(R.id.subject);
		messageText = (EditText) findViewById(R.id.message);
		
		// Tampilkan data
		subjectText.setText(R.string.app_name);
		Intent intent = getIntent();
		if (intent.hasExtra(MESSAGE_EXTRA)) {
			message = intent.getStringExtra(MESSAGE_EXTRA);
			messageText.setText(message);
		}
		setListeners();
	}
	
	/**
	 * Set listener tombol
	 */
	private void setListeners() {

		Button sendButton = (Button) findViewById(R.id.send);
		sendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String to = toText.getText().toString();
				String[] toArray = to.split(",");
				String subject = subjectText.getText().toString();
				String message = messageText.getText().toString();
				
				sendMessage(toArray, subject, message);
			}
		});
		
		Button cancelButton = (Button) findViewById(R.id.cancel);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	private void sendMessage(final String[] to, final String subject, final String message) {
		new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					String username = prefs.getMailUsername();
					EarthquakeMail mail = new EarthquakeMail(MailSendActivity.this);
					mail.setFrom(username);
					mail.setTo(to);
					mail.setSubject(subject);
					mail.setBody(message);
					
					return mail.send();
					
				} catch (Exception e) {
					Log.e(TAG, "Tidak dapat mengirim email", e);
					return false;
				}
			}
			
			@Override
			protected void onPostExecute(Boolean result) {
				Context context = MailSendActivity.this;
				if (result) {
					Toast.makeText(context, R.string.mail_sent, Toast.LENGTH_SHORT)
							.show();
				} else {
					Toast.makeText(context, R.string.mail_fail, Toast.LENGTH_SHORT)
							.show();
				}
			}
		}.execute();
		
		finish();
	}
	
}
