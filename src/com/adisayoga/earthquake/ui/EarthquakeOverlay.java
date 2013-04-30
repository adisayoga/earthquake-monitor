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

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.adisayoga.earthquake.dto.EarthquakeDTO;
import com.adisayoga.earthquake.dto.EarthquakeTheme;
import com.adisayoga.earthquake.wrapper.DrawValues;
import com.adisayoga.earthquake.wrapper.Prefs;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MapView.LayoutParams;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

/**
 * Overlay untuk titik gempa.
 * 
 * @author Adi Sayoga
 */
public class EarthquakeOverlay extends ItemizedOverlay<OverlayItem> {
	
	@SuppressWarnings("unused")
	private static final String TAG = "EarthquakeOverlay";
	
	private static final int TEXT_SIZE = 10;
	private static final int PADDING = 2;
	
	private final EarthquakeTheme theme;
	
	private final Point point = new Point();
	private final MapView mapView;
	private final MapController controller;
	private EarthquakeBalloonView balloonView = null;
	private View clickRegion;
	private int viewOffset = 0;
	
	private final List<OverlayItem> items = new ArrayList<OverlayItem>();
	private final List<EarthquakeDTO> quakes = new ArrayList<EarthquakeDTO>();
	private Location location;
	
	public EarthquakeOverlay(Drawable defaultMarker, MapView mapView, Location location) {
		super(boundCenterBottom(defaultMarker));
		
		this.mapView = mapView;
		this.controller = mapView.getController();
		this.location = location;
		
		Prefs prefs = Prefs.getInstance(mapView.getContext());
		theme = prefs.getTheme();
	}
	
	@Override
	protected boolean onTap(int index) {
		if (balloonView == null) {
			balloonView =  new EarthquakeBalloonView(mapView.getContext(), viewOffset);
			mapView.addView(balloonView);
			
			clickRegion = balloonView.getClickRegion();
			clickRegion.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View view, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						// Toggle state
						Drawable drawable = view.getBackground();
						int[] states = drawable.getState();
						if (states.length == 0) {
							states = new int[] { android.R.attr.state_pressed };
						} else {
							states = new int[] {}; 
						}
						if (drawable.setState(states)) drawable.invalidateSelf();
						return true;
					}
					
					return false;
				}
			});
		}
		
		// Reset state
		Drawable drawable = clickRegion.getBackground();
		drawable.setState(new int[] {});
		drawable.invalidateSelf();
		
		GeoPoint point = createItem(index).getPoint();
		MapView.LayoutParams params = new MapView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams
				.WRAP_CONTENT, point, LayoutParams.BOTTOM_CENTER);
		params.mode = LayoutParams.MODE_MAP;
		balloonView.setLayoutParams(params);
		balloonView.setData(quakes.get(index), location);
		
		List<Overlay> overlays = mapView.getOverlays();
		if (overlays.size() > 1) hideOtherBalloons(overlays);
		
		balloonView.setVisibility(View.VISIBLE);
		controller.animateTo(point);
		return true;
	}
	
	/**
	 * Sembunikan balloon view lainnya.
	 * 
	 * @param overlays Daftar overlay
	 */
	private void hideOtherBalloons(List<Overlay> overlays) {
		for (Overlay overlay : overlays) {
			if (overlay instanceof EarthquakeOverlay && overlay != this) {
				((EarthquakeOverlay) overlay).hideBalloon();
			}
		}
	}
	
	/**
	 * Sembunyikan balloon view ini.
	 */
	private void hideBalloon() {
		if (balloonView != null) balloonView.setVisibility(View.GONE);
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (shadow) return;
		
		for (int i = items.size() - 1; i >= 0; i--) {
			drawQuake(canvas, mapView, quakes.get(i), theme);
		}
	}
	
	/**
	 * Gambar titik gempa pada peta.
	 * 
	 * @param canvas Canvas
	 * @param mapView MapView
	 * @param quake Data gempa
	 * @param theme Tema yang digunakan
	 */
	private void drawQuake(Canvas canvas, MapView mapView, EarthquakeDTO quake, 
			EarthquakeTheme theme) {
		
		Projection projection = mapView.getProjection();
		projection.toPixels(quake.getPoint(), point);
		
		int height = mapView.getHeight();
		int width = mapView.getWidth();
		int scrollX = mapView.getScrollX();
		int scrollY = mapView.getScrollY();
		
		if (point.x < scrollX || point.x > scrollX + width) return;
		if (point.y < scrollY || point.y > scrollY + height) return;
		
		DrawValues.prepare(theme, TEXT_SIZE, quake.magnitude, quake.time);
		
		int zoom = mapView.getZoomLevel();
		int radius = (zoom >= 6) ? 4 : DrawValues.markRadius;
		
		canvas.drawCircle(point.x, point.y, radius + 2, DrawValues.markGlowPaint);
		canvas.drawCircle(point.x, point.y, radius + 1, DrawValues.markOutlinePaint);
		canvas.drawCircle(point.x, point.y, radius, DrawValues.markPaint);
		
		int dmgRadius = (int) projection.metersToEquatorPixels(DrawValues.dmgMeters);
		if (dmgRadius > 3 * DrawValues.markRadius) {
			canvas.drawCircle(point.x, point.y, dmgRadius, DrawValues.feelOutlinePaint);
			canvas.drawCircle(point.x, point.y, dmgRadius, DrawValues.feelPaint);
		}
		
		if (zoom >= 6 && DrawValues.text != "") {
			int x = point.x;
			int y = point.y + radius + TEXT_SIZE + PADDING * 3;
			canvas.drawText(DrawValues.text, x, y, DrawValues.textOutlinePaint);
			canvas.drawText(DrawValues.text, x, y, DrawValues.textPaint);
		}
	}
	
	/**
	 * Tambahkan overlay.
	 * 
	 * @param overlay Overlay
	 */
	public void addOverlay(OverlayItem overlay) {
		items.add(overlay);
		populate();
	}
	
	/**
	 * Tambahkan data gempa.
	 * 
	 * @param quake Data Gempa
	 */
	public void addQuake(EarthquakeDTO quake) {
		quakes.add(quake);
		
		OverlayItem overlay = new OverlayItem(quake.getPoint(), "", "");
		items.add(overlay);
		populate();
	}
	
	/**
	 * Hapus/kosongkan overlay dan data gempa
	 */
	public void clear() {
		quakes.clear();
		items.clear();
		populate();
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
	 * Set offset bawah bolloon view.
	 * 
	 * @param pixels Offset dalam pixels
	 */
	public void setBalloonBotomOffset(int pixels) {
		this.viewOffset = pixels;
	}
	
	@Override
	protected OverlayItem createItem(int i) {
		return (items.size() > 0) ? items.get(i) : null;
	}

	@Override
	public int size() {
		return items.size();
	}

}
