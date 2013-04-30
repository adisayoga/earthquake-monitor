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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.adisayoga.earthquake.R;
import com.adisayoga.earthquake.wrapper.Prefs;

/**
 * Template pesan SMS saat mengirim pesan secara otomatis.
 * 
 * @author Adi Sayoga
 */
public class SmsTemplateActivity extends Activity {

	private static final String[] TAGS = new String[] { Prefs.TPL_DETAILS };
	private static final String[] DETAIL_TAGS = new String[] {
		Prefs.TPL_DATE,     Prefs.TPL_MAGNITUDE, Prefs.TPL_REGION, 
		Prefs.TPL_LOCATION, Prefs.TPL_DEPTH,     Prefs.TPL_DISTANCE
	};
	
	private static Prefs prefs;
	private EditText templateText;
	private EditText detailText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		prefs = Prefs.getInstance(this);
		setTheme(prefs.getTheme().resId);
		setContentView(R.layout.template_message);
		
		bindView();
		setListeners();
	}

	/**
	 * Menampilkan template dari preference ke view.
	 */
	private void bindView() {
		templateText = (EditText) findViewById(R.id.template_text);
		templateText.setText(prefs.getSmsTemplate(this));
		detailText = (EditText) findViewById(R.id.detail_text);
		detailText.setText(prefs.getSmsTemplateDetail(this));
	}
	
	/**
	 * Setup click listener.
	 */
	private void setListeners() {
		Button templateTags = (Button) findViewById(R.id.template_tags);
		templateTags.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showTemplateTagsDialog();
			}
		});
		
		Button detailTags = (Button) findViewById(R.id.detail_tags);
		detailTags.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDetailTagsDialog();
			}
		});
	}
	
	/**
	 * Perlihatkan pilihan tag.
	 */
	private void showTemplateTagsDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.template_main);
		builder.setItems(TAGS, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int start = templateText.getSelectionStart();
				String tag = TAGS[which];
				templateText.getText().insert(start, tag);
			}
		});
		builder.show();
	}
	
	/**
	 * Perlihatkan pilihan detail tag.
	 */
	private void showDetailTagsDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.template_details);
		builder.setItems(DETAIL_TAGS, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int start = detailText.getSelectionStart();
				String tag = DETAIL_TAGS[which];
				detailText.getText().insert(start, tag);
			}
		});
		builder.show();
	}
	
	@Override
	protected void onPause() {
		super.onResume();
		saveSettings();
	}

	/**
	 * Simpan ke preference.
	 */
	private void saveSettings() {
		prefs.setSmsTemplate(templateText.getText().toString());
		prefs.setSmsTemplateDetail(detailText.getText().toString());
	}
}
