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

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.adisayoga.earthquake.R;
import com.adisayoga.earthquake.utils.BrowserActivityClient;

/**
 * Activity untuk menampilkan bantuan penggunaan.
 * 
 * @author Adi Sayoga
 */
public class HelpActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	    requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.webview);
		
		WebView webView = (WebView) findViewById(R.id.webview);
		webView.setWebViewClient(new BrowserActivityClient());
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
		
		webView.loadUrl("file:///android_asset/help/index.html");
	}
}
