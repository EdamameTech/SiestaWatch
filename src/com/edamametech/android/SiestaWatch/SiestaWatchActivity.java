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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
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

    private static final int LOGLEVEL = 1;
    private static final boolean DEBUG = (LOGLEVEL > 0);
    private static final String LogTag = "SiestaWatchActivity";
    public static final String PrefsName = "SiestaWatchActivity";

    private Context mContext;

    /* parameters */
    // Key for Extras in Intent to supply uriOfAlarmSound as a String
    public static final String UriOfAlarmSound = "UriOfAlarmSound";
    // URI of the alarm sound
    private Uri mUriOfAlarmSound = null;
    public static Uri defaultUriOfAlarmSound = Settings.System.DEFAULT_ALARM_ALERT_URI;

    // Key for Extras in Intent to supply sleepDurationMIllis as a long
    public static final String SleepDurationMillis = "SleepDurationMillis";
    // Time duration in msec to alarm after user felt asleep
    private long mSleepDurationMillis = 0;
    public static final long defaultSleepDurationMillis = 1800000; // 30 min
    private final long sleepDurationStepMillis = 300000; // 5 min
    private EditText mDurationField = null;

    // Key for Extras in Intent to supply TimeLimit as Strings
    public static final String TimeLimitHour = "TimeLimitHour";
    public static final String TimeLimitMinute = "TimeLimitMinute";
    private int mTimeLimitHour = 0; /* 24-hour expression */
    private int mTimeLimitMinute = 0;
    private Button timeLimitButton = null;
    private boolean hasTimeLimit;
    private CheckBox mTimeLimitCheckBox = null;
    private static final long timeLimitDefaultDelayMillis = 1800000; // 30 min
    private static final long timeLimitGranuarityMillis = 300000; // 5 min
    private static final long timeLimitCheckDuration = 10800000; // 3 hours
    private TimePickerDialog.OnTimeSetListener mTimeLimitListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hour, int minute) {
            mTimeLimitHour = hour;
            mTimeLimitMinute = minute;
            updateTimeLimitDisplay();
            mTimeLimitCheckBox.setChecked(true);
        }
    };
    private TimePickerDialog mTimeLimitDialog;

    CheckBox mVibrationCheckBox;

    // Key for Usage Dialog
    private static final String ShownUsageVersion = "ShownUsageVersion";
    private int mShownUsageVersion = 0;
    private int currentUsageVersion = 1;

    @Override
    public void onPause() {
        super.onPause();

        if (DEBUG)
            Log.v(LogTag, "onPause()");

        storeParameters();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (DEBUG)
            Log.v(LogTag, "onResume()");

        restoreParameters();
    }

    private void storeParameters() {
        if (DEBUG)
            Log.v(LogTag, "storeParameters()");

        SiestaWatchConf.setSleepDuration(mContext, mSleepDurationMillis);
        SiestaWatchConf.setNeedsTimeLimit(mContext, mTimeLimitCheckBox.isChecked());
        SiestaWatchConf.setTimeLimitHour(mContext, mTimeLimitHour);
        SiestaWatchConf.setTimeLimitMinute(mContext, mTimeLimitMinute);
        SiestaWatchConf.setUriOfAlarmSound(mContext, mUriOfAlarmSound);
        SiestaWatchConf.setShownUsageVersion(mContext, mShownUsageVersion);
    }

    private void restoreParameters() {
        if (DEBUG)
            Log.v(LogTag, "restoreParameters()");

        mSleepDurationMillis = SiestaWatchConf.sleepDuration(mContext);
        if (mVibrationCheckBox != null)
            mVibrationCheckBox.setChecked(SiestaWatchConf.needsVibration(mContext));
        if (mTimeLimitCheckBox != null)
            mTimeLimitCheckBox.setChecked(SiestaWatchConf.needsTimeLimit(mContext));
        mTimeLimitHour = SiestaWatchConf.timeLimitHour(mContext);
        mTimeLimitMinute = SiestaWatchConf.timeLimitMinute(mContext);
        mUriOfAlarmSound = SiestaWatchConf.uriOfAlarmSound(mContext);
        mShownUsageVersion = SiestaWatchConf.shownUsageVersion(mContext);
    }

    private String getDurationInMins() {
        return String
                .format("%.0f", ((float) mSleepDurationMillis) / 1e3 / 60.0);
    }

    private void obtainDurationFromDisplay() {
        mSleepDurationMillis = (long) (Float.valueOf(mDurationField.getText()
                .toString()) * 60 * 1e3);
    }

    private long timeLimitInMillis() {
        return SiestaWatchUtil.timeHhmmToLong(mTimeLimitHour, mTimeLimitMinute,
                TimeZone.getDefault());
    }

    private void updateSleepDurationDisplay() {
        mDurationField.setText(getDurationInMins());
    }

    /* communications to the Service */
    private void startSiestaWatchService() {
        if (DEBUG)
            Log.v(LogTag, "startSiestaWatchService()");
        Intent intent = new Intent();
        intent.setClass(this, SiestaWatchService.class);
        intent.putExtra(SiestaWatchService.SleepDurationMillis,
                mSleepDurationMillis);
        if (hasTimeLimit == true) {
            intent.putExtra(SiestaWatchService.TimeLimitMillis,
                    timeLimitInMillis());
        } else {
            intent.putExtra(SiestaWatchService.TimeLimitMillis, 0);
        }
        intent.putExtra(SiestaWatchService.UriOfAlarmSound,
                mUriOfAlarmSound.toString());
        startService(intent);
    }

    private void stopSiestaWatchService() {
        if (DEBUG)
            Log.v(LogTag, "stopSiestaWatchService()");
        Intent intent = new Intent();
        intent.setClass(this, SiestaWatchService.class);
        intent.putExtra(SiestaWatchService.SERVICE_ACTION,
                SiestaWatchService.ACTION_CANCEL);
        startService(intent);
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

        mContext = getBaseContext();

        restoreParameters();
        mDurationField = (EditText) findViewById(R.id.sleepDurationInMins);
        updateSleepDurationDisplay();
        ((Button) findViewById(R.id.sleepDurationPlusButton))
                .setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        obtainDurationFromDisplay();
                        mSleepDurationMillis += sleepDurationStepMillis;
                        updateSleepDurationDisplay();
                    }
                });
        ((Button) findViewById(R.id.sleepDurationMinusButton))
                .setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        obtainDurationFromDisplay();
                        mSleepDurationMillis -= sleepDurationStepMillis;
                        if (mSleepDurationMillis < 0)
                            mSleepDurationMillis = 0;
                        updateSleepDurationDisplay();
                    }
                });

        mTimeLimitDialog = new TimePickerDialog(this, mTimeLimitListener,
                mTimeLimitHour, mTimeLimitMinute, true);
        timeLimitButton = (Button) findViewById(R.id.timeLimitButton);
        updateTimeLimitDisplay();
        timeLimitButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                mTimeLimitDialog.show();
            }
        });
        mTimeLimitCheckBox = (CheckBox) findViewById(R.id.timeLimitCheckBox);
        if (timeLimitInMillis() < System.currentTimeMillis()
                + timeLimitCheckDuration) {
            mTimeLimitCheckBox.setChecked(true);
        } else {
            mTimeLimitCheckBox.setChecked(false);
        }

        mVibrationCheckBox = (CheckBox) findViewById(R.id.vibrateCheckBox);
        mVibrationCheckBox.setChecked(SiestaWatchConf.needsVibration(mContext));
        mVibrationCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (DEBUG)
                    Log.v(LogTag, "mVibrationCheckBox/onCheckedChanged(" + isChecked + ")");

                SiestaWatchConf.setNeedsVibration(mContext, isChecked);
            }
        });

        ((Button) findViewById(R.id.done))
                .setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        obtainDurationFromDisplay();
                        long timeLimitMillis = timeLimitInMillis();
                        mTimeLimitHour = Integer.valueOf(SiestaWatchUtil.timeLongToHhmm(
                                timeLimitMillis, new SimpleDateFormat("HH")));
                        mTimeLimitMinute = Integer.valueOf(SiestaWatchUtil.timeLongToHhmm(
                                timeLimitMillis, new SimpleDateFormat("mm")));
                        hasTimeLimit = mTimeLimitCheckBox.isChecked();
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

        restoreParameters();

        if (mShownUsageVersion < currentUsageVersion) {
            showAboutDialog();
            mShownUsageVersion = currentUsageVersion;
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
