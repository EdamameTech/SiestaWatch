package com.edamametech.android.SiestaWatch;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class SiestaWatchService extends Service {
	private static int LOGLEVEL = 1;
	private static boolean DEBUG = (LOGLEVEL > 0);
	private static String LogTag = "SiestaWatchService";
	private static String PrefsName = "SiestaWatchService";

	/* state */
	public enum State {
		Off, // Application has not been executed
		StandingBy, // Waiting for the user to fall asleep
		CountingDown, // Counting down to raise alarm
		Alarming, // Waking up the user
		Silencing, // Pausing the alarm
	};

	private State state = State.Off;

	// for SiestaWatchServiceTestCases
	public State getState() {
		return state;
	}

	private void standBy() {
		if (uriOfAlarmSound != null && sleepDurationMillis > 0) {
			state = State.StandingBy;
		}
	}

	/* parameters */
	// Key for Extras in Intent to supply uriOfAlarmSound as a String
	public static String UriOfAlarmSound = "UriOfAlarmSound";
	// Key for Extras in Intent to supply sleepDurationMIllis as a long
	public static String SleepDurationMillis = "SleepDurationMillis";
	// URI of the alarm sound
	private Uri uriOfAlarmSound = null;
	// Time duration in msec to alarm after user felt asleep
	private long sleepDurationMillis = 0;

	private static final BroadcastReceiver screenEventReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (DEBUG)
				Log.v(LogTag, "screenEventReceiver.onReceive()");
			if (DEBUG)
				Log.v(LogTag, context.toString());
			if (DEBUG)
				Log.v(LogTag, intent.toString());
		}
	};

	private void storeParameters() {
		if (DEBUG)
			Log.v(LogTag, "SiestaWatchService.storeParameters()");
		SharedPreferences.Editor editor = getSharedPreferences(PrefsName, 0)
				.edit();
		editor.putString(UriOfAlarmSound, uriOfAlarmSound.toString());
		editor.putLong(SleepDurationMillis, sleepDurationMillis);
		editor.commit();
	}

	private void clearStoredParameters() {
		if (DEBUG)
			Log.v(LogTag, "SiestaWatchService.storeParameters()");
		SharedPreferences.Editor editor = getSharedPreferences(PrefsName, 0)
				.edit();
		editor.clear();
		editor.commit();
	}

	private void restoreParameters() {
		if (DEBUG)
			Log.v(LogTag, "SiestaWatchService.restoreParameters()");
		SharedPreferences prefs = getSharedPreferences(PrefsName, 0);
		if (prefs.contains(UriOfAlarmSound)) {
			uriOfAlarmSound = Uri.parse(prefs.getString(UriOfAlarmSound, ""));
		}
		if (prefs.contains(SleepDurationMillis)) {
			sleepDurationMillis = prefs.getLong(SleepDurationMillis, 0);
		}
	}

	private static final IntentFilter screenEventFilter = new IntentFilter();

	@Override
	public IBinder onBind(Intent arg0) {
		// Will not be bound
		return null;
	}

	@Override
	public void onCreate() {
		if (DEBUG)
			Log.v(LogTag, "SiestaWatchService.onCreate()");
		if (DEBUG)
			Log.v(LogTag, "Registering screenEvents");
		screenEventFilter.addAction(Intent.ACTION_SCREEN_OFF);
		screenEventFilter.addAction(Intent.ACTION_SCREEN_ON);
		screenEventFilter.addAction(Intent.ACTION_USER_PRESENT);
		registerReceiver(screenEventReceiver, screenEventFilter);
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
		if (DEBUG)
			Log.v(LogTag, "SiestaWatchService.handleStartCommand()");

		if (intent == null) {
			if (DEBUG)
				Log.v(LogTag, "Got a null intent");
			restoreParameters();
			standBy();
			return;
		}

		if (DEBUG)
			Log.v(LogTag, intent.toString());

		Bundle extras = intent.getExtras();
		if (extras == null) {
			clearStoredParameters();
			return;
		}

		if (extras.containsKey(UriOfAlarmSound)) {
			if (DEBUG)
				Log.v(LogTag,
						UriOfAlarmSound + ": "
								+ extras.getString(UriOfAlarmSound));
			uriOfAlarmSound = Uri.parse(extras.getString(UriOfAlarmSound));
		}
		if (extras.containsKey(SleepDurationMillis)) {
			if (DEBUG)
				Log.v(LogTag,
						SleepDurationMillis + ": "
								+ extras.getLong(SleepDurationMillis));
			sleepDurationMillis = extras.getLong(SleepDurationMillis);
		}
		storeParameters();
		standBy();
	}

	@Override
	public void onDestroy() {
		if (DEBUG)
			Log.v(LogTag, "SiestaWatchService.onDestroy()");
		if (DEBUG)
			Log.v(LogTag, "Unregistering screenEvents");
		unregisterReceiver(screenEventReceiver);
	}
}
