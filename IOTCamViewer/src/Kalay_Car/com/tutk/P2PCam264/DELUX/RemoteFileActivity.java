package com.tutk.P2PCam264.DELUX;

import android.app.ActionBar;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.tutk.Kalay.general.R;

/**
 * Created by James Huang on 2015/4/3.
 */
public class RemoteFileActivity extends SherlockActivity implements Custom_OkCancle_Dialog.OkCancelDialogListener {

    private PhotoListFragment PHOTO_fragment;
    private EventListFragment EVENT_fragment;
    private MyMode mMode = MyMode.PHOTO;

    private Button btnPhoto;
    private Button btnVideo;
    public Button btnEdit;

    private boolean photoList = false;
    private boolean eventList = false;
    private boolean mIsEdit = false;

    public enum MyMode {
        PHOTO, EVENT
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.titlebar);
        TextView tv = (TextView) this.findViewById(R.id.bar_text);
        btnEdit = (Button) findViewById(R.id.bar_right_btn);
        RelativeLayout btn_change_mode = (RelativeLayout) findViewById(R.id.bar_gallery);
        btnPhoto = (Button) findViewById(R.id.bar_btn_photo);
        btnVideo = (Button) findViewById(R.id.bar_btn_video);

        tv.setVisibility(View.GONE);
        btnEdit.setText(R.string.txt_edit);
        btnEdit.setTextColor(Color.WHITE);
        btnEdit.setVisibility(View.VISIBLE);
        btnEdit.setEnabled(false);
        btn_change_mode.setVisibility(View.VISIBLE);
        btnPhoto.setBackgroundResource(R.drawable.btn_tabl_h);
        btnPhoto.setTextColor(Color.BLACK);

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                switch (mMode) {
                    case PHOTO:
                        PHOTO_fragment.showHideBar();
                        mIsEdit = ! mIsEdit;
                        if (mIsEdit) {
                            btnEdit.setText(R.string.cancel);
                        } else {
                            btnEdit.setText(R.string.txt_edit);
                        }
                        break;

                    case EVENT:
                        EVENT_fragment.showHideBar();
                        mIsEdit = ! mIsEdit;
                        if (mIsEdit) {
                            btnEdit.setText(R.string.cancel);
                        } else {
                            btnEdit.setText(R.string.txt_edit);
                        }
                        break;
                }
            }
        });
        btnPhoto.setOnClickListener(ClickTab);
        btnVideo.setOnClickListener(ClickTab);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.remote_file_activity);

        EVENT_fragment = new EventListFragment(this);
        PHOTO_fragment = new PhotoListFragment(this);
        getFragmentManager().beginTransaction().add(R.id.layout_container, EVENT_fragment).add(R.id.layout_container,
                PHOTO_fragment).hide(EVENT_fragment).commit();

    }

    @Override
    protected void onResume () {
        super.onResume();
        Custom_OkCancle_Dialog.SetDialogListener(this);
    }

    private View.OnClickListener ClickTab = new View.OnClickListener() {
        @Override
        public void onClick (View v) {
            switch (v.getId()) {
                case R.id.bar_btn_photo:
                    if (mMode == MyMode.EVENT) {
                        getFragmentManager().beginTransaction().hide(EVENT_fragment).show(PHOTO_fragment).commit();
                        btnVideo.setBackgroundResource(R.drawable.btn_photo);
                        try {
                            btnVideo.setTextColor(ColorStateList.createFromXml(getResources(), getResources().getXml(R.drawable.txt_color_gallery)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        btnPhoto.setBackgroundResource(R.drawable.btn_tabl_h);
                        btnPhoto.setTextColor(Color.BLACK);

                        if (photoList) {
                            btnEdit.setEnabled(true);
                        }

                        mMode = MyMode.PHOTO;

                        if (mIsEdit) {
                            EVENT_fragment.showHideBar();
                            mIsEdit = ! mIsEdit;
                            if (mIsEdit) {
                                btnEdit.setText(R.string.cancel);
                            } else {
                                btnEdit.setText(R.string.txt_edit);
                            }
                        }
                    }
                    break;

                case R.id.bar_btn_video:
                    if (mMode == MyMode.PHOTO) {
                        getFragmentManager().beginTransaction().hide(PHOTO_fragment).show(EVENT_fragment).commit();
                        btnPhoto.setBackgroundResource(R.drawable.btn_photo);
                        try {
                            btnPhoto.setTextColor(ColorStateList.createFromXml(getResources(), getResources().getXml(R.drawable.txt_color_gallery)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        btnVideo.setBackgroundResource(R.drawable.btn_tabr_h);
                        btnVideo.setTextColor(Color.BLACK);

                        if (eventList) {
                            btnEdit.setEnabled(true);
                        }

                        mMode = MyMode.EVENT;

                        if (mIsEdit) {
                            PHOTO_fragment.showHideBar();
                            mIsEdit = ! mIsEdit;
                            if (mIsEdit) {
                                btnEdit.setText(R.string.cancel);
                            } else {
                                btnEdit.setText(R.string.txt_edit);
                            }
                        }
                    }
                    break;
            }
        }
    };

    public void ListFinished (MyMode mode) {
        switch (mode) {
            case PHOTO:
                photoList = true;
                if (mMode == MyMode.PHOTO) {
                    btnEdit.setEnabled(true);
                }
                break;

            case EVENT:
                eventList = true;
                if (mMode == MyMode.EVENT) {
                    btnEdit.setEnabled(true);
                }
                break;
        }
    }

    public void cancleEdit () {
        mIsEdit = false;
        btnEdit.setText(R.string.txt_edit);
    }

    public boolean isOnFocus (MyMode mode) {
        if (mode == mMode) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onKeyDown (int keyCode, KeyEvent event) {

        if (EVENT_fragment.onKeyDown(keyCode, event)) {
            return super.onKeyDown(keyCode, event);
        } else {
            return false;
        }
    }

    @Override
    public void ok () {
        switch (mMode) {
            case PHOTO:
                PHOTO_fragment.DialogOK();
                break;

            case EVENT:
                EVENT_fragment.DialogOK();
                break;
        }
    }

    @Override
    public void cancel () {

    }
}
