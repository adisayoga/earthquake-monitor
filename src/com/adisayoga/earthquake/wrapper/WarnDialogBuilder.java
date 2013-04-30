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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.adisayoga.earthquake.R;

/**
 * Dialog builder peringatan.
 * 
 * @author Adi Sayoga
 */
public class WarnDialogBuilder extends AlertDialog.Builder {

	private final Context context;
	private final String warnId;
	
	private final TextView message;
	private final CheckBox checkBox;

	public WarnDialogBuilder(ContextThemeWrapper wrapper, String id) {
		
		super(wrapper);
		this.context = wrapper.getBaseContext();
		this.warnId = id;
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.warn_dialog, null);
		layout.setMinimumWidth(240);
		layout.setMinimumHeight(180);
		setView(layout);
		
		message = (TextView) layout.findViewById(R.id.warn_text);
		
		checkBox = (CheckBox) layout.findViewById(R.id.warn_check);
		checkBox.setChecked(false);
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// Simpan ke preference perlihatkan/sembunyikan dialog
				Prefs prefs = Prefs.getInstance(context);
				prefs.setDialogShown(warnId, !isChecked);
			}
		});
		
		setNeutralButton(android.R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
	}
	
	@Override
	public AlertDialog.Builder setMessage(int messageId) {
		message.setText(context.getResources().getString(messageId));
		return this;
	}
	
}
