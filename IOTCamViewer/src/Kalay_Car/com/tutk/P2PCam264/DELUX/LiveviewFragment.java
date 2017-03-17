package com.tutk.P2PCam264.DELUX;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.AVIOCTRLDEFs.SMsgAVIoctrlGetSupportStreamReq;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.IRegisterIOTCListener;
import com.tutk.IOTC.Monitor;
import com.tutk.Kalay.general.R;
import com.tutk.Logger.Glog;
import com.tutk.P2PCam264.DELUX.Multi_Setting_custom_Dialog.On_Dialog_button_click_Listener;
import com.tutk.P2PCam264.DeviceInfo;
import com.tutk.P2PCam264.EditDeviceActivity;
import com.tutk.P2PCam264.LiveViewActivity;
import com.tutk.P2PCam264.MyCamera;

import java.io.File;
import java.util.ArrayList;

import addition.TUTK.AddDeviceActivity;
import addition.TUTK.AddDeviceTipsActivity;
import appteam.WifiAdmin;
import general.DatabaseManager;

public class LiveviewFragment extends Fragment implements IRegisterIOTCListener, View.OnClickListener, On_Dialog_button_click_Listener {

    private static final String TAG = "MultiViewActivity";
    private static final String TAG_RECONNECT = "Reconnect";

    private static final int CLICK_BUTTON1 = 0;
    private static final int CLICK_BUTTON2 = 1;
    private static final int CLICK_BUTTON3 = 2;
    private static final int CLICK_BUTTON4 = 3;
    private static final int CLICK_BUTTON5 = 4;
    private static final int CLICK_BUTTON6 = 5;

    // private ArrayList<Chaanel_to_Monitor_Info> mChaanel_Info;

    private static final int MultiView_NUM = 4;

    private View layout;
    private WifiAdmin WifiAdmin;

    private Monitor[] marrMonitor = new Monitor[MultiView_NUM];
    private MyCamera[] marrCamera = new MyCamera[MultiView_NUM];
    private DeviceInfo[] marrDevice = new DeviceInfo[MultiView_NUM];
    private Chaanel_to_Monitor_Info[] Monitor_Info_Array = new Chaanel_to_Monitor_Info[MultiView_NUM];
    private MultiViewStatusBar[] marrStatusBar = new MultiViewStatusBar[MultiView_NUM];

    private String[] muidMatrix = new String[MultiView_NUM];
    private String[] muuidMatrix = new String[MultiView_NUM];
    private int[] mnSelChannelID = new int[MultiView_NUM];
    private ArrayList<Boolean> subDelList = new ArrayList<Boolean>();
    private ArrayList<Integer> subMonitorList = new ArrayList<Integer>();
    private ArrayList<Boolean> addingList = new ArrayList<Boolean>();
    private boolean mCallResume = false;
    private boolean mIsRemove = false;
    private boolean mChangeWiFi = false;
    private boolean mGoLiveView = false;
    private boolean mTheFirst = true;
    private boolean mIsFocused = true;
    private int Page;
    private int mSelectIndex = -1;
    private Handler handler = new Handler();

    private int[] marrMonitorResId = {R.id.mv_monitor_0, R.id.mv_monitor_1, R.id.mv_monitor_2, R.id.mv_monitor_3,
//			R.id.mv_monitor_4,
//			R.id.mv_monitor_5,
    };

    private int[] marrMonitorPlaceBtnResId = {R.id.btn_monitor_place_1, R.id.btn_monitor_place_2, R.id.btn_monitor_place_3, R.id.btn_monitor_place_4,
//			R.id.btn_monitor_place_5,
//			R.id.btn_monitor_place_6,
    };
    private int[] marrStatusBarResId = {R.id.status_bar1, R.id.status_bar2, R.id.status_bar3, R.id.status_bar4,
//			R.id.status_bar5,
//			R.id.status_bar6,
    };

    private int[] SettingButtonResId = {R.id.Settingbutton1, R.id.Settingbutton2, R.id.Settingbutton3, R.id.Settingbutton4,
//			R.id.Settingbutton5,
//			R.id.Settingbutton6,
    };

    private int[] layoutDelResId = {R.id.layoutDelete_0, R.id.layoutDelete_1, R.id.layoutDelete_2, R.id.layoutDelete_3};
    private int[] DeleteButtonResId = {R.id.btn_delete_0, R.id.btn_delete_1, R.id.btn_delete_2, R.id.btn_delete_3,};
    private int[] ImgNewResId = {R.id.img_new_0, R.id.img_new_1, R.id.img_new_2, R.id.img_new_3};
    private int[] layoutProgressResId = {R.id.layout_progress_1, R.id.layout_progress_2, R.id.layout_progress_3, R.id.layout_progress_4};

    // private GaBtnHighlightOnTouchListener mGaBtnHighlightOnTouchListener = new
    // GaBtnHighlightOnTouchListener();

    public static LiveviewFragment newInstance (ArrayList<Chaanel_to_Monitor_Info> Chaanel_Info, int page) {
        LiveviewFragment fragment = new LiveviewFragment(Chaanel_Info, page);

        return fragment;
    }

    public LiveviewFragment () {
        // TODO Auto-generated constructor stub
    }

    public LiveviewFragment (ArrayList<Chaanel_to_Monitor_Info> Chaanel_Info, int page) {
        super();
        // mChaanel_Info = Chaanel_Info;
        Page = page;
        for (Chaanel_to_Monitor_Info Chaanel_to_Monitor_Info : Chaanel_Info) {
            if (Chaanel_to_Monitor_Info.MonitorIndex < MultiView_NUM) {
                Monitor_Info_Array[Chaanel_to_Monitor_Info.MonitorIndex] = Chaanel_to_Monitor_Info;
                muidMatrix[Chaanel_to_Monitor_Info.MonitorIndex] = Chaanel_to_Monitor_Info.UID;
                muuidMatrix[Chaanel_to_Monitor_Info.MonitorIndex] = Chaanel_to_Monitor_Info.UUID;
                mnSelChannelID[Chaanel_to_Monitor_Info.MonitorIndex] = Chaanel_to_Monitor_Info.ChannelIndex;
            }
        }

        addingList.clear();
        for (int i = 0 ; i < 4 ; i++) {
            addingList.add(false);
        }
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // Retain this fragment across configuration changes.
        setRetainInstance(true);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        layout = use_multi_templates(4, inflater, container);
//		LocationViews();
//		InitMultiView(layout);

        new Thread(new Runnable() {
            @Override
            public void run () {
                LocationViews();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run () {
                        InitMultiView(layout);
                    }
                });

//                if (livaviewFragmentAdapter.page == 0 && livaviewFragmentAdapter.page == Page) {
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run () {
//                            start();
//                        }
//                    }, 2000);
//                }
            }

        }).start();

        return layout;
    }

    @Override
    public void onConfigurationChanged (Configuration newConfig) {
        getFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void LocationViews () {
    }

    private void InitMultiView (View layout) {
        int nIdx = 0;
        for (String uid : muidMatrix) {

            if (nIdx >= MultiView_NUM) {
                Glog.E(TAG, "Too many camera!");
                break;
            }
            boolean bFound = false;
            for (MyCamera camera : MultiViewActivity.CameraList) {
                if (muidMatrix[nIdx] != null && muuidMatrix[nIdx] != null) {
                    if (muidMatrix[nIdx].equalsIgnoreCase(camera.getUID()) && muuidMatrix[nIdx].equalsIgnoreCase(camera.getUUID())) {
                        marrCamera[Monitor_Info_Array[nIdx].MonitorIndex] = camera;
                        marrCamera[Monitor_Info_Array[nIdx].MonitorIndex].setMonitorIndex(Monitor_Info_Array[nIdx].MonitorIndex);
                        bFound = true;
                        break;
                    }
                }
            }

            if (bFound) {
                bFound = false;
                for (DeviceInfo dev : MultiViewActivity.DeviceList) {
                    if (muidMatrix[nIdx] != null && muuidMatrix[nIdx] != null) {
                        if (muidMatrix[nIdx].equalsIgnoreCase(dev.UID) && muuidMatrix[nIdx].equalsIgnoreCase(dev.UUID)) {
                            marrDevice[Monitor_Info_Array[nIdx].MonitorIndex] = dev;
                            marrDevice[Monitor_Info_Array[nIdx].MonitorIndex].mMonitorIndex = Monitor_Info_Array[nIdx].MonitorIndex;
                            marrDevice[Monitor_Info_Array[nIdx].MonitorIndex].ChannelIndex = mnSelChannelID[nIdx];

                            bFound = true;
                            break;
                        }
                    }
                }
            }
            nIdx++;
        }

        nIdx = 0;
        for (Monitor monitor : marrMonitor) {
            if (monitor != null) {
                monitor.deattachCamera();
            }
            if (marrCamera[nIdx] != null) {
                monitor = null;
                monitor = (Monitor) layout.findViewById(marrMonitorResId[nIdx]);
                if (monitor != null) {
                    int nSelectedChannel = mnSelChannelID[nIdx];
                    monitor.setPTZ(false);
                    monitor.setFixXY(true);
                    monitor.setMaxZoom(3.0f);
                    monitor.mEnableDither = marrCamera[nIdx].mEnableDither;
                    final int Fidx = nIdx;
                    final Monitor Fmonitor = monitor;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run () {
                            if (marrDevice[Fidx].Online) {
                                Fmonitor.setVisibility(View.VISIBLE);
                            } else {
                                Fmonitor.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            } else {
                monitor = (Monitor) layout.findViewById(marrMonitorResId[nIdx]);
                if (monitor != null) {
                    final Monitor Fmonitor = monitor;

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run () {
                            Fmonitor.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }

            monitor.setOnClickListener(this);
            marrMonitor[nIdx] = monitor;
            nIdx++;
        }

        nIdx = 0;
        for (int nResId : marrStatusBarResId) {
            marrStatusBar[nIdx] = new MultiViewStatusBar((View) layout.findViewById(nResId));
            final int Fidx = nIdx;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run () {
                    marrStatusBar[Fidx].InitStatusBar(Fidx, marrDevice[Fidx], LiveviewFragment.this);
                }
            });
            nIdx++;
        }
        nIdx = 0;
        ImageButton btn = null;
        for (int nResId : marrMonitorPlaceBtnResId) {
            btn = (ImageButton) layout.findViewById(nResId);
            final int Fidx = nIdx;
            final ImageButton Fbtn = btn;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run () {
                    Fbtn.setOnClickListener(LiveviewFragment.this);
                    ChangePlaceBtn(Fidx);
                }
            });
            nIdx++;
        }
        for (int nResId : SettingButtonResId) {
            btn = (ImageButton) layout.findViewById(nResId);
            final ImageButton Fbtn = btn;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run () {
                    Fbtn.setOnClickListener(LiveviewFragment.this);
                }
            });
        }

        nIdx = 0;
        for (MyCamera camera : marrCamera) {

            if (camera != null) {
                camera.registerIOTCListener(this);

                if (! camera.isSessionConnected()) {

                    camera.connect(muidMatrix[nIdx]);
                    camera.start(Camera.DEFAULT_AV_CHANNEL, marrDevice[nIdx].View_Account, marrDevice[nIdx].View_Password);
                    camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETSUPPORTSTREAM_REQ,
                            SMsgAVIoctrlGetSupportStreamReq.parseContent());
                    camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_REQ, AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoReq
                            .parseContent());
                    camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETAUDIOOUTFORMAT_REQ, AVIOCTRLDEFs
                            .SMsgAVIoctrlGetAudioOutFormatReq.parseContent());
                    camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIMEZONE_REQ, AVIOCTRLDEFs.SMsgAVIoctrlTimeZone
                            .parseContent());

                }

            }
            nIdx++;
        }

        final View Flayout = layout;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run () {
                if (mIsRemove) {
                    for (int index = 0 ; index < MultiView_NUM ; index++) {
                        if (marrDevice[index] != null) {
                            RelativeLayout layoutDel = (RelativeLayout) Flayout.findViewById(layoutDelResId[index]);
                            ImageButton btnDel = (ImageButton) Flayout.findViewById(DeleteButtonResId[index]);
                            layoutDel.setVisibility(View.VISIBLE);
                            layoutDel.setOnClickListener(LiveviewFragment.this);
                            btnDel.setVisibility(View.VISIBLE);
                            btnDel.setOnClickListener(LiveviewFragment.this);
                            if (subDelList.get(index)) {
                                btnDel.setBackgroundResource(R.drawable.btn_delete_h);
                            } else {
                                btnDel.setBackgroundResource(R.drawable.btn_delete);
                            }
                        } else {
                            RelativeLayout layoutDel = (RelativeLayout) Flayout.findViewById(layoutDelResId[index]);
                            ImageButton btnDel = (ImageButton) Flayout.findViewById(DeleteButtonResId[index]);

                            layoutDel.setVisibility(View.VISIBLE);
                            layoutDel.setOnClickListener(LiveviewFragment.this);
                            btnDel.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });

        nIdx = 0;
        for (boolean isNew : addingList) {
            ImageView img = (ImageView) layout.findViewById(ImgNewResId[nIdx]);
            final ImageView Fimg = img;
            final boolean FisNew = isNew;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run () {
                    if (FisNew) {
                        Fimg.setVisibility(View.VISIBLE);
                    } else {
                        Fimg.setVisibility(View.GONE);
                    }
                }
            });
            nIdx++;
        }
    }

    private void ChangePlaceBtn (int Index) {
        if(getActivity() == null){
            return;
        }

        if (marrDevice[Index] == null) {
            ImageButton btn = (ImageButton) layout.findViewById(marrMonitorPlaceBtnResId[Index]);
            ImageButton imagebtn = (ImageButton) layout.findViewById(SettingButtonResId[Index]);
            ImageView img = (ImageView) layout.findViewById(ImgNewResId[Index]);
            RelativeLayout progress = (RelativeLayout) layout.findViewById(layoutProgressResId[Index]);
            if (imagebtn != null) {
                imagebtn.setVisibility(View.GONE);
            }
            if (marrMonitor[Index] != null) {
                marrMonitor[Index].setVisibility(View.GONE);
            }
            if (btn != null) {
                btn.setImageResource(R.drawable.btn_add_device_ok_switch);
            }
            if (img != null) {
                img.setVisibility(View.GONE);
            }
            if (progress != null) {
                progress.setVisibility(View.GONE);
            }
            if(addingList.size() > Index)
                addingList.set(Index, false);
        } else {
            if (! marrDevice[Index].Online) {
                ImageButton btn = (ImageButton) layout.findViewById(marrMonitorPlaceBtnResId[Index]);
                RelativeLayout progress = (RelativeLayout) layout.findViewById(layoutProgressResId[Index]);
                if (btn != null) {
                    if (marrDevice[Index].Status.equals(getString(R.string.connstus_wrong_password)) || marrDevice[Index].Status.equals(getString(R
                            .string.connstus_unknown_device))) {
                        btn.setImageResource(R.drawable.btn_error);
                        if (progress != null) {
                            progress.setVisibility(View.GONE);
                        }
                    } else if (marrDevice[Index].Status.equals(getString(R.string.connstus_connecting))) {
                        if (progress != null) {
                            progress.setVisibility(View.VISIBLE);
                        }
                    } else {
                        btn.setImageResource(R.drawable.btn_refresh_switch);
                        if (progress != null) {
                            progress.setVisibility(View.GONE);
                        }
                    }

                    btn.setVisibility(View.VISIBLE);
                }
                ImageButton imagebtn = (ImageButton) layout.findViewById(SettingButtonResId[Index]);
                if (imagebtn != null) {
                    imagebtn.setVisibility(View.VISIBLE);
                }

                if (marrCamera[Index] != null && marrMonitor[Index] != null && marrDevice[Index] != null && livaviewFragmentAdapter.page == Page) {
                    stoping_monitor(marrCamera[Index], marrMonitor[Index], marrDevice[Index], mnSelChannelID[Index], true);
                }
            } else {
                ImageButton imagebtn = (ImageButton) layout.findViewById(SettingButtonResId[Index]);
                if (imagebtn != null) {
                    imagebtn.setVisibility(View.VISIBLE);
                }

                if (marrCamera[Index] != null && marrMonitor[Index] != null && marrDevice[Index] != null && livaviewFragmentAdapter.page == Page) {
                    playing_monitor(marrCamera[Index], marrMonitor[Index], marrDevice[Index], mnSelChannelID[Index]);
                }
            }
        }
    }

    private View use_multi_templates (int Type, LayoutInflater inflater, ViewGroup container) {
        switch (Type) {
            case 1:
                return inflater.inflate(R.layout.multi_monitor_1, container, false);
            case 2:
                return inflater.inflate(R.layout.multi_monitor_2, container, false);
            case 3:
                return inflater.inflate(R.layout.multi_monitor_3, container, false);
            case 4:
                return inflater.inflate(R.layout.multi_monitor_4, container, false);
            case 5:
                return inflater.inflate(R.layout.multi_monitor_5, container, false);
            case 6:
                return inflater.inflate(R.layout.multi_monitor_6, container, false);
            default:
                return inflater.inflate(R.layout.multi_monitor_6, container, false);

        }
    }

    public void checkPlayingChannel(String uid){
        int index = 0;
        for(DeviceInfo dev : marrDevice){
            if(dev != null) {
                if (dev.UID.equals(uid) && mnSelChannelID[index] == 0) {
                    mGoLiveView = true;
                    mSelectIndex = index;
                    break;
                }
            }
            index ++;
        }
    }

    @Override
    public void receiveFrameData (Camera camera, int avChannel, Bitmap bmp) {
        // TODO Auto-generated method stub

    }

    @Override
    public void receiveFrameInfo (Camera camera, int avChannel, long bitRate, int frameRate, int onlineNm, int frameCount, int incompleteFrameCount) {
        // TODO Auto-generated method stub

    }

    @Override
    public void receiveSessionInfo (Camera camera, int resultCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void receiveChannelInfo (Camera camera, int avChannel, int resultCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void receiveIOCtrlData (Camera camera, int avChannel, int avIOCtrlMsgType, byte[] data) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onPause () {
        super.onPause();
        mIsFocused = false;
        if(!mGoLiveView) {
            stop();
        }else {
            stopAndGoLiveView();
            mGoLiveView = false;
        }
        stop();
    }

    @Override
    public void onDestroy () {
        super.onDestroy();
        stop();
    }

    @Override
    public void onResume () {
        super.onResume();
        mIsFocused = true;
        if (mCallResume) {
//            start();
            startBackFromLiveView();
            mCallResume = false;
        }
    }

    @Override
    public void onDestroyView () {
        super.onDestroyView();
    }

    // 暫停所有畫面
    public void stop () {

        new Thread(new Runnable() {
            @Override
            public void run () {
                int nIdx = 0;
                for (MyCamera camera : marrCamera) {
                    if (camera != null) {
                        stoping_monitor(camera, marrMonitor[nIdx], marrDevice[nIdx], mnSelChannelID[nIdx], true);
                    }
                    nIdx++;
                }
            }
        }).start();

    }

    private void stopAndGoLiveView () {

        new Thread(new Runnable() {
            @Override
            public void run () {
                int nIdx = 0;
                DeviceInfo dev = marrDevice[mSelectIndex];
                String uid = dev.UID;
                int channel = mnSelChannelID[mSelectIndex];
                boolean result = false;
                for (MyCamera camera : marrCamera) {
                    if (camera != null) {
                        if(nIdx != mSelectIndex) {
                            if(uid.equals(marrDevice[nIdx].UID) && channel == mnSelChannelID[nIdx]){
                                result = camera.stopShowWithoutIOCtrl(mnSelChannelID[nIdx]);
                                marrMonitor[nIdx].deattachCamera();

                                if(!MultiViewActivity.mStartShowWithoutIOCtrl && result){
                                    MultiViewActivity.mStartShowWithoutIOCtrl = true;
                                }
                            }else{
                                stoping_monitor(camera, marrMonitor[nIdx], marrDevice[nIdx], mnSelChannelID[nIdx], true);
                            }
                        }else{
                            result = camera.stopShowWithoutIOCtrl(mnSelChannelID[nIdx]);
                            marrMonitor[nIdx].deattachCamera();

                            if(!MultiViewActivity.mStartShowWithoutIOCtrl && result){
                                MultiViewActivity.mStartShowWithoutIOCtrl = true;
                            }
                        }
                    }
                    nIdx++;
                }
            }
        }).start();

    }

    private void startBackFromLiveView(){
        new Thread(new Runnable() {
            @Override
            public void run () {
                int nIdx = 0;
                for (MyCamera camera : marrCamera) {
                    if (camera != null) {
                        if(nIdx != mSelectIndex) {
                            playing_monitor(camera, marrMonitor[nIdx], marrDevice[nIdx], nIdx);
                        }else{
                            marrMonitor[nIdx].mEnableDither = camera.mEnableDither;
                            marrMonitor[nIdx].attachCamera(camera, mnSelChannelID[nIdx]);
                            camera.startShowWithoutIOCtrl(mnSelChannelID[nIdx], true);
                            mSelectIndex = -1;
                        }
                    }
                    nIdx++;
                }
            }
        }).start();
    }

    // 暫停指定channel畫面
    public void stopOneChannel (int Index) {
        stopOneChannel(Index, true);
    }

    public void stopOneChannel (int Index, boolean seedStopShow) {
        stoping_monitor(marrCamera[Index], marrMonitor[Index], marrDevice[Index], mnSelChannelID[Index], seedStopShow);
    }

    // IOTC_StartShow: 當有同多個monitor載入同一個camera 及同一個channel 不能執行 stopshow
    private void stoping_monitor (MyCamera Camera, Monitor Monitor, DeviceInfo Device, int SelectedChannel, boolean IOTC_StopShow) {
        if (Camera != null) {
            if (IOTC_StopShow) {
                // Camera.stopSpeaking(SelectedChannel);
                // Camera.stopListening(SelectedChannel);
                Camera.stopShow(SelectedChannel);
            }

            if (Monitor != null) {
                Monitor.deattachCamera();
            }
        }
    }

    // 確認目前有無多個Monitor綁定同一個UID同一個channel
    private boolean Check_Same_channel_toMonitor (String UID, String UUID, int ChannelIndex) {
        int SameCount = 0;
        for (int i = 0 ; i < marrCamera.length ; i++) {
            if (marrDevice[i] != null) {
                if (marrDevice[i].UID.equalsIgnoreCase(UID) && marrDevice[i].UUID.equalsIgnoreCase(UUID) && mnSelChannelID[i] == ChannelIndex) {
                    SameCount++;
                }
            }
        }
        if (SameCount > 1) {
            return true;
        } else {
            return false;
        }
    }

    // 啟動所有畫面
    public void start () {

        new Thread(new Runnable() {
            @Override
            public void run () {
                int nIdx = 0;
                for (MyCamera camera : marrCamera) {
                    if (camera != null) {
                        playing_monitor(camera, marrMonitor[nIdx], marrDevice[nIdx], nIdx);
                    }
                    nIdx++;
                }
            }
        }).start();
    }

    // 啟動指定channel的畫面
    public void startOneChannel (int Index) {
        playing_monitor(marrCamera[Index], marrMonitor[Index], marrDevice[Index], Index);
    }

    // 啟動指定uid的畫面
    public void startOneUID (String UID) {
        int nIdx = 0;
        for (DeviceInfo device : marrDevice) {
            if (device != null) {
                if (device.UID.equalsIgnoreCase(UID)) {
                    // playing_monitor(marrCamera[nIdx],marrMonitor[nIdx],marrDevice[nIdx],nIdx);
                    // int nSelectedChannel = mnSelChannelID[nIdx];

                    // if (marrMonitor[nIdx] != null) {
                    // marrMonitor[nIdx].mEnableDither = marrCamera[nIdx].mEnableDither;
                    // marrMonitor[nIdx].attachCamera(marrCamera[nIdx], nSelectedChannel);
                    // }
                }
            }
            nIdx++;
        }

    }

    private void playing_monitor (final MyCamera Camera, Monitor Monitor, DeviceInfo Device, int Index) {
        if (Camera != null) {
            final int nSelectedChannel = mnSelChannelID[Index];

            if (Monitor != null) {
                // if(!Monitor.mEnableDither)
                // {
                Monitor.mEnableDither = Camera.mEnableDither;
                Monitor.attachCamera(Camera, nSelectedChannel);
                // }
                if(mIsFocused) {
                    Camera.startShow(nSelectedChannel, true, true);
                    if (! Camera.mIsShow) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run () {
                                Camera.startShow(nSelectedChannel, true, true);
                                Delay_to_get_info_for_NVT(Camera);
                            }
                        }, 3000);
                    } else {
                        Delay_to_get_info_for_NVT(Camera);
                    }
                }
            }
        }
    }

    // 每當連線狀態更新時 需要更新狀態攔
    public void reflash_Status () {
        int nIdx = 0;
        for (MultiViewStatusBar MultiViewStatusBar : marrStatusBar) {
            if (MultiViewStatusBar != null) {
                MultiViewStatusBar.ChangeStatusBar(nIdx, marrDevice[nIdx], marrMonitor[nIdx], this);
                ChangePlaceBtn(nIdx);
            }
            nIdx++;
        }

    }

    // 特定uid連線成功時
    public void connected_Status (String UID) {
        // startOneUID(UID);
    }

    private void create_dialog (On_Dialog_button_click_Listener _On_button_click_Listener, int index, boolean device_online) {
        Multi_Setting_custom_Dialog Multi_Setting_custom_Dialog = new Multi_Setting_custom_Dialog(getActivity(), index, device_online);
        Multi_Setting_custom_Dialog.set_button_click_Listener(this);
        Window window = Multi_Setting_custom_Dialog.getWindow();
        window.setWindowAnimations(R.style.setting_dailog_animstyle);
        Multi_Setting_custom_Dialog.show();
    }

    private void setNewIcon (int index) {
        if (layout != null) {
            ImageView img = (ImageView) layout.findViewById(ImgNewResId[index]);
            img.setVisibility(View.VISIBLE);
        }
        addingList.set(index, true);
    }

    @Override
    public void onClick (View v) {
        // TODO Auto-generated method stub

        switch (v.getId()) {
            case R.id.Settingbutton1:
                Glog.D(TAG, "ClickSettingButton( " + 1 + " )...");
                create_dialog(this, CLICK_BUTTON1, marrDevice[CLICK_BUTTON1].Online);
                break;
            case R.id.Settingbutton2:
                Glog.D(TAG, "ClickSettingButton( " + 2 + " )...");
                create_dialog(this, CLICK_BUTTON2, marrDevice[CLICK_BUTTON2].Online);
                break;
            case R.id.Settingbutton3:
                Glog.D(TAG, "ClickSettingButton( " + 3 + " )...");
                create_dialog(this, CLICK_BUTTON3, marrDevice[CLICK_BUTTON3].Online);
                break;
            case R.id.Settingbutton4:
                Glog.D(TAG, "ClickSettingButton( " + 4 + " )...");
                create_dialog(this, CLICK_BUTTON4, marrDevice[CLICK_BUTTON4].Online);
                break;
            case R.id.Settingbutton5:
                Glog.D(TAG, "ClickSettingButton( " + 5 + " )...");
                create_dialog(this, CLICK_BUTTON5, marrDevice[CLICK_BUTTON5].Online);
                break;
            case R.id.Settingbutton6:
                Glog.D(TAG, "ClickSettingButton( " + 6 + " )...");
                create_dialog(this, CLICK_BUTTON6, marrDevice[CLICK_BUTTON6].Online);
                break;
            case R.id.btn_monitor_place_1:
                IntentDeviceList(CLICK_BUTTON1, livaviewFragmentAdapter.page);
                break;
            case R.id.btn_monitor_place_2:
                IntentDeviceList(CLICK_BUTTON2, livaviewFragmentAdapter.page);
                break;
            case R.id.btn_monitor_place_3:
                IntentDeviceList(CLICK_BUTTON3, livaviewFragmentAdapter.page);
                break;
            case R.id.btn_monitor_place_4:
                IntentDeviceList(CLICK_BUTTON4, livaviewFragmentAdapter.page);
                break;
            case R.id.btn_monitor_place_5:
                IntentDeviceList(CLICK_BUTTON5, livaviewFragmentAdapter.page);
                break;
            case R.id.btn_monitor_place_6:
                IntentDeviceList(CLICK_BUTTON6, livaviewFragmentAdapter.page);
                break;
            case R.id.mv_monitor_0:
                IntentLiveView(CLICK_BUTTON1);
                break;
            case R.id.mv_monitor_1:
                IntentLiveView(CLICK_BUTTON2);
                break;
            case R.id.mv_monitor_2:
                IntentLiveView(CLICK_BUTTON3);
                break;
            case R.id.mv_monitor_3:
                IntentLiveView(CLICK_BUTTON4);
                break;
            case R.id.mv_monitor_4:
                IntentLiveView(CLICK_BUTTON5);
                break;
            case R.id.mv_monitor_5:
                IntentLiveView(CLICK_BUTTON6);
                break;
            case R.id.btn_delete_0:
                changeDelList(0);
                break;
            case R.id.btn_delete_1:
                changeDelList(1);
                break;
            case R.id.btn_delete_2:
                changeDelList(2);
                break;
            case R.id.btn_delete_3:
                changeDelList(3);
                break;
            default:
                break;
        }
    }

    public boolean layoutReady () {
        if (layout != null) {
            return true;
        } else {
            return false;
        }
    }

    public void ClearNewIcon (int index) {
        if (index == MultiViewActivity.CLEAR_ALL) {
            addingList.clear();
            for (int i = 0 ; i < 4 ; i++) {
                addingList.add(false);
                if (layout != null) {
                    ImageView img = (ImageView) layout.findViewById(ImgNewResId[i]);
                    img.setVisibility(View.GONE);
                }
            }
        } else {
            addingList.set(index, false);
            if (layout != null) {
                ImageView img = (ImageView) layout.findViewById(ImgNewResId[index]);
                img.setVisibility(View.GONE);
            }
        }
    }

    // 塞入Monitor新的畫面
    public void SetMonitor (Chaanel_to_Monitor_Info Chaanel_to_Monitor_Info, int MonitorIndex, boolean isNew) {
        // 如果原本不為空 先停止撥放原本的
        if (marrDevice[MonitorIndex] != null) {
            stoping_monitor(marrCamera[MonitorIndex], marrMonitor[MonitorIndex], marrDevice[MonitorIndex], mnSelChannelID[MonitorIndex], !
                    Check_Same_channel_toMonitor(marrDevice[MonitorIndex].UID, marrDevice[MonitorIndex].UUID, mnSelChannelID[MonitorIndex]));
        }

        int old_mnSelChannelID = mnSelChannelID[MonitorIndex];

        Monitor_Info_Array[MonitorIndex] = Chaanel_to_Monitor_Info;
        muidMatrix[MonitorIndex] = Chaanel_to_Monitor_Info.UID;
        muuidMatrix[MonitorIndex] = Chaanel_to_Monitor_Info.UUID;
        mnSelChannelID[MonitorIndex] = Chaanel_to_Monitor_Info.ChannelIndex;

        if (muidMatrix[MonitorIndex] != null && muuidMatrix[MonitorIndex] != null) {
            for (MyCamera camera : MultiViewActivity.CameraList) {
                if (muidMatrix[MonitorIndex].equalsIgnoreCase(camera.getUID()) && muuidMatrix[MonitorIndex].equalsIgnoreCase(camera.getUUID())) {
                    marrCamera[MonitorIndex] = null;
                    marrCamera[MonitorIndex] = camera;
                    marrCamera[MonitorIndex].setMonitorIndex(Chaanel_to_Monitor_Info.MonitorIndex);
                    break;
                }
            }
            for (DeviceInfo dev : MultiViewActivity.DeviceList) {

                if (muidMatrix[MonitorIndex].equalsIgnoreCase(dev.UID) && muuidMatrix[MonitorIndex].equalsIgnoreCase(dev.UUID)
                /* && mnSelChannelID[MonitorIndex] == old_mnSelChannelID */) {
                    marrDevice[MonitorIndex] = null;
                    marrDevice[MonitorIndex] = dev;
                    marrDevice[MonitorIndex].ChannelIndex = Chaanel_to_Monitor_Info.ChannelIndex;
                    marrDevice[MonitorIndex].mMonitorIndex = Chaanel_to_Monitor_Info.MonitorIndex;
                    break;
                }
            }
        } else {
            marrCamera[MonitorIndex] = null;
            marrDevice[MonitorIndex] = null;

        }
        if (marrMonitor[MonitorIndex] != null) {
            marrMonitor[MonitorIndex].deattachCamera();
        }
        if (layout != null) {
            if (marrCamera[MonitorIndex] != null) {

                int nSelectedChannel = mnSelChannelID[MonitorIndex];

                marrMonitor[MonitorIndex] = null;
                marrMonitor[MonitorIndex] = (Monitor) layout.findViewById(marrMonitorResId[MonitorIndex]);
                if (marrMonitor[MonitorIndex] != null) {
                    marrMonitor[MonitorIndex].setPTZ(false);
                    marrMonitor[MonitorIndex].setFixXY(true);
                    marrMonitor[MonitorIndex].setMaxZoom(3.0f);
                    marrMonitor[MonitorIndex].mEnableDither = marrCamera[MonitorIndex].mEnableDither;
                    marrMonitor[MonitorIndex].attachCamera(marrCamera[MonitorIndex], nSelectedChannel);
                }
            } else {
                marrMonitor[MonitorIndex] = (Monitor) layout.findViewById(marrMonitorResId[MonitorIndex]);
                if (marrMonitor[MonitorIndex] != null) {
                    marrMonitor[MonitorIndex].setVisibility(View.INVISIBLE);
                }
            }
        }
        if (marrMonitor[MonitorIndex] != null) {
            marrMonitor[MonitorIndex].setOnClickListener(this);
        }
        if (marrCamera[MonitorIndex] != null) {
            marrCamera[MonitorIndex].registerIOTCListener(this);

            if (! marrCamera[MonitorIndex].isSessionConnected()) {

                marrCamera[MonitorIndex].connect(muidMatrix[MonitorIndex]);
                marrCamera[MonitorIndex].start(Camera.DEFAULT_AV_CHANNEL, marrDevice[MonitorIndex].View_Account, marrDevice[MonitorIndex]
                        .View_Password);
                marrCamera[MonitorIndex].sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETSUPPORTSTREAM_REQ,
                        SMsgAVIoctrlGetSupportStreamReq.parseContent());
                marrCamera[MonitorIndex].sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_REQ, AVIOCTRLDEFs
                        .SMsgAVIoctrlDeviceInfoReq.parseContent());
                marrCamera[MonitorIndex].sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETAUDIOOUTFORMAT_REQ, AVIOCTRLDEFs
                        .SMsgAVIoctrlGetAudioOutFormatReq.parseContent());
                marrCamera[MonitorIndex].sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIMEZONE_REQ, AVIOCTRLDEFs
                        .SMsgAVIoctrlTimeZone.parseContent());
                if (MultiViewActivity.SupportOnDropbox) {
                    marrCamera[MonitorIndex].sendIOCtrl(MyCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_SAVE_DROPBOX_REQ, "0"
                            .getBytes());
                }
            }
        }
        reflash_Status();

        if (isNew) {
            setNewIcon(MonitorIndex);
        }
    }

    public void DeleteAllMonitor (String uid, String uuid) {
        int index = 0;
        // 同一台camera可能對應好幾個monitor 故要搜尋
        for (MyCamera camera : marrCamera) {
            if (camera != null) {
                if (camera.getUID().equalsIgnoreCase(uid) && camera.getUUID().equalsIgnoreCase(uuid)) {
                    StopAndClearMonitor(index);

                }
            }
            index++;
        }

    }

    public void DeleteOneMonitor (String uid, String uuid, int MonitorIndex, boolean seedStopShow) {
        int index = MonitorIndex % 4;

        if (marrCamera[index] == null) {
            return;
        }
        if (marrCamera[index].getUID().equalsIgnoreCase(uid) && marrCamera[index].getUUID().equalsIgnoreCase(uuid)) {
            Multi_StopAndClearMonitor(MonitorIndex, seedStopShow);
        }
    }

    private void StopAndClearMonitor (int index) {
        StopAndClearMonitor(index, true);
    }

    // 取消綁定Monitor 要把Monitor清掉
    private void StopAndClearMonitor (int index, boolean seedStopShow) {
        stopOneChannel(index, seedStopShow);
        int monitorIndex = livaviewFragmentAdapter.page * 4 + index;
        DatabaseManager DatabaseManager = new DatabaseManager(getActivity());
        DatabaseManager.remove_Device_Channel_Allonation_To_MonitorByUID(marrCamera[index].getUID(), mnSelChannelID[index], monitorIndex);
        DatabaseManager = null;
        muidMatrix[index] = null;
        muuidMatrix[index] = null;
        mnSelChannelID[index] = 0;
        marrCamera[index] = null;
        marrDevice[index] = null;
        Monitor_Info_Array[index] = null;
        marrStatusBar[index].ChangeStatusBar(index, marrDevice[index], marrMonitor[index], this);
        ChangePlaceBtn(index);
    }

    private void Multi_StopAndClearMonitor (int MonitorIndex, boolean seedStopShow) {
        int index = MonitorIndex % 4;
        stopOneChannel(index, seedStopShow);
        DatabaseManager DatabaseManager = new DatabaseManager(getActivity());
        DatabaseManager.remove_Device_Channel_Allonation_To_MonitorByUID(marrCamera[index].getUID(), mnSelChannelID[index], MonitorIndex);
        DatabaseManager = null;
        muidMatrix[index] = null;
        muuidMatrix[index] = null;
        mnSelChannelID[index] = 0;
        marrCamera[index] = null;
        marrDevice[index] = null;
        Monitor_Info_Array[index] = null;
        marrStatusBar[index].ChangeStatusBar(index, marrDevice[index], marrMonitor[index], this);
        ChangePlaceBtn(index);
    }

    // 前往liveview
    private void IntentLiveView (int index) {
        if (marrDevice[index] == null) {
            return;
        }

        if (marrCamera[index].mUID == null) {
            marrCamera[index].mUID = marrDevice[index].UID;
        }
        addingList.set(index, false);
        if (layout != null) {
            ImageView img = (ImageView) layout.findViewById(ImgNewResId[index]);
            img.setVisibility(View.GONE);
        }

        mGoLiveView = true;
        mSelectIndex = index;

        Bundle extras = new Bundle();
        extras.putString("dev_uid", marrDevice[index].UID);
        extras.putString("dev_uuid", marrDevice[index].UUID);
        extras.putString("dev_nickname", marrDevice[index].NickName);
        extras.putString("conn_status", marrDevice[index].Status);
        extras.putString("view_acc", marrDevice[index].View_Account);
        extras.putString("view_pwd", marrDevice[index].View_Password);
        extras.putInt("camera_channel", mnSelChannelID[index]);
        extras.putInt("MonitorIndex", index);

        extras.putString("OriginallyUID", marrDevice[index].UID);
        extras.putInt("OriginallyChannelIndex", mnSelChannelID[index]);
        Intent intent = new Intent();
        intent.putExtras(extras);
        intent.setClass(getActivity(), LiveViewActivity.class);
        getActivity().startActivityForResult(intent, MultiViewActivity.REQUEST_CODE_CAMERA_VIEW);
        getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    // 前往devicelist
    private void IntentDeviceList (int index, int page) {
        int monitorIndex = page * 4 + index;

        // 沒有綁定機器 跳去adddevice
        if (marrDevice[index] == null) {
            Intent intent = new Intent();
            Bundle extras = new Bundle();
            extras.putString("OriginallyUID", null);
            extras.putInt("OriginallyChannelIndex", 0);
            extras.putInt("MonitorIndex", monitorIndex);
//            intent.setClass(getActivity(), AddDeviceTipsActivity.class);
            intent.setClass(getActivity(), AddDeviceActivity.class);
            intent.putExtras(extras);
//            getActivity().startActivityForResult(intent, MultiViewActivity.REQUEST_CODE_CAMERA_SELECT_MONITOR);
            getActivity().startActivityForResult(intent, MultiViewActivity.REQUEST_CODE_CAMERA_ADD_ONLY);
            getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        }
        // 已綁定機器 重新連線
        else {
            if ((marrDevice[index].Status.equals(getText(R.string.connstus_unknown_device).toString()) || marrDevice[index].Status.equals(getText(R
                    .string.connstus_disconnect).toString()) || marrDevice[index].Status.equals(getText(R.string.connstus_connection_failed)
                    .toString())) && ! mChangeWiFi) {
                mChangeWiFi = true;
                stopOneChannel(index);
                marrCamera[index].disconnect();

                final RelativeLayout progress = (RelativeLayout) layout.findViewById(layoutProgressResId[index]);
                progress.setVisibility(View.VISIBLE);

                WifiAdmin = new WifiAdmin(getActivity());
                if (WifiAdmin.checkState() != WifiManager.WIFI_STATE_ENABLED) {
                    Glog.D(TAG_RECONNECT, "open WiFi");
                    WifiAdmin.openWifi();
                }

                final int indexF = index;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run () {
                        if (WifiAdmin.checkState() == WifiManager.WIFI_STATE_ENABLED) {
                            Glog.D(TAG_RECONNECT, "WiFi is open");
                            if (marrDevice[indexF].WIFI.length() != 0) {
                                if (ChangeWiFi(marrDevice[indexF].WIFI, marrDevice[indexF].WIFI_Pwd)) {
                                    Glog.D(TAG_RECONNECT, "change to Network");
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run () {
                                            if (WifiAdmin.isWifi()) {
                                                Glog.D(TAG_RECONNECT, "WiFi connected");
                                                marrCamera[indexF].connect(marrDevice[indexF].UID);
                                                marrCamera[indexF].start(Camera.DEFAULT_AV_CHANNEL, marrDevice[indexF].View_Account,
                                                        marrDevice[indexF].View_Password);
                                                marrCamera[indexF].sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs
                                                        .IOTYPE_USER_IPCAM_DEVINFO_REQ, AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoReq.parseContent());
                                                marrCamera[indexF].sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs
                                                        .IOTYPE_USER_IPCAM_GETSUPPORTSTREAM_REQ, SMsgAVIoctrlGetSupportStreamReq.parseContent());
                                                marrCamera[indexF].sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs
                                                        .IOTYPE_USER_IPCAM_GETAUDIOOUTFORMAT_REQ, AVIOCTRLDEFs.SMsgAVIoctrlGetAudioOutFormatReq.parseContent());
                                                marrCamera[indexF].sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs
                                                        .IOTYPE_USER_IPCAM_GET_TIMEZONE_REQ, AVIOCTRLDEFs.SMsgAVIoctrlTimeZone.parseContent());
//                                                startOneChannel(indexF);
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run () {
                                                        if (marrDevice[indexF].Status.equals(getText(R.string.connstus_unknown_device).toString())
                                                                || marrDevice[indexF].Status.equals(getText(R.string.connstus_disconnect).toString
                                                                ()) || marrDevice[indexF].Status.equals(getText(R.string
                                                                .connstus_connection_failed).toString())) {

                                                            Glog.D(TAG_RECONNECT, "Network Failed");
                                                            stopOneChannel(indexF);
                                                            marrCamera[indexF].disconnect();
                                                            progress.setVisibility(View.VISIBLE);

                                                            reconnectWithAP(indexF, progress);

                                                        } else if (marrDevice[indexF].Status.equals(getText(R.string.connstus_connected).toString()
                                                        )) {
                                                            Glog.D(TAG_RECONNECT, "Network Success");
                                                            mChangeWiFi = false;
                                                        } else {
                                                            Glog.D(TAG_RECONNECT, "Network ELSE retry");
                                                            handler.postDelayed(this, 2000);
                                                        }
                                                    }
                                                }, 10000);
                                            } else {
                                                if (WifiAdmin.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                                                    Custom_Ok_img_Dialog dlg = new Custom_Ok_img_Dialog(getActivity(), "WiFi err");
                                                    dlg.setCanceledOnTouchOutside(false);
                                                    Window window = dlg.getWindow();
                                                    window.setWindowAnimations(R.style.setting_dailog_animstyle);
                                                    dlg.show();
                                                    progress.setVisibility(View.GONE);
                                                    mChangeWiFi = false;
                                                } else {
                                                    Glog.D(TAG_RECONNECT, "WiFi still connecting");
                                                    handler.postDelayed(this, 2000);
                                                }
                                            }
                                        }
                                    }, 2000);
                                } else {
                                    reconnectWithAP(indexF, progress);
                                }
                            } else {
                                reconnectWithAP(indexF, progress);
                            }
                        } else {
                            handler.postDelayed(this, 2000);
                        }
                    }
                }, 5000);
            }
        }

    }

    public ArrayList<Integer> getMonitorList (int mPage) {

        subMonitorList.clear();

        for (int index = 0 ; index < MultiView_NUM ; index++) {
            int MonitorIndex = mPage * 4 + index;
            if (marrDevice[index] != null) {
                subMonitorList.add(MonitorIndex);
            } else {
                subMonitorList.add(- 1);
            }
        }

        return subMonitorList;
    }

    public ArrayList<Boolean> showDelelteLayout () {

        subDelList.clear();
        mIsRemove = true;
        if (layout != null) {
            for (int index = 0 ; index < MultiView_NUM ; index++) {

                if (marrDevice[index] != null) {
                    RelativeLayout layoutDel = (RelativeLayout) layout.findViewById(layoutDelResId[index]);
                    ImageButton btnDel = (ImageButton) layout.findViewById(DeleteButtonResId[index]);

                    layoutDel.setVisibility(View.VISIBLE);
                    layoutDel.setOnClickListener(this);
                    btnDel.setVisibility(View.VISIBLE);
                    btnDel.setBackgroundResource(R.drawable.btn_delete);
                    btnDel.setOnClickListener(this);
                } else {
                    RelativeLayout layoutDel = (RelativeLayout) layout.findViewById(layoutDelResId[index]);
                    ImageButton btnDel = (ImageButton) layout.findViewById(DeleteButtonResId[index]);

                    layoutDel.setVisibility(View.VISIBLE);
                    layoutDel.setOnClickListener(this);
                    btnDel.setVisibility(View.GONE);
                }
                subDelList.add(false);
            }
        } else {
            for (int index = 0 ; index < MultiView_NUM ; index++) {
                subDelList.add(false);
            }
        }

        return subDelList;
    }

    public void hideDelelteLayout () {

        mIsRemove = false;
        if (layout != null) {
            for (int index = 0 ; index < MultiView_NUM ; index++) {
                if (marrDevice[index] != null) {
                    RelativeLayout layoutDel = (RelativeLayout) layout.findViewById(layoutDelResId[index]);
                    ImageButton btnDel = (ImageButton) layout.findViewById(DeleteButtonResId[index]);

                    layoutDel.setVisibility(View.GONE);
                    layoutDel.setOnClickListener(this);
                    btnDel.setBackgroundResource(R.drawable.btn_delete);
                    btnDel.setOnClickListener(this);
                } else {
                    RelativeLayout layoutDel = (RelativeLayout) layout.findViewById(layoutDelResId[index]);
                    ImageButton btnDel = (ImageButton) layout.findViewById(DeleteButtonResId[index]);

                    layoutDel.setVisibility(View.GONE);
                    layoutDel.setOnClickListener(this);
                    btnDel.setVisibility(View.GONE);
                }
            }
        }
    }

    private void changeDelList (int index) {
        ImageButton btnDel = (ImageButton) layout.findViewById(DeleteButtonResId[index]);
        if (subDelList.get(index)) {
            btnDel.setBackgroundResource(R.drawable.btn_delete);
        } else {
            btnDel.setBackgroundResource(R.drawable.btn_delete_h);
        }
        subDelList.set(index, ! subDelList.get(index));
    }

    public boolean checkDelete () {
        for (int index = 0 ; index < subDelList.size() ; index++) {
            if (subDelList.get(index)) {
                return true;
            }
        }
        return false;
    }

    public void doDelete (int page) {
        for (int index = 0 ; index < subDelList.size() ; index++) {
            int MonitorIndex = page * 4 + index;

            if (subDelList.get(index)) {
                DeleteOneMonitor(marrDevice[index].UID, marrDevice[index].UUID, MonitorIndex, ! Check_Same_channel_toMonitor(marrDevice[index].UID,
                        marrDevice[index].UUID, mnSelChannelID[index]));
            }
        }
    }

    public void viewOnResume () {
        mCallResume = true;
    }

    public void clearView () {
        if (getFragmentManager() != null) {
            getFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
        }
    }

    private void Delay_to_get_info_for_NVT(final MyCamera Camera){
        handler.postDelayed(new Runnable() {
            @Override
            public void run () {
                if(mTheFirst){
                    Camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_REQ, AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoReq
                            .parseContent());
                    Camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETAUDIOOUTFORMAT_REQ, AVIOCTRLDEFs
                            .SMsgAVIoctrlGetAudioOutFormatReq.parseContent());
                    Camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIMEZONE_REQ, AVIOCTRLDEFs.SMsgAVIoctrlTimeZone
                            .parseContent());
                    if (MultiViewActivity.SupportOnDropbox) {
                        Camera.sendIOCtrl(MyCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_SAVE_DROPBOX_REQ, "0".getBytes());
                    }
                    mTheFirst = false;
                }
            }
        },1500);
    }

    @Override
    public void cancel_click (DialogInterface Dialog) {
        // TODO Auto-generated method stub
    }

    @Override
    public void btn_event_click (DialogInterface Dialog, int index) {
        // TODO Auto-generated method stub

        if (marrDevice[index] == null) {
            return;
        }
        Bundle extras = new Bundle();
        extras.putString("dev_uid", marrDevice[index].UID);
        extras.putString("dev_uuid", marrDevice[index].UUID);
        extras.putString("dev_nickname", marrDevice[index].NickName);
        extras.putString("conn_status", marrDevice[index].Status);
        extras.putString("view_acc", marrDevice[index].View_Account);
        extras.putString("view_pwd", marrDevice[index].View_Password);
        extras.putInt("camera_channel", mnSelChannelID[index]);
        Intent intent = new Intent();
        intent.putExtras(extras);
//		intent.setClass(getActivity(), EventListActivity.class);
        intent.setClass(getActivity(), RemoteFileActivity.class);
        getActivity().startActivity(intent);
        getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    @Override
    public void btn_photo_click (DialogInterface Dialog, int index) {
        // TODO Auto-generated method stub
        if (marrDevice[index] == null) {
            return;
        }
//		File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Snapshot/" + marrDevice[index].UID);
//		File folder_video = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Record/" + marrDevice[index].UID);
//		String[] allFiles = folder.list();
//		String[] allVideos = folder_video.list();
//		Intent intent2 = new Intent(getActivity(), GridViewGalleryActivity.class);
//		intent2.putExtra("snap", marrDevice[index].UID);
//		intent2.putExtra("images_path", folder.getAbsolutePath());
//		intent2.putExtra("videos_path", folder_video.getAbsolutePath());
//		getActivity().startActivity(intent2);
//              getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

//        getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Snapshot/" + marrDevice[index].UID + "/CH1/");
        File folder_video = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Record/" + marrDevice[index].UID + "/CH1/");
        if (folder.exists()) {
            for (File file : folder.listFiles()) {
                getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            }
        }

        if (folder_video.exists()) {
            for (File file : folder_video.listFiles()) {
                getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            }
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setType("image/*");
        getActivity().startActivity(intent);
        getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    @Override
    public void btn_set_click (DialogInterface Dialog, int index) {
        // TODO Auto-generated method stub
        if (marrDevice[index] == null) {
            return;
        }
        Bundle extras = new Bundle();
        extras.putString("dev_uid", marrDevice[index].UID);
        extras.putString("dev_uuid", marrDevice[index].UUID);
        extras.putString("dev_nickname", marrDevice[index].NickName);
        extras.putString("conn_status", marrDevice[index].Status);
        extras.putString("view_acc", marrDevice[index].View_Account);
        extras.putString("view_pwd", marrDevice[index].View_Password);
        extras.putInt("camera_channel", mnSelChannelID[index]);
        Intent intent = new Intent();
        intent.putExtras(extras);
        intent.setClass(getActivity(), EditDeviceActivity.class);
        getActivity().startActivityForResult(intent, MultiViewActivity.REQUEST_CODE_CAMERA_EDIT);
        getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

    }

    @Override
    public void btn_delete_click (DialogInterface Dialog, final int index) {
        // TODO Auto-generated method stub
        MultiViewActivity.mDeleteUID = muidMatrix[index];

        Custom_OkCancle_Dialog dlg = new Custom_OkCancle_Dialog(getActivity(), getText(R.string.tips_remove_camera_monitor_confirm).toString());
        dlg.setCanceledOnTouchOutside(false);
        Window window = dlg.getWindow();
        window.setWindowAnimations(R.style.setting_dailog_animstyle);
        dlg.show();
    }

    @Override
    public void receiveFrameDataForMediaCodec (Camera camera, int avChannel, byte[] buf, int length, int pFrmNo, byte[] pFrmInfoBuf, boolean
            isIframe, int codecId) {
        // TODO Auto-generated method stub

    }

    private boolean ChangeWiFi (String ap, String ap_pwd) {
        Glog.D(TAG_RECONNECT, "change WiFi = " + ap + " pwd " + ap_pwd);

        boolean hasWifi = false;
        WifiAdmin.startScan();
        for (String search : WifiAdmin.getWifiListsSSID()) {
            if (search.equals(ap)) {
                hasWifi = true;
                break;
            }
        }

        if (! hasWifi) {
            Glog.D(TAG_RECONNECT, "Can not scan the SSID");
            return false;
        }

        WifiConfiguration WifiConfiguration = null;

        if (ap_pwd.length() == 0) {
            WifiConfiguration = WifiAdmin.CreateWifiInfo(ap, "", 1);
        } else {
            WifiConfiguration = WifiAdmin.CreateWifiInfo(ap, ap_pwd, 3);
        }
        if (WifiConfiguration != null) {
            WifiAdmin.openWifi();
            if (! WifiAdmin.addNetwork(WifiConfiguration)) {
                Glog.D(TAG_RECONNECT, "Can not ADD the SSID");
                return false;
            } else {
                Glog.D(TAG_RECONNECT, "change success");
                return true;
            }
        } else {
            Glog.D(TAG_RECONNECT, "WiFiAdmin err");
            return false;
        }
    }

    private void reconnectWithAP (final int indexF, final RelativeLayout progress) {
        if (ChangeWiFi(marrDevice[indexF].AP, marrDevice[indexF].AP_Pwd)) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run () {
                    if (WifiAdmin.isWifi()) {
                        marrCamera[indexF].connect(marrDevice[indexF].UID);
                        marrCamera[indexF].start(Camera.DEFAULT_AV_CHANNEL, marrDevice[indexF].View_Account, marrDevice[indexF].View_Password);
                        marrCamera[indexF].sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_REQ, AVIOCTRLDEFs
                                .SMsgAVIoctrlDeviceInfoReq.parseContent());
                        marrCamera[indexF].sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETSUPPORTSTREAM_REQ,
                                SMsgAVIoctrlGetSupportStreamReq.parseContent());
                        marrCamera[indexF].sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETAUDIOOUTFORMAT_REQ, AVIOCTRLDEFs
                                .SMsgAVIoctrlGetAudioOutFormatReq.parseContent());
                        marrCamera[indexF].sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIMEZONE_REQ, AVIOCTRLDEFs
                                .SMsgAVIoctrlTimeZone.parseContent());
                        startOneChannel(indexF);
                        mChangeWiFi = false;
                    } else {
                        handler.postDelayed(this, 2000);
                    }
                }
            }, 5000);
        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run () {
                    Custom_Ok_img_Dialog dlg = new Custom_Ok_img_Dialog(getActivity(), getText(R.string.tips_change_mode).toString());
                    dlg.setCanceledOnTouchOutside(false);
                    Window window = dlg.getWindow();
                    window.setWindowAnimations(R.style.setting_dailog_animstyle);
                    dlg.show();
                    progress.setVisibility(View.GONE);
                    mChangeWiFi = false;
                }
            });
        }
    }
}
