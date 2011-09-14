package com.edamametech.android.SiestaWatch;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;
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

	// Key for Extras in Intent to supply timeLimitMIllis as a long
	public static final String TimeLimitMillis = "TimeLimitMillis";
	// Absolute time in a day in msec to alarm
	private long timeLimitMillis = 0;
	TimePicker timeLimitField = null;
	private boolean isTimeLimit;
	CheckBox timeLimitCheckBox = null;

	private void storeParameters() {
		if (DEBUG)
			Log.v(LogTag, "storeParameters()");
		SharedPreferences.Editor editor = getSharedPreferences(PrefsName, 0)
				.edit();
		editor.putLong(SleepDurationMillis, sleepDurationMillis);
		editor.putLong(TimeLimitMillis, timeLimitMillis);
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
		if (prefs.contains(TimeLimitMillis)) {
			timeLimitMillis = prefs.getLong(TimeLimitMillis, 0);
		} else {
			timeLimitMillis = (System.currentTimeMillis() + defaultSleepDurationMillis)
					% (24 * 3600 * 1000); // TODO: debug
		}
	}

	private String getDurationInMins() {
		return String
				.format("%.1f", ((float) sleepDurationMillis) / 1e3 / 60.0);
	}

	private void setDurationInMins(String string) {
		sleepDurationMillis = (long) (Float.valueOf(string) * 60 * 1e3);
	}

	private int getTimeLimitHour() {
		Log.v(LogTag, "timeLimitMillis: " + timeLimitMillis);
		return (int) (timeLimitMillis / (3600 * 1000));
	}

	private int getTimeLimitMinute() {
		return (int) ((timeLimitMillis / (60 * 1000)) % 60);
	}

	private void setTimeLimit(int hour, int minute) {
		timeLimitMillis = (long) (hour * 3600 + minute * 60) * 1000L;
	}

	private long timeLimitForService() {
		long currentTime, timeLimit;
		currentTime = System.currentTimeMillis();
		timeLimit = (currentTime / (24 * 3600 * 1000) * (24 * 3600 * 1000))
				+ timeLimitMillis;
		if (timeLimit < currentTime)
			timeLimit += 24 * 3600 * 1000;
		return timeLimit;
	}

	/* communications to the Service */
	private void startSiestaWatchService() {
		if (DEBUG)
			Log.v(LogTag, "startSiestaWatchService()");
		Intent intent = new Intent();
		intent.setClass(this, SiestaWatchService.class);
		intent.putExtra(SiestaWatchService.SleepDurationMillis,
				sleepDurationMillis);
		if (isTimeLimit == true) {
			intent.putExtra(SiestaWatchService.TimeLimitMillis,
					timeLimitForService());
		} else {
			intent.putExtra(SiestaWatchService.TimeLimitMillis, 0);
		}
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
		timeLimitField = (TimePicker) findViewById(R.id.timeLimitPicker);
		timeLimitField.setIs24HourView(true);
		timeLimitField.setCurrentHour(getTimeLimitHour());
		timeLimitField.setCurrentMinute(getTimeLimitMinute());
		timeLimitCheckBox = (CheckBox) findViewById(R.id.timeLimitCheckBox);
		timeLimitCheckBox.setChecked(true);

		((Button) findViewById(R.id.done))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View view) {
						setDurationInMins(durationField.getText().toString());
						setTimeLimit(timeLimitField.getCurrentHour(),
								timeLimitField.getCurrentMinute());
						storeParameters();
						isTimeLimit = timeLimitCheckBox.isChecked();
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