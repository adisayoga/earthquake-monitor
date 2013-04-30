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

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.adisayoga.earthquake.utils.LocationUtils;
import com.google.android.maps.GeoPoint;

/**
 * Data Transfer Object untuk data gempa.
 * 
 * @author Adi Sayoga
 */
public class EarthquakeDTO implements Parcelable {
	
	public static final int FRACTION_DEPTH = 1;
	public static final int FRACTION_DISTANCE = 0;
	
	public long id;
	public String source;
	public String eqid;
	public String version;
	
	public long time;
	public double latitude;
	public double longitude;
	public float magnitude;
	public float depth;
	public int nst;
	public String region;
	
	public EarthquakeDTO(long id, String source, String eqid, String version, 
			long time, double latitude, double longitude, float magnitude, 
			float depth, int nst, String region) {
		this.id = id;
		this.source = source;
		this.eqid = eqid;
		this.version = version;
		
		this.time = time;
		this.latitude = latitude;
		this.longitude = longitude;
		this.magnitude = magnitude;
		this.depth = depth;
		this.nst = nst;
		this.region = region;
	}

	/**
	 * Mendapatkan lokasi gempa bumi (GeoPoint).
	 * 
	 * @return GeoPoint
	 */
	public GeoPoint getPoint() {
		return new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6));
	}
	
	/**
	 * Mendapatkan lokasi gempa bumi (Location).
	 * 
	 * @return Location
	 */
	public Location getLocation() {
		Location location = new Location(LocationUtils.CONSTRUCT_PROVIDER);
		location.setLatitude(latitude);
		location.setLongitude(longitude);
		return location;
	}
	
	public EarthquakeDTO(Parcel in) {
		id = in.readLong();
		source = in.readString();
		eqid = in.readString();
		version = in.readString();
		
		time = in.readLong();
		latitude = in.readDouble();
		longitude = in.readDouble();
		magnitude = in.readFloat();
		depth = in.readFloat();
		nst = in.readInt();
		region = in.readString();
	}
	
	@Override
	public void writeToParcel(Parcel out, int flag) {
		out.writeLong(id);
		out.writeString(source);
		out.writeString(eqid);
		out.writeString(version);
		
		out.writeLong(time);
		out.writeDouble(latitude);
		out.writeDouble(longitude);
		out.writeFloat(magnitude);
		out.writeFloat(depth);
		out.writeInt(nst);
		out.writeString(region);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<EarthquakeDTO> CREATOR 
			= new Parcelable.Creator<EarthquakeDTO>() {
		@Override
		public EarthquakeDTO createFromParcel(Parcel in) {
		    return new EarthquakeDTO(in);
		}
		
		@Override
		public EarthquakeDTO[] newArray(int size) {
		    return new EarthquakeDTO[size];
		}
	};
}
