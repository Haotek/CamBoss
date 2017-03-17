package com.tutk.Kalay.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.IRegisterIOTCListener;
import com.tutk.Kalay.general.R;
import com.tutk.P2PCam264.DELUX.Custom_Ok_Dialog;
import com.tutk.P2PCam264.DELUX.MultiViewActivity;
import com.tutk.P2PCam264.DeviceInfo;
import com.tutk.P2PCam264.MyCamera;

import java.util.Timer;
import java.util.TimerTask;

import general.DatabaseManager;

public class SecurityPasswordActivity extends Activity implements IRegisterIOTCListener, Custom_Ok_Dialog.DialogListener {

	private final int TIME_OUT = 60000;
	private final int DIALOG_BACK = 0;

	private MyCamera mCamera;
	private DeviceInfo mDevice;
	private Timer timer = new Timer();
	private TimerTask timerTask;

	private EditText etOld;
	private EditText etNew;
	private EditText etConfirm;
	private Button btnOK;
	private Button btnCancel;
	private LinearLayout layoutMask;

	private boolean isChangingPwd = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
		actionBar.setCustomView(R.layout.titlebar);
		TextView tv = (TextView)this.findViewById(R.id.bar_text);
		tv.setText(getText(R.string.txtSecuritySetting));	
		btnOK = (Button) this.findViewById(R.id.bar_right_btn);
		btnCancel = (Button) this.findViewById(R.id.bar_left_btn);
		btnOK.setText(getString(R.string.ok));
		btnOK.setTextColor(Color.WHITE);
		btnOK.setVisibility(View.VISIBLE);
		btnCancel.setText(getString(R.string.cancel));
		btnCancel.setTextColor(Color.WHITE);
		btnCancel.setVisibility(View.VISIBLE);
		btnOK.setOnClickListener(clickOK);
		btnCancel.setOnClickListener(clickCancel);
		
		setContentView(R.layout.security_pwd);	
		
		etOld = (EditText) findViewById(R.id.edtOldPwd);
		etNew = (EditText) findViewById(R.id.edtNewPwd);
		etConfirm = (EditText) findViewById(R.id.edtConfirmPwd);
		layoutMask = (LinearLayout) findViewById(R.id.layoutMask);
		
		String devUUID = getIntent().getStringExtra("dev_uuid");
		String devUID = getIntent().getStringExtra("dev_uid");
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
	}

	private void ChangePwd() {
		String oldPwd = etOld.getText().toString();
		String newPwd = etNew.getText().toString();
		String confirmPwd = etConfirm.getText().toString();

		if (oldPwd.length() == 0 || newPwd.length() == 0 || confirmPwd.length() == 0) {
			Custom_Ok_Dialog dlg = new Custom_Ok_Dialog(this, getText(R.string.tips_all_field_can_not_empty).toString(), getText(R.string.ok)
					.toString());
			dlg.setCanceledOnTouchOutside(false);
			Window window = dlg.getWindow();
			window.setWindowAnimations(R.style.setting_dailog_animstyle);
			dlg.show();
			return;
		}

		if (!oldPwd.equalsIgnoreCase(mDevice.View_Password)) {
			Custom_Ok_Dialog dlg = new Custom_Ok_Dialog(this, getText(R.string.tips_old_password_is_wrong).toString(), getText(R.string.ok)
					.toString());
			dlg.setCanceledOnTouchOutside(false);
			Window window = dlg.getWindow();
			window.setWindowAnimations(R.style.setting_dailog_animstyle);
			dlg.show();
			return;
		}

		if (!newPwd.equalsIgnoreCase(confirmPwd)) {
			Custom_Ok_Dialog dlg = new Custom_Ok_Dialog(this, getText(R.string.tips_new_passwords_do_not_match).toString(), getText(R.string.ok)
					.toString());
			dlg.setCanceledOnTouchOutside(false);
			Window window = dlg.getWindow();
			window.setWindowAnimations(R.style.setting_dailog_animstyle);
			dlg.show();
			return;
		}

		if (mCamera != null)
			mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETPASSWORD_REQ,
					AVIOCTRLDEFs.SMsgAVIoctrlSetPasswdReq.parseContent(oldPwd, newPwd));

		btnOK.setEnabled(false);
		btnCancel.setEnabled(false);
		layoutMask.setVisibility(View.VISIBLE);
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		isChangingPwd = true;

		timerTask = new TimerTask() {
			@Override
			public void run () {
				if (mCamera != null) {
					mCamera.disconnect();
					if (mCamera != null) {
						mCamera.unregisterIOTCListener(SecurityPasswordActivity.this);
					}
				}

				runOnUiThread(new Runnable() {
					@Override
					public void run () {
						Custom_Ok_Dialog dlg = new Custom_Ok_Dialog(SecurityPasswordActivity.this, getText(R.string.tips_change_pwd_timeout)
								.toString(), getText(R.string.ok).toString(), DIALOG_BACK);
						dlg.setCanceledOnTouchOutside(false);
						Window window = dlg.getWindow();
						window.setWindowAnimations(R.style.setting_dailog_animstyle);
						dlg.show();
					}
				});
			}
		};
		timer.schedule(timerTask, TIME_OUT);
	}

	private OnClickListener clickOK = new OnClickListener() {

		@Override
		public void onClick(View v) {
			ChangePwd();
		}
	};

	private OnClickListener clickCancel = new OnClickListener() {

		@Override
		public void onClick(View v) {
			setResult(RESULT_CANCELED);
			finish();
			overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
		}
	};

	@Override
	public boolean onKeyDown (int keyCode, KeyEvent event) {

		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				if (isChangingPwd) {
					return false;
				}

				finish();
				overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
				return false;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume () {
		super.onResume();
		if (mCamera != null) {
			mCamera.registerIOTCListener(this);
		}
		Custom_Ok_Dialog.registDialogListener(this);
	}

	@Override
	protected void onPause () {
		super.onPause();
		if (mCamera != null) {
			mCamera.unregisterIOTCListener(this);
		}

		if (timerTask != null) {
			timerTask.cancel();
		}
	}

	@Override
	public void receiveFrameData (Camera camera, int avChannel, Bitmap bmp) {

	}

	@Override
	public void receiveFrameDataForMediaCodec (Camera camera, int avChannel, byte[] buf, int length, int pFrmNo, byte[] pFrmInfoBuf, boolean
			isIframe, int codecId) {

	}

	@Override
	public void receiveFrameInfo (Camera camera, int avChannel, long bitRate, int frameRate, int onlineNm, int frameCount, int incompleteFrameCount) {

	}

	@Override
	public void receiveSessionInfo (Camera camera, int resultCode) {

	}

	@Override
	public void receiveChannelInfo (Camera camera, int avChannel, int resultCode) {

	}

	@Override
	public void receiveIOCtrlData (Camera camera, int avChannel, int avIOCtrlMsgType, byte[] data) {
		if (camera == mCamera && avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETPASSWORD_RESP) {
			runOnUiThread(new Runnable() {
				@Override
				public void run () {
					Toast.makeText(SecurityPasswordActivity.this, getText(R.string.tips_modify_security_code_ok).toString(), Toast.LENGTH_SHORT)
							.show();

					String newPwd = etNew.getText().toString();
					mDevice.View_Password = newPwd;

					DatabaseManager manager = new DatabaseManager(SecurityPasswordActivity.this);
					manager.updateDeviceInfoByDBID(mDevice.DBID, mDevice.UID, mDevice.NickName, "", "", mDevice.View_Account, mDevice
							.View_Password, mDevice.EventNotification, mDevice.ChannelIndex);

					Intent intent = new Intent();
					intent.putExtra("new", newPwd);
					setResult(RESULT_OK, intent);
					finish();
					overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
				}
			});
		}
	}

	@Override
	public void click (int request) {
		if(request == DIALOG_BACK) {
			setResult(RESULT_CANCELED);
			finish();
			overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
		}
	}
}
