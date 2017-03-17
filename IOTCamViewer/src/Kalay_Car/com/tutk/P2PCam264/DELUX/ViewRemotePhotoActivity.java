package com.tutk.P2PCam264.DELUX;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.IRegisterIOTCListener;
import com.tutk.IOTC.LargeDownloadListener;
import com.tutk.IOTC.Packet;
import com.tutk.Kalay.general.R;
import com.tutk.P2PCam264.MyCamera;
import com.tutk.customized.command.CustomCommand;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by James Huang on 2015/4/24.
 */
public class ViewRemotePhotoActivity extends Activity implements IRegisterIOTCListener, LargeDownloadListener,
        Custom_OkCancle_Dialog.OkCancelDialogListener, GestureDetector.OnGestureListener, View.OnTouchListener {

    private static final int FLING_MIN_DISTANCE = 20;
    private static final int FLING_MIN_VELOCITY = 0;

    private MyCamera mCamera;
    private ByteArrayOutputStream bos;
    private GestureDetector mGestureDetector;

    private ImageView img;
    private ProgressBar progress;
    private ImageButton btnDownload;
    private ImageButton btnRemove;

    private List<PhotoListFragment.PhotoInfo> photo_list = new ArrayList<PhotoListFragment.PhotoInfo>();
    private String mDevUUID;
    private int mCurrentPos;
    private int mCameraChannel;
    private byte[] buff;

    private MODE mMode;

    private enum MODE {
        Download, Remove
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        mDevUUID = bundle.getString("dev_uuid");
        mCurrentPos = bundle.getInt("pos");
        mCameraChannel = bundle.getInt("channel");
        photo_list = PhotoListFragment.photo_list;


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.titlebar);
        actionBar.show();
        TextView tvTitle = (TextView) findViewById(R.id.bar_text);
        TextView tvTitleSub = (TextView) findViewById(R.id.bar_text_sub);
        AVIOCTRLDEFs.STimeDay mTime = photo_list.get(mCurrentPos).EventTime;
        tvTitle.setText(mTime.year + "/" + mTime.month + "/" + mTime.day);
        tvTitleSub.setText(mTime.hour + ":" + mTime.minute);
        tvTitleSub.setVisibility(View.VISIBLE);

        for (MyCamera camera : MultiViewActivity.CameraList) {

            if (mDevUUID.equalsIgnoreCase(camera.getUUID())) {
                mCamera = camera;
                mCamera.registerIOTCListener(this);
                mCamera.initLargeDownloadManager(mCameraChannel);

                break;
            }
        }

        setContentView(R.layout.view_remote_photo);

        img = (ImageView) findViewById(R.id.img);
        progress = (ProgressBar) findViewById(R.id.progress);
        btnDownload = (ImageButton) findViewById(R.id.btnDownload);
        btnRemove = (ImageButton) findViewById(R.id.btnRemove);

        img.setOnTouchListener(this);
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                mMode = MODE.Download;
                if (! photo_list.get(mCurrentPos).mHasFile) {
                    Custom_OkCancle_Dialog dlg = new Custom_OkCancle_Dialog(ViewRemotePhotoActivity.this,
                            getText(R.string.tips_download_photo).toString());

                    dlg.setCanceledOnTouchOutside(false);
                    Window window = dlg.getWindow();
                    window.setWindowAnimations(R.style.setting_dailog_animstyle);
                    dlg.show();
                }
            }
        });
        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                mMode = MODE.Remove;

                Custom_OkCancle_Dialog dlg = new Custom_OkCancle_Dialog(ViewRemotePhotoActivity.this,
                        getText(R.string.dlgAreYouSureToDeleteThisSnapshot).toString());
                dlg.setCanceledOnTouchOutside(false);
                Window window = dlg.getWindow();
                window.setWindowAnimations(R.style.setting_dailog_animstyle);
                dlg.show();
            }
        });

        getPhoto(photo_list.get(mCurrentPos).path);
        Custom_OkCancle_Dialog.SetDialogListener(this);
        mGestureDetector = new GestureDetector(this);
    }

    private void getPhoto (String path) {

        btnDownload.setEnabled(false);
        btnRemove.setEnabled(false);
        img.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        if (photo_list.get(mCurrentPos).mHasFile) {
            btnDownload.setBackgroundResource(R.drawable.btn_download_h);
        } else {
            btnDownload.setBackgroundResource(R.drawable.btn_download);
        }

        if (mCamera != null) {
            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, CustomCommand.IOTYPE_USER_IPCAM_DOWNLOAD_FILE_REQ,
                    CustomCommand.SMsgAVIoctrlDownloadReq.parseContent(mCameraChannel, path, CustomCommand.AVIOCTRL_VIEW_FILE));
        }
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();

        if (mCamera != null) {
            mCamera.unregisterIOTCListener(this);
            mCamera.unregisterLargeDownload(this);
        }
    }

    @Override
    public void receiveFrameData (Camera camera, int avChannel, Bitmap bmp) {

    }

    @Override
    public void receiveFrameDataForMediaCodec (Camera camera, int avChannel, byte[] buf, int length, int pFrmNo, byte[] pFrmInfoBuf,
                                               boolean isIframe, int codecId) {

    }

    @Override
    public void receiveFrameInfo (Camera camera, int avChannel, long bitRate, int frameRate, int onlineNm, int frameCount, int incompleteFrameCount) {

    }

    @Override
    public void receiveSessionInfo (Camera camera, int resultCode) {

    }

    @Override
    public void receiveChannelInfo (Camera camera, int avChannel, int resultCode) {

    }

    @Override
    public void receiveIOCtrlData (Camera camera, int avChannel, int avIOCtrlMsgType, byte[] data) {
        if (mCamera == camera) {
            switch (avIOCtrlMsgType) {
                case CustomCommand.IOTYPE_USER_IPCAM_DOWNLOAD_FILE_RESP:
                    int ch = Packet.byteArrayToInt_Little(data, 4);

                    if (data[8] == CustomCommand.AVIOCTRL_VIEW_FILE) {
                        mCamera.StartLargeDownload(ch, ViewRemotePhotoActivity.this);
                    }
                    break;
            }
        }
    }

    @Override
    public void getDownload (byte[] buf, int size, boolean start, boolean end) {
        if (start) {
            bos = new ByteArrayOutputStream();
            String path = new String(buf, 0, 72);

            bos.write(buf, 72, size - 72);
        } else {
            bos.write(buf, 0, size);
        }

        if (end) {

            buff = bos.toByteArray();
            final Bitmap bmp = BitmapFactory.decodeByteArray(buff, 0, buff.length);

            runOnUiThread(new Runnable() {
                @Override
                public void run () {
                    img.setImageBitmap(bmp);
                    progress.setVisibility(View.GONE);
                    img.setVisibility(View.VISIBLE);
                    Animation anim = AnimationUtils.loadAnimation(ViewRemotePhotoActivity.this, R.anim.fade_in);
                    img.startAnimation(anim);

                    btnDownload.setEnabled(true);
                    btnRemove.setEnabled(true);
                }
            });

            try {
                bos.flush();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void getFinish (String path) {

    }

    @Override
    public void ok () {
        switch (mMode) {
            case Download:
                String[] filter = photo_list.get(mCurrentPos).path.split("\\\\");
                int end = filter.length;
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


                File file = new File(appFolder.getAbsolutePath() + "/" + filter[end - 1]);
                try {
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                    bos.write(buff);
                    photo_list.get(mCurrentPos).setHasFile(true);
                    btnDownload.setBackgroundResource(R.drawable.btn_download_h);
                    bos.flush();
                    bos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case Remove:
                String req = "custom=1&cmd=4003&str=" + photo_list.get(mCurrentPos).path;
                mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, CustomCommand.IOTYPE_USER_WIFICMD_REQ,
                        CustomCommand.SMsgAVIoctrlWifiCmdReq.parseContent(mCameraChannel, 0, 0, 4003, 1, 0, req.length(), req));

                int remove_pos = mCurrentPos;

                if (photo_list.size() > 1) {
                    if (mCurrentPos == (photo_list.size() - 1)) {
                        mCurrentPos = 0;
                        getPhoto(photo_list.get(mCurrentPos).path);
                    } else {
                        mCurrentPos++;
                        getPhoto(photo_list.get(mCurrentPos).path);
                    }

                    photo_list.remove(remove_pos);

                } else {
                    photo_list.remove(remove_pos);
                    finish();
                    overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
                }


                break;
        }
    }

    @Override
    public void cancel () {

    }

    @Override
    public boolean onDown (MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress (MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp (MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll (MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress (MotionEvent e) {

    }

    @Override
    public boolean onFling (MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        int mSize = photo_list.size();

        if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
            // Fling left
            if (mCurrentPos == (mSize - 1)) {
                mCurrentPos = 0;
                getPhoto(photo_list.get(mCurrentPos).path);
            } else {
                mCurrentPos++;
                getPhoto(photo_list.get(mCurrentPos).path);
            }
        } else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
            // Fling right
            if (mCurrentPos == 0) {
                mCurrentPos = mSize - 1;
                getPhoto(photo_list.get(mCurrentPos).path);
            } else {
                mCurrentPos--;
                getPhoto(photo_list.get(mCurrentPos).path);
            }
        }
        return false;
    }

    @Override
    public boolean onTouch (View v, MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return true;
    }
}
