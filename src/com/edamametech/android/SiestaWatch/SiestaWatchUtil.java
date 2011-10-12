/*

Copyright (C) 2011 Green Soybean Technologies, LLC
<edamametech@gmail.com>

This program is free software: you can redistribute it and/or
modify it under the terms of the GNU General Public License as
published by the Free Software Foundation, either version 3 of
the License, or (at your option) any later version.

 */

package com.edamametech.android.SiestaWatch;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class SiestaWatchUtil {
    public static String timeLongToHhmm(long timeMillis, DateFormat df) {
        Date d = new Date(timeMillis);
        return df.format(d);
    }

    public static long timeHhmmToLong(int hh, int mm, TimeZone tz) {
        Calendar cal = Calendar.getInstance(tz);
        cal.setTime(new Date());
        Long currentMillis = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, hh);
        cal.set(Calendar.MINUTE, mm);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Long result = cal.getTimeInMillis();
        if (result < currentMillis) {
            result += 3600 * 24 * 1000;
        }
        return result;
    }
}
