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

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.adisayoga.earthquake.R;
import com.adisayoga.earthquake.dto.EarthquakeDTO;

/**
 * Class bantuan untuk memudahkan berhubungan dengan Twitter.
 * 
 * @author Adi Sayoga
 */
public class EarthquakeTwitter {

	private static final String TAG = "EarthquakeTwitter";
	
	/** Consumer Key dihasilkan ketika mendaftarkan aplikasi pada  
	    <a href="https://dev.twitter.com/apps/">https://dev.twitter.com/apps/</a> */
	public static final String CONSUMER_KEY = "JduBsbLPSVG6zuKgq8Qbw";
	
	/** Consumer Secret dihasilkan ketika mendaftarkan aplikasi pada  
	    <a href="https://dev.twitter.com/apps/">https://dev.twitter.com/apps/</a> */
	public static final String CONSUMER_SECRET = "1wKD8Slh9a3mu61DQrtN9cOlslhXnwEY2wuqZpz80a0";

	private static final int DEFAULT_REQUEST_CODE = 42;
	private static Prefs prefs;
	
	private int requestCode = DEFAULT_REQUEST_CODE;
	private AuthListener listener;
	
	private final Handler handler = new Handler();
	private final Context context;
	private final Twitter twitter;
	
	public EarthquakeTwitter(Context context) {
		this.context = context;
		twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
		
		prefs = Prefs.getInstance(context);
	}
	/**
	 * Login ke twitter.
	 * <p>
	 * Jika sudah di-otorisasi, gunakan user sebelumnya, jika tidak mulai
	 * activity untuk menginputkan username dan password.
	 * 
	 * @param listener Listener saat login berhasil/gagal
	 */
	public void login(final Activity activity, final AuthListener listener) {
		// Coba login dengan user sebelumnya
		loginAuthorizedUser(new AuthListener() {
			@Override
			public void onAuthComplete() {
				listener.onAuthComplete();
			}

			@Override
			public void onAuthFail() {
				// Mulai activity baru untuk login user baru
				Log.w(TAG, "Login authorized user gagal");
				if (activity != null)
					loginNewUser(activity, listener);
			}
		});
	}
	
	/**
	 * User sebelumnya telah diberikan ijin untuk menggunakan Twitter. Oleh
	 * karena itu kita mengambil mandat ini dan mengisinya ke helper Twitter4j.
	 * 
	 * @param listener Listener saat login berhasil/gagal
	 */
	public void loginAuthorizedUser(final AuthListener listener) {
		Log.d(TAG, "Login authorized user...");
		
		String[] tokens = prefs.getTwitterToken();
		String token = tokens[0];
		String secret = tokens[1];
		
		if (token.equals("") || secret.equals("")) {
			// Token pada preference kosong, return
			listener.onAuthFail();
			return;
		}
		
		try {
			// Buat akses token twitter dari mandat yang kita dapat sebelumnya
			AccessToken accessToken = new AccessToken(token, secret);
			twitter.setOAuthAccessToken(accessToken);
			listener.onAuthComplete();
			
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			listener.onAuthFail();
		}
	}

	/**
	 * Mulai activity untuk menampilkan login user baru.
	 * 
	 * @param activity Activity
	 * @param listener Listener saat login berhasil/gagal
	 */
	public void loginNewUser(final Activity activity, final AuthListener listener) {
		Log.d(TAG, "Login new user...");
		
		this.listener = listener;
		Intent intent = new Intent(activity, TwitterLoginActivity.class);
		activity.startActivityForResult(intent, requestCode);
	}

	/**
	 * Callback activity login.
	 * 
	 * @param requestCode Request code
	 * @param resultCode Result code
	 * @param data Data
	 */
    public void authorizeCallback(int requestCode, int resultCode, Intent data) {
    	if (requestCode != this.requestCode) return;
    	
    	if (resultCode == Activity.RESULT_OK) {
    		listener.onAuthComplete();
    	} else {
    		listener.onAuthFail();
    	}
    }
    
	/**
	 * Posting pesan ke Twitter (pada thread berbeda).
	 * 
	 * @param message Pesan yang diposting
	 */
	public void postMessageInThread(String message) {
		new AsyncTask<String, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(String... messages) {
				return postMessage(messages[0]);
			}

			@Override
			protected void onPostExecute(Boolean postSent) {
				showMessage((postSent) ? R.string.post_sent : R.string.post_fail);
			}
			
		}.execute(message);
	}

	/**
	 * Kirim sebuah tweet pada timeline anda.
	 * 
	 * @param message Pesan yang dikirim
	 * @return True jika tweet berhasil, false jika gagal
	 */
	public boolean postMessage(String message) {
		try {
			twitter.updateStatus(message);
			Log.d(TAG, "Post sent: " + message);
			return true;
			
		} catch (TwitterException e) {
			Log.e(TAG, "Post error: " + e.getMessage(), e);
			return false;
		}
	}

    /**
     * Share data gempa ke Twitter. Login dulu jika diperlukan.
     * 
     * @param activity Activity
     * @param quake Data gempa
     * @param location Lokasi saat ini
     */
    public void postQuake(final Activity activity, EarthquakeDTO quake, 
    		final Location location) {
    	// Generate message, kemudian tampilkan dialog share
    	Log.d(TAG, "Menampilkan dialog...");
    	new AsyncTask<EarthquakeDTO, Void, String>() {
			@Override
			protected String doInBackground(EarthquakeDTO... quakes) {
				return getPostMessage(quakes[0], location);
			}
			
			@Override
			protected void onPostExecute(String message) {
				// Setup dialog
				LayoutInflater inflater = (LayoutInflater) activity.getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
				View layout = inflater.inflate(R.layout.share_message, null);
				final EditText messageText = (EditText) layout.findViewById(R.id.message);
				messageText.setText(message);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setView(layout);
				builder.setTitle(R.string.share_to_twitter);
				
				builder.setPositiveButton(R.string.share, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Dapatkan kembali isi pesan, kemungkinan pesan ini dapat
						// diubah pada saat menampilkan dialog
						final String message = messageText.getText().toString();
						if (message == null) return;
						
						login(activity, new AuthListener() {
							@Override
							public void onAuthComplete() {
								postMessageInThread(message);
							}

							@Override
							public void onAuthFail() {
								showMessage(R.string.auth_fail);
							}
						});
					}
				});
				builder.show();
			}
    	}.execute(quake);
    }
    
    /**
     * Mendapatkan pesan yang akan diposting berdasarkan template pesan.
     * 
     * @param quake Data gempa
     * @param location Lokasi saat ini
     * @return Pesan yang akan diposting
     */
    public String getPostMessage(EarthquakeDTO quake, Location location) {
    	EarthquakeTemplate templateUtils = EarthquakeTemplate.getInstance(context);
		String templateDetails = prefs.getTwitterTemplate(context);
		
		float distance = -1;
		if (location != null) distance = location.distanceTo(quake.getLocation());
		String message = templateUtils.getDetailMessage(templateDetails, quake, 
				distance);
		return message;
    }
    
    /**
     * Tampilkan pesan menggunakan {@link Toast}.
     * 
     * @param resId Pesan yang ditampilkan
     */
    private void showMessage(final int resId) {
    	handler.post(new Runnable() {
    		@Override
			public void run() {
    			Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
    		}
    	});
    }
    
    /**
     * Mendapatkan nama user Twitter ini.
     * 
     * @return Nama user
     */
    public String getScreenName() {
		try {
			String[] tokens = prefs.getTwitterToken();
			String token = tokens[0];
			String secret = tokens[1];
			AccessToken accessToken = new AccessToken(token, secret);
			twitter.setOAuthAccessToken(accessToken);
			
			return twitter.getScreenName();
			
		} catch (IllegalStateException e) {
			Log.e(TAG, "IllegalStateException", e);
			return "";
			
		} catch (TwitterException e) {
			Log.e(TAG, "TwitterException", e);
			return "";
		}
    }
    
	/**
	 * Mendapatkan objek Twitter.
	 * 
	 * @return objek Twitter
	 */
	public Twitter getTwitter() {
		return twitter;
	}

	public void setRequestCode(int requestCode) {
		this.requestCode = requestCode;
	}
	
	/**
	 * Interface untuk callback request.
	 * 
	 * @author Adi Sayoga
	 */
	public interface AuthListener {
		public void onAuthComplete();
		public void onAuthFail();
	}
}
