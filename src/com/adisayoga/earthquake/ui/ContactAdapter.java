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

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.adisayoga.earthquake.R;
import com.adisayoga.earthquake.dto.ContactDTO;

/**
 * Adapter daftar kontak.
 * 
 * @author Adi Sayoga
 */
public class ContactAdapter extends ArrayAdapter<ContactDTO> {

	private final LayoutInflater inflater;
	private final int resourceId;
	
	public ContactAdapter(Context context, int resourceId, List<ContactDTO> contacts) {
		super(context, resourceId, contacts);

		inflater = (LayoutInflater) context.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		this.resourceId = resourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// ViewHolder menyimpan reference view untuk menghindari
		// pemanggilan tidak perlu findViewById()
		ViewHolder holder;
		
		// Ketika convertView tidak null, kita menggunakannya kembali secara 
		// langsung, tidak diperlukan inflate ulang. Kita hanya inflate View 
		// baru ketika convertView null
		if (convertView == null) {
			convertView = inflater.inflate(resourceId, null);
			
			// Buat sebuah ViewHolder dan simpan reference view
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.phoneOrMail = (TextView) convertView.findViewById(R.id.phone_or_mail);
			
			convertView.setTag(holder);
		} else {
			// Ambil ViewHolder kembali untuk mendapatkan akses cepat ke view
			holder = (ViewHolder) convertView.getTag();
		}
		
		ContactDTO contact = getItem(position);
		holder.name.setText(contact.name);
		String phoneOrMail = contact.phoneNumber;
		if (!contact.mail.equals("") && !phoneOrMail.equals("")) phoneOrMail += ", ";
		phoneOrMail += contact.mail;
		holder.phoneOrMail.setText(phoneOrMail);
		return convertView;
	}
	
	private static class ViewHolder {
		public TextView name;
		public TextView phoneOrMail;
	}
}
