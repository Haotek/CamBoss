package com.tutk.Kalay.settings;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.P2PCam264.DeviceInfo;
import com.tutk.P2PCam264.MyCamera;
import com.tutk.P2PCam264.DELUX.MultiViewActivity;
import com.tutk.Kalay.general.R;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class FTPSettingsActivity extends Activity {

	private MyCamera mCamera;
	private DeviceInfo mDevice;

	private EditText etServer;
	private EditText etPort;
	private EditText etAcc;
	private EditText etPwd;
	private EditText etPath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.titlebar);
		TextView tv = (TextView) this.findViewById(R.id.bar_text);
		tv.setText(getText(R.string.txtFTPSettings));
		Button btnOK = (Button) this.findViewById(R.id.bar_right_btn);
		Button btnCancel = (Button) this.findViewById(R.id.bar_left_btn);
		btnOK.setText(getString(R.string.ok));
		btnOK.setTextColor(Color.WHITE);
		btnOK.setVisibility(View.VISIBLE);
		btnCancel.setText(getString(R.string.cancel));
		btnCancel.setTextColor(Color.WHITE);
		btnCancel.setVisibility(View.VISIBLE);

		setContentView(R.layout.ftp_settings);

		Bundle bundle = getIntent().getExtras();
		String devUUID = bundle.getString("dev_uuid");
		String devUID = bundle.getString("dev_uid");
		String ftp_server = bundle.getString("server");
		int ftp_port = bundle.getInt("port");
		String ftp_usr = bundle.getString("usr");
		String ftp_pwd = bundle.getString("pwd");
		String ftp_path = bundle.getString("path");
		int ftp_mode = bundle.getInt("mode");
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

		etServer = (EditText) findViewById(R.id.etServer);
		etPort = (EditText) findViewById(R.id.etPort);
		etAcc = (EditText) findViewById(R.id.etAcc);
		etPwd = (EditText) findViewById(R.id.etPwd);
		etPath = (EditText) findViewById(R.id.etPath);

		etServer.setText(ftp_server);
		etPort.setText(ftp_port + "");
		etAcc.setText(ftp_usr);
		etPwd.setText(ftp_pwd);
		etPath.setText(ftp_path);

		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
				overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
			}
		});

		btnOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCamera != null && mDevice != null) {
					mCamera.sendIOCtrl(mDevice.ChannelIndex, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_FTP_REQ, AVIOCTRLDEFs.SMsgAVIoctrlSetFtpReq
							.parseContent(mDevice.ChannelIndex, etServer.getText().toString(), Integer.parseInt(etPort.getText().toString()), etAcc
									.getText().toString(), etPwd.getText().toString(), etPath.getText().toString(), 0));

					setResult(RESULT_OK);
					finish();
					overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
				}
			}
		});
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
