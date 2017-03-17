package com.tutk.P2PCam264.DELUX;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.android.pushservice.CustomPushNotificationBuilder;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.push.Utils;
import com.crashlytics.android.Crashlytics;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.IOTCAPIs;
import com.tutk.IOTC.st_LanSearchInfo2;
import com.tutk.IOTC.IRegisterIOTCListener;
import com.tutk.IOTC.Packet;
import com.tutk.Kalay.general.R;
import com.tutk.Logger.Glog;
import com.tutk.P2PCam264.DELUX.Custom_OkCancle_Dialog.OkCancelDialogListener;
import com.tutk.P2PCam264.DeviceInfo;
import com.tutk.P2PCam264.EventListActivity;
import com.tutk.P2PCam264.LiveViewActivity;
import com.tutk.P2PCam264.MyCamera;
import com.tutk.P2PCam264.onDropbox.LinkDropBoxActivity;
import com.viewpagerindicator.CirclePageIndicator;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import javax.microedition.khronos.opengles.GL10;

import addition.TUTK.AddDeviceActivity;
import addition.TUTK.AddDeviceTipsActivity;
import appteam.WifiAdmin;
import general.DatabaseManager;
import general.IOTC_GCM_IntentService;
import general.ThreadTPNS;
import io.fabric.sdk.android.Fabric;

public class MultiViewActivity extends InitCamActivity implements IRegisterIOTCListener, View.OnClickListener,
		Custom_popupWindow.On_PopupWindow_click_Listener, OkCancelDialogListener {

	private static final String TAG = "MultiViewActivity";
	public static final boolean SupportMultiPage = false;
	public static final boolean SupportOnDropbox = true;
	public static final boolean SupportEasyWiFiSetting = true;
	public static final boolean SupportDeviceOnCloud = false;
	private static final int idCmd_AddCamera = 7000;
	private static final int idCmd_LiveView = 7001;
	public static final int REQUEST_CODE_CAMERA_ADD = 0;
	public static final int REQUEST_CODE_CAMERA_VIEW = 1;
	public static final int REQUEST_CODE_CAMERA_EDIT = 2;
	public static final int REQUEST_CODE_CAMERA_EDIT_DELETE_OK = 1;
	public static final int REQUEST_CODE_CAMERA_EDIT_DATA_OK = 5;
	public static final int REQUEST_CODE_CAMERA_HISTORY = 3;
	public static final int REQUEST_CODE_CAMERA_SELECT_MONITOR = 4;
	private static final int REQUEST_CODE_Dropbox_SETTING = 6;
	public static final int REQUEST_CODE_LOGIN = 7;
	public static final int REQUEST_CODE_LOGIN_QUIT = 8;
	public static final int REQUEST_CODE_PUSH_SETTINGS = 9;
	public static final int REQUEST_CODE_CAMERA_ADD_ONLY = 10;
	public static final int MODE_EVENT = 0;
	public static final int MODE_GALLERY = 1;
	public static final int CLEAR_ALL = -1;
	private static livaviewFragmentAdapter mAdapter;
	public static int nShowMessageCount = 0;
	public static final int CAMERA_MAX_LIMITS = 4;
	private static IRegisterIOTCListener IRegisterIOTCListener;

	private ViewPager mPager;
	private CirclePageIndicator mIndicator;
	private SlidingMenu slidingMenu;

	private ImageButton btn_menu = null;
	private Button btnEvent;
	private Button btnGallery;
	private Button btnDropBox;
	private Button btnPushSettings;
	private Button btnAbout;
	private Button btnExit;
	private Button btnLive;
	private Button btnDevices;
	private Fragment VIEW_fragment;
	private TextView tvEdit;

	private int mCustomURL_CmdID;
    public static String mDeleteUID;
	private String mstrUid_FromCustomURL;
	private boolean mRemoving = false;
	public static boolean mNeedToRebuild = false;
	public static boolean mStartShowWithoutIOCtrl = false;

	// pageview無法重復新增
	private boolean IsAddpage = false;

	private enum MyMode {
		VIEW, LIST
	}

	private MyMode mMode = MyMode.VIEW;

	private WifiAdmin wifiAdmin; // For checking current wifi status

	@Override
	protected void onCreate(Bundle savedInstanceState) { this.getWindow().addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD); this.getWindow().addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);

//		apModeProcess(false);

		ActionBar actionBar = getActionBar();
		actionBar.hide();
		setContentView(R.layout.multi_view_activity);

		super.onCreate(savedInstanceState);
		Fabric.with(this, new Crashlytics());

		mCustomURL_CmdID = -1;
		Intent tIntent = this.getIntent();
		String tSchema = tIntent.getScheme();

		Glog.I("P2PCamLive", "MainActivity.onCreate... tSchema:\"" + ((tSchema == null) ? "(null)" : tSchema) + "\"");
		if (tSchema != null && tSchema.equals("p2pcamlive")) {
			Uri myURI = tIntent.getData();
			if (myURI != null) {
				Glog.I("p2pcamlive", "MainActivity.onCreate... myURI:\"" + myURI.toString() + "\"");
				if (myURI != null) {
					String strUri = myURI.toString();
					int nIdx_ValidURI = strUri.indexOf("com.tutk.p2pcamlive?");
					if (0 <= nIdx_ValidURI) {

						String strQueryParameterCP = strUri.substring(nIdx_ValidURI + "com.tutk.p2pcamlive?".length());
						// p2pcamlive://com.tutk.p2pcamlive?tabIdx:0
						// 0: Camera list
						// 1: Event list (NOT support on Android UI)
						//
						int nIdx_Parameter = strQueryParameterCP.indexOf("tabIdx:");
						if (0 <= nIdx_Parameter) {
							String strTabIndex = strQueryParameterCP.substring(nIdx_Parameter + "tabIdx:".length());

							Glog.I("p2pcamlive", "CameraList count:" + CameraList.size() + " Jump to Event list!");
						}

						// p2pcamlive://com.tutk.p2pcamlive?addDev:XXXXXXXXXXXXXXXXXXXX
						//
						nIdx_Parameter = strQueryParameterCP.indexOf("addDev:");
						if (0 <= nIdx_Parameter) {
							String strUid = strQueryParameterCP.substring(nIdx_Parameter + "addDev:".length());

							Glog.I("p2pcamlive", "CameraList count:" + CameraList.size() + " Add Camera UID:\"" + strUid + "\"");
							mCustomURL_CmdID = idCmd_AddCamera;
							mstrUid_FromCustomURL = strUid;
						}

						// p2pcamlive://com.tutk.p2pcamlive?liveView:XXXXXXXXXXXXXXXXXXXX
						//
						nIdx_Parameter = strQueryParameterCP.indexOf("liveView:");
						if (0 <= nIdx_Parameter) {
							String strUid = strQueryParameterCP.substring(nIdx_Parameter + "liveView:".length());

							Glog.I("p2pcamlive", "CameraList count:" + CameraList.size() + " Live view UID:\"" + strUid + "\"");
							mCustomURL_CmdID = idCmd_LiveView;
							mstrUid_FromCustomURL = strUid;

							handler.postDelayed(new Runnable() {

								@Override
								public void run() {

									for (DeviceInfo dev_info : DeviceList) {
										if (dev_info.UID.equals(mstrUid_FromCustomURL)) {

											Bundle extras = new Bundle();
											extras.putString("dev_uid", dev_info.UID);
											extras.putString("dev_uuid", dev_info.UUID);
											extras.putString("dev_nickname", dev_info.NickName);
											extras.putString("conn_status", dev_info.Status);
											extras.putString("view_acc", dev_info.View_Account);
											extras.putString("view_pwd", dev_info.View_Password);
											extras.putInt("camera_channel", dev_info.ChannelIndex);

											Intent intent = new Intent();
											intent.putExtras(extras);
											intent.setClass(MultiViewActivity.this, LiveViewActivity.class);
											startActivityForResult(intent, REQUEST_CODE_CAMERA_VIEW);
											overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

											break;
										}
									}

								}

							}, 1000);

						}

					}
				}
			}
		}

        setupView();
		setupSlideMenu();
		IRegisterIOTCListener = this;
		if(mNeedToRebuild){
			handler.post(new Runnable() {
				@Override
				public void run () {
					mNeedToRebuild = false;
					getFragmentManager().beginTransaction().remove(VIEW_fragment).commitAllowingStateLoss();
					getFragmentManager().executePendingTransactions();
					onConfigurationChanged(null);
				}
			});
		}

		if(DeviceList.size() == 0){
			/* do nothing
			Intent intent = new Intent();
			intent = intent.setClass(MultiViewActivity.this, AddDeviceActivity.class);
			Bundle bundle = new Bundle();
			bundle.putInt("MonitorIndex", 0);
			intent.putExtras(bundle);
			startActivityForResult(intent, REQUEST_CODE_CAMERA_ADD_ONLY);
/*
            Intent intent = new Intent(MultiViewActivity.this, AddDeviceTipsActivity.class);
            Bundle extras = new Bundle();
            extras.putInt("MonitorIndex", 0);
            intent.putExtras(extras);
            startActivityForResult(intent, REQUEST_CODE_CAMERA_SELECT_MONITOR);
*/
        }
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Custom_OkCancle_Dialog.SetDialogListener(this);
		MultiViewFragment.viewOnResume();
		apModeProcess(true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		quit();
	}

	private void apModeProcess(boolean isAddCamera) {
		// Check Network for AP Mode and Add Device if not in list
		Glog.I("P2PCamLive","apModeProcess: " + isAddCamera);
		wifiAdmin = new WifiAdmin(this);
//		wifiAdmin.openWifi();
		String ssid = null;
		if (wifiAdmin.isWifi()) {
			ssid = wifiAdmin.getSSID().toString().replace("\"", "");
//			ssid = wifiAdmin.getSSID();
		}
		Glog.I("P2PCamLive","Get SSID: " + ssid);
		String[] checkSSID = {"localhost", "Trywin", "Haotek", "DV202", "WD6"};
		Boolean isApMode = false;

		for (String curSSID: checkSSID ) {
//			Glog.I("P2PCamLive","checking SSID, checked SSID: " + curSSID + "," + ssid);
			if ( ssid != null && ssid.length() >= curSSID.length() ) {
				if ( ssid.substring(0,curSSID.length()).compareToIgnoreCase(curSSID) == 0 ) {
					isApMode = true;
					break;
				}
			}
		}
		if ( isApMode ) {
			// TODO search device uid && add device UID
//			initCameraList(true);
			int[] arrNum = { 20 };
//			Glog.I("P2PCamLive", "SSID " + ssid + " is Ap Mode");

//			IOTCAPIs.IOTC_Initialize2(0);
			st_LanSearchInfo2[] searchSSID = IOTCAPIs.IOTC_Lan_Search2(arrNum, 100);
//			IOTCAPIs.IOTC_DeInitialize();
			if ( searchSSID != null && searchSSID.length > 0 ) {
				Glog.I("P2PCamLive","searchSSID count: " + searchSSID.length);
				String newUid = new String(searchSSID[0].UID);
				Glog.I("P2PCamLive","uid: " + newUid);
				DatabaseManager manager = new DatabaseManager(MultiViewActivity.this);
				SQLiteDatabase db = manager.getReadableDatabase();
				Cursor cursor = db.query(DatabaseManager.TABLE_DEVICE, new String[]{"_id", "dev_uid"},
						"dev_uid = '" + newUid + "'", null, null, null, null);
				Boolean isExists = false;
				while (cursor.moveToNext()) {
					Glog.I("P2PCamLive","Database exists: " + cursor.getString(0) + "," + cursor.getString(1));
					isExists = true;
				}
				cursor.close();
				db.close();
				if ( ! isExists ) {
					db = manager.getReadableDatabase();
					cursor = db.query(DatabaseManager.TABLE_DEVICE_CHANNEL_ALLOCATION_TO_MONITOR, new String[] { "dev_uid", "dev_channel_Index",
							"Monitor_Index" }, null, null, null, null, "Monitor_Index" + " ASC");
					int monitorIndex = 0;
					while ( cursor.moveToNext() ) {
						Glog.I("P2PCamLive","Monitor Data: " + cursor.getString(0) + "," + cursor.getString(1) + "," + cursor.getString(2));
						int curIndex = Integer.parseInt(cursor.getString(2));
						if ( monitorIndex == curIndex ) {
							monitorIndex++;
						}
						else if ( monitorIndex < curIndex ) {
							break;
						}
					}
					Glog.I("P2PCamLive", "Monitor Index: " + monitorIndex);
					cursor.close();
					db.close();
					String dev_nickname = "Camera";
					String view_acc = "admin";
					String view_pwd = "888888";
					long db_id = manager.addDevice(dev_nickname, newUid, "", "", view_acc, view_pwd, 3, 0, ssid, "12345678", "", "");
					// nickname, uid, dev_name, dev_pwd, view_name, view_pwd,
					Glog.I("P2PCamLive", "Add Device Status: " + db_id);
					manager.add_Device_Channel_Allonation_To_MonitorByUID(newUid, 0, monitorIndex);
					//Toast.makeText(MultiViewActivity.this, getText(R.string.tips_add_camera_ok).toString(), Toast.LENGTH_SHORT).show();

					if ( isAddCamera ) {
						// add camera to current list
						Glog.I("P2PCamLive","New Camera: " + dev_nickname + "," + newUid+ "," + view_acc + "," + view_pwd);
						MyCamera camera = new MyCamera(dev_nickname, newUid, view_acc, view_pwd);
						DeviceInfo dev = new DeviceInfo(db_id, camera.getUUID(), dev_nickname, newUid, view_acc, view_pwd, "", 3, 0,
								null, null, null, null, null);
						Glog.I("P2PCamLive","New Device Info: " + db_id + "," + camera.getUUID());
						MultiViewActivity.DeviceList.add(dev);

						camera.registerIOTCListener(MultiViewActivity.getMultiViewActivityIRegisterIOTCListener());
						camera.connect(newUid);
						camera.start(MyCamera.DEFAULT_AV_CHANNEL, view_acc, view_pwd);
						camera.sendIOCtrl(MyCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_REQ,
								AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoReq.parseContent());
						camera.sendIOCtrl(MyCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETSUPPORTSTREAM_REQ,
								AVIOCTRLDEFs.SMsgAVIoctrlGetSupportStreamReq.parseContent());
						camera.sendIOCtrl(MyCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETAUDIOOUTFORMAT_REQ,
								AVIOCTRLDEFs.SMsgAVIoctrlGetAudioOutFormatReq.parseContent());
						camera.sendIOCtrl(MyCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIMEZONE_REQ,
								AVIOCTRLDEFs.SMsgAVIoctrlTimeZone.parseContent());

						camera.LastAudioMode = 1;

						MultiViewActivity.CameraList.add(camera);
						ChangeMutliMonitor(newUid, camera.getUUID(), dev_nickname, 0, monitorIndex, true);
					}
				}
				manager = null;
			}
//			DeviceList.clear();
		}
// Finish Check Ap Mode
	}

	public static IRegisterIOTCListener getMultiViewActivityIRegisterIOTCListener() {
		return IRegisterIOTCListener;
	}

	private void setupView() {
		getWindow().setFlags(LayoutParams.FLAG_KEEP_SCREEN_ON, LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		getWindow().clearFlags(LayoutParams.FLAG_FULLSCREEN);

		mAdapter = new livaviewFragmentAdapter(MultiViewActivity.this.getSupportFragmentManager());
		VIEW_fragment = new MultiViewFragment(this, CameraList, mAdapter);
//		getFragmentManager().beginTransaction().replace(R.id.layout_container, VIEW_fragment).commit();
		getFragmentManager().beginTransaction().add(R.id.layout_container, VIEW_fragment).commit();

		btn_menu = (ImageButton) this.findViewById(R.id.bar_btn);
		btnLive = (Button) this.findViewById(R.id.btnMultiView);
		btnDevices = (Button) this.findViewById(R.id.btnCameraLis);
		tvEdit = (TextView) this.findViewById(R.id.bar_txt);

		btn_menu.setOnClickListener(this);
		btnLive.setOnClickListener(this);
		btnDevices.setOnClickListener(this);
		tvEdit.setOnClickListener(this);

		btnLive.setBackgroundResource(R.drawable.btn_left_h);
		btnLive.setTextColor(Color.WHITE);

        btnLive.setVisibility(View.GONE);
        btnDevices.setVisibility(View.GONE);
	}

	private void setupSlideMenu() {
		slidingMenu = new SlidingMenu(this);
		slidingMenu.setFadeDegree(0.35f);
		slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
		slidingMenu.setShadowDrawable(R.drawable.shadow);
		slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset_left);
		slidingMenu.setMode(SlidingMenu.LEFT);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		slidingMenu.setMenu(R.layout.slide_menu);
		slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);

		btnEvent = (Button) findViewById(R.id.btnEvent);
		btnGallery = (Button) findViewById(R.id.btnGallery);
		btnDropBox = (Button) findViewById(R.id.btnRecording);
		btnPushSettings = (Button) findViewById(R.id.btnPush);
		btnAbout = (Button) findViewById(R.id.btnAbout);
		btnExit = (Button) findViewById(R.id.btnExit);

		btnEvent.setOnClickListener(this);
		btnGallery.setOnClickListener(this);
		btnDropBox.setOnClickListener(this);
		btnPushSettings.setOnClickListener(this);
		btnAbout.setOnClickListener(this);
		btnExit.setOnClickListener(this);
	}

	@SuppressWarnings("deprecation")
	private void startOnGoingNotification(String Text) {

		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		try {

			Intent intent = new Intent(this, MultiViewActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

			Notification.Builder builder = new Notification.Builder(this);
			builder.setContentIntent(pendingIntent).setSmallIcon(R.drawable.ic_launcher_s)
					.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
					.setTicker(String.format(getText(R.string.ntfAppRunning).toString(), getText(R.string.app_name).toString())).setAutoCancel(false)
					.setContentTitle(getText(R.string.app_name)).setContentText(Text).setWhen(0);

			Notification notification = builder.build();
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			manager.notify(0, notification);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void stopOnGoingNotification() {

		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.cancel(0);
		manager.cancel(1);
	}

	@SuppressWarnings("deprecation")
	private void showNotification(DeviceInfo dev, int camChannel, int evtType, long evtTime) {

		try {

			NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

			Bundle extras = new Bundle();
			extras.putString("dev_uuid", dev.UUID);
			extras.putString("dev_uid", dev.UID);
			extras.putString("dev_nickname", dev.NickName);
			extras.putInt("camera_channel", camChannel);
			extras.putString("view_acc", dev.View_Account);
			extras.putString("view_pwd", dev.View_Password);

			Intent intent = new Intent(this, EventListActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtras(extras);

			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

			Calendar cal = Calendar.getInstance();
			cal.setTimeZone(TimeZone.getDefault());
			cal.setTimeInMillis(evtTime);
			cal.add(Calendar.MONTH, 0);

			Notification.Builder builder = new Notification.Builder(this);
			builder.setContentIntent(pendingIntent).setSmallIcon(R.drawable.ic_push_s)
					.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_push))
					.setTicker(String.format(getText(R.string.ntfIncomingEvent).toString(), dev.NickName)).setAutoCancel(true)
					.setContentTitle(String.format(getText(R.string.ntfIncomingEvent).toString(), dev.NickName))
					.setContentText(String.format(getText(R.string.ntfLastEventIs).toString(), getEventType(this, evtType, false)))
					.setWhen(cal.getTimeInMillis());

			Notification notification = builder.build();
			notification.flags |= Notification.FLAG_NO_CLEAR;

			if (dev.EventNotification == 0)
				notification.defaults = Notification.DEFAULT_LIGHTS;
			else if (dev.EventNotification == 1)
				notification.defaults = Notification.DEFAULT_SOUND;
			else if (dev.EventNotification == 2)
				notification.defaults = Notification.DEFAULT_VIBRATE;
			else
				notification.defaults = Notification.DEFAULT_ALL;

			manager.notify(1, notification);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showSDCardFormatDialog(final Camera camera, final DeviceInfo device) {

		final AlertDialog dlg = new AlertDialog.Builder(MultiViewActivity.this).create();
		dlg.setTitle(R.string.dialog_FormatSDCard);
		dlg.setIcon(android.R.drawable.ic_menu_more);

		LayoutInflater inflater = dlg.getLayoutInflater();
		View view = inflater.inflate(R.layout.format_sdcard, null);
		dlg.setView(view);

		final CheckBox chbShowTipsFormatSDCard = (CheckBox) view.findViewById(R.id.chbShowTipsFormatSDCard);
		final Button btnFormat = (Button) view.findViewById(R.id.btnFormatSDCard);
		final Button btnClose = (Button) view.findViewById(R.id.btnClose);

		btnFormat.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_FORMATEXTSTORAGE_REQ,
						AVIOCTRLDEFs.SMsgAVIoctrlFormatExtStorageReq.parseContent(0));
				device.ShowTipsForFormatSDCard = chbShowTipsFormatSDCard.isChecked();

				DatabaseManager db = new DatabaseManager(MultiViewActivity.this);
				db.updateDeviceAskFormatSDCardByUID(device.UID, device.ShowTipsForFormatSDCard);

				dlg.dismiss();
			}
		});

		btnClose.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				device.ShowTipsForFormatSDCard = chbShowTipsFormatSDCard.isChecked();

				DatabaseManager db = new DatabaseManager(MultiViewActivity.this);
				db.updateDeviceAskFormatSDCardByUID(device.UID, device.ShowTipsForFormatSDCard);

				dlg.dismiss();
			}
		});
	}

	@Override
	public void receiveFrameData(Camera camera, int avChannel, Bitmap bmp) {
		// TODO Auto-generated method stub

	}

	private String getSessionMode(int mode) {

		String result = "";
		if (mode == 0)
			result = getText(R.string.connmode_p2p).toString();
		else if (mode == 1)
			result = getText(R.string.connmode_relay).toString();
		else if (mode == 2)
			result = getText(R.string.connmode_lan).toString();
		else
			result = getText(R.string.none).toString();

		return result;
	}

	private String getPerformance(int mode) {

		String result = "";
		if (mode < 30)
			result = getText(R.string.txtBad).toString();
		else if (mode < 60)
			result = getText(R.string.txtNormal).toString();
		else
			result = getText(R.string.txtGood).toString();

		return result;
	}

	public static void showAlert(Context context, CharSequence title, CharSequence message, CharSequence btnTitle) {

		AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(context);
		dlgBuilder.setIcon(android.R.drawable.ic_dialog_alert);
		dlgBuilder.setTitle(title);
		dlgBuilder.setMessage(message);
		dlgBuilder.setPositiveButton(btnTitle, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		}).show();
	}

	public static void showSelectDialog(Context context, CharSequence title, CharSequence message, CharSequence okBtnTxt, CharSequence cancelBtnTxt,
			OnClickListener okClickListener, OnClickListener cancelClickLinstener) {

		AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(context);
		dlgBuilder.setIcon(android.R.drawable.ic_dialog_alert);
		dlgBuilder.setTitle(title);
		dlgBuilder.setMessage(message);
		dlgBuilder.setPositiveButton(okBtnTxt, okClickListener);
		dlgBuilder.setNegativeButton(cancelBtnTxt, cancelClickLinstener);
		dlgBuilder.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_CAMERA_ADD) {

			switch (resultCode) {
			case RESULT_OK:
				break;
			}
		} else if (requestCode == REQUEST_CODE_CAMERA_VIEW) {

			if (mCustomURL_CmdID != -1) {
				finish();
				return;
			}

			switch (resultCode) {
			case RESULT_OK:
				if (mMode == MyMode.VIEW) {
					Bundle extras = data.getExtras();
					String dev_uuid = extras.getString("dev_uuid");
					String dev_uid = extras.getString("dev_uid");
					byte[] byts = extras.getByteArray("snapshot");
					int channelIndex = extras.getInt("camera_channel");
					int MonitorIndex = extras.getInt("MonitorIndex");
					int OriginallyChannelIndex = extras.getInt("OriginallyChannelIndex");
					String dev_nickname = extras.getString("dev_nickname");

					Bitmap snapshot = null;
					if (byts != null && byts.length > 0)
						snapshot = DatabaseManager.getBitmapFromByteArray(byts);

					for (int i = 0; i < DeviceList.size(); i++) {

						if (dev_uuid.equalsIgnoreCase(DeviceList.get(i).UUID) && dev_uid.equalsIgnoreCase(DeviceList.get(i).UID)) {

							DeviceList.get(i).ChannelIndex = channelIndex;

							if (snapshot != null)
								DeviceList.get(i).Snapshot = snapshot;

							break;
						}
					}
					if (channelIndex != OriginallyChannelIndex) {
						DatabaseManager DatabaseManager = new DatabaseManager(this);
						DatabaseManager.remove_Device_Channel_Allonation_To_MonitorByUID(dev_uid, OriginallyChannelIndex, MonitorIndex);
						DatabaseManager.add_Device_Channel_Allonation_To_MonitorByUID(dev_uid, channelIndex, MonitorIndex);
						DatabaseManager = null;
						int index = MonitorIndex % 4;
						ChangeMutliMonitor(dev_uid, dev_uuid, dev_nickname, channelIndex, index, false);
					}
//					if (mAdapter != null)
//						mAdapter.ReflashChannelInfo();
//					MultiViewFragment.ReflashChannelInfo();
				} else {
					Bundle extras = data.getExtras();
					String dev_uuid = extras.getString("dev_uuid");
					String dev_uid = extras.getString("dev_uid");
					byte[] byts = extras.getByteArray("snapshot");
					int channelIndex = extras.getInt("camera_channel");

					Bitmap snapshot = null;
					if (byts != null && byts.length > 0)
						snapshot = DatabaseManager.getBitmapFromByteArray(byts);

					for (int i = 0; i < DeviceList.size(); i++) {

						if (dev_uuid.equalsIgnoreCase(DeviceList.get(i).UUID) && dev_uid.equalsIgnoreCase(DeviceList.get(i).UID)) {
							DeviceList.get(i).ChannelIndex = channelIndex;
							if (snapshot != null)
								DeviceList.get(i).Snapshot = snapshot;
							break;
						}
					}
					break;
				}
				break;
			// 流程上有可能從LIVEVIEW 刪除
			case REQUEST_CODE_CAMERA_EDIT_DELETE_OK:

				if (mMode == MyMode.VIEW) {
					Bundle extras2 = data.getExtras();
					String dev_uuid2 = extras2.getString("dev_uuid");
					String dev_uid2 = extras2.getString("dev_uid");
					DeviceInfo device = null;
					MyCamera mCamera = null;
					for (DeviceInfo deviceInfo : MultiViewActivity.DeviceList) {
						if (dev_uuid2.equalsIgnoreCase(deviceInfo.UUID) && dev_uid2.equalsIgnoreCase(deviceInfo.UID)) {
							device = deviceInfo;
							break;
						}
					}
					for (MyCamera camera : MultiViewActivity.CameraList) {

						if (dev_uuid2.equalsIgnoreCase(camera.getUUID()) && dev_uid2.equalsIgnoreCase(camera.getUID())) {

							mCamera = camera;
							break;
						}

					}
					if (device == null || mCamera == null)
						return;
					removeFromMultiView(device.UID, device.UUID);

					ThreadTPNS thread = new ThreadTPNS(MultiViewActivity.this, device.UID, ThreadTPNS.MAPPING_UNREG);
					thread.start();
					// stop & remove camera
					mCamera.stop(Camera.DEFAULT_AV_CHANNEL);
					mCamera.disconnect();
					mCamera.unregisterIOTCListener(this);
					CameraList.remove(mCamera);
					// remove snapshot from database & storage
					DatabaseManager manager = new DatabaseManager(MultiViewActivity.this);
					SQLiteDatabase db = manager.getReadableDatabase();
					Cursor cursor = db.query(DatabaseManager.TABLE_SNAPSHOT, new String[] { "_id", "dev_uid", "file_path", "time" }, "dev_uid = '"
							+ device.UID + "'", null, null, null, "_id LIMIT " + MultiViewActivity.CAMERA_MAX_LIMITS);
					while (cursor.moveToNext()) {
						String file_path = cursor.getString(2);
						File file = new File(file_path);
						if (file.exists())
							file.delete();
					}
					cursor.close();
					db.close();

					manager.removeSnapshotByUID(device.UID);

					// remove camera from database
					manager.removeDeviceByUID(device.UID);

					// remove item from listview
					MultiViewActivity.DeviceList.remove(device);
					MultiViewActivity.showAlert(MultiViewActivity.this, getText(R.string.tips_warning), getText(R.string.tips_remove_camera_ok),
							getText(R.string.ok));
				}

				break;
			}
		} else if (requestCode == REQUEST_CODE_CAMERA_SELECT_MONITOR) {
			switch (resultCode) {
			case RESULT_OK:
				Bundle extras = data.getExtras();
				String dev_uuid = extras.getString("dev_uuid");
				String dev_uid = extras.getString("dev_uid");
				String dev_nickname = extras.getString("dev_nickname");
				String OriginallyUID = extras.getString("OriginallyUID");
				int OriginallyChannelIndex = extras.getInt("OriginallyChannelIndex");
				int channelIndex = extras.getInt("camera_channel");
				int MonitorIndex = extras.getInt("MonitorIndex");
				int index = MonitorIndex % 4;
				int page = MonitorIndex / 4;
				ArrayList<Integer> mMonitorList = MultiViewFragment.getMonitorList(MonitorIndex);
				HashMap<Integer, Boolean> mAddMap = (HashMap<Integer, Boolean>) extras.getSerializable("AddMap");
				MultiViewFragment.ClearNewIcon(CLEAR_ALL);

				if (mAddMap == null) {
					DatabaseManager DatabaseManager = new DatabaseManager(this);
					DatabaseManager.remove_Device_Channel_Allonation_To_MonitorByUID(OriginallyUID, OriginallyChannelIndex, MonitorIndex);
					DatabaseManager.add_Device_Channel_Allonation_To_MonitorByUID(dev_uid, channelIndex, MonitorIndex);
					DatabaseManager = null;
					ChangeMutliMonitor(dev_uid, dev_uuid, dev_nickname, channelIndex, MonitorIndex, true);
				} else {
					// set empty to the monitor which is user selected
					mMonitorList.set(MonitorIndex, -1);
					
					for (int i = 0; i < mAddMap.size(); i++) {
						// the channel which has been selected
						if (mAddMap.get(i)) {
							
							for (int j = MonitorIndex; j < mMonitorList.size(); j++) {
								// find the empty monitor
								if (mMonitorList.get(j) == -1) {
									
									if (j == MonitorIndex) {
										channelIndex = i;

										DatabaseManager DatabaseManager = new DatabaseManager(this);
										DatabaseManager.remove_Device_Channel_Allonation_To_MonitorByUID(OriginallyUID, OriginallyChannelIndex,
												MonitorIndex);
										DatabaseManager.add_Device_Channel_Allonation_To_MonitorByUID(dev_uid, channelIndex, MonitorIndex);
										DatabaseManager = null;
										ChangeMutliMonitor(dev_uid, dev_uuid, dev_nickname, channelIndex, MonitorIndex, true);
										OriginallyUID = null;
										OriginallyChannelIndex = 0;
										mMonitorList.set(j, MonitorIndex);
										break;
									} else {
										channelIndex = i;
										MonitorIndex = j;

										DatabaseManager DatabaseManager = new DatabaseManager(this);
										DatabaseManager.remove_Device_Channel_Allonation_To_MonitorByUID(OriginallyUID, OriginallyChannelIndex,
												MonitorIndex);
										DatabaseManager.add_Device_Channel_Allonation_To_MonitorByUID(dev_uid, channelIndex, MonitorIndex);
										DatabaseManager = null; 
										if (!(ChangeMutliMonitor(dev_uid, dev_uuid, dev_nickname, channelIndex, MonitorIndex, true))) {
											mMonitorList.set(j, -1);
											j--;
										} else {
											mMonitorList.set(j, MonitorIndex);
											break;
										}
										OriginallyUID = null;
										OriginallyChannelIndex = 0;
									}

								}
							}
						}
					}
				}

				break;
			}
		} else if (requestCode == REQUEST_CODE_CAMERA_EDIT) {

			switch (resultCode) {
			case REQUEST_CODE_CAMERA_EDIT_DELETE_OK:

				Bundle extras = data.getExtras();
				String dev_uuid = extras.getString("dev_uuid");
				String dev_uid = extras.getString("dev_uid");
				DeviceInfo device = null;
				MyCamera mCamera = null;
				for (DeviceInfo deviceInfo : MultiViewActivity.DeviceList) {
					if (dev_uuid.equalsIgnoreCase(deviceInfo.UUID) && dev_uid.equalsIgnoreCase(deviceInfo.UID)) {
						device = deviceInfo;
						break;
					}
				}
				for (MyCamera camera : MultiViewActivity.CameraList) {

					if (dev_uuid.equalsIgnoreCase(camera.getUUID()) && dev_uid.equalsIgnoreCase(camera.getUID())) {

						mCamera = camera;
						break;
					}

				}
				if (device == null || mCamera == null)
					return;
				removeFromMultiView(device.UID, device.UUID);

				ThreadTPNS thread = new ThreadTPNS(MultiViewActivity.this, device.UID, ThreadTPNS.MAPPING_UNREG);
				thread.start();
				// stop & remove camera
				mCamera.stop(Camera.DEFAULT_AV_CHANNEL);
				mCamera.disconnect();
				mCamera.unregisterIOTCListener(this);
				CameraList.remove(mCamera);
				// remove snapshot from database & storage
				DatabaseManager manager = new DatabaseManager(MultiViewActivity.this);
				SQLiteDatabase db = manager.getReadableDatabase();
				Cursor cursor = db.query(DatabaseManager.TABLE_SNAPSHOT, new String[] { "_id", "dev_uid", "file_path", "time" }, "dev_uid = '"
						+ device.UID + "'", null, null, null, "_id LIMIT " + MultiViewActivity.CAMERA_MAX_LIMITS);
				while (cursor.moveToNext()) {
					String file_path = cursor.getString(2);
					File file = new File(file_path);
					if (file.exists())
						file.delete();
				}
				cursor.close();
				db.close();

				manager.removeSnapshotByUID(device.UID);

				// remove camera from database
				manager.removeDeviceByUID(device.UID);

				// remove item from listview
				MultiViewActivity.DeviceList.remove(device);
				MultiViewActivity.showAlert(MultiViewActivity.this, getText(R.string.tips_warning), getText(R.string.tips_remove_camera_ok),
						getText(R.string.ok));
				break;

			case REQUEST_CODE_CAMERA_EDIT_DATA_OK:
//				mAdapter.ReflashChannelInfo();
				MultiViewFragment.ReflashChannelInfo();
				break;
			}

		} else if (requestCode == REQUEST_CODE_LOGIN) {
			switch (resultCode) {
			case REQUEST_CODE_LOGIN_QUIT:
				quit();
				break;
			}
		} else if (requestCode == REQUEST_CODE_PUSH_SETTINGS) {
			switch (resultCode) {
			case RESULT_OK:
				boolean result = data.getBooleanExtra("settings", false);
				if (result) {
					InitCamActivity.mSupportBaidu = true;
					IOTC_GCM_IntentService.mSupportBaidu = true;
					ThreadTPNS.mSupportBaidu = true;

					Utils.logStringCache = Utils.getLogText(getApplicationContext());

					Resources resource = this.getResources();
					String pkgName = this.getPackageName();

					PushManager.startWork(getApplicationContext(), PushConstants.LOGIN_TYPE_API_KEY,
							Utils.getMetaValue(MultiViewActivity.this, "api_key"));
					CustomPushNotificationBuilder cBuilder = new CustomPushNotificationBuilder(getApplicationContext(), resource.getIdentifier(
							"notification_custom_builder", "layout", pkgName), resource.getIdentifier("notification_icon", "id", pkgName),
							resource.getIdentifier("notification_title", "id", pkgName), resource.getIdentifier("notification_text", "id", pkgName));
					cBuilder.setNotificationFlags(Notification.FLAG_AUTO_CANCEL);
					cBuilder.setNotificationDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
					cBuilder.setStatusbarIcon(this.getApplicationInfo().icon);
					cBuilder.setLayoutDrawable(resource.getIdentifier("simple_notification_icon", "drawable", pkgName));
					PushManager.setNotificationBuilder(this, 1, cBuilder);
				} else {
					InitCamActivity.mSupportBaidu = false;
					IOTC_GCM_IntentService.mSupportBaidu = false;
					ThreadTPNS.mSupportBaidu = false;

					PushManager.stopWork(MultiViewActivity.this);
				}
				break;
			}
		}
		else if ( requestCode == REQUEST_CODE_CAMERA_ADD_ONLY ) {
			if ( resultCode == RESULT_OK ) {
				Glog.I("P2PCamLive","Add Only Result OK");
				Bundle extras = data.getExtras();
				long db_id = extras.getLong("db_id");
				String dev_uid = extras.getString("dev_uid");
				String view_acc = extras.getString("view_acc");
				String view_pwd = extras.getString("view_pwd");
				String dev_nickname = extras.getString("nickname");
				int channelIndex = 0;
				int MonitorIndex = extras.getInt("MonitorIndex");
				DatabaseManager DatabaseManager = new DatabaseManager(this);
//				long db_id = DatabaseManager.addDevice(dev_nickname, dev_uid, "", "", "admin", "888888", 3, 0, "", "12345678", "", "");
				DatabaseManager.add_Device_Channel_Allonation_To_MonitorByUID(dev_uid, channelIndex, MonitorIndex);
				DatabaseManager = null;
				Glog.I("P2PCamLive","Device Added to monitor index: " + MonitorIndex);


				// add camera to current list
				Glog.I("P2PCamLive","New Camera: " + dev_nickname + "," + dev_uid+ "," + view_acc + "," + view_pwd);
				MyCamera camera = new MyCamera(dev_nickname, dev_uid, view_acc, view_pwd);
				DeviceInfo dev = new DeviceInfo(db_id, camera.getUUID(), dev_nickname, dev_uid, view_acc, view_pwd, "", 3, channelIndex,
						null, null, null, null, null);
				Glog.I("P2PCamLive","New Device Info: " + db_id + "," + camera.getUUID());
				MultiViewActivity.DeviceList.add(dev);

				camera.registerIOTCListener(MultiViewActivity.getMultiViewActivityIRegisterIOTCListener());
				camera.connect(dev_uid);
				camera.start(MyCamera.DEFAULT_AV_CHANNEL, view_acc, view_pwd);
				camera.sendIOCtrl(MyCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_REQ,
						AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoReq.parseContent());
				camera.sendIOCtrl(MyCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETSUPPORTSTREAM_REQ,
						AVIOCTRLDEFs.SMsgAVIoctrlGetSupportStreamReq.parseContent());
				camera.sendIOCtrl(MyCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETAUDIOOUTFORMAT_REQ,
						AVIOCTRLDEFs.SMsgAVIoctrlGetAudioOutFormatReq.parseContent());
				camera.sendIOCtrl(MyCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIMEZONE_REQ,
						AVIOCTRLDEFs.SMsgAVIoctrlTimeZone.parseContent());

				camera.LastAudioMode = 1;

				MultiViewActivity.CameraList.add(camera);
				ChangeMutliMonitor(dev_uid, camera.getUUID(), dev_nickname, channelIndex, MonitorIndex, true);
			}
		}
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

		}
	};

	private void quit() {

		Glog.D(TAG, "issue activity result code:" + RESULT_OK + "L ...");
		stopOnGoingNotification();
		setResult(RESULT_OK);
		finish();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {

		case KeyEvent.KEYCODE_BACK:

			if (!mRemoving)
				if (slidingMenu.isMenuShowing())
					slidingMenu.toggle();
				else
					moveTaskToBack(false);
			else
				showHideDelelteLayout();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	@SuppressWarnings("unused")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.bar_btn:
			if (!mRemoving)
				slidingMenu.toggle();
			break;

		case R.id.btnEvent:
			slidingMenu.toggle(false);
			Intent intent = new Intent(MultiViewActivity.this, NicknameListActivity.class);
			intent.putExtra("mode", MODE_EVENT);
			startActivity(intent);
			overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			break;

		case R.id.btnGallery:
			slidingMenu.toggle(false);
			intent = new Intent(MultiViewActivity.this, NicknameListActivity.class);
			intent.putExtra("mode", MODE_GALLERY);
			startActivity(intent);
			overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			break;

		case R.id.btnRecording:
			slidingMenu.toggle(false);
			if (MultiViewActivity.SupportOnDropbox) {
				intent = new Intent();
				intent.setClass(MultiViewActivity.this, CloudRecordingActivity.class);
				startActivityForResult(intent, REQUEST_CODE_Dropbox_SETTING);
				overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			}
			break;

		case R.id.btnPush:
			slidingMenu.toggle(false);
			intent = new Intent(MultiViewActivity.this, PushSettingActivity.class);
			startActivityForResult(intent, REQUEST_CODE_PUSH_SETTINGS);
			overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			break;

		case R.id.btnAbout:
//			String versionName = "";
//			try {
//				versionName = this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
//			} catch (NameNotFoundException e) {
//			}
//			AboutDialog dlg = new AboutDialog(this, getText(R.string.dialog_AboutMe).toString(), versionName);
//			dlg.setCanceledOnTouchOutside(true);
//			dlg.show();
			slidingMenu.toggle(false);
			intent = new Intent(MultiViewActivity.this, AboutActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			break;

		case R.id.btnExit:
			quit();
			break;

		case R.id.bar_txt:
			doDelete();
			break;
		}

	}

	private boolean ChangeMutliMonitor(String UID, String UUID, String name, int CameraChannel, int MonitorIndex, boolean isNew) {

		int index = MonitorIndex % 4;
		Chaanel_to_Monitor_Info mChaanel_Info = new Chaanel_to_Monitor_Info(UUID, name, UID, CameraChannel, "CH" + CameraChannel, index);

		return MultiViewFragment.ChangeMutliMonitor(mChaanel_Info, MonitorIndex, isNew);

//		if (mAdapter.ChangeChannelInfo(mChaanel_Info, MonitorIndex) && !IsAddpage) {
//			if (SupportMultiPage) {
//				mIndicator.setViewPager(mPager);
//				mIndicator.setOnPageChangeListener(mAdapter);
//			}
//		}
//		mAdapter.notifyDataSetChanged();
	}

	private void updateView() {
		MultiViewFragment.updateView();
	}

	private void InitMutliMonitor() {

		ArrayList<ArrayList<Chaanel_to_Monitor_Info>> mChaanel_Info = new ArrayList<ArrayList<Chaanel_to_Monitor_Info>>();
		int LinkMonitorCount = 0;
		int PageCount = 0;
		ArrayList<Chaanel_to_Monitor_Info> BasicsubInfo = new ArrayList<Chaanel_to_Monitor_Info>();
		mChaanel_Info.add(BasicsubInfo);
		for (MyCamera camera : CameraList) {
			if (camera != null) {

				DatabaseManager manager = new DatabaseManager(MultiViewActivity.this);
				SQLiteDatabase db = manager.getReadableDatabase();
				Cursor cursor = db.query(DatabaseManager.TABLE_DEVICE_CHANNEL_ALLOCATION_TO_MONITOR, new String[] { "dev_uid", "dev_channel_Index",
						"Monitor_Index" }, "dev_uid = ?", new String[] { camera.getUID() }, null, null, "Monitor_Index" + " ASC");
				while (cursor.moveToNext()) {
					int ChannelIndex = cursor.getInt(1);
					int monitorindex = cursor.getInt(2);
					int index = monitorindex % 4;

					PageCount = monitorindex / 4;
					while (mChaanel_Info.size() < (PageCount + 1)) {
						ArrayList<Chaanel_to_Monitor_Info> subInfo = new ArrayList<Chaanel_to_Monitor_Info>();
						mChaanel_Info.add(subInfo);
					}

					mChaanel_Info.get(PageCount)
							.add(new Chaanel_to_Monitor_Info(camera.getUUID(), camera.getName(), camera.getUID(), ChannelIndex, "CH" + ChannelIndex,
									index));
				}
				cursor.close();
			}
		}

		if (PageCount != 0) {
			if (SupportMultiPage) {
				mIndicator.setViewPager(mPager);
				mIndicator.setOnPageChangeListener(mAdapter);
			}
		}
		mAdapter.SetChannelInfo(mChaanel_Info);
		mAdapter.notifyDataSetChanged();
	}

	public void checkPlayingChannel(String uid){
		if(VIEW_fragment != null) {
			((MultiViewFragment) VIEW_fragment).checkPlayingChannel(uid);
		}
	}

	@Override
	protected void INIT_CAMERA_LIST_OK() {
		// TODO Auto-generated method stub
		String notify = "";
		if (CameraList.size() == 0)
			notify = String.format(getText(R.string.ntfAppRunning).toString(), getText(R.string.app_name).toString());
		else
			notify = String.format(getText(R.string.ntfCameraRunning).toString(), CameraList.size());

		startOnGoingNotification(notify);
		reflash_Status();

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Configuration cfg = getResources().getConfiguration();
		setContentView(R.layout.multi_view_activity);

//		MultiViewFragment.clearView();

		if (cfg.orientation == Configuration.ORIENTATION_LANDSCAPE) {

			getFragmentManager().beginTransaction().remove(VIEW_fragment).commitAllowingStateLoss();
			getFragmentManager().executePendingTransactions();

			getFragmentManager().beginTransaction().add(R.id.layout_container, VIEW_fragment).commitAllowingStateLoss();

			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					MultiViewFragment.startView();
				}
			}, 500);
		} else if (cfg.orientation == Configuration.ORIENTATION_PORTRAIT) {

			getFragmentManager().beginTransaction().remove(VIEW_fragment).commitAllowingStateLoss();
			getFragmentManager().executePendingTransactions();

			getFragmentManager().beginTransaction().add(R.id.layout_container, VIEW_fragment).commitAllowingStateLoss();

			btn_menu = (ImageButton) this.findViewById(R.id.bar_btn);
			btnLive = (Button) this.findViewById(R.id.btnMultiView);
			btnDevices = (Button) this.findViewById(R.id.btnCameraLis);
			tvEdit = (TextView) this.findViewById(R.id.bar_txt);

			btn_menu.setOnClickListener(this);
			btnLive.setOnClickListener(this);
			btnDevices.setOnClickListener(this);
			tvEdit.setOnClickListener(this);

			btnLive.setBackgroundResource(R.drawable.btn_left_h);
			btnLive.setTextColor(Color.WHITE);
			setupSlideMenu();
			btnLive.setVisibility(View.GONE);
			btnDevices.setVisibility(View.GONE);

			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					MultiViewFragment.startView();
					MultiViewFragment.updateView();
				}
			}, 500);
		}
	}

	@Override
	protected void IOTYPE_USER_IPCAM_DEVINFO_RESP(MyCamera camera, DeviceInfo device, byte[] data) {
		// TODO Auto-generated method stub
		int total = Packet.byteArrayToInt_Little(data, 40);
		if (total == -1 && camera != null && camera.getSDCardFormatSupported(0) && device != null && device.ShowTipsForFormatSDCard)
			showSDCardFormatDialog(camera, device);

//		reflash_Status();
	}

	@Override
	protected void IOTYPE_USER_IPCAM_EVENT_REPORT(DeviceInfo DeviceInfo, byte[] data) {
		byte[] t = new byte[8];
		System.arraycopy(data, 0, t, 0, 8);
		AVIOCTRLDEFs.STimeDay evtTime = new AVIOCTRLDEFs.STimeDay(t);

		int camChannel = Packet.byteArrayToInt_Little(data, 12);
		int evtType = Packet.byteArrayToInt_Little(data, 16);

		SharedPreferences settings = getSharedPreferences("Interval", 0);
		int mSwitch = settings.getInt(DeviceInfo.UID, -1);
		if (mSwitch == ThreadTPNS.INTERVAL_OFF)
			return;

		if (evtType != AVIOCTRLDEFs.AVIOCTRL_EVENT_MOTIONPASS && evtType != AVIOCTRLDEFs.AVIOCTRL_EVENT_IOALARMPASS)
			showNotification(DeviceInfo, camChannel, evtType, evtTime.getTimeInMillis());		
	};

	@Override
	protected void IOTYPE_USER_IPCAM_FORMATEXTSTORAGE_RESP(byte[] data) {
		// TODO Auto-generated method stub

		int result = data[4];

		if (result == 0)
			Toast.makeText(this, getText(R.string.tips_format_sdcard_success).toString(), Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(this, getText(R.string.tips_format_sdcard_failed).toString(), Toast.LENGTH_SHORT).show();

	}

	@Override
	protected void CONNECTION_STATE_CONNECTING(boolean Status) {
		// TODO Auto-generated method stub
		reflash_Status();
	}

	@Override
	protected void CONNECTION_STATE_CONNECTED(boolean Status, String UID) {
		reflash_Status();
		connected_Status(UID);
	}

	@Override
	protected void CONNECTION_STATE_UNKNOWN_DEVICE(boolean Status) {
		// TODO Auto-generated method stub
		reflash_Status();
	}

	@Override
	protected void CONNECTION_STATE_DISCONNECTED(boolean Status) {
		// TODO Auto-generated method stub
		reflash_Status();
	}

	@Override
	protected void CONNECTION_STATE_TIMEOUT(boolean Status) {
		// TODO Auto-generated method stub
		reflash_Status();
	}

	@Override
	protected void CONNECTION_STATE_WRONG_PASSWORD(boolean Status) {
		// TODO Auto-generated method stub
		reflash_Status();
	}

	@Override
	protected void CONNECTION_STATE_CONNECT_FAILED(boolean Status) {
		// TODO Auto-generated method stub
		reflash_Status();
	}

	private void reflash_Status() {
//		if (mAdapter != null)
//			mAdapter.reflash_Status();
		if (mMode == MyMode.VIEW && VIEW_fragment != null) {
			if (VIEW_fragment.isVisible())
				MultiViewFragment.reflash_Status();
		}
	}

	private void connected_Status(String UID) {
//		if (mAdapter != null)
//			mAdapter.connected_Status(UID);
		MultiViewFragment.connected_Status(UID);
	}

	private void doDelete() {
		if (!mRemoving)
			showHideDelelteLayout();
		else {
			if (mMode == MyMode.VIEW) {
				if (MultiViewFragment.checkDelete()) {
					Custom_OkCancle_Dialog dlg = new Custom_OkCancle_Dialog(MultiViewActivity.this, getText(
							R.string.tips_remove_camera_monitor_confirm).toString());
					dlg.setCanceledOnTouchOutside(false);
					Window window = dlg.getWindow();
					window.setWindowAnimations(R.style.setting_dailog_animstyle);
					dlg.show();
				} else {
					showHideDelelteLayout();
				}
			} else {
				if (DeviceListFragment.checkDelete()) {
					Custom_OkCancle_Dialog dlg = new Custom_OkCancle_Dialog(MultiViewActivity.this, getText(R.string.tips_remove_camera_confirm)
							.toString());
					dlg.setCanceledOnTouchOutside(false);
					Window window = dlg.getWindow();
					window.setWindowAnimations(R.style.setting_dailog_animstyle);
					dlg.show();
				} else {
					showHideDelelteLayout();
				}
			}
		}
	}

	private void showHideDelelteLayout() {
		if (!mRemoving) {
			mRemoving = !mRemoving;
			tvEdit.setText(getString(R.string.ok));
			btn_menu.setEnabled(false);
			btnLive.setEnabled(false);
			btnDevices.setEnabled(false);
			if (mMode == MyMode.VIEW) {
				MultiViewFragment.showDelelteLayout();
			} else {
				DeviceListFragment.showHideDelelteLayout();
			}
		} else {
			mRemoving = !mRemoving;
			tvEdit.setText(getString(R.string.txt_edit));
			btn_menu.setEnabled(true);
			btnLive.setEnabled(true);
			btnDevices.setEnabled(true);
			if (mMode == MyMode.VIEW) {
				MultiViewFragment.hideDelelteLayout();
			} else {
				DeviceListFragment.showHideDelelteLayout();
			}
		}
	}

	@Override
	public void btn_infomation_click(PopupWindow PopupWindow) {
		// TODO Auto-generated method stub

	}

	@Override
	public void btn_log_in_out_click(PopupWindow PopupWindow) {
		// TODO Auto-generated method stub

	}

	private void clearAllDeviceSync() {
		for (int i = 0; i < CameraList.size(); i++) {
			DatabaseManager manager = new DatabaseManager(MultiViewActivity.this);
			manager.updateDeviceIsSyncByUID(CameraList.get(i).getUID(), false);
		}
	}

    private void removeDevice (String uid) {

        DatabaseManager manager = new DatabaseManager(MultiViewActivity.this);
        SQLiteDatabase db = manager.getReadableDatabase();
        Cursor cursor = db.query(DatabaseManager.TABLE_SNAPSHOT, new String[] {"_id", "dev_uid", "file_path", "time"},
                "dev_uid = '" + uid + "'", null, null, null,
                "_id LIMIT " + MultiViewActivity.CAMERA_MAX_LIMITS);
        while (cursor.moveToNext()) {
            String file_path = cursor.getString(2);
            File file = new File(file_path);
            if (file.exists()) {
                file.delete();
            }
        }
        cursor.close();
        db.close();

        ThreadTPNS threadTPNS = new ThreadTPNS(MultiViewActivity.this, uid, ThreadTPNS.MAPPING_UNREG);
        threadTPNS.start();

        manager.removeSnapshotByUID(uid);
        manager.removeDeviceByUID(uid);

        int position = 0;
        for(int i = 0;i < MultiViewActivity.DeviceList.size(); i++){
            if(uid.equals(MultiViewActivity.DeviceList.get(i).UID)){
                position = i;
                break;
            }
        }
        MyCamera myCamera = MultiViewActivity.CameraList.get(position);
        DeviceInfo deviceInfo = MultiViewActivity.DeviceList.get(position);

        myCamera.stop(Camera.DEFAULT_AV_CHANNEL);
        myCamera.disconnect();
//			myCamera.unregisterIOTCListener(mActivity);

        MultiViewActivity.DeviceList.remove(position);
        MultiViewActivity.CameraList.remove(position);
        MultiViewActivity.removeFromMultiView(deviceInfo.UID, deviceInfo.UUID);
    }

	@Override
	public void btn_onDropbox_click(PopupWindow PopupWindow) {
		// TODO Auto-generated method stub
		if (MultiViewActivity.SupportOnDropbox) {
			Intent intent = new Intent();
			intent.setClass(MultiViewActivity.this, LinkDropBoxActivity.class);
			startActivityForResult(intent, REQUEST_CODE_Dropbox_SETTING);
			overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
		}
	}

	public static void removeFromMultiView(String uid, String uuid) {
//		if (mAdapter != null)
//			mAdapter.remove_uid(uid, uuid);
		MultiViewFragment.removeFromMultiView(uid, uuid);
	}

	@Override
	public void btn_change_ch(int channel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void btn_change_quality(int level) {
		// TODO Auto-generated method stub

	}

	@Override
	public void btn_change_env(int mode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void btn_add_monitor(HashMap<Integer, Boolean> AddMap, int DeviceNo) {
		// TODO Auto-generated method stub

	}

    @Override
    public void btn_photo (int value) {

    }

    @Override
	public void ok() {
        removeDevice(mDeleteUID);
	}

	@Override
	public void cancel() {
//		showHideDelelteLayout();
	}
}
