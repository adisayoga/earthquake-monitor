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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.adisayoga.earthquake.R;
import com.adisayoga.earthquake.wrapper.Prefs;

/**
 * Activity login email.
 * 
 * @author Adi Sayoga
 */
public class MailLoginActivity extends Activity {

	private static final String TAG = "MailLoginActivity";
	private static Prefs prefs;
	
	private EditText username;
	private EditText pass;

	private boolean needSave = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        
        prefs = Prefs.getInstance(this);
        setTheme(prefs.getTheme().resId);
		setContentView(R.layout.mail_login);
		
		username = (EditText) findViewById(R.id.username);
		pass = (EditText) findViewById(R.id.pass);
		
		bindView();
		setListeners();
	}

	/**
	 * Load username dan password dari preference.
	 */
	private void bindView() {
		username.setText(prefs.getMailUsername());
		pass.setText(prefs.getMailPass());
	}

	/**
	 * Set listener tombol
	 */
	private void setListeners() {
		// Tombol update
		Button okButton = (Button) findViewById(R.id.ok);
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveLoginInformation();
				needSave = false;
				finish();
			}
		});
		
		// Tombol kembali
		Button cancelButton = (Button) findViewById(R.id.cancel);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				needSave = false;
				finish();
			}
		});
	}
	
	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
		if (needSave) saveLoginInformation();
	}

	/**
	 * Simpan username dan password.
	 */
	private void saveLoginInformation() {
		prefs.setMailUsername(username.getText().toString());
		prefs.setMailPass(pass.getText().toString());
	}
	
}
