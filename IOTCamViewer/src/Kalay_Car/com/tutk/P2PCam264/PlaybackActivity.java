package com.tutk.P2PCam264;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.AVIOCTRLDEFs.STimeDay;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.IMonitor;
import com.tutk.IOTC.IRegisterIOTCListener;
import com.tutk.IOTC.LargeDownloadListener;
import com.tutk.IOTC.MediaCodecListener;
import com.tutk.IOTC.MediaCodecMonitor;
import com.tutk.IOTC.Packet;
import com.tutk.Kalay.general.R;
import com.tutk.P2PCam264.DELUX.Custom_OkCancle_Dialog;
import com.tutk.P2PCam264.DELUX.MultiViewActivity;

import java.io.File;

public class PlaybackActivity extends SherlockActivity implements IRegisterIOTCListener, MediaCodecListener,
        Custom_OkCancle_Dialog.OkCancelDialogListener, LargeDownloadListener {

    private static final int Build_VERSION_CODES_ICE_CREAM_SANDWICH = 14;
    private static final int STS_CHANGE_CHANNEL_STREAMINFO = 99;
    private final boolean SOFTWARE_DECODE = true;
    private final boolean HARDWARE_DECODE = false;

    // private TouchedMonitor monitor = null;
    private IMonitor monitor = null;
    //	private IMonitor mSoftMonitor = null;
//    private IMonitor mHardMonitor = null;
    private MyCamera mCamera = null;
    private SharedPreferences mCodecSettings;


    private RelativeLayout layoutSoftMonitor;
    private RelativeLayout layoutHardMonitor;

    private TextView txtEventType;
    private TextView txtEventTime;
    private TextView txtResolution;
    private TextView txtFrameRate;
    private TextView txtBitRate;
    private TextView txtFrameCount;
    private TextView txtIncompleteFrameCount;

    private String mDevUUID;
    private String mDevNickname;
    private String mViewAcc;
    private String mViewPwd;
    private String mEvtUUID;
    private String mFilePath;

    private int mCameraChannel;
    private int mEvtType;
    private STimeDay mEvtTime2;

    private int mVideoWidth;
    private int mVideoHeight;

    private final int MEDIA_STATE_STOPPED = 0;
    private final int MEDIA_STATE_PLAYING = 1;
    private final int MEDIA_STATE_PAUSED = 2;
    private final int MEDIA_STATE_OPENING = 3;

    private int mPlaybackChannel = - 1;
    private int mMediaState = MEDIA_STATE_STOPPED;
    private int mMiniMonitorHeight;

    private BitmapDrawable bg;
    private BitmapDrawable bgSplit;
    private ImageButton btnPlayPause;
    private ImageButton btnDownload;
    private ProgressBar progress;

    private boolean mIsListening = true;
    private boolean unavailable = false;
    private boolean mHasFile = false;

    private enum FrameMode {
        PORTRAIT, LANDSCAPE_ROW_MAJOR, LANDSCAPE_COL_MAJOR
    }

    private FrameMode mFrameMode = FrameMode.PORTRAIT;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playback_portrait);

        bg = (BitmapDrawable) getResources().getDrawable(R.drawable.bg_striped);
        bgSplit = (BitmapDrawable) getResources().getDrawable(R.drawable.bg_striped_split_img);

        Bundle bundle = this.getIntent().getExtras();
        mDevUUID = bundle != null ? bundle.getString("dev_uuid") : "";
        mDevNickname = bundle != null ? bundle.getString("dev_nickname") : "";
        mCameraChannel = bundle != null ? bundle.getInt("camera_channel") : - 1;
        mViewAcc = bundle != null ? bundle.getString("view_acc") : "";
        mViewPwd = bundle != null ? bundle.getString("view_pwd") : "";
        mEvtType = bundle != null ? bundle.getInt("event_type") : - 1;
        // mEvtTime = bundle != null ? bundle.getLong("event_time") : -1;
        mEvtUUID = bundle != null ? bundle.getString("event_uuid") : null;
        mEvtTime2 = bundle != null ? new STimeDay(bundle.getByteArray("event_time2")) : null;
        mHasFile = bundle != null ? bundle.getBoolean("has_file") : false;
        mFilePath = bundle != null ? bundle.getString("path") : null;

        for (MyCamera camera : MultiViewActivity.CameraList) {

            if (mDevUUID.equalsIgnoreCase(camera.getUUID())) {
                mCamera = camera;
                mCamera.registerIOTCListener(this);
                mCamera.resetEventCount();
                break;
            }
        }

        mCodecSettings = getSharedPreferences("CodecSettings", 0);
        if (mCodecSettings != null) {
            unavailable = mCodecSettings.getBoolean("unavailable", false);
        }

        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getOrientation();

        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
            setupViewInPortraitLayout(unavailable);
        } else {
            setupViewInLandscapeLayout(unavailable);
        }

        if (mCamera != null) {
            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL,
                    AVIOCTRLDEFs.SMsgAVIoctrlPlayRecord.parseContent(mCameraChannel, AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_START, 0,
                            mEvtTime2.toByteArray()));
            mMediaState = MEDIA_STATE_OPENING;

			/* if server no response, close playback function */
            handler.postDelayed(new Runnable() {
                @Override
                public void run () {
                    if (mPlaybackChannel < 0 && mMediaState == MEDIA_STATE_OPENING) {
                        mMediaState = MEDIA_STATE_STOPPED;
                        Toast.makeText(PlaybackActivity.this, getText(R.string.tips_play_record_timeout), Toast.LENGTH_SHORT).show();
                        if (btnPlayPause != null) {
                            btnPlayPause.setBackgroundResource(R.drawable.btn_play);
                        }
                    }
                }
            }, 5000);
        }

        Custom_OkCancle_Dialog.SetDialogListener(this);

    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        quit();
    }

    @Override
    protected void onStart () {
        super.onStart();
    }

    @Override
    protected void onStop () {
        super.onStop();
    }

    @Override
    protected void onPause () {
        super.onPause();

        if (mCamera != null) {
            mCamera.stopListening(mPlaybackChannel);
            mCamera.stopShow(mPlaybackChannel);
            mCamera.stop(mPlaybackChannel);
            mCamera.unregisterIOTCListener(this);
        }

        if (monitor != null) {
            monitor.deattachCamera();
        }
    }

    @Override
    protected void onResume () {
        super.onResume();
    }

    @Override
    public void onConfigurationChanged (Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

//        if (mSoftMonitor != null) {
//            mSoftMonitor.deattachCamera();
//        }
//        if (mHardMonitor != null) {
//            mHardMonitor.deattachCamera();
//        }

        if (monitor != null) {
            monitor.deattachCamera();
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
    public boolean onKeyDown (int keyCode, KeyEvent event) {

        switch (keyCode) {

            case KeyEvent.KEYCODE_BACK:

                quit();

                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    private OnClickListener clickPlayPause = new OnClickListener() {
        @Override
        public void onClick (View v) {
            if (mPlaybackChannel < 0) {

                if (mCamera != null) {
                    mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL,
                            AVIOCTRLDEFs.SMsgAVIoctrlPlayRecord.parseContent(mCameraChannel, AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_START, 0,
                                    mEvtTime2.toByteArray()));
                    mMediaState = MEDIA_STATE_OPENING;

					/* if server no response, close playback function */
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run () {
                            if (mPlaybackChannel < 0 && mMediaState == MEDIA_STATE_OPENING) {
                                mMediaState = MEDIA_STATE_STOPPED;
                                Toast.makeText(PlaybackActivity.this, getText(R.string.tips_play_record_timeout), Toast.LENGTH_SHORT).show();
                                if (btnPlayPause != null) {
                                    btnPlayPause.setBackgroundResource(R.drawable.btn_play);
                                }
                            }
                        }
                    }, 5000);
                }
            } else {
                if (mCamera != null) {
                    mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL,
                            AVIOCTRLDEFs.SMsgAVIoctrlPlayRecord.parseContent(mCameraChannel, AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_PAUSE, 0,
                                    mEvtTime2.toByteArray()));

                    if (btnPlayPause != null) {
                        btnPlayPause.setBackgroundResource(R.drawable.btn_pause);
                    }
                }
            }
        }
    };

    private void setupViewInPortraitLayout (final boolean runSoftwareDecode) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.titlebar);
        TextView tv = (TextView) this.findViewById(R.id.bar_text);
        tv.setText(getText(R.string.dialog_Playback));

        setContentView(R.layout.playback_portrait);

        if (Build.VERSION.SDK_INT < Build_VERSION_CODES_ICE_CREAM_SANDWICH) {
            bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
            getSupportActionBar().setBackgroundDrawable(bg);

            bgSplit.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
            getSupportActionBar().setSplitBackgroundDrawable(bgSplit);
        }

        txtEventType = (TextView) findViewById(R.id.txtEventType);
        txtEventTime = (TextView) findViewById(R.id.txtEventTime);
        txtResolution = (TextView) findViewById(R.id.txtResolution);
        txtFrameRate = (TextView) findViewById(R.id.txtFrameRate);
        txtBitRate = (TextView) findViewById(R.id.txtBitRate);
        txtFrameCount = (TextView) findViewById(R.id.txtFrameCount);
        txtIncompleteFrameCount = (TextView) findViewById(R.id.txtIncompleteFrameCount);
        btnPlayPause = (ImageButton) findViewById(R.id.btn_playpause);
        btnDownload = (ImageButton) findViewById(R.id.btnDownload);
        progress = (ProgressBar) findViewById(R.id.progressBar);

        btnPlayPause.setOnClickListener(clickPlayPause);
        btnPlayPause.setBackgroundResource(R.drawable.btn_pause);

        txtEventType.setText(MultiViewActivity.getEventType(PlaybackActivity.this, mEvtType, false));
        txtEventTime.setText(mEvtTime2.getLocalTime());

        layoutSoftMonitor = (RelativeLayout) findViewById(R.id.layoutSoftMonitor);
        layoutHardMonitor = (RelativeLayout) findViewById(R.id.layoutHardMonitor);

        if (runSoftwareDecode) {
            layoutSoftMonitor.setVisibility(View.VISIBLE);
            layoutHardMonitor.setVisibility(View.GONE);
            monitor = (IMonitor) findViewById(R.id.softMonitor);
        } else {
            layoutSoftMonitor.setVisibility(View.GONE);
            layoutHardMonitor.setVisibility(View.VISIBLE);
            monitor = (IMonitor) findViewById(R.id.hardMonitor);

            // calculate surface view size
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;

            final SurfaceView surfaceView = (SurfaceView) monitor;
            surfaceView.getLayoutParams().width = width;
            handler.post(new Runnable() {
                @Override
                public void run () {
                    if (layoutHardMonitor.getMeasuredHeight() == 0) {
                        handler.postDelayed(this, 200);
                    } else {
                        mMiniMonitorHeight = layoutHardMonitor.getMeasuredHeight();
                        surfaceView.getLayoutParams().height = layoutHardMonitor.getMeasuredHeight();
                        surfaceView.setLayoutParams(surfaceView.getLayoutParams());
                        reScaleMonitor();
                    }
                }
            });
        }

        if (mPlaybackChannel >= 0) {
            monitor.enableDither(mCamera.mEnableDither);
            monitor.attachCamera(mCamera, mPlaybackChannel);
            monitor.setMediaCodecListener(this);
        }

        if (mHasFile) {
            btnDownload.setBackgroundResource(R.drawable.btn_download_h);
        } else {
            btnDownload.setBackgroundResource(R.drawable.btn_download);
        }

        btnDownload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick (View v) {
                if (! mHasFile) {
                    Custom_OkCancle_Dialog dlg = new Custom_OkCancle_Dialog(PlaybackActivity.this, getText(R.string.tips_download_video).toString());
                    dlg.setCanceledOnTouchOutside(false);
                    android.view.Window window = dlg.getWindow();
                    window.setWindowAnimations(R.style.setting_dailog_animstyle);
                    dlg.show();
                }
            }
        });
    }

    private void setupViewInLandscapeLayout (final boolean runSoftwareDecode) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        setContentView(R.layout.playback_landscape);

        if (Build.VERSION.SDK_INT < Build_VERSION_CODES_ICE_CREAM_SANDWICH) {
            bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
            getSupportActionBar().setBackgroundDrawable(bg);

            bgSplit.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
            getSupportActionBar().setSplitBackgroundDrawable(bgSplit);
        }

        getActionBar().hide();

        txtEventType = null;
        txtEventTime = null;
        txtResolution = null;
        txtFrameRate = null;
        txtBitRate = null;
        txtFrameCount = null;
        txtIncompleteFrameCount = null;

        layoutSoftMonitor = (RelativeLayout) findViewById(R.id.layoutSoftMonitor);
        layoutHardMonitor = (RelativeLayout) findViewById(R.id.layoutHardMonitor);
        progress = (ProgressBar) findViewById(R.id.progressBar);

        if (runSoftwareDecode) {
            layoutSoftMonitor.setVisibility(View.VISIBLE);
            layoutHardMonitor.setVisibility(View.GONE);
            monitor = (IMonitor) findViewById(R.id.softMonitor);
        } else {
            layoutSoftMonitor.setVisibility(View.GONE);
            layoutHardMonitor.setVisibility(View.VISIBLE);
            monitor = (IMonitor) findViewById(R.id.hardMonitor);
        }

        if (mPlaybackChannel >= 0) {
            monitor.enableDither(mCamera.mEnableDither);
            monitor.attachCamera(mCamera, mPlaybackChannel);
            monitor.setMediaCodecListener(this);
        }
    }

    private void quit () {

        if (monitor != null) {
            monitor.deattachCamera();
        }

        if (mCamera != null) {

            if (mPlaybackChannel >= 0) {

                mCamera.stopListening(mPlaybackChannel);
                mCamera.stopShow(mPlaybackChannel);
                mCamera.stop(mPlaybackChannel);
                mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL,
                        AVIOCTRLDEFs.SMsgAVIoctrlPlayRecord.parseContent(mCameraChannel, AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_STOP, 0,
                                mEvtTime2.toByteArray()));
                mPlaybackChannel = - 1;
            }
        }

        Bundle extras = new Bundle();
        extras.putInt("event_type", mEvtType);
        // extras.putLong("event_time", mEvtTime);
        extras.putByteArray("event_time2", mEvtTime2.toByteArray());
        extras.putString("event_uuid", mEvtUUID);
        extras.putBoolean("has_file", mHasFile);

        Intent intent = new Intent();
        intent.putExtras(extras);
        setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    private void reScaleMonitor () {

        if (mVideoHeight == 0 || mVideoWidth == 0 || mMiniMonitorHeight == 0 || unavailable) {
            return;
        }

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int screenWidth = size.x;
        final int screenHeight = size.y;

        final SurfaceView surfaceView;
        surfaceView = (SurfaceView) monitor;
        if (monitor != null) {
            monitor.callSurfaceChange();
        }


        if (surfaceView == null || layoutHardMonitor == null) {
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

            final int scrollViewHeight = surfaceView.getLayoutParams().height;
            handler.post(new Runnable() {
                @Override
                public void run () {
                    layoutHardMonitor.setPadding(0, (mMiniMonitorHeight - scrollViewHeight) / 2, 0, 0);
                }
            });
        }
        /**
         * landscape mode
         */
        else {

            if (surfaceView.getLayoutParams().width > screenWidth) {
                /**
                 * up down space
                 */
                mFrameMode = FrameMode.LANDSCAPE_COL_MAJOR;

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
                        layoutHardMonitor.setPadding(0, (screenHeight - statusbar - scrollViewHeight) / 2, 0, 0);
                    }
                });
            } else {
                /**
                 * left right space
                 */
                mFrameMode = FrameMode.LANDSCAPE_ROW_MAJOR;

                surfaceView.getLayoutParams().height = screenHeight;
                surfaceView.getLayoutParams().width = (int) (screenHeight * mVideoWidth / (float) mVideoHeight);

                final int scrollViewWidth = surfaceView.getLayoutParams().width;
                handler.post(new Runnable() {
                    @Override
                    public void run () {
                        int statusbar = 0;
                        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                        if (resourceId > 0) {
                            statusbar = getResources().getDimensionPixelSize(resourceId);
                        }
                        layoutHardMonitor.setPadding((screenWidth - scrollViewWidth) / 2, 0, 0, 0);
                    }
                });

            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run () {
                surfaceView.setLayoutParams(surfaceView.getLayoutParams());
            }
        });
    }


    @Override
    public void receiveFrameData (final Camera camera, int sessionChannel, Bitmap bmp) {

        if (mCamera == camera && sessionChannel == mPlaybackChannel && bmp != null) {
            mVideoWidth = bmp.getWidth();
            mVideoHeight = bmp.getHeight();
        }
    }

    @Override
    public void receiveSessionInfo (final Camera camera, int resultCode) {
    }

    @Override
    public void receiveChannelInfo (final Camera camera, int sessionChannel, int resultCode) {
    }

    @Override
    public void receiveFrameInfo (final Camera camera, int sessionChannel, long bitRate, int frameRate, int onlineNm, int frameCount,
                                  int incompleteFrameCount) {

        if (mCamera == camera && sessionChannel == mPlaybackChannel) {
            Bundle bundle = new Bundle();
            bundle.putInt("sessionChannel", sessionChannel);
            bundle.putInt("videoFPS", frameRate);
            bundle.putLong("videoBPS", bitRate);
            bundle.putInt("frameCount", frameCount);
            bundle.putInt("inCompleteFrameCount", incompleteFrameCount);

            Message msg = handler.obtainMessage();
            msg.what = STS_CHANGE_CHANNEL_STREAMINFO;
            msg.setData(bundle);
            handler.sendMessage(msg);
        }
    }

    @Override
    public void receiveIOCtrlData (final Camera camera, int sessionChannel, int avIOCtrlMsgType, byte[] data) {

        if (mCamera == camera) {
            Bundle bundle = new Bundle();
            bundle.putInt("sessionChannel", sessionChannel);
            bundle.putByteArray("data", data);

            Message msg = new Message();
            msg.what = avIOCtrlMsgType;
            msg.setData(bundle);
            handler.sendMessage(msg);
        }
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage (Message msg) {

            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");

            if (msg.what == STS_CHANGE_CHANNEL_STREAMINFO) {

                int videoFPS = bundle.getInt("videoFPS");
                long videoBPS = bundle.getLong("videoBPS");
                int frameCount = bundle.getInt("frameCount");
                int inCompleteFrameCount = bundle.getInt("inCompleteFrameCount");

                if (txtResolution != null) {
                    txtResolution.setText(String.valueOf(mVideoWidth) + "x" + String.valueOf(mVideoHeight));
                }
                if (txtFrameRate != null) {
                    txtFrameRate.setText(String.valueOf(videoFPS));
                }
                if (txtBitRate != null) {
                    txtBitRate.setText(String.valueOf(videoBPS) + "Kb");
                }
                if (txtFrameCount != null) {
                    txtFrameCount.setText(String.valueOf(frameCount));
                }
                if (txtIncompleteFrameCount != null) {
                    txtIncompleteFrameCount.setText(String.valueOf(inCompleteFrameCount));
                }

            } else if (msg.what == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL_RESP) {

                int command = Packet.byteArrayToInt_Little(data, 0);
                int result = Packet.byteArrayToInt_Little(data, 4);

                switch (command) {

                    case AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_START:

                        System.out.println("AVIOCTRL_RECORD_PLAY_START");

                        if (mMediaState == MEDIA_STATE_OPENING) {
                            if (0 <= result && result <= 31) {

                                mPlaybackChannel = result;
                                mMediaState = MEDIA_STATE_PLAYING;

                                if (mCamera != null) {
                                    mCamera.start(mPlaybackChannel, mViewAcc, mViewPwd);
                                    mCamera.startShow(mPlaybackChannel, false, unavailable);
                                    mCamera.startListening(mPlaybackChannel, mIsListening);
                                    monitor.enableDither(mCamera.mEnableDither);
                                    monitor.attachCamera(mCamera, mPlaybackChannel);
                                    monitor.setMediaCodecListener(PlaybackActivity.this);
                                }

                                if (btnPlayPause != null) {
                                    btnPlayPause.setBackgroundResource(R.drawable.btn_pause);
                                }

                            } else {
                                Toast.makeText(PlaybackActivity.this, PlaybackActivity.this.getText(R.string.tips_play_record_failed),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        break;

                    case AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_PAUSE:

                        System.out.println("AVIOCTRL_RECORD_PLAY_PAUSE");

                        if (mPlaybackChannel >= 0 && mCamera != null) {

                            if (mMediaState == MEDIA_STATE_PAUSED) {
                                mMediaState = MEDIA_STATE_PLAYING;
                                if (btnPlayPause != null) {
                                    btnPlayPause.setBackgroundResource(R.drawable.btn_pause);
                                }
                            } else if (mMediaState == MEDIA_STATE_PLAYING) {
                                mMediaState = MEDIA_STATE_PAUSED;
                                if (btnPlayPause != null) {
                                    btnPlayPause.setBackgroundResource(R.drawable.btn_play);
                                }
                            }

                            if (monitor != null) {
                                if (mMediaState == MEDIA_STATE_PAUSED) {
                                    monitor.deattachCamera();
                                } else {
                                    monitor.enableDither(mCamera.mEnableDither);
                                    monitor.attachCamera(mCamera, mPlaybackChannel);
                                }
                            }
                        }

                        break;

                    case AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_STOP:

                        System.out.println("AVIOCTRL_RECORD_PLAY_STOP");

                        if (mPlaybackChannel >= 0 && mCamera != null) {
                            mCamera.stopListening(mPlaybackChannel);
                            mCamera.stopShow(mPlaybackChannel);
                            mCamera.stop(mPlaybackChannel);
                            if (monitor != null) {
                                monitor.deattachCamera();
                            }
                        }

                        mPlaybackChannel = - 1;
                        mMediaState = MEDIA_STATE_STOPPED;

                        if (btnPlayPause != null) {
                            btnPlayPause.setBackgroundResource(R.drawable.btn_play);
                        }

                        break;

                    case AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_END:

                        System.out.println("AVIOCTRL_RECORD_PLAY_END");

                        if (mPlaybackChannel >= 0 && mCamera != null) {
                            mCamera.stopListening(mPlaybackChannel);
                            mCamera.stopShow(mPlaybackChannel);
                            mCamera.stop(mPlaybackChannel);
                            if (monitor != null) {
                                monitor.deattachCamera();
                            }

                            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL,
                                    AVIOCTRLDEFs.SMsgAVIoctrlPlayRecord.parseContent(mCameraChannel, AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_STOP, 0,
                                            mEvtTime2.toByteArray()));
                        }

                        Toast.makeText(PlaybackActivity.this, getText(R.string.tips_play_record_end), Toast.LENGTH_LONG).show();

                        if (txtFrameRate != null) {
                            txtFrameRate.setText("0");
                        }
                        if (txtBitRate != null) {
                            txtBitRate.setText("0kb");
                        }

                        mPlaybackChannel = - 1;
                        mMediaState = MEDIA_STATE_STOPPED;

                        if (btnPlayPause != null) {
                            btnPlayPause.setBackgroundResource(R.drawable.btn_play);
                        }

                        break;

                    case AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_BACKWARD:

                        break;

                    case AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_FORWARD:

                        break;
                }
            }

            super.handleMessage(msg);
        }
    };

    @Override
    public void receiveFrameDataForMediaCodec (Camera camera, int avChannel, byte[] buf, int length, int pFrmNo, byte[] pFrmInfoBuf,
                                               boolean isIframe, int codecId) {
        // TODO Auto-generated method stub
        if (monitor != null && monitor.getClass().equals(MediaCodecMonitor.class)) {
            if ((mVideoWidth != ((MediaCodecMonitor) monitor).getVideoWidth() || mVideoHeight != ((MediaCodecMonitor) monitor)
                    .getVideoHeight())) {

                mVideoWidth = ((MediaCodecMonitor) monitor).getVideoWidth();
                mVideoHeight = ((MediaCodecMonitor) monitor).getVideoHeight();

                reScaleMonitor();
            }
        }
    }

    @Override
    public void Unavailable () {
        if (unavailable) {
            return;
        }

        unavailable = true;
        if (mCodecSettings != null) {
            mCodecSettings.edit().putBoolean("unavailable", unavailable).commit();
        }

        if (monitor != null) {
            monitor.deattachCamera();
        }

        Configuration cfg = getResources().getConfiguration();

        if (cfg.orientation == Configuration.ORIENTATION_PORTRAIT) {
            runOnUiThread(new Runnable() {
                @Override
                public void run () {
                    if (mCamera != null) {

                        mCamera.stopShow(mPlaybackChannel);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run () {
                                mCamera.startShow(mPlaybackChannel, true, SOFTWARE_DECODE);
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

                        mCamera.stopShow(mPlaybackChannel);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run () {
                                mCamera.startShow(mPlaybackChannel, true, true);
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
        if (progress != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run () {
                    progress.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void zoomSurface (float scale) {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int screenWidth = size.x;
        final int screenHeight = size.y;

        runOnUiThread(new Runnable() {
            @Override
            public void run () {
                SurfaceView surfaceView = (SurfaceView) monitor;
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
                }else if(mFrameMode == FrameMode.LANDSCAPE_ROW_MAJOR) {
                    paddingLeft = (screenWidth - lp.width) / 2;
                    if(paddingLeft < 0)
                        paddingLeft = 0;
                }else{
                    paddingTop = (mMiniMonitorHeight - lp.height) / 2;
                    if(paddingTop < 0)
                        paddingTop = 0;
                }

                layoutHardMonitor.setPadding(paddingLeft, paddingTop, 0, 0);
            }
        });
    }

    @Override
    public void ok () {
        if (mFilePath.length() > 0 && mCamera != null) {

            String[] filter = mFilePath.split("\\\\");
            int path_end = filter.length;
            File rootFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/");
            if (! rootFolder.exists()) {
                try {
                    rootFolder.mkdir();
                } catch (SecurityException se) {
                }
            }
            File appFolder = new File(rootFolder.getAbsolutePath() + "/KalayCar/");
            if (! appFolder.exists()) {
                try {
                    appFolder.mkdir();
                } catch (SecurityException se) {
                }
            }

            String store_path = appFolder.getAbsolutePath() + "/" + filter[path_end - 1];

            mCamera.initLargeDownloadManager(mCameraChannel);
            mCamera.EnqueueLargeDownloaReqList(mFilePath, store_path);
            mCamera.StartMultiLargeDownload(PlaybackActivity.this);
        }
    }

    @Override
    public void cancel () {

    }

    @Override
    public void getDownload (byte[] buf, int size, boolean start, boolean end) {

    }

    @Override
    public void getFinish (String path) {
        if (path.equals(mFilePath)) {
            if (btnDownload != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run () {
                        btnDownload.setBackgroundResource(R.drawable.btn_download_h);
                        mHasFile = true;
                    }
                });
            }
        }
    }
}