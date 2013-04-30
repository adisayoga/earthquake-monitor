package com.adisayoga.earthquake.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import android.util.Log;

import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

/**
 * Kerangka class dasar untuk RequestListener, menyediakan penanganan error 
 * default.
 */
public abstract class BaseRequestListener implements RequestListener {
	
	private static String TAG = "BaseRequestListener";
	
	@Override
	public void onIOException(IOException e, Object state) {
		Log.e(TAG, "onIOException: " + e.getMessage(), e);
	}

	@Override
	public void onFileNotFoundException(FileNotFoundException e, Object state) {
		Log.e(TAG, "onFileNotFoundException: " + e.getMessage(), e);
	}

	@Override
	public void onMalformedURLException(MalformedURLException e, Object state) {
		Log.e(TAG, "onMalformedURLException: " + e.getMessage(), e);
	}

	@Override
	public void onFacebookError(FacebookError e, Object state) {
		Log.e(TAG, "onFacebookError: " + e.getMessage(), e);
	}
}
