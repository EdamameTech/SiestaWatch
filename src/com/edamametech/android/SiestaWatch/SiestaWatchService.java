package com.edamametech.android.SiestaWatch;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class SiestaWatchService extends Service {
	private static final int LOGLEVEL = 1;
	private static final boolean DEBUG = (LOGLEVEL > 0);
	private static final String LogTag = "SiestaWatchService";
	private static final String PrefsName = "SiestaWatchService";

	/* state */
	public static enum State {
		Off, // Application has not been executed
		StandingBy, // Waiting for the user to fall asleep
		CountingDown, // Counting down to raise alarm
		Alarming, // Waking up the user
		Silencing, // Pausing the alarm
	};

	private State state = State.Off;

	// public for SiestaWatchServiceTestCases
	public State getState() {
		return state;
	}

	private void standBy() {
		if (DEBUG)
			Log.v(LogTag, "standBy()");
		state = State.StandingBy;
	}

	private void countDown() {
		if (DEBUG)
			Log.v(LogTag, "countDown()");
		setAlarm();
		state = State.CountingDown;
	}

	private void alarm() {
		if (DEBUG)
			Log.v(LogTag, "alarm()");
		clearAlarm();
		state = State.Alarming;
	}

	private void silent() {
		if (DEBUG)
			Log.v(LogTag, "silent()");
		clearAlarm();
		state = State.Silencing;
	}

	private void off() {
		if (DEBUG)
			Log.v(LogTag, "off()");
		clearAlarm();
		state = State.Off;
		stopSelf();
	}

	/* receiving Broadcasts */
	private final BroadcastReceiver screenEventReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent broadcast) {
			if (DEBUG)
				Log.v(LogTag, "screenEventReceiver.onReceive()");
			if (DEBUG)
				Log.v(LogTag, broadcast.toString());
			String action = broadcast.getAction();
			if (action.equals(Intent.ACTION_SCREEN_OFF)) {
				actionScreenOff();
			}
			if (action.equals(Intent.ACTION_SCREEN_ON)) {
				actionScreenOn();
			}
		}
	};

	private static final IntentFilter screenEventFilter = new IntentFilter();

	/* following methods are public for SiestaWatchServiceTestCases */
	public void actionScreenOff() {
		if (DEBUG)
			Log.v(LogTag, "actionScreenOff()");
		if (state == State.StandingBy) {
			countDown();
			return;
		}
		if (state == State.Silencing){
			alarm();
			return;
		}
	}

	public void actionUserPresent() {
		if (DEBUG)
			Log.v(LogTag, "actionUserPresent()");
		if (state == State.CountingDown) {
			standBy();
			return;
		}
		if (state == State.Alarming) {
			off();
		}
	}

	public void actionAlarm() {
		if (DEBUG)
			Log.v(LogTag, "actionAlarm()");
		if (state == State.CountingDown) {
			alarm();
			return;
		}
	}

	public void actionScreenOn() {
		if (DEBUG)
			Log.v(LogTag, "actionScreenOn()");
		if (state == State.Alarming) {
			silent();
			return;
		}
	}

	/* parameters */
	// Key for Extras in Intent to supply uriOfAlarmSound as a String
	public static final String UriOfAlarmSound = "UriOfAlarmSound";
	// URI of the alarm sound
	private Uri uriOfAlarmSound = null;

	// Key for Extras in Intent to supply sleepDurationMIllis as a long
	public static final String SleepDurationMillis = "SleepDurationMillis";
	// Time duration in msec to alarm after user felt asleep
	private long sleepDurationMillis = 0;

	private void storeParameters() {
		if (DEBUG)
			Log.v(LogTag, "storeParameters()");
		SharedPreferences.Editor editor = getSharedPreferences(PrefsName, 0)
				.edit();
		editor.putString(UriOfAlarmSound, uriOfAlarmSound.toString());
		editor.putLong(SleepDurationMillis, sleepDurationMillis);
		editor.commit();
	}

	private void clearStoredParameters() {
		if (DEBUG)
			Log.v(LogTag, "storeParameters()");
		SharedPreferences.Editor editor = getSharedPreferences(PrefsName, 0)
				.edit();
		editor.clear();
		editor.commit();
	}

	private void restoreParameters() {
		if (DEBUG)
			Log.v(LogTag, "restoreParameters()");
		SharedPreferences prefs = getSharedPreferences(PrefsName, 0);
		if (prefs.contains(UriOfAlarmSound)) {
			uriOfAlarmSound = Uri.parse(prefs.getString(UriOfAlarmSound, ""));
		}
		if (prefs.contains(SleepDurationMillis)) {
			sleepDurationMillis = prefs.getLong(SleepDurationMillis, 0);
		}
	}

	/* alarms */
	// Key for Extras in Intent to supply Action as an Int
	public static final String Action = "Action";
	// Intent that makes us to go off the alarm
	private static final int ActionAlarm = 1;

	private AlarmManager alarmManager = null;
	private Intent alarmIntent = null;
	private PendingIntent alarmSender = null;

	private void setAlarm() {
		if (DEBUG)
			Log.v(LogTag, "setAlarm()");

		if (alarmIntent == null) {
			alarmIntent = new Intent();
			alarmIntent.setClass(this, SiestaWatchService.class);
			alarmIntent.putExtra(SiestaWatchService.Action, ActionAlarm);
		}
		if (alarmSender == null) {
			alarmSender = PendingIntent.getService(this, 0, alarmIntent, 0);
		}
		if (alarmManager == null) {
			alarmManager = (AlarmManager) this
					.getSystemService(Context.ALARM_SERVICE);
		}

		alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
				+ sleepDurationMillis, alarmSender);
	}

	private void clearAlarm() {
		if (DEBUG)
			Log.v(LogTag, "clearAlarm()");

		if (alarmManager != null) {
			alarmManager.cancel(alarmSender);
			alarmManager = null;
		}
	}

	/* Service things */
	@Override
	public IBinder onBind(Intent arg0) {
		// Will not be bound
		return null;
	}

	@Override
	public void onCreate() {
		if (DEBUG)
			Log.v(LogTag, "onCreate()");
		screenEventFilter.addAction(Intent.ACTION_SCREEN_OFF);
		screenEventFilter.addAction(Intent.ACTION_SCREEN_ON);
		screenEventFilter.addAction(Intent.ACTION_USER_PRESENT);
		registerReceiver(screenEventReceiver, screenEventFilter);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		handleStartCommand(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleStartCommand(intent);
		return START_STICKY;
	}

	private void handleStartCommand(Intent intent) {
		if (DEBUG)
			Log.v(LogTag, "handleStartCommand()");

		if (intent == null) {
			if (DEBUG)
				Log.v(LogTag, "Got a null intent");
			restoreParameters();
		} else {
			if (DEBUG)
				Log.v(LogTag, intent.toString());

			Bundle extras = intent.getExtras();
			if (extras == null) {
				clearStoredParameters();
			} else {
				if (extras.containsKey(Action)) {
					if (DEBUG)
						Log.v(LogTag, Action + ": " + extras.getInt(Action));
					switch (extras.getInt(Action)) {
					case ActionAlarm:
						actionAlarm();
						break;
					}
				} else {
					if (extras.containsKey(UriOfAlarmSound)) {
						if (DEBUG)
							Log.v(LogTag,
									UriOfAlarmSound + ": "
											+ extras.getString(UriOfAlarmSound));
						uriOfAlarmSound = Uri.parse(extras
								.getString(UriOfAlarmSound));
					}
					if (extras.containsKey(SleepDurationMillis)) {
						if (DEBUG)
							Log.v(LogTag,
									SleepDurationMillis
											+ ": "
											+ extras.getLong(SleepDurationMillis));
						sleepDurationMillis = extras
								.getLong(SleepDurationMillis);
					}
					storeParameters();
					if (uriOfAlarmSound != null && sleepDurationMillis > 0)
						standBy();
				}
			}
		}
	}

	@Override
	public void onDestroy() {
		if (DEBUG)
			Log.v(LogTag, "onDestroy()");
		unregisterReceiver(screenEventReceiver);
	}
}
