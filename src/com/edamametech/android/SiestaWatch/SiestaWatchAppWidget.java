/*

Copyright (C) 2011 Green Soybean Technologies, LLC
<edamametech@gmail.com>

This program is free software: you can redistribute it and/or
modify it under the terms of the GNU General Public License as
published by the Free Software Foundation, either version 3 of
the License, or (at your option) any later version.

 */

package com.edamametech.android.SiestaWatch;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

public class SiestaWatchAppWidget extends AppWidgetProvider {
	private static final int LOGLEVEL = 1;
	private static final boolean DEBUG = (LOGLEVEL > 0);
	private static final String LogTag = "SiestaWatchAppWidget";
	private static final String PrefsName = "SiestaWatchAppWidget";
	private static final String SleepDurationMillisFormat = "SleepDurationMillis%d";

	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		if (DEBUG)
			Log.v(LogTag, "onUpdate()");

		final int nWidgets = appWidgetIds.length;
		for (int i = 0; i < nWidgets; i++) {
			int appWidgetId = appWidgetIds[i];

			long sleepDurationMillis = widgetSleepDurationMillis(context,
					appWidgetId);
			RemoteViews widget = new RemoteViews(context.getPackageName(),
					R.layout.appwidget);
			widget.setTextViewText(R.id.widgetDuration, String.format(
					context.getString(R.string.widget_duration_format),
					((float) sleepDurationMillis) / 1e3 / 60.0));

			appWidgetManager.updateAppWidget(appWidgetId, widget);
		}

		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	public void onDeleted(Context context, int[] appWidgetIds) {
		if (DEBUG)
			Log.v(LogTag, "onUpdate()");

		final int nWidgets = appWidgetIds.length;
		for (int i = 0; i < nWidgets; i++) {
			removeWidgetSleepDuration(context, appWidgetIds[i]);
		}
	}

	private String sleepDurationKey(int appWidgetId) {
		return String.format(SleepDurationMillisFormat, appWidgetId);
	}

	private void removeWidgetSleepDuration(Context context, int appWidgetId) {
		if (DEBUG)
			Log.v(LogTag, "removeWidgetSleepDuration()");
		SharedPreferences.Editor editor = context.getSharedPreferences(
				PrefsName, 0).edit();
		editor.remove(sleepDurationKey(appWidgetId));
		editor.commit();
	}

	private long widgetSleepDurationMillis(Context context, int appWidgetId) {
		if (DEBUG)
			Log.v(LogTag, "widgetSleepDurationMillis()");
		long sleepDurationMillis = -1;
		String prefsKey = sleepDurationKey(appWidgetId);

		SharedPreferences prefs = context.getSharedPreferences(PrefsName, 0);
		if (prefs.contains(prefsKey)) {
			sleepDurationMillis = prefs.getLong(sleepDurationKey(appWidgetId),
					-1);
		}

		if (sleepDurationMillis < 0) {
			sleepDurationMillis = currentSleepDurationMillis(context);
			SharedPreferences.Editor editor = context.getSharedPreferences(
					PrefsName, 0).edit();
			editor.putLong(prefsKey, sleepDurationMillis);
			editor.commit();
		}

		return sleepDurationMillis;
	}

	private long currentSleepDurationMillis(Context context) {
		if (DEBUG)
			Log.v(LogTag, "currentSleepDurationMillis()");
		SharedPreferences prefs = context.getSharedPreferences(SiestaWatchActivity.PrefsName, 0);
		if (prefs.contains(SiestaWatchActivity.SleepDurationMillis)) {
			return prefs.getLong(SiestaWatchActivity.SleepDurationMillis, 0);
		} else {
			return SiestaWatchActivity.defaultSleepDurationMillis;
		}
	}
}
