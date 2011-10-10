package com.edamametech.android.SiestaWatch;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

public class SiestaWatchAppWidget extends AppWidgetProvider {
	private static final int LOGLEVEL = 1;
	private static final boolean DEBUG = (LOGLEVEL > 0);
	private static final String LogTag = "SiestaWatchAppWidget";
	private static final String PrefsName = "SiestaWatchAppWidget";

	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		if (DEBUG)
			Log.v(LogTag, "onUpdate()");

		final int nWidgets = appWidgetIds.length;
		for (int i = 0; i < nWidgets; i++) {
			int appWidgetId = appWidgetIds[i];

			RemoteViews widget = new RemoteViews(context.getPackageName(),
					R.layout.appwidget);
			widget.setTextViewText(R.id.widgetDuration, String.format(
					context.getString(R.string.widget_duration_format), 999.0));

			appWidgetManager.updateAppWidget(appWidgetId, widget);
		}

		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
}
