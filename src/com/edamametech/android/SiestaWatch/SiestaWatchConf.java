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

/** holds configurations from the Activity and for the Service */
public class SiestaWatchConf {
    private static final String PREF_NAME = "SiestaWatchConf";

    /** true if the user wants vibration with alarm sound */
    private static final String KEY_NEEDS_VIBRATION = "NeedsVibration";
    private static Boolean mNeedsVibration = null;
    private static Boolean DEFAULT_NEEDS_VIBRATION = true;

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
        } finally {
            mNeedsVibration = Boolean.valueOf(needsVibration);
        }
    }
}
