package com.tutk.Kalay.settings;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.P2PCam264.DeviceInfo;
import com.tutk.P2PCam264.MyCamera;

import com.tutk.P2PCam264.DELUX.MultiViewActivity;
import com.tutk.Kalay.general.R;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

public class EventSettingsActivity extends Activity {

	private final int SENS_LOW = 0;
	private final int SENS_MEDIUM = 1;
	private final int SENS_HIGH = 2;
	private final int SENS_MAX = 3;
	private final int PRESET_OFF = 0;
	private final int PRESET_POS_1 = 1;
	private final int PRESET_POS_2 = 2;
	private final int PRESET_POS_3 = 3;
	private final int PRESET_POS_4 = 4;
	private final int INTERVAL_NO_LIMIT = 0;
	private final int INTERVAL_1_MIN = 1;
	private final int INTERVAL_3_MIN = 2;
	private final int INTERVAL_5_MIN = 3;
	private final int INTERVAL_10_MIN = 4;
	private final int INTERVAL_30_MIN = 5;
	private final int INTERVAL_OFF = 6;

	private MyCamera mCamera;
	private DeviceInfo mDevice;

	private LinearLayout layoutSens;
	private LinearLayout layoutGarbage;
	private Switch swMain;
	private Switch swMail;
	private Spinner spSens;
	private Spinner spPreset;
	private Spinner spInterval;

	private boolean mIsMajor = true;
	private byte alarm_motion_armed = -1;
	private byte alarm_motion_sensitivity = -1;
	private byte alarm_preset = -1;
	private byte alarm_mail = -1;
	private int alarm_upload_interval = -1;
	private int mSensitivity = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.titlebar);
		TextView tv = (TextView) this.findViewById(R.id.bar_text);
		tv.setText(getText(R.string.txtEventSetting));

		setContentView(R.layout.event_settings);

		Bundle bundle = getIntent().getExtras();
		String devUUID = bundle.getString("dev_uuid");
		String devUID = bundle.getString("dev_uid");
		mIsMajor = bundle.getBoolean("major");
		mSensitivity = bundle.getInt("sens");
		alarm_motion_armed = bundle.getByte("main");
		alarm_motion_sensitivity = bundle.getByte("g_sens");
		alarm_preset = bundle.getByte("preset");
		alarm_mail = bundle.getByte("mail");
		alarm_upload_interval = bundle.getInt("ftp");

		for (MyCamera camera : MultiViewActivity.CameraList) {

			if (devUUID.equalsIgnoreCase(camera.getUUID()) && devUID.equalsIgnoreCase(camera.getUID())) {
				mCamera = camera;
				break;
			}
		}

		for (DeviceInfo dev : MultiViewActivity.DeviceList) {

			if (devUUID.equalsIgnoreCase(dev.UUID) && devUID.equalsIgnoreCase(dev.UID)) {
				mDevice = dev;
				break;
			}
		}

		layoutSens = (LinearLayout) findViewById(R.id.layoutSens);
		layoutGarbage = (LinearLayout) findViewById(R.id.layoutGarbage);
		swMain = (Switch) findViewById(R.id.swMain);
		swMail = (Switch) findViewById(R.id.swMail);
		spSens = (Spinner) findViewById(R.id.spSens);
		spPreset = (Spinner) findViewById(R.id.spPreset);
		spInterval = (Spinner) findViewById(R.id.spInterval);

		ArrayAdapter<CharSequence> adapterSens = ArrayAdapter.createFromResource(this, R.array.motion_detection_ignore, R.layout.myspinner_title);
		ArrayAdapter<CharSequence> adapterPreset = ArrayAdapter.createFromResource(this, R.array.event_preset, R.layout.myspinner_title);
		ArrayAdapter<CharSequence> adapterInterval = ArrayAdapter.createFromResource(this, R.array.event_interval, R.layout.myspinner_title);
		adapterSens.setDropDownViewResource(R.layout.myspinner);
		adapterPreset.setDropDownViewResource(R.layout.myspinner);
		adapterInterval.setDropDownViewResource(R.layout.myspinner);

		if (mIsMajor) {
			spSens.setAdapter(adapterSens);
			if (mSensitivity > 0) {
				swMain.setChecked(true);
				layoutSens.setVisibility(View.VISIBLE);
				if (mSensitivity <= 35) {
					spSens.setSelection(SENS_LOW);
				} else if (mSensitivity > 35 && mSensitivity <= 65) {
					spSens.setSelection(SENS_MEDIUM);
				} else if (mSensitivity > 65 && mSensitivity <= 95) {
					spSens.setSelection(SENS_HIGH);
				} else if (mSensitivity > 95) {
					spSens.setSelection(SENS_MAX);
				}
			}
		} else {
			layoutGarbage.setVisibility(View.VISIBLE);
			spSens.setAdapter(adapterSens);
			spPreset.setAdapter(adapterPreset);
			spInterval.setAdapter(adapterInterval);
			if (alarm_motion_armed == 1) {
				swMain.setChecked(true);
				layoutSens.setVisibility(View.VISIBLE);
			}

			if (alarm_motion_sensitivity > 0 && alarm_motion_sensitivity <= 35) {
				spSens.setSelection(SENS_LOW);
			} else if (alarm_motion_sensitivity > 35 && alarm_motion_sensitivity <= 65) {
				spSens.setSelection(SENS_MEDIUM);
			} else if (alarm_motion_sensitivity > 65 && alarm_motion_sensitivity <= 95) {
				spSens.setSelection(SENS_HIGH);
			} else if (alarm_motion_sensitivity > 95) {
				spSens.setSelection(SENS_MAX);
			}

			switch (alarm_preset) {
			case PRESET_OFF:
				spPreset.setSelection(PRESET_OFF);
				break;

			case PRESET_POS_1:
				spPreset.setSelection(PRESET_POS_1);
				break;

			case PRESET_POS_2:
				spPreset.setSelection(PRESET_POS_2);
				break;

			case PRESET_POS_3:
				spPreset.setSelection(PRESET_POS_3);
				break;

			case PRESET_POS_4:
				spPreset.setSelection(PRESET_POS_4);
				break;
			}

			if (alarm_mail == 1) {
				swMail.setChecked(true);
			} else {
				swMail.setChecked(false);
			}

			if (alarm_upload_interval == 0) {
				spInterval.setSelection(INTERVAL_OFF);
			} else if (alarm_upload_interval == 1) {
				spInterval.setSelection(INTERVAL_NO_LIMIT);
			} else if (alarm_upload_interval > 1 && alarm_upload_interval <= 60) {
				spInterval.setSelection(INTERVAL_1_MIN);
			} else if (alarm_upload_interval > 60 && alarm_upload_interval <= 180) {
				spInterval.setSelection(INTERVAL_3_MIN);
			} else if (alarm_upload_interval > 180 && alarm_upload_interval <= 300) {
				spInterval.setSelection(INTERVAL_5_MIN);
			} else if (alarm_upload_interval > 300 && alarm_upload_interval <= 600) {
				spInterval.setSelection(INTERVAL_10_MIN);
			} else if (alarm_upload_interval > 600) {
				spInterval.setSelection(INTERVAL_30_MIN);
			}
		}

		swMain.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					layoutSens.setVisibility(View.VISIBLE);
				} else {
					layoutSens.setVisibility(View.GONE);
				}
			}
		});

	}

	private void quit() {
		if (mIsMajor) {
			int sens = -1;

			if (swMain.isChecked()) {
				switch (spSens.getSelectedItemPosition()) {
				case SENS_LOW:
					sens = 25;
					break;

				case SENS_MEDIUM:
					sens = 50;
					break;

				case SENS_HIGH:
					sens = 75;
					break;

				case SENS_MAX:
					sens = 100;
					break;
				}
			} else {
				sens = 0;
			}

			if (sens >= 0 && sens != mSensitivity && mCamera != null && mDevice != null) {
				mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETMOTIONDETECT_REQ,
						AVIOCTRLDEFs.SMsgAVIoctrlSetMotionDetectReq.parseContent(mDevice.ChannelIndex, sens));

				setResult(RESULT_OK);
			} else {
				setResult(RESULT_CANCELED);
			}

			finish();
			overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
		} else {
			byte sens = -1;
			byte preset = -1;
			byte mail = -1;
			int interval = -1;

			if (swMain.isChecked()) {
				switch (spSens.getSelectedItemPosition()) {
				case SENS_LOW:
					sens = 25;
					break;

				case SENS_MEDIUM:
					sens = 50;
					break;

				case SENS_HIGH:
					sens = 75;
					break;

				case SENS_MAX:
					sens = 100;
					break;
				}
			} else {
				sens = 0;
			}

			preset = (byte) spPreset.getSelectedItemPosition();
			if (swMail.isChecked()) {
				mail = 1;
			} else {
				mail = 0;
			}

			switch (spInterval.getSelectedItemPosition()) {
			case INTERVAL_NO_LIMIT:
				interval = 1;
				break;

			case INTERVAL_1_MIN:
				interval = 60;
				break;

			case INTERVAL_3_MIN:
				interval = 180;
				break;

			case INTERVAL_5_MIN:
				interval = 300;
				break;

			case INTERVAL_10_MIN:
				interval = 600;
				break;

			case INTERVAL_30_MIN:
				interval = 1800;
				break;

			case INTERVAL_OFF:
				interval = 0;
				break;
			}

			if (swMain.isChecked()) {
				if ((alarm_motion_armed == 0 || sens != alarm_motion_sensitivity || preset != alarm_preset || mail != alarm_mail
						|| interval != alarm_upload_interval) && mCamera != null && mDevice != null) {
					
					mCamera.sendIOCtrl(mDevice.ChannelIndex,AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETGUARD_REQ,AVIOCTRLDEFs.SMsgAVIoctrlSetGuardReq.parseContent(mDevice.ChannelIndex,
							(byte) 1, sens, preset, mail, interval));
					setResult(RESULT_OK);
				} else {
					setResult(RESULT_CANCELED);
				}
			} else {				
				if (alarm_motion_armed == 1 && mCamera != null && mDevice != null) {
					
					sens = 0;
					preset = 0;
					mail = 0;
					interval = 0;
					mCamera.sendIOCtrl(mDevice.ChannelIndex,AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETGUARD_REQ,AVIOCTRLDEFs.SMsgAVIoctrlSetGuardReq.parseContent(mDevice.ChannelIndex,
							(byte) 0, sens, preset, mail, interval));
					setResult(RESULT_OK);
				} else {
					setResult(RESULT_CANCELED);
				}
			}
			finish();
			overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {

		case KeyEvent.KEYCODE_BACK:
			quit();
			break;
		}

		return super.onKeyDown(keyCode, event);
	}
}
