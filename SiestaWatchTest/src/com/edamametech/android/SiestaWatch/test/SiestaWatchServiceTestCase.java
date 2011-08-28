package com.edamametech.android.SiestaWatch.test;

import android.content.Intent;
import android.provider.Settings;
import android.test.ServiceTestCase;

import com.edamametech.android.SiestaWatch.SiestaWatchService;
import com.edamametech.android.SiestaWatch.SiestaWatchService.State;

public class SiestaWatchServiceTestCase extends
		ServiceTestCase<SiestaWatchService> {

	private SiestaWatchService mService;

	public SiestaWatchServiceTestCase() {
		super(SiestaWatchService.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testRestartWithNullIntentFromStandBy() {
		/* pretending the normal Service is stopped and ... */
		Intent intent = new Intent();
		intent.setClass(getContext(), SiestaWatchService.class);
		intent.putExtra(SiestaWatchService.SleepDurationMillis, 1000L);
		intent.putExtra(SiestaWatchService.UriOfAlarmSound,
				Settings.System.DEFAULT_ALARM_ALERT_URI.toString());
		startService(intent);
		mService = getService();
		mService.stopSelf();
		/* restarted by the ActivityManager, intent seems to be null, */
		startService(null);
		mService = getService();
		/* and we want the Service to keep the parameters */
		assertEquals(State.StandingBy, mService.getState());
	}


	public void testRestartWithNullIntentFromOff() {
		Intent intent = new Intent();
		intent.setClass(getContext(), SiestaWatchService.class);
		startService(intent);
		mService = getService();
		assertEquals(State.Off, mService.getState());
		mService.stopSelf();
		startService(null);
		mService = getService();
		assertEquals(State.Off, mService.getState());
	}

	public void testDefaultState() {
		Intent intent = new Intent();
		intent.setClass(getContext(), SiestaWatchService.class);
		startService(intent);
		mService = getService();
		assertEquals(State.Off, mService.getState());
	}

	public void testStandBy() {
		Intent intent = new Intent();
		intent.setClass(getContext(), SiestaWatchService.class);
		intent.putExtra(SiestaWatchService.SleepDurationMillis, 1000L);
		intent.putExtra(SiestaWatchService.UriOfAlarmSound,
				Settings.System.DEFAULT_ALARM_ALERT_URI.toString());
		startService(intent);
		mService = getService();
		assertEquals(State.StandingBy, mService.getState());
	}
}
