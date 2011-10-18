/*

Copyright (C) 2011 Green Soybean Technologies, LLC
<edamametech@gmail.com>

This program is free software: you can redistribute it and/or
modify it under the terms of the GNU General Public License as
published by the Free Software Foundation, either version 3 of
the License, or (at your option) any later version.

 */

package com.edamametech.android.SiestaWatch.test;

import com.edamametech.android.SiestaWatch.SiestaWatchConf;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.test.mock.MockContext;

public class SiestaWatchConfTestCase extends AndroidTestCase {
    Context mContext;

    private class ConfTestMockContext extends MockContext {
        private Context mTestContext;
        private static final String PREFIX = "SiestaWatchConfTestCase.";

        public ConfTestMockContext(Context context) {
            mTestContext = context;
        }

        public SharedPreferences getSharedPreferences(String name, int mode) {
            return mTestContext.getSharedPreferences(PREFIX + name, mode);
        }
    }

    public void setUp() {
        mContext = new ConfTestMockContext(getContext());
    }

    public void testSleepDuration() {
        long orig = SiestaWatchConf.sleepDuration(mContext);
        SiestaWatchConf.setSleepDuration(mContext, 12345L);
        assertEquals(12345L, SiestaWatchConf.sleepDuration(mContext));
        SiestaWatchConf.setSleepDuration(mContext, orig);
    }

    public void testUriOfAlarmSound() {
        Uri orig = SiestaWatchConf.uriOfAlarmSound(mContext);
        SiestaWatchConf.setUriOfAlarmSound(mContext, Uri.parse("http://example.com"));
        assertEquals(Uri.parse("http://example.com"), SiestaWatchConf.uriOfAlarmSound(mContext));
        SiestaWatchConf.setUriOfAlarmSound(mContext, orig);
    }

    public void testNeedsVibration() {
        boolean orig = SiestaWatchConf.needsVibration(mContext);
        SiestaWatchConf.setNeedsVibration(mContext, true);
        assertEquals(true, SiestaWatchConf.needsVibration(mContext));
        SiestaWatchConf.setNeedsVibration(mContext, false);
        assertEquals(false, SiestaWatchConf.needsVibration(mContext));
        SiestaWatchConf.setNeedsVibration(mContext, orig);
    }

    public void testTimeLimitHour() {
        int orig = SiestaWatchConf.timeLimitHour(mContext);
        SiestaWatchConf.setTimeLimitHour(mContext, 12);
        assertEquals(12, SiestaWatchConf.timeLimitHour(mContext));
        SiestaWatchConf.setTimeLimitHour(mContext, orig);
    }

    public void testTimeLimitMinute() {
        int orig = SiestaWatchConf.timeLimitMinute(mContext);
        SiestaWatchConf.setTimeLimitMinute(mContext, 34);
        assertEquals(34, SiestaWatchConf.timeLimitMinute(mContext));
        SiestaWatchConf.setTimeLimitMinute(mContext, orig);
    }

    public void testNeedsTimeLimit() {
        boolean orig = SiestaWatchConf.needsTimeLimit(mContext);
        SiestaWatchConf.setNeedsTimeLimit(mContext, true);
        assertEquals(true, SiestaWatchConf.needsTimeLimit(mContext));
        SiestaWatchConf.setNeedsTimeLimit(mContext, false);
        assertEquals(false, SiestaWatchConf.needsTimeLimit(mContext));
        SiestaWatchConf.setNeedsTimeLimit(mContext, orig);
    }

    public void testShownUsageVersion() {
        int orig = SiestaWatchConf.shownUsageVersion(mContext);
        SiestaWatchConf.setShownUsageVersion(mContext, 12345);
        assertEquals(12345, SiestaWatchConf.shownUsageVersion(mContext));
        SiestaWatchConf.setShownUsageVersion(mContext, orig);
    }
}
