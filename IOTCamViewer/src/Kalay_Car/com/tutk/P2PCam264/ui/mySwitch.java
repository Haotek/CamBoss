package com.tutk.P2PCam264.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.tutk.Kalay.general.R;

/**
 * Created by James Huang on 2015/4/2.
 */
public class mySwitch extends View {

    public final static int MODE_OFF = 0;
    public final static int MODE_ON = 1;
    private final int TOUCH_MODE_DOWN = 0;
    private final int TOUCH_MODE_DRAG = 1;

    private Context mContext;
    private Drawable mTrackOn;
    private Drawable mTrackOff;
    private Drawable mThumbOn;
    private Drawable mThumbOff;
//    private SwitchListener mListener;
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

    private int mMode = MODE_OFF;
    private int TouchkMode;
    private boolean mEnable = true;

    public mySwitch (Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mTrackOn = mContext.getResources().getDrawable(R.drawable.bg_switchon);
        mTrackOff = mContext.getResources().getDrawable(R.drawable.bg_switchoff);
        mThumbOn = mContext.getResources().getDrawable(R.drawable.btn_switch);
        mThumbOff = mContext.getResources().getDrawable(R.drawable.btn_switch);

        track_width = mContext.getResources().getDimension(R.dimen.sw_track_width);
        track_height = mContext.getResources().getDimension(R.dimen.sw_track_height);
        thumb_width = mContext.getResources().getDimension(R.dimen.sw_thumb_width);
        thumb_height = mContext.getResources().getDimension(R.dimen.sw_thumb_height);
        switch_offset = mContext.getResources().getDimension(R.dimen.switch_offset_g);
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

        mTrackOn.setBounds(0, 0, track_x, track_y);
        mTrackOff.setBounds(0, 0, track_x, track_y);
        if(mMode == MODE_OFF) {
            mTrackOff.draw(canvas);
        }else{
            mTrackOn.draw(canvas);
        }

        if(thumb_pos_x > move_limit || mMode == MODE_ON){
            thumb_pos_x = move_limit;
        }else if(thumb_pos_x < thumb_pos_y){
            thumb_pos_x = thumb_pos_y;
        }
        mThumbOn.setBounds(thumb_pos_x, thumb_pos_y, thumb_pos_x + thumb_x, thumb_pos_y + thumb_y);
        mThumbOff.setBounds(thumb_pos_x, thumb_pos_y, thumb_pos_x + thumb_x, thumb_pos_y + thumb_y);
        if(mMode == MODE_OFF) {
            mThumbOff.draw(canvas);
        }else{
            mThumbOn.draw(canvas);
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
                    thumb_pos_x = (int) (thumb_pos_x + (x - touch_x));
                    invalidate();

                }
                return true;
            }

            case MotionEvent.ACTION_UP: {

                if(TouchkMode == TOUCH_MODE_DOWN) {

                    if( mMode == MODE_ON){
                        thumb_pos_x = thumb_pos_y;
                        mStateListener.stateChange(MODE_OFF);
                        mMode = MODE_OFF;
                    }else{
                        thumb_pos_x = move_limit;
                        mStateListener.stateChange(MODE_ON);
                        mMode = MODE_ON;
                    }
                    invalidate();
//                    if(mListener != null) {
//                        mListener.click();
//                    }
                }else{
                    if(thumb_pos_x >= thumb_midline){
                        thumb_pos_x = move_limit;
                        if(mStateListener != null && mMode == MODE_OFF){
                            mStateListener.stateChange(MODE_ON);
                        }
                        mMode = MODE_ON;
                    }else{
                        thumb_pos_x = thumb_pos_y;
                        if(mStateListener != null && mMode == MODE_ON){
                            mStateListener.stateChange(MODE_OFF);
                        }
                        mMode = MODE_OFF;
                    }
                    invalidate();
                }

                break;
            }
        }
        return super.onTouchEvent(event);
    }

    public interface StateListener {
        public void stateChange(int mode);
    }

    public void setStateChangeListener(StateListener listener){
        mStateListener = listener;
    }

    public int getMode(){
        return mMode;
    }

    public void setCheck(boolean check){
        if(check){
            mMode = MODE_ON;
        }else {
            mMode = MODE_OFF;
        }
        invalidate();
    }

}
