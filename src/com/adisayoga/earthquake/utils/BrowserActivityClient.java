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
 
package com.adisayoga.earthquake.utils;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class BrowserActivityClient extends WebViewClient {
	
	private static final String TAG = "BrowserActivityClient";
	
	private String callbackUrl = "";
	private CallbackListener listener = null;
	private boolean hasCallaback = false;
	
	public BrowserActivityClient(String callbackUrl) {
		this.callbackUrl = callbackUrl;
		Log.d(TAG, "callbackUrl=" + callbackUrl);
	}
	
	public BrowserActivityClient() {
	}
	
	@Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
		view.loadUrl(url);
		return true;
    }
	
	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		Log.d(TAG, "url=" + url);
		if (!callbackUrl.equals("") && url.contains(callbackUrl)) {
			if (listener != null && !hasCallaback) {
				// Redirect hanya sekali saja
				hasCallaback = true;
				view.stopLoading();
				listener.onRedirect(Uri.parse(url));
			}
		}
	}

	public void setCallbackListener(CallbackListener listener) {
		this.listener = listener;
	}
	
	public interface CallbackListener {
		public void onRedirect(Uri uri);
	}
}
