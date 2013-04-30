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

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.adisayoga.earthquake.R;
import com.adisayoga.earthquake.utils.BaseRequestListener;
import com.adisayoga.earthquake.utils.SessionEvents;
import com.adisayoga.earthquake.wrapper.EarthquakeFacebook;
import com.adisayoga.earthquake.wrapper.EarthquakeTwitter;
import com.adisayoga.earthquake.wrapper.Prefs;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;

/**
 * Activity login/logout dari jejaring sosial (Facebook dan Twitter).
 * 
 * @author Adi Sayoga
 */
public class SocialConnectActivity extends Activity {
	
	private static final String TAG = "SocialConnectActivity";
	private static Prefs prefs;
	
	private EarthquakeFacebook facebook;
	private AsyncFacebookRunner runner;
	private EarthquakeTwitter twitter;
	
	private ToggleButton facebookLoginButton;
	private TextView facebookLoginStatus;
	private ToggleButton twitterLoginButton;
	private TextView twitterLoginStatus;
	
	private boolean twitterLoggedIn = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		prefs = Prefs.getInstance(this);
		setTheme(prefs.getTheme().resId);
		setContentView(R.layout.social_connect);
		
		facebook = new EarthquakeFacebook(this);
		runner = new AsyncFacebookRunner(facebook.getFacebook());
		twitter = new EarthquakeTwitter(this);
		
		setListeners();
		updateFacebookView();
		
		// Coba login authorized user
		twitter.loginAuthorizedUser(new EarthquakeTwitter.AuthListener() {
			@Override
			public void onAuthComplete() {
				twitterLoggedIn = true;
				updateTwitterView();
			}

			@Override
			public void onAuthFail() {
				// Abaikan
			}
		});
	}
	
	/**
	 * Set listener untuk masing-masing tombol.
	 */
	private void setListeners() {
		facebookLoginButton = (ToggleButton) findViewById(R.id.facebook_connect);
		facebookLoginStatus = (TextView) findViewById(R.id.facebook_status);
		
		facebookLoginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Kembalikan keadaan seperti semula, saat login berhasil baru
				// view akan diupdate
				facebookLoginButton.setChecked(!facebookLoginButton.isChecked());
				
				if (facebook.isSessionValid()) {
					facebookLogout();
				} else {
					facebookLogin();
				}
			}
		});
		
		twitterLoginButton = (ToggleButton) findViewById(R.id.twitter_connect);
		twitterLoginStatus = (TextView) findViewById(R.id.twitter_status);
		
		twitterLoginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Kembalikan keadaan seperti semula, saat login berhasil baru
				// view akan diupdate
				twitterLoginButton.setChecked(!twitterLoginButton.isChecked());
				
				if (twitterLoggedIn) {
					twitterLogout();
				} else {
					twitterLogin();
				}
			}
		});
	}
	
	/**
	 * Update view Facebook, sudah login atau belum.
	 */
	private void updateFacebookView() {
		if (facebook.isSessionValid()) {
			facebookLoginButton.setChecked(true);
			showFacebookInfo();
		} else {
			facebookLoginButton.setChecked(false);
			facebookLoginStatus.setText(R.string.not_login);
		}
	}

	/**
	 * Tampilkan informasi Facebook sebagai tanda user telah login.
	 */
	private void showFacebookInfo() {
		facebookLoginStatus.setText(R.string.loading);
		
		// Tampilkan nama user yang login
		runner.request("me", new BaseRequestListener() {
			@Override
			public void onComplete(String response, Object state) {
				try {
					Log.d(TAG, "Response: " + response.toString());
	                JSONObject json = Util.parseJson(response);
	                final String name = json.getString("name");
	                
	                runOnUiThread(new Runnable() {
	                	@Override
						public void run() {
	                		facebookLoginStatus.setText(getText(R.string.welcome) 
	                				+ " " + name);
	                	}
	                });
	                
				} catch (JSONException e) {
	                Log.w(TAG, "JSON Error pada response");
	                
	            } catch (FacebookError e) {
	                Log.w(TAG, "Facebook Error: " + e.getMessage());
	                runOnUiThread(new Runnable() {
	                	@Override
						public void run() {
	                		facebookLoginStatus.setText(R.string.facebook_error);
	                	}
	                });
	            }
			}
			
			@Override
			public void onIOException(IOException e, Object state) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						facebookLoginStatus.setText(R.string.network_error);
					}
				});
				super.onIOException(e, state);
			}
		});
	}
	
	/**
	 * Login ke Facebook.
	 */
	private void facebookLogin() {
		Log.d(TAG, "Login...");
		SessionEvents.AuthListener listener = new SessionEvents.AuthListener() {
			@Override
			public void onAuthSucceed() {
				Log.d(TAG, "Auth success!");
				updateFacebookView();
			}
			
			@Override
			public void onAuthFail(String error) {
				Toast.makeText(SocialConnectActivity.this, R.string.auth_fail, 
						Toast.LENGTH_LONG).show();
				facebookLoginStatus.setText(R.string.auth_fail);
				Log.w(TAG, error);
			}
		};
		SessionEvents.addAuthListener(listener);
		facebook.login(this);
	}
	
	/**
	 * Logout dari Facebook.
	 */
	private void facebookLogout() {
		Log.d(TAG, "Logout...");
		SessionEvents.LogoutListener listener = new SessionEvents.LogoutListener() {
			@Override
			public void onLogoutBegin() {
				Log.d(TAG, "Logout begin...");
				facebookLoginStatus.setText(R.string.logging_out);
			}

			@Override
			public void onLogoutFinish() {
				Log.d(TAG, "Logout finish.");
				updateFacebookView();
			}
			
		};
		SessionEvents.addLogoutListener(listener);
		facebook.logout();
	}

	/**
	 * Update view Twitter sudah login/belum.
	 */
	private void updateTwitterView() {
		if (twitterLoggedIn) {
			twitterLoginButton.setChecked(true);
			showTwitterInfo();
		} else {
			twitterLoginButton.setChecked(false);
			twitterLoginStatus.setText(R.string.not_login);
		}
	}
	
	/**
	 * Tampilkan informasi Twitter sebagai tanda sudah login/belum.
	 */
	private void showTwitterInfo() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				publishProgress();
				return twitter.getScreenName();
			}
			
			@Override
			protected void onProgressUpdate(Void... values) {
				twitterLoginStatus.setText(R.string.loading);
			}

			@Override
			protected void onPostExecute(String result) {
				twitterLoginStatus.setText(getText(R.string.welcome) 
        				+ " " + result);
			}
			
		}.execute();
	}
	
	/**
	 * Login ke Twitter.
	 */
	private void twitterLogin() {
		twitter.login(this, new EarthquakeTwitter.AuthListener() {
			@Override
			public void onAuthComplete() {
				twitterLoggedIn = true;
				updateTwitterView();
			}
			
			@Override
			public void onAuthFail() {
				twitterLoggedIn = false;
				updateTwitterView();
			}
		});
	}
	
	/**
	 * Logout dari Twitter.
	 */
	private void twitterLogout() {
		// Hapus token dari preference
		prefs.setTwitterToken("", "");
		twitterLoggedIn = false;
		updateTwitterView();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		facebook.getFacebook().authorizeCallback(requestCode, resultCode, data);
		twitter.authorizeCallback(requestCode, resultCode, data);
	}
}
