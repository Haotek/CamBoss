package com.tutk.Kalay.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.Kalay.general.R;
import com.tutk.P2PCam264.DeviceInfo;
import com.tutk.P2PCam264.MyCamera;

import com.tutk.P2PCam264.DELUX.MultiViewActivity;

public class MailSettingsActivity extends Activity {

	private final int PROTOCOL_NONE = 0;
	private final int PROTOCOL_TLS = 1;
	private final int PROTOCOL_STARTLS = 2;

	private MyCamera mCamera;
	private DeviceInfo mDevice;

	private EditText etSender;
	private EditText etReceiver;
	private EditText etServer;
	private EditText etAcc;
	private EditText etPwd;
	private EditText etPort;
	private Spinner spProtocol;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.titlebar);
		TextView tv = (TextView) this.findViewById(R.id.bar_text);
		tv.setText(getText(R.string.txtMailSettings));
		Button btnOK = (Button) this.findViewById(R.id.bar_right_btn);
		Button btnCancel = (Button) this.findViewById(R.id.bar_left_btn);
		btnOK.setText(getString(R.string.ok));
		btnOK.setTextColor(Color.WHITE);
		btnOK.setVisibility(View.VISIBLE);
		btnCancel.setText(getString(R.string.cancel));
		btnCancel.setTextColor(Color.WHITE);
		btnCancel.setVisibility(View.VISIBLE);

		setContentView(R.layout.mail_settings);

		Bundle bundle = getIntent().getExtras();
		String devUUID = bundle.getString("dev_uuid");
		String devUID = bundle.getString("dev_uid");
		String mail_sender = bundle.getString("sender");
		String mail_receiver = bundle.getString("receiver");
		String mail_server = bundle.getString("server");
		int mail_port = bundle.getInt("port");
		int mail_protocol = bundle.getInt("protocol");
		String mail_usr = bundle.getString("usr");
		String mail_pwd = bundle.getString("pwd");
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

		etSender = (EditText) findViewById(R.id.etSender);
		etReceiver = (EditText) findViewById(R.id.etReceiver);
		etServer = (EditText) findViewById(R.id.etServer);
		etAcc = (EditText) findViewById(R.id.etAcc);
		etPwd = (EditText) findViewById(R.id.etPwd);
		etPort = (EditText) findViewById(R.id.etPort);
		spProtocol = (Spinner) findViewById(R.id.spProtocol);

		etSender.setText(mail_sender);
		etReceiver.setText(mail_receiver);
		etServer.setText(mail_server);
		etAcc.setText(mail_usr);
		etPwd.setText(mail_pwd);
		etPort.setText(mail_port + "");

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.mail_protocol, R.layout.myspinner_title);
		adapter.setDropDownViewResource(R.layout.myspinner);
		spProtocol.setAdapter(adapter);
		switch (mail_protocol) {
		case PROTOCOL_NONE:
			spProtocol.setSelection(PROTOCOL_NONE);
			break;
		case PROTOCOL_TLS:
			spProtocol.setSelection(PROTOCOL_TLS);
			break;
		case PROTOCOL_STARTLS:
			spProtocol.setSelection(PROTOCOL_STARTLS);
			break;
		}

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
					mCamera.sendIOCtrl(mCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_SMTP_REQ, AVIOCTRLDEFs.SMsgAVIoctrlExSetSmtpReq
							.parseContent(mDevice.ChannelIndex, etSender.getText().toString(), etReceiver.getText().toString(), etServer.getText()
									.toString(), Integer.parseInt(etPort.getText().toString()), spProtocol.getSelectedItemPosition(), etAcc.getText()
									.toString(), etPwd.getText().toString()));

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
