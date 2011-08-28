package com.edamametech.android.SiestaWatch;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.content.Intent;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;

public class SiestaWatchActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		((Button) findViewById(R.id.startService))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View view) {
						startSiestaWatchService();
					}
				});
		((Button) findViewById(R.id.stopService))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View view) {
						stopSiestaWatchService();
					}
				});
	}

	private void startSiestaWatchService() {
		Intent intent = new Intent(SiestaWatchActivity.this,
				SiestaWatchService.class);
		intent.putExtra(SiestaWatchService.SleepDurationMillis, 5000L);
		intent.putExtra(SiestaWatchService.UriOfAlarmSound,
				Settings.System.DEFAULT_ALARM_ALERT_URI.toString());
		startService(intent);
	}

	private void stopSiestaWatchService() {
		Intent intent = new Intent(SiestaWatchActivity.this,
				SiestaWatchService.class);
		stopService(intent);
	}
}