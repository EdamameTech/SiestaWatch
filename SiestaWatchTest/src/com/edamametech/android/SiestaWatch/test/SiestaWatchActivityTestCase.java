
package com.edamametech.android.SiestaWatch.test;

import com.edamametech.android.SiestaWatch.R;
import com.edamametech.android.SiestaWatch.SiestaWatchActivity;
import com.edamametech.android.SiestaWatch.SiestaWatchConf;

import android.app.Instrumentation;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.widget.CheckBox;

public class SiestaWatchActivityTestCase extends
        ActivityInstrumentationTestCase2<SiestaWatchActivity> {

    SiestaWatchActivity mActivity;
    Context mContext;
    Instrumentation mInstrumentation;

    public SiestaWatchActivityTestCase() {
        super("com.edamametech.android.SiestaWatch", SiestaWatchActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        mActivity = getActivity();
        mContext = new SiestaWatchTestMockContext(mActivity.getBaseContext());
        mInstrumentation = this.getInstrumentation();
    }

    public void testPreConditions() {
        assertTrue(mActivity != null);
    }

    @UiThreadTest
    public void testRestoreNeedsVibration() {
        CheckBox vibrationCheckBox = (CheckBox) mActivity.findViewById(R.id.vibrateCheckBox);

        vibrationCheckBox.setChecked(true);
        mInstrumentation.callActivityOnPause(mActivity);
        assertEquals(true, SiestaWatchConf.needsVibration(mContext));
        vibrationCheckBox.setChecked(false);
        mInstrumentation.callActivityOnResume(mActivity);
        assertEquals(true, vibrationCheckBox.isChecked());

        vibrationCheckBox.setChecked(false);
        mInstrumentation.callActivityOnPause(mActivity);
        assertEquals(false, SiestaWatchConf.needsVibration(mContext));
        vibrationCheckBox.setChecked(true);
        mInstrumentation.callActivityOnResume(mActivity);
        assertEquals(false, vibrationCheckBox.isChecked());
    }
}
