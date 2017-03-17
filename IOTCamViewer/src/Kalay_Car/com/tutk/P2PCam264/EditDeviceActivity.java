package com.tutk.P2PCam264;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.IRegisterIOTCListener;
import com.tutk.IOTC.Packet;
import com.tutk.Kalay.general.R;
import com.tutk.Kalay.settings.DeviceInfoActivity;
import com.tutk.Kalay.settings.EventSettingsActivity;
import com.tutk.Kalay.settings.FTPSettingsActivity;
import com.tutk.Kalay.settings.FormatSDActivity;
import com.tutk.Kalay.settings.MailSettingsActivity;
import com.tutk.Kalay.settings.SecurityPasswordActivity;
import com.tutk.Kalay.settings.SetWiFiActivity;
import com.tutk.Kalay.settings.TimeZoneActivity;
import com.tutk.P2PCam264.DELUX.Custom_OkCancle_Dialog;
import com.tutk.P2PCam264.DELUX.Custom_OkCancle_Dialog.OkCancelDialogListener;
import com.tutk.P2PCam264.DELUX.Custom_Ok_Dialog;
import com.tutk.P2PCam264.DELUX.MultiViewActivity;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import general.DatabaseManager;

public class EditDeviceActivity extends Activity implements IRegisterIOTCListener, OkCancelDialogListener {

	private static final int REQUEST_CODE_ADVANCED = 0;
	private static final int REQUEST_CODE_PWD = 1;
	private static final int REQUEST_CODE_TIME_ZONE = 2;
	private static final int REQUEST_CODE_WIFI = 3;
	private static final int REQUEST_CODE_EVENT = 4;
	private static final int REQUEST_CODE_FTP = 5;
	private static final int REQUEST_CODE_MAIL = 6;

	private final int[] mAnims = { R.anim.topbar_slide_show, R.anim.topbar_slide_show_double, R.anim.topbar_slide_show_triple,
			R.anim.topbar_slide_show_fourfold, R.anim.topbar_slide_show_fivefold, R.anim.topbar_slide_show_sixfold,
			R.anim.topbar_slide_show_sevenfold, R.anim.topbar_slide_show_eightfold };

	private EditDeviceActivity mActivity;
	private Button btnOK;
	private Button btnCancel;
	private Button btnRemove;
	private Button mReconnectBtn;

	private EditText edtUID;
	private EditText edtNickName;
	private EditText edtSecurityCode;

	private LinearLayout mTouchLayout;
	private LinearLayout layoutWiFi;
	private LinearLayout layoutNotiInterval;
	private LinearLayout layoutRecording;
	private LinearLayout layoutTimeZone;
	private LinearLayout layoutEvent;
	private LinearLayout layoutFTP;
	private LinearLayout layoutMail;
	private RelativeLayout layoutPwd;
	private RelativeLayout layoutSD;
	private RelativeLayout layoutInfo;
    private RelativeLayout layoutUpgrade;

	private ImageButton btnPwd;
	private ImageButton btnWiFi;
	private ImageButton btnTimeZone;
	private ImageButton btnSD;
	private ImageButton btnEvent;
	private ImageButton btnFTP;
	private ImageButton btnMail;
	private ImageButton btnInfo;
	private Spinner spInterval;
	private Spinner spinRecordingMode;
	private TextView tvWiFi;
	private TextView tvTimeZone;
	private TextView tvEvent;
	private TextView tvFTP;
	private TextView tvMail;

	private MyCamera mCamera = null;
	private DeviceInfo mDevice = null;

	private boolean mIsModifyAdvancedSettingAndNeedReconnect = false;

	private int mRecordType = -1;
	private int mTotalSize = -1;
	private boolean mShowRecording = false;
	private boolean mShowTimeZone = false;
	private boolean mShowPwd = false;
	private boolean mShowWiFi = false;
	private boolean mShowInterval = false;
	private boolean mShowEvent = false;
	private boolean mShowInfo = false;
    private boolean mShowUpgrade = false;
	private boolean mShowMailFTP = false;
	private boolean mHideAll = false;
	private boolean mIsMajor = true;
    private boolean mClickOK = false;
	private boolean mHasSSID = false;
	public static List<AVIOCTRLDEFs.SWifiAp> m_wifiList = new ArrayList<AVIOCTRLDEFs.SWifiAp>();
	private byte alarm_motion_armed = -1;
	private byte alarm_motion_sensitivity = -1;
	private byte alarm_preset = -1;
	private byte alarm_mail = -1;
	private int alarm_upload_interval = -1;
	private int mSensitivity = -1;
	private String ftp_server = "";
	private int ftp_port = -1;
	private String ftp_usr = "";
	private String ftp_pwd = "";
	private String ftp_path = "";
	private int ftp_mode = -1;
	private String mail_sender = "";
	private String mail_receiver = "";
	private String mail_server = "";
	private int mail_port = -1;
	private int mail_protocol = -1;
	private String mail_usr = "";
	private String mail_pwd = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = this;
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.titlebar);
		TextView tv = (TextView) this.findViewById(R.id.bar_text);
		tv.setText(getText(R.string.txtDeviceSetting));

		setContentView(R.layout.edit_device);

		Bundle bundle = this.getIntent().getExtras();
		String devUUID = bundle.getString("dev_uuid");
		String devUID = bundle.getString("dev_uid");

		for (DeviceInfo deviceInfo : MultiViewActivity.DeviceList) {

			if (devUUID.equalsIgnoreCase(deviceInfo.UUID) && devUID.equalsIgnoreCase(deviceInfo.UID)) {

				mDevice = deviceInfo;
				break;
			}
		}

		for (MyCamera camera : MultiViewActivity.CameraList) {

			if (devUUID.equalsIgnoreCase(camera.getUUID()) && devUID.equalsIgnoreCase(camera.getUID())) {

				mCamera = camera;
				mCamera.registerIOTCListener(this);
				break;
			}
		}

		/* find view */
		edtUID = (EditText) findViewById(R.id.edtUID);
		edtSecurityCode = (EditText) findViewById(R.id.edtSecurityCode);
		edtNickName = (EditText) findViewById(R.id.edtNickName);
		btnOK = (Button) findViewById(R.id.btnOK);
		btnCancel = (Button) findViewById(R.id.btnCancel);
		btnRemove = (Button) findViewById(R.id.btnRemove);
		mReconnectBtn = (Button) findViewById(R.id.btnReconnect);
		mTouchLayout = (LinearLayout) findViewById(R.id.touch_layout);

		layoutWiFi = (LinearLayout) findViewById(R.id.layoutWiFi);
		layoutNotiInterval = (LinearLayout) findViewById(R.id.layoutNotiInterval);
		layoutRecording = (LinearLayout) findViewById(R.id.layoutRecording);
		layoutTimeZone = (LinearLayout) findViewById(R.id.layoutTimeZone);
		layoutEvent = (LinearLayout) findViewById(R.id.layoutEvent);
		layoutFTP = (LinearLayout) findViewById(R.id.layoutFTP);
		layoutMail = (LinearLayout) findViewById(R.id.layoutMail);
		layoutPwd = (RelativeLayout) findViewById(R.id.layoutPwd);
		layoutSD = (RelativeLayout) findViewById(R.id.layoutSD);
		layoutInfo = (RelativeLayout) findViewById(R.id.layoutInfo);
        layoutUpgrade = (RelativeLayout) findViewById(R.id.layoutUpgrade);

		btnPwd = (ImageButton) findViewById(R.id.btnPwd);
		btnWiFi = (ImageButton) findViewById(R.id.btnWiFi);
		btnTimeZone = (ImageButton) findViewById(R.id.btnTimeZone);
		btnSD = (ImageButton) findViewById(R.id.btnSD);
		btnEvent = (ImageButton) findViewById(R.id.btnEvent);
		btnFTP = (ImageButton) findViewById(R.id.btnFTP);
		btnMail = (ImageButton) findViewById(R.id.btnMail);
		btnInfo = (ImageButton) findViewById(R.id.btnInfo);
		spInterval = (Spinner) findViewById(R.id.spNotiInterval);
		tvWiFi = (TextView) findViewById(R.id.txtWiFi);
		spinRecordingMode = (Spinner) findViewById(R.id.spRecording);
		tvWiFi = (TextView) findViewById(R.id.txtWiFi);
		tvTimeZone = (TextView) findViewById(R.id.txtTimeZone);
		tvEvent = (TextView) findViewById(R.id.tvEvent);
		tvFTP = (TextView) findViewById(R.id.tvFTP);
		tvMail = (TextView) findViewById(R.id.tvMail);

		/* set valude */
		edtUID.setText(devUID);
		edtUID.setEnabled(false);

		if (mDevice != null) {
			edtSecurityCode.setText(mDevice.View_Password);
			edtNickName.setText(mDevice.NickName);
		}
		if (mCamera != null && mCamera.isChannelConnected(0)) {
			mReconnectBtn.setText(R.string.connstus_connected);

			layoutPwd.setVisibility(View.VISIBLE);

			mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_REQ,
					AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoReq.parseContent());
			initFTPMail();

			if (mCamera.getWiFiSettingSupported(0)) {
				initWiFiSSID();
				layoutWiFi.setVisibility(View.VISIBLE);
			}

			/*
			if (mCamera.getEventSettingSupported(0)) {
				initInterval();
				layoutNotiInterval.setVisibility(View.VISIBLE);
			}
			*/
			if (mCamera.getRecordSettingSupported(0)) {
				initRecordingMode();
				layoutRecording.setVisibility(View.VISIBLE);
			}
			/*
			if (mCamera.getTimeZone(0)) {
				try {
					String strRecvMsg = new String(mCamera.getTimeZoneString(), 0, mCamera.getTimeZoneString().length, "utf-8");
					tvTimeZone.setText(strRecvMsg);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				layoutTimeZone.setVisibility(View.VISIBLE);
			}
			*/

			if (mCamera.getEventSettingSupported(0)) {
				initEventSettings();
				layoutEvent.setVisibility(View.VISIBLE);
			}

			if (mCamera.getDeviceInfoSupport(0)) {
				layoutInfo.setVisibility(View.VISIBLE);
			}

//            layoutUpgrade.setVisibility(View.VISIBLE);

		} else {
			mReconnectBtn.setText(R.string.connstus_disconnect);
		}

		/* set listener */
		btnOK.setOnClickListener(btnOKOnClickListener);
		btnCancel.setOnClickListener(btnCancelOnClickListener);
		btnRemove.setOnClickListener(btnRomoveOnClickListener);
		mReconnectBtn.setOnClickListener(btnReconnectOnClickListener);
		mTouchLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				InputMethodManager imm = (InputMethodManager) getSystemService(mActivity.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mTouchLayout.getWindowToken(), 0);
			}
		});

		btnPwd.setOnClickListener(clickPwd);
		btnWiFi.setOnClickListener(clickWiFi);
		btnSD.setOnClickListener(clickSD);
		btnEvent.setOnClickListener(clickEvent);
		btnFTP.setOnClickListener(clickFTP);
		btnMail.setOnClickListener(clickMail);
		btnInfo.setOnClickListener(clickInfo);
		btnTimeZone.setOnClickListener(clickTimeZone);

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Custom_OkCancle_Dialog.SetDialogListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {

		case KeyEvent.KEYCODE_BACK:
			quit(false);
			break;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_ADVANCED) {

			Bundle extras = data.getExtras();

			switch (resultCode) {
			case RESULT_OK:

				mIsModifyAdvancedSettingAndNeedReconnect = extras.getBoolean("need_reconnect");
				boolean isChangePassword = extras.getBoolean("change_password");
				String newPassword = extras.getString("new_password");

				if (isChangePassword)
					edtSecurityCode.setText(newPassword);

				break;

			case RESULT_CANCELED:
				break;
			}
		} else if (requestCode == REQUEST_CODE_PWD) {
			switch (resultCode) {
			case RESULT_OK:
				String newPwd = data.getStringExtra("new");
				edtSecurityCode.setText(newPwd);
				mIsModifyAdvancedSettingAndNeedReconnect = true;
				break;
			}
		} else if (requestCode == REQUEST_CODE_TIME_ZONE) {
			if (resultCode == RESULT_OK) {
				String location = data.getStringExtra("name");
				tvTimeZone.setText(location);
				mIsModifyAdvancedSettingAndNeedReconnect = true;
			}
		} else if (requestCode == REQUEST_CODE_WIFI) {
			if (resultCode == RESULT_OK) {
				String ssid = data.getStringExtra("ssid");
				boolean isHotSpot = data.getBooleanExtra("isHotspot",false);
				tvWiFi.setText(ssid);
				mIsModifyAdvancedSettingAndNeedReconnect = true;
			}
		} else if (requestCode == REQUEST_CODE_EVENT) {
			if (resultCode == RESULT_OK) {
				mIsModifyAdvancedSettingAndNeedReconnect = true;
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						initEventSettings();
						mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETGUARD_REQ,
								AVIOCTRLDEFs.SMsgAVIoctrlGetGuardReq.parseContent(mDevice.ChannelIndex));
					}
				}, 500);
			}
		} else if (requestCode == REQUEST_CODE_FTP) {
			if (resultCode == RESULT_OK) {
				mIsModifyAdvancedSettingAndNeedReconnect = true;
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						initFTPMail();
					}
				}, 500);
			}
		} else if (requestCode == REQUEST_CODE_MAIL) {
			if (resultCode == RESULT_OK) {
				mIsModifyAdvancedSettingAndNeedReconnect = true;
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						initFTPMail();
					}
				}, 500);
			}
		}
	}

	private OnClickListener btnRomoveOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			Custom_OkCancle_Dialog dlg = new Custom_OkCancle_Dialog(EditDeviceActivity.this, getText(R.string.tips_remove_camera_confirm).toString());
			dlg.setCanceledOnTouchOutside(false);
			Window window = dlg.getWindow();
			window.setWindowAnimations(R.style.setting_dailog_animstyle);
			dlg.show();

		}
	};

	private OnClickListener btnReconnectOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (!mHideAll)
				clearAllPanel();

			mReconnectBtn.setText(R.string.connstus_connecting);
			mCamera.disconnect();
			mCamera.connect(mDevice.UID);
			mCamera.start(Camera.DEFAULT_AV_CHANNEL, mDevice.View_Account, mDevice.View_Password);
			mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_REQ,
					AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoReq.parseContent());
			mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETSUPPORTSTREAM_REQ,
					AVIOCTRLDEFs.SMsgAVIoctrlGetSupportStreamReq.parseContent());
			mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETAUDIOOUTFORMAT_REQ,
					AVIOCTRLDEFs.SMsgAVIoctrlGetAudioOutFormatReq.parseContent());
			mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIMEZONE_REQ,
					AVIOCTRLDEFs.SMsgAVIoctrlTimeZone.parseContent());

			initFTPMail();
		}
	};

	private OnClickListener btnOKOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			int recType = spinRecordingMode.getSelectedItemPosition();

			if (mCamera != null) {
				if (mRecordType != -1 && mRecordType != recType) {

					int rd = 0;

					if (spinRecordingMode.getSelectedItemPosition() == 0)
						rd = AVIOCTRLDEFs.AVIOTC_RECORDTYPE_OFF;
					else if (spinRecordingMode.getSelectedItemPosition() == 1)
						rd = AVIOCTRLDEFs.AVIOTC_RECORDTYPE_FULLTIME;
					else if (spinRecordingMode.getSelectedItemPosition() == 2)
						rd = AVIOCTRLDEFs.AVIOTC_RECORDTYPE_ALAM;
					else if (spinRecordingMode.getSelectedItemPosition() == 3)
						rd = AVIOCTRLDEFs.AVIOTC_RECORDTYPE_MANUAL;

					mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETRECORD_REQ,
							AVIOCTRLDEFs.SMsgAVIoctrlSetRecordReq.parseContent(mDevice.ChannelIndex, rd));

					mIsModifyAdvancedSettingAndNeedReconnect = true;
				}
			}

			quit(true);
		}
	};

	private OnClickListener btnCancelOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			quit(false);
		}
	};

	private OnClickListener clickPwd = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(EditDeviceActivity.this, SecurityPasswordActivity.class);
			intent.putExtra("dev_uuid", mDevice.UUID);
			intent.putExtra("dev_uid", mDevice.UID);
			startActivityForResult(intent, REQUEST_CODE_PWD);
			overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
		}
	};

	private OnClickListener clickWiFi = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(EditDeviceActivity.this, SetWiFiActivity.class);
			intent.putExtra("dev_uuid", mDevice.UUID);
			intent.putExtra("dev_uid", mDevice.UID);
			startActivityForResult(intent, REQUEST_CODE_WIFI);
			overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
		}
	};

	private OnClickListener clickSD = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(EditDeviceActivity.this, FormatSDActivity.class);
			intent.putExtra("dev_uuid", mDevice.UUID);
			intent.putExtra("dev_uid", mDevice.UID);
			startActivity(intent);
			overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
		}
	};

	private OnClickListener clickEvent = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(EditDeviceActivity.this, EventSettingsActivity.class);
			Bundle bundle = new Bundle();

			bundle.putString("dev_uuid", mDevice.UUID);
			bundle.putString("dev_uid", mDevice.UID);
			bundle.putBoolean("major", mIsMajor);
			bundle.putInt("sens", mSensitivity);
			bundle.putByte("main", alarm_motion_armed);
			bundle.putByte("g_sens", alarm_motion_sensitivity);
			bundle.putByte("preset", alarm_preset);
			bundle.putByte("mail", alarm_mail);
			bundle.putInt("ftp", alarm_upload_interval);

			intent.putExtras(bundle);
			startActivityForResult(intent, REQUEST_CODE_EVENT);
			overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
		}
	};

	private OnClickListener clickFTP = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(EditDeviceActivity.this, FTPSettingsActivity.class);
			Bundle bundle = new Bundle();

			bundle.putString("dev_uuid", mDevice.UUID);
			bundle.putString("dev_uid", mDevice.UID);
			bundle.putString("server", ftp_server);
			bundle.putInt("port", ftp_port);
			bundle.putString("usr", ftp_usr);
			bundle.putString("pwd", ftp_pwd);
			bundle.putString("path", ftp_path);
			bundle.putInt("mode", ftp_mode);

			intent.putExtras(bundle);
			startActivityForResult(intent, REQUEST_CODE_FTP);
			overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
		}
	};

	private OnClickListener clickMail = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(EditDeviceActivity.this, MailSettingsActivity.class);
			Bundle bundle = new Bundle();

			bundle.putString("dev_uuid", mDevice.UUID);
			bundle.putString("dev_uid", mDevice.UID);
			bundle.putString("sender", mail_sender);
			bundle.putString("receiver", mail_receiver);
			bundle.putString("server", mail_server);
			bundle.putInt("port", mail_port);
			bundle.putInt("protocol", mail_protocol);
			bundle.putString("usr", mail_usr);
			bundle.putString("pwd", mail_pwd);

			intent.putExtras(bundle);
			startActivityForResult(intent, REQUEST_CODE_MAIL);
			overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
		}
	};

	private OnClickListener clickInfo = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(EditDeviceActivity.this, DeviceInfoActivity.class);
			intent.putExtra("dev_uuid", mDevice.UUID);
			intent.putExtra("dev_uid", mDevice.UID);
			startActivity(intent);
			overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
		}
	};

	private OnClickListener clickTimeZone = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(EditDeviceActivity.this, TimeZoneActivity.class);
			intent.putExtra("dev_uuid", mDevice.UUID);
			intent.putExtra("dev_uid", mDevice.UID);
			startActivityForResult(intent, REQUEST_CODE_TIME_ZONE);
			overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
		}
	};

	private void clearAllPanel() {
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.list_fade_out);
		anim.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
                if(mHideAll) {
                    layoutWiFi.setVisibility(View.GONE);
                    layoutNotiInterval.setVisibility(View.GONE);
                    layoutRecording.setVisibility(View.GONE);
                    layoutTimeZone.setVisibility(View.GONE);
                    layoutPwd.setVisibility(View.GONE);
                    layoutEvent.setVisibility(View.GONE);
                    layoutSD.setVisibility(View.GONE);
                    layoutInfo.setVisibility(View.GONE);
                    layoutUpgrade.setVisibility(View.GONE);

                    layoutFTP.setVisibility(View.GONE);
                    layoutMail.setVisibility(View.GONE);
                }
			}
		});

		layoutPwd.startAnimation(anim);
		layoutWiFi.startAnimation(anim);
		layoutNotiInterval.startAnimation(anim);
		layoutRecording.startAnimation(anim);
		layoutTimeZone.startAnimation(anim);
		layoutSD.startAnimation(anim);
		layoutEvent.startAnimation(anim);
		layoutInfo.startAnimation(anim);
        layoutUpgrade.startAnimation(anim);

		mShowRecording = false;
		mShowTimeZone = false;
		mShowPwd = false;
		mShowWiFi = false;
		mShowInterval = false;
		mShowEvent = false;
		mShowInfo = false;
        mShowUpgrade = false;
		mShowMailFTP = false;
		mHideAll = true;
		mIsMajor = true;
	}

	private void quit(boolean isPressOK) {

		if (!isPressOK && !mIsModifyAdvancedSettingAndNeedReconnect) {

			if (mCamera != null) {
				mCamera.unregisterIOTCListener(this);
			}

			Intent intent = new Intent();
			setResult(RESULT_CANCELED, intent);
			finish();
			overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
		} else if (!isPressOK && mIsModifyAdvancedSettingAndNeedReconnect) {
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (mCamera != null) {
						mCamera.setPassword(mDevice.View_Password);
						mCamera.stop(Camera.DEFAULT_AV_CHANNEL);
						mCamera.disconnect();
						mCamera.connect(mDevice.UID);
						mCamera.start(Camera.DEFAULT_AV_CHANNEL, mDevice.View_Account, mDevice.View_Password);
						mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_REQ,
								AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoReq.parseContent());
						mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETSUPPORTSTREAM_REQ,
								AVIOCTRLDEFs.SMsgAVIoctrlGetSupportStreamReq.parseContent());
						mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETAUDIOOUTFORMAT_REQ,
								AVIOCTRLDEFs.SMsgAVIoctrlGetAudioOutFormatReq.parseContent());
						mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIMEZONE_REQ,
								AVIOCTRLDEFs.SMsgAVIoctrlTimeZone.parseContent());
					}
				}
			}, 500);

			Intent intent = new Intent();
			setResult(MultiViewActivity.REQUEST_CODE_CAMERA_EDIT_DATA_OK, intent);
			finish();
			overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
		} else if (isPressOK && mIsModifyAdvancedSettingAndNeedReconnect) {

			String nickname = edtNickName.getText().toString();
			String uid = edtUID.getText().toString();
			String view_acc = mDevice.View_Account;
			String view_pwd = edtSecurityCode.getText().toString();

			if (nickname.length() == 0) {
//				MultiViewActivity.showAlert(EditDeviceActivity.this, getText(R.string.tips_warning), getText(R.string.tips_camera_name), getText(R.string.ok));
				Custom_Ok_Dialog dlg = new Custom_Ok_Dialog(this, getText(R.string.tips_camera_name).toString(), getText(R.string.ok).toString());
				dlg.setCanceledOnTouchOutside(false);
				Window window = dlg.getWindow();
				window.setWindowAnimations(R.style.setting_dailog_animstyle);
				dlg.show();
				return;
			}

			if (uid.length() == 0) {
//				MultiViewActivity.showAlert(EditDeviceActivity.this, getText(R.string.tips_warning), getText(R.string.tips_dev_uid), getText(R.string.ok));
				Custom_Ok_Dialog dlg = new Custom_Ok_Dialog(this, getText(R.string.tips_dev_uid).toString(), getText(R.string.ok).toString());
				dlg.setCanceledOnTouchOutside(false);
				Window window = dlg.getWindow();
				window.setWindowAnimations(R.style.setting_dailog_animstyle);
				dlg.show();
				return;
			}

			if (uid.length() != 20) {
//				MultiViewActivity.showAlert(EditDeviceActivity.this, getText(R.string.tips_warning), getText(R.string.tips_dev_uid_character), getText(R.string.ok));
				Custom_Ok_Dialog dlg = new Custom_Ok_Dialog(this, getText(R.string.tips_dev_uid_character).toString(), getText(R.string.ok)
						.toString());
				dlg.setCanceledOnTouchOutside(false);
				Window window = dlg.getWindow();
				window.setWindowAnimations(R.style.setting_dailog_animstyle);
				dlg.show();
				return;
			}

			if (view_pwd.length() <= 0) {
//				MultiViewActivity.showAlert(EditDeviceActivity.this, getText(R.string.tips_warning), getText(R.string.tips_dev_security_code), getText(R.string.ok));
				Custom_Ok_Dialog dlg = new Custom_Ok_Dialog(this, getText(R.string.tips_dev_security_code).toString(), getText(R.string.ok)
						.toString());
				dlg.setCanceledOnTouchOutside(false);
				Window window = dlg.getWindow();
				window.setWindowAnimations(R.style.setting_dailog_animstyle);
				dlg.show();
				return;
			}

			// To verify whether is something been modified.
			if (!nickname.equalsIgnoreCase(mDevice.NickName) || !uid.equalsIgnoreCase(mDevice.UID)
					|| !view_acc.equalsIgnoreCase(mDevice.View_Account) || !view_pwd.equalsIgnoreCase(mDevice.View_Password)) {

				mDevice.NickName = nickname;
				mDevice.UID = uid;
				mDevice.View_Account = view_acc;
				mDevice.View_Password = view_pwd;

				/* update value to data base */
				DatabaseManager manager = new DatabaseManager(EditDeviceActivity.this);
				manager.updateDeviceInfoByDBID(mDevice.DBID, mDevice.UID, nickname, "", "", view_acc, view_pwd, mDevice.EventNotification,
						mDevice.ChannelIndex);
			}

			if (mCamera != null && mCamera.getEventSettingSupported(0)) {
				SharedPreferences settings = getSharedPreferences("Interval", 0);
				settings.edit().putInt(mDevice.UID, spInterval.getSelectedItemPosition()).commit();
			}

            if(mClickOK){
                return;
            }

            mClickOK = true;

			/* reconnect camera */
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (mCamera != null) {
						mCamera.setPassword(mDevice.View_Password);
						mCamera.stop(Camera.DEFAULT_AV_CHANNEL);
						mCamera.disconnect();
						mCamera.connect(mDevice.UID);
						mCamera.start(Camera.DEFAULT_AV_CHANNEL, mDevice.View_Account, mDevice.View_Password);
						mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_REQ,
								AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoReq.parseContent());
						mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETSUPPORTSTREAM_REQ,
								AVIOCTRLDEFs.SMsgAVIoctrlGetSupportStreamReq.parseContent());
						mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETAUDIOOUTFORMAT_REQ,
								AVIOCTRLDEFs.SMsgAVIoctrlGetAudioOutFormatReq.parseContent());
						mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIMEZONE_REQ,
								AVIOCTRLDEFs.SMsgAVIoctrlTimeZone.parseContent());
					}

                    Toast.makeText(EditDeviceActivity.this, getText(R.string.tips_edit_camera_ok).toString(), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent();
                    setResult(MultiViewActivity.REQUEST_CODE_CAMERA_EDIT_DATA_OK, intent);
                    finish();
					overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
				}
			}, 500);

		} else if (isPressOK && !mIsModifyAdvancedSettingAndNeedReconnect) {

			String nickname = edtNickName.getText().toString();
			String uid = edtUID.getText().toString();
			String view_acc = mDevice.View_Account;
			String view_pwd = edtSecurityCode.getText().toString();

			if (nickname.length() == 0) {
//				MultiViewActivity.showAlert(EditDeviceActivity.this, getText(R.string.tips_warning), getText(R.string.tips_camera_name), getText(R.string.ok));
				Custom_Ok_Dialog dlg = new Custom_Ok_Dialog(this, getText(R.string.tips_camera_name).toString(), getText(R.string.ok).toString());
				dlg.setCanceledOnTouchOutside(false);
				Window window = dlg.getWindow();
				window.setWindowAnimations(R.style.setting_dailog_animstyle);
				dlg.show();
				return;
			}

			if (uid.length() == 0) {
//				MultiViewActivity.showAlert(EditDeviceActivity.this, getText(R.string.tips_warning), getText(R.string.tips_dev_uid), getText(R.string.ok));
				Custom_Ok_Dialog dlg = new Custom_Ok_Dialog(this, getText(R.string.tips_dev_uid).toString(), getText(R.string.ok).toString());
				dlg.setCanceledOnTouchOutside(false);
				Window window = dlg.getWindow();
				window.setWindowAnimations(R.style.setting_dailog_animstyle);
				dlg.show();
				return;
			}

			if (uid.length() != 20) {
//				MultiViewActivity.showAlert(EditDeviceActivity.this, getText(R.string.tips_warning), getText(R.string.tips_dev_uid_character), getText(R.string.ok));
				Custom_Ok_Dialog dlg = new Custom_Ok_Dialog(this, getText(R.string.tips_dev_uid_character).toString(), getText(R.string.ok)
						.toString());
				dlg.setCanceledOnTouchOutside(false);
				Window window = dlg.getWindow();
				window.setWindowAnimations(R.style.setting_dailog_animstyle);
				dlg.show();
				return;
			}

			if (view_pwd.length() <= 0) {
//				MultiViewActivity.showAlert(EditDeviceActivity.this, getText(R.string.tips_warning), getText(R.string.tips_dev_security_code), getText(R.string.ok));
				Custom_Ok_Dialog dlg = new Custom_Ok_Dialog(this, getText(R.string.tips_dev_security_code).toString(), getText(R.string.ok)
						.toString());
				dlg.setCanceledOnTouchOutside(false);
				Window window = dlg.getWindow();
				window.setWindowAnimations(R.style.setting_dailog_animstyle);
				dlg.show();
				return;
			}

			/* reconnect camera */
			if (mCamera != null
					&& (!uid.equalsIgnoreCase(mDevice.UID) || !view_acc.equalsIgnoreCase(mDevice.View_Account) || !view_pwd
							.equalsIgnoreCase(mDevice.View_Password))) {
				mCamera.setPassword(view_pwd);
				mCamera.unregisterIOTCListener(this);
				mCamera.stop(Camera.DEFAULT_AV_CHANNEL);
				mCamera.disconnect();
				mCamera.connect(uid);
				mCamera.start(Camera.DEFAULT_AV_CHANNEL, view_acc, view_pwd);
				mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_REQ,
						AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoReq.parseContent());
				mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETSUPPORTSTREAM_REQ,
						AVIOCTRLDEFs.SMsgAVIoctrlGetSupportStreamReq.parseContent());
				mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETAUDIOOUTFORMAT_REQ,
						AVIOCTRLDEFs.SMsgAVIoctrlGetAudioOutFormatReq.parseContent());
				mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIMEZONE_REQ,
						AVIOCTRLDEFs.SMsgAVIoctrlTimeZone.parseContent());

			}

			/* Save to database */
			if (!nickname.equalsIgnoreCase(mDevice.NickName) || !uid.equalsIgnoreCase(mDevice.UID)
					|| !view_acc.equalsIgnoreCase(mDevice.View_Account) || !view_pwd.equalsIgnoreCase(mDevice.View_Password)) {

				if (!view_pwd.equalsIgnoreCase(mDevice.View_Password)) {
					mDevice.ChangePassword = true;
				}
				mDevice.NickName = nickname;
				mDevice.UID = uid;
				mDevice.View_Account = view_acc;
				mDevice.View_Password = view_pwd;

				/* update value to data base */
				DatabaseManager manager = new DatabaseManager(EditDeviceActivity.this);
				manager.updateDeviceInfoByDBID(mDevice.DBID, mDevice.UID, nickname, "", "", view_acc, view_pwd, mDevice.EventNotification,
						mDevice.ChannelIndex);
			}

			if (mCamera != null && mCamera.getEventSettingSupported(0)) {
				SharedPreferences settings = getSharedPreferences("Interval", 0);
				settings.edit().putInt(mDevice.UID, spInterval.getSelectedItemPosition()).commit();
			}

			Intent intent = new Intent();
			EditDeviceActivity.this.setResult(MultiViewActivity.REQUEST_CODE_CAMERA_EDIT_DATA_OK, intent);
			EditDeviceActivity.this.finish();
			overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
		}
	}

	@Override
	public void receiveFrameData(final Camera camera, int sessionChannel, Bitmap bmp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void receiveFrameInfo(final Camera camera, int sessionChannel, long bitRate, int frameRate, int onlineNm, int frameCount,
			int incompleteFrameCount) {
		// TODO Auto-generated method stub

	}

	@Override
	public void receiveSessionInfo(final Camera camera, int resultCode) {
		Bundle bundle = new Bundle();
		bundle.putString("requestDevice", ((MyCamera) camera).getUUID());

		Message msg = handler.obtainMessage();
		msg.what = resultCode;
		msg.setData(bundle);
		handler.sendMessage(msg);
	}

	@Override
	public void receiveChannelInfo(final Camera camera, int sessionChannel, final int resultCode) {

		if (mCamera == camera) {
			Bundle bundle = new Bundle();
			bundle.putString("requestDevice", ((MyCamera) camera).getUUID());
			bundle.putInt("sessionChannel", sessionChannel);

			Message msg = handler.obtainMessage();
			msg.what = resultCode;
			msg.setData(bundle);
			handler.sendMessage(msg);
		}
	}

	@Override
	public void receiveIOCtrlData(final Camera camera, int sessionChannel, int avIOCtrlMsgType, byte[] data) {

		if (mCamera == camera) {
			Bundle bundle = new Bundle();
			bundle.putInt("sessionChannel", sessionChannel);
			bundle.putByteArray("data", data);

			Message msg = handler.obtainMessage();
			msg.what = avIOCtrlMsgType;
			msg.setData(bundle);
			handler.sendMessage(msg);
		}
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			Bundle bundle = msg.getData();
			byte[] data = bundle.getByteArray("data");

			switch (msg.what) {
			case Camera.CONNECTION_STATE_CONNECTED:

				int animsID = 0;
				mHideAll = false;
				if (!mShowPwd) {
					mShowPwd = true;
					layoutPwd.setVisibility(View.VISIBLE);
					layoutPwd.startAnimation(AnimationUtils.loadAnimation(EditDeviceActivity.this, mAnims[animsID]));
				}

				if (mCamera.getWiFiSettingSupported(0) && !mShowWiFi) {
					initWiFiSSID();
					mShowWiFi = true;
					animsID ++;
					layoutWiFi.setVisibility(View.VISIBLE);
					layoutWiFi.startAnimation(AnimationUtils.loadAnimation(EditDeviceActivity.this, mAnims[animsID]));
				}

				/*
				if (mCamera.getEventSettingSupported(0) && !mShowInterval) {
					initInterval();
					mShowInterval = true;
					animsID ++;
					layoutNotiInterval.setVisibility(View.VISIBLE);
					layoutNotiInterval.startAnimation(AnimationUtils.loadAnimation(EditDeviceActivity.this, mAnims[animsID]));
				}
				*/
				if (mCamera.getRecordSettingSupported(0) && !mShowRecording) {
					initRecordingMode();
					mShowRecording = true;
					animsID ++;
					layoutRecording.setVisibility(View.VISIBLE);
					layoutRecording.startAnimation(AnimationUtils.loadAnimation(EditDeviceActivity.this, mAnims[animsID]));
				}
				/*
				if (mCamera.getTimeZone(0) && !mShowTimeZone) {
					try {
						String strRecvMsg = new String(mCamera.getTimeZoneString(), 0, mCamera.getTimeZoneString().length, "utf-8");
						tvTimeZone.setText(strRecvMsg);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					animsID ++;
					mShowTimeZone = true;
					layoutTimeZone.setVisibility(View.VISIBLE);
					layoutTimeZone.startAnimation(AnimationUtils.loadAnimation(EditDeviceActivity.this, mAnims[animsID]));
				}
				*/

				if (mCamera.getEventSettingSupported(0) && !mShowEvent) {
					initEventSettings();
					animsID ++;
					mShowEvent = true;
					layoutEvent.setVisibility(View.VISIBLE);
					layoutEvent.startAnimation(AnimationUtils.loadAnimation(EditDeviceActivity.this, mAnims[animsID]));
				}

				if (mCamera.getDeviceInfoSupport(0) && !mShowInfo) {
					animsID ++;
					mShowInfo = true;
					layoutInfo.setVisibility(View.VISIBLE);
					layoutInfo.startAnimation(AnimationUtils.loadAnimation(EditDeviceActivity.this, mAnims[animsID]));
				}

                animsID ++;
                mShowUpgrade = true;
                layoutUpgrade.setVisibility(View.VISIBLE);
                layoutUpgrade.startAnimation(AnimationUtils.loadAnimation(EditDeviceActivity.this, mAnims[animsID]));

				handler.postDelayed(new Runnable() {
					@Override
					public void run () {
						mReconnectBtn.setText(R.string.connstus_connected);
					}
				}, 1000);

				break;

			case Camera.CONNECTION_STATE_CONNECTING:
				mReconnectBtn.setText(R.string.connstus_connecting);
				break;

			case Camera.CONNECTION_STATE_DISCONNECTED:
				mReconnectBtn.setText(R.string.connstus_disconnect);
				break;

			case Camera.CONNECTION_STATE_UNKNOWN_DEVICE:
				mReconnectBtn.setText(R.string.connstus_unknown_device);
				break;

			case Camera.CONNECTION_STATE_WRONG_PASSWORD:
				mReconnectBtn.setText(R.string.connstus_wrong_password);
				break;

			case Camera.CONNECTION_STATE_TIMEOUT:
				if (!mHideAll)
					clearAllPanel();
				mReconnectBtn.setText(R.string.connection_timeout);
				break;

			case Camera.CONNECTION_STATE_CONNECT_FAILED:
				mReconnectBtn.setText(R.string.connstus_connection_failed);
				break;

			case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETRECORD_RESP:
				int recordType = Packet.byteArrayToInt_Little(data, 4);

				if (recordType >= 0 && recordType <= 2) {
					spinRecordingMode.setSelection(recordType);
					spinRecordingMode.setEnabled(true);
					mRecordType = recordType;
				}
				break;

			case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_RESP:
				mTotalSize = Packet.byteArrayToInt_Little(data, 40);
				if (mTotalSize >= 0 && mCamera != null && mCamera.getSDCardFormatSupported(0)) {
					layoutSD.setVisibility(View.VISIBLE);
					layoutSD.startAnimation(AnimationUtils.loadAnimation(EditDeviceActivity.this, R.anim.topbar_slide_show));
				}
				break;

			case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_LISTWIFIAP_RESP:

				int cnt = Packet.byteArrayToInt_Little(data, 0);
				int size = AVIOCTRLDEFs.SWifiAp.getTotalSize();

				m_wifiList.clear();

				if (cnt > 0 && data.length >= 40) {

					int pos = 4;
					for (int i = 0; i < cnt; i++) {
						if((i * size + pos) >= data.length){
							break;
						}

						byte[] ssid = new byte[32];
						System.arraycopy(data, i * size + pos, ssid, 0, 32);

						byte mode = data[i * size + pos + 32];
						byte enctype = data[i * size + pos + 33];
						byte signal = data[i * size + pos + 34];
						byte status = data[i * size + pos + 35];

						m_wifiList.add(new AVIOCTRLDEFs.SWifiAp(ssid, mode, enctype, signal, status));

						if (status >= 1 && status <= 4) {
							tvWiFi.setText(getString(ssid));
							tvWiFi.setTextColor(Color.BLACK);
							mHasSSID = true;
						}
					}

					if(!mHasSSID){
						tvWiFi.setText(getText(R.string.none));
						tvWiFi.setTextColor(Color.BLACK);
					}
				}

				btnWiFi.setEnabled(true);
				break;

			case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETGUARD_RESP:
				if(data.length < 12)
					return;

				byte[] interval = new byte[4];
				alarm_motion_armed = data[4];
				alarm_motion_sensitivity = data[5];
				alarm_preset = data[6];
				alarm_mail = data[7];
				System.arraycopy(data, 8, interval, 0, 4);
				alarm_upload_interval = Packet.byteArrayToInt_Little(interval, 0);

				if (!mShowMailFTP) {
					layoutFTP.setVisibility(View.VISIBLE);
					layoutMail.setVisibility(View.VISIBLE);
					layoutMail.setAnimation(AnimationUtils.loadAnimation(EditDeviceActivity.this, R.anim.topbar_slide_show));
					mShowMailFTP = true;
				}
				mIsMajor = false;
				btnEvent.setEnabled(true);
				tvEvent.setTextColor(Color.BLACK);
				break;

			case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETMOTIONDETECT_RESP:
				mSensitivity = Packet.byteArrayToInt_Little(data, 4);
				btnEvent.setEnabled(true);
				tvEvent.setTextColor(Color.BLACK);
				break;
			case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_FTP_RESP:
				if(data.length < 188)
					return;


				btnFTP.setEnabled(true);
				tvFTP.setTextColor(Color.BLACK);
				byte[] ftpServerTmp = new byte[68];
				byte[] ftpPortTmp = new byte[4];
				byte[] userNameTmp = new byte[20];
				byte[] passwordTmp = new byte[20];
				byte[] pathTmp = new byte[68];
				byte[] passiveModeTmp = new byte[4];
				System.arraycopy(data, 4, ftpServerTmp, 0, 68);
				System.arraycopy(data, 72, ftpPortTmp, 0, 4);
				System.arraycopy(data, 76, userNameTmp, 0, 20);
				System.arraycopy(data, 96, passwordTmp, 0, 20);
				System.arraycopy(data, 116, pathTmp, 0, 68);
				System.arraycopy(data, 184, passiveModeTmp, 0, 4);

				ftp_server = getString(ftpServerTmp);
				ftp_port = Packet.byteArrayToInt_Little(ftpPortTmp, 0);
				ftp_usr = getString(userNameTmp);
				ftp_pwd = getString(passwordTmp);
				ftp_path = getString(pathTmp);
				ftp_mode = Packet.byteArrayToInt_Little(passiveModeTmp, 0);
				break;
			case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_SMTP_RESP:
				if(data.length < 268)
					return;

				btnMail.setEnabled(true);
				tvMail.setTextColor(Color.BLACK);
				byte[] Sender = new byte[64];
				byte[] Receiver = new byte[64];
				byte[] Server = new byte[64];
				byte[] Port = new byte[4];
				byte[] mail_tls = new byte[4];
				byte[] user = new byte[32];
				byte[] pwd = new byte[32];

				System.arraycopy(data, 4, Sender, 0, 64);
				System.arraycopy(data, 68, Receiver, 0, 64);
				System.arraycopy(data, 132, Server, 0, 64);
				System.arraycopy(data, 196, Port, 0, 4);
				System.arraycopy(data, 200, mail_tls, 0, 4);
				System.arraycopy(data, 204, user, 0, 32);
				System.arraycopy(data, 236, pwd, 0, 32);

				mail_sender = getString(Sender);
				mail_receiver = getString(Receiver);
				mail_server = getString(Server);
				mail_port = Packet.byteArrayToInt_Little(Port, 0);
				mail_protocol = Packet.byteArrayToInt_Little(mail_tls, 0);
				mail_usr = getString(user);
				mail_pwd = getString(pwd);
				break;
			}
		}
	};

	private void initWiFiSSID() {

		tvWiFi.setText(getText(R.string.tips_wifi_retrieving));
		tvWiFi.setTextColor(getResources().getColor(R.color.settings_gray));
		btnWiFi.setEnabled(false);

		if (mCamera != null) {
			mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_LISTWIFIAP_REQ,
					AVIOCTRLDEFs.SMsgAVIoctrlListWifiApReq.parseContent());
		}
	}

	private void initInterval() {
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.event_interval, R.layout.myspinner_title);
		adapter.setDropDownViewResource(R.layout.myspinner);
		spInterval.setAdapter(adapter);

		SharedPreferences settings = getSharedPreferences("Interval", 0);
		if (mDevice != null) {
			int position = settings.getInt(mDevice.UID, 0);
			spInterval.setSelection(position);
		}
	}

	private void initRecordingMode() {

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.recording_mode, R.layout.myspinner_title);
		adapter.setDropDownViewResource(R.layout.myspinner);
		spinRecordingMode.setAdapter(adapter);
		spinRecordingMode.setEnabled(false);

		if (mCamera != null) {
			mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETRECORD_REQ,
					AVIOCTRLDEFs.SMsgAVIoctrlGetMotionDetectReq.parseContent(mDevice.ChannelIndex));
		}
	}

	private void initEventSettings() {
		btnEvent.setEnabled(false);
		tvEvent.setTextColor(getResources().getColor(R.color.settings_gray));
		if (mCamera != null) {
			mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETMOTIONDETECT_REQ,
					AVIOCTRLDEFs.SMsgAVIoctrlGetMotionDetectReq.parseContent(mDevice.ChannelIndex));
		}
	}

	private void initFTPMail() {
		btnFTP.setEnabled(false);
		btnMail.setEnabled(false);
		tvFTP.setTextColor(getResources().getColor(R.color.settings_gray));
		tvMail.setTextColor(getResources().getColor(R.color.settings_gray));

		if (mCamera != null) {
			mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETGUARD_REQ,
					AVIOCTRLDEFs.SMsgAVIoctrlGetGuardReq.parseContent(mDevice.ChannelIndex));
			mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_FTP_REQ,
					AVIOCTRLDEFs.SMsgAVIoctrlGetFtpReq.parseContent(mDevice.ChannelIndex));
			mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_SMTP_REQ,
					AVIOCTRLDEFs.SMsgAVIoctrlExGetSmtpReq.parseContent(mDevice.ChannelIndex));
		}
	}

	private static String getString(byte[] data) {

		StringBuilder sBuilder = new StringBuilder();

		for (int i = 0; i < data.length; i++) {

			if (data[i] == 0x0)
				break;

			sBuilder.append((char) data[i]);
		}

		return sBuilder.toString();
	}

	@Override
	public void receiveFrameDataForMediaCodec(Camera camera, int avChannel, byte[] buf, int length, int pFrmNo, byte[] pFrmInfoBuf, boolean isIframe,
			int codecId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void ok() {
		Bundle extras = new Bundle();
		extras.putString("dev_uuid", mDevice.UUID);
		extras.putString("dev_uid", mDevice.UID);
		Intent intent = new Intent();
		intent.putExtras(extras);
		EditDeviceActivity.this.setResult(MultiViewActivity.REQUEST_CODE_CAMERA_EDIT_DELETE_OK, intent);
		EditDeviceActivity.this.finish();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}
}
