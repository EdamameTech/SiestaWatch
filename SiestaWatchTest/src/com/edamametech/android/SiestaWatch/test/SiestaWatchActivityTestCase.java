
package com.edamametech.android.SiestaWatch.test;

import com.edamametech.android.SiestaWatch.SiestaWatchActivity;

import android.test.ActivityInstrumentationTestCase2;

public class SiestaWatchActivityTestCase extends
        ActivityInstrumentationTestCase2<SiestaWatchActivity> {

    SiestaWatchActivity mActivity;
    
    public SiestaWatchActivityTestCase() {
        super("com.edamametech.android.SiestaWatch", SiestaWatchActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        mActivity = getActivity();
    }

    public void testPreConditions() {
        assertTrue(mActivity != null);
    }
}
