package com.tutk.P2PCam264;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Window;
import com.tutk.Kalay.general.R;
import com.tutk.P2PCam264.DELUX.MultiViewActivity;

public class SplashScreenActivity extends SherlockActivity {

	private static final int SPLASH_DISPLAY_TIME = 2000; /* 2 seconds */
	private static boolean isFirstLaunch = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.splash);

//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

		// VMRuntime.getRuntime().setMinimumHeapSize(CWJ_HEAP_SIZE);

		String versionName = "";
		try {
			versionName = this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
		}

//		Splash splash = (Splash) findViewById(R.id.splash);
		TextView tvVersion = (TextView) findViewById(R.id.tvVersion);

//			splash.setVersion(versionName);
			tvVersion.setText(versionName);
			if (!isFirstLaunch) {
//				splash.setVisibility(View.INVISIBLE);
			} else {
				// Clean connection leaved by crash last time
				SharedPreferences settings = this.getSharedPreferences("ProcessList", 0);
				if (settings != null) {

					int killpid = settings.getInt("pid", 0);
					android.os.Process.killProcess(killpid);

					int pid = android.os.Process.myPid();
					SharedPreferences.Editor PE = settings.edit();
					PE.putInt("pid", pid);
					PE.commit();
				}

				// End of clean
			}
		

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {

				Intent mainIntent = new Intent(SplashScreenActivity.this, MultiViewActivity.class);
				SplashScreenActivity.this.startActivity(mainIntent);
				SplashScreenActivity.this.finish();
				overridePendingTransition(R.anim.mainfadein, R.anim.splashfadeout);
			}
		}, isFirstLaunch ? SPLASH_DISPLAY_TIME : 500);

		isFirstLaunch = false;
	}

	@Override
	protected void onStart() {
		super.onStart();
		// FlurryAgent.onStartSession(this, "Q1SDXDZQ21BQMVUVJ16W");
	}

	@Override
	protected void onStop() {
		super.onStop();
		// FlurryAgent.onEndSession(this);
	}
}