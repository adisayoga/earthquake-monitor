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
 
package com.adisayoga.earthquake.providers;

import android.provider.BaseColumns;

/**
 * Berisi konstanta daftar nama field dan field index untuk tabel contact.
 * 
 * @author Adi Sayoga
 */
public class ContactColumns {

	public static final String TABLE_NAME = "contact";
	
	// Nama kolom
	public static final String _ID = BaseColumns._ID;
	public static final String NAME = "name";
	public static final String PHONE_NUMBER = "phone_number";
	public static final String MAIL = "mail";
	
	// Kolom index
	public static final int _ID_INDEX = 0;
	public static final int NAME_INDEX = 1;
	public static final int PHONE_NUMBER_INDEX = 2;
	public static final int MAIL_INDEX = 3;
}
