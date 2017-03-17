package com.tutk.P2PCam264.onDropbox;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.TokenPair;
import com.dropbox.client2.session.Session.AccessType;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.IRegisterIOTCListener;
import com.tutk.IOTC.AVIOCTRLDEFs.SMsgAVIoctrlGetDropbox;
import general.DatabaseManager;
import com.tutk.P2PCam264.DeviceInfo;
import com.tutk.P2PCam264.MyCamera;
import com.tutk.P2PCam264.DELUX.MultiViewActivity;
import com.tutk.Kalay.general.R;

public class LinkDropBoxActivity extends SherlockActivity implements IRegisterIOTCListener, View.OnClickListener {

	// dropbox section
	final static private String TAG = "MainActivity";

	final static private String APP_KEY = "zo6kr8w12onxr8c";
	final static private String APP_SECRET = "0xjdiq7mrprnsat";

	final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;

	// You don't need to change these, leave them alone.
	final static private String ACCOUNT_PREFS_NAME = "prefs";
	final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
	final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
	private Button btn_menu = null;

	DropboxAPI<AndroidAuthSession> mApi;
	private ImageButton Link_Button;
	private ListView listView;
	private boolean mLoggedIn;
	private DeviceListAdapter DeviceListAdapter;

	// end dropbox section
	@Override
	public void onCreate(Bundle savedInstanceState) {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.titlebar);
		TextView tv = (TextView) this.findViewById(R.id.bar_text);
		tv.setText(getText(R.string.optDropbox));
		btn_menu = (Button) this.findViewById(R.id.bar_right_btn);
		btn_menu.setVisibility(View.VISIBLE);
		btn_menu.setBackgroundResource(R.drawable.btn_drop_refresh_switch);
		btn_menu.setOnClickListener(this);
		super.onCreate(savedInstanceState);
		// dropbox section
		AndroidAuthSession session = buildSession();
		mApi = new DropboxAPI<AndroidAuthSession>(session);

		setContentView(R.layout.linkdropbox);

		Link_Button = (ImageButton) findViewById(R.id.Linkbutton);
		listView = (ListView) findViewById(R.id.camaralistView);
		DeviceListAdapter = new DeviceListAdapter(LinkDropBoxActivity.this);
		listView.setAdapter(DeviceListAdapter);
		Link_Button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mLoggedIn) {
					logOut();
					// Link_Button.setText(LinkDropBoxActivity.this.getText(R.string.txtlink));
					btn_menu.setVisibility(View.GONE);
					Link_Button.setBackgroundResource(R.drawable.btn_drop_link_switch);

					listView.setVisibility(View.GONE);
				} else {
					// Start the remote authentication
					if (DeviceListAdapter.getCount() == 0) {
						return;
					}
					mApi.getSession().startAuthentication(LinkDropBoxActivity.this);
				}
			}
		});
		checkAppKeySetup();
		initCameraList();
		// Display the proper UI state if logged in or not
		setLoggedIn(mApi.getSession().isLinked());
	}

	private void initCameraList() {
		for (int i = 0; i < MultiViewActivity.DeviceList.size(); i++) {
			MultiViewActivity.CameraList.get(i).registerIOTCListener(this);
			MultiViewActivity.CameraList.get(i).sendIOCtrl(MyCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_SAVE_DROPBOX_REQ,
					"0".getBytes());
			// if(MainActivity.DeviceList.get(i).nSupportDropbox==1)
			// {
			// mCameraList.add(MainActivity.CameraList.get(i));
			// }
			// if(mCameraList = new ArrayList<MyCamera>());
			// if(mDeviceList = Collections.synchronizedList(new ArrayList<DeviceInfo>());
		}
	}

	// dropbox section
	@Override
	protected void onResume() {
		super.onResume();

		AndroidAuthSession session = mApi.getSession();

		// The next part must be inserted in the onResume() method of the
		// activity from which session.startAuthentication() was called, so
		// that Dropbox authentication completes properly.
		if (session.authenticationSuccessful()) {
			try {
				// Mandatory call to complete the auth
				session.finishAuthentication();

				// Store it locally in our app for later use
				TokenPair tokens = session.getAccessTokenPair();
				storeKeys(tokens.key, tokens.secret);
				setLoggedIn(true);
			} catch (IllegalStateException e) {
				// showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
				Log.i(TAG, "Error authenticating", e);
			}
		}
		// else
		// {
		// if(Link_Button!=null)
		// Link_Button.setText(LinkDropBoxActivity.this.getText(R.string.txtlink));
		// listView.setVisibility(View.GONE);

		// }
	}

	// end dropbox section
	@Override
	protected void onDestroy() {
		super.onDestroy();
		quit();
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
	protected void onPause() {
		super.onPause();

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {

		case KeyEvent.KEYCODE_BACK:

			quit();

			break;
		}

		return super.onKeyDown(keyCode, event);
	}

	private void quit() {
		for (int i = 0; i < MultiViewActivity.DeviceList.size(); i++) {
			MultiViewActivity.CameraList.get(i).unregisterIOTCListener(this);
		}
		finish();
		overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
	}

	@Override
	public void receiveFrameData(final Camera camera, int sessionChannel, Bitmap bmp) {
	}

	@Override
	public void receiveSessionInfo(final Camera camera, int resultCode) {
	}

	@Override
	public void receiveChannelInfo(final Camera camera, int sessionChannel, int resultCode) {
	}

	@Override
	public void receiveFrameInfo(final Camera camera, int sessionChannel, long bitRate, int frameRate, int onlineNm, int frameCount,
			int incompleteFrameCount) {

	}

	@Override
	public void receiveIOCtrlData(final Camera camera, int sessionChannel, int avIOCtrlMsgType, byte[] data) {

		Bundle bundle = new Bundle();
		bundle.putString("requestDevice", ((MyCamera) camera).getUUID());
		bundle.putInt("sessionChannel", sessionChannel);
		bundle.putByteArray("data", data);

		Message msg = new Message();
		msg.what = avIOCtrlMsgType;
		msg.setData(bundle);
		handler.sendMessage(msg);
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			Bundle bundle = msg.getData();
			String requestDevice = bundle.getString("requestDevice");
			byte[] data = bundle.getByteArray("data");
			int i = 0;
			DeviceInfo device = null;
			MyCamera camera = null;

			for (i = 0; i < MultiViewActivity.DeviceList.size(); i++) {

				if (MultiViewActivity.DeviceList.get(i).UUID.equalsIgnoreCase(requestDevice)) {
					device = MultiViewActivity.DeviceList.get(i);
					break;
				}
			}

			for (i = 0; i < MultiViewActivity.CameraList.size(); i++) {

				if (MultiViewActivity.CameraList.get(i).getUUID().equalsIgnoreCase(requestDevice)) {
					camera = MultiViewActivity.CameraList.get(i);
					break;
				}
			}

			switch (msg.what) {
			case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_SAVE_DROPBOX_RESP:

				if (camera == null)
					break;
				SMsgAVIoctrlGetDropbox SMsgAVIoctrlGetDropbox = new SMsgAVIoctrlGetDropbox(data);
				device.nLinked = SMsgAVIoctrlGetDropbox.nLinked;
				device.nSupportDropbox = SMsgAVIoctrlGetDropbox.nSupportDropbox;
				// String MacAddress =
				// MainActivity.getMacAddress(LinkDropBoxActivity.this).toString();
				// String UDID = SMsgAVIoctrlGetDropbox.szLinkUDID.toString();
				device.nLinked = SMsgAVIoctrlGetDropbox.nLinked;
//					if(SMsgAVIoctrlGetDropbox.szLinkUDID.toString().equals(DatabaseManager.uid_Produce(LinkDropBoxActivity.this).toString()))
//					{
//						device.nLinked=1;
//					}
//					else
//					{
//						device.nLinked=0;
//					}
				DeviceListAdapter.notifyDataSetChanged();
				break;
			case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_SAVE_DROPBOX_RESP:
				break;
			}
			super.handleMessage(msg);
		}
	};

	private class DeviceListAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public DeviceListAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		public int getCount() {

			int count = 0;
			for (int i = 0; i < MultiViewActivity.DeviceList.size(); i++) {
				if (MultiViewActivity.DeviceList.get(i).nSupportDropbox == 1)
					count++;
			}
			return count;
		}

		public Object getItem(int position) {

			int count = 0;
			int select = 0;
			for (int i = 0; i < MultiViewActivity.DeviceList.size(); i++) {
				if (MultiViewActivity.DeviceList.get(i).nSupportDropbox == 1) {
					count++;
					if (position + 1 == count)
						select = i;
				}
			}
			return MultiViewActivity.DeviceList.get(select);
		}

		public long getItemId(int position) {

			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			int count = 0;
			int select = 0;
			for (int i = 0; i < MultiViewActivity.DeviceList.size(); i++) {
				if (MultiViewActivity.DeviceList.get(i).nSupportDropbox == 1) {
					count++;
					if (position + 1 == count)
						select = i;
				}
			}
			final DeviceInfo dev = MultiViewActivity.DeviceList.get(select);
			final MyCamera cam = MultiViewActivity.CameraList.get(select);

			if (dev == null || cam == null)
				return null;

			ViewHolder holder = null;

			if (convertView == null) {

				convertView = mInflater.inflate(R.layout.dropbox_device_list, null);

				holder = new ViewHolder();
				holder.title = (TextView) convertView.findViewById(R.id.title);
				holder.info = (TextView) convertView.findViewById(R.id.info);
				holder.link_dropbox_Button = (Switch) convertView.findViewById(R.id.link_dropbox_Button);
				convertView.setTag(holder);

			} else {

				holder = (ViewHolder) convertView.getTag();
			}

			if (holder != null) {

				holder.title.setText(dev.NickName);
				holder.info.setText(dev.UID);
				if (MultiViewActivity.DeviceList.get(select).nLinked == 0) {
					holder.link_dropbox_Button.setOnCheckedChangeListener(null);
					holder.link_dropbox_Button.setChecked(false);
				} else {
					holder.link_dropbox_Button.setOnCheckedChangeListener(null);
					holder.link_dropbox_Button.setChecked(true);
				}

				// if(MainActivity.DeviceList.get(select).Mode==2)
				// {
				// holder.link_dropbox_Button.setEnabled(true);
				// }
				// else
				// {
				// holder.link_dropbox_Button.setEnabled(false);
				// }
				holder.link_dropbox_Button.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
						// TODO Auto-generated method stub
						if (arg1) {
							// arg0.setChecked(true);
							cam.sendIOCtrl(MyCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_SAVE_DROPBOX_REQ,
									AVIOCTRLDEFs.SMsgAVIoctrlSetDropbox.parseContent(1, DatabaseManager.uid_Produce(LinkDropBoxActivity.this)
											.toString(), getKeys()[0], getKeys()[1], APP_KEY, APP_SECRET, "0"));
						} else {
							// arg0.setChecked(false);
							cam.sendIOCtrl(MyCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_SAVE_DROPBOX_REQ,
									AVIOCTRLDEFs.SMsgAVIoctrlSetDropbox.parseContent(0, DatabaseManager.uid_Produce(LinkDropBoxActivity.this)
											.toString(), getKeys()[0], getKeys()[1], APP_KEY, APP_SECRET, "0"));
						}
					}
				});

			}

			return convertView;

		}

		public final class ViewHolder {
			public TextView title;
			public TextView info;
			public Switch link_dropbox_Button;
		}
	}

	// dropbox section
	private void logOut() {
		// Remove credentials from the session
		mApi.getSession().unlink();

		// Clear our stored keys
		clearKeys();
		// Change UI state to display logged out version
		setLoggedIn(false);
	}

	/**
	 * Convenience function to change UI state based on being logged in
	 */
	private void setLoggedIn(boolean loggedIn) {
		mLoggedIn = loggedIn;
		if (mLoggedIn) {
			btn_menu.setVisibility(View.VISIBLE);
			Link_Button.setBackgroundResource(R.drawable.btn_drop_unlink_switch);
			// Link_Button.setText(LinkDropBoxActivity.this.getText(R.string.txtunlink));
			listView.setVisibility(View.VISIBLE);
		} else {
			btn_menu.setVisibility(View.GONE);
			Link_Button.setBackgroundResource(R.drawable.btn_drop_link_switch);
			// Link_Button.setText(LinkDropBoxActivity.this.getText(R.string.txtlink));
			listView.setVisibility(View.GONE);
		}
	}

	private void checkAppKeySetup() {
		// Check to make sure that we have a valid app key
		if (APP_KEY.startsWith("CHANGE") || APP_SECRET.startsWith("CHANGE")) {
			showToast("You must apply for an app key and secret from developers.dropbox.com, and add them to the DBRoulette ap before trying it.");
			finish();
			overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
			return;
		}

		// Check if the app has set up its manifest properly.
		Intent testIntent = new Intent(Intent.ACTION_VIEW);
		String scheme = "db-" + APP_KEY;
		String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
		testIntent.setData(Uri.parse(uri));
		PackageManager pm = getPackageManager();
		if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
			showToast("URL scheme in your app's " + "manifest is not set up correctly. You should have a "
					+ "com.dropbox.client2.android.AuthActivity with the " + "scheme: " + scheme);
			finish();
			overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
		}
	}

	private void showToast(String msg) {
		Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		error.show();
	}

	/**
	 * Shows keeping the access keys returned from Trusted Authenticator in a local store, rather
	 * than storing user name & password, and re-authenticating each time (which is not to be done,
	 * ever).
	 * 
	 * @return Array of [access_key, access_secret], or null if none stored
	 */
	private String[] getKeys() {
		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		String key = prefs.getString(ACCESS_KEY_NAME, null);
		String secret = prefs.getString(ACCESS_SECRET_NAME, null);
		if (key != null && secret != null) {
			String[] ret = new String[2];
			ret[0] = key;
			ret[1] = secret;
			return ret;
		} else {
			return null;
		}
	}

	/**
	 * Shows keeping the access keys returned from Trusted Authenticator in a local store, rather
	 * than storing user name & password, and re-authenticating each time (which is not to be done,
	 * ever).
	 */
	private void storeKeys(String key, String secret) {
		// Save the access key for later
		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		Editor edit = prefs.edit();
		edit.putString(ACCESS_KEY_NAME, key);
		edit.putString(ACCESS_SECRET_NAME, secret);
		edit.commit();
	}

	private void clearKeys() {
		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		Editor edit = prefs.edit();
		edit.clear();
		edit.commit();
	}

	private AndroidAuthSession buildSession() {
		AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
		AndroidAuthSession session;

		String[] stored = getKeys();
		if (stored != null) {
			AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
			session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
		} else {
			session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
		}

		return session;
	}

	// end dropbox section

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.bar_right_btn:
			for (int i = 0; i < MultiViewActivity.DeviceList.size(); i++) {
				MultiViewActivity.CameraList.get(i).sendIOCtrl(MyCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_SAVE_DROPBOX_REQ,
						"0".getBytes());
			}
			break;
		}
	}

	@Override
	public void receiveFrameDataForMediaCodec(Camera camera, int avChannel, byte[] buf, int length, int pFrmNo, byte[] pFrmInfoBuf, boolean isIframe,
			int codecId) {
		// TODO Auto-generated method stub

	}
}
