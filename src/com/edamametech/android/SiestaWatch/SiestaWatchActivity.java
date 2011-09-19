package com.edamametech.android.SiestaWatch;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;
import android.text.format.DateFormat;
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
	private final long sleepDurationStepMillis = 300000; // 5 min
	private EditText durationField = null;

	// Key for Extras in Intent to supply TimeLimit as Strings
	public static final String TimeLimitHour = "TimeLimitHour";
	public static final String TimeLimitMinute = "TimeLimitMinute";
	private int timeLimitHour = 0;
	private int timeLimitMinute = 0;
	private Button timeLimitButton = null;
	private boolean hasTimeLimit;
	private CheckBox timeLimitCheckBox = null;
	private static final long timeLimitDefaultDelayMillis = 1800000; // 30 min
	private static final long timeLimitGranuarityMillis = 300000; // 5 min
	private static final long timeLimitCheckDuration = 10800000;	 // 3 hours
	private TimePickerDialog.OnTimeSetListener timeLimitListener = new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hour, int minute) {
			timeLimitHour = hour;
			timeLimitMinute = minute;
			updateTimeLimitDisplay();
		}
	};
	private TimePickerDialog timeLimitDialog;

	public static final String NeedsVibration = "NeedsVibration";
	private boolean needsVibration = true;
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
				.format("%.0f", ((float) sleepDurationMillis) / 1e3 / 60.0);
	}

	private void obtainDurationFromDisplay() {
		sleepDurationMillis = (long) (Float.valueOf(durationField.getText()
				.toString()) * 60 * 1e3);
	}

	private long timeLimitInMillis() {
		return SiestaWatchUtil.timeHhmmToLong(timeLimitHour, timeLimitMinute,
				TimeZone.getDefault());
	}

	private void updateSleepDurationDisplay() {
		durationField.setText(getDurationInMins());
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
					timeLimitInMillis());
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

	private void updateTimeLimitDisplay() {
		timeLimitButton.setText(SiestaWatchUtil.timeLongToHhmm(
				timeLimitInMillis(), DateFormat.getTimeFormat(this)));
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
		updateSleepDurationDisplay();
		((Button) findViewById(R.id.sleepDurationPlusButton))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View view) {
						obtainDurationFromDisplay();
						sleepDurationMillis += sleepDurationStepMillis;
						updateSleepDurationDisplay();
					}
				});
		((Button) findViewById(R.id.sleepDurationMinusButton))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View view) {
						obtainDurationFromDisplay();
						sleepDurationMillis -= sleepDurationStepMillis;
						if (sleepDurationMillis < 0)
							sleepDurationMillis = 0;
						updateSleepDurationDisplay();
					}
				});

		timeLimitDialog = new TimePickerDialog(this, timeLimitListener,
				timeLimitHour, timeLimitMinute, true);
		timeLimitButton = (Button) findViewById(R.id.timeLimitButton);
		updateTimeLimitDisplay();
		timeLimitButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				timeLimitDialog.show();
			}
		});
		timeLimitCheckBox = (CheckBox) findViewById(R.id.timeLimitCheckBox);
		if (timeLimitInMillis() < System.currentTimeMillis() + timeLimitCheckDuration) {
			timeLimitCheckBox.setChecked(true);
		} else {
			timeLimitCheckBox.setChecked(false);
		}

		vibrationCheckBox = (CheckBox) findViewById(R.id.vibrateCheckBox);
		vibrationCheckBox.setChecked(needsVibration);

		((Button) findViewById(R.id.done))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View view) {
						obtainDurationFromDisplay();
						String[] timeLimitFields = timeLimitButton.getText()
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