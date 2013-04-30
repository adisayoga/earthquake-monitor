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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.adisayoga.earthquake.R;
import com.adisayoga.earthquake.dto.ContactDTO;
import com.adisayoga.earthquake.models.ContactModel;
import com.adisayoga.earthquake.providers.ContactColumns;
import com.adisayoga.earthquake.providers.ContactProvider;
import com.adisayoga.earthquake.wrapper.Prefs;

/**
 * Activity daftar kontak.
 * 
 * @author Adi Sayoga
 */
public class ContactActivity extends Activity {

	private static final String TAG = "ContactActivity";
	private static final int PICK_CONTACT_REQUEST = 1;
	private static final int NEW_CONTACT_REQUEST = 2;
	
	private static Prefs prefs;
	
	private final List<ContactDTO> contacts = new ArrayList<ContactDTO>();
	private ContactAdapter adapter;
	private ListView list;
	private LinearLayout noContact;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		
		prefs = Prefs.getInstance(this);
		setTheme(prefs.getTheme().resId);
		setContentView(R.layout.contact);
		
		setupListAdapter();
		
		// Load data dari database
        noContact = (LinearLayout) findViewById(R.id.no_contact);
        updateListContact();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = new MenuInflater(this);
    	inflater.inflate(R.menu.contact_menu, menu);
    	return true;
	}

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	super.onOptionsItemSelected(item);
    	
    	switch (item.getItemId()) {
    	case R.id.contact_add:
    		Intent intentContactPick = new Intent(Intent.ACTION_PICK, 
					ContactsContract.Contacts.CONTENT_URI); 
			startActivityForResult(intentContactPick, PICK_CONTACT_REQUEST);
    		return true;
    	case R.id.contact_new:
    		Intent intentContactNew = new Intent(this, ContactEditActivity.class);
    		startActivityForResult(intentContactNew, NEW_CONTACT_REQUEST);
    		return true;
    	}
    	return false;
    }

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.d(TAG, "RequestCode = " + requestCode + ", resultCode=" + resultCode);
    	switch (requestCode) {
    	// Mengambil data dari daftar kontak
    	case PICK_CONTACT_REQUEST:
    		if (data != null && resultCode == Activity.RESULT_OK) {
    			pickContact(data);
    		}
	    	break;
	    // Menginput kontak manual
    	case NEW_CONTACT_REQUEST:
    		if (resultCode == Activity.RESULT_OK) updateListContact();
    		break;
    	}
    }
    
    /**
     * Mengambil data kontak dari daftar kontak telepon.
     * 
     * @param data Data
     */
    private void pickContact(Intent data) {
    	Log.i(TAG, data.getDataString());
		ContactModel contactTable = new ContactModel(this);
		
		ContactDTO contact = contactTable.getSystemContact(data.getData());
		if (contact != null) {
			if (!contact.phoneNumber.equals("") || !contact.mail.equals("")) {
				addContact(contact);
				updateListContact();
				return;
			}
		}
		
		Toast.makeText(this, R.string.no_contact, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Menambahkan data contact baru ke database.
     * 
     * @param contact Contact
     * @return id tabel yang baru saja diinsert
     */
    private long addContact(ContactDTO contact) {
    	Log.d(TAG, "Menyimpan kontak...");
    	contacts.add(contact);
    	
    	ContentValues values = new ContentValues();
		values.put(ContactColumns.NAME, contact.name);
		values.put(ContactColumns.PHONE_NUMBER, contact.phoneNumber);
		values.put(ContactColumns.MAIL, contact.mail);
		
    	ContentResolver resolver = getContentResolver();
		Uri uri = resolver.insert(ContactProvider.CONTENT_URI, values);
		
		// Mendapatkan id dari data yang baru saja diinsert
		long segment = Long.parseLong(uri.getPathSegments().get(1));
		return segment;
    }
    
	/**
	 * Setup list adapter.
	 */
	private void setupListAdapter() {
		list = (ListView) findViewById(R.id.listview);
		
		adapter = new ContactAdapter(this, R.layout.contact_detail, contacts);
        list.setAdapter(adapter);
        list.setOnItemClickListener(itemClickListener);
		list.setOnItemLongClickListener(itemLongClickListener);
	}
	
	/**
	 * Load data dari provider, kemudian update list. (pada background thread)
	 */
	private void updateListContact() {
		new AsyncTask<Void, Void, List<ContactDTO>>() {

			@Override
			protected List<ContactDTO> doInBackground(Void... params) {
				// Load data dari provider
    			ContactModel table = new ContactModel(ContactActivity.this);
    			List<ContactDTO> newContacts = table.getContacts();
    			
				return newContacts;
			}
    		
			@Override
			protected void onPostExecute(List<ContactDTO> newContacts) {
				// Tambahkan ke contacts
    			contacts.clear();
    			if (newContacts != null) {
    				for (ContactDTO contact : newContacts) {
    					contacts.add(contact);
    				}
    			}

    			// Perlihatkan atau sembunyikan view
				if (contacts.size() > 0) {
		    		Log.d(TAG, "Terdapat data, memperlihatkan list");
		    		noContact.setVisibility(View.GONE);
		    		list.setVisibility(View.VISIBLE);
		    	} else {
		    		Log.d(TAG, "Tidak terdapat data, menyembunyikan list");
		    		noContact.setVisibility(View.VISIBLE);
		    		list.setVisibility(View.GONE);
		    	}
		    	adapter.notifyDataSetChanged();
			}
			
		}.execute();
	}
	
	/**
	 * Memanggil activity edit kontak untuk mengedit data ini.
	 * 
	 * @param contact Data kontak
	 */
	private void editContact(ContactDTO contact) {
		Intent intent = new Intent(this, ContactEditActivity.class);
		intent.putExtra(ContactColumns.TABLE_NAME, contact);
		startActivityForResult(intent, NEW_CONTACT_REQUEST);
	}
	
	/**
	 * Menghapus data kontak.
	 * 
	 * @param contact Data kontak
	 */
	private void deleteContact(ContactDTO contact) {
		ContentResolver resolver = getContentResolver();
		resolver.delete(ContactProvider.CONTENT_URI, ContactColumns._ID + " = ?", 
				new String[] { "" + contact.id });
	}

	/**
	 * Listener saat item list diklik.
	 */
	private final AdapterView.OnItemClickListener itemClickListener 
			= new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			editContact(contacts.get(position));
		}
	};
	
	/**
	 * Listener untuk menampilkan context menu.
	 */
	private final AdapterView.OnItemLongClickListener itemLongClickListener
			= new AdapterView.OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			final ContactDTO contact = contacts.get(position);
			AlertDialog.Builder builder = new AlertDialog.Builder(ContactActivity.this);
			builder.setTitle(contact.name);
			builder.setItems(R.array.contact_list_options, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0: // Edit kontak
						editContact(contact);
						updateListContact();
						break;
					case 1: // Hapus kontak
						deleteContact(contact);
						updateListContact();
						break;
					}
				}
			});
			builder.show();
			return true;
		}
	};
}
