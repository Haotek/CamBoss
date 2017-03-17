package com.tutk.P2PCam264.DELUX;

import com.tutk.IOTC.AVAPIs;
import com.tutk.IOTC.IOTCAPIs;
import com.tutk.Kalay.general.R;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().hide();
		
		setContentView(R.layout.about_activity);
		
		TextView tvVersion = (TextView) findViewById(R.id.tvVersion);
		TextView tvIOTC = (TextView) findViewById(R.id.tvIOTC);
		TextView tvAV = (TextView) findViewById(R.id.tvAV);
		
		String version ="";
		try {
			version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		tvVersion.setText(version);
		tvIOTC.setText(getIOTCAPis());
		tvAV.setText(getAVAPis());
	}
	
	private String getIOTCAPis() {

		byte[] bytVer = new byte[4];
		int[] lVer = new int[1];
		int ver;

		IOTCAPIs.IOTC_Get_Version(lVer);

		ver = (int) lVer[0];

		StringBuffer sb = new StringBuffer();
		bytVer[3] = (byte) (ver);
		bytVer[2] = (byte) (ver >>> 8);
		bytVer[1] = (byte) (ver >>> 16);
		bytVer[0] = (byte) (ver >>> 24);
		sb.append((int) (bytVer[0] & 0xff));
		sb.append('.');
		sb.append((int) (bytVer[1] & 0xff));
		sb.append('.');
		sb.append((int) (bytVer[2] & 0xff));
		sb.append('.');
		sb.append((int) (bytVer[3] & 0xff));

		return sb.toString();
	}

	private String getAVAPis() {

		byte[] bytVer = new byte[4];
		int ver = AVAPIs.avGetAVApiVer();

		StringBuffer sb = new StringBuffer();
		bytVer[3] = (byte) (ver);
		bytVer[2] = (byte) (ver >>> 8);
		bytVer[1] = (byte) (ver >>> 16);
		bytVer[0] = (byte) (ver >>> 24);
		sb.append((int) (bytVer[0] & 0xff));
		sb.append('.');
		sb.append((int) (bytVer[1] & 0xff));
		sb.append('.');
		sb.append((int) (bytVer[2] & 0xff));
		sb.append('.');
		sb.append((int) (bytVer[3] & 0xff));

		return sb.toString();
	}

	@Override
	public boolean onKeyDown (int keyCode, KeyEvent event) {

		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				finish();
				overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
				return false;
		}

		return super.onKeyDown(keyCode, event);
	}
}
