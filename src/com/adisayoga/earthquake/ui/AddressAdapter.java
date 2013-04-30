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
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.adisayoga.earthquake.R;

/**
 * Adapter daftar alamat.
 * 
 * @author Adi Sayoga
 */
public class AddressAdapter extends ArrayAdapter<Address> {

	private final LayoutInflater inflater;
	private final int resourceId;
	
	public AddressAdapter(Context context, int resourceId, List<Address> addresses) {
		super(context, resourceId, addresses);

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
			holder.address = (TextView) convertView.findViewById(R.id.address);
			holder.lat = (TextView) convertView.findViewById(R.id.latitude);
			holder.lng = (TextView) convertView.findViewById(R.id.longitude);
			
			convertView.setTag(holder);
		} else {
			// Ambil ViewHolder kembali untuk mendapatkan akses cepat ke view
			holder = (ViewHolder) convertView.getTag();
		}
		
		Address address = getItem(position);
		String addressLine = "";
		for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
			if (addressLine != "") addressLine += ", ";
			addressLine += address.getAddressLine(i);
		}
		holder.address.setText(addressLine);
		holder.lat.setText("Lat: " + address.getLatitude());
		holder.lng.setText("Lng: " + address.getLongitude());
		
		return convertView;
	}
	
	private static class ViewHolder {
		public TextView address;
		public TextView lat;
		public TextView lng;
	}
}
