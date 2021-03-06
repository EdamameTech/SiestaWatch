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
    // Time duration in msec to alarm after user felt asleep
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

    public static boolean guessTimeLimitNeeded(long currentTimeMillis, int timeLimitHour,
            int timeLimitMinute, TimeZone timeZone) {
        return (currentTimeMillis + timeLimitCheckDuration > SiestaWatchUtil.timeLimitMillis(
                timeLimitHour, timeLimitMinute, currentTimeMillis, timeZone));
    }

    private void storeParameters() {
        if (DEBUG)
            Log.v(LogTag, "storeParameters()");

        SiestaWatchConf.setSleepDuration(mContext, shownSleepDurationMillis());
        SiestaWatchConf.setNeedsTimeLimit(mContext, mTimeLimitCheckBox.isChecked());
        SiestaWatchConf.setTimeLimitHour(mContext, mTimeLimitHour);
        SiestaWatchConf.setTimeLimitMinute(mContext, mTimeLimitMinute);
        SiestaWatchConf.setShownUsageVersion(mContext, mShownUsageVersion);
    }

    private void restoreParameters() {
        if (DEBUG)
            Log.v(LogTag, "restoreParameters()");

        if (mVibrationCheckBox != null)
            mVibrationCheckBox.setChecked(SiestaWatchConf.needsVibration(mContext));
        if (mTimeLimitCheckBox != null)
            mTimeLimitCheckBox.setChecked(SiestaWatchConf.needsTimeLimit(mContext));
        mTimeLimitHour = SiestaWatchConf.timeLimitHour(mContext);
        mTimeLimitMinute = SiestaWatchConf.timeLimitMinute(mContext);
        mShownUsageVersion = SiestaWatchConf.shownUsageVersion(mContext);
    }

    private String sleepDurationInMins(long sleepDurationMillis) {
        return String
                .format("%.0f", ((float) sleepDurationMillis) / 1e3 / 60.0);
    }

    private long shownSleepDurationMillis() {
        return (long) (Float.valueOf(mDurationField.getText()
                .toString()) * 60 * 1e3);
    }

    private long timeLimitInMillis() {
        return SiestaWatchUtil.timeHhmmToLong(mTimeLimitHour, mTimeLimitMinute,
                TimeZone.getDefault());
    }

    private void setSleepDurationDisplay(long sleepDurationMillis) {
        mDurationField.setText(sleepDurationInMins(sleepDurationMillis));
    }

    /* communications to the Service */
    private void startSiestaWatchService() {
        if (DEBUG)
            Log.v(LogTag, "startSiestaWatchService()");
        Intent intent = new Intent();
        intent.setClass(this, SiestaWatchService.class);
        intent.putExtra(SiestaWatchService.SleepDurationMillis,
                SiestaWatchConf.sleepDuration(mContext));
        if (hasTimeLimit == true) {
            intent.putExtra(SiestaWatchService.TimeLimitMillis,
                    timeLimitInMillis());
        } else {
            intent.putExtra(SiestaWatchService.TimeLimitMillis, 0);
        }
        intent.putExtra(SiestaWatchService.UriOfAlarmSound,
                SiestaWatchConf.uriOfAlarmSound(mContext).toString());
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
        setSleepDurationDisplay(SiestaWatchConf.sleepDuration(mContext));
        ((Button) findViewById(R.id.sleepDurationPlusButton))
                .setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        setSleepDurationDisplay(shownSleepDurationMillis() + sleepDurationStepMillis);
                    }
                });
        ((Button) findViewById(R.id.sleepDurationMinusButton))
                .setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        long d;
                        d = shownSleepDurationMillis() - sleepDurationStepMillis;
                        if (d < 0)
                            d = 0;
                        setSleepDurationDisplay(d);
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
        mTimeLimitCheckBox.setChecked(SiestaWatchActivity.guessTimeLimitNeeded(
                System.currentTimeMillis(), SiestaWatchConf.timeLimitHour(mContext),
                SiestaWatchConf.timeLimitMinute(mContext), TimeZone.getDefault()));

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
                        storeParameters();
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
