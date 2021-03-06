/*

Copyright (C) 2011 Green Soybean Technologies, LLC
<edamametech@gmail.com>

This program is free software: you can redistribute it and/or
modify it under the terms of the GNU General Public License as
published by the Free Software Foundation, either version 3 of
the License, or (at your option) any later version.

 */

package com.edamametech.android.SiestaWatch;

import java.io.IOException;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

public class SiestaWatchService extends Service {
    private static final int LOGLEVEL = 1;
    private static final boolean DEBUG = (LOGLEVEL > 0);
    private static final String LogTag = "SiestaWatchService";
    private static final String PrefsName = "SiestaWatchService";

    private MediaPlayer mAlarmPlayer = null;
    private Vibrator mVibrator = null;

    private static final long[] vibratePattern = new long[] {
            500, 500
    };

    /* state */
    public static final String STATE = "State";
    public static final int STATE_OFF = 1;
    // Application has not been executed
    public static final int STATE_STANDING_BY = 2;
    // Waiting for the user to fall asleep
    public static final int STATE_COUNTING_DOWN = 3;
    // Counting down to raise alarm
    public static final int STATE_ALARMING = 4;
    // Waking up the user
    public static final int STATE_SILENCING = 5;
    // Waiting for the user to fall asleep
    public static final int STATE_TIME_LIMIT = 6;
    // Reached absolute time limit

    private int mState = STATE_OFF;

    // public for SiestaWatchServiceTestCases
    public int getState() {
        return mState;
    }

    private void standBy() {
        if (DEBUG)
            Log.v(LogTag, "standBy()");
        clearAlarm();
        showStatusBarIcon(true);
        mState = STATE_STANDING_BY;
        storeParameters();
    }

    private void countDown() {
        if (DEBUG)
            Log.v(LogTag, "countDown()");
        setAlarm();
        mState = STATE_COUNTING_DOWN;
        storeParameters();
    }

    private void timeLimit() {
        if (DEBUG)
            Log.v(LogTag, "timeLimit()");
        playAlarm();
        mState = STATE_TIME_LIMIT;
        storeParameters();
    }

    private void alarm() {
        if (DEBUG)
            Log.v(LogTag, "alarm()");
        playAlarm();
        mState = STATE_ALARMING;
        storeParameters();
    }

    private void playAlarm() {
        if (DEBUG)
            Log.v(LogTag, "playAlarm()");
        clearAlarm();
        if (needsVibration) {
            if (mVibrator == null)
                mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            mVibrator.vibrate(vibratePattern, 0);
        }
        if (mAlarmPlayer == null) {
            mAlarmPlayer = new MediaPlayer();
            try {
                mAlarmPlayer.setDataSource(this, uriOfAlarmSound);
                // TODO: Better handling of exceptions
            } catch (IllegalArgumentException e) {
                Log.e(LogTag, e.toString());
            } catch (SecurityException e) {
                Log.e(LogTag, e.toString());
            } catch (IllegalStateException e) {
                Log.e(LogTag, e.toString());
            } catch (IOException e) {
                Log.e(LogTag, e.toString());
            }
            mAlarmPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mAlarmPlayer.setLooping(true);
            try {
                mAlarmPlayer.prepare();
                // TODO: Better handling of exceptions
            } catch (IllegalStateException e) {
                Log.e(LogTag, e.toString());
            } catch (IOException e) {
                Log.e(LogTag, e.toString());
            }
        }
        mAlarmPlayer.seekTo(0);
        mAlarmPlayer.start();
    }

    private void silent() {
        if (DEBUG)
            Log.v(LogTag, "silent()");
        if (needsVibration && mVibrator != null)
            mVibrator.cancel();
        if (mAlarmPlayer != null)
            mAlarmPlayer.pause();
        mState = STATE_SILENCING;
        storeParameters();
    }

    private void cancel() {
        if (DEBUG)
            Log.v(LogTag, "cancel()");
        clearAlarm();
        clearTimeLimit();
        clearStatusBarIcon();
        if (mAlarmPlayer != null) {
            mAlarmPlayer.stop();
            mAlarmPlayer.release();
        }
        mState = STATE_OFF;
        storeParameters();
        stopSelf();
    }

    private void off() {
        if (DEBUG)
            Log.v(LogTag, "off()");
        cancel();
        stopSelf();
    }

    /* receiving Broadcasts */
    private final BroadcastReceiver screenEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent broadcast) {
            if (DEBUG) {
                Log.v(LogTag, "screenEventReceiver.onReceive()");
                Log.v(LogTag, broadcast.toString());
            }
            String action = broadcast.getAction();
            if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                actionScreenOff();
            }
            if (action.equals(Intent.ACTION_SCREEN_ON)) {
                actionScreenOn();
            }
            if (action.equals(Intent.ACTION_USER_PRESENT)) {
                actionUserPresent();
            }
        }
    };

    private static final IntentFilter screenEventFilter = new IntentFilter();

    /* following methods are public for SiestaWatchServiceTestCases */
    public void actionScreenOff() {
        if (DEBUG)
            Log.v(LogTag, "actionScreenOff()");
        switch (mState) {
            case STATE_STANDING_BY:
                countDown();
                break;
            case STATE_SILENCING:
                alarm();
                break;
            case STATE_TIME_LIMIT:
                off();
                break;
        }
    }

    public void actionUserPresent() {
        if (DEBUG)
            Log.v(LogTag, "actionUserPresent()");
        switch (mState) {
            case STATE_COUNTING_DOWN:
                showRemainingTime();
                standBy();
                break;
            case STATE_ALARMING:
                off();
                break;
            case STATE_SILENCING:
                off();
                break;
        }
    }

    public void actionAlarm() {
        if (DEBUG)
            Log.v(LogTag, "actionAlarm()");
        if (mState == STATE_COUNTING_DOWN) {
            alarm();
            return;
        }
    }

    public void actionTimeLimit() {
        if (DEBUG)
            Log.v(LogTag, "actionTimeLimit()");
        switch (mState) {
            case STATE_STANDING_BY:
                timeLimit();
                break;
            case STATE_COUNTING_DOWN:
                alarm();
                break;
        }
    }

    public void actionScreenOn() {
        if (DEBUG)
            Log.v(LogTag, "actionScreenOn()");
        if (mState == STATE_ALARMING) {
            silent();
            return;
        }
    }

    /* parameters */
    // Key for Extras in Intent to supply uriOfAlarmSound as a String
    public static final String UriOfAlarmSound = "UriOfAlarmSound";
    // URI of the alarm sound
    private Uri uriOfAlarmSound = null;

    // Key for Extras in Intent to supply sleepDurationMIllis as a long
    public static final String SleepDurationMillis = "SleepDurationMillis";
    // Time duration in msec to alarm after user felt asleep
    private long sleepDurationMillis = 0;
    private long sleepUntilMillis = 0;

    // Key for Extras in Intent to supply absolute TimeLimit as a long
    public static final String TimeLimitMillis = "TimeLimitMillis";
    // Absolute Time in msec to alarm user about the time limit
    private long timeLimitMillis = 0;

    // Key for Extras in Intent to supply if vibration is needed as a boolean
    public static final String NeedsVibration = "NeedsVibration";
    private boolean needsVibration = false;

    private void storeParameters() {
        if (DEBUG)
            Log.v(LogTag, "storeParameters()");
        SharedPreferences.Editor editor = getSharedPreferences(PrefsName, 0)
                .edit();
        editor.putString(UriOfAlarmSound, uriOfAlarmSound.toString());
        editor.putLong(SleepDurationMillis, sleepDurationMillis);
        editor.putLong(TimeLimitMillis, timeLimitMillis);
        editor.putBoolean(NeedsVibration, needsVibration);
        editor.putInt(STATE, mState);
        editor.commit();
    }

    private void clearStoredParameters() {
        if (DEBUG)
            Log.v(LogTag, "storeParameters()");
        SharedPreferences.Editor editor = getSharedPreferences(PrefsName, 0)
                .edit();
        editor.clear();
        editor.commit();
    }

    private void restoreParameters() {
        if (DEBUG)
            Log.v(LogTag, "restoreParameters()");
        SharedPreferences prefs = getSharedPreferences(PrefsName, 0);
        if (prefs.contains(UriOfAlarmSound)) {
            uriOfAlarmSound = Uri.parse(prefs.getString(UriOfAlarmSound, ""));
        }
        if (prefs.contains(SleepDurationMillis)) {
            sleepDurationMillis = prefs.getLong(SleepDurationMillis, 0);
        }
        if (prefs.contains(TimeLimitMillis)) {
            timeLimitMillis = prefs.getLong(TimeLimitMillis, 0);
        }
        if (prefs.contains(NeedsVibration)) {
            needsVibration = prefs.getBoolean(NeedsVibration, false);
        }
        if (prefs.contains(TimeLimitMillis) || prefs.contains(NeedsVibration)) {
            setTimeLimit();
        }
        if (prefs.contains(STATE)) {
            mState = prefs.getInt(STATE, 0);
        }
    }

    /* alarms */
    // Key for Extras in Intent to supply Action as an Int
    public static final String SERVICE_ACTION = "Action";
    // Intent that makes us to go off the alarm
    private static final int ACTION_ALARM = 0;
    private static final int ACTION_TIME_LIMIT = 1;
    public static final int ACTION_CANCEL = 2;

    private void setAlarm() {
        if (DEBUG)
            Log.v(LogTag, "setAlarm()");
        sleepUntilMillis = System.currentTimeMillis() + sleepDurationMillis;
        setAlarmWithAction(ACTION_ALARM, sleepUntilMillis);
    }

    private void setTimeLimit() {
        if (DEBUG)
            Log.v(LogTag, "setTimeLimit()");
        if (timeLimitMillis > System.currentTimeMillis()) {
            setAlarmWithAction(ACTION_TIME_LIMIT, timeLimitMillis);
        }
    }

    private void setAlarmWithAction(Integer action, long alarmTime) {
        if (DEBUG)
            Log.v(LogTag, "setAlarmWithAction(" + action + ", ... )");

        Intent alarmIntent = new Intent();
        alarmIntent.setClass(this, SiestaWatchService.class);
        alarmIntent.putExtra(SiestaWatchService.SERVICE_ACTION, action);

        PendingIntent alarmSender = PendingIntent.getService(this, action,
                alarmIntent, 0);

        AlarmManager alarmManager = (AlarmManager) this
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, alarmSender);

        if (DEBUG) {
            Log.v(LogTag,
                    "Alarm is set at "
                            + DateUtils.formatDateTime(this, alarmTime,
                                    DateUtils.FORMAT_SHOW_TIME
                                            + DateUtils.FORMAT_SHOW_DATE)
                            + " for action " + action);
        }
    }

    private void clearAlarm() {
        if (DEBUG)
            Log.v(LogTag, "clearAlarm()");
        clearAlarmWithAction(ACTION_ALARM);
        sleepUntilMillis = 0;
    }

    private void clearTimeLimit() {
        if (DEBUG)
            Log.v(LogTag, "clearTimeLimit()");
        clearAlarmWithAction(ACTION_TIME_LIMIT);
    }

    private void clearAlarmWithAction(Integer action) {
        if (DEBUG)
            Log.v(LogTag, "clearAlarmWithAction(" + action + ")");

        AlarmManager alarmManager = (AlarmManager) this
                .getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmSender = PendingIntent.getService(this, action,
                null, 0);
        alarmManager.cancel(alarmSender);
    }

    /* Service things */
    @Override
    public IBinder onBind(Intent arg0) {
        // Will not be bound
        return null;
    }

    @Override
    public void onCreate() {
        if (DEBUG)
            Log.v(LogTag, "onCreate()");
        screenEventFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenEventFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenEventFilter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(screenEventReceiver, screenEventFilter);
        restoreParameters();
        /* Application Manager only calls onCreate() when restarting the service */
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
            Log.v(LogTag, "handleStartCommand()");

        if (intent == null) {
            if (DEBUG)
                Log.v(LogTag, "Got a null intent");
            restoreParameters();
            showStatusBarIcon(false);
        } else {
            if (DEBUG)
                Log.v(LogTag, intent.toString());

            Bundle extras = intent.getExtras();
            if (extras == null) {
                clearStoredParameters();
            } else {
                if (extras.containsKey(SERVICE_ACTION)) {
                    if (DEBUG)
                        Log.v(LogTag, SERVICE_ACTION + ": " + extras.getInt(SERVICE_ACTION));
                    switch (extras.getInt(SERVICE_ACTION)) {
                        case ACTION_ALARM:
                            actionAlarm();
                            break;
                        case ACTION_TIME_LIMIT:
                            actionTimeLimit();
                            break;
                        case ACTION_CANCEL:
                            cancel();
                            break;
                    }
                } else {
                    if (extras.containsKey(UriOfAlarmSound)) {
                        if (DEBUG)
                            Log.v(LogTag,
                                    UriOfAlarmSound + ": "
                                            + extras.getString(UriOfAlarmSound));
                        uriOfAlarmSound = Uri.parse(extras
                                .getString(UriOfAlarmSound));
                    }
                    if (extras.containsKey(SleepDurationMillis)) {
                        if (DEBUG)
                            Log.v(LogTag,
                                    SleepDurationMillis
                                            + ": "
                                            + extras.getLong(SleepDurationMillis));
                        sleepDurationMillis = extras
                                .getLong(SleepDurationMillis);
                    }
                    if (extras.containsKey(TimeLimitMillis)) {
                        if (DEBUG)
                            Log.v(LogTag,
                                    TimeLimitMillis + ": "
                                            + extras.getLong(TimeLimitMillis));
                        timeLimitMillis = extras.getLong(TimeLimitMillis);
                    }
                    if (extras.containsKey(NeedsVibration)) {
                        if (DEBUG)
                            Log.v(LogTag,
                                    NeedsVibration + ": "
                                            + extras.getBoolean(NeedsVibration));
                        needsVibration = extras.getBoolean(NeedsVibration);
                    }
                    storeParameters();
                    if (uriOfAlarmSound != null && sleepDurationMillis > 0) {
                        setTimeLimit();
                        standBy();
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        if (DEBUG)
            Log.v(LogTag, "onDestroy()");
        unregisterReceiver(screenEventReceiver);
        clearStatusBarIcon();
    }

    private void showStatusBarIcon(boolean showTicker) {
        if (DEBUG)
            Log.v(LogTag, "showStatusBarIcon()");
        Intent intent = new Intent();
        intent.setClass(this, SiestaWatchActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, 0);
        String ticker = null;
        if (showTicker)
            ticker = getString(R.string.notification_ticker);
        Notification notification = new Notification(R.drawable.ic_stat_notify,
                ticker, System.currentTimeMillis());
        notification.setLatestEventInfo(this, getString(R.string.app_name),
                currentStatusText(), pendingIntent);
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }

    private void clearStatusBarIcon() {
        if (DEBUG)
            Log.v(LogTag, "clearStatusBarIcon()");
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    private String currentStatusText() {
        if (DEBUG)
            Log.v(LogTag, "currentStatusText()");
        String sleepDurationText = String.format(
                getString(R.string.sleep_duration_format),
                (float) sleepDurationMillis / 60000);
        if (timeLimitMillis > System.currentTimeMillis()) {
            String timeLimitText = SiestaWatchUtil.timeLongToHhmm(
                    timeLimitMillis, DateFormat.getTimeFormat(this));
            return String.format(
                    getString(R.string.status_text_with_time_limit),
                    sleepDurationText, timeLimitText);
        } else {
            return String.format(
                    getString(R.string.status_text_without_time_limit),
                    sleepDurationText);
        }
    }

    private void showRemainingTime() {
        if (DEBUG)
            Log.v(LogTag, "showRemainingTime()");
        long untilMillis;
        untilMillis = sleepUntilMillis;
        if (timeLimitMillis > 0 && sleepUntilMillis > timeLimitMillis)
            untilMillis = timeLimitMillis;
        Context context = getApplicationContext();
        String toastText = String
                .format(getString(R.string.remaining_time_format),
                        getString(R.string.app_name),
                        (float) (untilMillis - System.currentTimeMillis()) / 60000);
        Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();
    }
}
