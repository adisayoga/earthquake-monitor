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

package com.adisayoga.earthquake.wrapper;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.adisayoga.earthquake.R;
import com.adisayoga.earthquake.dto.EarthquakeDTO;
import com.adisayoga.earthquake.dto.Unit;
import com.adisayoga.earthquake.models.UsgsSource;
import com.adisayoga.earthquake.utils.BaseRequestListener;
import com.adisayoga.earthquake.utils.LocationUtils;
import com.adisayoga.earthquake.utils.SessionEvents;
import com.adisayoga.earthquake.utils.SessionEvents.AuthListener;
import com.adisayoga.earthquake.utils.SessionEvents.LogoutListener;
import com.adisayoga.earthquake.utils.SessionStore;
import com.adisayoga.earthquake.utils.TimeUtils;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

/**
 * Class bantuan untuk memudahkan dalam berhubungan dengan Facebook.
 * 
 * @author Adi Sayoga
 */
public class EarthquakeFacebook {

	public static final String TAG = "EarthquakeFacebook";
	public static final String FACEBOOK_APP_ID = "239877329388468";
	public static final String[] FACEBOOK_PERMISSION = new String[] { "publish_stream" };
	
	private static Prefs prefs;
	
	private final Context context;
	private final Handler handler = new Handler();
	private final Facebook facebook = new Facebook(FACEBOOK_APP_ID);
	private final SessionListener sessionListener = new SessionListener();
	
	public EarthquakeFacebook(Context context) {
		this.context = context;
		prefs = Prefs.getInstance(context);
		
		SessionStore.restore(facebook, context);
        SessionEvents.addAuthListener(sessionListener);
        SessionEvents.addLogoutListener(sessionListener);
	}
	
	/**
	 * Posting data gempa ke wall. Sebelumnya akan ditampilkan dialog apa yang 
	 * dipikirkan.
	 * 
	 * @param activity Activity
	 * @param quake Data gempa
	 * @param location Lokasi saat ini
	 */
	public void postQuake(final Activity activity, final EarthquakeDTO quake, 
			final Location location) {
		// Setup dialog
		LayoutInflater inflater = (LayoutInflater) activity.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.share_message, null);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setView(layout);
		builder.setTitle(R.string.share_to_facebook);
		
		builder.setPositiveButton(R.string.share, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				EditText messageText = (EditText) layout.findViewById(R.id.message);
				final String message = messageText.getText().toString();
				
				new Thread() {
					@Override
					public void run() {
						// Generate parameter pada thread berbeda
						final Bundle params = genereateParams(quake, message, location);
						handler.post(new Runnable() {
							@Override
							public void run() {
								postMessageOrLogin(activity, params);
							}
						});
					}
				}.start();
			}
		});
		
		builder.show();
    }

	/**
	 * Posting pesan ke wall, jika belum login dan activity tidak null maka akan 
	 * otomatis diarahkan untuk login, setelah itu baru pesan diposting.
	 * 
	 * @param activity Activity
	 * @param params Key-value string parameter
	 */
	public void postMessageOrLogin(Activity activity, final Bundle params) {
		if (facebook.isSessionValid()) {
			postMessageInThread(params);
		} else if (activity != null) {
			SessionEvents.AuthListener listener = new SessionEvents.AuthListener() {
				@Override
				public void onAuthSucceed() {
					postMessageInThread(params);
				}
				
				@Override
				public void onAuthFail(String error) {
					Toast.makeText(context, R.string.auth_fail, Toast.LENGTH_SHORT).show();
					Log.w(TAG, error);
				}
			};
			SessionEvents.addAuthListener(listener);
			login(activity);
		} else {
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(context, R.string.post_fail, Toast.LENGTH_SHORT).show();
					Log.d(TAG, "Belum login, tidak dapat login karena activity null");
				}
			});
		}
	}

	/**
	 * Posting pesan ke wall (pada thread berbeda).
	 * 
	 * @param params Key-value string parameter
	 */
	public void postMessageInThread(final Bundle params) {
		new Thread() {
			@Override
			public void run() {
				if (postMessage(params)) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(context, R.string.post_sent, 
									Toast.LENGTH_SHORT).show();
						}
					});
				} else {
					handler.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(context, R.string.post_fail, 
									Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		}.start();
	}
	
	/**
	 * Posting pesan ke wall.
	 * 
	 * @param params Key-value string parameter
	 * @return True jika posting berhasil, false jika gagal
	 */
	public boolean postMessage(Bundle params) {
		try {
			String response = facebook.request("me/feed", params, "POST");
			Log.i(TAG, "Post sent: " + params.getString("description"));
			Log.d(TAG, "response: " + response);
			return true;
			
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
			return false;
		}
	}
	
	/**
	 * Menghasilkan parameter untuk diposting ke wall.
	 * 
	 * @param quake Data gempa
	 * @param message Pesan tambahan yang diinputkan user
	 * @param location Lokasi saat ini
	 * @param currentAddress Alamat  lokasi saat ini
	 * 
	 * @return Parameter
	 */
	public Bundle genereateParams(EarthquakeDTO quake, String message, Location location) {
		final Bundle params = new Bundle();
    	
		// message
		if (message != null && !message.equals("")) 
			params.putString("message", message);
		
		// link
    	params.putString("link", UsgsSource.getExternalUri(quake).toString());
    	
    	// caption
    	String date = TimeUtils.getInstance(context).toHumanReadable(quake.time);
    	params.putString("caption", date);
    	
    	// picture
    	URL pictureUrl = UsgsSource.getGlobeURL(quake);
    	if (pictureUrl != null) params.putString("picture", pictureUrl.toString());
    	
    	// description
		params.putString("description", generateDescription(quake, location));
		
		return params;
	}
	
	/**
	 * Men-generate description yaitu terdiri dari lokasi gempa, kedalaman gempa,
	 * jarak user dengan gempa itu, dan alamat dimana user saat ini.
	 * 
	 * @param quake Data gempa
	 * @param location Lokasi user saat ini
	 * @return String description
	 */
	private String generateDescription(EarthquakeDTO quake, Location location) {
		StringBuilder description = new StringBuilder();
    	Unit unit = prefs.getUnit();
    	
    	// lokasi gempa
    	String quakeLocation = LocationUtils.getInstance(context).formatLocation(
    			quake.latitude, quake.longitude, true);
    	description.append(context.getString(R.string.location));
    	description.append(" (").append(quakeLocation).append(")");
    	
    	// kedalaman gempa
    	String depth = unit.formatNumber(quake.depth, EarthquakeDTO
    			.FRACTION_DEPTH);
    	description.append(", ").append(context.getString(R.string.depth));
    	description.append(" ").append(depth);
    	
    	if (location != null) {
	    	// jarak gempa
			float distance = quake.getLocation().distanceTo(location);
			String distanceString = unit.formatNumber(distance, EarthquakeDTO
	    			.FRACTION_DEPTH);;
			description.append(", ").append(context.getString(R.string.distance));
			description.append(" ").append(distanceString);
			
			// Mendapatkan alamat berdasarkan koordinat latitude dan longitude
			try {
				Geocoder geocoder = new Geocoder(context);
				List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), 
						location.getLongitude(), 5);
				
				if (addresses != null && addresses.size() > 0) {
					// Kita hanya memerlukan satu alamat
					String addressLine = LocationUtils.getAddressLine(addresses.get(0));
					if (!addressLine.equals("")) {
						// Tambahkan ke description
						description.append(" ").append(context.getString(R.string.conj_from));
						description.append(" ").append(addressLine);
					}
				}
			} catch (IOException e) {
				// Terdapat error pada geocoder, abaikan
				Log.e(TAG, e.getMessage(), e);
			}
    	}
    	
    	return description.toString();
	}
	
	/**
	 * Login ke facebook
	 * 
	 * @param activity Activity
	 */
	public void login(Activity activity) {
		if (facebook.isSessionValid()) {
			SessionEvents.onLoginSuccess();
			return;
		}
		
		facebook.authorize(activity, FACEBOOK_PERMISSION, new DialogListener() {
			@Override
			public void onComplete(Bundle values) {
				SessionEvents.onLoginSuccess();
			}

			@Override
			public void onFacebookError(FacebookError e) {
				SessionEvents.onLoginError(e.getMessage());
			}

			@Override
			public void onError(DialogError e) {
				SessionEvents.onLoginError(e.getMessage());
			}

			@Override
			public void onCancel() {
				SessionEvents.onLoginError("Action cancelled");
			}
		});
	}
	
	/**
	 * Menghapus access token pada memory dan membersihkan browser cookies
	 */
	public void logout() {
		SessionEvents.onLogoutBegin();
		AsyncFacebookRunner runner = new AsyncFacebookRunner(facebook);
		runner.logout(context, new BaseRequestListener() {
			@Override
			public void onComplete(String response, Object state) {
				// callback harus berjalan pada UI thread, tidak pada background thread
				handler.post(new Runnable() {
					@Override
					public void run() {
						SessionEvents.onLogoutFinish();
					}
				});
			}
		});
	}
	
	/**
	 * Mendapatkan objek facebook
	 * @return Facebook
	 */
	public Facebook getFacebook() {
		return facebook;
	}

	/**
	 * Mendapatkan apakah sesi Facebook valid atau tidak.
	 * 
	 * @return True jika valid, false jika tidak
	 */
	public boolean isSessionValid() {
		return facebook.isSessionValid();
	}
	
	private class SessionListener implements AuthListener, LogoutListener {
        
        @Override
		public void onAuthSucceed() {
            SessionStore.save(facebook, context);
        }

        @Override
		public void onAuthFail(String error) {
        }
        
        @Override
		public void onLogoutBegin() {           
        }
        
        @Override
		public void onLogoutFinish() {
            SessionStore.clear(context);
        }
    }
}
