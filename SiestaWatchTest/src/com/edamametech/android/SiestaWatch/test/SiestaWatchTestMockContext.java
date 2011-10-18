
package com.edamametech.android.SiestaWatch.test;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.mock.MockContext;

public class SiestaWatchTestMockContext extends MockContext {
    private static final String PREFIX = "SiestaWatchTestMockContext.";

    Context mTestContext;
    
    public SiestaWatchTestMockContext(Context context) {
        mTestContext = context;
    }

    public SharedPreferences getSharedPreferences(String name, int mode) {
        return mTestContext.getSharedPreferences(PREFIX + name, mode);
    }
}
