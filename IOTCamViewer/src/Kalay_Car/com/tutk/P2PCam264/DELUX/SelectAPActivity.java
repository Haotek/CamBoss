package com.tutk.P2PCam264.DELUX;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.Kalay.general.R;

import java.util.List;

import appteam.WifiAdmin;

public class SelectAPActivity extends Activity {
	
	private ListView lvNickname;	
	private SSIDAdapter mAdapter;
	
	private WifiAdmin WifiAdmin;
    private List<ScanResult> mWiFiList;
	private String mSSID;
    private String mAP;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
		actionBar.setCustomView(R.layout.titlebar);
		TextView tv = (TextView)this.findViewById(R.id.bar_text);
		tv.setText("Wi-Fi");
		
		setContentView(R.layout.nickname_list);
		
		lvNickname = (ListView) findViewById(R.id.lstNickname);
		
		mSSID = getIntent().getStringExtra("ssid");
        mAP = getIntent().getStringExtra("ap");
		
		WifiAdmin = new WifiAdmin(this);
		WifiAdmin.startScan();
        mWiFiList = WifiAdmin.getWifiList();
        for(int i = 0; i < mWiFiList.size(); i ++){
            if(mWiFiList.get(i).SSID.equals(mAP)){
                mWiFiList.remove(i);
                break;
            }
        }

		mAdapter = new SSIDAdapter(this);
		lvNickname.setAdapter(mAdapter);
		lvNickname.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mSSID = WifiAdmin.getWifiList().get(position).SSID;
				String enc = WifiAdmin.getWifiList().get(position).capabilities;
				int wifi_enc = AVIOCTRLDEFs.AVIOTC_WIFIAPENC_NONE;
				if (enc.length() != 0) {
					if (enc.contains("WPA2")) {
						wifi_enc = AVIOCTRLDEFs.AVIOTC_WIFIAPENC_WPA2_AES;
					} else {
						wifi_enc = AVIOCTRLDEFs.AVIOTC_WIFIAPENC_WPA_AES;
					}
				}
				Intent intent = new Intent();
				intent.putExtra("SSID", mSSID);
				intent.putExtra("enc", wifi_enc);
				setResult(RESULT_OK, intent);
				finish();
				overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
			}
		});
		
	}
	
	public class SSIDAdapter extends BaseAdapter{
		
		private LayoutInflater mInflater;
		
		public SSIDAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);			
		}

		@Override
		public int getCount() {
			if(WifiAdmin.getWifiList() != null) {
				return WifiAdmin.getWifiList().size();
			}else{
				return 0;
			}
		}

		@Override
		public Object getItem(int position) {
			return mWiFiList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return -1;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			if (mWiFiList == null)
				return null;
			
			ViewHolder holder = null;

			if (convertView == null) {

				convertView = mInflater.inflate(R.layout.item_nickname, null);

				holder = new ViewHolder();
				holder.Nickname = (TextView) convertView.findViewById(R.id.txtName);
				holder.imgCheck = (ImageView) convertView.findViewById(R.id.imgRight);
				convertView.setTag(holder);

			} else {

				holder = (ViewHolder) convertView.getTag();
			}
			
			if (holder != null) {
				holder.Nickname.setText(mWiFiList.get(position).SSID);
				if(mSSID.equals(mWiFiList.get(position).SSID))
					holder.imgCheck.setVisibility(View.VISIBLE);
				else
					holder.imgCheck.setVisibility(View.GONE);
			}

			return convertView;
		}
		
		public class ViewHolder {
			public TextView Nickname;
			public ImageView imgCheck;
		}
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
