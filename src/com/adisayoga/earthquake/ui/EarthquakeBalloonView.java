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

import android.content.Context;
import android.location.Location;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adisayoga.earthquake.R;
import com.adisayoga.earthquake.dto.EarthquakeDTO;
import com.adisayoga.earthquake.dto.Unit;
import com.adisayoga.earthquake.utils.TimeUtils;
import com.adisayoga.earthquake.wrapper.Prefs;

/**
 * Class untuk balloon view.
 * 
 * @author Adi Sayoga
 */
public class EarthquakeBalloonView extends FrameLayout {
	
	private static Prefs prefs;
	private final Context context;
	
	private final LinearLayout layout;
	private final TextView magnitudeView;
	private final TextView regionView;
	private final TextView dateView;
	private final TextView distanceView;
	private final TextView depthView;
	
	private final String distanceLabel;
	private final String depthLabel;
	
	/**
	 * Create sebuah earthquake balloon overlay.
	 * 
	 * @param context Activity context
	 * @param bottomOffset Padding bawah (dalam pixel) yang diaplikasikan ketika
	 * merender view ini
	 */
	public EarthquakeBalloonView(Context context, int bottomOffset) {
		super(context);
		this.context = context;
		prefs = Prefs.getInstance(context);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.balloon_view, null);
		
		layout = (LinearLayout) view.findViewById(R.id.layout);
		magnitudeView = (TextView) view.findViewById(R.id.magnitude);
		regionView = (TextView) view.findViewById(R.id.region);
		dateView = (TextView) view.findViewById(R.id.date);
		distanceView = (TextView) view.findViewById(R.id.distance);
		depthView = (TextView) view.findViewById(R.id.depth);
		distanceLabel = context.getString(R.string.distance_short);
		depthLabel = context.getString(R.string.depth_short);
		
		ImageView closeButton = (ImageView) view.findViewById(R.id.close_button);
		closeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				layout.setVisibility(GONE);
			}
		});
		
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, 
				LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.NO_GRAVITY;
		addView(layout, params);
		
		setPadding(10, 0, 10, bottomOffset);
	}
	
	/**
	 * Set data pada balloon view.
	 * 
	 * @param quake Data gempa
	 * @param location Lokasi saat ini
	 */
	public void setData(EarthquakeDTO quake, Location location) {
		layout.setVisibility(VISIBLE);
		
    	if (quake != null) {
			float distance = 0;
			if (location != null) distance = quake.getLocation().distanceTo(location);
			
			Unit unit = prefs.getUnit();
			
			magnitudeView.setText("M:" + quake.magnitude);
			regionView.setText(quake.region);
			dateView.setText(TimeUtils.getInstance(context).toHumanReadable(quake.time));
			distanceView.setText(distanceLabel + ": " + unit.formatNumber(
					distance, EarthquakeDTO.FRACTION_DISTANCE));
			depthView.setText(depthLabel + ": " + unit.formatNumber(
					quake.depth, EarthquakeDTO.FRACTION_DEPTH));
    	}
    }
	
	/**
	 * Menapatkan layout balloon view.
	 * 
	 * @return Layout
	 */
	public LinearLayout getClickRegion() {
		return layout;
	}
}
