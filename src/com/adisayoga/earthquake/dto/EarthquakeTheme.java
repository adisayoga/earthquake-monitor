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
 
package com.adisayoga.earthquake.dto;

import android.graphics.Color;

import com.adisayoga.earthquake.R;

/**
 * Tema yang digunakan.
 * 
 * @author Adi Sayoga
 */
public enum EarthquakeTheme {
	DEFAULT(R.style.Theme, 0), LIGHT(R.style.Theme_Light, 1), BLUE(R.style.Theme_Blue, 2);
	
	/** Resource style theme */
	public final int resId;
	/** Posisi index theme */
	public final int position;
	
	// Warna kekuatan gempa
	private final String shockColor[] = new String[]       { "46ef00", "46ef00", "46ef00" };
	private final String smallColor[] = new String[]       { "bcfd00", "bcfd00", "bcfd00" };
	private final String strongColor[] = new String[]      { "eeff00", "eeff00", "eeff00" };
	private final String damageColor[] = new String[]      { "fbdd00", "fbdd00", "fbdd00" };
	private final String destructiveColor[] = new String[] { "fca900", "fca900", "fca900" };
	private final String majorColor[] = new String[]       { "fc5a00", "fc5a00", "fc5a00" };
	private final String dissasterColor[] = new String[]   { "ec0000", "ec0000", "ec0000" };
	
	private EarthquakeTheme(int resId, int position) {
		this.resId = resId;
		this.position = position;
	}
	
	/**
	 * Mendapatkan warna sesuai dengan magnitudo gempa.
	 * 
	 * @param magnitude Magnitudo gempa
	 * @return color-int
	 */
	public int getQuakeColor(float magnitude) {
		return getQuakeColor(magnitude, "");
	}
	
	/**
	 * Mendapatkan warna (dengan alpha) sesuai dengan magnitudo gempa.
	 * 
	 * @param magnitude Magnitudo gempa
	 * @param alpha Warna alpha dalam hexa (00-FF)
	 * @return color-int
	 */
	public int getQuakeColor(float magnitude, String alpha) {
		String colorString;
		
		if (magnitude < 3) {
			colorString = shockColor[position];
		} else if (magnitude < 4) {
			colorString = smallColor[position];
		} else if (magnitude < 5) {
			colorString = strongColor[position];
		} else if (magnitude < 6) {
			colorString = damageColor[position];
		} else if (magnitude < 7) {
			colorString = destructiveColor[position];
		} else if (magnitude < 8) {
			colorString = majorColor[position];
		} else {
			colorString = dissasterColor[position];
		}
		
		return Color.parseColor("#" + alpha + colorString);
	}
}
