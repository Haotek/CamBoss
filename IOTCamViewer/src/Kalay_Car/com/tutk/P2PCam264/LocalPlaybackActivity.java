package com.tutk.P2PCam264;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.SherlockActivity;
import com.tutk.Kalay.general.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.content.pm.PackageManager;

public class LocalPlaybackActivity extends SherlockActivity implements OnClickListener, GestureDetector.OnGestureListener, OnTouchListener {

	private static final int FLING_MIN_DISTANCE = 20;
	private static final int FLING_MIN_VELOCITY = 0;
	private GestureDetector mGestureDetector;

	private String path;
	private Handler handler = new Handler();
	private VideoView mVideoView;
	private SeekBar mSeekBar;

	private ImageButton btnPlayPause;
	private ImageButton btnCenter;

	private TextView tvCurrentTime;
	private TextView tvTotalTime;

	private RelativeLayout layoutTitleBar;
	private RelativeLayout layoutButtonBar;

	private boolean mIsPlaying = false;
	private boolean mIsFling = false;
	private boolean mIsBarShowing = true;
	private boolean mIsLand = false;

	private static final int DEFAULT_LIST_SIZE = 1;
	private List<String> VIDEO_FILES = new ArrayList<String>(DEFAULT_LIST_SIZE);
	private int mPosition;
	private int mSize;
	private int mCurrentPosition;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.gc();

		Bundle extras = this.getIntent().getExtras();
		VIDEO_FILES = extras.getStringArrayList("videos");
		mPosition = extras.getInt("position");
		mSize = extras.getInt("size");
		path = VIDEO_FILES.get(mPosition);

		initialWidgets();
	}

	@Override
	protected void onPause() {
		handler.removeCallbacks(r);
		super.onPause();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		handler.removeCallbacks(r);
		initialWidgets();
	}

	private void initialWidgets() {
		Configuration cfg = getResources().getConfiguration();
		ActionBar actionBar = getActionBar();

		if (cfg.orientation == Configuration.ORIENTATION_PORTRAIT) {
			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
			actionBar.setCustomView(R.layout.titlebar);
			TextView tv = (TextView) this.findViewById(R.id.bar_text);
			tv.setText(getText(R.string.dialog_Playback));
			actionBar.show();
			mIsLand = false;
		} else {
			actionBar.hide();
			mIsLand = true;
		}

		setContentView(R.layout.local_playback);

		mVideoView = (VideoView) findViewById(R.id.videoView);
		mSeekBar = (SeekBar) findViewById(R.id.sbVideo);
		btnPlayPause = (ImageButton) findViewById(R.id.btn_playpause);
		tvCurrentTime = (TextView) findViewById(R.id.txt_current);
		tvTotalTime = (TextView) findViewById(R.id.txt_total);
		layoutButtonBar = (RelativeLayout) findViewById(R.id.button_bar);
		layoutTitleBar = (RelativeLayout) findViewById(R.id.title_bar);

		mGestureDetector = new GestureDetector(this);
		mVideoView.setOnTouchListener(this);
		mVideoView.setLongClickable(true);
		mGestureDetector.setIsLongpressEnabled(true);

		btnPlayPause.setOnClickListener(this);

		if (mIsLand) {
			btnCenter = (ImageButton) findViewById(R.id.btnCenter);
			btnCenter.setOnClickListener(this);
			if(mIsBarShowing){
				layoutButtonBar.setVisibility(View.VISIBLE);
				layoutTitleBar.setVisibility(View.VISIBLE);
			}else{
				layoutButtonBar.setVisibility(View.INVISIBLE);
				layoutTitleBar.setVisibility(View.INVISIBLE);
			}
		}

		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					mVideoView.seekTo(progress);
				}
			}
		});

		mVideoView.setVideoPath(path);
		mVideoView.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {

				return true;
			}
		});
		if (mIsPlaying) {
			mVideoView.seekTo(mCurrentPosition);
		}
		mVideoView.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				tvTotalTime.setText(FormatTime(mVideoView.getDuration()));
				mVideoView.start();
				mIsPlaying = true;
				btnPlayPause.setBackgroundResource(R.drawable.btn_pause);
				mSeekBar.setMax(mVideoView.getDuration());
				updateSeekBar();
			}
		});
	}

	private void updateSeekBar() {
		handler.post(r);
	}

	Runnable r = new Runnable() {
		@Override
		public void run() {
			mCurrentPosition = mVideoView.getCurrentPosition();
			mSeekBar.setProgress(mCurrentPosition);
			tvCurrentTime.setText(FormatTime(mCurrentPosition));

			if (!mVideoView.isPlaying() && mCurrentPosition == 0) {
				player_not_support();
				return;
			}

			if (!mVideoView.isPlaying()) {
				btnPlayPause.setBackgroundResource(R.drawable.btn_play);
				if(mIsLand)
				btnCenter.setVisibility(View.VISIBLE);
					mIsPlaying = false;
				return;
			}

			handler.postDelayed(r, 100);
		}
	};

	private String FormatTime(int time) {
		time /= 1000;
		int minute = time / 60;
		int second = time % 60;
		minute %= 60;

		return String.format("%02d:%02d", minute, second);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_playpause:
			if (mIsPlaying) {
				mVideoView.pause();
				btnPlayPause.setBackgroundResource(R.drawable.btn_play);
				if(mIsLand)
					btnCenter.setVisibility(View.VISIBLE);
				mIsPlaying = false;
				handler.removeCallbacks(r);
			} else {
				mVideoView.start();
				btnPlayPause.setBackgroundResource(R.drawable.btn_pause);
				if(mIsLand)
					btnCenter.setVisibility(View.GONE);
				mIsPlaying = true;
				updateSeekBar();
			}
			break;
		case R.id.btnCenter:
			if (!mIsPlaying) {
				if(mIsLand)
					btnCenter.setVisibility(View.GONE);
				mVideoView.start();
				btnPlayPause.setBackgroundResource(R.drawable.btn_pause);
				mIsPlaying = true;
				updateSeekBar();
			}
			break;
		}
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		mIsFling = false;
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		mIsFling = true;
		handler.removeCallbacks(r);
		if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
			// Fling left
			if (mPosition == (mSize - 1)) {
				mPosition = 0;
				path = VIDEO_FILES.get(mPosition);
				mVideoView.setVideoPath(path);
				mVideoView.setOnPreparedListener(new OnPreparedListener() {
					@Override
					public void onPrepared(MediaPlayer mp) {
						tvTotalTime.setText(FormatTime(mVideoView.getDuration()));
						mVideoView.start();
						mIsPlaying = true;
						btnPlayPause.setBackgroundResource(R.drawable.btn_pause);
						if(mIsLand)
							btnCenter.setVisibility(View.GONE);
						mSeekBar.setMax(mVideoView.getDuration());
						updateSeekBar();
					}
				});
			} else {
				mPosition += 1;
				path = VIDEO_FILES.get(mPosition);
				mVideoView.setVideoPath(path);
				mVideoView.setOnPreparedListener(new OnPreparedListener() {
					@Override
					public void onPrepared(MediaPlayer mp) {
						tvTotalTime.setText(FormatTime(mVideoView.getDuration()));
						mVideoView.start();
						mIsPlaying = true;
						btnPlayPause.setBackgroundResource(R.drawable.btn_pause);
						if(mIsLand)
							btnCenter.setVisibility(View.GONE);
						mSeekBar.setMax(mVideoView.getDuration());
						updateSeekBar();
					}
				});
			}
		} else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
			// Fling right
			if (mPosition == 0) {
				mPosition = mSize - 1;
				path = VIDEO_FILES.get(mPosition);
				mVideoView.setVideoPath(path);
				mVideoView.setOnPreparedListener(new OnPreparedListener() {
					@Override
					public void onPrepared(MediaPlayer mp) {
						tvTotalTime.setText(FormatTime(mVideoView.getDuration()));
						mVideoView.start();
						mIsPlaying = true;
						btnPlayPause.setBackgroundResource(R.drawable.btn_pause);
						mSeekBar.setMax(mVideoView.getDuration());
						updateSeekBar();
					}
				});
			} else {
				mPosition -= 1;
				path = VIDEO_FILES.get(mPosition);
				mVideoView.setVideoPath(path);
				mVideoView.setOnPreparedListener(new OnPreparedListener() {
					@Override
					public void onPrepared(MediaPlayer mp) {
						tvTotalTime.setText(FormatTime(mVideoView.getDuration()));
						mVideoView.start();
						mIsPlaying = true;
						btnPlayPause.setBackgroundResource(R.drawable.btn_pause);
						mSeekBar.setMax(mVideoView.getDuration());
						updateSeekBar();
					}
				});
			}
		}
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		mGestureDetector.onTouchEvent(event);
		Configuration cfg = getResources().getConfiguration();

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_UP:
			if (mIsLand && !mIsFling) {
				if (mIsBarShowing) {
					layoutTitleBar.startAnimation(AnimationUtils.loadAnimation(LocalPlaybackActivity.this, R.anim.topbar_slide_hide));
					layoutTitleBar.setVisibility(View.INVISIBLE);
					layoutButtonBar.startAnimation(AnimationUtils.loadAnimation(LocalPlaybackActivity.this, R.anim.bottombar_slide_hide));
					layoutButtonBar.setVisibility(View.INVISIBLE);
				} else {
					layoutTitleBar.startAnimation(AnimationUtils.loadAnimation(LocalPlaybackActivity.this, R.anim.topbar_slide_show));
					layoutTitleBar.setVisibility(View.VISIBLE);
					layoutButtonBar.startAnimation(AnimationUtils.loadAnimation(LocalPlaybackActivity.this, R.anim.bottombar_slide_show));
					layoutButtonBar.setVisibility(View.VISIBLE);
				}
				mIsBarShowing = !mIsBarShowing;
			}
			break;
		}
		return true;
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

	private void player_not_support() {
		Intent intent = getPackageManager().getLaunchIntentForPackage("com.mxtech.videoplayer.ad");
		if (intent == null) {
			Builder adbNoPlayer = new Builder(LocalPlaybackActivity.this);
			adbNoPlayer.setTitle(getString(R.string.dialog_no_app));
			adbNoPlayer.setMessage(getString(R.string.txt_intent_to_app));
			adbNoPlayer.setPositiveButton(R.string.ok, intent_to_googleplay);
			adbNoPlayer.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
					overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
				}
			});
			adbNoPlayer.show();
		} else {
			Toast.makeText(LocalPlaybackActivity.this, getString(R.string.txt_intent_to_app), Toast.LENGTH_LONG).show();
			intent = new Intent(Intent.ACTION_VIEW);
			File file = new File(path);
			intent.setDataAndType(Uri.fromFile(file), "video/*");

			startActivity(intent);
			finish();
			overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
//			startActivityForResult(intent, RESULT_OK);
		}
	}

	private DialogInterface.OnClickListener intent_to_googleplay = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			Uri uri = Uri.parse("market://details?id=com.mxtech.videoplayer.ad");
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
			finish();
			overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
		}
	};

}
