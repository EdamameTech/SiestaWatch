package com.edamametech.android.SiestaWatch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

public class SiestaWatchWebViewActivity extends Activity {
	private static final int LOGLEVEL = 0;
	private static final boolean DEBUG = (LOGLEVEL > 0);
	private static final String LogTag = "SiestaWatchWebViewActivity";
	
	WebView webView = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (DEBUG)
			Log.v(LogTag, "onCreate()");

		setContentView(R.layout.webview);
		webView = (WebView) findViewById(R.id.webView);
		
		webView.loadUrl(getIntent().getData().toString());
	}
}
