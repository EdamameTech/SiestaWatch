/*

Copyright (C) 2011 Green Soybean Technologies, LLC
<edamametech@gmail.com>

This program is free software: you can redistribute it and/or
modify it under the terms of the GNU General Public License as
published by the Free Software Foundation, either version 3 of
the License, or (at your option) any later version.

 */

package com.edamametech.android.SiestaWatch.test;

import android.content.Intent;
import android.provider.Settings;
import android.test.ServiceTestCase;

import com.edamametech.android.SiestaWatch.SiestaWatchService;

public class SiestaWatchServiceTestCase extends
        ServiceTestCase<SiestaWatchService> {

    private SiestaWatchService mService;
    private Intent mStandardIntent;

    public SiestaWatchServiceTestCase() {
        super(SiestaWatchService.class);
    }

    @Override
    protected void setUp() {
        mStandardIntent = new Intent();
        mStandardIntent.setClass(getContext(), SiestaWatchService.class);
        mStandardIntent.putExtra(SiestaWatchService.SleepDurationMillis, 1000L);
        mStandardIntent.putExtra(SiestaWatchService.TimeLimitMillis,
                System.currentTimeMillis() + 180000L);
        mStandardIntent.putExtra(SiestaWatchService.UriOfAlarmSound,
                Settings.System.DEFAULT_ALARM_ALERT_URI.toString());
    }

    public void testRestartWithNullIntentFromStandBy() {
        /* pretending the normal Service is stopped and ... */
        startService(mStandardIntent);
        mService = getService();
        assertEquals(SiestaWatchService.STATE_STANDING_BY, mService.getState());
        mService.stopSelf();
        /* restarted by the ActivityManager, intent seems to be null, */
        startService(null);
        mService = getService();
        /* and we want the Service to keep the parameters */
        assertEquals(SiestaWatchService.STATE_STANDING_BY, mService.getState());
    }

    public void testRestartWithNullIntentFromOff() {
        Intent intent = new Intent();
        intent.setClass(getContext(), SiestaWatchService.class);
        startService(intent);
        getService().stopSelf();
        startService(null);
        mService = getService();
        assertEquals(SiestaWatchService.STATE_OFF, mService.getState());
    }

    public void testStandBy() {
        startService(mStandardIntent);
        mService = getService();
        assertEquals(SiestaWatchService.STATE_STANDING_BY, mService.getState());
    }

    public void testFromStandByToCountDown() {
        startService(mStandardIntent);
        mService = getService();
        mService.actionScreenOff();
        assertEquals(SiestaWatchService.STATE_COUNTING_DOWN, mService.getState());
    }

    public void testFromStandByToTimeLimit() {
        startService(mStandardIntent);
        mService = getService();
        mService.actionTimeLimit();
        assertEquals(SiestaWatchService.STATE_TIME_LIMIT, mService.getState());
    }

    public void testFromCountDownToStandBy() {
        startService(mStandardIntent);
        mService = getService();
        mService.actionScreenOff();
        mService.actionUserPresent();
        assertEquals(SiestaWatchService.STATE_STANDING_BY, mService.getState());
    }

    public void testFromCountDownToAlarm() {
        startService(mStandardIntent);
        mService = getService();
        mService.actionScreenOff();
        mService.actionAlarm();
        assertEquals(SiestaWatchService.STATE_ALARMING, mService.getState());
    }

    public void testFromCountDownToAlarmThroughTimeLimit() {
        startService(mStandardIntent);
        mService = getService();
        mService.actionScreenOff();
        mService.actionTimeLimit();
        assertEquals(SiestaWatchService.STATE_ALARMING, mService.getState());
    }

    public void testFromTimeLimitToOff() {
        startService(mStandardIntent);
        mService = getService();
        mService.actionTimeLimit();
        mService.actionScreenOff();
        assertEquals(SiestaWatchService.STATE_OFF, mService.getState());
    }

    public void testFromAlarmToSilent() {
        startService(mStandardIntent);
        mService = getService();
        mService.actionScreenOff();
        mService.actionAlarm();
        mService.actionScreenOn();
        assertEquals(SiestaWatchService.STATE_SILENCING, mService.getState());
    }

    public void testFromAlarmToOff() {
        startService(mStandardIntent);
        mService = getService();
        mService.actionScreenOff();
        mService.actionAlarm();
        mService.actionUserPresent();
        assertEquals(SiestaWatchService.STATE_OFF, mService.getState());
    }

    public void testFromSilentToAlarm() {
        startService(mStandardIntent);
        mService = getService();
        mService.actionScreenOff();
        mService.actionAlarm();
        mService.actionScreenOn();
        mService.actionScreenOff();
        assertEquals(SiestaWatchService.STATE_ALARMING, mService.getState());
    }

    public void testFromSilentToOff() {
        startService(mStandardIntent);
        mService = getService();
        mService.actionScreenOff();
        mService.actionAlarm();
        mService.actionScreenOn();
        mService.actionUserPresent();
        assertEquals(SiestaWatchService.STATE_OFF, mService.getState());
    }
}
