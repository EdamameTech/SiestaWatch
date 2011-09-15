package com.edamametech.android.SiestaWatch;

import java.io.IOException;
import java.util.Hashtable;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class SiestaWatchService extends Service {
	private static final int LOGLEVEL = 1;
	private static final boolean DEBUG = (LOGLEVEL > 0);
	private static final String LogTag = "SiestaWatchService";
	private static final String PrefsName = "SiestaWatchService";

	private MediaPlayer alarmPlayer = null;

	/* state */
	public static final String State = "State";
	public static final int StateOff = 1;
	// Application has not been executed
	public static final int StateStandingBy = 2;
	// Waiting for the user to fall asleep
	public static final int StateCountingDown = 3;
	// Counting down to raise alarm
	public static final int StateAlarming = 4;
	// Waking up the user
	public static final int StateSilencing = 5;
	// Waiting for the user to fall asleep
	public static final int StateTimeLimit = 6;
	// Reached absolute time limit

	private int state = StateOff;

	// public for SiestaWatchServiceTestCases
	public int getState() {
		return state;
	}

	private void standBy() {
		if (DEBUG)
			Log.v(LogTag, "standBy()");
		clearAlarm();
		state = StateStandingBy;
		storeParameters();
	}

	private void countDown() {
		if (DEBUG)
			Log.v(LogTag, "countDown()");
		setAlarm();
		state = StateCountingDown;
		storeParameters();
	}

	private void timeLimit() {
		if (DEBUG)
			Log.v(LogTag, "timeLimit()");
		playAlarm();
		state = StateTimeLimit;
		storeParameters();
	}

	private void alarm() {
		if (DEBUG)
			Log.v(LogTag, "alarm()");
		playAlarm();
		state = StateAlarming;
		storeParameters();
	}

	private void playAlarm() {
		if (DEBUG)
			Log.v(LogTag, "playAlarm()");
		clearAlarm();
		if (alarmPlayer == null) {
			alarmPlayer = new MediaPlayer();
			try {
				alarmPlayer.setDataSource(this, uriOfAlarmSound);
				// TODO: Better handling of exceptions
			} catch (IllegalArgumentException e) {
				Log.e(LogTag, e.toString());
			} catch (SecurityException e) {
				Log.e(LogTag, e.toString());
			} catch (IllegalStateException e) {
				Log.e(LogTag, e.toString());
			} catch (IOException e) {
				Log.e(LogTag, e.toString());
			}
			alarmPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
			alarmPlayer.setLooping(true);
			try {
				alarmPlayer.prepare();
				// TODO: Better handling of exceptions
			} catch (IllegalStateException e) {
				Log.e(LogTag, e.toString());
			} catch (IOException e) {
				Log.e(LogTag, e.toString());
			}
		}
		alarmPlayer.seekTo(0);
		alarmPlayer.start();
	}

	private void silent() {
		if (DEBUG)
			Log.v(LogTag, "silent()");
		clearAlarm();
		if (alarmPlayer != null)
			alarmPlayer.pause();
		state = StateSilencing;
		storeParameters();
	}

	private void off() {
		if (DEBUG)
			Log.v(LogTag, "off()");
		clearAlarm();
		clearTimeLimit();
		if (alarmPlayer != null) {
			alarmPlayer.stop();
			alarmPlayer.release();
		}
		state = StateOff;
		storeParameters();
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
			if (action.equals(Intent.ACTION_USER_PRESENT)) {
				actionUserPresent();
			}
		}
	};

	private static final IntentFilter screenEventFilter = new IntentFilter();

	/* following methods are public for SiestaWatchServiceTestCases */
	public void actionScreenOff() {
		if (DEBUG)
			Log.v(LogTag, "actionScreenOff()");
		if (state == StateStandingBy) {
			countDown();
			return;
		}
		if (state == StateSilencing) {
			alarm();
			return;
		}
		if (state == StateTimeLimit) {
			off();
			return;
		}
	}

	public void actionUserPresent() {
		if (DEBUG)
			Log.v(LogTag, "actionUserPresent()");
		if (state == StateCountingDown) {
			standBy();
			return;
		}
		if (state == StateAlarming) {
			off();
			return;
		}
		if (state == StateSilencing) {
			off();
			return;
		}
	}

	public void actionAlarm() {
		if (DEBUG)
			Log.v(LogTag, "actionAlarm()");
		if (state == StateCountingDown) {
			alarm();
			return;
		}
	}

	public void actionTimeLimit() {
		if (DEBUG)
			Log.v(LogTag, "actionTimeLimit()");
		if (state == StateStandingBy) {
			timeLimit();
			return;
		}
		if (state == StateCountingDown) {
			alarm();
			return;
		}
	}

	public void actionScreenOn() {
		if (DEBUG)
			Log.v(LogTag, "actionScreenOn()");
		if (state == StateAlarming) {
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

	// Key for Extras in Intent to supply absolute TimeLimit as a long
	public static final String TimeLimitMillis = "TimeLimitMillis";
	// Absolute Time in msec to alarm user about the time limit
	private long timeLimitMillis = 0;

	private void storeParameters() {
		if (DEBUG)
			Log.v(LogTag, "storeParameters()");
		SharedPreferences.Editor editor = getSharedPreferences(PrefsName, 0)
				.edit();
		editor.putString(UriOfAlarmSound, uriOfAlarmSound.toString());
		editor.putLong(SleepDurationMillis, sleepDurationMillis);
		editor.putLong(TimeLimitMillis, timeLimitMillis);
		editor.putInt(State, state);
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
		if (prefs.contains(TimeLimitMillis)) {
			timeLimitMillis = prefs.getLong(TimeLimitMillis, 0);
			setTimeLimit();
		}
		if (prefs.contains(State)) {
			state = prefs.getInt(State, 0);
		}
	}

	/* alarms */
	// Key for Extras in Intent to supply Action as an Int
	public static final String Action = "Action";
	// Intent that makes us to go off the alarm
	private static final int ActionAlarm = 1;
	private static final int ActionTimeLimit = 2;

	private AlarmManager alarmManager = null;
	private Hashtable<Integer, PendingIntent> alarmSenders = new Hashtable<Integer, PendingIntent>();

	private void setAlarm() {
		if (DEBUG)
			Log.v(LogTag, "setAlarm()");
		setAlarmWithAction(ActionAlarm, System.currentTimeMillis()
				+ sleepDurationMillis);
	}

	private void setTimeLimit() {
		if (DEBUG)
			Log.v(LogTag, "setTimeLimit()");
		if (timeLimitMillis > System.currentTimeMillis()) {
			setAlarmWithAction(ActionTimeLimit, timeLimitMillis);
		}
	}

	private void setAlarmWithAction(Integer action, long alarmTime) {
		if (DEBUG)
			Log.v(LogTag, "setAlarmWithAction()");

		Intent intent = new Intent();
		intent.setClass(this, SiestaWatchService.class);
		intent.putExtra(SiestaWatchService.Action, action);

		if (alarmSenders.get(action) == null) {
			alarmSenders.put(action,
					PendingIntent.getService(this, 0, intent, 0));
		}
		if (alarmManager == null) {
			alarmManager = (AlarmManager) this
					.getSystemService(Context.ALARM_SERVICE);
		}
		alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime,
				alarmSenders.get(action));
		if (DEBUG)
			Log.v(LogTag, "Alarm is set at " + alarmTime + " for action "
					+ action);
	}

	private void clearAlarm() {
		if (DEBUG)
			Log.v(LogTag, "clearAlarm()");
		clearAlarmWithAction(ActionAlarm);
	}

	private void clearTimeLimit() {
		if (DEBUG)
			Log.v(LogTag, "clearTimeLimit()");
		clearAlarmWithAction(ActionTimeLimit);
	}

	private void clearAlarmWithAction(Integer action) {
		if (DEBUG)
			Log.v(LogTag, "clearAlarmWithAction()");

		if (alarmManager == null) {
			alarmManager = (AlarmManager) this
					.getSystemService(Context.ALARM_SERVICE);
		}
		alarmManager.cancel(alarmSenders.get(action));
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
					case ActionTimeLimit:
						actionTimeLimit();
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
					if (extras.containsKey(TimeLimitMillis)) {
						if (DEBUG)
							Log.v(LogTag,
									TimeLimitMillis + ": "
											+ extras.getLong(TimeLimitMillis));
						timeLimitMillis = extras.getLong(TimeLimitMillis);
					}
					storeParameters();
					if (uriOfAlarmSound != null && sleepDurationMillis > 0) {
						setTimeLimit();
						standBy();
					}
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
