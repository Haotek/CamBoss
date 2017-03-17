package com.tutk.Kalay.settings;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.IRegisterIOTCListener;
import com.tutk.IOTC.Packet;
import com.tutk.P2PCam264.MyCamera;
import com.tutk.P2PCam264.DELUX.MultiViewActivity;
import com.tutk.Kalay.general.R;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

public class DeviceInfoActivity extends Activity implements IRegisterIOTCListener {
	
	private MyCamera mCamera;
	
	private TextView txtDeviceModel;
	private TextView txtDeviceVersion;
	private TextView txtVenderName;
	private TextView txtStorageTotalSize;
	private TextView txtStorageFreeSize;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
		actionBar.setCustomView(R.layout.titlebar);
		TextView tv = (TextView)this.findViewById(R.id.bar_text);
		tv.setText(getText(R.string.txtDeviceInfo));
		
		setContentView(R.layout.device_info);
		
		String devUUID = getIntent().getStringExtra("dev_uuid");
		String devUID = getIntent().getStringExtra("dev_uid");
		for (MyCamera camera : MultiViewActivity.CameraList) {

			if (devUUID.equalsIgnoreCase(camera.getUUID()) && devUID.equalsIgnoreCase(camera.getUID())) {
				mCamera = camera;
				mCamera.registerIOTCListener(this);
				break;
			}
		}
		
		txtDeviceModel = (TextView) findViewById(R.id.txtDeviceModel);
		txtDeviceVersion = (TextView) findViewById(R.id.txtDeviceVersion);
		txtVenderName = (TextView) findViewById(R.id.txtVenderName);
		txtStorageTotalSize = (TextView) findViewById(R.id.txtStorageTotalSize);
		txtStorageFreeSize = (TextView) findViewById(R.id.txtStorageFreeSize);
		
		initDeviceInfo();

		
	}
	
	private void initDeviceInfo() {

		txtDeviceModel.setText(getText(R.string.tips_wifi_retrieving));
		txtDeviceVersion.setText(getText(R.string.tips_wifi_retrieving));
		txtVenderName.setText(getText(R.string.tips_wifi_retrieving));
		txtStorageTotalSize.setText(getText(R.string.tips_wifi_retrieving));
		txtStorageFreeSize.setText(getText(R.string.tips_wifi_retrieving));

		if (mCamera != null) {
			mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_REQ,
					AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoReq.parseContent());
		}
	}

	@Override
	protected void onDestroy() {
		mCamera.unregisterIOTCListener(this);
		super.onDestroy();
	}

	@Override
	public void receiveFrameData(Camera camera, int avChannel, Bitmap bmp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveFrameDataForMediaCodec(Camera camera, int avChannel, byte[] buf, int length, int pFrmNo, byte[] pFrmInfoBuf, boolean isIframe, int codecId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveFrameInfo(Camera camera, int avChannel, long bitRate, int frameRate, int onlineNm, int frameCount, int incompleteFrameCount) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveSessionInfo(Camera camera, int resultCode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveChannelInfo(Camera camera, int avChannel, int resultCode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveIOCtrlData(Camera camera, int avChannel, int avIOCtrlMsgType, byte[] data) {
		if(avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_RESP){
			byte[] bytModel = new byte[16];
			byte[] bytVender = new byte[16];
			System.arraycopy(data, 0, bytModel, 0, 16);
			System.arraycopy(data, 16, bytVender, 0, 16);

			final String model = getString(bytModel);
			final String vender = getString(bytVender);
			final int version = Packet.byteArrayToInt_Little(data, 32);
			final int free = Packet.byteArrayToInt_Little(data, 44);
			final int TotalSize = Packet.byteArrayToInt_Little(data, 40);
			
			runOnUiThread(new Runnable() {				
				@Override
				public void run() {
					txtDeviceModel.setText(model);
					txtDeviceVersion.setText(getVersion(version));
					txtVenderName.setText(vender);
					txtStorageFreeSize.setText(String.valueOf(free) + " MB");
					txtStorageTotalSize.setText(String.valueOf(TotalSize) + " MB");
				}
			});			
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

	private String getVersion(int version) {

		byte[] bytVer = new byte[4];

		StringBuffer sb = new StringBuffer();
		bytVer[3] = (byte) (version);
		bytVer[2] = (byte) (version >>> 8);
		bytVer[1] = (byte) (version >>> 16);
		bytVer[0] = (byte) (version >>> 24);
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
