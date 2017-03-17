package com.tutk.P2PCam264.DELUX;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.tutk.P2PCam264.DeviceInfo;
import com.tutk.P2PCam264.EventListActivity;
import com.tutk.P2PCam264.GridViewGalleryActivity;
import com.tutk.Kalay.general.R;

import java.io.File;

public class NicknameListActivity extends Activity {
	
	public static int GALLERY_PHOTO = 0;
	public static int GALLERY_VIDEO = 1;
	
	private ListView lvNickname;
//	private RelativeLayout btn_change_mode;
//	private Button btnPhoto;
//	private Button btnVideo;
	
	private NicnameAdapter mAdapter;
	private int mMode;
	private int mGalleryMode = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
		actionBar.setCustomView(R.layout.titlebar);
		TextView tv = (TextView)this.findViewById(R.id.bar_text);
//		btn_change_mode = (RelativeLayout) findViewById(R.id.bar_gallery);
//		btnPhoto = (Button) findViewById(R.id.bar_btn_photo);
//		btnVideo = (Button) findViewById(R.id.bar_btn_video);
		
		mMode = getIntent().getIntExtra("mode", -1);
		switch(mMode){
		case MultiViewActivity.MODE_EVENT:
			tv.setText(getText(R.string.ctxViewEvent));
			break;
		case MultiViewActivity.MODE_GALLERY:
			tv.setText(getText(R.string.ctxViewSnapshot));
//			btn_change_mode.setVisibility(View.VISIBLE);
//			btnPhoto.setBackgroundResource(R.drawable.btn_tabl_h);
//			btnPhoto.setTextColor(Color.BLACK);
//			btnPhoto.setOnClickListener(mode_change);
//			btnVideo.setOnClickListener(mode_change);
			break;
		}
		
		setContentView(R.layout.nickname_list);
		
		lvNickname = (ListView) findViewById(R.id.lstNickname);
		
		mAdapter = new NicnameAdapter(this);
		lvNickname.setAdapter(mAdapter);
		lvNickname.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch(mMode){
				case MultiViewActivity.MODE_EVENT:
					Bundle extras = new Bundle();
					extras.putString("dev_uid", MultiViewActivity.DeviceList.get(position).UID);
					extras.putString("dev_uuid", MultiViewActivity.DeviceList.get(position).UUID);
					extras.putString("dev_nickname", MultiViewActivity.DeviceList.get(position).NickName);
					extras.putString("conn_status", MultiViewActivity.DeviceList.get(position).Status);
					extras.putString("view_acc", MultiViewActivity.DeviceList.get(position).View_Account);
					extras.putString("view_pwd", MultiViewActivity.DeviceList.get(position).View_Password);
					extras.putInt("camera_channel", 0);
					Intent intent = new Intent();
					intent.putExtras(extras);
					intent.setClass(NicknameListActivity.this, EventListActivity.class);
					startActivity(intent);
					overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
					break;
					
				case MultiViewActivity.MODE_GALLERY:
					File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Snapshot/" + MultiViewActivity.DeviceList.get(position).UID);
					File folder_video = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Record/" + MultiViewActivity.DeviceList.get(position).UID);
					String[] allFiles = folder.list();
					String[] allVideos = folder_video.list();
					Intent intent2 = new Intent(NicknameListActivity.this, GridViewGalleryActivity.class);
					intent2.putExtra("snap", MultiViewActivity.DeviceList.get(position).UID);
					intent2.putExtra("images_path", folder.getAbsolutePath());
					intent2.putExtra("videos_path", folder_video.getAbsolutePath());
					intent2.putExtra("mode", mGalleryMode);
					startActivity(intent2);
					overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

					break;
				}
			}
		});
		
	}
	
	public class NicnameAdapter extends BaseAdapter{
		
		private LayoutInflater mInflater;
		
		public NicnameAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return MultiViewActivity.DeviceList.size();
		}

		@Override
		public Object getItem(int position) {
			return MultiViewActivity.DeviceList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return -1;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			DeviceInfo dev = MultiViewActivity.DeviceList.get(position);
			
			if (dev == null)
				return null;
			
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
				holder.Nickname.setText(dev.NickName);
			}

			return convertView;
		}
		
		public class ViewHolder {
			public TextView Nickname;
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
//	private OnClickListener mode_change = new OnClickListener() {
//		@Override
//		public void onClick(View v) {
//			switch(v.getId()){
//			case R.id.bar_btn_photo:
//				mGalleryMode = GALLERY_PHOTO;
//				mode_change();
//				break;
//
//			case R.id.bar_btn_video:
//				mGalleryMode = GALLERY_VIDEO;
//				mode_change();
//				break;
//			}
//		}
//	};
	
//	private void mode_change(){
//		if (mGalleryMode == GALLERY_VIDEO) {
//			btnPhoto.setBackgroundResource(R.drawable.btn_photo);
//			try {
//				btnPhoto.setTextColor(ColorStateList.createFromXml(getResources(), getResources().getXml(R.drawable.txt_color_gallery)));
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			btnVideo.setBackgroundResource(R.drawable.btn_tabr_h);
//			btnVideo.setTextColor(Color.BLACK);
//		} else {
//			btnVideo.setBackgroundResource(R.drawable.btn_video);
//			try {
//				btnVideo.setTextColor(ColorStateList.createFromXml(getResources(), getResources().getXml(R.drawable.txt_color_gallery)));
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			btnPhoto.setBackgroundResource(R.drawable.btn_tabl_h);
//			btnPhoto.setTextColor(Color.BLACK);
//		}
//	}
	
}
