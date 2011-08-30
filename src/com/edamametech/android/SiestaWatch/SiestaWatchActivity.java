package com.edamametech.android.SiestaWatch;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class SiestaWatchActivity extends Activity {
	private static final int LOGLEVEL = 1;
	private static final boolean DEBUG = (LOGLEVEL > 0);
	private static final String LogTag = "SiestaWatchActivity";
	private static final String PrefsName = "SiestaWatchActivity";

	/* parameters */
	// Key for Extras in Intent to supply uriOfAlarmSound as a String
	public static final String UriOfAlarmSound = "UriOfAlarmSound";
	// URI of the alarm sound
	private Uri uriOfAlarmSound = null;
	public static Uri defaultUriOfAlarmSound = Settings.System.DEFAULT_ALARM_ALERT_URI;

	// Key for Extras in Intent to supply sleepDurationMIllis as a long
	public static final String SleepDurationMillis = "SleepDurationMillis";
	// Time duration in msec to alarm after user felt asleep
	private long sleepDurationMillis = 0;
	public static final long defaultSleepDurationMillis = 1800000; // 30 min
	EditText durationField = null;

	private void storeParameters() {
		if (DEBUG)
			Log.v(LogTag, "storeParameters()");
		SharedPreferences.Editor editor = getSharedPreferences(PrefsName, 0)
				.edit();
		editor.putLong(SleepDurationMillis, sleepDurationMillis);
		editor.commit();
	}

	private void restoreParameters() {
		if (DEBUG)
			Log.v(LogTag, "restoreParameters()");
		SharedPreferences prefs = getSharedPreferences(PrefsName, 0);
		uriOfAlarmSound = defaultUriOfAlarmSound;
		if (prefs.contains(SleepDurationMillis)) {
			sleepDurationMillis = prefs.getLong(SleepDurationMillis, 0);
		} else {
			sleepDurationMillis = defaultSleepDurationMillis;
		}
	}

	private String getDurationInMins() {
		return String
				.format("%.1f", ((float) sleepDurationMillis) / 1e3 / 60.0);
	}

	private void setDurationInMins(String string) {
		sleepDurationMillis = (long) (Float.valueOf(string) * 60 * 1e3);
	}

	/* communications to the Service */
	private void startSiestaWatchService() {
		if (DEBUG)
			Log.v(LogTag, "startSiestaWatchService()");
		Intent intent = new Intent();
		intent.setClass(this, SiestaWatchService.class);
		intent.putExtra(SiestaWatchService.SleepDurationMillis,
				sleepDurationMillis);
		intent.putExtra(SiestaWatchService.UriOfAlarmSound,
				uriOfAlarmSound.toString());
		startService(intent);
	}

	private void stopSiestaWatchService() {
		if (DEBUG)
			Log.v(LogTag, "stopSiestaWatchService()");
		Intent intent = new Intent();
		intent.setClass(this, SiestaWatchService.class);
		stopService(intent);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (DEBUG)
			Log.v(LogTag, "onCreate()");

		setContentView(R.layout.main);

		restoreParameters();
		durationField = (EditText) findViewById(R.id.sleepDurationInMins);
		durationField.setText(getDurationInMins());

		((Button) findViewById(R.id.done))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View view) {
						setDurationInMins(durationField.getText().toString());
						storeParameters();
						startSiestaWatchService();
						finish();
					}
				});
		((Button) findViewById(R.id.cancel))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View view) {
						stopSiestaWatchService();
						finish();
					}
				});
	}
}