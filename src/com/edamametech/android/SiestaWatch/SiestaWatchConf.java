/*

Copyright (C) 2011 Green Soybean Technologies, LLC
<edamametech@gmail.com>

This program is free software: you can redistribute it and/or
modify it under the terms of the GNU General Public License as
published by the Free Software Foundation, either version 3 of
the License, or (at your option) any later version.

 */

package com.edamametech.android.SiestaWatch;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import java.util.Calendar;
import java.util.TimeZone;

/** holds configurations from the Activity and for the Service */
public class SiestaWatchConf {
    private static final String PREF_NAME = "SiestaWatchActivity";

    /** duration the user wants to sleep in msec */
    private static final String KEY_SLEEP_DURATRION_MILLIS = "SleepDurationMillis";
    private static Long mSleepDurationMillis = null;
    private static long DEFAULT_SLEEP_DURATION_MILLIS = 1800000;

    /** URI of the alarm sound */
    private static final String KEY_URI_OF_ALARM_SOUND = "UriOfAlarmSound";
    private static Uri mUriOfAlarmSound = null;
    private static Uri DEFAULT_URI_OF_ALARM_SOUND = Settings.System.DEFAULT_ALARM_ALERT_URI;

    /** true if the user wants vibration with alarm sound */
    private static final String KEY_NEEDS_VIBRATION = "NeedsVibration";
    private static Boolean mNeedsVibration = null;
    private static boolean DEFAULT_NEEDS_VIBRATION = true;

    /** time limit the user want to wake up in 24-hour time */
    private static final String KEY_TIME_LIMIT_HOUR = "TimeLimitHour";
    private static Integer mTimeLimitHour = null;
    private static int DEFAULT_TIME_LIMIT_HOUR = 14;

    private static final String KEY_TIME_LIMIT_MINUTE = "TimeLimitMinute";
    private static Integer mTimeLimitMinute = null;
    private static int DEFAULT_TIME_LIMIT_MINUTE = 0;

    /** true if user want to be waken up at time limit */
    private static final String KEY_NEEDS_TIME_LIMIT = "NeedsTimeLimit";
    private static Boolean mNeedsTimeLimit = null;
    private static boolean DEFAULT_NEEDS_TIME_LIMIT = false;

    /** version of usage dialog the user has seen */
    private static final String KEY_SHOWN_USAGE_VERSION = "ShownUsageVersion";
    private static Integer mShownUsageVersion = null;
    private static int DEFAULT_SHOWN_USAGE_VERSION = 0;

    public static synchronized long sleepDuration(Context context) {
        if (mSleepDurationMillis != null) {
            return mSleepDurationMillis.longValue();
        }
        long result = currentLongConfiguration(context, KEY_SLEEP_DURATRION_MILLIS,
                DEFAULT_SLEEP_DURATION_MILLIS);
        mSleepDurationMillis = Long.valueOf(result);
        return result;
    }

    public static synchronized void setSleepDuration(Context context, long sleepDuration) {
        try {
            setLongConfiguration(context, KEY_SLEEP_DURATRION_MILLIS, sleepDuration);
        } finally {
            mSleepDurationMillis = Long.valueOf(sleepDuration);
        }
    }

    public static synchronized Uri uriOfAlarmSound(Context context) {
        if (mUriOfAlarmSound != null) {
            return mUriOfAlarmSound;
        }
        final SharedPreferences pref = context
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mUriOfAlarmSound = Uri.parse(pref.getString(KEY_URI_OF_ALARM_SOUND,
                DEFAULT_URI_OF_ALARM_SOUND.toString()));
        return mUriOfAlarmSound;
    }

    public static synchronized void setUriOfAlarmSound(Context context, Uri uriOfAlarmSound) {
        final Editor edit = context
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        try {
            edit.putString(KEY_URI_OF_ALARM_SOUND, uriOfAlarmSound.toString());
            edit.commit();
        } finally {
            mUriOfAlarmSound = uriOfAlarmSound;
        }
    }

    public static synchronized boolean needsVibration(Context context) {
        if (mNeedsVibration != null) {
            return mNeedsVibration.booleanValue();
        }
        boolean result = currentBooleanConfiguration(context, KEY_NEEDS_VIBRATION,
                DEFAULT_NEEDS_VIBRATION);
        mNeedsVibration = Boolean.valueOf(result);
        return result;
    }

    public static synchronized void setNeedsVibration(Context context, boolean needsVibration) {
        try {
            setBooleanConfiguration(context, KEY_NEEDS_VIBRATION, needsVibration);
        } finally {
            mNeedsVibration = Boolean.valueOf(needsVibration);
        }
    }

    public static synchronized int timeLimitHour(Context context) {
        if (mTimeLimitHour != null) {
            return mTimeLimitHour.intValue();
        }
        int result = currentIntConfiguration(context, KEY_TIME_LIMIT_HOUR, DEFAULT_TIME_LIMIT_HOUR);
        mTimeLimitHour = Integer.valueOf(result);
        return result;
    }

    public static synchronized void setTimeLimitHour(Context context, int timeLimitHour) {
        try {
            setIntConfiguration(context, KEY_TIME_LIMIT_HOUR, timeLimitHour);
        } finally {
            mTimeLimitHour = Integer.valueOf(timeLimitHour);
        }
    }

    public static synchronized int timeLimitMinute(Context context) {
        if (mTimeLimitMinute != null) {
            return mTimeLimitMinute.intValue();
        }
        int result = currentIntConfiguration(context, KEY_TIME_LIMIT_MINUTE,
                DEFAULT_TIME_LIMIT_MINUTE);
        mTimeLimitMinute = Integer.valueOf(result);
        return result;
    }

    public static synchronized void setTimeLimitMinute(Context context, int timeLimitMinute) {
        try {
            setIntConfiguration(context, KEY_TIME_LIMIT_MINUTE, timeLimitMinute);
        } finally {
            mTimeLimitMinute = Integer.valueOf(timeLimitMinute);
        }
    }

    public static synchronized long timeLimitMillis(Context context, long currentMillis,
            TimeZone timeZone) {
        /* obtain parameters onto our own fields */
        timeLimitHour(context);
        timeLimitMinute(context);

        /* calculate */
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentMillis);
        Log.i("conf", cal.getTime().toString());
        cal.setTimeZone(timeZone);
        cal.set(Calendar.HOUR_OF_DAY, mTimeLimitHour);
        Log.i("conf", cal.getTime().toString());
        cal.set(Calendar.MINUTE, mTimeLimitMinute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Log.i("conf", cal.getTime().toString());
        return cal.getTimeInMillis();
    }

    public static synchronized boolean needsTimeLimit(Context context) {
        if (mNeedsTimeLimit != null) {
            return mNeedsTimeLimit.booleanValue();
        }
        boolean result = currentBooleanConfiguration(context, KEY_NEEDS_TIME_LIMIT,
                DEFAULT_NEEDS_TIME_LIMIT);
        mNeedsTimeLimit = Boolean.valueOf(result);
        return result;
    }

    public static synchronized void setNeedsTimeLimit(Context context, boolean needsTimeLimit) {
        try {
            setBooleanConfiguration(context, KEY_NEEDS_TIME_LIMIT, needsTimeLimit);
        } finally {
            mNeedsTimeLimit = Boolean.valueOf(needsTimeLimit);
        }
    }

    public static synchronized int shownUsageVersion(Context context) {
        if (mShownUsageVersion != null) {
            return mShownUsageVersion.intValue();
        }
        final SharedPreferences pref = context
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int result = pref.getInt(KEY_SHOWN_USAGE_VERSION, DEFAULT_SHOWN_USAGE_VERSION);
        mShownUsageVersion = Integer.valueOf(result);
        return result;
    }

    public static synchronized void setShownUsageVersion(Context context, int shownUsageVersion) {
        final Editor edit = context
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        try {
            edit.putInt(KEY_SHOWN_USAGE_VERSION, shownUsageVersion);
            edit.commit();
        } finally {
            mShownUsageVersion = Integer.valueOf(shownUsageVersion);
        }
    }

    /* common routine */
    private static int currentIntConfiguration(Context context, String prefKey,
            int defaultValue) {
        final SharedPreferences pref = context
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(prefKey, defaultValue);
    }

    private static void setIntConfiguration(Context context, String prefKey, int value) {
        final Editor edit = context
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        edit.putInt(prefKey, value);
        edit.commit();
    }

    private static long currentLongConfiguration(Context context, String prefKey,
            long defaultValue) {
        final SharedPreferences pref = context
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getLong(prefKey, defaultValue);
    }

    private static void setLongConfiguration(Context context, String prefKey, long value) {
        final Editor edit = context
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        edit.putLong(prefKey, value);
        edit.commit();
    }

    private static Boolean currentBooleanConfiguration(Context context, String prefKey,
            Boolean defaultValue) {
        final SharedPreferences pref = context
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(prefKey, defaultValue);
    }

    private static void setBooleanConfiguration(Context context, String prefKey, Boolean value) {
        final Editor edit = context
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        edit.putBoolean(prefKey, value);
        edit.commit();
    }
}
