package com.edamametech.android.SiestaWatch;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class SiestaWatchActivity extends Activity {
	private static final int LOGLEVEL = 0;
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

	// Key for Extras in Intent to supply TimeLimit as Strings
	public static final String TimeLimitHour = "TimeLimitHour";
	public static final String TimeLimitMinute = "TimeLimitMinute";
	private int timeLimitHour = 0;
	private int timeLimitMinute = 0;
	EditText timeLimitField = null;
	private boolean hasTimeLimit;
	CheckBox timeLimitCheckBox = null;
	private static final long timeLimitDefaultDelayMillis = 1800000; // 30 min
	private static final long timeLimitGranuarityMillis = 300000; // 5 min

	public static final String NeedsVibration = "NeedsVibration";
	private boolean needsVibration = false;
	CheckBox vibrationCheckBox = null;

	private void storeParameters() {
		if (DEBUG)
			Log.v(LogTag, "storeParameters()");
		SharedPreferences.Editor editor = getSharedPreferences(PrefsName, 0)
				.edit();
		editor.putLong(SleepDurationMillis, sleepDurationMillis);
		editor.putInt(TimeLimitHour, timeLimitHour);
		editor.putInt(TimeLimitMinute, timeLimitMinute);
		editor.putBoolean(NeedsVibration, needsVibration);
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
		if (prefs.contains(TimeLimitHour) && prefs.contains(TimeLimitMinute)) {
			timeLimitHour = prefs.getInt(TimeLimitHour, 0);
			timeLimitMinute = prefs.getInt(TimeLimitMinute, 0);
		} else {
			long defaultTimeLimit = System.currentTimeMillis()
					+ timeLimitDefaultDelayMillis;
			defaultTimeLimit = (long) Math.ceil((double) defaultTimeLimit
					/ (double) timeLimitGranuarityMillis)
					* timeLimitGranuarityMillis;
			timeLimitHour = Integer.valueOf(SiestaWatchUtil.timeLongToHhmm(
					defaultTimeLimit, new SimpleDateFormat("HH")));
			timeLimitMinute = Integer.valueOf(SiestaWatchUtil.timeLongToHhmm(
					defaultTimeLimit, new SimpleDateFormat("mm")));
		}
		if (prefs.contains(NeedsVibration)) {
			needsVibration = prefs.getBoolean(NeedsVibration, false);
		}
	}

	private String getDurationInMins() {
		return String
				.format("%.1f", ((float) sleepDurationMillis) / 1e3 / 60.0);
	}

	private void setDurationInMins(String string) {
		sleepDurationMillis = (long) (Float.valueOf(string) * 60 * 1e3);
	}

	private long timeLimitForService() {
		return SiestaWatchUtil.timeHhmmToLong(timeLimitHour, timeLimitMinute,
				TimeZone.getDefault());
	}

	/* communications to the Service */
	private void startSiestaWatchService() {
		if (DEBUG)
			Log.v(LogTag, "startSiestaWatchService()");
		Intent intent = new Intent();
		intent.setClass(this, SiestaWatchService.class);
		intent.putExtra(SiestaWatchService.SleepDurationMillis,
				sleepDurationMillis);
		if (hasTimeLimit == true) {
			intent.putExtra(SiestaWatchService.TimeLimitMillis,
					timeLimitForService());
		} else {
			intent.putExtra(SiestaWatchService.TimeLimitMillis, 0);
		}
		intent.putExtra(SiestaWatchService.UriOfAlarmSound,
				uriOfAlarmSound.toString());
		intent.putExtra(SiestaWatchService.NeedsVibration, needsVibration);
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
		timeLimitField = (EditText) findViewById(R.id.timeLimit);
		timeLimitField.setText(String.format("%1$02d:%2$02d", timeLimitHour,
				timeLimitMinute));
		timeLimitCheckBox = (CheckBox) findViewById(R.id.timeLimitCheckBox);
		timeLimitCheckBox.setChecked(true);
		vibrationCheckBox = (CheckBox) findViewById(R.id.vibrateCheckBox);
		vibrationCheckBox.setChecked(needsVibration);

		((Button) findViewById(R.id.done))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View view) {
						setDurationInMins(durationField.getText().toString());
						String[] timeLimitFields = timeLimitField.getText()
								.toString().split(":");
						timeLimitHour = Integer.valueOf(timeLimitFields[0]);
						timeLimitMinute = Integer.valueOf(timeLimitFields[1]);
						needsVibration = vibrationCheckBox.isChecked();
						storeParameters();
						hasTimeLimit = timeLimitCheckBox.isChecked();
						/* hasTimeLimit will not be recorded in preferences */
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