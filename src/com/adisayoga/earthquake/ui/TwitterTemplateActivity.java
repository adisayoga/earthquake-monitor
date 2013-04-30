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
import android.widget.TextView;

import com.adisayoga.earthquake.R;
import com.adisayoga.earthquake.wrapper.Prefs;

/**
 * Template Twitter saat mengirim pesan secara otomatis.
 * 
 * @author Adi Sayoga
 */
public class TwitterTemplateActivity extends Activity {

	private static final String[] TAGS = new String[] {
		Prefs.TPL_DATE,     Prefs.TPL_MAGNITUDE, Prefs.TPL_REGION, 
		Prefs.TPL_LOCATION, Prefs.TPL_DEPTH,     Prefs.TPL_DISTANCE
	};
	
	private static Prefs prefs;
	private EditText detailText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		prefs = Prefs.getInstance(this);
		setTheme(prefs.getTheme().resId);
		setContentView(R.layout.template_message);
		hideMainTemplate();
		
		bindView();
		setListeners();
	}

	/**
	 * Twitter memiliki keterbatasan karakter, jadi pesan akan dikirim per item
	 * gempa, jadi template utama tidak diperlukan disini.
	 */
	private void hideMainTemplate() {
		TextView mainTextView = (TextView) findViewById(R.id.template_textview);
		mainTextView.setVisibility(View.GONE);
		EditText mainText = (EditText) findViewById(R.id.template_text);
		mainText.setVisibility(View.GONE);
		Button mainButton = (Button) findViewById(R.id.template_tags);
		mainButton.setVisibility(View.GONE);
	}
	
	/**
	 * Menampilkan template dari preference ke view.
	 */
	private void bindView() {
		detailText = (EditText) findViewById(R.id.detail_text);
		detailText.setText(prefs.getTwitterTemplate(this));
	}
	
	/**
	 * Setup click listener.
	 */
	private void setListeners() {
		Button detailTags = (Button) findViewById(R.id.detail_tags);
		detailTags.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDetailTagsDialog();
			}
		});
	}
	
	/**
	 * Perlihatkan pilihan detail tag.
	 */
	private void showDetailTagsDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.template_details);
		builder.setItems(TAGS, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int start = detailText.getSelectionStart();
				String tag = TAGS[which];
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
		prefs.setTwitterTemplate(detailText.getText().toString());
	}

}
