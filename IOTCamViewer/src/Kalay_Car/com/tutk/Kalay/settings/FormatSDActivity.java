package com.tutk.Kalay.settings;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.P2PCam264.MyCamera;
import com.tutk.P2PCam264.DELUX.Custom_OkCancle_Dialog;
import com.tutk.P2PCam264.DELUX.Custom_OkCancle_Dialog.OkCancelDialogListener;
import com.tutk.P2PCam264.DELUX.MultiViewActivity;
import com.tutk.Kalay.general.R;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class FormatSDActivity extends Activity implements OkCancelDialogListener {

	private MyCamera mCamera;

	private ImageButton btnFormat;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.titlebar);
		TextView tv = (TextView) this.findViewById(R.id.bar_text);
		tv.setText(getText(R.string.btnFormatSDCard));

		setContentView(R.layout.format_sd);

		Custom_OkCancle_Dialog.SetDialogListener(this);
		String devUUID = getIntent().getStringExtra("dev_uuid");
		String devUID = getIntent().getStringExtra("dev_uid");
		for (MyCamera camera : MultiViewActivity.CameraList) {

			if (devUUID.equalsIgnoreCase(camera.getUUID()) && devUID.equalsIgnoreCase(camera.getUID())) {
				mCamera = camera;
				break;
			}
		}

		btnFormat = (ImageButton) findViewById(R.id.btnFormat);
		btnFormat.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Custom_OkCancle_Dialog dlg = new Custom_OkCancle_Dialog(FormatSDActivity.this, getText(R.string.tips_format_sdcard_confirm)
						.toString());
				dlg.setCanceledOnTouchOutside(false);
				Window window = dlg.getWindow();
				window.setWindowAnimations(R.style.setting_dailog_animstyle);
				dlg.show();
			}
		});
	}

	@Override
	public void ok() {
		if (mCamera != null)
			mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_FORMATEXTSTORAGE_REQ,
					AVIOCTRLDEFs.SMsgAVIoctrlFormatExtStorageReq.parseContent(0));
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub

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
