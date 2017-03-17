package com.tutk.P2PCam264.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.tutk.Kalay.general.R;

/**
 * Created by James Huang on 2015/3/27.
 */
public class mySwitchButton extends View {

    public final static int MODE_TIME_LAPSE = 0;
    public final static int MODE_LOCAL_RECORDING = 1;
    private final int TOUCH_MODE_DOWN = 0;
    private final int TOUCH_MODE_DRAG = 1;

    private Context mContext;
    private Drawable mTrack;
    private Drawable mThumbTime;
    private Drawable mThumbLocal;
    private Drawable mThumbTime_start;
    private Drawable mThumbLocal_start;
    private SwitchListener mListener;
    private StateListener mStateListener;

    private float track_width;
    private float track_height;
    private float thumb_width;
    private float thumb_height;
    private float switch_offset;
    private float touch_x;
    private int track_x;
    private int track_y;
    private int thumb_x;
    private int thumb_y;
    private int thumb_pos_x;
    private int thumb_pos_y;
    private int move_limit;
    private int thumb_midline;

    private int mMode = MODE_TIME_LAPSE;
    private int TouchkMode;
    private boolean mIsClick = false;
    private boolean mEnable = true;

    public mySwitchButton (Context context) {
        super(context);
        mContext = context;
        mTrack = mContext.getResources().getDrawable(R.drawable.btn_switchcam_bg);
    }

    public mySwitchButton (Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mTrack = mContext.getResources().getDrawable(R.drawable.btn_switchcam_bg);
        mThumbTime = mContext.getResources().getDrawable(R.drawable.btn_switchcam_timelapse_n);
        mThumbLocal = mContext.getResources().getDrawable(R.drawable.btn_switchcam_video_n);
        mThumbTime_start = mContext.getResources().getDrawable(R.drawable.btn_switchcam_timelapse_h);
        mThumbLocal_start = mContext.getResources().getDrawable(R.drawable.btn_switchcam_video_h);

        track_width = mContext.getResources().getDimension(R.dimen.track_width);
        track_height = mContext.getResources().getDimension(R.dimen.track_height);
        thumb_width = mContext.getResources().getDimension(R.dimen.thumb_width);
        thumb_height = mContext.getResources().getDimension(R.dimen.thumb_height);
        switch_offset = mContext.getResources().getDimension(R.dimen.switch_offset);
        track_x = (int) track_width;
        track_y = (int) track_height;
        thumb_x = (int) thumb_width;
        thumb_y = (int) thumb_height;
        thumb_pos_y = (int) switch_offset;
        thumb_pos_x = thumb_pos_y;
        move_limit =(int) (track_width - thumb_width - switch_offset);
        thumb_midline = (move_limit - thumb_pos_y) / 2;
    }

    @Override
    protected void onDraw (Canvas canvas) {
        super.onDraw(canvas);

        mTrack.setBounds(0, 0, track_x, track_y);
        mTrack.draw(canvas);

        if(thumb_pos_x < thumb_pos_y){
            thumb_pos_x = thumb_pos_y;
        }else if(thumb_pos_x > move_limit){
            thumb_pos_x = move_limit;
        }
        mThumbTime.setBounds(thumb_pos_x, thumb_pos_y, thumb_pos_x + thumb_x, thumb_pos_y + thumb_y);
        mThumbLocal.setBounds(thumb_pos_x, thumb_pos_y, thumb_pos_x + thumb_x, thumb_pos_y + thumb_y);
        mThumbTime_start.setBounds(thumb_pos_x, thumb_pos_y, thumb_pos_x + thumb_x, thumb_pos_y + thumb_y);
        mThumbLocal_start.setBounds(thumb_pos_x, thumb_pos_y, thumb_pos_x + thumb_x, thumb_pos_y + thumb_y);
        if(mMode == MODE_TIME_LAPSE) {
            if(mIsClick){
                mThumbTime_start.draw(canvas);
            }else{
                mThumbTime.draw(canvas);
            }
        }else{
            if(mIsClick){
                mThumbLocal_start.draw(canvas);
            }else{
                mThumbLocal.draw(canvas);
            }
        }
    }

    @Override
    public boolean onTouchEvent (MotionEvent event) {

        if(!mEnable){
            return false;
        }

        final int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                touch_x = event.getX();
                TouchkMode = TOUCH_MODE_DOWN;
                return true;
            }

            case MotionEvent.ACTION_MOVE: {
                float x = event.getX();
                if( Math.abs(x - touch_x) > switch_offset){
                    TouchkMode = TOUCH_MODE_DRAG;
                    if(!mIsClick) {
                        thumb_pos_x = (int) (thumb_pos_x + (x - touch_x));
                        invalidate();
                    }
                }
                return true;
            }

            case MotionEvent.ACTION_UP: {
                if(thumb_pos_x >= thumb_midline){
                    thumb_pos_x = move_limit;
                    if(mStateListener != null && mMode == MODE_TIME_LAPSE){
                        mStateListener.stateChange(MODE_LOCAL_RECORDING);
                    }
                    mMode = MODE_LOCAL_RECORDING;
                }else{
                    thumb_pos_x = thumb_pos_y;
                    if(mStateListener != null && mMode == MODE_LOCAL_RECORDING){
                        mStateListener.stateChange(MODE_TIME_LAPSE);
                    }
                    mMode = MODE_TIME_LAPSE;
                }
                invalidate();

                if(TouchkMode == TOUCH_MODE_DOWN) {
                    mIsClick = !mIsClick;

                    if(mListener != null) {
                        mListener.click();
                    }
                }

                break;
            }
        }
        return super.onTouchEvent(event);
    }

    public int getMode(){
        return mMode;
    }

    public void setOnClickListener(SwitchListener listener){
        mListener = listener;
    }

    public interface SwitchListener {
        public void click();
    }

    public void setStateChangeListener(StateListener listener){
        mStateListener = listener;
    }

    public interface StateListener {
        public void stateChange(int mode);
    }

    public void stopRecord(){
        mIsClick = false;
        invalidate();
    }

    public void isRecording(){
        mIsClick = true;
        mMode = MODE_LOCAL_RECORDING;
        thumb_pos_x = move_limit;
        invalidate();
    }

    public void isLapsing(){
        mIsClick = true;
        mMode = MODE_TIME_LAPSE;
        thumb_pos_x = thumb_pos_y;
        invalidate();
    }

    public void setEnable(boolean value){
        mEnable = value;
    }
}
