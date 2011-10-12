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
}
