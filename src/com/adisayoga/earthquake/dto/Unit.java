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

import java.text.NumberFormat;

/**
 * Standar unit yang digunakan, metric atau US, atau default untuk unit yang sesuai
 * dengan preference.
 * 
 * @author Adi Sayoga
 */
public enum Unit {
	METRIC (0), US (1);
	
	public final int position;
	
	private Unit(int position) {
		this.position = position;
	}
	
	/**
	 * Format nomor.
	 * 
	 * @param value Nomor
	 * @param fractionDigit Digit pecahan
	 * @return Nomor yang sudah diformat
	 */
	public String formatNumber(float value, int fractionDigit) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(fractionDigit);
		
		if (position == METRIC.position) {
			if (value >= 1000) {
				return nf.format(value / 1000) + "km";
			} else {
				return nf.format(value) + "m";
			}
		} else if (position == US.position) {
			if (value > 1609) {
				return nf.format(value / 1609.344f) + "mi";
			} else {
				return nf.format(value / 0.3048f) + "ft";
			}
		}
		
		return Float.toString(value);
	}
}
