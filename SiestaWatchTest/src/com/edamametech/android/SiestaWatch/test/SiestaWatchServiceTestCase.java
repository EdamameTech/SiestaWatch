package com.edamametech.android.SiestaWatch.test;

import com.edamametech.android.SiestaWatch.SiestaWatchService;
import com.edamametech.android.SiestaWatch.SiestaWatchService.State;

import android.content.Intent;
import android.test.ServiceTestCase;
import android.provider.Settings;

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
		intent.putExtra(SiestaWatchService.UriOfAlarmSound, Settings.System.DEFAULT_ALARM_ALERT_URI.toString());
		startService(intent);
		mService = getService();
		assertEquals(State.StandingBy, mService.getState());
	}
}
