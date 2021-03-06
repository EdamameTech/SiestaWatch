/*

Copyright (C) 2011 Green Soybean Technologies, LLC
<edamametech@gmail.com>

This program is free software: you can redistribute it and/or
modify it under the terms of the GNU General Public License as
published by the Free Software Foundation, either version 3 of
the License, or (at your option) any later version.

 */

package com.edamametech.android.SiestaWatch.test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

import com.edamametech.android.SiestaWatch.SiestaWatchUtil;

public class SiestaWatchUtilTestCase extends TestCase {
    public void testTimeLongToHhmm() {
        DateFormat df = new SimpleDateFormat("HH:mm");
        TimeZone tz = TimeZone.getTimeZone("US/Hawaii");
        df.setTimeZone(tz);
        assertEquals("12:34",
                SiestaWatchUtil.timeLongToHhmm(1316039640000L, df));
        /* 2011-09-14 12:34 HST */
    }

    public void testTimeHhmmToLong() {
        String hour = "12";
        String minutes = "34";
        TimeZone tz = TimeZone.getTimeZone("US/Hawaii");
        Calendar cal = Calendar.getInstance(tz);
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hour));
        cal.set(Calendar.MINUTE, Integer.valueOf(minutes));
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long expected = cal.getTimeInMillis();
        long result = SiestaWatchUtil.timeHhmmToLong(12, 34, tz);
        /*
         * return value of the method depends upon current date and might be
         * pointing the next day
         */
        if (expected < result) {
            expected += 24 * 3600 * 1000;
        }
        assertEquals(expected, result);
    }

    public void testTimeLimitMillis() {
        long current = 1319012502000L; // 2011-10-18 22:21:42 -1000
        long target = 1319014800000L; // 2011-10-18 23:00:00 -1000
        TimeZone tz = TimeZone.getTimeZone("Pacific/Honolulu");
        assertEquals(target, SiestaWatchUtil.timeLimitMillis(23, 00, current, tz));
    }

    public void testTimeLimitMillisWithOneDayAdvance() {
        long current = 1319012502000L; // 2011-10-18 22:21:42 -1000
        long target = 1319090400000L; // 2011-10-19 20:00:00 -1000
        TimeZone tz = TimeZone.getTimeZone("Pacific/Honolulu");
        assertEquals(target, SiestaWatchUtil.timeLimitMillis(20, 00, current, tz));
    }

    public void testTimeLimitMillisFromSummerToWinter() {
        long current = 1319905800000L; // 2011-10-29 18:30:00 CEST
        long target = 1319981400000L; // 2011-10-30 14:30:00 CET
        TimeZone tz = TimeZone.getTimeZone("Europe/Berlin");
        assertEquals(3600 * 1000L, tz.getRawOffset());

        long calculated = SiestaWatchUtil.timeLimitMillis(14, 30, current, tz);
        assertEquals(target, calculated);
    }

    public void testTimeLimitMillisFromWinterToSummer() {
        long current = 1301160600000L; // 2011-03-26 18:30:00 CET
        long target = 1301229000000L; // 2011-03-27 14:30:00 CEST
        TimeZone tz = TimeZone.getTimeZone("Europe/Berlin");
        assertEquals(3600 * 1000L, tz.getRawOffset());

        long calculated = SiestaWatchUtil.timeLimitMillis(14, 30, current, tz);
        assertEquals(target, calculated);
    }
}
