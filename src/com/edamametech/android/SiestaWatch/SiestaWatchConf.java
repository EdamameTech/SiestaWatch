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

import java.util.Calendar;

/** holds configurations from the Activity and for the Service */
public class SiestaWatchConf {
    private static final String PREF_NAME = "SiestaWatchConf";
    /* TODO: write tests! */

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

    /** time limit the user want to wake up in msec */
    private static final String KEY_TIME_LIMIT_MILLIS = "TimeLimitMillis";
    private static Long mTimeLimitMillis = null;
    private static long DEFAULT_TIME_LIMIT_DURATION_MILLIS = 3600000;

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
        final SharedPreferences pref = context
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long result = pref.getLong(KEY_SLEEP_DURATRION_MILLIS, DEFAULT_SLEEP_DURATION_MILLIS);
        mSleepDurationMillis = Long.valueOf(result);
        return result;
    }

    public static synchronized void setSleepDuration(Context context, long sleepDuration) {
        final Editor edit = context
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        try {
            edit.putLong(KEY_SLEEP_DURATRION_MILLIS, sleepDuration);
            edit.commit();
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

    public static synchronized void setUriOfAlarmSound(Context context, String stringUriOfAlarmSound) {
        final Editor edit = context
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        try {
            edit.putString(KEY_URI_OF_ALARM_SOUND, stringUriOfAlarmSound);
            edit.commit();
        } finally {
            mUriOfAlarmSound = Uri.parse(stringUriOfAlarmSound);
        }
    }

    public static synchronized boolean needsVibration(Context context) {
        if (mNeedsVibration != null) {
            return mNeedsVibration.booleanValue();
        }
        final SharedPreferences pref = context
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean result = pref.getBoolean(KEY_NEEDS_VIBRATION, DEFAULT_NEEDS_VIBRATION);
        mNeedsVibration = Boolean.valueOf(result);
        return result;
    }

    public static synchronized void setNeedsVibration(Context context, boolean needsVibration) {
        final Editor edit = context
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        try {
            edit.putBoolean(KEY_NEEDS_VIBRATION, needsVibration);
            edit.commit();
        } finally {
            mNeedsVibration = Boolean.valueOf(needsVibration);
        }
    }

    /*
     * returns time limit in millis, advancing a day if it is in the past -
     * comparing with currentTimeMillis
     */
    public static synchronized long timeLimitMillis(Context context, long currentTimeMillis) {
        if (mTimeLimitMillis != null) {
            return mTimeLimitMillis.longValue();
        }
        final SharedPreferences pref = context
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long result = pref.getLong(KEY_TIME_LIMIT_MILLIS, System.currentTimeMillis()
                + DEFAULT_TIME_LIMIT_DURATION_MILLIS);
        result = advancedTimeInMillis(result, currentTimeMillis);
        mTimeLimitMillis = Long.valueOf(result);
        return result;
    }

    public static synchronized void setTimeLimitMillis(Context context, long timeLimit,
            long currentTimeMillis) {
        final Editor edit = context
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        try {
            timeLimit = advancedTimeInMillis(timeLimit, currentTimeMillis);
            edit.putLong(KEY_TIME_LIMIT_MILLIS, timeLimit);
            edit.commit();
        } finally {
            mTimeLimitMillis = Long.valueOf(timeLimit);
        }
    }

    /** returns the same time for tomorrow */
    private static long advancedTimeInMillis(long timeInMillis, long currentTimeMillis) {
        if (timeInMillis < currentTimeMillis) {
            Calendar orig = Calendar.getInstance();
            orig.setTimeInMillis(timeInMillis);
            Calendar current = Calendar.getInstance();
            current.setTimeInMillis(currentTimeMillis);
            Calendar advanced = Calendar.getInstance();
            advanced.setTimeInMillis(currentTimeMillis);
            advanced.set(Calendar.HOUR_OF_DAY, orig.get(Calendar.HOUR_OF_DAY));
            advanced.set(Calendar.MINUTE, orig.get(Calendar.MINUTE));
            advanced.set(Calendar.SECOND, orig.get(Calendar.SECOND));
            if (advanced.before(current)) {
                advanced.add(Calendar.DATE, 1);
            }
            timeInMillis = advanced.getTimeInMillis();
        }
        return timeInMillis;
    }

    public static synchronized boolean needsTimeLimit(Context context) {
        if (mNeedsTimeLimit != null) {
            return mNeedsTimeLimit.booleanValue();
        }
        final SharedPreferences pref = context
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean result = pref.getBoolean(KEY_NEEDS_TIME_LIMIT, DEFAULT_NEEDS_TIME_LIMIT);
        mNeedsTimeLimit = Boolean.valueOf(result);
        return result;
    }

    public static synchronized void setNeedsTimeLimit(Context context, boolean needsTimeLimit) {
        final Editor edit = context
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        try {
            edit.putBoolean(KEY_NEEDS_TIME_LIMIT, needsTimeLimit);
            edit.commit();
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

}
