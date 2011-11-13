
package com.edamametech.android.SiestaWatch.test;

import com.edamametech.android.SiestaWatch.R;
import com.edamametech.android.SiestaWatch.SiestaWatchActivity;
import com.edamametech.android.SiestaWatch.SiestaWatchConf;

import android.app.Instrumentation;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.TimeZone;

public class SiestaWatchActivityTestCase extends
        ActivityInstrumentationTestCase2<SiestaWatchActivity> {

    SiestaWatchActivity mActivity;
    Context mContext;
    Instrumentation mInstrumentation;
    EditText mDurationField;
    Button mDoneButton;

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
        mContext = mActivity.getBaseContext();
        mInstrumentation = this.getInstrumentation();

        mDurationField = (EditText) mActivity.findViewById(R.id.sleepDurationInMins);
        mDoneButton = (Button) mActivity.findViewById(R.id.done);
    }

    public void testPreConditions() {
        startTestActivity();
        assertTrue(mActivity != null);
        assertTrue(mContext != null);

        assertTrue(mDurationField != null);
        mActivity.finish();
    }

    @UiThreadTest
    public void testSleepDurationDisplay() {
        long sleepDurationMinutes = 15;
        startTestActivity();
        SiestaWatchConf.setSleepDuration(mContext, sleepDurationMinutes * 60 * 1000);
        mActivity.finish();

        startTestActivity();
        assertEquals("15", mDurationField.getText().toString());
    }

    @UiThreadTest
    public void testSleepDurationUpdateFromDisplay() {
        long sleepDurationMinutes = 25;
        startTestActivity();
        mDurationField.setText(String.valueOf(sleepDurationMinutes));
        mDoneButton.performClick();
        assertEquals(sleepDurationMinutes * 60 * 1000, SiestaWatchConf.sleepDuration(mContext));
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

    public void testGuessTimeLimitTrue() {
        /* 1319585977000L is 2011-10-25 13:39:37 -1000 */
        assertTrue(SiestaWatchActivity.guessTimeLimitNeeded(1319585977000L, 14, 30,
                TimeZone.getTimeZone("Pacific/Honolulu")));
    }

    public void testGuessTimeLimitFalse() {
        assertFalse(SiestaWatchActivity.guessTimeLimitNeeded(1319585977000L, 13, 30,
                TimeZone.getTimeZone("Pacific/Honolulu")));
    }
}
