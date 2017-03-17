package com.tutk.P2PCam264;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tutk.Kalay.general.R;
/**
 *
 * 
 * @author guolin
 * 
 */
public class RefreshableView extends LinearLayout implements OnTouchListener {

	public static final int STATUS_PULL_TO_REFRESH = 0;

	public static final int STATUS_RELEASE_TO_REFRESH = 1;

	public static final int STATUS_REFRESHING = 2;

	public static final int STATUS_REFRESH_FINISHED = 3;

	public static final int SCROLL_SPEED = -20;

	public static final long ONE_MINUTE = 60 * 1000;

	public static final long ONE_HOUR = 60 * ONE_MINUTE;

	public static final long ONE_DAY = 24 * ONE_HOUR;

	public static final long ONE_MONTH = 30 * ONE_DAY;

	public static final long ONE_YEAR = 12 * ONE_MONTH;

	private static final String UPDATED_AT = "updated_at";

	private PullToRefreshListener mListener;

	private SharedPreferences preferences;

	private View header;

	private ListView listView;

	private ProgressBar progressBar;

	private ImageView arrow;

	private TextView description;

	//private TextView updateAt;

	private MarginLayoutParams headerLayoutParams;

	//private long lastUpdateTime;

	private int mId = -1;

	private int hideHeaderHeight;

	private int currentStatus = STATUS_REFRESH_FINISHED;;

	private int lastStatus = currentStatus;

	private float yDown;

	private int touchSlop;

	private boolean loadOnce;

	private boolean ableToPull;
	
	private Context mcontext;

	public RefreshableView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mcontext = context;
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		header = LayoutInflater.from(context).inflate(R.layout.pull_to_refresh, null, true);
		progressBar = (ProgressBar) header.findViewById(R.id.progress_bar);
		arrow = (ImageView) header.findViewById(R.id.arrow);
		description = (TextView) header.findViewById(R.id.description);
		//updateAt = (TextView) header.findViewById(R.id.updated_at);
		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		//refreshUpdatedAtValue();
		setOrientation(VERTICAL);
		addView(header, 0);

        hideHeaderHeight = (int) -(getResources().getDimension(R.dimen.refresh_h));
        headerLayoutParams = (MarginLayoutParams) header.getLayoutParams();
		headerLayoutParams.topMargin = hideHeaderHeight;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (changed && !loadOnce) {
//			hideHeaderHeight = -header.getHeight();
//			headerLayoutParams = (MarginLayoutParams) header.getLayoutParams();
//			headerLayoutParams.topMargin = hideHeaderHeight;
			listView = (ListView) findViewById(R.id.lstEventList);
			listView.setOnTouchListener(this);
			loadOnce = true;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		setIsAbleToPull(event);
		if (ableToPull) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				yDown = event.getRawY();
				break;
			case MotionEvent.ACTION_MOVE:
				float yMove = event.getRawY();
				int distance = (int) (yMove - yDown);
				if (distance <= 0 && headerLayoutParams.topMargin <= hideHeaderHeight) {
					return false;
				}
				if (distance < touchSlop) {
					return false;
				}
				if (currentStatus != STATUS_REFRESHING) {
					if (headerLayoutParams.topMargin > 0) {
						currentStatus = STATUS_RELEASE_TO_REFRESH;
					} else {
						currentStatus = STATUS_PULL_TO_REFRESH;
					}
					headerLayoutParams.topMargin = (distance / 2) + hideHeaderHeight;
					header.setLayoutParams(headerLayoutParams);
				}
				break;
			case MotionEvent.ACTION_UP:
			default:
				if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
					Refreshing_Thread Refreshing_Thread =new Refreshing_Thread();
					Refreshing_Thread.start();
					//new RefreshingTask().execute();
				} else if (currentStatus == STATUS_PULL_TO_REFRESH) {
					HideHeader_Thread HideHeader_Thread =new HideHeader_Thread();
					HideHeader_Thread.start();
					//new HideHeaderTask().execute();
				}
				break;
			}
			//��s�ثe���A
			if (currentStatus == STATUS_PULL_TO_REFRESH
					|| currentStatus == STATUS_RELEASE_TO_REFRESH) {
				updateHeaderView();
				listView.setPressed(false);
				listView.setFocusable(false);
				listView.setFocusableInTouchMode(false);
				lastStatus = currentStatus;
				return true;
			}
		}
		return false;
	}
	public void setOnRefreshListener(PullToRefreshListener listener, int id) {
		mListener = listener;
		mId = id;
	}


	public void finishRefreshing() {
		currentStatus = STATUS_REFRESH_FINISHED;
		preferences.edit().putLong(UPDATED_AT + mId, System.currentTimeMillis()).commit();
		//new HideHeaderTask().execute();
		HideHeader_Thread HideHeader_Thread =new HideHeader_Thread();
		HideHeader_Thread.start();
	}

	private void setIsAbleToPull(MotionEvent event) {
		View firstChild = listView.getChildAt(0);
		if (firstChild != null) {
			int firstVisiblePos = listView.getFirstVisiblePosition();
			if (firstVisiblePos == 0 && firstChild.getTop() == 0) {
				if (!ableToPull) {
					yDown = event.getRawY();
				}
				ableToPull = true;
			} else {
				if (headerLayoutParams.topMargin != hideHeaderHeight) {
					headerLayoutParams.topMargin = hideHeaderHeight;
					header.setLayoutParams(headerLayoutParams);
				}
				ableToPull = false;
			}
		} else {
			ableToPull = true;
		}
	}
	private void updateHeaderView() {
		if (lastStatus != currentStatus) {
			if (currentStatus == STATUS_PULL_TO_REFRESH) {
				description.setText(getResources().getString(R.string.pull_to_refresh));
				arrow.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
				rotateArrow();
			} else if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
				description.setText(getResources().getString(R.string.release_to_refresh));
				arrow.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
				rotateArrow();
			} else if (currentStatus == STATUS_REFRESHING) {
				description.setText(getResources().getString(R.string.refreshing));
				//progressBar.setVisibility(View.VISIBLE);
				arrow.clearAnimation();
				arrow.setVisibility(View.GONE);
			}
			//refreshUpdatedAtValue();
		}
	}
	private void rotateArrow() {
		float pivotX = arrow.getWidth() / 2f;
		float pivotY = arrow.getHeight() / 2f;
		float fromDegrees = 0f;
		float toDegrees = 0f;
		if (currentStatus == STATUS_PULL_TO_REFRESH) {
			fromDegrees = 180f;
			toDegrees = 360f;
		} else if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
			fromDegrees = 0f;
			toDegrees = 180f;
		}
		RotateAnimation animation = new RotateAnimation(fromDegrees, toDegrees, pivotX, pivotY);
		animation.setDuration(100);
		animation.setFillAfter(true);
		arrow.startAnimation(animation);
	}

	private class Refreshing_Thread extends Thread 
	{
		@Override
		public void run() 
		{
			// TODO Auto-generated method stub
			super.run();
			int topMargin = headerLayoutParams.topMargin;
			while (true) {
				topMargin = topMargin + SCROLL_SPEED;
				if (topMargin <= 0) {
					topMargin = 0;
					break;
				}
				//publishProgress(topMargin);
				Message msg = Message.obtain(handler);
				msg.what = 0;
				msg.obj = topMargin;
				handler.sendMessage(msg);
				try {
					sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			currentStatus = STATUS_REFRESHING;
			Message msg = Message.obtain(handler);
			msg.what = 2;
			msg.obj = topMargin;
			handler.sendMessage(msg);
			//if (mListener != null) {
			//	mListener.onRefresh();
			//}
		}

	}
	private class HideHeader_Thread extends Thread 
	{
		@Override
		public void run() 
		{
			// TODO Auto-generated method stub
			super.run();
			int topMargin = headerLayoutParams.topMargin;
			while (true) {
				topMargin = topMargin + SCROLL_SPEED;
				if (topMargin <= hideHeaderHeight) {
					topMargin = hideHeaderHeight;
					break;
				}
				//publishProgress(topMargin);
				Message msg = Message.obtain(handler);
				msg.what = 0;
				msg.obj = topMargin;
				handler.sendMessage(msg);
				try {
					sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Message msg = Message.obtain(handler);
			msg.what = 1;
			msg.obj = topMargin;
			handler.sendMessage(msg);
			//return topMargin;
		}

	}
	
	private Handler handler = new Handler() 
	{
		public void handleMessage(Message message) 
		{
			switch (message.what) 
			{
				
				case 0:
					updateHeaderView();
					headerLayoutParams.topMargin = Integer.parseInt(message.obj.toString());
					header.setLayoutParams(headerLayoutParams);
				break;
				case 1:
					headerLayoutParams.topMargin = Integer.parseInt(message.obj.toString());
					header.setLayoutParams(headerLayoutParams);
					currentStatus = STATUS_REFRESH_FINISHED;
				break;
				case 2:
					updateHeaderView();
					headerLayoutParams.topMargin = 0;
					header.setLayoutParams(headerLayoutParams);
					if (mListener != null) {
						mListener.onRefresh();
					}
				break;
				
			}
		}
	};
	private void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	
	
	public interface PullToRefreshListener {

		void onRefresh();

	}

}
