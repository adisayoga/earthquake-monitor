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
 
package com.adisayoga.earthquake.models;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import com.adisayoga.earthquake.dto.ContactDTO;
import com.adisayoga.earthquake.providers.ContactColumns;
import com.adisayoga.earthquake.providers.ContactProvider;

/**
 * Class yang digunakan untuk memudahkan mendapatkan data dari provider daftar
 * kontak.
 * 
 * @author Adi Sayoga
 */
public class ContactModel {
	private static final String TAG = "ContactModel";
	
	private final Context context;
	
	public ContactModel(Context context) {
		this.context = context;
	}
	
	/**
	 * Mendapatkan data kontak.
	 * 
	 * @return contacts List kontak
	 */
	public List<ContactDTO> getContacts() {
		Log.i(TAG, "Mengambil data dari provider...");
    	
		ContentResolver resolver = context.getContentResolver();
    	Cursor cursor = resolver.query(ContactProvider.CONTENT_URI, null, null, null, null);
    	
    	List<ContactDTO> contacts = new ArrayList<ContactDTO>();
		while (cursor.moveToNext()) {
    		long id = cursor.getLong(ContactColumns._ID_INDEX);
    		String name = cursor.getString(ContactColumns.NAME_INDEX);
    		String phone = cursor.getString(ContactColumns.PHONE_NUMBER_INDEX);
    		String email = cursor.getString(ContactColumns.MAIL_INDEX);
    		
    		// Tambahkan ke arraylist
    		ContactDTO contact = new ContactDTO(id, name, phone, email);
    		contacts.add(contact);
    	}
		cursor.close();
    	
    	Log.d(TAG, "Selesai mengambil data, " + cursor.getCount() + " items");
    	return contacts;
	}
	
	/**
	 * Mendapatkan data nomor telepon
	 * 
	 * @return Daftar nomor telepon
	 */
	public String[] getPhones() {
		Log.i(TAG, "Mengambil data dari provider...");
    	
		ContentResolver resolver = context.getContentResolver();
    	Cursor cursor = resolver.query(ContactProvider.CONTENT_URI, null, null, null, null);
    	
    	String phones = "";
		while (cursor.moveToNext()) {
    		String phone = cursor.getString(ContactColumns.PHONE_NUMBER_INDEX);
    		if (!phones.equals("") && !phone.equals("")) phones += ",";
    		phones += phone;
    	}
		cursor.close();
    	
    	Log.d(TAG, "Selesai mengambil data, " + cursor.getCount() + " items");
    	if (phones == "") return null;
    	return phones.replace(" ", "").split(",");
	}
	
	/**
	 * Mendapatkan data email
	 * 
	 * @return Daftar email
	 */
	public String[] getMails() {
		Log.i(TAG, "Mengambil data dari provider...");

		ContentResolver resolver = context.getContentResolver();
    	Cursor cursor = resolver.query(ContactProvider.CONTENT_URI, null, null, null, null);
    	
    	String mails = "";
		while (cursor.moveToNext()) {
    		String mail = cursor.getString(ContactColumns.MAIL_INDEX);
    		if (!mails.equals("") && !mail.equals("")) mails += ",";
    		mails += mail;
    	}
		cursor.close();
    	
    	Log.d(TAG, "Selesai mengambil data, " + cursor.getCount() + " items");
    	if (mails == "") return null;
    	return mails.replace(" ", "").split(",");
	}
	
	/**
	 * Mendapatkan data kontak telepon.
	 * 
	 * @param uri Uri kontak
	 * @return Data kontak
	 */
	public ContactDTO getSystemContact(Uri uri) {
		ContactDTO contact = null;
		
		ContentResolver resolver = context.getContentResolver();
		Cursor contactCursor = resolver.query(uri, null, null, null, null);
		
	    if (contactCursor.moveToNext()) {
    		long id = contactCursor.getLong(contactCursor.getColumnIndex(
    				ContactsContract.Contacts._ID));
    		String name = contactCursor.getString(contactCursor.getColumnIndex(
    				ContactsContract.Contacts.DISPLAY_NAME));
    		
    		// Nomor telepon
    		String phones = "";
    		String hasPhone = contactCursor.getString(contactCursor.getColumnIndex(
    				ContactsContract.Contacts.HAS_PHONE_NUMBER));
			if (hasPhone.equals("1")) {
				Cursor phoneCursor = resolver.query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " 
						+ id, null, null);
				
				while (phoneCursor.moveToNext()) {
					String phone = phoneCursor.getString(phoneCursor.getColumnIndex(
							ContactsContract.CommonDataKinds.Phone.NUMBER));
					if (phone == null) phone = "";
					if (!phones.equals("") && !phone.equals("")) phones += ", ";
					phones += phone;
				}
				phoneCursor.close();
			}
			
			// Email
			String mails = "";
			Cursor mailCursor = resolver.query(
					ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, 
					ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = "
					+ id, null, null);
			while (mailCursor.moveToNext()) {
				String mail = mailCursor.getString(mailCursor.getColumnIndex(
						ContactsContract.CommonDataKinds.Email.DATA));
				if (mail == null) mail = "";
				if (!mails.equals("") && !mail.equals("")) mails += ", ";
				mails += mail;
			}
			mailCursor.close();
			
			Log.d(TAG, "name=" + name + ", phone=" + phones + ", mail=" + mails);
			contact = new ContactDTO(id, name, phones, mails);
	    }
	    contactCursor.close();
	    
	    return contact;
	}
}
