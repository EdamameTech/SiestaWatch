
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
    }

    private void startTestActivity() {
        mActivity = getActivity();
        mContext = new SiestaWatchTestMockContext(mActivity.getBaseContext());
        mInstrumentation = this.getInstrumentation();
    }

    public void testPreConditions() {
        startTestActivity();
        assertTrue(mActivity != null);
    }

    @UiThreadTest
    public void testPersistentNeedsVibration() {
        startTestActivity();
        CheckBox vibrationCheckBox = (CheckBox) mActivity.findViewById(R.id.vibrateCheckBox);
        vibrationCheckBox.setChecked(true);
        assertEquals(true, SiestaWatchConf.needsVibration(mContext));
        mActivity.finish();
        assertEquals(true, SiestaWatchConf.needsVibration(mContext));

        startTestActivity();
        vibrationCheckBox = (CheckBox) mActivity.findViewById(R.id.vibrateCheckBox);
        assertEquals(true, SiestaWatchConf.needsVibration(mContext));
        assertEquals(true, vibrationCheckBox.isChecked());
        vibrationCheckBox.setChecked(false);
        mActivity.finish();

        assertEquals(false, SiestaWatchConf.needsVibration(mContext));

        startTestActivity();
        SiestaWatchConf.setNeedsVibration(mContext, false);
        mActivity.finish();

        startTestActivity();
        vibrationCheckBox = (CheckBox) mActivity.findViewById(R.id.vibrateCheckBox);
        assertEquals(false, vibrationCheckBox.isChecked());
        mActivity.finish();
    }

    // TODO: add test for needsTimeLimit
    // TODO: also test needsTimeLimit with different relation between TimeLimit
    // and currentTime
}
