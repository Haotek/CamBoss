package com.tutk.Kalay.settings;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.Logger.Glog;
import com.tutk.P2PCam264.EditDeviceActivity;
import com.tutk.P2PCam264.MyCamera;
import com.tutk.P2PCam264.DELUX.Custom_Pwd_Dialog;
import com.tutk.P2PCam264.DELUX.Custom_Pwd_Dialog.PwdDialogListener;
import com.tutk.P2PCam264.DELUX.MultiViewActivity;

import com.tutk.Kalay.general.R;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import java.lang.reflect.Method;

public class SetWiFiActivity extends Activity implements PwdDialogListener {

	private MyCamera mCamera;

	private ListView list;

	private String[] arySSID = null;
	private int choice = -1;
	private Boolean isHotspot = false;
	private String hotspotSSID = null;
	private WifiConfiguration myHotspot = null;
	private WifiManager myWifiMgr = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.titlebar);
		TextView tv = (TextView) this.findViewById(R.id.bar_text);
		tv.setText(getText(R.string.txtWiFiSetting));

		setContentView(R.layout.time_zone_settings);

		Custom_Pwd_Dialog.SetDialogListener(this);
		String devUUID = getIntent().getStringExtra("dev_uuid");
		String devUID = getIntent().getStringExtra("dev_uid");
		for (MyCamera camera : MultiViewActivity.CameraList) {

			if (devUUID.equalsIgnoreCase(camera.getUUID()) && devUID.equalsIgnoreCase(camera.getUID())) {
				mCamera = camera;
				break;
			}
		}

		myWifiMgr = (WifiManager) this.getSystemService(this.WIFI_SERVICE);
		int ssidOffset = 0;
		try {
			Method method = myWifiMgr.getClass().getDeclaredMethod("getWifiApConfiguration");
			method.setAccessible(true);
			myHotspot = (WifiConfiguration) method.invoke(myWifiMgr);
			if ( myHotspot  != null) {
				hotspotSSID = myHotspot.SSID;
			}
		}
		catch (Throwable ignored) {}

		if ( hotspotSSID == null ) {
			arySSID = new String[EditDeviceActivity.m_wifiList.size()];
		}
		else {
			arySSID = new String[EditDeviceActivity.m_wifiList.size() + 1];
			arySSID[0] = hotspotSSID + " (Hotspot)".toString();
			ssidOffset = 1;
		}

		for (int i = ssidOffset; i < EditDeviceActivity.m_wifiList.size(); i++) {
			Glog.I("P2PCamLive", "Wifi List[" +i +"]: SSID: " + getString(EditDeviceActivity.m_wifiList.get(i).ssid) + ", ENCTYPE: " + String.format("0x%x",EditDeviceActivity.m_wifiList.get(i).enctype) + ", Mode:" + String.format("0x%x",EditDeviceActivity.m_wifiList.get(i).mode));
			arySSID[i] = getString(EditDeviceActivity.m_wifiList.get(i).ssid);
		}

		list = (ListView) findViewById(R.id.lvTimeZone);
		NicnameAdapter adapter = new NicnameAdapter(this);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				choice = position;
				Custom_Pwd_Dialog dlg = new Custom_Pwd_Dialog(SetWiFiActivity.this, getText(R.string.txt_pls_enter_pwd).toString());
				dlg.setCanceledOnTouchOutside(false);
				Window window = dlg.getWindow();
				window.setWindowAnimations(R.style.setting_dailog_animstyle);
				dlg.show();
			}
		});

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

	public class NicnameAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public NicnameAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return arySSID.length;
		}

		@Override
		public Object getItem(int position) {
			return arySSID[position];
		}

		@Override
		public long getItemId(int position) {
			return -1;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder = null;

			if (convertView == null) {

				convertView = mInflater.inflate(R.layout.item_nickname, null);

				holder = new ViewHolder();
				holder.Nickname = (TextView) convertView.findViewById(R.id.txtName);
				convertView.setTag(holder);

			} else {

				holder = (ViewHolder) convertView.getTag();
			}

			if (holder != null) {
				holder.Nickname.setText(arySSID[position]);
			}

			return convertView;
		}

		public class ViewHolder {
			public TextView Nickname;
		}
	}

	@Override
	public void ok(String password) {
		AVIOCTRLDEFs.SWifiAp wifi = EditDeviceActivity.m_wifiList.get(choice);

		isHotspot = false;
		if ( hotspotSSID != null && choice == 0 ) {
			isHotspot = true;
			wifi.ssid = hotspotSSID.getBytes();
			Glog.I("P2PCamLive","Wifi Mode: " + wifi.mode);
			wifi.mode = AVIOCTRLDEFs.AVIOTC_WIFIAPMODE_MANAGED;
			wifi.enctype = AVIOCTRLDEFs.AVIOTC_WIFIAPENC_WPA_PSK_AES;
			if (myHotspot.allowedGroupCiphers.get(WifiConfiguration.GroupCipher.CCMP)) {
				Glog.I("P2PCamLive", "Ciphers AES");
				wifi.enctype = AVIOCTRLDEFs.AVIOTC_WIFIAPENC_WPA_PSK_AES;
			} else if (myHotspot.allowedGroupCiphers.get(WifiConfiguration.GroupCipher.TKIP)) {
				Glog.I("P2PCamLive", "Ciphers TKIP");
				wifi.enctype = AVIOCTRLDEFs.AVIOTC_WIFIAPENC_WPA_PSK_TKIP;
			}
		}
		MultiViewActivity.noResetWiFi = false;

		if (mCamera != null) {
			if ( isHotspot ) {
				mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETWIFI_REQ,
						AVIOCTRLDEFs.SMsgAVIoctrlSetWifiReq.parseContent(wifi.ssid, password.getBytes(), wifi.mode, wifi.enctype, "hotspot".getBytes()));
			} else {
				mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETWIFI_REQ,
						AVIOCTRLDEFs.SMsgAVIoctrlSetWifiReq.parseContent(wifi.ssid, password.getBytes(), wifi.mode, wifi.enctype));
			}
		}

		if ( isHotspot ) {
			try {
				if(myWifiMgr.isWifiEnabled())
				{
					myWifiMgr.setWifiEnabled(false);
				}
//				myHotspot.preSharedKey = password;
//				WifiConfiguration hotspotConf = new WifiConfiguration();
//				hotspotConf.SSID = hotspotSSID;
//				hotspotConf.preSharedKey = password;
//				hotspotConf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
//				hotspotConf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
//				hotspotConf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
//				hotspotConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//				hotspotConf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
//				hotspotConf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
//				hotspotConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//				hotspotConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
				Method method = myWifiMgr.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
				if ( method != null ) {
					Object o = method.invoke(myWifiMgr, myHotspot, true);
					Glog.I("P2PCamLive", "Invokde setWifiAppEnabled: " + (Boolean) o);
				}
			} catch (Exception e) {
				Glog.I("P2PCamLive","setWifiApEnabled Error: " + e.getMessage() +"," + e.getLocalizedMessage());
			}
		}

		Intent intent = new Intent();
		intent.putExtra("ssid", arySSID[choice]);
		intent.putExtra("isHotspot",isHotspot);
		setResult(RESULT_OK, intent);
		finish();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
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
