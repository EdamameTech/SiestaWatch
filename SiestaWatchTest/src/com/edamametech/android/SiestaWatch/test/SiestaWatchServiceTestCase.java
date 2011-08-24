package com.edamametech.android.SiestaWatch.test;

import com.edamametech.android.SiestaWatch.SiestaWatchService;
import com.edamametech.android.SiestaWatch.SiestaWatchService.State;

import android.content.Intent;
import android.test.ServiceTestCase;

public class SiestaWatchServiceTestCase extends
		ServiceTestCase<SiestaWatchService> {

	private SiestaWatchService mService;

	public SiestaWatchServiceTestCase() {
		super(SiestaWatchService.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Intent intent = new Intent();
		intent.setClass(getContext(), SiestaWatchService.class);
		startService(intent);
		mService = getService();
	}

	public void testDefaultState() {
		assertEquals(State.Off, mService.getState());
	}
}
