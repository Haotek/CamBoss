package appteam;

import com.tutk.Logger.Glog;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;


public class ExpandAnimation extends Animation {

	private static final String TAG = "P2PDVRLive";
	
	private View mAnimatedView;
	private ViewGroup.MarginLayoutParams mViewLayoutParams;
	private int mMarginStart, mMarginEnd;
	private boolean mIsVisibleAfter = false;
	private boolean mWasEndedAlready = false;

	/**
	* Initialize the animation
	* @param view The layout we want to animate
	* @param duration The duration of the animation, in ms
	*/
	public ExpandAnimation(View view, int duration) {

		setDuration(duration);
		mAnimatedView = view;
		mViewLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
	
		// if the bottom margin is 0,
		// then after the animation will end it'll be negative, and invisible.
		mIsVisibleAfter = (mViewLayoutParams.bottomMargin == 0);
	
		mMarginStart = mViewLayoutParams.bottomMargin;
		mMarginEnd = (mMarginStart == 0 ? (0- view.getHeight()) : 0);
	
		Glog.D( TAG, "ExpandAnimation - From: " + mMarginStart + " to: " + mMarginEnd );
		view.setVisibility(View.VISIBLE);
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		super.applyTransformation(interpolatedTime, t);
	
		if (interpolatedTime < 1.0f) {
	
			// Calculating the new bottom margin, and setting it
			int idy = (int) ((mMarginEnd - mMarginStart) * interpolatedTime);
			mViewLayoutParams.bottomMargin = mMarginStart + idy;

		
			////Glog.D( TAG, "ExpandAnimation - bottomMargin: " + mMarginStart + " + " + idy + " = " + mViewLayoutParams.bottomMargin );
			// Invalidating the layout, making us seeing the changes we made
			mAnimatedView.requestLayout();
		
			// Making sure we didn't run the ending before (it happens!)
		} else if (!mWasEndedAlready) {
			mViewLayoutParams.bottomMargin = mMarginEnd;
			
			mAnimatedView.requestLayout();
		
			////Glog.D( TAG, "ExpandAnimation - bottomMargin: " + mMarginEnd + " [End]...Visibility:" + ((!mIsVisibleAfter)?"VISIBLE":"INVISIBLE") );
			if (mIsVisibleAfter) {
				mAnimatedView.setVisibility(View.GONE);
			}
			
			mWasEndedAlready = true;
		}
	}
}
