/*

Copyright (C) 2011 Green Soybean Technologies, LLC
<edamametech@gmail.com>

This program is free software: you can redistribute it and/or
modify it under the terms of the GNU General Public License as
published by the Free Software Foundation, either version 3 of
the License, or (at your option) any later version.

 */

package com.edamametech.android.SiestaWatch;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
	private static final long timeLimitCheckDuration = 10800000; // 3 hours
	private TimePickerDialog.OnTimeSetListener timeLimitListener = new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hour, int minute) {
			timeLimitHour = hour;
			timeLimitMinute = minute;
			updateTimeLimitDisplay();
			timeLimitCheckBox.setChecked(true);
		}
	};
	private TimePickerDialog timeLimitDialog;

	public static final String NeedsVibration = "NeedsVibration";
	private boolean needsVibration = true;
	CheckBox vibrationCheckBox = null;

	// Key for Usage Dialog
	private static final String ShownUsageVersion = "ShownUsageVersion";
	private int shownUsageVersion = 0;
	private int currentUsageVersion = 1;

	private void storeParameters() {
		if (DEBUG)
			Log.v(LogTag, "storeParameters()");
		SharedPreferences.Editor editor = getSharedPreferences(PrefsName, 0)
				.edit();
		editor.putLong(SleepDurationMillis, sleepDurationMillis);
		editor.putInt(TimeLimitHour, timeLimitHour);
		editor.putInt(TimeLimitMinute, timeLimitMinute);
		editor.putBoolean(NeedsVibration, needsVibration);
		editor.putInt(ShownUsageVersion, shownUsageVersion);
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
		if (prefs.contains(ShownUsageVersion)) {
			shownUsageVersion = prefs.getInt(ShownUsageVersion, 0);
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
		if (timeLimitInMillis() < System.currentTimeMillis()
				+ timeLimitCheckDuration) {
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

		if (shownUsageVersion < currentUsageVersion) {
			showAboutDialog();
			shownUsageVersion = currentUsageVersion;
			storeParameters();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_help_about:
			showAboutDialog();
			return true;
		case R.id.menu_help_help:
			showManual();
			return true;
		case R.id.menu_help_license:
			showLicense();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void showAboutDialog() {
		String versionName;
		try {
			versionName = getString(R.string.version)
					+ " "
					+ getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			versionName = getString(R.string.unknown_version);
		}
		StringBuilder message = new StringBuilder();
		message.append(versionName);
		message.append("\n");
		message.append(getString(R.string.copyright));
		message.append("\n");
		message.append(getString(R.string.notice));

		new AlertDialog.Builder(SiestaWatchActivity.this)
				.setTitle(getString(R.string.menu_help_about))
				.setMessage(message.toString())
				.setPositiveButton(getString(R.string.dialog_ok),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// does nothing
							}
						})
				.setNeutralButton(getString(R.string.menu_help_usage),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								showManual();
							}
						}).create().show();
	}

	public void showLicense() {
		Intent intent = new Intent(Intent.ACTION_VIEW,
				Uri.parse(getString(R.string.manual_uri) + "#license"));
		intent.setClass(this, SiestaWatchWebViewActivity.class);
		startActivity(intent);
	}

	public void showManual() {
		Intent intent = new Intent(Intent.ACTION_VIEW,
				Uri.parse(getString(R.string.manual_uri)));
		intent.setClass(this, SiestaWatchWebViewActivity.class);
		startActivity(intent);
	}
}
