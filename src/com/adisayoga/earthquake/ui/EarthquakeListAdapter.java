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

import android.app.AlarmManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.adisayoga.earthquake.R;
import com.adisayoga.earthquake.dto.EarthquakeDTO;
import com.adisayoga.earthquake.dto.EarthquakeTheme;
import com.adisayoga.earthquake.dto.Unit;
import com.adisayoga.earthquake.utils.QuickAction;
import com.adisayoga.earthquake.utils.QuickAction.OnActionItemClickListener;
import com.adisayoga.earthquake.utils.QuickactionItem;
import com.adisayoga.earthquake.utils.TimeUtils;
import com.adisayoga.earthquake.wrapper.Prefs;

/**
 * Adapter untuk daftar gempa.
 * 
 * @author Adi Sayoga
 */
public class EarthquakeListAdapter extends ArrayAdapter<EarthquakeDTO> {
	
	private static final String TAG = "EarthquakeListAdapter";
	private static Prefs prefs;
	
	private final Context context;
	private final int resourceId;
	private final EarthquakeTheme theme;
	private final OnQuickActionListener quickActionListener;
	
	private final LayoutInflater inflater;
	private final TimeUtils timeUtils;
	
	private final String distanceDesc;
	private final String depthDesc;
	
	private int quakePos = 0;
	private QuickAction quickAction;
	private Location location;
	
	public EarthquakeListAdapter(Context context, int resourceId, Location location, 
			List<EarthquakeDTO> quakes, OnQuickActionListener quickActionListener) {
		super(context, resourceId, quakes);
		
		prefs = Prefs.getInstance(context);
		this.context = context;
		this.resourceId = resourceId;
		this.theme = Prefs.getInstance(context).getTheme();
		this.location = location;
		this.quickActionListener = quickActionListener;
		
		inflater = (LayoutInflater) context.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		
		timeUtils = TimeUtils.getInstance(context);
		
		distanceDesc = context.getString(R.string.distance_short);
		depthDesc = context.getString(R.string.depth_short);
		
		setupQuickAction();
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
			holder.severity = convertView.findViewById(R.id.severity);
			holder.magnitude = (TextView) convertView.findViewById(R.id.magnitude);
			holder.region = (TextView) convertView.findViewById(R.id.region);
			holder.date = (TextView) convertView.findViewById(R.id.date);
			holder.distance = (TextView) convertView.findViewById(R.id.distance);
			holder.depth = (TextView) convertView.findViewById(R.id.depth);
			holder.quickActionRegion = (FrameLayout) convertView.findViewById(R.id.frame_magnitude);
			
			convertView.setTag(holder);
		} else {
			// Ambil ViewHolder kembali untuk mendapatkan akses cepat ke view
			holder = (ViewHolder) convertView.getTag();
		}
		
		// Bind data secara efektif dengan holder
		EarthquakeDTO quake = getItem(position);
		bindView(holder, quake, position);
		return convertView;
	}
	
	/**
	 * Tampilkan data gempa ke view.
	 * 
	 * @param view View
	 * @param quake Data gempa
	 */
	private void bindView(ViewHolder holder, EarthquakeDTO quake, final int position) {
		// Update value
		holder.severity.setBackgroundColor(theme.getQuakeColor(quake.magnitude, "88"));
		holder.magnitude.setText(Float.toString(quake.magnitude));
		holder.region.setText(quake.region);
		holder.date.setText(timeUtils.toHumanReadableShort(quake.time));
		String distanceString = "-";
		Unit unit = prefs.getUnit();
		if (location != null) {
			float distance = quake.getLocation().distanceTo(location);
			distanceString = unit.formatNumber(distance, EarthquakeDTO
					.FRACTION_DISTANCE);
		}
		holder.distance.setText(distanceDesc + ": " + distanceString);
		holder.depth.setText(depthDesc + ": " + unit.formatNumber(quake.depth, 
				EarthquakeDTO.FRACTION_DEPTH));
		
		// Tampilkan font tebal untuk data gempa baru, normal untuk yang lama
		long delta = System.currentTimeMillis() - quake.time;
		boolean isNew = delta < AlarmManager.INTERVAL_HALF_DAY;
		if (isNew) {
			holder.magnitude.setTypeface(null, Typeface.BOLD);
			holder.region.setTypeface(null, Typeface.BOLD);
		} else {
			holder.magnitude.setTypeface(null, Typeface.NORMAL);
			holder.region.setTypeface(null, Typeface.NORMAL);
		}
		
		holder.quickActionRegion.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				quickAction.show((View) v.getParent());
				quakePos = position;
			}
		});
	}

	/**
	 * Setup QuickAction.
	 * 
	 * @param view View
	 * @param quake Data gempa
	 */
	private void setupQuickAction() {
		quickAction = new QuickAction(context);
		Resources resources = context.getResources();
		
		/*// Detail gempa
		QuickactionItem detailAction = new QuickactionItem();
		detailAction.setIcon(resources.getDrawable(R.drawable.ic_quickaction_detail));
		detailAction.setTitle(context.getString(R.string.show_detail_short));
		quickAction.addActionItem(detailAction);
		
		// Menampilkan pada peta
		QuickactionItem mapAction = new QuickactionItem();
		mapAction.setIcon(resources.getDrawable(R.drawable.ic_quickaction_map));
		mapAction.setTitle((String) context.getText(R.string.show_map_short));
		quickAction.addActionItem(mapAction);*/
		
		// Share ke Facebook
		QuickactionItem facebookAction = new QuickactionItem();
		facebookAction.setIcon(resources.getDrawable(R.drawable.ic_quickaction_facebook));
		facebookAction.setTitle((String) context.getText(R.string.share_to_facebook_short));
		quickAction.addActionItem(facebookAction);
		
		// Share ke Twitter
		QuickactionItem twitterAction = new QuickactionItem();
		twitterAction.setIcon(resources.getDrawable(R.drawable.ic_quickaction_twitter));
		twitterAction.setTitle((String) context.getText(R.string.share_to_twitter_short));
		quickAction.addActionItem(twitterAction);

		// Kirim email
		QuickactionItem mailAction = new QuickactionItem();
		mailAction.setIcon(resources.getDrawable(R.drawable.ic_quickaction_mail));
		mailAction.setTitle((String) context.getText(R.string.send_mail_short));
		quickAction.addActionItem(mailAction);

		// Kirim SMS
		QuickactionItem smsAction = new QuickactionItem();
		smsAction.setIcon(resources.getDrawable(R.drawable.ic_quickaction_sms));
		smsAction.setTitle((String) context.getText(R.string.send_sms_short));
		quickAction.addActionItem(smsAction);
		
		// Share lainnya
		QuickactionItem shareAction = new QuickactionItem();
		shareAction.setIcon(resources.getDrawable(R.drawable.ic_quickaction_share));
		shareAction.setTitle((String) context.getText(R.string.others));
		quickAction.addActionItem(shareAction);
		
		//setup the action item click listener
		quickAction.setOnActionItemClickListener(new OnActionItemClickListener() {
			@Override
			public void onItemClick(int quickActionPos) {
				Log.d(TAG, "onActionItemClickListener, pos=" + quickActionPos);
				quickActionListener.onItemClick(quakePos, quickActionPos);
			}
		});
	}
	
	/**
	 * Set lokasi saat ini.
	 * 
	 * @param location Lokasi
	 */
	public void setLocation(Location location) {
		this.location = location;
	}
	
	/**
	 * Interface untuk QuickAction.
	 * 
	 * @author Adi Sayoga
	 */
	public interface OnQuickActionListener {
		
		/**
		 * Saat tombol pada QuickAction diklik.
		 * 
		 * @param quakePos Posisi dari list gempa
		 * @param quickActionPos Posisi dari quickAction
		 */
		public void onItemClick(int quakePos, int quickActionPos);
	}
	
	private static class ViewHolder {
		public View severity;
		public TextView magnitude;
		public TextView region;
		public TextView date;
		public TextView distance;
		public TextView depth;
		public FrameLayout quickActionRegion;
	}
}
