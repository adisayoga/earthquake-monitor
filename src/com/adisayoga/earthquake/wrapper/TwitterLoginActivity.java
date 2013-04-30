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
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import com.adisayoga.earthquake.R;
import com.adisayoga.earthquake.utils.BrowserActivityClient;

public class TwitterLoginActivity extends Activity {

	private static final String TAG = "TwitterLoginActivity";
	
	private static Prefs prefs;
	private Twitter twitter;
	private WebView webView;

	/** Request token sebagai tanda id unik dari request yang dikirim ke Twitter */
	private RequestToken reqToken;

	/** Url yang akan di-redirect oleh Twitter setelah user log in - ini akan
	 * diambil oleh manifest aplikasi dan di-redirect ke activity ini */
	public static final String CALLBACK_URL = "http://adisayoga.xtreemhost.com";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	    requestWindowFeature(Window.FEATURE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
		
		prefs = Prefs.getInstance(this);
		twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(EarthquakeTwitter.CONSUMER_KEY, 
				EarthquakeTwitter.CONSUMER_SECRET);
		
		BrowserActivityClient client = new BrowserActivityClient(CALLBACK_URL);
		client.setCallbackListener(new BrowserActivityClient.CallbackListener() {
			@Override
			public void onRedirect(Uri uri) {
				dealWithResponse(uri);
			}
		});
		
		webView = (WebView) findViewById(R.id.webview);
		webView.setWebViewClient(client);
		webView.setWebChromeClient(new WebChromeClient() {
	        @Override
			public void onProgressChanged(WebView view, int progress) {
	            setProgress(progress * 100);
	           if(progress == 100) {
	              setProgressBarIndeterminateVisibility(false);
	              setProgressBarVisibility(false);
	           }
	        }
	     });
		loginNewUser();
	}
	
	/**
	 * Buat sebuah request yang akan dikirim ke Twitter menanyakan 
	 * <i>'apakah aplikasi ini punya ijin untuk menggunakan Twitter untuk user ini?</i>
	 * <p>
	 * Akan dikembalikan ke {@link #reqToken} yaitu sebuah pengenal unik
	 * pada request ini.
	 * Browse kemudian muncul pada website Twitter dan user login (kita tidak
	 * pernah melihat informasi ini).
	 * Twitter kemudian redirect ke callbackUrl jika login sukses.
	 */
	private void loginNewUser() {
		try {
			Log.d(TAG, "Request app authentication...");
			reqToken = twitter.getOAuthRequestToken(CALLBACK_URL);
			String url = reqToken.getAuthenticationURL();
			Log.d(TAG, "Authentication url: " + url);
			webView.loadUrl(url);
			
		} catch (TwitterException e) {
			Log.e(TAG, "Twitter login error: " + e.getMessage(), e);
			Toast.makeText(this, R.string.auth_fail, Toast.LENGTH_SHORT).show();
			finish();
		}
	}
	
	/**
	 * Twitter telah mengirim kembali ke aplikasi.
	 * <p>
	 * Dengan ini kita mendapatkan 'key' untuk digunakan untuk autentikasi user.
	 * 
	 * @param uri Uri
	 * @param callbackUrl Callback Url
	 * @param callbackListener Listener callback
	 */
	private void dealWithResponse(Uri uri) {
		if (uri != null && uri.toString().startsWith(CALLBACK_URL)) {
			String oauthVerifier = uri.getQueryParameter("oauth_verifier");
			
			try {
				AccessToken accessToken = twitter.getOAuthAccessToken(reqToken, 
						oauthVerifier);
				twitter.setOAuthAccessToken(accessToken);
				saveAccessToken(accessToken);
				Log.d(TAG, "login ke Twitter berhasil");
				Toast.makeText(this, R.string.auth_success, Toast.LENGTH_SHORT).show();
				setResult(Activity.RESULT_OK);
				
			} catch (Exception e) {
				Log.e(TAG, "Twitter login error: " + e.getMessage(), e);
				Toast.makeText(this, R.string.auth_fail, Toast.LENGTH_SHORT).show();
			}
			finish();
		}
	}

	/**
	 * Simpan akses token ke preference.
	 * 
	 * @param accessToken Akses token
	 */
	private void saveAccessToken(AccessToken accessToken) {
		String token = accessToken.getToken();
		String secret = accessToken.getTokenSecret();
		
		prefs.setTwitterToken(token, secret);
		Log.d(TAG, "AccessToken disimpan: token=" + token + ", secret=" + secret);
	}
	
}
