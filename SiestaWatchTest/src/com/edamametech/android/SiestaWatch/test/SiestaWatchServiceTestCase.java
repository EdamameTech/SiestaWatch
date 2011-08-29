package com.edamametech.android.SiestaWatch.test;

import android.content.Intent;
import android.provider.Settings;
import android.test.ServiceTestCase;

import com.edamametech.android.SiestaWatch.SiestaWatchService;

public class SiestaWatchServiceTestCase extends
		ServiceTestCase<SiestaWatchService> {

	private SiestaWatchService mService;
	private Intent standardIntent;

	public SiestaWatchServiceTestCase() {
		super(SiestaWatchService.class);
	}

	@Override
	protected void setUp() {
		standardIntent = new Intent();
		standardIntent.setClass(getContext(), SiestaWatchService.class);
		standardIntent.putExtra(SiestaWatchService.SleepDurationMillis, 1000L);
		standardIntent.putExtra(SiestaWatchService.UriOfAlarmSound,
				Settings.System.DEFAULT_ALARM_ALERT_URI.toString());
	}

	public void testRestartWithNullIntentFromStandBy() {
		/* pretending the normal Service is stopped and ... */
		startService(standardIntent);
		mService = getService();
		mService.stopSelf();
		/* restarted by the ActivityManager, intent seems to be null, */
		startService(null);
		mService = getService();
		/* and we want the Service to keep the parameters */
		assertEquals(SiestaWatchService.StateStandingBy, mService.getState());
	}

	public void testRestartWithNullIntentFromOff() {
		Intent intent = new Intent();
		intent.setClass(getContext(), SiestaWatchService.class);
		startService(intent);
		getService().stopSelf();
		startService(null);
		mService = getService();
		assertEquals(SiestaWatchService.StateOff, mService.getState());
	}

	public void testDefaultState() {
		Intent intent = new Intent();
		intent.setClass(getContext(), SiestaWatchService.class);
		startService(intent);
		mService = getService();
		assertEquals(SiestaWatchService.StateOff, mService.getState());
	}

	public void testStandBy() {
		startService(standardIntent);
		mService = getService();
		assertEquals(SiestaWatchService.StateStandingBy, mService.getState());
	}

	public void testFromStandByToCountDown() {
		startService(standardIntent);
		mService = getService();
		mService.actionScreenOff();
		assertEquals(SiestaWatchService.StateCountingDown, mService.getState());
	}

	public void testFromCountDownToStandBy() {
		startService(standardIntent);
		mService = getService();
		mService.actionScreenOff();
		mService.actionUserPresent();
		assertEquals(SiestaWatchService.StateStandingBy, mService.getState());
	}

	public void testFromCountDownToAlarm() {
		startService(standardIntent);
		mService = getService();
		mService.actionScreenOff();
		mService.actionAlarm();
		assertEquals(SiestaWatchService.StateAlarming, mService.getState());
	}

	public void testFromAlarmToSilent() {
		startService(standardIntent);
		mService = getService();
		mService.actionScreenOff();
		mService.actionAlarm();
		mService.actionScreenOn();
		assertEquals(SiestaWatchService.StateSilencing, mService.getState());
	}

	public void testFromAlarmToOff() {
		startService(standardIntent);
		mService = getService();
		mService.actionScreenOff();
		mService.actionAlarm();
		mService.actionUserPresent();
		assertEquals(SiestaWatchService.StateOff, mService.getState());
	}

	public void testFromSilentToAlarm() {
		startService(standardIntent);
		mService = getService();
		mService.actionScreenOff();
		mService.actionAlarm();
		mService.actionScreenOn();
		mService.actionScreenOff();
		assertEquals(SiestaWatchService.StateAlarming, mService.getState());
	}

	public void testFromSilentToOff() {
		startService(standardIntent);
		mService = getService();
		mService.actionScreenOff();
		mService.actionAlarm();
		mService.actionScreenOn();
		mService.actionUserPresent();
		assertEquals(SiestaWatchService.StateOff, mService.getState());
	}
}
