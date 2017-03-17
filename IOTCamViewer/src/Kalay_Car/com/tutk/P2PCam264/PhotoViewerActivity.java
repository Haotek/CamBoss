package com.tutk.P2PCam264;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.tutk.P2PCam264.DELUX.Custom_popupWindow;
import com.tutk.Kalay.general.R;

import java.util.HashMap;
import java.util.List;

public class PhotoViewerActivity extends Activity implements GestureDetector.OnGestureListener, OnTouchListener, Custom_popupWindow.On_PopupWindow_click_Listener {

    private static final int FLING_MIN_DISTANCE = 20;
    private static final int FLING_MIN_VELOCITY = 0;
    public final static int DELETE = 0;
    public final static int SHARE = 1;
    private GestureDetector mGestureDetector;
    private String mFileName;
    private List<String> IMAGE_FILES;
    private int mPosition;
    private int mSize;
    private Bitmap bm;
    private ImageView iv;

    @Override
    public void onCreate (Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.titlebar);
        TextView tv = (TextView) this.findViewById(R.id.bar_text);
        tv.setText(getText(R.string.ctxViewSnapshot));
        ImageButton btnShare = (ImageButton) this.findViewById(R.id.bar_right_imgBtn);
        btnShare.setBackgroundResource(R.drawable.btn_share);
//        btnShare.setVisibility(View.VISIBLE);
        System.gc();
        Intent i = getIntent();
        Bundle extras = i.getExtras();
        //BitmapFactory.Options bfo = new BitmapFactory.Options();
        //bfo.inSampleSize = 2;
        mFileName = extras.getString("filename");
        IMAGE_FILES = extras.getStringArrayList("files");
        mPosition = extras.getInt("pos");
        mSize = IMAGE_FILES.size();
        iv = new ImageView(getApplicationContext());
        mGestureDetector = new GestureDetector(this);
        iv.setOnTouchListener(this);
        bm = BitmapFactory.decodeFile(mFileName);// ,bfo);
        iv.setImageBitmap(bm);
        setContentView(iv);

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                PopupWindow popupWindow = null;
                ViewGroup layout = null;
                layout = (LinearLayout) LayoutInflater.from(PhotoViewerActivity.this).inflate(R.layout.popup_liveview_ch, null);
                popupWindow = Custom_popupWindow.Menu_PopupWindow_newInstance(PhotoViewerActivity.this, layout, PhotoViewerActivity.this,
                        Custom_popupWindow.PHOTO, 0);
                Configuration cfg = getResources().getConfiguration();

                popupWindow.showAsDropDown(v, 0, 0);

            }
        });
    }

    @Override
    public boolean onTouch (View v, MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onDown (MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onShowPress (MotionEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onSingleTapUp (MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onScroll (MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onLongPress (MotionEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onFling (MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
            // Fling left
            if (mPosition == (mSize - 1)) {
                mPosition = 0;
                mFileName = IMAGE_FILES.get(mPosition);
                bm = BitmapFactory.decodeFile(mFileName);
                iv.setImageBitmap(bm);
                setContentView(iv);
            } else {
                mPosition += 1;
                mFileName = IMAGE_FILES.get(mPosition);
                bm = BitmapFactory.decodeFile(mFileName);
                iv.setImageBitmap(bm);
                setContentView(iv);
            }
        } else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
            // Fling right
            if (mPosition == 0) {
                mPosition = mSize - 1;
                mFileName = IMAGE_FILES.get(mPosition);
                bm = BitmapFactory.decodeFile(mFileName);
                iv.setImageBitmap(bm);
                setContentView(iv);
            } else {
                mPosition -= 1;
                mFileName = IMAGE_FILES.get(mPosition);
                bm = BitmapFactory.decodeFile(mFileName);
                iv.setImageBitmap(bm);
                setContentView(iv);
            }
        }
        return false;
    }

    @Override
    public void btn_onDropbox_click (PopupWindow PopupWindow) {

    }

    @Override
    public void btn_infomation_click (PopupWindow PopupWindow) {

    }

    @Override
    public void btn_log_in_out_click (PopupWindow PopupWindow) {

    }

    @Override
    public void btn_change_ch (int channel) {

    }

    @Override
    public void btn_change_quality (int level) {

    }

    @Override
    public void btn_change_env (int mode) {

    }

    @Override
    public void btn_add_monitor (HashMap<Integer, Boolean> AddMap, int DeviceNo) {

    }

    @Override
    public void btn_photo (int value) {

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
