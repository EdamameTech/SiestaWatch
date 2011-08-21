package com.edamametech.android.SiestaWatch;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class SiestaWatchService extends Service {
	private static String LogTag = "SiestaWatchService";

	private static void logIntent(Intent intent) {
		Log.i(LogTag, intent.toString());
		Bundle extras = intent.getExtras();
		if (extras != null) {
			Log.i(LogTag, extras.toString());
			if (extras.containsKey("Greeting")) {
				Log.i(LogTag, "Greeting: " + extras.getString("Greeting"));
			}
		} else {
			Log.i(LogTag, "No extras in Intent");
		}
	}

	private static final BroadcastReceiver screenEventReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(LogTag, "screenEventReceiver.onReceive()");
			Log.i(LogTag, context.toString());
			logIntent(intent);
		}
	};

	private static IntentFilter screenEventFilter = null;

	@Override
	public IBinder onBind(Intent arg0) {
		// Will not be bound
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		handleStartCommand(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleStartCommand(intent);
		return START_STICKY;
	}

	private void handleStartCommand(Intent intent) {
		Log.i(LogTag, "SiestaWatchService.handleStartCommand()");
		logIntent(intent);
		registerScreenEvents();
	}

	@Override
	public void onDestroy() {
		Log.i(LogTag, "SiestaWatchService.onDestroy()");
		unregisterScreenEvents();
	}

	private void registerScreenEvents() {
		if (screenEventFilter == null) {
			Log.i(LogTag, "Registering screenEvents");
			screenEventFilter = new IntentFilter();
			screenEventFilter.addAction(Intent.ACTION_SCREEN_OFF);
			screenEventFilter.addAction(Intent.ACTION_SCREEN_ON);
			screenEventFilter.addAction(Intent.ACTION_USER_PRESENT);
			registerReceiver(screenEventReceiver, screenEventFilter);
		}
	}

	private void unregisterScreenEvents() {
		if (screenEventFilter != null) {
			Log.i(LogTag, "Unregistering screenEvents");
			unregisterReceiver(screenEventReceiver);
			screenEventFilter = null;
		}
	}
}
