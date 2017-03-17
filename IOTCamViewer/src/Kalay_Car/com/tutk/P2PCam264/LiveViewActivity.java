package com.tutk.P2PCam264;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.text.format.Time;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.actionbarsherlock.app.SherlockActivity;
import com.tutk.IOTC.AVFrame;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.AVIOCTRLDEFs.SMsgAVIoctrlGetSupportStreamReq;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.CameraListener;
import com.tutk.IOTC.IMonitor;
import com.tutk.IOTC.IOTCAPIs;
import com.tutk.IOTC.IRegisterIOTCListener;
import com.tutk.IOTC.MediaCodecListener;
import com.tutk.IOTC.MediaCodecMonitor;
import com.tutk.IOTC.MonitorClickListener;
import com.tutk.IOTC.Packet;
import com.tutk.IOTC.St_SInfo;
import com.tutk.Kalay.general.R;
import com.tutk.P2PCam264.DELUX.MultiViewActivity;
import com.tutk.P2PCam264.ui.mySwitch;
import com.tutk.P2PCam264.ui.mySwitchButton;
import com.tutk.customized.command.CustomCommand;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Calendar;

import general.DatabaseManager;

public class LiveViewActivity extends SherlockActivity implements ViewSwitcher.ViewFactory, IRegisterIOTCListener, MonitorClickListener,
        OnTouchListener, CameraListener, MediaCodecListener {

    private static final int BUILD_VERSION_CODES_ICE_CREAM_SANDWICH = 14;
    private static final int STS_CHANGE_CHANNEL_STREAMINFO = 99;
    private static final int STS_SNAPSHOT_SCANED = 98;
    private static final int LEVEL_LOWEST = 5;
    private static final int LEVEL_LOW = 4;
    private static final int LEVEL_MEDIUM = 3;
    private static final int LEVEL_HIGH = 2;
    private static final int LEVEL_HIGHEST = 1;
    private static final int PRESET_FIRST = 1;
    private static final int PRESET_SECOND = 2;
    private static final int PRESET_THIRD = 3;
    private static final int PRESET_FOURTH = 4;
    private final int ROTATEMODE_NORMAL = 0;
    private final int ROTATEMODE_FLIP = 1;
    private final int ROTATEMODE_MIRROR = 2;
    private final int ROTATEMODE_FLIPANDMIRROR = 3;
    private final int BUBBLE_LEVEL_QVGA = 0;
    private final int BUBBLE_LEVEL_BRIGHT = 1;
    private final int BUBBLE_LEVEL_CONTRAST = 2;
    private final int TIME_LAPSE_1SEC = AVIOCTRLDEFs.TIME_LAPSE_1SEC;
    private final int TIME_LAPSE_5SEC = AVIOCTRLDEFs.TIME_LAPSE_5SEC;
    private final int TIME_LAPSE_10SEC = AVIOCTRLDEFs.TIME_LAPSE_10SEC;
    private final int TIME_LAPSE_30SEC = AVIOCTRLDEFs.TIME_LAPSE_30SEC;
    private final int TIME_LAPSE_1MIN = AVIOCTRLDEFs.TIME_LAPSE_1MIN;
    private final int TIME_LAPSE_5MIN = AVIOCTRLDEFs.TIME_LAPSE_5MIN;
    private final int TIME_LAPSE_10MIN = AVIOCTRLDEFs.TIME_LAPSE_10MIN;
    private final int TIME_LAPSE_30MIN = AVIOCTRLDEFs.TIME_LAPSE_30MIN;
    private final int TIME_LAPSE_1HR = AVIOCTRLDEFs.TIME_LAPSE_1HR;
    private final int TIME_LAPSE_2HR = AVIOCTRLDEFs.TIME_LAPSE_2HR;
    private final int TIME_LAPSE_3HR = AVIOCTRLDEFs.TIME_LAPSE_3HR;
    private final int TIME_LAPSE_1DAY = AVIOCTRLDEFs.TIME_LAPSE_1DAY;


    private final int THUMBNAIL_LIMIT_HEIGHT = 720;
    private final int THUMBNAIL_LIMIT_WIDTH = 1280;
    private final boolean SOFTWARE_DECODE = true;
    private final boolean HARDWARE_DECODE = false;

    //	private IMonitor monitor = null;
    private IMonitor mSoftMonitor = null;
    private IMonitor mHardMonitor = null;
    private MyCamera mCamera = null;
    private DeviceInfo mDevice = null;
    private SharedPreferences mCodecSettings;
    // private int SOPTZOOM=-1;

    private int mMonitorIndex = - 1;
    private int OriginallyChannelIndex = - 1;

    private TextView txt_title;
    private String mDevUID;
    private String mDevUUID;
    private String mConnStatus = "";
    private String mFilePath = "";
    private int mVideoFPS;
    private long mVideoBPS;
    private int mOnlineNm;
    private int mFrameCount;
    private int mIncompleteFrameCount;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mMiniVideoWidth;
    private int mMiniVideoHeight;
    private int mMiniMonitorHeight;
    private int mSelectedChannel;
    private int mQVGA = - 1;
    private int mEnv = - 1;
    private int mBrightness = - 1;
    private int mContrast = - 1;
    private int mTimeLapse = 1;
    private int mFlip = ROTATEMODE_NORMAL;
    private int mLevelPanel;
    private int mRecordingMode = mySwitchButton.MODE_TIME_LAPSE;
    private float mScale;

    private RelativeLayout toolbar_layout;
    private RelativeLayout linSpeaker;
    private RelativeLayout layoutTitleBar;
    private RelativeLayout mSoftMonitorLayout;
    private RelativeLayout mHardMonitorLayout;
    private RelativeLayout layoutSettings;
    private RelativeLayout layoutOperation;
    private LinearLayout linPnlCameraInfo;
    private LinearLayout nullLayout;

    private LinearLayout layoutSettingsMain;
    private LinearLayout layoutLevel;
    private LinearLayout layoutLevel_env;
    private LinearLayout layoutOperMain;
    private LinearLayout layoutOperFlip;
    private LinearLayout layoutOperTimeLapse;
    private RelativeLayout layoutBack;
    private RelativeLayout layoutQVGA;
    private RelativeLayout layoutEnv;
    private RelativeLayout layoutBright;
    private RelativeLayout layoutContrast;
    private RelativeLayout layoutMax;
    private RelativeLayout layoutHigh;
    private RelativeLayout layoutMedium;
    private RelativeLayout layoutLow;
    private RelativeLayout layoutMin;
    private RelativeLayout layout50hz;
    private RelativeLayout layout60hz;
    private RelativeLayout layoutOutdoor;
    private RelativeLayout layoutNight;
    private RelativeLayout layoutBack_oper;
    private RelativeLayout layoutFlip_title;
    private RelativeLayout layoutTimeLapse;
    private RelativeLayout layoutPreset;
    private RelativeLayout layoutCruise;
    private RelativeLayout layoutFlip;
    private RelativeLayout layoutMirror;
    private RelativeLayout layout_1sec;
    private RelativeLayout layout_5sec;
    private RelativeLayout layout_10sec;
    private RelativeLayout layout_30sec;
    private RelativeLayout layout_1min;
    private RelativeLayout layout_5min;
    private RelativeLayout layout_10min;
    private RelativeLayout layout_30min;
    private RelativeLayout layout_1hr;
    private RelativeLayout layout_2hr;
    private RelativeLayout layout_3hr;
    private RelativeLayout layout_1day;


    private boolean LevelFlip = false;
    private boolean VerticalFlip = false;
    private boolean isShowToolBar = true;
    private boolean isShowPreset = false;
    private boolean isShowCruise = false;
    private boolean isShowContrast = false;
    private boolean isShowBrightness = false;
    private boolean canSetContrast = false;
    private boolean canSetBrightness = false;

    // TOOLBAR
    private ImageButton button_toolbar_speaker;
    private ImageButton button_toolbar_snapshot;
    private ImageButton button_toolbar_operation;
    private ImageButton button_toolbar_settings;
    private ImageButton btn_speaker;

    // Bubble
    private TextView tvTitle_bubble;
    private TextView tvTitle_bubble_oper;
    private TextView tvQVGA;
    private TextView tvEnv;
    private TextView tvBright;
    private TextView tvContrast;
    private mySwitch swWDR;
    private Button btnOK_bubble;
    private Button btnOK_bubble_oper;
    private ImageButton btnBack_bubble;
    private ImageButton btnBack_bubble_oper;
    private ImageView imgTrin;
    private ImageView imgTrin_oper;
    private ImageView imgMax;
    private ImageView imgHigh;
    private ImageView imgMedium;
    private ImageView imgLow;
    private ImageView imgMin;
    private ImageView img50hz;
    private ImageView img60hz;
    private ImageView imgOutdoor;
    private ImageView imgNight;
    private ImageView img_1sec;
    private ImageView img_5sec;
    private ImageView img_10sec;
    private ImageView img_30sec;
    private ImageView img_1min;
    private ImageView img_5min;
    private ImageView img_10min;
    private ImageView img_30min;
    private ImageView img_1hr;
    private ImageView img_2hr;
    private ImageView img_3hr;
    private ImageView img_1day;

    private LinearLayout layoutRecording;
    private Button speaker_tips;

    private TextView txtConnectionSlash;
    private TextView txtResolutionSlash;
    private TextView txtShowFPS;
    private TextView txtFPSSlash;
    private TextView txtShowBPS;
    private TextView txtShowOnlineNumber;
    private TextView txtOnlineNumberSlash;
    private TextView txtShowFrameRatio;
    private TextView txtFrameCountSlash;
    private TextView txtQuality;
    private TextView txtRecvFrmPreSec;
    private TextView txtRecvFrmSlash;
    private TextView txtDispFrmPreSeco;

    private TextView txtConnectionStatus;
    private TextView txtConnectionMode;
    private TextView txtCodec;
    private TextView txtResolution;
    private TextView txtFrameRate;
    private TextView txtBitRate;
    private TextView txtOnlineNumber;
    private TextView txtFrameCount;
    private TextView txtIncompleteFrameCount;
    private TextView txtPerformance;
    private TextView tvRecording;

    private TextView tvScale;
    private TextView tvSwitch;

    private boolean mIsListening = false;
    private boolean mIsSpeaking = false;
    private boolean mIsRecording = false;
    private boolean mIsLapsing = false;
    private boolean unavailable = false;
    private boolean mShowSettingsBubble = false;
    private boolean mShowOperationBubble = false;
    private boolean mIsInit = false;
    private boolean mIsQuit = false;

    private BitmapDrawable bg;
    private BitmapDrawable bgSplit;

    private ImageButton btnMagicZoom;
    private mySwitchButton swRecording;
    private ImageView imgBattery;
    private ProgressBar progress;

    private Context mContext;
    private static String filename;
    //	private static boolean wait_receive = true;
    private ThreadTimer mThreadShowRecodTime;

    private enum FrameMode {
        PORTRAIT, LANDSCAPE_ROW_MAJOR, LANDSCAPE_COL_MAJOR
    }

    private FrameMode mFrameMode = FrameMode.PORTRAIT;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate (Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mContext = this;
        mIsInit = true;

//		setContentView(R.layout.live_view_portrait);

        bg = (BitmapDrawable) getResources().getDrawable(R.drawable.bg_striped);
        bgSplit = (BitmapDrawable) getResources().getDrawable(R.drawable.bg_striped_split_img);

        Bundle bundle = this.getIntent().getExtras();
        mDevUID = bundle.getString("dev_uid");
        mDevUUID = bundle.getString("dev_uuid");
        mConnStatus = bundle.getString("conn_status");
        mSelectedChannel = bundle.getInt("camera_channel");
        OriginallyChannelIndex = bundle.getInt("camera_channel");
        mMonitorIndex = bundle.getInt("MonitorIndex");

        for (MyCamera camera : MultiViewActivity.CameraList) {

            if (mDevUID.equalsIgnoreCase(camera.getUID()) && mDevUUID.equalsIgnoreCase(camera.getUUID())) {
                mCamera = camera;
                break;
            }
        }

        for (DeviceInfo dev : MultiViewActivity.DeviceList) {

            if (mDevUID.equalsIgnoreCase(dev.UID) && mDevUUID.equalsIgnoreCase(dev.UUID)) {
                mDevice = dev;
                break;
            }
        }

        if (mCamera != null) {

            mCamera.registerIOTCListener(this);
            mCamera.SetCameraListener(this);
            if (! mCamera.isSessionConnected()) {

                mCamera.connect(mDevUID);
                mCamera.start(Camera.DEFAULT_AV_CHANNEL, mDevice.View_Account, mDevice.View_Password);
                mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETSUPPORTSTREAM_REQ,
                        SMsgAVIoctrlGetSupportStreamReq.parseContent());
                mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_REQ,
                        AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoReq.parseContent());
                mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETAUDIOOUTFORMAT_REQ,
                        AVIOCTRLDEFs.SMsgAVIoctrlGetAudioOutFormatReq.parseContent());
                mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIMEZONE_REQ,
                        AVIOCTRLDEFs.SMsgAVIoctrlTimeZone.parseContent());
            }
        }

        mCodecSettings = getSharedPreferences("CodecSettings", 0);
        if(mCodecSettings != null){
            unavailable = mCodecSettings.getBoolean("unavailable", false);
        }

        Configuration cfg = getResources().getConfiguration();

        if (cfg.orientation == Configuration.ORIENTATION_PORTRAIT) {
            runOnUiThread(new Runnable() {
                @Override
                public void run () {
                    setupViewInPortraitLayout(unavailable);
                }
            });
        } else if (cfg.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            runOnUiThread(new Runnable() {
                @Override
                public void run () {
                    setupViewInLandscapeLayout(unavailable);
                }
            });
        }

        SharedPreferences TimeLapseSettings = getSharedPreferences("time_lapse_settings", 0);
        mTimeLapse = TimeLapseSettings.getInt(mDevUID, TIME_LAPSE_1SEC);
    }

    @Override
    protected void onStart () {
        super.onStart();
        // FlurryAgent.onStartSession(this, "Q1SDXDZQ21BQMVUVJ16W");
    }

    @Override
    protected void onStop () {
        super.onStop();
        // FlurryAgent.onEndSession(this);
    }

    @Override
    protected void onPause () {
        super.onPause();

        mIsInit = false;

        if (mCamera != null) {
            mCamera.unregisterIOTCListener(this);
            mCamera.stopSpeaking(mSelectedChannel);
            mCamera.stopListening(mSelectedChannel);
            if(mIsQuit){
                mCamera.stopShowWithoutIOCtrl(mSelectedChannel);
            }else {
                mCamera.stopShow(mSelectedChannel);
            }
        }
        if (mIsRecording) {
            mCamera.stopRecording();
            mThreadShowRecodTime.stopThread();
            swRecording.stopRecord();
            layoutRecording.setVisibility(View.GONE);
            mIsRecording = false;
            button_toolbar_snapshot.setEnabled(true);
            button_toolbar_speaker.setEnabled(true);
            button_toolbar_settings.setEnabled(true);
            button_toolbar_operation.setEnabled(true);
        }

        if (mSoftMonitor != null) {
            mSoftMonitor.deattachCamera();
        }
        if (mHardMonitor != null) {
            mHardMonitor.deattachCamera();
        }
    }

    @Override
    protected void onResume () {
        super.onResume();

        if (mCamera != null) {
            mCamera.registerIOTCListener(this);

//            mCamera.startShow(mSelectedChannel, true, unavailable);
            if(mIsInit && MultiViewActivity.mStartShowWithoutIOCtrl){
                mCamera.startShowWithoutIOCtrl(mSelectedChannel, unavailable);
                MultiViewActivity.mStartShowWithoutIOCtrl = false;
            }else {
                mCamera.startShow(mSelectedChannel, true, unavailable);
            }

            if (mIsListening) {
                mCamera.startListening(mSelectedChannel, mIsListening);
            }
        }

        if (unavailable) {
            mSoftMonitor.attachCamera(mCamera, mSelectedChannel);
        } else {
            mHardMonitor.attachCamera(mCamera, mSelectedChannel);
        }
    }

    @Override
    public void onConfigurationChanged (Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (mSoftMonitor != null) {
            mSoftMonitor.deattachCamera();
        }
        if (mHardMonitor != null) {
            mHardMonitor.deattachCamera();
        }

        Configuration cfg = getResources().getConfiguration();

        if (cfg.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (unavailable) {
                setupViewInLandscapeLayout(SOFTWARE_DECODE);
            } else {
                setupViewInLandscapeLayout(HARDWARE_DECODE);
            }

        } else if (cfg.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (unavailable) {
                setupViewInPortraitLayout(SOFTWARE_DECODE);
            } else {
                setupViewInPortraitLayout(HARDWARE_DECODE);
            }
        }
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MultiViewActivity.REQUEST_CODE_CAMERA_EDIT) {

            txt_title.setText(mDevice.NickName);
            switch (resultCode) {
                case MultiViewActivity.REQUEST_CODE_CAMERA_EDIT_DELETE_OK:
                    Bundle extras = data.getExtras();
                    Intent Intent = new Intent();
                    Intent.putExtras(extras);
                    setResult(MultiViewActivity.REQUEST_CODE_CAMERA_EDIT_DELETE_OK, Intent);
                    finish();

                    break;
            }
        }
    }

    private void setupViewInLandscapeLayout (final boolean runSoftwareDecode) {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.titlebar);
        actionBar.hide();

        setContentView(R.layout.live_view_landscape);

        toolbar_layout = (RelativeLayout) findViewById(R.id.toolbar_layout);
        layoutTitleBar = (RelativeLayout) findViewById(R.id.layoutTitleBar);
        layoutSettings = (RelativeLayout) findViewById(R.id.layoutSettings);
        layoutOperation = (RelativeLayout) findViewById(R.id.layoutOperation);
        layoutSettingsMain = (LinearLayout) findViewById(R.id.layoutSettingsMain);
        layoutLevel = (LinearLayout) findViewById(R.id.layoutLevel);
        layoutLevel_env = (LinearLayout) findViewById(R.id.layoutLevel_env);
        layoutOperMain = (LinearLayout) findViewById(R.id.layoutOperMain);
        layoutOperFlip = (LinearLayout) findViewById(R.id.layoutOperFlip);
        layoutOperTimeLapse = (LinearLayout) findViewById(R.id.layoutOperTimeLapse);
        layoutBack = (RelativeLayout) findViewById(R.id.layoutBack);
        layoutBack_oper = (RelativeLayout) findViewById(R.id.layoutBack_oper);
        layoutQVGA = (RelativeLayout) findViewById(R.id.layoutQVGA);
        layoutEnv = (RelativeLayout) findViewById(R.id.layoutEnv);
        layoutBright = (RelativeLayout) findViewById(R.id.layoutBright);
        layoutContrast = (RelativeLayout) findViewById(R.id.layoutContrast);
        layoutMax = (RelativeLayout) findViewById(R.id.layoutMax);
        layoutHigh = (RelativeLayout) findViewById(R.id.layoutHigh);
        layoutMedium = (RelativeLayout) findViewById(R.id.layoutMedium);
        layoutLow = (RelativeLayout) findViewById(R.id.layoutLow);
        layoutMin = (RelativeLayout) findViewById(R.id.layoutMin);
        layout50hz = (RelativeLayout) findViewById(R.id.layout50hz);
        layout60hz = (RelativeLayout) findViewById(R.id.layout60hz);
        layoutOutdoor = (RelativeLayout) findViewById(R.id.layoutOutdoor);
        layoutNight = (RelativeLayout) findViewById(R.id.layoutNight);
        layoutFlip_title = (RelativeLayout) findViewById(R.id.layoutFlip_title);
        layoutTimeLapse = (RelativeLayout) findViewById(R.id.layoutTimeLapse);
        layoutPreset = (RelativeLayout) findViewById(R.id.layoutPreset);
        layoutCruise = (RelativeLayout) findViewById(R.id.layoutPanTilt);
        layoutFlip = (RelativeLayout) findViewById(R.id.layoutFlip);
        layoutMirror = (RelativeLayout) findViewById(R.id.layoutMirror);
        layout_1sec = (RelativeLayout) findViewById(R.id.layout_1sec);
        layout_5sec = (RelativeLayout) findViewById(R.id.layout_5sec);
        layout_10sec = (RelativeLayout) findViewById(R.id.layout_10sec);
        layout_30sec = (RelativeLayout) findViewById(R.id.layout_30sec);
        layout_1min = (RelativeLayout) findViewById(R.id.layout_1min);
        layout_5min = (RelativeLayout) findViewById(R.id.layout_5min);
        layout_10min = (RelativeLayout) findViewById(R.id.layout_10min);
        layout_30min = (RelativeLayout) findViewById(R.id.layout_30min);
        layout_1hr = (RelativeLayout) findViewById(R.id.layout_1hr);
        layout_2hr = (RelativeLayout) findViewById(R.id.layout_2hr);
        layout_3hr = (RelativeLayout) findViewById(R.id.layout_3hr);
        layout_1day = (RelativeLayout) findViewById(R.id.layout_1day);

        mSoftMonitorLayout = (RelativeLayout) findViewById(R.id.softMonitorLayout);
        mHardMonitorLayout = (RelativeLayout) findViewById(R.id.layoutHardMonitor);

        nullLayout = (LinearLayout) findViewById(R.id.null_layout);

        linPnlCameraInfo = (LinearLayout) findViewById(R.id.pnlCameraInfo);
        layoutRecording = (LinearLayout) findViewById(R.id.layoutRecording);

        linSpeaker = (RelativeLayout) findViewById(R.id.speaker_layout);
        linSpeaker.setVisibility(View.GONE);

        txt_title = (TextView) this.findViewById(R.id.tvTitle);
        txt_title.setText(mDevice.NickName);
        tvScale = (TextView) findViewById(R.id.tvScale);

        // toolbar
        button_toolbar_speaker = (ImageButton) findViewById(R.id.button_speaker);
        button_toolbar_snapshot = (ImageButton) findViewById(R.id.button_snapshot);
        button_toolbar_operation = (ImageButton) findViewById(R.id.button_operation);
        button_toolbar_settings = (ImageButton) findViewById(R.id.button_settings);

        // Bubble
        btnBack_bubble = (ImageButton) findViewById(R.id.btnBack);
        btnBack_bubble_oper = (ImageButton) findViewById(R.id.btnBack_oper);
        btnOK_bubble = (Button) findViewById(R.id.btnOK);
        btnOK_bubble_oper = (Button) findViewById(R.id.btnOK_oper);
        swWDR = (mySwitch) findViewById(R.id.swWDR);
        tvTitle_bubble = (TextView) findViewById(R.id.tvTitle_bubble);
        tvTitle_bubble_oper = (TextView) findViewById(R.id.tvTittle_oper);
        tvQVGA = (TextView) findViewById(R.id.tvQVGA);
        tvEnv = (TextView) findViewById(R.id.tvEnv);
        tvBright = (TextView) findViewById(R.id.tvBright);
        tvContrast = (TextView) findViewById(R.id.tvContrast);
        imgTrin = (ImageView) findViewById(R.id.imgTrin);
        imgTrin_oper = (ImageView) findViewById(R.id.imgTrin_oper);
        imgMax = (ImageView) findViewById(R.id.imgMax);
        imgHigh = (ImageView) findViewById(R.id.imgHigh);
        imgMedium = (ImageView) findViewById(R.id.imgMedium);
        imgLow = (ImageView) findViewById(R.id.imgLow);
        imgMin = (ImageView) findViewById(R.id.imgMin);
        img50hz = (ImageView) findViewById(R.id.img50hz);
        img60hz = (ImageView) findViewById(R.id.img60hz);
        imgOutdoor = (ImageView) findViewById(R.id.imgOutdoor);
        imgNight = (ImageView) findViewById(R.id.imgNight);
        img_1sec = (ImageView) findViewById(R.id.img_1sec);
        img_5sec = (ImageView) findViewById(R.id.img_5sec);
        img_10sec = (ImageView) findViewById(R.id.img_10sec);
        img_30sec = (ImageView) findViewById(R.id.img_30sec);
        img_1min = (ImageView) findViewById(R.id.img_1min);
        img_5min = (ImageView) findViewById(R.id.img_5min);
        img_10min = (ImageView) findViewById(R.id.img_10min);
        img_30min = (ImageView) findViewById(R.id.img_30min);
        img_1hr = (ImageView) findViewById(R.id.img_1hr);
        img_2hr = (ImageView) findViewById(R.id.img_2hr);
        img_3hr = (ImageView) findViewById(R.id.img_3hr);
        img_1day = (ImageView) findViewById(R.id.img_1day);

        tvRecording = (TextView) findViewById(R.id.tvRecording);
        tvSwitch = (TextView) findViewById(R.id.tvSwitch);
        swRecording = (mySwitchButton) findViewById(R.id.swRecording);
        btn_speaker = (ImageButton) findViewById(R.id.btn_speaker);
        btn_speaker.setOnTouchListener(this);
        speaker_tips = (Button) findViewById(R.id.speaker_text);

        button_toolbar_speaker.setOnClickListener(ToolBarClick);
        button_toolbar_snapshot.setOnClickListener(ToolBarClick);
        button_toolbar_operation.setOnClickListener(ToolBarClick);
        button_toolbar_settings.setOnClickListener(ToolBarClick);

        btnOK_bubble.setOnClickListener(ToolBarClick);
        btnOK_bubble_oper.setOnClickListener(ToolBarClick);
        btnBack_bubble.setOnClickListener(ClickLevel);
        btnBack_bubble_oper.setOnClickListener(ClickOper);
        swWDR.setStateChangeListener(WDRStateListener);
        layoutQVGA.setOnClickListener(ClickSettings);
        layoutEnv.setOnClickListener(ClickSettings);
        layoutBright.setOnClickListener(ClickSettings);
        layoutContrast.setOnClickListener(ClickSettings);
        layoutMax.setOnClickListener(ClickLevel);
        layoutHigh.setOnClickListener(ClickLevel);
        layoutMedium.setOnClickListener(ClickLevel);
        layoutLow.setOnClickListener(ClickLevel);
        layoutMin.setOnClickListener(ClickLevel);
        layout50hz.setOnClickListener(ClickEnv);
        layout60hz.setOnClickListener(ClickEnv);
        layoutOutdoor.setOnClickListener(ClickEnv);
        layoutNight.setOnClickListener(ClickEnv);
        layoutFlip_title.setOnClickListener(ClickOper);
        layoutTimeLapse.setOnClickListener(ClickOper);
        layoutFlip.setOnClickListener(ClickFlip);
        layoutMirror.setOnClickListener(ClickFlip);
        layout_1sec.setOnClickListener(ClickTimeLapse);
        layout_5sec.setOnClickListener(ClickTimeLapse);
        layout_10sec.setOnClickListener(ClickTimeLapse);
        layout_30sec.setOnClickListener(ClickTimeLapse);
        layout_1min.setOnClickListener(ClickTimeLapse);
        layout_5min.setOnClickListener(ClickTimeLapse);
        layout_10min.setOnClickListener(ClickTimeLapse);
        layout_30min.setOnClickListener(ClickTimeLapse);
        layout_1hr.setOnClickListener(ClickTimeLapse);
        layout_2hr.setOnClickListener(ClickTimeLapse);
        layout_3hr.setOnClickListener(ClickTimeLapse);
        layout_1day.setOnClickListener(ClickTimeLapse);

        swRecording.setOnClickListener(ClickRecording);
        swRecording.setStateChangeListener(stateListener);

        tvSwitch.setText(getText(R.string.txt_time_lapse));
        progress = (ProgressBar) findViewById(R.id.progressBar);

        btnMagicZoom = (ImageButton) findViewById(R.id.btnMagicZoom);
        btnMagicZoom.setOnClickListener(ClickMagicZoom);

        layoutTitleBar.setVisibility(View.GONE);
        toolbar_layout.setVisibility(View.GONE);
        isShowToolBar = false;

        if (mIsRecording) {
            swRecording.isRecording();
            tvSwitch.setText(getText(R.string.txtNormal));
            layoutRecording.setVisibility(View.VISIBLE);
            if(!mIsListening) {
                button_toolbar_speaker.setEnabled(false);
            }
            button_toolbar_snapshot.setEnabled(false);
            button_toolbar_settings.setEnabled(false);
            button_toolbar_operation.setEnabled(false);
        }

        if (mIsLapsing) {
            swRecording.isLapsing();
        }

        if (mIsListening) {
            if(mCamera != null){
                mCamera.stopSpeaking(mSelectedChannel);
                mCamera.startListening(mSelectedChannel, mIsListening);
            }
            button_toolbar_speaker.setBackgroundResource(R.drawable.btn_tb_sound_h);
            linSpeaker.setVisibility(View.VISIBLE);
            nullLayout.setVisibility(View.GONE);
            toolbar_layout.setVisibility(View.VISIBLE);
            isShowToolBar = true;
        }

        if (Build.VERSION.SDK_INT < BUILD_VERSION_CODES_ICE_CREAM_SANDWICH) {

            bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
            getSupportActionBar().setBackgroundDrawable(bg);

            bgSplit.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
            getSupportActionBar().setSplitBackgroundDrawable(bgSplit);
        }

        txtConnectionStatus = null;
        txtConnectionMode = null;
        txtCodec = null;
        txtResolution = null;
        txtFrameRate = null;
        txtBitRate = null;
        txtOnlineNumber = null;
        txtFrameCount = null;
        txtIncompleteFrameCount = null;
        txtRecvFrmPreSec = null;
        txtDispFrmPreSeco = null;
        txtPerformance = null;

        if (! runSoftwareDecode) {
            mHardMonitor = (IMonitor) findViewById(R.id.hardMonitor);
            mHardMonitor.setMaxZoom(3.0f);
            mHardMonitor.enableDither(mCamera.mEnableDither);
            mHardMonitor.attachCamera(mCamera, mSelectedChannel);

            mSoftMonitorLayout.setVisibility(View.GONE);
            mHardMonitorLayout.setVisibility(View.VISIBLE);
            mHardMonitor.SetOnMonitorClickListener(this);

            mHardMonitor.cleanFrameQueue();
            mHardMonitor.setMediaCodecListener(this);

            // calculate surface view size
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;

            final SurfaceView surfaceView = (SurfaceView) mHardMonitor;
            surfaceView.getLayoutParams().width = width;
            surfaceView.getLayoutParams().height = height;
            mMiniVideoHeight = surfaceView.getLayoutParams().height;
            mMiniVideoWidth = surfaceView.getLayoutParams().width;
            surfaceView.setLayoutParams(surfaceView.getLayoutParams());
        } else {
            mSoftMonitor = (IMonitor) findViewById(R.id.softMonitor);
            mSoftMonitor.setMaxZoom(3.0f);
            mSoftMonitor.enableDither(mCamera.mEnableDither);
            mSoftMonitor.attachCamera(mCamera, mSelectedChannel);
            mSoftMonitor.SetOnMonitorClickListener(this);

            mSoftMonitorLayout.setVisibility(View.VISIBLE);
            mHardMonitorLayout.setVisibility(View.GONE);
        }

        reScaleMonitor();
        getOptionsStatus();

    }

    private void setupViewInPortraitLayout (final boolean runSoftwareDecode) {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.titlebar);
        actionBar.show();
        txt_title = (TextView) this.findViewById(R.id.bar_text);
        txt_title.setText(mDevice.NickName);

        setContentView(R.layout.live_view_portrait);

        nullLayout = (LinearLayout) findViewById(R.id.null_layout);
        toolbar_layout = (RelativeLayout) findViewById(R.id.layoutToolBar);
        linPnlCameraInfo = (LinearLayout) findViewById(R.id.pnlCameraInfo);
        layoutRecording = (LinearLayout) findViewById(R.id.layoutRecording);
        layoutSettings = (RelativeLayout) findViewById(R.id.layoutSettings);
        layoutOperation = (RelativeLayout) findViewById(R.id.layoutOperation);
        layoutSettingsMain = (LinearLayout) findViewById(R.id.layoutSettingsMain);
        layoutLevel = (LinearLayout) findViewById(R.id.layoutLevel);
        layoutLevel_env = (LinearLayout) findViewById(R.id.layoutLevel_env);
        layoutOperMain = (LinearLayout) findViewById(R.id.layoutOperMain);
        layoutOperFlip = (LinearLayout) findViewById(R.id.layoutOperFlip);
        layoutOperTimeLapse = (LinearLayout) findViewById(R.id.layoutOperTimeLapse);
        layoutBack = (RelativeLayout) findViewById(R.id.layoutBack);
        layoutBack_oper = (RelativeLayout) findViewById(R.id.layoutBack_oper);
        layoutQVGA = (RelativeLayout) findViewById(R.id.layoutQVGA);
        layoutEnv = (RelativeLayout) findViewById(R.id.layoutEnv);
        layoutBright = (RelativeLayout) findViewById(R.id.layoutBright);
        layoutContrast = (RelativeLayout) findViewById(R.id.layoutContrast);
        layoutMax = (RelativeLayout) findViewById(R.id.layoutMax);
        layoutHigh = (RelativeLayout) findViewById(R.id.layoutHigh);
        layoutMedium = (RelativeLayout) findViewById(R.id.layoutMedium);
        layoutLow = (RelativeLayout) findViewById(R.id.layoutLow);
        layoutMin = (RelativeLayout) findViewById(R.id.layoutMin);
        layout50hz = (RelativeLayout) findViewById(R.id.layout50hz);
        layout60hz = (RelativeLayout) findViewById(R.id.layout60hz);
        layoutOutdoor = (RelativeLayout) findViewById(R.id.layoutOutdoor);
        layoutNight = (RelativeLayout) findViewById(R.id.layoutNight);
        layoutFlip_title = (RelativeLayout) findViewById(R.id.layoutFlip_title);
        layoutTimeLapse = (RelativeLayout) findViewById(R.id.layoutTimeLapse);
        layoutPreset = (RelativeLayout) findViewById(R.id.layoutPreset);
        layoutCruise = (RelativeLayout) findViewById(R.id.layoutPanTilt);
        layoutFlip = (RelativeLayout) findViewById(R.id.layoutFlip);
        layoutMirror = (RelativeLayout) findViewById(R.id.layoutMirror);
        layout_1sec = (RelativeLayout) findViewById(R.id.layout_1sec);
        layout_5sec = (RelativeLayout) findViewById(R.id.layout_5sec);
        layout_10sec = (RelativeLayout) findViewById(R.id.layout_10sec);
        layout_30sec = (RelativeLayout) findViewById(R.id.layout_30sec);
        layout_1min = (RelativeLayout) findViewById(R.id.layout_1min);
        layout_5min = (RelativeLayout) findViewById(R.id.layout_5min);
        layout_10min = (RelativeLayout) findViewById(R.id.layout_10min);
        layout_30min = (RelativeLayout) findViewById(R.id.layout_30min);
        layout_1hr = (RelativeLayout) findViewById(R.id.layout_1hr);
        layout_2hr = (RelativeLayout) findViewById(R.id.layout_2hr);
        layout_3hr = (RelativeLayout) findViewById(R.id.layout_3hr);
        layout_1day = (RelativeLayout) findViewById(R.id.layout_1day);

        mSoftMonitorLayout = (RelativeLayout) findViewById(R.id.softMonitorLayout);
        mHardMonitorLayout = (RelativeLayout) findViewById(R.id.layoutHardMonitor);

        linSpeaker = (RelativeLayout) findViewById(R.id.speaker_layout);
        linSpeaker.setVisibility(View.GONE);

        // toolbar
        button_toolbar_speaker = (ImageButton) findViewById(R.id.button_speaker);
        button_toolbar_snapshot = (ImageButton) findViewById(R.id.button_snapshot);
        button_toolbar_operation = (ImageButton) findViewById(R.id.button_operation);
        button_toolbar_settings = (ImageButton) findViewById(R.id.button_settings);

        // Bubble
        btnBack_bubble = (ImageButton) findViewById(R.id.btnBack);
        btnBack_bubble_oper = (ImageButton) findViewById(R.id.btnBack_oper);
        btnOK_bubble = (Button) findViewById(R.id.btnOK);
        btnOK_bubble_oper = (Button) findViewById(R.id.btnOK_oper);
        swWDR = (mySwitch) findViewById(R.id.swWDR);
        tvTitle_bubble = (TextView) findViewById(R.id.tvTitle_bubble);
        tvTitle_bubble_oper = (TextView) findViewById(R.id.tvTittle_oper);
        tvQVGA = (TextView) findViewById(R.id.tvQVGA);
        tvEnv = (TextView) findViewById(R.id.tvEnv);
        tvBright = (TextView) findViewById(R.id.tvBright);
        tvContrast = (TextView) findViewById(R.id.tvContrast);
        imgTrin = (ImageView) findViewById(R.id.imgTrin);
        imgTrin_oper = (ImageView) findViewById(R.id.imgTrin_oper);
        imgMax = (ImageView) findViewById(R.id.imgMax);
        imgHigh = (ImageView) findViewById(R.id.imgHigh);
        imgMedium = (ImageView) findViewById(R.id.imgMedium);
        imgLow = (ImageView) findViewById(R.id.imgLow);
        imgMin = (ImageView) findViewById(R.id.imgMin);
        img50hz = (ImageView) findViewById(R.id.img50hz);
        img60hz = (ImageView) findViewById(R.id.img60hz);
        imgOutdoor = (ImageView) findViewById(R.id.imgOutdoor);
        imgNight = (ImageView) findViewById(R.id.imgNight);
        img_1sec = (ImageView) findViewById(R.id.img_1sec);
        img_5sec = (ImageView) findViewById(R.id.img_5sec);
        img_10sec = (ImageView) findViewById(R.id.img_10sec);
        img_30sec = (ImageView) findViewById(R.id.img_30sec);
        img_1min = (ImageView) findViewById(R.id.img_1min);
        img_5min = (ImageView) findViewById(R.id.img_5min);
        img_10min = (ImageView) findViewById(R.id.img_10min);
        img_30min = (ImageView) findViewById(R.id.img_30min);
        img_1hr = (ImageView) findViewById(R.id.img_1hr);
        img_2hr = (ImageView) findViewById(R.id.img_2hr);
        img_3hr = (ImageView) findViewById(R.id.img_3hr);
        img_1day = (ImageView) findViewById(R.id.img_1day);

        button_toolbar_speaker.setOnClickListener(ToolBarClick);
        button_toolbar_snapshot.setOnClickListener(ToolBarClick);
        button_toolbar_operation.setOnClickListener(ToolBarClick);
        button_toolbar_settings.setOnClickListener(ToolBarClick);

        btnOK_bubble.setOnClickListener(ToolBarClick);
        btnOK_bubble_oper.setOnClickListener(ToolBarClick);
        btnBack_bubble.setOnClickListener(ClickLevel);
        btnBack_bubble_oper.setOnClickListener(ClickOper);
        swWDR.setStateChangeListener(WDRStateListener);
        layoutQVGA.setOnClickListener(ClickSettings);
        layoutEnv.setOnClickListener(ClickSettings);
        layoutBright.setOnClickListener(ClickSettings);
        layoutContrast.setOnClickListener(ClickSettings);
        layoutMax.setOnClickListener(ClickLevel);
        layoutHigh.setOnClickListener(ClickLevel);
        layoutMedium.setOnClickListener(ClickLevel);
        layoutLow.setOnClickListener(ClickLevel);
        layoutMin.setOnClickListener(ClickLevel);
        layout50hz.setOnClickListener(ClickEnv);
        layout60hz.setOnClickListener(ClickEnv);
        layoutOutdoor.setOnClickListener(ClickEnv);
        layoutNight.setOnClickListener(ClickEnv);
        layoutFlip_title.setOnClickListener(ClickOper);
        layoutTimeLapse.setOnClickListener(ClickOper);
        layoutFlip.setOnClickListener(ClickFlip);
        layoutMirror.setOnClickListener(ClickFlip);
        layout_1sec.setOnClickListener(ClickTimeLapse);
        layout_5sec.setOnClickListener(ClickTimeLapse);
        layout_10sec.setOnClickListener(ClickTimeLapse);
        layout_30sec.setOnClickListener(ClickTimeLapse);
        layout_1min.setOnClickListener(ClickTimeLapse);
        layout_5min.setOnClickListener(ClickTimeLapse);
        layout_10min.setOnClickListener(ClickTimeLapse);
        layout_30min.setOnClickListener(ClickTimeLapse);
        layout_1hr.setOnClickListener(ClickTimeLapse);
        layout_2hr.setOnClickListener(ClickTimeLapse);
        layout_3hr.setOnClickListener(ClickTimeLapse);
        layout_1day.setOnClickListener(ClickTimeLapse);

        txtConnectionSlash = (TextView) findViewById(R.id.txtConnectionSlash);
        txtResolutionSlash = (TextView) findViewById(R.id.txtResolutionSlash);
        txtShowFPS = (TextView) findViewById(R.id.txtShowFPS);
        txtFPSSlash = (TextView) findViewById(R.id.txtFPSSlash);
        txtShowBPS = (TextView) findViewById(R.id.txtShowBPS);
        txtShowOnlineNumber = (TextView) findViewById(R.id.txtShowOnlineNumber);
        txtOnlineNumberSlash = (TextView) findViewById(R.id.txtOnlineNumberSlash);
        txtShowFrameRatio = (TextView) findViewById(R.id.txtShowFrameRatio);
        txtFrameCountSlash = (TextView) findViewById(R.id.txtFrameCountSlash);
        txtQuality = (TextView) findViewById(R.id.txtQuality);
        txtDispFrmPreSeco = (TextView) findViewById(R.id.txtDispFrmPreSeco);
        txtRecvFrmSlash = (TextView) findViewById(R.id.txtRecvFrmSlash);
        txtRecvFrmPreSec = (TextView) findViewById(R.id.txtRecvFrmPreSec);
        txtPerformance = (TextView) findViewById(R.id.txtPerformance);

        txtConnectionStatus = (TextView) findViewById(R.id.txtConnectionStatus);
        txtConnectionMode = (TextView) findViewById(R.id.txtConnectionMode);
        txtCodec = (TextView) findViewById(R.id.txtCodec);
        txtResolution = (TextView) findViewById(R.id.txtResolution);
        txtFrameRate = (TextView) findViewById(R.id.txtFrameRate);
        txtBitRate = (TextView) findViewById(R.id.txtBitRate);
        txtOnlineNumber = (TextView) findViewById(R.id.txtOnlineNumber);
        txtFrameCount = (TextView) findViewById(R.id.txtFrameCount);
        txtIncompleteFrameCount = (TextView) findViewById(R.id.txtIncompleteFrameCount);
        tvRecording = (TextView) findViewById(R.id.tvRecording);

        tvScale = (TextView) findViewById(R.id.tvScale);
        tvSwitch = (TextView) findViewById(R.id.tvSwitch);

        btn_speaker = (ImageButton) findViewById(R.id.btn_speaker);
        btnMagicZoom = (ImageButton) findViewById(R.id.btnMagicZoom);
        swRecording = (mySwitchButton) findViewById(R.id.swRecording);
        speaker_tips = (Button) findViewById(R.id.speaker_text);
        imgBattery = (ImageView) findViewById(R.id.imgBattery);
        progress = (ProgressBar) findViewById(R.id.progressBar);

        btn_speaker.setOnTouchListener(this);
        btnMagicZoom.setOnClickListener(ClickMagicZoom);
        swRecording.setOnClickListener(ClickRecording);
        swRecording.setStateChangeListener(stateListener);

        tvSwitch.setText(getText(R.string.txt_time_lapse));

        if (mIsRecording) {
            swRecording.isRecording();
            tvSwitch.setText(getText(R.string.txtNormal));
            layoutRecording.setVisibility(View.VISIBLE);
            if(!mIsListening) {
                button_toolbar_speaker.setEnabled(false);
            }
            button_toolbar_snapshot.setEnabled(false);
            button_toolbar_settings.setEnabled(false);
            button_toolbar_operation.setEnabled(false);
        }

        if (mIsLapsing) {
            swRecording.isLapsing();
        }

        if (mIsListening) {
            if(mCamera != null){
                mCamera.stopSpeaking(mSelectedChannel);
                mCamera.startListening(mSelectedChannel, mIsListening);
            }
            button_toolbar_speaker.setBackgroundResource(R.drawable.btn_tb_sound_h);
            linSpeaker.setVisibility(View.VISIBLE);
            nullLayout.setVisibility(View.GONE);
        }

        if (txtConnectionStatus != null) {
            if (getText(R.string.connstus_connecting).toString().equals(mConnStatus)) {
                txtConnectionStatus.setBackgroundResource(R.drawable.bg_unknow);
            } else if (getText(R.string.connstus_connected).toString().equals(mConnStatus)) {
                txtConnectionStatus.setBackgroundResource(R.drawable.bg_online);
            } else {
                txtConnectionStatus.setBackgroundResource(R.drawable.bg_offline);
            }
        }

        txtConnectionStatus.setText(mConnStatus);

        txtConnectionSlash.setText("");
        txtResolutionSlash.setText("");
        txtShowFPS.setText("");
        txtFPSSlash.setText("");
        txtShowBPS.setText("");
        txtOnlineNumberSlash.setText("");
        txtShowFrameRatio.setText("");
        txtFrameCountSlash.setText("");
        txtRecvFrmSlash.setText("");
        txtPerformance.setText(getPerformance((int) (((float) mCamera.getDispFrmPreSec() / (float) mCamera.getRecvFrmPreSec()) * 100)));

        txtConnectionMode.setVisibility(View.GONE);
        txtCodec.setVisibility(View.GONE);
        txtFrameRate.setVisibility(View.GONE);
        txtBitRate.setVisibility(View.GONE);
        txtFrameCount.setVisibility(View.GONE);
        txtIncompleteFrameCount.setVisibility(View.GONE);
        txtRecvFrmPreSec.setVisibility(View.GONE);
        txtDispFrmPreSeco.setVisibility(View.GONE);

        linPnlCameraInfo.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick (View arg0) {

                MultiViewActivity.nShowMessageCount++;
                showMessage();

            }
        });

        if (! runSoftwareDecode) {

            mHardMonitor = (IMonitor) findViewById(R.id.hardMonitor);
            mHardMonitor.setMaxZoom(3.0f);
            mHardMonitor.enableDither(mCamera.mEnableDither);
            mHardMonitor.attachCamera(mCamera, mSelectedChannel);

            mSoftMonitorLayout.setVisibility(View.GONE);
            mHardMonitorLayout.setVisibility(View.VISIBLE);

            mHardMonitor.cleanFrameQueue();
            mHardMonitor.setMediaCodecListener(this);

            // calculate surface view size
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;

            final SurfaceView surfaceView = (SurfaceView) mHardMonitor;
            surfaceView.getLayoutParams().width = width;
            handler.post(new Runnable() {
                @Override
                public void run () {
                    if (mHardMonitorLayout.getMeasuredHeight() == 0) {
                        handler.postDelayed(this, 200);
                    } else {
                        mMiniMonitorHeight = mHardMonitorLayout.getMeasuredHeight();
                        surfaceView.getLayoutParams().height = mHardMonitorLayout.getMeasuredHeight();
                        mMiniVideoHeight = surfaceView.getLayoutParams().height;
                        mMiniVideoWidth = surfaceView.getLayoutParams().width;
                        surfaceView.setLayoutParams(surfaceView.getLayoutParams());
                        reScaleMonitor();
                    }
                }
            });
        } else {
            mSoftMonitor = (IMonitor) findViewById(R.id.softMonitor);
            mSoftMonitor.setMaxZoom(3.0f);
            mSoftMonitor.enableDither(mCamera.mEnableDither);
            mSoftMonitor.attachCamera(mCamera, mSelectedChannel);

            mSoftMonitorLayout.setVisibility(View.VISIBLE);
            mHardMonitorLayout.setVisibility(View.GONE);

            // calculate surface view size
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;

            final SurfaceView surfaceView = (SurfaceView) mSoftMonitor;
            surfaceView.getLayoutParams().width = width;
            handler.post(new Runnable() {
                @Override
                public void run () {
                    if (mSoftMonitorLayout.getMeasuredHeight() == 0) {
                        handler.postDelayed(this, 200);
                    } else {
                        mMiniMonitorHeight = mSoftMonitorLayout.getMeasuredHeight();
                        surfaceView.getLayoutParams().height = mSoftMonitorLayout.getMeasuredHeight();
                        mMiniVideoHeight = surfaceView.getLayoutParams().height;
                        mMiniVideoWidth = surfaceView.getLayoutParams().width;
                        surfaceView.setLayoutParams(surfaceView.getLayoutParams());
                        reScaleMonitor();
                    }
                }
            });
        }

        getOptionsStatus();

    }

    private void showMessage () {

        St_SInfo stSInfo = new St_SInfo();
        IOTCAPIs.IOTC_Session_Check(mCamera.getMSID(), stSInfo);

        if (MultiViewActivity.nShowMessageCount >= 10) {

            txtConnectionStatus.setText(mConnStatus);
            txtConnectionMode.setText(getSessionMode(mCamera != null ? mCamera.getSessionMode() : - 1) + " C: " + IOTCAPIs.IOTC_Get_Nat_Type() + "," +
                    " D: " + stSInfo.NatType + ",R" + mCamera.getbResend());
            txtCodec.setText(unavailable ? " SW" : " HW");

            txtConnectionSlash.setText(" / ");
            txtResolutionSlash.setText(" / ");
            txtShowFPS.setText(getText(R.string.txtFPS));
            txtFPSSlash.setText(" / ");
            txtShowBPS.setText(getText(R.string.txtBPS));
            // txtShowOnlineNumber.setText(getText(R.string.txtOnlineNumber));
            txtOnlineNumberSlash.setText(" / ");
            txtShowFrameRatio.setText(getText(R.string.txtFrameRatio));
            txtFrameCountSlash.setText(" / ");
            txtQuality.setText(getText(R.string.txtQuality));
            txtRecvFrmSlash.setText(" / ");
            // mCamera.getDispFrmPreSec()
            txtConnectionMode.setVisibility(View.VISIBLE);
            txtCodec.setVisibility(View.VISIBLE);
            // txtResolution.setVisibility(View.VISIBLE);
            txtFrameRate.setVisibility(View.VISIBLE);
            txtBitRate.setVisibility(View.VISIBLE);
            txtOnlineNumber.setVisibility(View.VISIBLE);
            txtFrameCount.setVisibility(View.VISIBLE);
            txtIncompleteFrameCount.setVisibility(View.VISIBLE);
            txtRecvFrmPreSec.setVisibility(View.VISIBLE);
            txtDispFrmPreSeco.setVisibility(View.VISIBLE);
        }
    }

    private Button.OnClickListener ToolBarClick = new Button.OnClickListener() {

        @Override
        public void onClick (View v) {
            // TODO Auto-generated method stub
            switch (v.getId()) {

                case R.id.button_speaker:
                    inittoolar();
                    if (mIsListening) {
//                        swRecording.setEnable(true);
                        button_toolbar_speaker.setBackgroundResource(R.drawable.btn_speaker_off_switch);
                        linSpeaker.setVisibility(View.GONE);
                        nullLayout.setVisibility(View.VISIBLE);
                        mCamera.stopSpeaking(mSelectedChannel);
                        if(!mIsRecording) {
                            mCamera.stopListening(mSelectedChannel);
                        }
                        mIsListening = false;
                        mIsSpeaking = false;

                        if(mIsRecording){
                            button_toolbar_speaker.setEnabled(false);
                        }

                    } else {
//                        swRecording.setEnable(false);
                        inittoolarboolean();
                        mIsListening = true;
                        mCamera.startListening(mSelectedChannel, mIsListening);
                        button_toolbar_speaker.setBackgroundResource(R.drawable.btn_tb_sound_h);
                        speaker_tips.setVisibility(View.VISIBLE);
                        linSpeaker.setVisibility(View.VISIBLE);
                        nullLayout.setVisibility(View.GONE);
                    }
                    break;
                case R.id.button_snapshot:
//                    inittoolar();
//                    inittoolarboolean();
                    if (mCamera != null && mCamera.isChannelConnected(mSelectedChannel)) {

                        if (isSDCardValid()) {

                            File rootFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Snapshot/");
                            File uidFolder = new File(rootFolder.getAbsolutePath() + "/" + mDevUID);
                            File targetFolder = new File(uidFolder.getAbsolutePath() + "/" + "CH" + (mSelectedChannel + 1));

                            if (! rootFolder.exists()) {
                                try {
                                    rootFolder.mkdir();
                                } catch (SecurityException se) {
                                }
                            }

                            if (! uidFolder.exists()) {

                                try {
                                    uidFolder.mkdir();
                                } catch (SecurityException se) {
                                }
                            }

                            if (! targetFolder.exists()) {

                                try {
                                    targetFolder.mkdir();
                                } catch (SecurityException se) {
                                }
                            }

                            final String file = targetFolder.getAbsoluteFile() + "/" + getFileNameWithTime();
                            mFilePath = file;
                            if (mCamera != null) {
                                mCamera.setSnapshot(mContext, file);
                            }

                        } else {

                        }
                    }
                    break;

                case R.id.button_settings:
                case R.id.btnOK:
                    if (! mShowSettingsBubble) {
                        if (mShowOperationBubble) {
                            mShowOperationBubble = false;
                            layoutOperation.setVisibility(View.GONE);
                            layoutBack_oper.setVisibility(View.GONE);
                            layoutOperFlip.setVisibility(View.GONE);
//                            layoutOperTimeLapse.setVisibility(View.GONE);
                            layoutOperTimeLapse.setVisibility(View.INVISIBLE);
                            layoutOperMain.setVisibility(View.VISIBLE);
                        }

                        int[] locate = new int[2];
                        v.getLocationInWindow(locate);
                        Display display = getWindowManager().getDefaultDisplay();
                        Point size = new Point();
                        display.getSize(size);
                        final int screenWidth = size.x;
                        final int screenHeight = size.y;
                        int offset_x = (int) (screenWidth - locate[0] - v.getWidth() / 2);
                        int offset_y = (int) (screenHeight - locate[1] + getResources().getDimensionPixelSize(R.dimen.bubble_liveview_offset));

                        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) imgTrin.getLayoutParams();
                        lp.setMargins(0, 0, offset_x, 0);
                        imgTrin.setLayoutParams(lp);
                        lp = (RelativeLayout.LayoutParams) layoutSettings.getLayoutParams();
                        lp.setMargins(0, 0, 0, offset_y);
                        layoutSettings.setLayoutParams(lp);

                        mShowSettingsBubble = true;
                        initSettings();
                        tvTitle_bubble.setText(getText(R.string.txt_camera_settings));
                        layoutSettings.setVisibility(View.VISIBLE);
                    } else {
                        mShowSettingsBubble = false;
                        layoutSettings.setVisibility(View.GONE);
                        initSettings();
                    }
                    break;

                case R.id.button_operation:
                case R.id.btnOK_oper:
                    if (! mShowOperationBubble) {
                        if (mShowSettingsBubble) {
                            mShowSettingsBubble = false;
                            layoutSettings.setVisibility(View.GONE);
                        }

                        int[] locate = new int[2];
                        v.getLocationInWindow(locate);
                        Display display = getWindowManager().getDefaultDisplay();
                        Point size = new Point();
                        display.getSize(size);
                        final int screenWidth = size.x;
                        final int screenHeight = size.y;
                        int offset_x = (int) (screenWidth - locate[0] - v.getWidth() / 2);
                        int offset_y = (int) (screenHeight - locate[1] + getResources().getDimensionPixelSize(R.dimen.bubble_liveview_offset));

                        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) imgTrin_oper.getLayoutParams();
                        lp.setMargins(0, 0, offset_x, 0);
                        imgTrin_oper.setLayoutParams(lp);
                        lp = (RelativeLayout.LayoutParams) layoutOperation.getLayoutParams();
                        lp.setMargins(0, 0, 0, offset_y);
                        layoutOperation.setLayoutParams(lp);

                        mShowOperationBubble = true;
                        tvTitle_bubble_oper.setText(getText(R.string.txt_oper_settings));
                        layoutOperation.setVisibility(View.VISIBLE);
                    } else {
                        mShowOperationBubble = false;
                        layoutOperation.setVisibility(View.GONE);
                        layoutBack_oper.setVisibility(View.GONE);
                        layoutOperFlip.setVisibility(View.GONE);
//                        layoutOperTimeLapse.setVisibility(View.GONE);
                        layoutOperTimeLapse.setVisibility(View.INVISIBLE);
                        layoutOperMain.setVisibility(View.VISIBLE);

                        SharedPreferences TimeLapseSettings = getSharedPreferences("time_lapse_settings", 0);
                        TimeLapseSettings.edit().putInt(mDevUID, mTimeLapse).commit();
                    }
                    break;
            }

        }
    };

    private View.OnClickListener ClickSettings = new View.OnClickListener() {
        @Override
        public void onClick (View v) {
            clearLevels();
            switch (v.getId()) {
                case R.id.layoutQVGA:
                    switch (mQVGA) {
                        case AVIOCTRLDEFs.AVIOCTRL_QUALITY_MIN:
                            imgMin.setBackgroundResource(R.drawable.btn_tickbox_h);
                            break;
                        case AVIOCTRLDEFs.AVIOCTRL_QUALITY_LOW:
                            imgLow.setBackgroundResource(R.drawable.btn_tickbox_h);
                            break;
                        case AVIOCTRLDEFs.AVIOCTRL_QUALITY_MIDDLE:
                            imgMedium.setBackgroundResource(R.drawable.btn_tickbox_h);
                            break;
                        case AVIOCTRLDEFs.AVIOCTRL_QUALITY_HIGH:
                            imgHigh.setBackgroundResource(R.drawable.btn_tickbox_h);
                            break;
                        case AVIOCTRLDEFs.AVIOCTRL_QUALITY_MAX:
                            imgMax.setBackgroundResource(R.drawable.btn_tickbox_h);
                            break;
                    }

                    mLevelPanel = BUBBLE_LEVEL_QVGA;
                    tvTitle_bubble.setText(getText(R.string.txt_qvga));
                    layoutSettingsMain.setVisibility(View.GONE);
                    layoutLevel.setVisibility(View.VISIBLE);
                    layoutBack.setVisibility(View.VISIBLE);
                    break;

                case R.id.layoutEnv:
                    switch (mEnv) {
                        case AVIOCTRLDEFs.AVIOCTRL_ENVIRONMENT_INDOOR_50HZ:
                            img50hz.setBackgroundResource(R.drawable.btn_tickbox_h);
                            break;
                        case AVIOCTRLDEFs.AVIOCTRL_ENVIRONMENT_INDOOR_60HZ:
                            img60hz.setBackgroundResource(R.drawable.btn_tickbox_h);
                            break;
                        case AVIOCTRLDEFs.AVIOCTRL_ENVIRONMENT_OUTDOOR:
                            imgOutdoor.setBackgroundResource(R.drawable.btn_tickbox_h);
                            break;
                        case AVIOCTRLDEFs.AVIOCTRL_ENVIRONMENT_NIGHT:
                            imgNight.setBackgroundResource(R.drawable.btn_tickbox_h);
                            break;
                    }

                    tvTitle_bubble.setText(getText(R.string.txt_environment));
                    layoutSettingsMain.setVisibility(View.GONE);
                    layoutLevel_env.setVisibility(View.VISIBLE);
                    layoutBack.setVisibility(View.VISIBLE);
                    break;

                case R.id.layoutBright:
                    switch (mBrightness) {
                        case AVIOCTRLDEFs.AVIOCTRL_BRIGHT_MIN:
                            imgMin.setBackgroundResource(R.drawable.btn_tickbox_h);
                            break;
                        case AVIOCTRLDEFs.AVIOCTRL_BRIGHT_LOW:
                            imgLow.setBackgroundResource(R.drawable.btn_tickbox_h);
                            break;
                        case AVIOCTRLDEFs.AVIOCTRL_BRIGHT_MIDDLE:
                            imgMedium.setBackgroundResource(R.drawable.btn_tickbox_h);
                            break;
                        case AVIOCTRLDEFs.AVIOCTRL_BRIGHT_HIGH:
                            imgHigh.setBackgroundResource(R.drawable.btn_tickbox_h);
                            break;
                        case AVIOCTRLDEFs.AVIOCTRL_BRIGHT_MAX:
                            imgMax.setBackgroundResource(R.drawable.btn_tickbox_h);
                            break;
                    }

                    mLevelPanel = BUBBLE_LEVEL_BRIGHT;
                    tvTitle_bubble.setText(getText(R.string.txt_brightness));
                    layoutSettingsMain.setVisibility(View.GONE);
                    layoutLevel.setVisibility(View.VISIBLE);
                    layoutBack.setVisibility(View.VISIBLE);
                    break;

                case R.id.layoutContrast:
                    switch (mContrast) {
                        case AVIOCTRLDEFs.AVIOCTRL_CONTRASTT_MIN:
                            imgMin.setBackgroundResource(R.drawable.btn_tickbox_h);
                            break;
                        case AVIOCTRLDEFs.AVIOCTRL_CONTRAST_LOW:
                            imgLow.setBackgroundResource(R.drawable.btn_tickbox_h);
                            break;
                        case AVIOCTRLDEFs.AVIOCTRL_CONTRAST_MIDDLE:
                            imgMedium.setBackgroundResource(R.drawable.btn_tickbox_h);
                            break;
                        case AVIOCTRLDEFs.AVIOCTRL_CONTRAST_HIGH:
                            imgHigh.setBackgroundResource(R.drawable.btn_tickbox_h);
                            break;
                        case AVIOCTRLDEFs.AVIOCTRL_CONTRAST_MAX:
                            imgMax.setBackgroundResource(R.drawable.btn_tickbox_h);
                            break;
                    }

                    mLevelPanel = BUBBLE_LEVEL_CONTRAST;
                    tvTitle_bubble.setText(getText(R.string.txt_contrast));
                    layoutSettingsMain.setVisibility(View.GONE);
                    layoutLevel.setVisibility(View.VISIBLE);
                    layoutBack.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    private View.OnClickListener ClickLevel = new View.OnClickListener() {

        @Override
        public void onClick (View v) {
            switch (v.getId()) {
                case R.id.layoutMax:
                    switch (mLevelPanel) {
                        case BUBBLE_LEVEL_QVGA:
                            mQVGA = AVIOCTRLDEFs.AVIOCTRL_QUALITY_MAX;
                            changeQVGA(AVIOCTRLDEFs.AVIOCTRL_QUALITY_MAX);
                            tvQVGA.setText(getText(R.string.txt_highest));
                            break;
                        case BUBBLE_LEVEL_BRIGHT:
                            mBrightness = AVIOCTRLDEFs.AVIOCTRL_BRIGHT_MAX;
                            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_BRIGHT_SETBRIGHT_REQ,
                                    AVIOCTRLDEFs.SMsgAVIoctrlSetBrightReq.parseContent(mSelectedChannel, AVIOCTRLDEFs.AVIOCTRL_BRIGHT_MAX));
                            tvBright.setText(getText(R.string.txt_highest));
                            break;
                        case BUBBLE_LEVEL_CONTRAST:
                            mContrast = AVIOCTRLDEFs.AVIOCTRL_CONTRAST_MAX;
                            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_CONTRAST_SETCONTRAST_REQ,
                                    AVIOCTRLDEFs.SMsgAVIoctrSetContrastReq.parseContent(mSelectedChannel, AVIOCTRLDEFs.AVIOCTRL_CONTRAST_MAX));
                            tvContrast.setText(getText(R.string.txt_highest));
                            break;
                    }
                    clearLevels();
                    imgMax.setBackgroundResource(R.drawable.btn_tickbox_h);
                    break;
                case R.id.layoutHigh:
                    switch (mLevelPanel) {
                        case BUBBLE_LEVEL_QVGA:
                            mQVGA = AVIOCTRLDEFs.AVIOCTRL_QUALITY_HIGH;
                            changeQVGA(AVIOCTRLDEFs.AVIOCTRL_QUALITY_HIGH);
                            tvQVGA.setText(getText(R.string.txt_high));
                            break;
                        case BUBBLE_LEVEL_BRIGHT:
                            mBrightness = AVIOCTRLDEFs.AVIOCTRL_BRIGHT_HIGH;
                            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_BRIGHT_SETBRIGHT_REQ,
                                    AVIOCTRLDEFs.SMsgAVIoctrlSetBrightReq.parseContent(mSelectedChannel, AVIOCTRLDEFs.AVIOCTRL_BRIGHT_HIGH));
                            tvBright.setText(getText(R.string.txt_high));
                            break;
                        case BUBBLE_LEVEL_CONTRAST:
                            mContrast = AVIOCTRLDEFs.AVIOCTRL_CONTRAST_HIGH;
                            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_CONTRAST_SETCONTRAST_REQ,
                                    AVIOCTRLDEFs.SMsgAVIoctrSetContrastReq.parseContent(mSelectedChannel, AVIOCTRLDEFs.AVIOCTRL_CONTRAST_HIGH));
                            tvContrast.setText(getText(R.string.txt_high));
                            break;
                    }
                    clearLevels();
                    imgHigh.setBackgroundResource(R.drawable.btn_tickbox_h);
                    break;
                case R.id.layoutMedium:
                    switch (mLevelPanel) {
                        case BUBBLE_LEVEL_QVGA:
                            mQVGA = AVIOCTRLDEFs.AVIOCTRL_QUALITY_MIDDLE;
                            changeQVGA(AVIOCTRLDEFs.AVIOCTRL_QUALITY_MIDDLE);
                            tvQVGA.setText(getText(R.string.txt_medium));
                            break;
                        case BUBBLE_LEVEL_BRIGHT:
                            mBrightness = AVIOCTRLDEFs.AVIOCTRL_BRIGHT_MIDDLE;
                            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_BRIGHT_SETBRIGHT_REQ,
                                    AVIOCTRLDEFs.SMsgAVIoctrlSetBrightReq.parseContent(mSelectedChannel, AVIOCTRLDEFs.AVIOCTRL_BRIGHT_MIDDLE));
                            tvBright.setText(getText(R.string.txt_medium));
                            break;
                        case BUBBLE_LEVEL_CONTRAST:
                            mContrast = AVIOCTRLDEFs.AVIOCTRL_CONTRAST_MIDDLE;
                            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_CONTRAST_SETCONTRAST_REQ,
                                    AVIOCTRLDEFs.SMsgAVIoctrSetContrastReq.parseContent(mSelectedChannel, AVIOCTRLDEFs.AVIOCTRL_CONTRAST_MIDDLE));
                            tvContrast.setText(getText(R.string.txt_medium));
                            break;
                    }
                    clearLevels();
                    imgMedium.setBackgroundResource(R.drawable.btn_tickbox_h);
                    break;
                case R.id.layoutLow:
                    switch (mLevelPanel) {
                        case BUBBLE_LEVEL_QVGA:
                            mQVGA = AVIOCTRLDEFs.AVIOCTRL_QUALITY_LOW;
                            changeQVGA(AVIOCTRLDEFs.AVIOCTRL_QUALITY_LOW);
                            tvQVGA.setText(getText(R.string.txt_low));
                            break;
                        case BUBBLE_LEVEL_BRIGHT:
                            mBrightness = AVIOCTRLDEFs.AVIOCTRL_BRIGHT_LOW;
                            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_BRIGHT_SETBRIGHT_REQ,
                                    AVIOCTRLDEFs.SMsgAVIoctrlSetBrightReq.parseContent(mSelectedChannel, AVIOCTRLDEFs.AVIOCTRL_BRIGHT_LOW));
                            tvBright.setText(getText(R.string.txt_low));
                            break;
                        case BUBBLE_LEVEL_CONTRAST:
                            mContrast = AVIOCTRLDEFs.AVIOCTRL_CONTRAST_LOW;
                            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_CONTRAST_SETCONTRAST_REQ,
                                    AVIOCTRLDEFs.SMsgAVIoctrSetContrastReq.parseContent(mSelectedChannel, AVIOCTRLDEFs.AVIOCTRL_CONTRAST_LOW));
                            tvContrast.setText(getText(R.string.txt_low));
                            break;
                    }
                    clearLevels();
                    imgLow.setBackgroundResource(R.drawable.btn_tickbox_h);
                    break;
                case R.id.layoutMin:
                    switch (mLevelPanel) {
                        case BUBBLE_LEVEL_QVGA:
                            mQVGA = AVIOCTRLDEFs.AVIOCTRL_QUALITY_MIN;
                            changeQVGA(AVIOCTRLDEFs.AVIOCTRL_QUALITY_MIN);
                            tvQVGA.setText(getText(R.string.txt_lowest));
                            break;
                        case BUBBLE_LEVEL_BRIGHT:
                            mBrightness = AVIOCTRLDEFs.AVIOCTRL_BRIGHT_MIN;
                            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_BRIGHT_SETBRIGHT_REQ,
                                    AVIOCTRLDEFs.SMsgAVIoctrlSetBrightReq.parseContent(mSelectedChannel, AVIOCTRLDEFs.AVIOCTRL_BRIGHT_MIN));
                            tvBright.setText(getText(R.string.txt_lowest));
                            break;
                        case BUBBLE_LEVEL_CONTRAST:
                            mContrast = AVIOCTRLDEFs.AVIOCTRL_CONTRASTT_MIN;
                            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_CONTRAST_SETCONTRAST_REQ,
                                    AVIOCTRLDEFs.SMsgAVIoctrSetContrastReq.parseContent(mSelectedChannel, AVIOCTRLDEFs.AVIOCTRL_CONTRASTT_MIN));
                            tvContrast.setText(getText(R.string.txt_lowest));
                            break;
                    }
                    clearLevels();
                    imgMin.setBackgroundResource(R.drawable.btn_tickbox_h);
                    break;
                case R.id.btnBack:
                    tvTitle_bubble.setText(getText(R.string.txt_camera_settings));
                    layoutBack.setVisibility(View.GONE);
                    layoutLevel.setVisibility(View.GONE);
                    layoutLevel_env.setVisibility(View.GONE);
                    layoutSettingsMain.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    private View.OnClickListener ClickEnv = new View.OnClickListener() {

        @Override
        public void onClick (View v) {
            switch (v.getId()) {
                case R.id.layout50hz:
                    mEnv = AVIOCTRLDEFs.AVIOCTRL_ENVIRONMENT_INDOOR_50HZ;
                    changeEnv(mEnv);
                    clearLevels();
                    img50hz.setBackgroundResource(R.drawable.btn_tickbox_h);
                    tvEnv.setText(getText(R.string.txtmEnvironment50));
                    break;
                case R.id.layout60hz:
                    mEnv = AVIOCTRLDEFs.AVIOCTRL_ENVIRONMENT_INDOOR_60HZ;
                    changeEnv(mEnv);
                    clearLevels();
                    img60hz.setBackgroundResource(R.drawable.btn_tickbox_h);
                    tvEnv.setText(getText(R.string.txtmEnvironment60));
                    break;
                case R.id.layoutOutdoor:
                    mEnv = AVIOCTRLDEFs.AVIOCTRL_ENVIRONMENT_OUTDOOR;
                    changeEnv(mEnv);
                    clearLevels();
                    imgOutdoor.setBackgroundResource(R.drawable.btn_tickbox_h);
                    tvEnv.setText(getText(R.string.txtmEnvironmentout));
                    break;
                case R.id.layoutNight:
                    mEnv = AVIOCTRLDEFs.AVIOCTRL_ENVIRONMENT_NIGHT;
                    changeEnv(mEnv);
                    clearLevels();
                    imgNight.setBackgroundResource(R.drawable.btn_tickbox_h);
                    tvEnv.setText(getText(R.string.txtmEnvironmentnight));
                    break;
            }
        }
    };

    private View.OnClickListener ClickOper = new View.OnClickListener() {

        @Override
        public void onClick (View v) {
            switch (v.getId()) {
                case R.id.layoutFlip_title:
                    tvTitle_bubble_oper.setText(getText(R.string.txt_flip_title));
                    layoutOperMain.setVisibility(View.GONE);
                    layoutBack_oper.setVisibility(View.VISIBLE);
                    layoutOperFlip.setVisibility(View.VISIBLE);
                    break;

                case R.id.layoutTimeLapse:
                    clearTimeLapse();
                    setTimeLapseCheck(mTimeLapse);

                    tvTitle_bubble_oper.setText(getText(R.string.txt_time_lapse_recording));
                    layoutOperMain.setVisibility(View.GONE);
                    layoutBack_oper.setVisibility(View.VISIBLE);
//                    layoutOperTimeLapse.setVisibility(View.VISIBLE);
                    layoutOperTimeLapse.setVisibility(View.INVISIBLE);
                    break;

                case R.id.btnBack_oper:
                    tvTitle_bubble_oper.setText(getText(R.string.txt_oper_settings));
                    layoutOperFlip.setVisibility(View.GONE);
//                    layoutOperTimeLapse.setVisibility(View.GONE);
                    layoutOperTimeLapse.setVisibility(View.INVISIBLE);
                    layoutBack_oper.setVisibility(View.GONE);
                    layoutOperMain.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    private View.OnClickListener ClickFlip = new View.OnClickListener() {

        @Override
        public void onClick (View v) {
            switch (v.getId()) {
                case R.id.layoutFlip:
//                    if (LevelFlip) {
//                        LevelFlip = false;
//                        if (VerticalFlip) {
//                            mRotateMode = ROTATEMODE_MIRROR;
//                        } else {
//                            mRotateMode = ROTATEMODE_NORMAL;
//                        }
//                    } else {
//                        LevelFlip = true;
//                        if (VerticalFlip) {
//                            mRotateMode = ROTATEMODE_FLIPANDMIRROR;
//                        } else {
//                            mRotateMode = ROTATEMODE_FLIP;
//                        }
//                    }
                    switch (mFlip){
                        case ROTATEMODE_NORMAL:
                            mFlip = ROTATEMODE_FLIP;
                            break;
                        case ROTATEMODE_FLIP:
                            mFlip = ROTATEMODE_NORMAL;
                            break;
                        case ROTATEMODE_MIRROR:
                            mFlip = ROTATEMODE_FLIPANDMIRROR;
                            break;
                        case ROTATEMODE_FLIPANDMIRROR:
                            mFlip = ROTATEMODE_MIRROR;
                            break;
                    }

                    mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_VIDEOMODE_REQ,
                            AVIOCTRLDEFs.SMsgAVIoctrlSetVideoModeReq.parseContent(mDevice.ChannelIndex, (byte) mFlip));
                    break;
                case R.id.layoutMirror:
//                    if (VerticalFlip) {
//                        VerticalFlip = false;
//                        if (LevelFlip) {
//                            mRotateMode = ROTATEMODE_FLIP;
//                        } else {
//                            mRotateMode = ROTATEMODE_NORMAL;
//                        }
//                    } else {
//                        VerticalFlip = true;
//                        if (LevelFlip) {
//                            mRotateMode = ROTATEMODE_FLIPANDMIRROR;
//                        } else {
//                            mRotateMode = ROTATEMODE_MIRROR;
//                        }
//                    }
                    switch (mFlip){
                        case ROTATEMODE_NORMAL:
                            mFlip = ROTATEMODE_MIRROR;
                            break;
                        case ROTATEMODE_FLIP:
                            mFlip = ROTATEMODE_FLIPANDMIRROR;
                            break;
                        case ROTATEMODE_MIRROR:
                            mFlip = ROTATEMODE_NORMAL;
                            break;
                        case ROTATEMODE_FLIPANDMIRROR:
                            mFlip = ROTATEMODE_FLIP;
                            break;
                    }

                    mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_VIDEOMODE_REQ,
                            AVIOCTRLDEFs.SMsgAVIoctrlSetVideoModeReq.parseContent(mDevice.ChannelIndex, (byte) mFlip));
                    break;
            }
        }
    };

    private View.OnClickListener ClickTimeLapse = new View.OnClickListener() {

        @Override
        public void onClick (View v) {
            switch (v.getId()) {
                case R.id.layout_1sec:
                    mTimeLapse = TIME_LAPSE_1SEC;
                    break;
                case R.id.layout_5sec:
                    mTimeLapse = TIME_LAPSE_5SEC;
                    break;
                case R.id.layout_10sec:
                    mTimeLapse = TIME_LAPSE_10SEC;
                    break;
                case R.id.layout_30sec:
                    mTimeLapse = TIME_LAPSE_30SEC;
                    break;
                case R.id.layout_1min:
                    mTimeLapse = TIME_LAPSE_1MIN;
                    break;
                case R.id.layout_5min:
                    mTimeLapse = TIME_LAPSE_5MIN;
                    break;
                case R.id.layout_10min:
                    mTimeLapse = TIME_LAPSE_10MIN;
                    break;
                case R.id.layout_30min:
                    mTimeLapse = TIME_LAPSE_30MIN;
                    break;
                case R.id.layout_1hr:
                    mTimeLapse = TIME_LAPSE_1HR;
                    break;
                case R.id.layout_2hr:
                    mTimeLapse = TIME_LAPSE_2HR;
                    break;
                case R.id.layout_3hr:
                    mTimeLapse = TIME_LAPSE_3HR;
                    break;
                case R.id.layout_1day:
                    mTimeLapse = TIME_LAPSE_1DAY;
                    break;
            }

            clearTimeLapse();
            setTimeLapseCheck(mTimeLapse);
        }
    };

    private void clearLevels () {
        imgMax.setBackgroundResource(R.drawable.btn_tickbox_n);
        imgHigh.setBackgroundResource(R.drawable.btn_tickbox_n);
        imgMedium.setBackgroundResource(R.drawable.btn_tickbox_n);
        imgLow.setBackgroundResource(R.drawable.btn_tickbox_n);
        imgMin.setBackgroundResource(R.drawable.btn_tickbox_n);
        img50hz.setBackgroundResource(R.drawable.btn_tickbox_n);
        img60hz.setBackgroundResource(R.drawable.btn_tickbox_n);
        imgOutdoor.setBackgroundResource(R.drawable.btn_tickbox_n);
        imgNight.setBackgroundResource(R.drawable.btn_tickbox_n);
    }

    private void clearTimeLapse () {
        img_1sec.setBackgroundResource(R.drawable.btn_tickbox_n);
        img_5sec.setBackgroundResource(R.drawable.btn_tickbox_n);
        img_10sec.setBackgroundResource(R.drawable.btn_tickbox_n);
        img_30sec.setBackgroundResource(R.drawable.btn_tickbox_n);
        img_1min.setBackgroundResource(R.drawable.btn_tickbox_n);
        img_5min.setBackgroundResource(R.drawable.btn_tickbox_n);
        img_10min.setBackgroundResource(R.drawable.btn_tickbox_n);
        img_30min.setBackgroundResource(R.drawable.btn_tickbox_n);
        img_1hr.setBackgroundResource(R.drawable.btn_tickbox_n);
        img_2hr.setBackgroundResource(R.drawable.btn_tickbox_n);
        img_3hr.setBackgroundResource(R.drawable.btn_tickbox_n);
        img_1day.setBackgroundResource(R.drawable.btn_tickbox_n);
    }

    private void setTimeLapseCheck (int position) {
        switch (position) {
            case TIME_LAPSE_1SEC:
                img_1sec.setBackgroundResource(R.drawable.btn_tickbox_h);
                break;
            case TIME_LAPSE_5SEC:
                img_5sec.setBackgroundResource(R.drawable.btn_tickbox_h);
                break;
            case TIME_LAPSE_10SEC:
                img_10sec.setBackgroundResource(R.drawable.btn_tickbox_h);
                break;
            case TIME_LAPSE_30SEC:
                img_30sec.setBackgroundResource(R.drawable.btn_tickbox_h);
                break;
            case TIME_LAPSE_1MIN:
                img_1min.setBackgroundResource(R.drawable.btn_tickbox_h);
                break;
            case TIME_LAPSE_5MIN:
                img_5min.setBackgroundResource(R.drawable.btn_tickbox_h);
                break;
            case TIME_LAPSE_10MIN:
                img_10min.setBackgroundResource(R.drawable.btn_tickbox_h);
                break;
            case TIME_LAPSE_30MIN:
                img_30min.setBackgroundResource(R.drawable.btn_tickbox_h);
                break;
            case TIME_LAPSE_1HR:
                img_1hr.setBackgroundResource(R.drawable.btn_tickbox_h);
                break;
            case TIME_LAPSE_2HR:
                img_2hr.setBackgroundResource(R.drawable.btn_tickbox_h);
                break;
            case TIME_LAPSE_3HR:
                img_3hr.setBackgroundResource(R.drawable.btn_tickbox_h);
                break;
            case TIME_LAPSE_1DAY:
                img_1day.setBackgroundResource(R.drawable.btn_tickbox_h);
                break;
        }
    }

    private Button.OnClickListener ClickMagicZoom = new Button.OnClickListener() {
        @Override
        public void onClick (View v) {
            int[] location_view = new int[2];
            int[] location_monitor = new int[2];

            SurfaceView surfaceView = (SurfaceView) mHardMonitor;
            surfaceView.getLocationInWindow(location_view);

            int x_offset = location_monitor[0] - location_view[0];
            int y_offset = location_monitor[1] - location_view[1];

//            int x = x_offset + myScrollView.getHeight() / 2;
//            int y = y_offset + myScrollView.getWidth() / 2;
//            Log.i("RRR", x + " " + y);

//            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_SETAMBCROPSET_REQ, AVIOCTRLDEFs.SMsgAVIoctrlSetAmbCropSetReq
//                    .parseContent(x, y, surfaceView.getWidth(), surfaceView.getHeight(), mScale, (byte) mSelectedChannel));
        }
    };

    private mySwitch.StateListener WDRStateListener = new mySwitch.StateListener() {
        @Override
        public void stateChange (int mode) {
            switch (mode) {
                case mySwitch.MODE_ON:
                    if (mCamera != null) {
                        String req = "custom=1&cmd=2004&par=1";
                        mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, CustomCommand.IOTYPE_USER_WIFICMD_REQ,
                                CustomCommand.SMsgAVIoctrlWifiCmdReq.parseContent(mSelectedChannel, 0, 0, 2004, 1, 0, req.length(), req));
                    }
                    break;
                case mySwitch.MODE_OFF:
                    if (mCamera != null) {
                        String req = "custom=1&cmd=2004&par=0";
                        mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, CustomCommand.IOTYPE_USER_WIFICMD_REQ,
                                CustomCommand.SMsgAVIoctrlWifiCmdReq.parseContent(mSelectedChannel, 0, 0, 2004, 1, 0, req.length(), req));
                    }
                    break;
            }
        }
    };

    private mySwitchButton.SwitchListener ClickRecording = new mySwitchButton.SwitchListener() {
        @Override
        public void click () {
            if (swRecording.getMode() == mySwitchButton.MODE_TIME_LAPSE) {
                if (mCamera != null) {
                    String req;
                    if (! mIsLapsing) {
                        mIsLapsing = true;
                        req = "custom=1&cmd=2019&par=" + mTimeLapse;
                    } else {
                        mIsLapsing = false;
                        req = "custom=1&cmd=2019&par=0";
                    }
                    mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, CustomCommand.IOTYPE_USER_WIFICMD_REQ,
                            CustomCommand.SMsgAVIoctrlWifiCmdReq.parseContent(mSelectedChannel, 0, 0, 2019, 1, 0, req.length(), req)); Log.i("RRR", "TL "+req);
                }
            } else {
                if (mIsRecording) {
                    button_toolbar_speaker.setEnabled(true);
                    button_toolbar_snapshot.setEnabled(true);
                    button_toolbar_operation.setEnabled(true);
                    button_toolbar_settings.setEnabled(true);
                    layoutRecording.setVisibility(View.GONE);

                    if(!mIsListening) {
                        mCamera.stopListening(mSelectedChannel);
                    }
                    mCamera.stopSpeaking(mSelectedChannel);
                    mCamera.stopRecording();
                    mThreadShowRecodTime.stopThread();
                    mIsRecording = false;
                } else {
                    if (getAvailaleSize() <= 300) {
                        Toast.makeText(mContext, R.string.recording_tips_size, Toast.LENGTH_SHORT).show();
                    }

                    if (mCamera.codec_ID_for_recording == AVFrame.MEDIA_CODEC_VIDEO_H264) {
                        if(!mIsListening) {
                            button_toolbar_speaker.setEnabled(false);
                        }
                        button_toolbar_snapshot.setEnabled(false);
                        button_toolbar_operation.setEnabled(false);
                        button_toolbar_settings.setEnabled(false);
                        layoutRecording.setVisibility(View.VISIBLE);

                        mIsRecording = true;
                        mCamera.startListening(mSelectedChannel, mIsListening);
                        mCamera.stopSpeaking(mSelectedChannel);
                        File rootFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Record/");
                        File uidFolder = new File(rootFolder.getAbsolutePath() + "/" + mDevUID);
                        File targetFolder = new File(uidFolder.getAbsolutePath() + "/" + "CH" + (mSelectedChannel + 1));
                        if (! rootFolder.exists()) {
                            try {
                                rootFolder.mkdir();
                            } catch (SecurityException se) {
                            }
                        }

                        if (! uidFolder.exists()) {

                            try {
                                uidFolder.mkdir();
                            } catch (SecurityException se) {
                            }
                        }

                        if (! targetFolder.exists()) {
                            try {
                                targetFolder.mkdir();
                            } catch (SecurityException se) {
                            }
                        }
                        String path = targetFolder.getAbsoluteFile() + "/" + getFileNameWithTime2();
                        mCamera.startRecording(path);
                        mThreadShowRecodTime = new ThreadTimer();
                        mThreadShowRecodTime.start();
                    } else {
                        Toast.makeText(mContext, R.string.recording_tips_format, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };

    private class ThreadTimer extends Thread {
        long mCurrentTime = 0;
        long mLastTime = 0;
        long mTime = 0;
        String mShow;
        public boolean mIsRunning = false;
        Time time = new Time();

        public void ThreadTimer () {

        }

        public void stopThread () {
            mIsRunning = false;
        }

        @Override
        public void run () {

            mIsRunning = true;

            while (mIsRunning) {
                mCurrentTime = System.currentTimeMillis();

                if ((mCurrentTime - mLastTime) >= 1000) {
                    if ((mCurrentTime - mLastTime) >= 2000) {
                        mTime += 0;
                    } else {
                        mTime += (mCurrentTime - mLastTime);
                    }

                    time.set(mTime);
                    mShow = time.format("%M:%S");

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run () {
                            tvRecording.setText(mShow);

                            if (mTime >= 180000 && mIsRecording) {
                                button_toolbar_speaker.setEnabled(true);
                                button_toolbar_snapshot.setEnabled(true);
                                button_toolbar_operation.setEnabled(true);
                                button_toolbar_settings.setEnabled(true);
                                layoutRecording.setVisibility(View.GONE);

                                mCamera.stopListening(mSelectedChannel);
                                mCamera.stopSpeaking(mSelectedChannel);
                                swRecording.stopRecord();
                                mCamera.stopRecording();
                                mIsRecording = false;
                                mIsRunning = false;
                            }
                        }
                    });

                    mLastTime = mCurrentTime;
                }
            }
        }
    }

    private mySwitchButton.StateListener stateListener = new mySwitchButton.StateListener() {
        @Override
        public void stateChange (int mode) {
            if (mode == mySwitchButton.MODE_TIME_LAPSE) {
                tvSwitch.setText(getText(R.string.txt_time_lapse));
            } else {
                tvSwitch.setText(getText(R.string.txtNormal));
            }
        }
    };

    private void initSettings () {
        layoutLevel.setVisibility(View.GONE);
        layoutLevel_env.setVisibility(View.GONE);
        layoutBack.setVisibility(View.GONE);
        layoutSettingsMain.setVisibility(View.VISIBLE);
        clearLevels();
        if (mCamera != null) {
            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETSTREAMCTRL_REQ,
                    AVIOCTRLDEFs.SMsgAVIoctrlGetStreamCtrlReq.parseContent(mSelectedChannel));
            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_ENVIRONMENT_REQ,
                    AVIOCTRLDEFs.SMsgAVIoctrlGetEnvironmentReq.parseContent(mSelectedChannel));
            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_BRIGHT_GETBRIGHT_REQ,
                    AVIOCTRLDEFs.SMsgAVIoctrlGetBrightReq.parseContent(mSelectedChannel));
            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_CONTRAST_GETCONTRAST_REQ,
                    AVIOCTRLDEFs.SMsgAVIoctrlGetContrastReq.parseContent(mSelectedChannel));
            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_CHECKSUPPORTMESSAGETYPE_REQ,
                    AVIOCTRLDEFs.SMsgAVIoctrlCheckSupportMessageTypeReq.parseContent(mSelectedChannel, AVIOCTRLDEFs.IOTYPE_PRESET_SETPRESET_REQ));
            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_CHECKSUPPORTMESSAGETYPE_REQ,
                    AVIOCTRLDEFs.SMsgAVIoctrlCheckSupportMessageTypeReq.parseContent(mSelectedChannel, AVIOCTRLDEFs.IOTYPE_CRUISEMODE_CRUISE_START));
        }
    }

    private void getDeviceState () {
        String req;
        if (mCamera != null) {
            req = "custom=1&cmd=3019";
            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, CustomCommand.IOTYPE_USER_WIFICMD_REQ, CustomCommand.SMsgAVIoctrlWifiCmdReq.parseContent
                    (mSelectedChannel, 0, 0, 3019, 1, 0, req.length(), req));

            req = "custom=1&cmd=2020";
            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, CustomCommand.IOTYPE_USER_WIFICMD_REQ, CustomCommand.SMsgAVIoctrlWifiCmdReq.parseContent
                    (mSelectedChannel, 0, 0, 2020, 1, 0, req.length(), req));

            req = "custom=1&cmd=2021";
            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, CustomCommand.IOTYPE_USER_WIFICMD_REQ, CustomCommand.SMsgAVIoctrlWifiCmdReq.parseContent
                    (mSelectedChannel, 0, 0, 2021, 1, 0, req.length(), req));
        }
    }

    private void getOptionsStatus(){
        if (mCamera != null) {
            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_LIVEVIEW_OPTIONS_REQ,
                    AVIOCTRLDEFs.SMsgAVIoctrlLiveviewOptionsReq.parseContent(mSelectedChannel));
        }
    }

    private void inittoolar () {
        button_toolbar_speaker.setBackgroundResource(R.drawable.btn_speaker_off_switch);
        nullLayout.setVisibility(View.VISIBLE);
        linSpeaker.setVisibility(View.GONE);
    }

    private void inittoolarboolean () {
        if (mIsListening) {
            mCamera.stopSpeaking(mSelectedChannel);
            mCamera.stopListening(mSelectedChannel);
        }
        mIsListening = false;
    }

    private void setBatteryState (int level) {
        if (imgBattery != null) {
            switch (level) {
                case AVIOCTRLDEFs.BATTERY_FULL:
                    imgBattery.setImageResource(R.drawable.ic_battery_3);
                    break;
                case AVIOCTRLDEFs.BATTERY_MED:
                    imgBattery.setImageResource(R.drawable.ic_battery_2);
                    break;
                case AVIOCTRLDEFs.BATTERY_LOW:
                    imgBattery.setImageResource(R.drawable.ic_battery_1);
                    break;
                case AVIOCTRLDEFs.BATTERY_EMPTY:
                case AVIOCTRLDEFs.BATTERY_EXHAUSTED:
                    imgBattery.setImageResource(R.drawable.ic_battery_0);
                    break;
                case AVIOCTRLDEFs.BATTERY_CHARGE:
                    imgBattery.setImageResource(R.anim.live_view_battery);
                    break;
            }
        }
    }

    private static String getFileNameWithTime () {

        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH) + 1;
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);
        int mSec = c.get(Calendar.SECOND);
        int mMilliSec = c.get(Calendar.MILLISECOND);

        StringBuffer sb = new StringBuffer();
        sb.append("IMG_");
        sb.append(mYear);
        if (mMonth < 10) {
            sb.append('0');
        }
        sb.append(mMonth);
        if (mDay < 10) {
            sb.append('0');
        }
        sb.append(mDay);
        sb.append('_');
        if (mHour < 10) {
            sb.append('0');
        }
        sb.append(mHour);
        if (mMinute < 10) {
            sb.append('0');
        }
        sb.append(mMinute);
        if (mSec < 10) {
            sb.append('0');
        }
        sb.append(mSec);
        sb.append(".jpg");

        return sb.toString();
    }

    private static String getFileNameWithTime2 () {

        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH) + 1;
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);
        int mSec = c.get(Calendar.SECOND);
        int mMilliSec = c.get(Calendar.MILLISECOND);

        StringBuffer sb = new StringBuffer();
        // sb.append("MP4_");
        sb.append(mYear);
        if (mMonth < 10) {
            sb.append('0');
        }
        sb.append(mMonth);
        if (mDay < 10) {
            sb.append('0');
        }
        sb.append(mDay);
        sb.append('_');
        if (mHour < 10) {
            sb.append('0');
        }
        sb.append(mHour);
        if (mMinute < 10) {
            sb.append('0');
        }
        sb.append(mMinute);
        if (mSec < 10) {
            sb.append('0');
        }
        sb.append(mSec);
        sb.append(".mp4");

        filename = sb.toString();

        return sb.toString();
    }

    private String getSessionMode (int mode) {

        String result = "";
        if (mode == 0) {
            result = getText(R.string.connmode_p2p).toString();
        } else if (mode == 1) {
            result = getText(R.string.connmode_relay).toString();
        } else if (mode == 2) {
            result = getText(R.string.connmode_lan).toString();
        } else {
            result = getText(R.string.none).toString();
        }

        return result;
    }

    private String getPerformance (int mode) {

        String result = "";
        if (mode < 30) {
            result = getText(R.string.txtBad).toString();
        } else {
            result = getText(R.string.txtGood).toString();
        }

        return result;
    }

    private static boolean isSDCardValid () {

        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    private void quit () {

        mIsQuit = true;
        byte[] snapshot = null;
        Bitmap bmp = mCamera.Snapshot(mSelectedChannel);
        if (bmp != null) {
            bmp = compressImage(mCamera.Snapshot(mSelectedChannel));
//			if (bmp.getWidth() * bmp.getHeight() > THUMBNAIL_LIMIT_WIDTH * THUMBNAIL_LIMIT_HEIGHT) {
//				bmp = Bitmap.createScaledBitmap(bmp, THUMBNAIL_LIMIT_WIDTH, THUMBNAIL_LIMIT_HEIGHT, false);
//			}
            snapshot = DatabaseManager.getByteArrayFromBitmap(bmp);
            bmp.recycle();
        }

        DatabaseManager manager = new DatabaseManager(this);
        manager.updateDeviceChannelByUID(mDevUID, mSelectedChannel);

        if (snapshot != null) {
            manager.updateDeviceSnapshotByUID(mDevUID, snapshot);
        }

        if (mCamera != null) {

            if (mIsListening) {
                mCamera.LastAudioMode = 1;
            } else if (mIsSpeaking) {
                mCamera.LastAudioMode = 2;
            } else {
                mCamera.LastAudioMode = 0;
            }
        }

		/* return values to main page */
        Bundle extras = new Bundle();
        extras.putString("dev_uuid", mDevUUID);
        extras.putString("dev_uid", mDevUID);
        extras.putString("dev_nickname", mDevice.NickName);
        extras.putByteArray("snapshot", snapshot);
        extras.putInt("camera_channel", mSelectedChannel);
        extras.putInt("OriginallyChannelIndex", OriginallyChannelIndex);
        extras.putInt("MonitorIndex", mMonitorIndex);

        Intent intent = new Intent();
        intent.putExtras(extras);
        setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    private Bitmap compressImage (Bitmap image) {

        Bitmap tempBitmap = image;

        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 5, baos);
            ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
            if (image.getWidth() * image.getHeight() > THUMBNAIL_LIMIT_WIDTH * THUMBNAIL_LIMIT_HEIGHT) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = 4;
                Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, opts);
                return bitmap;
            } else {
                Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
                return bitmap;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return tempBitmap;
    }

    @Override
    public boolean onKeyDown (int keyCode, KeyEvent event) {

        switch (keyCode) {

            case KeyEvent.KEYCODE_BACK:
                if (isShowContrast) {
                    isShowContrast = false;
                    toolbar_layout.startAnimation(AnimationUtils.loadAnimation(LiveViewActivity.this, R.anim.bottombar_slide_show));
                    toolbar_layout.setVisibility(View.VISIBLE);
                    return false;
                } else if (isShowBrightness) {
                    isShowBrightness = false;
                    toolbar_layout.startAnimation(AnimationUtils.loadAnimation(LiveViewActivity.this, R.anim.bottombar_slide_show));
                    toolbar_layout.setVisibility(View.VISIBLE);
                    return false;
                } else if (isShowCruise) {
                    isShowCruise = false;
                    toolbar_layout.startAnimation(AnimationUtils.loadAnimation(LiveViewActivity.this, R.anim.bottombar_slide_show));
                    toolbar_layout.setVisibility(View.VISIBLE);
                    return false;
                }
                if (isShowPreset) {
                    isShowPreset = false;
                    toolbar_layout.startAnimation(AnimationUtils.loadAnimation(LiveViewActivity.this, R.anim.bottombar_slide_show));
                    toolbar_layout.setVisibility(View.VISIBLE);
                    return false;
                } else {
                    quit();
                    break;
                }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void receiveFrameData (final Camera camera, int avChannel, Bitmap bmp) {

        if (mCamera == camera && avChannel == mSelectedChannel) {
            if (bmp.getWidth() != mVideoWidth || bmp.getHeight() != mVideoHeight) {
                mVideoWidth = bmp.getWidth();
                mVideoHeight = bmp.getHeight();

                reScaleMonitor();
            }
        }
    }

    @Override
    public void receiveFrameInfo (final Camera camera, int avChannel, long bitRate, int frameRate, int onlineNm, int frameCount,
                                  int incompleteFrameCount) {

        if (mCamera == camera && avChannel == mSelectedChannel) {

            mVideoFPS = frameRate;
            mVideoBPS = bitRate;
            mOnlineNm = onlineNm;
            mFrameCount = frameCount;
            mIncompleteFrameCount = incompleteFrameCount;

            Bundle bundle = new Bundle();
            bundle.putInt("avChannel", avChannel);

            Message msg = handler.obtainMessage();
            msg.what = STS_CHANGE_CHANNEL_STREAMINFO;
            msg.setData(bundle);
            handler.sendMessage(msg);
        }
    }

    @Override
    public void receiveChannelInfo (final Camera camera, int avChannel, int resultCode) {

        if (mCamera == camera && avChannel == mSelectedChannel) {
            Bundle bundle = new Bundle();
            bundle.putInt("avChannel", avChannel);

            Message msg = handler.obtainMessage();
            msg.what = resultCode;
            msg.setData(bundle);
            handler.sendMessage(msg);
        }
    }

    @Override
    public void receiveSessionInfo (final Camera camera, int resultCode) {

        if (mCamera == camera) {
            Bundle bundle = new Bundle();
            Message msg = handler.obtainMessage();
            msg.what = resultCode;
            msg.setData(bundle);
            handler.sendMessage(msg);
        }
    }

    @Override
    public void receiveIOCtrlData (final Camera camera, int avChannel, int avIOCtrlMsgType, byte[] data) {

        if (mCamera == camera) {
            Bundle bundle = new Bundle();
            bundle.putInt("avChannel", avChannel);
            bundle.putByteArray("data", data);

            Message msg = handler.obtainMessage();
            msg.what = avIOCtrlMsgType;
            msg.setData(bundle);
            handler.sendMessage(msg);
        }
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage (Message msg) {

            Bundle bundle = msg.getData();
            int avChannel = bundle.getInt("avChannel");
            byte[] data = bundle.getByteArray("data");

            St_SInfo stSInfo = new St_SInfo();
            IOTCAPIs.IOTC_Session_Check(mCamera.getMSID(), stSInfo);

            switch (msg.what) {

                case STS_CHANGE_CHANNEL_STREAMINFO:

                    if (txtResolution != null) {
                        txtResolution.setText(String.valueOf(mVideoWidth) + "x" + String.valueOf(mVideoHeight));
                    }

                    if (txtFrameRate != null) {
                        txtFrameRate.setText(String.valueOf(mVideoFPS));
                    }

                    if (txtBitRate != null) {
                        txtBitRate.setText(String.valueOf(mVideoBPS) + "Kbps");
                    }

                    if (txtOnlineNumber != null) {
                        txtOnlineNumber.setText(String.valueOf(mOnlineNm));
                    }

                    if (txtFrameCount != null) {
                        txtFrameCount.setText(String.valueOf(mFrameCount));
                    }

                    if (txtIncompleteFrameCount != null) {
                        txtIncompleteFrameCount.setText(String.valueOf(mIncompleteFrameCount));
                    }

                    if (txtConnectionMode != null) {
                        txtConnectionMode.setText(getSessionMode(mCamera != null ? mCamera.getSessionMode() : - 1) + " C: " + IOTCAPIs
                                .IOTC_Get_Nat_Type() + ", D: " + stSInfo.NatType + ",R" + mCamera.getbResend());
                    }

                    if (txtRecvFrmPreSec != null) {
                        txtRecvFrmPreSec.setText(String.valueOf(mCamera.getRecvFrmPreSec()));
                    }

                    if (txtDispFrmPreSeco != null) {
                        txtDispFrmPreSeco.setText(String.valueOf(mCamera.getDispFrmPreSec()));
                    }

                    if (txtPerformance != null) {
                        txtPerformance.setText(getPerformance((int) (((float) mCamera.getDispFrmPreSec() / (float) mCamera.getRecvFrmPreSec()) *
                                100)));
                    }

                    break;

                case STS_SNAPSHOT_SCANED:

                    Toast.makeText(LiveViewActivity.this, getText(R.string.tips_snapshot_ok), Toast.LENGTH_SHORT).show();

                    break;

                case Camera.CONNECTION_STATE_CONNECTING:

                    if (! mCamera.isSessionConnected() || ! mCamera.isChannelConnected(mSelectedChannel)) {

                        mConnStatus = getText(R.string.connstus_connecting).toString();

                        if (txtConnectionStatus != null) {
                            txtConnectionStatus.setText(mConnStatus);
                            txtConnectionStatus.setBackgroundResource(R.drawable.bg_unknow);

                        }
                    }

                    break;

                case Camera.CONNECTION_STATE_CONNECTED:

                    if (mCamera.isSessionConnected() && avChannel == mSelectedChannel && mCamera.isChannelConnected(mSelectedChannel)) {

                        mConnStatus = getText(R.string.connstus_connected).toString();

                        if (txtConnectionStatus != null) {
                            txtConnectionStatus.setText(mConnStatus);
                            txtConnectionStatus.setBackgroundResource(R.drawable.bg_online);
                        }

                        mCamera.startShow(mSelectedChannel, true, unavailable);

                        Configuration cfg = getResources().getConfiguration();
                        if (cfg.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            if (unavailable) {
                                setupViewInLandscapeLayout(SOFTWARE_DECODE);
                            } else {
                                setupViewInLandscapeLayout(HARDWARE_DECODE);
                            }

                        } else if (cfg.orientation == Configuration.ORIENTATION_PORTRAIT) {
                            if (unavailable) {
                                setupViewInPortraitLayout(SOFTWARE_DECODE);
                            } else {
                                setupViewInPortraitLayout(HARDWARE_DECODE);
                            }
                        }
                    }

                    break;

                case Camera.CONNECTION_STATE_DISCONNECTED:

                    mConnStatus = getText(R.string.connstus_disconnect).toString();

                    if (txtConnectionStatus != null) {
                        txtConnectionStatus.setText(mConnStatus);
                        txtConnectionStatus.setBackgroundResource(R.drawable.bg_offline);
                    }

                    break;

                case Camera.CONNECTION_STATE_UNKNOWN_DEVICE:

                    mConnStatus = getText(R.string.connstus_unknown_device).toString();

                    if (txtConnectionStatus != null) {
                        txtConnectionStatus.setText(mConnStatus);
                        txtConnectionStatus.setBackgroundResource(R.drawable.bg_offline);
                    }

                    break;

                case Camera.CONNECTION_STATE_TIMEOUT:

                    if (mCamera != null) {

                        mCamera.disconnect();
                        if (mSoftMonitor != null) {
                            mSoftMonitor.deattachCamera();
                        }
                        if (mHardMonitor != null) {
                            mHardMonitor.deattachCamera();
                        }

                        this.postDelayed(new Runnable() {
                            @Override
                            public void run () {
                                mCamera.connect(mDevUID);
                                mCamera.start(Camera.DEFAULT_AV_CHANNEL, mDevice.View_Account, mDevice.View_Password);
                                mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETSUPPORTSTREAM_REQ,
                                        SMsgAVIoctrlGetSupportStreamReq.parseContent());
                                mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_REQ, AVIOCTRLDEFs
                                        .SMsgAVIoctrlDeviceInfoReq.parseContent());
                                mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETAUDIOOUTFORMAT_REQ, AVIOCTRLDEFs
                                        .SMsgAVIoctrlGetAudioOutFormatReq.parseContent());
                                mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIMEZONE_REQ, AVIOCTRLDEFs
                                        .SMsgAVIoctrlTimeZone.parseContent());
                                mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_CONTRAST_GETCONTRAST_REQ, AVIOCTRLDEFs
                                        .SMsgAVIoctrlGetContrastReq.parseContent(mSelectedChannel));
                            }
                        }, 2000);
                    }

                    break;

                case Camera.CONNECTION_STATE_CONNECT_FAILED:

                    mConnStatus = getText(R.string.connstus_connection_failed).toString();

                    if (txtConnectionStatus != null) {
                        txtConnectionStatus.setText(mConnStatus);
                        txtConnectionStatus.setBackgroundResource(R.drawable.bg_offline);
                    }

                    break;

                case Camera.CONNECTION_STATE_WRONG_PASSWORD:

                    mConnStatus = getText(R.string.connstus_wrong_password).toString();

                    if (txtConnectionStatus != null) {
                        txtConnectionStatus.setText(mConnStatus);
                        txtConnectionStatus.setBackgroundResource(R.drawable.bg_offline);
                    }

                    break;

                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETSUPPORTSTREAM_RESP:

                    break;

                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_ENVIRONMENT_RESP:
                    mEnv = data[4];
                    switch (mEnv) {
                        case AVIOCTRLDEFs.AVIOCTRL_ENVIRONMENT_INDOOR_50HZ:
                            tvEnv.setText(getText(R.string.txtmEnvironment50));
                            break;
                        case AVIOCTRLDEFs.AVIOCTRL_ENVIRONMENT_INDOOR_60HZ:
                            tvEnv.setText(getText(R.string.txtmEnvironment60));
                            break;
                        case AVIOCTRLDEFs.AVIOCTRL_ENVIRONMENT_OUTDOOR:
                            tvEnv.setText(getText(R.string.txtmEnvironmentout));
                            break;
                        case AVIOCTRLDEFs.AVIOCTRL_ENVIRONMENT_NIGHT:
                            tvEnv.setText(getText(R.string.txtmEnvironmentnight));
                            break;
                    }
                    break;

                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETSTREAMCTRL_RESP:
                    mQVGA = data[4];
                    switch (mQVGA) {
                        case AVIOCTRLDEFs.AVIOCTRL_QUALITY_MIN:
                            tvQVGA.setText(getText(R.string.txt_lowest));
                            break;
                        case AVIOCTRLDEFs.AVIOCTRL_QUALITY_LOW:
                            tvQVGA.setText(getText(R.string.txt_low));
                            break;
                        case AVIOCTRLDEFs.AVIOCTRL_QUALITY_MIDDLE:
                            tvQVGA.setText(getText(R.string.txt_medium));
                            break;
                        case AVIOCTRLDEFs.AVIOCTRL_QUALITY_HIGH:
                            tvQVGA.setText(getText(R.string.txt_high));
                            break;
                        case AVIOCTRLDEFs.AVIOCTRL_QUALITY_MAX:
                            tvQVGA.setText(getText(R.string.txt_highest));
                            break;
                    }
                    break;

                case AVIOCTRLDEFs.IOTYPE_CONTRAST_GETCONTRAST_RESP:
                    mContrast = data[4];
                    switch (mContrast) {
                        case LEVEL_LOWEST:
                            tvContrast.setText(getText(R.string.txt_lowest));
                            break;
                        case LEVEL_LOW:
                            tvContrast.setText(getText(R.string.txt_low));
                            break;
                        case LEVEL_MEDIUM:
                            tvContrast.setText(getText(R.string.txt_medium));
                            break;
                        case LEVEL_HIGH:
                            tvContrast.setText(getText(R.string.txt_high));
                            break;
                        case LEVEL_HIGHEST:
                            tvContrast.setText(getText(R.string.txt_highest));
                            break;
                    }
                    break;

                case AVIOCTRLDEFs.IOTYPE_BRIGHT_GETBRIGHT_RESP:
                    if ((data[4] > 0) && (data[4] < 6)) {
                        mBrightness = data[4];
                        switch (mBrightness) {
                            case LEVEL_LOWEST:
                                tvBright.setText(getText(R.string.txt_lowest));
                                break;
                            case LEVEL_LOW:
                                tvBright.setText(getText(R.string.txt_low));
                                break;
                            case LEVEL_MEDIUM:
                                tvBright.setText(getText(R.string.txt_medium));
                                break;
                            case LEVEL_HIGH:
                                tvBright.setText(getText(R.string.txt_high));
                                break;
                            case LEVEL_HIGHEST:
                                tvBright.setText(getText(R.string.txt_highest));
                                break;
                        }
                    }
                    break;

                case CustomCommand.IOTYPE_USER_WIFICMD_RESP:
                    AVIOCTRLDEFs.SMsgAVIoctrlWifiCmdResp.fillContent(data);

                    byte[] buff = new byte[AVIOCTRLDEFs.SMsgAVIoctrlWifiCmdResp.nDataLength];
                    System.arraycopy(AVIOCTRLDEFs.SMsgAVIoctrlWifiCmdResp.response, 0, buff, 0,
                            AVIOCTRLDEFs.SMsgAVIoctrlWifiCmdResp.nDataLength);
                    String[] filter = new String(buff).split(",");
                    Log.i("RRR", "RECV " + AVIOCTRLDEFs.SMsgAVIoctrlWifiCmdResp.nCmdID + " " + new String(buff));
                    switch (AVIOCTRLDEFs.SMsgAVIoctrlWifiCmdResp.nCmdID) {
                        case AVIOCTRLDEFs.AVIOCTRL_WIFICMD_GET_BATTERY:
                            setBatteryState(Integer.valueOf(filter[2]));
                            break;

                        case AVIOCTRLDEFs.AVIOCTRL_WIFICMD_GET_WDR:
                            switch(Integer.valueOf(filter[2])){
                                case 0:   // off
                                    swWDR.setCheck(false);
                                    break;
                                case 1:  // on
                                    swWDR.setCheck(true);
                                    break;
                            }
                            break;

                        case AVIOCTRLDEFs.AVIOCTRL_WIFICMD_GET_TIME_LAPSE:
                            if(Integer.valueOf(filter[2]) > 0){
                                mIsLapsing = true;
                                swRecording.isLapsing();
                            }
                            break;
                    }

                    break;

                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_CHECKSUPPORTMESSAGETYPE_RESP:
                    if(Packet.byteArrayToInt_Little(data, 0) == mSelectedChannel){
                        switch (Packet.byteArrayToInt_Little(data, 4)){
                            case AVIOCTRLDEFs.IOTYPE_PRESET_SETPRESET_REQ:
                                if(data[8] == 1) {
                                    layoutPreset.setVisibility(View.VISIBLE);
                                }
                                break;

                            case AVIOCTRLDEFs.IOTYPE_CRUISEMODE_CRUISE_START:
                                if(data[8] == 1) {
                                    layoutCruise.setVisibility(View.VISIBLE);
                                }
                                break;
                        }
                    }
                    break;

                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_LIVEVIEW_OPTIONS_RESP:
                    if(Packet.byteArrayToInt_Little(data, 0) == mSelectedChannel){
                        mQVGA = data[4];
                        mEnv = data[5];
                        mContrast = data[6];
                        mBrightness = data[7];
                        mFlip = data[8];
                        setBatteryState(data[9]);
                        int wdr = data[10];
                        int timelapse = data[11];

                        switch (mQVGA) {
                            case AVIOCTRLDEFs.AVIOCTRL_QUALITY_MIN:
                                tvQVGA.setText(getText(R.string.txt_lowest));
                                break;
                            case AVIOCTRLDEFs.AVIOCTRL_QUALITY_LOW:
                                tvQVGA.setText(getText(R.string.txt_low));
                                break;
                            case AVIOCTRLDEFs.AVIOCTRL_QUALITY_MIDDLE:
                                tvQVGA.setText(getText(R.string.txt_medium));
                                break;
                            case AVIOCTRLDEFs.AVIOCTRL_QUALITY_HIGH:
                                tvQVGA.setText(getText(R.string.txt_high));
                                break;
                            case AVIOCTRLDEFs.AVIOCTRL_QUALITY_MAX:
                                tvQVGA.setText(getText(R.string.txt_highest));
                                break;
                        }

                        switch (mEnv) {
                            case AVIOCTRLDEFs.AVIOCTRL_ENVIRONMENT_INDOOR_50HZ:
                                tvEnv.setText(getText(R.string.txtmEnvironment50));
                                break;
                            case AVIOCTRLDEFs.AVIOCTRL_ENVIRONMENT_INDOOR_60HZ:
                                tvEnv.setText(getText(R.string.txtmEnvironment60));
                                break;
                            case AVIOCTRLDEFs.AVIOCTRL_ENVIRONMENT_OUTDOOR:
                                tvEnv.setText(getText(R.string.txtmEnvironmentout));
                                break;
                            case AVIOCTRLDEFs.AVIOCTRL_ENVIRONMENT_NIGHT:
                                tvEnv.setText(getText(R.string.txtmEnvironmentnight));
                                break;
                        }

                        switch (mContrast) {
                            case LEVEL_LOWEST:
                                tvContrast.setText(getText(R.string.txt_lowest));
                                break;
                            case LEVEL_LOW:
                                tvContrast.setText(getText(R.string.txt_low));
                                break;
                            case LEVEL_MEDIUM:
                                tvContrast.setText(getText(R.string.txt_medium));
                                break;
                            case LEVEL_HIGH:
                                tvContrast.setText(getText(R.string.txt_high));
                                break;
                            case LEVEL_HIGHEST:
                                tvContrast.setText(getText(R.string.txt_highest));
                                break;
                        }

                        switch (mBrightness) {
                            case LEVEL_LOWEST:
                                tvBright.setText(getText(R.string.txt_lowest));
                                break;
                            case LEVEL_LOW:
                                tvBright.setText(getText(R.string.txt_low));
                                break;
                            case LEVEL_MEDIUM:
                                tvBright.setText(getText(R.string.txt_medium));
                                break;
                            case LEVEL_HIGH:
                                tvBright.setText(getText(R.string.txt_high));
                                break;
                            case LEVEL_HIGHEST:
                                tvBright.setText(getText(R.string.txt_highest));
                                break;
                        }

                        switch(wdr){
                            case 0:   // off
                                swWDR.setCheck(false);
                                break;
                            case 1:  // on
                                swWDR.setCheck(true);
                                break;
                        }

                        if(timelapse > 0){
                            mIsLapsing = true;
                            swRecording.isLapsing();
                        }
                    }

                    break;
            }
        }
    };

    @Override
    public View makeView () {
        TextView t = new TextView(this);
        return t;
    }

    @Override
    public void OnClick () {
        // TODO Auto-generated method stub
        if (mIsListening) {
            return;
        }

        if (mShowSettingsBubble) {
            mShowSettingsBubble = false;
            layoutSettings.setVisibility(View.GONE);
            initSettings();
        } else if (mShowOperationBubble) {
            mShowOperationBubble = false;
            layoutOperation.setVisibility(View.GONE);
            layoutBack_oper.setVisibility(View.GONE);
            layoutOperFlip.setVisibility(View.GONE);
            layoutOperMain.setVisibility(View.VISIBLE);
        } else {
            if (isShowToolBar) {
                isShowToolBar = false;
                if (toolbar_layout != null) {
                    toolbar_layout.startAnimation(AnimationUtils.loadAnimation(LiveViewActivity.this, R.anim.bottombar_slide_hide));
                    toolbar_layout.setVisibility(View.GONE);
                }

                if (layoutTitleBar != null) {
                    layoutTitleBar.startAnimation(AnimationUtils.loadAnimation(LiveViewActivity.this, R.anim.topbar_slide_hide));
                    layoutTitleBar.setVisibility(View.GONE);
                }

                tvScale.startAnimation(AnimationUtils.loadAnimation(LiveViewActivity.this, R.anim.topbar_slide_hide));
                btnMagicZoom.startAnimation(AnimationUtils.loadAnimation(LiveViewActivity.this, R.anim.bottombar_slide_hide));
            } else {
                isShowToolBar = true;
                if (toolbar_layout != null) {
                    toolbar_layout.startAnimation(AnimationUtils.loadAnimation(LiveViewActivity.this, R.anim.bottombar_slide_show));
                    toolbar_layout.setVisibility(View.VISIBLE);
                }

                if (layoutTitleBar != null) {
                    layoutTitleBar.startAnimation(AnimationUtils.loadAnimation(LiveViewActivity.this, R.anim.topbar_slide_show));
                    layoutTitleBar.setVisibility(View.VISIBLE);
                }

                tvScale.startAnimation(AnimationUtils.loadAnimation(LiveViewActivity.this, R.anim.topbar_slide_show));
                btnMagicZoom.startAnimation(AnimationUtils.loadAnimation(LiveViewActivity.this, R.anim.bottombar_slide_show));
            }
        }
    }

    @Override
    public boolean onTouch (View v, MotionEvent event) {
        // TODO Auto-generated method stub

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                speaker_tips.setVisibility(View.INVISIBLE);
                if (mCamera != null) {
                    mCamera.startSpeaking(mSelectedChannel);
                    mCamera.stopListening(mSelectedChannel);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mCamera != null) {
                    mCamera.stopSpeaking(mSelectedChannel);
                    mCamera.startListening(mSelectedChannel, mIsListening);
                }
                break;
        }
        return false;
    }

    @Override
    public void OnSnapshotComplete () {
        // TODO Auto-generated method stub
        MediaScannerConnection.scanFile(LiveViewActivity.this, new String[] {mFilePath.toString()}, new String[] {"image/*"},
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted (String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                        Message msg = handler.obtainMessage();
                        msg.what = STS_SNAPSHOT_SCANED;
                        handler.sendMessage(msg);
                    }
                });
    }

    private long getAvailaleSize () {

        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();

        return (availableBlocks * blockSize) / 1024 / 1024;
    }

    private void reScaleMonitor () {

        if (mVideoHeight == 0 || mVideoWidth == 0 || mMiniMonitorHeight == 0) {
            return;
        }

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int screenWidth = size.x;
        final int screenHeight = size.y;

        final SurfaceView surfaceView;
        if (unavailable) {
            surfaceView = (SurfaceView) mSoftMonitor;
        } else {
            surfaceView = (SurfaceView) mHardMonitor;
            if (mHardMonitor != null) {
                mHardMonitor.callSurfaceChange();
            }
        }

        if (surfaceView == null || mHardMonitorLayout == null || mSoftMonitorLayout == null) {
            return;
        }

        /**
         * portrait mode
         */
        if (screenHeight >= screenWidth) {

            mFrameMode = FrameMode.PORTRAIT;
            surfaceView.getLayoutParams().width = screenWidth;
            surfaceView.getLayoutParams().height = (int) (screenWidth * mVideoHeight / (float) mVideoWidth);

            if (mMiniMonitorHeight < surfaceView.getLayoutParams().height) {
                surfaceView.getLayoutParams().height = mMiniMonitorHeight;
            }

            if(unavailable){
                mSoftMonitorLayout.getLayoutParams().width = surfaceView.getLayoutParams().width;
                mSoftMonitorLayout.getLayoutParams().height = surfaceView.getLayoutParams().height;
            }else{
                mHardMonitorLayout.getLayoutParams().width = surfaceView.getLayoutParams().width;
                mHardMonitorLayout.getLayoutParams().height = surfaceView.getLayoutParams().height;
            }
        }
        /**
         * landscape mode
         */
        else {

            if (surfaceView.getLayoutParams().width > screenWidth) {
                /**
                 * up down space
                 */
                if(!unavailable) {
                    surfaceView.getLayoutParams().width = screenWidth;
                    surfaceView.getLayoutParams().height = (int) (screenWidth * mVideoHeight / (float) mVideoWidth);

                    final int scrollViewHeight = surfaceView.getLayoutParams().height;
                    handler.post(new Runnable() {
                        @Override
                        public void run () {
                            int statusbar = 0;
                            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                            if (resourceId > 0) {
                                statusbar = getResources().getDimensionPixelSize(resourceId);
                            }
                            mHardMonitorLayout.setPadding(0, (screenHeight - statusbar - scrollViewHeight) / 2, 0, 0);
                        }
                    });
                }else{
                    surfaceView.getLayoutParams().width = screenWidth;
                    surfaceView.getLayoutParams().height = screenHeight;
                }


            } else {
                /**
                 * left right space
                 */
                mFrameMode = FrameMode.LANDSCAPE_ROW_MAJOR;
                if(!unavailable) {
                    surfaceView.getLayoutParams().height = screenHeight;
                    surfaceView.getLayoutParams().width = (int) (screenHeight * mVideoWidth / (float) mVideoHeight);

                    final int scrollViewWidth = surfaceView.getLayoutParams().width;
                    handler.post(new Runnable() {
                        @Override
                        public void run () {
                            mHardMonitorLayout.setPadding((screenWidth - scrollViewWidth) / 2, 0, 0, 0);
                        }
                    });
                }else{
                    surfaceView.getLayoutParams().width = screenWidth;
                    surfaceView.getLayoutParams().height = screenHeight;
                }
            }
        }

        mMiniVideoHeight = surfaceView.getLayoutParams().height;
        mMiniVideoWidth = surfaceView.getLayoutParams().width;

        runOnUiThread(new Runnable() {
            @Override
            public void run () {
                surfaceView.setLayoutParams(surfaceView.getLayoutParams());
            }
        });
    }

    @Override
    public void receiveFrameDataForMediaCodec (Camera camera, int avChannel, byte[] buf, int length, int pFrmNo, byte[] pFrmInfoBuf,
                                               boolean isIframe, int codecId) {
        if (mHardMonitor != null && mHardMonitor.getClass().equals(MediaCodecMonitor.class)) {
            if ((mVideoWidth != ((MediaCodecMonitor) mHardMonitor).getVideoWidth() || mVideoHeight != ((MediaCodecMonitor) mHardMonitor)
                    .getVideoHeight())) {

                mVideoWidth = ((MediaCodecMonitor) mHardMonitor).getVideoWidth();
                mVideoHeight = ((MediaCodecMonitor) mHardMonitor).getVideoHeight();

                reScaleMonitor();
            }
        }
    }

    private void changeQVGA (int level) {

        if (mCamera != null) {
            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETSTREAMCTRL_REQ,
                    AVIOCTRLDEFs.SMsgAVIoctrlSetStreamCtrlReq.parseContent(mDevice.ChannelIndex, (byte) level));
            mCamera.stopShow(mSelectedChannel);
        }

        if (mSoftMonitor != null) {
            mSoftMonitor.deattachCamera();
        }
        if (mHardMonitor != null) {
            mHardMonitor.deattachCamera();
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run () {
                if (unavailable) {
                    mCamera.startShow(mSelectedChannel, true, SOFTWARE_DECODE);
                    mSoftMonitor.attachCamera(mCamera, mSelectedChannel);
                } else {
                    mCamera.startShow(mSelectedChannel, true, HARDWARE_DECODE);
                    mHardMonitor.attachCamera(mCamera, mSelectedChannel);
                }
            }
        }, 500);
    }

    private void changeEnv (int mode) {
        mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_ENVIRONMENT_REQ,
                AVIOCTRLDEFs.SMsgAVIoctrlSetEnvironmentReq.parseContent(mDevice.ChannelIndex, (byte) mode));
    }

    @Override
    public void Unavailable () {
        if (unavailable) {
            return;
        }

        unavailable = true;
        if(mCodecSettings != null){
            mCodecSettings.edit().putBoolean("unavailable", unavailable).commit();
        }

        if (mSoftMonitor != null) {
            mSoftMonitor.deattachCamera();
        }
        if (mHardMonitor != null) {
            mHardMonitor.deattachCamera();
        }

        Configuration cfg = getResources().getConfiguration();

        if (cfg.orientation == Configuration.ORIENTATION_PORTRAIT) {
            runOnUiThread(new Runnable() {
                @Override
                public void run () {
                    if (mCamera != null) {

                        mCamera.stopShowWithoutIOCtrl(mSelectedChannel);
//                        mCamera.stopShow(mSelectedChannel);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run () {
//                                mCamera.startShow(mSelectedChannel, true, SOFTWARE_DECODE);
                                mCamera.startShowWithoutIOCtrl(mSelectedChannel, SOFTWARE_DECODE);
                                setupViewInPortraitLayout(SOFTWARE_DECODE);
                            }
                        }, 1000);

                    }
                }
            });
        } else if (cfg.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            runOnUiThread(new Runnable() {
                @Override
                public void run () {
                    if (mCamera != null) {

                        mCamera.stopShowWithoutIOCtrl(mSelectedChannel);
                        mCamera.stopShow(mSelectedChannel);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run () {
//                                mCamera.startShow(mSelectedChannel, true, SOFTWARE_DECODE);
                                mCamera.startShowWithoutIOCtrl(mSelectedChannel, SOFTWARE_DECODE);
                                setupViewInLandscapeLayout(SOFTWARE_DECODE);
                            }
                        }, 1000);
                    }
                }
            });
        }
    }

    @Override
    public void monitorIsReady () {
        if(progress != null){
            runOnUiThread(new Runnable() {
                @Override
                public void run () {
                    progress.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void zoomSurface (final float scale) {

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int screenWidth = size.x;
        final int screenHeight = size.y;

        runOnUiThread(new Runnable() {
            @Override
            public void run () {
                SurfaceView surfaceView = (SurfaceView) mHardMonitor;
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) surfaceView.getLayoutParams();

                int paddingLeft = 0;
                int paddingTop = 0;

                if(mFrameMode == FrameMode.LANDSCAPE_COL_MAJOR) {
                    int statusbar = 0;
                    int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                    if (resourceId > 0) {
                        statusbar = getResources().getDimensionPixelSize(resourceId);
                    }
                    paddingTop = (screenHeight - statusbar - lp.height) / 2;
                    if(paddingTop < 0)
                        paddingTop = 0;
                }
                else if(mFrameMode == FrameMode.LANDSCAPE_ROW_MAJOR) {
                    paddingLeft = (screenWidth - lp.width) / 2;
                    if(paddingLeft < 0)
                        paddingLeft = 0;
                }

                mHardMonitorLayout.setPadding(paddingLeft, paddingTop, 0, 0);
            }
        });
    }

}
