package com.tutk.P2PCam264.DELUX;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;

import com.baidu.android.pushservice.CustomPushNotificationBuilder;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.push.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.AVIOCTRLDEFs.SMsgAVIoctrlGetDropbox;
import com.tutk.IOTC.AVIOCTRLDEFs.SMsgAVIoctrlGetSupportStreamReq;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.IRegisterIOTCListener;
import com.tutk.Kalay.general.R;
import com.tutk.Logger.Glog;
import com.tutk.P2PCam264.DeviceInfo;
import com.tutk.P2PCam264.MyCamera;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import appteam.WifiAdmin;
import general.DatabaseManager;
import general.IOTC_GCM_IntentService;
import general.ThreadTPNS;

//此activity方便將來flow不同  搬移 mainActivity用 負責整個init的流程
public class InitCamActivity extends FragmentActivity implements IRegisterIOTCListener {

    private static final String TAG = "InitCamActivity";
    public static List<MyCamera> CameraList = new ArrayList<MyCamera>();
    public static List<DeviceInfo> DeviceList = Collections.synchronizedList(new ArrayList<DeviceInfo>());
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private ResultStateReceiver resultStateReceiver;
    private IntentFilter filter;
    private ThreadReconnect m_threadReconnect = null;
    public static long startTime = 0;
    public static boolean noResetWiFi = true;

    private static boolean mIsAddToCloud = false;

    private boolean mIsSetWifiSeccess = false;

    private WifiAdmin WifiAdmin;
    private WifiManager mWifiManager;
    private WifiReceiver mWifiReceiver;
    private WifiConfiguration mWifiConfiguration = new WifiConfiguration();
    private static String mCurrWifiSSID = "";
    private static String mCurrWifiPWD = "";
    private static String mCandidateUID = "";
    private static String mCandidateNickName = "";
    private static String mCandidateType = "";
    private DatabaseManager mDBManager = new DatabaseManager(this);
    private Object mIsSync = new Object();
    public static boolean mSupportBaidu = false;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MyCamera.init();
//		initDownLoad();
        setupView();

        WifiAdmin = new WifiAdmin(this);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(MultiViewActivity.class.getName());
        mWifiReceiver = new WifiReceiver();
        registerReceiver(mWifiReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
//        mDeviceOnCloudClient = new DeviceOnCloudClient();
//		mDeviceOnCloudClient.RegistrInterFace(this);

        if(checkPlayServices()) {

            Thread threadRegGCM = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(InitCamActivity.this);

                        if (gcm != null) {
                            gcm.register(DatabaseManager.s_GCM_sender);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            threadRegGCM.start();
        }

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            String GCMDevUID = bundle.getString("dev_uid");
            for (int i = 0 ; i < DeviceList.size() ; i++) {
                if (DeviceList.get(i).UID.equals(GCMDevUID) == true) {
                    DeviceList.get(i).n_gcm_count++;
                }
            }
        }

        filter = new IntentFilter();
        filter.addAction(MultiViewActivity.class.getName());
        resultStateReceiver = new ResultStateReceiver();

        registerReceiver(resultStateReceiver, filter);
        DatabaseManager.n_mainActivity_Status = 1;

        SharedPreferences baidu = this.getSharedPreferences("Push Setting", 0);
        mSupportBaidu = baidu.getBoolean("settings", false);

        if (mSupportBaidu) {
            Utils.logStringCache = Utils.getLogText(getApplicationContext());

            Resources resource = this.getResources();
            String pkgName = this.getPackageName();

            PushManager.startWork(getApplicationContext(), PushConstants.LOGIN_TYPE_API_KEY, Utils.getMetaValue(InitCamActivity.this, "api_key"));
            CustomPushNotificationBuilder cBuilder = new CustomPushNotificationBuilder(getApplicationContext(), resource.getIdentifier
                    ("notification_custom_builder", "layout", pkgName), resource.getIdentifier("notification_icon", "id", pkgName), resource
                    .getIdentifier("notification_title", "id", pkgName), resource.getIdentifier("notification_text", "id", pkgName));
            cBuilder.setNotificationFlags(Notification.FLAG_AUTO_CANCEL);
            cBuilder.setNotificationDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
            cBuilder.setStatusbarIcon(this.getApplicationInfo().icon);
            cBuilder.setLayoutDrawable(resource.getIdentifier("simple_notification_icon", "drawable", pkgName));
            PushManager.setNotificationBuilder(this, 1, cBuilder);

            IOTC_GCM_IntentService.mSupportBaidu = true;
            ThreadTPNS.mSupportBaidu = true;
        } else {
            IOTC_GCM_IntentService.mSupportBaidu = false;
            ThreadTPNS.mSupportBaidu = false;
        }
    }

    @Override
    protected void onRestart () {
        super.onRestart();
    }

    @Override
    protected void onPause () {
        super.onPause();

    }

    @Override
    protected void onResume () {
        super.onResume();

    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        unregisterReceiver(resultStateReceiver);
        DatabaseManager.n_mainActivity_Status = 0;
        quit();
    }

    @Override
    protected void onStart () {
        DatabaseManager.n_mainActivity_Status = 1;
        super.onStart();
    }

    @Override
    protected void onStop () {
        DatabaseManager.n_mainActivity_Status = 0;
        super.onStop();
    }

    private void quit () {

        for (MyCamera camera : CameraList) {
            camera.disconnect();
            camera.unregisterIOTCListener(this);
        }

        System.out.println("kill process");

        MyCamera.uninit();

        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);

    }

    private void setupView () {
        initCameraList(true);
    }

    public void initCameraList (boolean bFirstTime) {

        if (bFirstTime) {
            DatabaseManager manager = new DatabaseManager(this);
            SQLiteDatabase db = manager.getReadableDatabase();
            Cursor cursor = db.query(DatabaseManager.TABLE_DEVICE, new String[] {"_id", "dev_nickname", "dev_uid", "dev_name", "dev_pwd",
                    "view_acc", "view_pwd", "event_notification", "camera_channel", "snapshot", "ask_format_sdcard", "dev_ap", "dev_ap_pwd",
                    "wifi_ssid", "wifi_pwd"}, null, null, null, null, "_id LIMIT " + MultiViewActivity.CAMERA_MAX_LIMITS);
            while (cursor.moveToNext()) {
                long db_id = cursor.getLong(0);
                String dev_nickname = cursor.getString(1);
                String dev_uid = cursor.getString(2);
                String view_acc = cursor.getString(5);
                String view_pwd = cursor.getString(6);
                int event_notification = cursor.getInt(7);
                int channel = cursor.getInt(8);
                byte[] bytsSnapshot = cursor.getBlob(9);
                int ask_format_sdcard = cursor.getInt(10);
                String ap = cursor.getString(11);
                String ap_pwd = cursor.getString(12);
                String wifi = cursor.getString(13);
                String wifi_pwd = cursor.getString(14);
                Bitmap snapshot = (bytsSnapshot != null && bytsSnapshot.length > 0) ? DatabaseManager.getBitmapFromByteArray(bytsSnapshot) : null;

                MyCamera camera = new MyCamera(dev_nickname, dev_uid, view_acc, view_pwd);
                DeviceInfo dev = new DeviceInfo(db_id, camera.getUUID(), dev_nickname, dev_uid, view_acc, view_pwd, "", event_notification,
                        channel, snapshot, ap, ap_pwd, wifi, wifi_pwd);
                dev.ShowTipsForFormatSDCard = ask_format_sdcard == 1;
                DeviceList.add(dev);

                camera.registerIOTCListener(InitCamActivity.this);
                camera.connect(dev_uid);
                camera.start(Camera.DEFAULT_AV_CHANNEL, view_acc, view_pwd);
                camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETSUPPORTSTREAM_REQ, SMsgAVIoctrlGetSupportStreamReq
                        .parseContent());
//                camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_REQ, AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoReq
//                        .parseContent());
//                camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETAUDIOOUTFORMAT_REQ, AVIOCTRLDEFs
//                        .SMsgAVIoctrlGetAudioOutFormatReq.parseContent());
//                camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIMEZONE_REQ, AVIOCTRLDEFs.SMsgAVIoctrlTimeZone
//                        .parseContent());
//                if (MultiViewActivity.SupportOnDropbox) {
//                    camera.sendIOCtrl(MyCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_SAVE_DROPBOX_REQ, "0".getBytes());
//                }

                camera.LastAudioMode = 1;

                CameraList.add(camera);
            }

            cursor.close();
            db.close();
            INIT_CAMERA_LIST_OK();
        }

    }

    public final static String getEventType (Context context, int eventType, boolean isSearch) {

        String result = "";

        switch (eventType) {
            case AVIOCTRLDEFs.AVIOCTRL_EVENT_ALL:
                result = isSearch ? context.getText(R.string.evttype_all).toString() : context.getText(R.string.evttype_fulltime_recording)
                        .toString();
                break;

            case AVIOCTRLDEFs.AVIOCTRL_EVENT_MOTIONDECT:
                result = context.getText(R.string.evttype_motion_detection).toString();
                break;

            case AVIOCTRLDEFs.AVIOCTRL_EVENT_VIDEOLOST:
                result = context.getText(R.string.evttype_video_lost).toString();
                break;

            case AVIOCTRLDEFs.AVIOCTRL_EVENT_IOALARM:
                result = context.getText(R.string.evttype_io_alarm).toString();
                break;

            case AVIOCTRLDEFs.AVIOCTRL_EVENT_MOTIONPASS:
                result = context.getText(R.string.evttype_motion_pass).toString();
                break;

            case AVIOCTRLDEFs.AVIOCTRL_EVENT_VIDEORESUME:
                result = context.getText(R.string.evttype_video_resume).toString();
                break;

            case AVIOCTRLDEFs.AVIOCTRL_EVENT_IOALARMPASS:
                result = context.getText(R.string.evttype_io_alarm_pass).toString();
                break;

            case AVIOCTRLDEFs.AVIOCTRL_EVENT_MOVIE:
                result = context.getText(R.string.evttype_manual_recording).toString();
                break;

            case AVIOCTRLDEFs.AVIOCTRL_EVENT_TIME_LAPSE:
                result = context.getText(R.string.evttype_time_lapse).toString();
                break;

            case AVIOCTRLDEFs.AVIOCTRL_EVENT_EMERGENCY:
                result = context.getText(R.string.evttype_emg_recording).toString();
                break;

            case AVIOCTRLDEFs.AVIOCTRL_EVENT_EXPT_REBOOT:
                result = context.getText(R.string.evttype_expt_reboot).toString();
                break;

            case AVIOCTRLDEFs.AVIOCTRL_EVENT_SDFAULT:
                result = context.getText(R.string.evttype_sd_fault).toString();
                break;

            case AVIOCTRLDEFs.AVIOCTRL_EVENT_FULLTIME_RECORDING:
                result = context.getText(R.string.evttype_fulltime_recording).toString();
                break;
        }

        return result;
    }

    private class ResultStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive (Context context, Intent intent) {
            String result = intent.getStringExtra("dev_uid");
            if (result == null) {

            } else {
                for (int i = 0 ; i < DeviceList.size() ; i++) {
                    if (DeviceList.get(i).UID.equals(intent.getStringExtra("dev_uid")) == true) {
                        DeviceList.get(i).n_gcm_count++;
                    }
                }
            }
        }
    }

    private String getSessionMode (int mode) {

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

    private String getPerformance (int mode) {

        String result = "";
        if (mode < 30)
            result = getText(R.string.txtBad).toString();
        else if (mode < 60)
            result = getText(R.string.txtNormal).toString();
        else
            result = getText(R.string.txtGood).toString();

        return result;
    }

    public static void check_mapping_list (Context context) {
        DatabaseManager manager = new DatabaseManager(context);
        SQLiteDatabase db = manager.getReadableDatabase();
        Cursor cursor = db.query(DatabaseManager.TABLE_DEVICE, new String[] {"dev_uid"}, null, null, null, null, null);
        if (cursor != null) {
            SharedPreferences settings = context.getSharedPreferences("Preference", 0);
            cursor.moveToFirst();
            for (int i = 0 ; i < cursor.getCount() ; i++) {
                String uid = cursor.getString(0);
                settings.edit().putString(uid, uid).commit();
                ThreadTPNS threadTPNS = new ThreadTPNS(context, uid, ThreadTPNS.MAPPING_REG);
                threadTPNS.start();
                cursor.moveToNext();

            }
            cursor.close();
        }
        db.close();
        check_remove_list(context);
    }

    public static void check_remove_list (Context context) {
        DatabaseManager manager = new DatabaseManager(context);
        SQLiteDatabase db = manager.getReadableDatabase();
        Cursor cursor = db.query(DatabaseManager.TABLE_REMOVE_LIST, new String[] {"uid"}, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            for (int i = 0 ; i < cursor.getCount() ; i++) {
                String uid = cursor.getString(0);
                ThreadTPNS thread = new ThreadTPNS(context, uid, ThreadTPNS.MAPPING_UNREG);
                thread.start();
                cursor.moveToNext();

            }
            cursor.close();
        }
        db.close();
    }

    @Override
    public void receiveFrameData (final Camera camera, int sessionChannel, Bitmap bmp) {
    }

    @Override
    public void receiveFrameInfo (final Camera camera, int sessionChannel, long bitRate, int frameRate, int onlineNm, int frameCount, int
            incompleteFrameCount) {
    }

    @Override
    public void receiveSessionInfo (final Camera camera, int resultCode) {

        Bundle bundle = new Bundle();
        bundle.putString("requestDevice", ((MyCamera) camera).getUUID());

        Message msg = handler.obtainMessage();
        msg.what = resultCode;
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    @Override
    public void receiveChannelInfo (final Camera camera, int sessionChannel, int resultCode) {

        Bundle bundle = new Bundle();
        bundle.putString("requestDevice", ((MyCamera) camera).getUUID());
        bundle.putInt("sessionChannel", sessionChannel);

        Message msg = handler.obtainMessage();
        msg.what = resultCode;
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    @Override
    public void receiveIOCtrlData (final Camera camera, int sessionChannel, int avIOCtrlMsgType, byte[] data) {

        Bundle bundle = new Bundle();
        bundle.putString("requestDevice", ((MyCamera) camera).getUUID());
        bundle.putInt("sessionChannel", sessionChannel);
        bundle.putByteArray("data", data);

        Message msg = handler.obtainMessage();
        msg.what = avIOCtrlMsgType;
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage (Message msg) {

            Bundle bundle = msg.getData();
            String requestDevice = bundle.getString("requestDevice");

            byte[] data = bundle.getByteArray("data");
//			int i = 0;

            DeviceInfo device = null;
            MyCamera camera = null;

            for (int i = 0 ; i < DeviceList.size() ; i++) {

                if (DeviceList.get(i).UUID.equalsIgnoreCase(requestDevice)) {
                    device = DeviceList.get(i);
                    break;
                }
            }

            for (int i = 0 ; i < CameraList.size() ; i++) {

                if (CameraList.get(i).getUUID().equalsIgnoreCase(requestDevice)) {
                    camera = CameraList.get(i);
                    break;
                }
            }

            switch (msg.what) {

                case Camera.CONNECTION_STATE_CONNECTING:

                    if (camera != null) {
                        if (! camera.isSessionConnected() || ! camera.isChannelConnected(0)) {
                            if (device != null) {
                                device.Status = getText(R.string.connstus_connecting).toString();
                                device.Online = false;

                            }
                        }
                    }
                    CONNECTION_STATE_CONNECTING(true);

                    break;

                case Camera.CONNECTION_STATE_CONNECTED:

                    if (camera != null) {
                        if (camera.isSessionConnected() && camera.isChannelConnected(0)) {
                            if (device != null) {
                                device.Status = getText(R.string.connstus_connected).toString();
                                device.Online = true;
                                device.connect_count = 0;
                                if (m_threadReconnect != null) {
                                    m_threadReconnect.stopCheck = true;
                                    m_threadReconnect.interrupt();
                                    m_threadReconnect = null;
                                    device.connect_count++;
                                }
                                if (device.ChangePassword) {
                                    device.ChangePassword = false;
                                    ThreadTPNS threadTPNS = new ThreadTPNS(InitCamActivity.this, device.UID, ThreadTPNS.MAPPING_REG);
                                    threadTPNS.start();
                                    DatabaseManager manager = new DatabaseManager(InitCamActivity.this);
                                    manager.delete_remove_list(device.UID);
                                }

                            }
                        }
                    }
                    if (device != null)
                        CONNECTION_STATE_CONNECTED(true, device.UID);

                    break;

                case Camera.CONNECTION_STATE_UNKNOWN_DEVICE:

                    if (device != null) {
                        device.Status = getText(R.string.connstus_unknown_device).toString();
                        device.Online = false;

                    }

                    if (camera != null) {
                        camera.disconnect();
                    }
                    CONNECTION_STATE_UNKNOWN_DEVICE(true);

                    break;

                case Camera.CONNECTION_STATE_DISCONNECTED:
                    // no Use
                    if (device != null) {
                        device.Status = getText(R.string.connstus_disconnect).toString();
                        device.Online = false;

//					if (device.connect_count < 3 && noResetWiFi) {
//						if (m_threadReconnect == null) {
//							startTime = System.currentTimeMillis();
//							m_threadReconnect = new ThreadReconnect(camera, device);
//							m_threadReconnect.start();
//							device.connect_count++;
//						}
////						reconnect(camera,device);
//					}
                    }

                    if (camera != null) {
                        camera.disconnect();
                    }

                    CONNECTION_STATE_DISCONNECTED(true);
                    break;

                case Camera.CONNECTION_STATE_TIMEOUT:

                    if (device != null) {
                        device.Status = getText(R.string.connstus_disconnect).toString();
                        device.Online = false;

                        camera.disconnect();
//					if (device.connect_count < 3 && noResetWiFi) {
//						if (m_threadReconnect == null) {
//							startTime = System.currentTimeMillis();
//							m_threadReconnect = new ThreadReconnect(camera, device);
//							m_threadReconnect.start();
//							device.connect_count++;
//						}
////						reconnect(camera,device);
//					} else if (device.connect_count >= 3) {
//						camera.disconnect();
//					}
                    }
                    CONNECTION_STATE_TIMEOUT(true);
                    break;

                case Camera.CONNECTION_STATE_WRONG_PASSWORD:

                    if (device != null) {
                        device.Status = getText(R.string.connstus_wrong_password).toString();
                        device.Online = false;
                        ThreadTPNS threadTPNS = new ThreadTPNS(InitCamActivity.this, device.UID, ThreadTPNS.MAPPING_UNREG);
                        threadTPNS.start();
                    }

                    if (camera != null) {
                        final MyCamera finalCam = camera;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run () {
                                finalCam.disconnect();
                            }
                        }, 1000);
                    }
                    CONNECTION_STATE_WRONG_PASSWORD(true);
                    break;

                case Camera.CONNECTION_STATE_CONNECT_FAILED:

                    if (device != null) {
                        device.Status = getText(R.string.connstus_connection_failed).toString();
                        device.Online = false;

                    }
                    CONNECTION_STATE_CONNECT_FAILED(true);
                    break;

                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_EVENT_REPORT:

                    if (! mSupportBaidu)
                        IOTYPE_USER_IPCAM_EVENT_REPORT(device, data);
                    break;

                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_RESP:

                    IOTYPE_USER_IPCAM_DEVINFO_RESP(camera, device, data);
                    break;

                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_FORMATEXTSTORAGE_RESP:

                    IOTYPE_USER_IPCAM_FORMATEXTSTORAGE_RESP(data);
                    break;

                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_SAVE_DROPBOX_RESP:

                    if (MultiViewActivity.SupportOnDropbox) {
                        SMsgAVIoctrlGetDropbox SMsgAVIoctrlGetDropbox = new SMsgAVIoctrlGetDropbox(data);
                        device.nLinked = SMsgAVIoctrlGetDropbox.nLinked;
                        device.nSupportDropbox = SMsgAVIoctrlGetDropbox.nSupportDropbox;
                        if (SMsgAVIoctrlGetDropbox.szLinkUDID.equals(DatabaseManager.uid_Produce(InitCamActivity.this))) {
                            device.nLinked = 1;
                        } else {
                            device.nLinked = 0;
                        }
                    }

                    break;

                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETWIFI_RESP:

                    break;

            }

            if (device != null && camera != null)
                device.Mode = camera.getSessionMode();

            super.handleMessage(msg);
        }
    };

    class ThreadReconnect extends Thread {
        boolean stopCheck = true;
        Activity mActivity = null;
        Camera mReconnectCamera = null;
        DeviceInfo mReconnectDevice = null;

        public ThreadReconnect (Camera camera, DeviceInfo dev) {
            stopCheck = false;
            mReconnectCamera = camera;
            mReconnectDevice = dev;
        }

        public void run () {
            while (! stopCheck) {

                long endTime = System.currentTimeMillis();

                if (endTime - startTime > 30000) {

                    mReconnectCamera.disconnect();
                    mReconnectCamera.connect(mReconnectDevice.UID);
                    mReconnectCamera.start(Camera.DEFAULT_AV_CHANNEL, mReconnectDevice.View_Account, mReconnectDevice.View_Password);
                    mReconnectCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_REQ, AVIOCTRLDEFs
                            .SMsgAVIoctrlDeviceInfoReq.parseContent());
                    mReconnectCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETSUPPORTSTREAM_REQ, SMsgAVIoctrlGetSupportStreamReq.parseContent());
                    mReconnectCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETAUDIOOUTFORMAT_REQ, AVIOCTRLDEFs
                            .SMsgAVIoctrlGetAudioOutFormatReq.parseContent());
                    mReconnectCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIMEZONE_REQ, AVIOCTRLDEFs
                            .SMsgAVIoctrlTimeZone.parseContent());
                    if (MultiViewActivity.SupportOnDropbox) {
                        mReconnectCamera.sendIOCtrl(MyCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_SAVE_DROPBOX_REQ, "0".getBytes());
                    }
                    stopCheck = true;
                }

            }
            ;
        }
    }

    public static void isAddToCloud (boolean isAddTo) {
        mIsAddToCloud = isAddTo;
    }

    public static void setCandidate (String uid, String name, String type) {
        mCandidateUID = uid;
        mCandidateNickName = name;
        mCandidateType = type;
    }

    private class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive (Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

                if (WifiAdmin.isConnect()) {
                    if (! mCandidateUID.equals("") && mIsSetWifiSeccess && mIsAddToCloud) {

                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("cmd", "create");
                            jsonObject.put("usr", DatabaseManager.getLoginAccount());
                            jsonObject.put("pwd", DatabaseManager.getLoginPassword());
                            jsonObject.put("uid", mCandidateUID);
                            jsonObject.put("name", mCandidateNickName);
                            jsonObject.put("type", mCandidateType);

                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        mCandidateUID = "";
                        mCandidateNickName = "";
                        mCandidateType = "";
                        mIsSetWifiSeccess = false;
                        mIsAddToCloud = false;
                    }
                } else {

                    Glog.V(TAG, "Wifi does not connected");
                }
            }

        }
    }

    protected void INIT_CAMERA_LIST_OK () {
        // TODO Auto-generated method stub

    }

    protected void IOTYPE_USER_IPCAM_DEVINFO_RESP (MyCamera camera, DeviceInfo device, byte[] data) {
        // TODO Auto-generated method stub

    }

    protected void IOTYPE_USER_IPCAM_FORMATEXTSTORAGE_RESP (byte[] data) {
        // TODO Auto-generated method stub

    }

    protected void IOTYPE_USER_IPCAM_EVENT_REPORT (DeviceInfo DeviceInfo, byte[] data) {
        // TODO Auto-generated method stub
	};

    protected void CONNECTION_STATE_CONNECTING (boolean Status) {
        // TODO Auto-generated method stub

    }

    protected void CONNECTION_STATE_CONNECTED (boolean Status, String UID) {
        // TODO Auto-generated method stub

    }

    protected void CONNECTION_STATE_UNKNOWN_DEVICE (boolean Status) {
        // TODO Auto-generated method stub

    }

    protected void CONNECTION_STATE_DISCONNECTED (boolean Status) {
        // TODO Auto-generated method stub

    }

    protected void CONNECTION_STATE_TIMEOUT (boolean Status) {
        // TODO Auto-generated method stub

    }

    protected void CONNECTION_STATE_WRONG_PASSWORD (boolean Status) {
        // TODO Auto-generated method stub

    }

    protected void CONNECTION_STATE_CONNECT_FAILED (boolean Status) {
        // TODO Auto-generated method stub

    }

    @Override
    public void receiveFrameDataForMediaCodec (Camera camera, int avChannel, byte[] buf, int length, int pFrmNo, byte[] pFrmInfoBuf, boolean
            isIframe, int codecId) {
        // TODO Auto-generated method stub

    }

    //Check if support GooglePlayServices
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                finish();
            }
            return false;
        }
        return true;
    }

}
