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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Data Transfer Object untuk data kontak.
 * 
 * @author Adi Sayoga
 */
public class ContactDTO implements Parcelable {

	public long id;
	public String name;
	public String phoneNumber;
	public String mail;
	
	public ContactDTO(long id, String name, String phoneNumber, String mail) {
		this.id = id;
		this.name = name;
		this.phoneNumber = (phoneNumber == null) ? "" : phoneNumber;
		this.mail = (mail == null) ? "" : mail;
	}
	
	public ContactDTO(Parcel in) {
		id = in.readLong();
		name = in.readString();
		phoneNumber = in.readString();
		mail = in.readString();
	}
	
	@Override
	public void writeToParcel(Parcel out, int flag) {
		out.writeLong(id);
		out.writeString(name);
		out.writeString(phoneNumber);
		out.writeString(mail);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<ContactDTO> CREATOR = new Parcelable
			.Creator<ContactDTO>() {
		@Override
		public ContactDTO createFromParcel(Parcel in) {
		    return new ContactDTO(in);
		}
		
		@Override
		public ContactDTO[] newArray(int size) {
		    return new ContactDTO[size];
		}
	};
}
