package com.tutk.Kalay.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.Kalay.general.R;
import com.tutk.P2PCam264.DELUX.MultiViewActivity;
import com.tutk.P2PCam264.MyCamera;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TimeZoneActivity extends Activity {

	private MyCamera mCamera;
	
	private ListView list;

	private String[] timeZoneList = null;
	private String[] timeZoneNameList = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.titlebar);
		TextView tv = (TextView) this.findViewById(R.id.bar_text);
		tv.setText(getText(R.string.txtTimeZoneSetting));

		setContentView(R.layout.time_zone_settings);

		String devUUID = getIntent().getStringExtra("dev_uuid");
		String devUID = getIntent().getStringExtra("dev_uid");
		for (MyCamera camera : MultiViewActivity.CameraList) {

			if (devUUID.equalsIgnoreCase(camera.getUUID()) && devUID.equalsIgnoreCase(camera.getUID())) {
				mCamera = camera;
				break;
			}
		}
		
		getTimeZoneCSV();
		list = (ListView) findViewById(R.id.lvTimeZone);
		TimeZoneAdapter adapter = new TimeZoneAdapter(this);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				quit(position);
			}			
		});

	}

	private void getTimeZoneCSV() {

		String[] fileName = { "timeZone.csv" };
		int nCount[] = { 0, 0, 0, 0, 0, 0 };

		for (int i = 1; i <= fileName.length; i++) {
			nCount[i] = getConut(fileName[i - 1], nCount[i - 1]);
		}

		timeZoneList = new String[nCount[1]];
		timeZoneNameList = new String[nCount[1]];
		for (int i = 0; i < fileName.length; i++) {
			getCSVdata(fileName[i], nCount[i]);
		}
	}

	private int getConut(String sFileName, int count) {

		try {
			InputStream is = getResources().getAssets().open(sFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));

			try {
				String line;
				while ((line = reader.readLine()) != null) {
					String[] RowData = line.split(",");
					if (!RowData[2].equals("--")) {
						count++;
					}
				}
			} catch (IOException ex) {
				// handle exception
			}

			finally {
				try {
					is.close();
					reader.close();
				} catch (IOException e) {
					// handle exception
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return count;

	}

	private void getCSVdata(String sFileName, int nCount) {

		try {
			InputStream is = getResources().getAssets().open(sFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));

			try {

				String line;

				while ((line = reader.readLine()) != null) {
					String[] RowData = line.split(",");

					if (!RowData[2].equals("--")) {
						timeZoneList[nCount] = RowData[2];
						timeZoneNameList[nCount] = RowData[1];
						nCount++;
					}
				}
			} catch (IOException ex) {
				// handle exception
			}

			finally {
				try {
					is.close();
					reader.close();
				} catch (IOException e) {
					// handle exception
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public class TimeZoneAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public TimeZoneAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return timeZoneNameList.length;
		}

		@Override
		public Object getItem(int position) {
			return timeZoneNameList[position];
		}

		@Override
		public long getItemId(int position) {
			return -1;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder = null;

			if (convertView == null) {

				convertView = mInflater.inflate(R.layout.item_time_zone, null);

				holder = new ViewHolder();
				holder.location = (TextView) convertView.findViewById(R.id.tvName);
				holder.UTC = (TextView) convertView.findViewById(R.id.tvUTC);
				convertView.setTag(holder);

			} else {

				holder = (ViewHolder) convertView.getTag();
			}

			if (holder != null) {
				holder.location.setText(timeZoneNameList[position]);
				holder.UTC.setText(timeZoneList[position]);
			}

			return convertView;
		}

		public class ViewHolder {
			public TextView location;
			public TextView UTC;
		}
	}

	private void quit(int position) {
		byte[] szTimeZoneString = timeZoneNameList[position].getBytes();
		String nTime = timeZoneList[position].substring(4);
		int mtotalMinute = 0;

		if (nTime.indexOf("+") != -1) {
			int nStart = nTime.indexOf("+") + 1;
			int nEnd = nTime.indexOf(":");
			int nHour = Integer.parseInt(nTime.substring(nStart, nEnd));
			int nMinute = Integer.parseInt(nTime.substring(nEnd + 1));
			mtotalMinute = nHour * 60 + nMinute;

		} else {
			int nStart = nTime.indexOf("-") + 1;
			int nEnd = nTime.indexOf(":");
			int nHour = Integer.parseInt(nTime.substring(nStart, nEnd));
			int nMinute = Integer.parseInt(nTime.substring(nEnd + 1));
			mtotalMinute = nHour * (-60) - nMinute;
		}
		
		if(mCamera != null){
			mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_TIMEZONE_REQ,
					AVIOCTRLDEFs.SMsgAVIoctrlTimeZone.parseContent(268, mCamera.getIsSupportTimeZone(), mtotalMinute, szTimeZoneString));
		}
		
		Intent intent = new Intent();
		intent.putExtra("name", timeZoneNameList[position]);
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
