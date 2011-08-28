package com.edamametech.android.SiestaWatch.test;

import android.content.Intent;
import android.provider.Settings;
import android.test.ServiceTestCase;

import com.edamametech.android.SiestaWatch.SiestaWatchService;
import com.edamametech.android.SiestaWatch.SiestaWatchService.State;

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
		assertEquals(State.StandingBy, mService.getState());
	}

	public void testRestartWithNullIntentFromOff() {
		Intent intent = new Intent();
		intent.setClass(getContext(), SiestaWatchService.class);
		startService(intent);
		getService().stopSelf();
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
		startService(standardIntent);
		mService = getService();
		assertEquals(State.StandingBy, mService.getState());
	}

	public void testFromStandByToCountDown() {
		startService(standardIntent);
		mService = getService();
		mService.actionScreenOff();
		assertEquals(State.CountingDown, mService.getState());
	}

	public void testFromCountDowntoStandBy() {
		startService(standardIntent);
		mService = getService();
		mService.actionScreenOff();
		assertEquals(State.CountingDown, mService.getState());
		mService.actionUserPresent();
		assertEquals(State.StandingBy, mService.getState());
		}
}
