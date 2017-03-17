package com.tutk.P2PCam264;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.AVIOCTRLDEFs.SMsgAVIoctrlListEventReq;
import com.tutk.IOTC.AVIOCTRLDEFs.SStreamDef;
import com.tutk.IOTC.AVIOCTRLDEFs.STimeDay;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.IRegisterIOTCListener;
import com.tutk.P2PCam264.DELUX.MultiViewActivity;
import com.tutk.P2PCam264.RefreshableView.PullToRefreshListener;
import com.tutk.Kalay.general.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;

public class EventListActivity extends SherlockActivity implements IRegisterIOTCListener {

	private static final int Build_VERSION_CODES_ICE_CREAM_SANDWICH = 14;

	private static final int OPT_MENU_ITEM_SEARCH = 0;
	private static final int REQUEST_CODE_EVENT_DETAIL = 0;
	private static final int REQUEST_CODE_EVENT_SEARCH = 1;
	private static final int AN_HOUR = 0;
	private static final int HALF_DAY = 1;
	private static final int A_DAY = 2;
	private static final int A_WEEK = 3;
	private static final int COSTUM = 4;

	private List<EventInfo> list = Collections.synchronizedList(new ArrayList<EventInfo>());
	private EventListAdapter adapter;

	private MyCamera mCamera;

	private View loadingView = null;
	private View offlineView = null;
	private View noResultView = null;

	private ListView eventListView = null;
	private ImageButton searchBtn;
	private ImageButton btnCH;
	private TextView tvCH;
	private LinearLayout layoutMask;
	private LinearLayout layoutSearch;
	private LinearLayout layoutCH;
	private Button btnMask;
	private Button btnSearchCancel;
	private Button btnSearchOK;
	private Button btnCHCancel;
	private Button btnCHOK;
	private WheelView wheelSearch;
	private WheelView wheelCH;
	private SearchAdapter adapterSearch;
	private CHAdapter adapterCH;

	private String mDevUUID;
	private String mDevUID;
	private String mDevNickName;
	private String mViewAcc;
	private String mViewPwd;
	private int mCameraChannel;
	private int mSearchType = 0;
	private long startTime;
	private long stopTime;
	private int eventType;
	private ArrayList<String> chList = new ArrayList<String>();
	public static int currentItem = 0;
	private boolean mShowPanel = false;
	private Calendar mStartSearchCalendar;
	private Calendar mStopSearchCalendar;

	private Boolean mIsSearchingEvent = false;

	RefreshableView refreshableView;

	private enum MyMode {
		SEARCH, CHANNEL
	}

	private MyMode mMode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.titlebar);
		TextView tv = (TextView) this.findViewById(R.id.bar_text);
		tv.setText(getText(R.string.txt_Events_List));

		setContentView(R.layout.event_view);

		if (Build.VERSION.SDK_INT < Build_VERSION_CODES_ICE_CREAM_SANDWICH) {
			BitmapDrawable bg = (BitmapDrawable) getResources().getDrawable(R.drawable.bg_striped);
			bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
			getSupportActionBar().setBackgroundDrawable(bg);
		}

		Bundle bundle = this.getIntent().getExtras();
		mDevUUID = bundle.getString("dev_uuid");
		mDevUID = bundle.getString("dev_uid");
		mDevNickName = bundle.getString("dev_nickname");
		mCameraChannel = bundle.getInt("camera_channel");
		mViewAcc = bundle.getString("view_acc");
		mViewPwd = bundle.getString("view_pwd");

		/* register recvIOCtrl listener */
		for (MyCamera camera : MultiViewActivity.CameraList) {

			if (mDevUUID.equalsIgnoreCase(camera.getUUID())) {
				mCamera = camera;
				mCamera.registerIOTCListener(this);
				mCamera.resetEventCount();

				break;
			}
		}

		adapter = new EventListAdapter(this);
		refreshableView = (RefreshableView) findViewById(R.id.refreshable_view);
		refreshableView.setOnRefreshListener(new PullToRefreshListener() {
			@Override
			public void onRefresh() {
				// try {
				// Thread.sleep(3000);
				// } catch (InterruptedException e) {
				// e.printStackTrace();
				// }
				refreshableView.finishRefreshing();
				if (!mIsSearchingEvent) {
					eventListView.removeFooterView(loadingView);
					eventListView.removeFooterView(noResultView);
					if (mCamera == null || !mCamera.isChannelConnected(0)) {
						eventListView.addFooterView(offlineView);
						eventListView.setAdapter(adapter);
					} else {
						initEventList();
					}
				}

			}
		}, 1);
		eventListView = (ListView) findViewById(R.id.lstEventList);
		eventListView.setOnItemClickListener(listViewOnItemClickListener);

		loadingView = getLayoutInflater().inflate(R.layout.loading_events, null);
		offlineView = getLayoutInflater().inflate(R.layout.camera_is_offline, null);
		noResultView = getLayoutInflater().inflate(R.layout.no_result, null);

		layoutMask = (LinearLayout) findViewById(R.id.layoutMasking);
		layoutSearch = (LinearLayout) findViewById(R.id.layoutSearch);
		layoutCH = (LinearLayout) findViewById(R.id.layoutCH);
		btnMask = (Button) findViewById(R.id.btnScreen);
		btnSearchCancel = (Button) findViewById(R.id.btnSearchCancel);
		btnSearchOK = (Button) findViewById(R.id.btnSearchOK);
		btnCHCancel = (Button) findViewById(R.id.btnCHCancel);
		btnCHOK = (Button) findViewById(R.id.btnCHOK);

		btnMask.setOnClickListener(clickMask);
		btnSearchCancel.setOnClickListener(clickMask);
		btnCHCancel.setOnClickListener(clickMask);
		btnCHOK.setOnClickListener(clickCH);
		btnSearchOK.setOnClickListener(clickSearch);

		searchBtn = (ImageButton) findViewById(R.id.btnSearch);
		searchBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mIsSearchingEvent) {
//
//					Bundle extras = new Bundle();
//					extras.putString("dev_uid", mDevUID);
//
//					Intent intent = new Intent();
//					intent.putExtras(extras);
//					intent.setClass(EventListActivity.this, EventListActivity.class);
//
//					startActivityForResult(intent, REQUEST_CODE_EVENT_SEARCH);
					mMode = MyMode.SEARCH;
					layoutMask.setVisibility(View.VISIBLE);
					layoutSearch.startAnimation(AnimationUtils.loadAnimation(EventListActivity.this, R.anim.bottombar_slide_show));
					layoutSearch.setVisibility(View.VISIBLE);
					currentItem = mSearchType;
					wheelSearch.setCurrentItem(currentItem);
					adapterSearch.notifyDataChangedEvent();
					btnMask.setEnabled(true);
					mShowPanel = true;
				}
			}
		});
		btnCH = (ImageButton) findViewById(R.id.btnCH);
		tvCH = (TextView) findViewById(R.id.tvCH);

		if (mCamera == null || !mCamera.isChannelConnected(0)) {
			eventListView.addFooterView(offlineView);
			eventListView.setAdapter(adapter);
			searchBtn.setEnabled(false);
			btnCH.setEnabled(false);

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.MINUTE, -1 * 60 * 12);
			startTime = calendar.getTimeInMillis();
			stopTime = System.currentTimeMillis();
			String startTimeSring = getLocalTime(startTime, false);
			String stopTimeString = getLocalTime(stopTime, false);
			TextView txtSearchTime = (TextView) findViewById(R.id.txtSearchTimeDuration);
			txtSearchTime.setText(startTimeSring + " - " + stopTimeString);

		} else {
			initEventList();
		}

		if (mCamera != null && mCamera.isSessionConnected() && mCamera.getMultiStreamSupported(0) && mCamera.getSupportedStream().length > 1) {
			btnCH.setEnabled(true);
			int i = 0;
			chList.clear();
			for (SStreamDef streamDef : mCamera.getSupportedStream()) {
				i++;
				if (i < 10)
					chList.add("Channel 0" + i);
				else
					chList.add("Channel " + i);
			}
			chList.add("ALL");

			btnCH.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mMode = MyMode.CHANNEL;
					layoutMask.setVisibility(View.VISIBLE);
					layoutCH.startAnimation(AnimationUtils.loadAnimation(EventListActivity.this, R.anim.bottombar_slide_show));
					layoutCH.setVisibility(View.VISIBLE);
					currentItem = mCameraChannel;
					wheelCH.setCurrentItem(currentItem);
					adapterCH.notifyDataChangedEvent();
					btnMask.setEnabled(true);
					mShowPanel = true;
				}
			});
		} else {
			btnCH.setEnabled(false);
		}

		wheelSearch = (WheelView) findViewById(R.id.wheelSearch);
		wheelCH = (WheelView) findViewById(R.id.wheelCH);
		adapterSearch = new SearchAdapter(this);
		adapterCH = new CHAdapter(this);
		wheelSearch.setViewAdapter(adapterSearch);
		wheelCH.setViewAdapter(adapterCH);

		wheelSearch.addChangingListener(new OnWheelChangedListener() {			
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				currentItem = newValue;
				adapterSearch.notifyDataChangedEvent();
			}
		});
		
		wheelCH.addChangingListener(new OnWheelChangedListener() {			
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				currentItem = wheel.getCurrentItem();
				adapterCH.notifyDataChangedEvent();
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		quit();
	}

	@Override
	protected void onStart() {
		super.onStart();
		// FlurryAgent.onStartSession(this, "Q1SDXDZQ21BQMVUVJ16W");
	}

	@Override
	protected void onStop() {
		super.onStop();
		// FlurryAgent.onEndSession(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_EVENT_DETAIL) {

			Bundle extras = data.getExtras();
			String evtUUID = extras.getString("event_uuid");

			for (EventInfo evt : list) {

				if (evt.getUUID().equalsIgnoreCase(evtUUID)) {
					evt.EventStatus = EventInfo.EVENT_READED;
					adapter.notifyDataSetChanged();
					break;
				}
			}
		}

		else if (requestCode == REQUEST_CODE_EVENT_SEARCH && resultCode == RESULT_OK) {

			Bundle extras = data.getExtras();
			startTime = extras.getLong("start_time");
			stopTime = extras.getLong("stop_time");
			eventType = extras.getInt("event_type");

			searchEventList(startTime, stopTime, eventType, mCameraChannel);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {

		case KeyEvent.KEYCODE_BACK:
			if (mShowPanel) {
				btnMask.setEnabled(false);
				mShowPanel = false;

				Animation anim = AnimationUtils.loadAnimation(EventListActivity.this, R.anim.bottombar_slide_hide);
				anim.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						layoutMask.setVisibility(View.GONE);
					}
				});

				if (mMode == MyMode.CHANNEL) {
					layoutCH.startAnimation(anim);
					layoutCH.setVisibility(View.GONE);
				} else {
					layoutSearch.startAnimation(anim);
					layoutSearch.setVisibility(View.GONE);
				}
				return false;
			} else {
				quit();
				finish();
				overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
				break;
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		Configuration cfg = getResources().getConfiguration();

		if (cfg.orientation == Configuration.ORIENTATION_LANDSCAPE) {

		} else if (cfg.orientation == Configuration.ORIENTATION_PORTRAIT) {

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

//		menu.add(Menu.NONE, OPT_MENU_ITEM_SEARCH, 1, "Search").setIcon(R.drawable.ic_menu_search).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		if (id == OPT_MENU_ITEM_SEARCH) {

			if (!mIsSearchingEvent) {

				Bundle extras = new Bundle();
				extras.putString("dev_uid", mDevUID);

				Intent intent = new Intent();
				intent.putExtras(extras);
				intent.setClass(EventListActivity.this, EventListActivity.class);

				startActivityForResult(intent, REQUEST_CODE_EVENT_SEARCH);
				overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			}
		}

		return super.onOptionsItemSelected(item);
	}

	private AdapterView.OnItemClickListener listViewOnItemClickListener = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {

			if (list.size() == 0 || list.size() < position || !mCamera.getPlaybackSupported(0))
				return;

			int pos = position - eventListView.getHeaderViewsCount();

			if (pos < 0)
				return;

			EventInfo evt = list.get(pos);

			if (evt.EventStatus == EventInfo.EVENT_NORECORD)
				return;

			Bundle extras = new Bundle();
			extras.putString("dev_uuid", mDevUUID);
			extras.putString("dev_nickname", mDevNickName);
			extras.putInt("camera_channel", mCameraChannel);
			extras.putInt("event_type", evt.EventType);
			extras.putLong("event_time", evt.Time);
			extras.putString("event_uuid", evt.getUUID());
			extras.putString("view_acc", mViewAcc);
			extras.putString("view_pwd", mViewPwd);
			extras.putByteArray("event_time2", evt.EventTime.toByteArray());

			Intent intent = new Intent();
			intent.putExtras(extras);
			intent.setClass(EventListActivity.this, PlaybackActivity.class);
			startActivityForResult(intent, REQUEST_CODE_EVENT_DETAIL);
			overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
		}
	};

	private void quit() {

		if (mCamera != null) {

			mCamera.unregisterIOTCListener(this);
			mCamera = null;
		}

		// update history view info back to database
		// DatabaseManager dm = new DatabaseManager(this);
		// dm.updateDeviceHistoryViewByUID(mDevUID, System.currentTimeMillis());
		// dm = null;
	}

	private void initEventList() {

		// m_newHistoryView = System.currentTimeMillis();
		// searchEventList(m_origHistoryView, m_newHistoryView,
		// AVAPIs.AVIOCTRL_EVENT_ALL);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.MINUTE, -1 * 60 * 12);
		startTime = calendar.getTimeInMillis();
		stopTime = System.currentTimeMillis();
		mSearchType = 0;
		eventType = AVIOCTRLDEFs.AVIOCTRL_EVENT_ALL;
		searchEventList(startTime, stopTime, eventType, mCameraChannel);
	}

	private static String getLocalTime(long utcTime, boolean subMonth) {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("gmt"));
		calendar.setTimeInMillis(utcTime);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mma", Locale.getDefault());
		dateFormat.setTimeZone(TimeZone.getDefault());

		if (subMonth)
			calendar.add(Calendar.MONTH, -1);

		return dateFormat.format(calendar.getTime());
	}

	private void searchEventList(long startTime, long stopTime, int eventType, int channel) {

		if (mCamera != null) {

			list.clear();
			adapter.notifyDataSetChanged();

			mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_LISTFILE_REQ,
                    AVIOCTRLDEFs.SMsgAVIoctrlListFileReq.parseConent(channel, startTime, stopTime, (byte) eventType, (byte) 0, (byte) 0));

			eventListView.removeFooterView(noResultView);
			eventListView.addFooterView(loadingView);

			// set search time to actionbar title
			String startTimeSring = getLocalTime(startTime, false);
			String stopTimeString = getLocalTime(stopTime, false);
			TextView txtSearchTime = (TextView) findViewById(R.id.txtSearchTimeDuration);
			txtSearchTime.setText(startTimeSring + " - " + stopTimeString);

			eventListView.setAdapter(adapter);
			adapter.notifyDataSetChanged();

			mIsSearchingEvent = true;

			/* timeout for no search result been found */
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {

					if (mIsSearchingEvent) {

						mIsSearchingEvent = false;
						eventListView.removeFooterView(loadingView);
						eventListView.addFooterView(noResultView);
						adapter.notifyDataSetChanged();
					}
				}

			}, 180000);

		}
	}

	private void searchALLEventList(long startTime, long stopTime, int eventType) {

		if (mCamera != null) {

			list.clear();
			adapter.notifyDataSetChanged();

			int channel = 0;
			for (SStreamDef streamDef : mCamera.getSupportedStream()) {
				mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_LISTEVENT_REQ,
						SMsgAVIoctrlListEventReq.parseConent(channel, startTime, stopTime, (byte) eventType, (byte) 0));
				channel++;
			}

			eventListView.removeFooterView(noResultView);
			eventListView.addFooterView(loadingView);

			// set search time to actionbar title
			String startTimeSring = getLocalTime(startTime, false);
			String stopTimeString = getLocalTime(stopTime, false);
			TextView txtSearchTime = (TextView) findViewById(R.id.txtSearchTimeDuration);
			txtSearchTime.setText(startTimeSring + " - " + stopTimeString);

			eventListView.setAdapter(adapter);
			adapter.notifyDataSetChanged();

			mIsSearchingEvent = true;

			/* timeout for no search result been found */
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {

					if (mIsSearchingEvent) {

						mIsSearchingEvent = false;
						eventListView.removeFooterView(loadingView);
						eventListView.addFooterView(noResultView);
						adapter.notifyDataSetChanged();
					}
				}

			}, 180000);

		}
	}

	public class EventInfo {

		public static final int EVENT_UNREADED = 0;
		public static final int EVENT_READED = 1;
		public static final int EVENT_NORECORD = 2;

		public int EventType;
		public long Time;
		public STimeDay EventTime;
		public int EventStatus;

		private UUID m_uuid = UUID.randomUUID();

		public String getUUID() {
			return m_uuid.toString();
		}

		public EventInfo(int eventType, long time, int eventStatus) {

			EventType = eventType;
			Time = time;
			EventStatus = eventStatus;
		}

		public EventInfo(int eventType, STimeDay eventTime, int eventStatus) {

			EventType = eventType;
			EventTime = eventTime;
			EventStatus = eventStatus;
		}
	}

	public class EventListAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public EventListAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		public int getCount() {
			return list.size();
		}

		public Object getItem(int position) {
			return list.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		@Override
		public boolean isEnabled(int position) {

			if (list.size() == 0)
				return false;

			return super.isEnabled(position);
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			final EventInfo evt = (EventInfo) getItem(position);

			ViewHolder holder = null;

			if (convertView == null) {

				convertView = mInflater.inflate(R.layout.event_list, null);

				holder = new ViewHolder();
				holder.event = (TextView) convertView.findViewById(R.id.event);
				holder.time = (TextView) convertView.findViewById(R.id.time);
				holder.indicator = (FrameLayout) convertView.findViewById(R.id.eventLayout);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.event.setText(MultiViewActivity.getEventType(EventListActivity.this, evt.EventType, false));
			// holder.time.setText(EventListActivity.getLocalTime(evt.Time,
			// true));
			holder.time.setText(evt.EventTime.getLocalTime());
			if (holder.indicator != null && mCamera != null) {
				holder.indicator.setVisibility(mCamera.getPlaybackSupported(0) & evt.EventStatus != EventInfo.EVENT_NORECORD ? View.VISIBLE
						: View.GONE);
			}

			if (evt.EventStatus == EventInfo.EVENT_UNREADED) {
				holder.event.setTypeface(null, Typeface.BOLD);
				holder.event.setTextColor(0xFF000000);
			} else {
				holder.event.setTypeface(null, Typeface.NORMAL);
				holder.event.setTextColor(0xFF999999);
			}

			return convertView;

		}// getView()

		private final class ViewHolder {
			public TextView event;
			public TextView time;
			public FrameLayout indicator;
		}
	}// EventListAdapter

	private class SearchAdapter extends AbstractWheelTextAdapter {
		private String[] items = { getText(R.string.tips_search_within_an_hour).toString(),
				getText(R.string.tips_search_within_half_a_day).toString(), getText(R.string.tips_search_within_a_day).toString(),
				getText(R.string.tips_search_within_a_week).toString(), getText(R.string.tips_search_custom).toString() };

		protected SearchAdapter(Context context) {
			super(context, R.layout.wheel_view_adapter, NO_RESOURCE);

			setItemTextResource(R.id.txt_wheel_item);
		}

		@Override
		public View getItem(int index, View cachedView, ViewGroup parent) {
			View view = super.getItem(index, cachedView, parent);
			return view;
		}

		@Override
		public int getItemsCount() {
			return items.length;
		}

		@Override
		protected CharSequence getItemText(int index) {
			return items[index];
		}
	}

	private class CHAdapter extends AbstractWheelTextAdapter {
		protected CHAdapter(Context context) {
			super(context, R.layout.wheel_view_adapter, NO_RESOURCE);

			setItemTextResource(R.id.txt_wheel_item);
		}

		@Override
		public View getItem(int index, View cachedView, ViewGroup parent) {
			View view = super.getItem(index, cachedView, parent);
			return view;
		}

		@Override
		public int getItemsCount() {
			return chList.size();
		}

		@Override
		protected CharSequence getItemText(int index) {
			return chList.get(index);
		}
	}

	private OnClickListener clickMask = new OnClickListener() {
		@Override
		public void onClick(View v) {
			btnMask.setEnabled(false);
			mShowPanel = false;

			Animation anim = AnimationUtils.loadAnimation(EventListActivity.this, R.anim.bottombar_slide_hide);
			anim.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					layoutMask.setVisibility(View.GONE);
				}
			});

			if (mMode == MyMode.CHANNEL) {
				layoutCH.startAnimation(anim);
				layoutCH.setVisibility(View.GONE);
			} else {
				layoutSearch.startAnimation(anim);
				layoutSearch.setVisibility(View.GONE);
			}
		}
	};

	private OnClickListener clickSearch = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Calendar calStart = Calendar.getInstance(TimeZone.getTimeZone("gmt"));
			Calendar calStop = Calendar.getInstance(TimeZone.getTimeZone("gmt"));

			switch (wheelSearch.getCurrentItem()) {
			case AN_HOUR:
				calStart.add(Calendar.HOUR, -1);
				mSearchType = AN_HOUR;
				break;
			case HALF_DAY:
				calStart.add(Calendar.HOUR_OF_DAY, -12);
				mSearchType = HALF_DAY;
				break;
			case A_DAY:
				calStart.add(Calendar.DAY_OF_YEAR, -1);
				mSearchType = A_DAY;
				break;
			case A_WEEK:
				calStart.add(Calendar.DAY_OF_YEAR, -7);
				mSearchType = A_WEEK;
				break;
			case COSTUM:
				mSearchType = COSTUM;
				showCustomSearch();
				return;
			}

			startTime = calStart.getTimeInMillis();
			stopTime = calStop.getTimeInMillis();

			if ((mCameraChannel + 1) == chList.size())
				searchALLEventList(startTime, stopTime, eventType);
			else
				searchEventList(startTime, stopTime, eventType, mCameraChannel);

			btnMask.setEnabled(false);
			mShowPanel = false;

			Animation anim = AnimationUtils.loadAnimation(EventListActivity.this, R.anim.bottombar_slide_hide);
			anim.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					layoutMask.setVisibility(View.GONE);
				}
			});

			layoutSearch.startAnimation(anim);
			layoutSearch.setVisibility(View.GONE);
		}
	};

	private OnClickListener clickCH = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if ((wheelCH.getCurrentItem() + 1) > (chList.size() - 1)) {
				tvCH.setText("ALL");
				mCameraChannel = wheelCH.getCurrentItem();
				searchALLEventList(startTime, stopTime, eventType);
			} else {
				tvCH.setText("CH" + (wheelCH.getCurrentItem() + 1));
				mCameraChannel = wheelCH.getCurrentItem();
				searchEventList(startTime, stopTime, eventType, mCameraChannel);
			}

			btnMask.setEnabled(false);
			mShowPanel = false;

			Animation anim = AnimationUtils.loadAnimation(EventListActivity.this, R.anim.bottombar_slide_hide);
			anim.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					layoutMask.setVisibility(View.GONE);
				}
			});

			layoutCH.startAnimation(anim);
			layoutCH.setVisibility(View.GONE);
		}
	};

	private void showCustomSearch() {

		AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.HoloAlertDialog));
		final AlertDialog dlg = builder.create();
		dlg.setTitle(getText(R.string.dialog_EventSearch));
		dlg.setIcon(android.R.drawable.ic_dialog_info);

		LayoutInflater inflater = dlg.getLayoutInflater();
		View view = inflater.inflate(R.layout.search_event_custom, null);
		dlg.setView(view);

		final Spinner spinEventType = (Spinner) view.findViewById(R.id.spinEventType);
		final Button btnStartDate = (Button) view.findViewById(R.id.btnStartDate);
		final Button btnStartTime = (Button) view.findViewById(R.id.btnStartTime);
		final Button btnStopDate = (Button) view.findViewById(R.id.btnStopDate);
		final Button btnStopTime = (Button) view.findViewById(R.id.btnStopTime);
		Button btnOK = (Button) view.findViewById(R.id.btnOK);
		Button btnCancel = (Button) view.findViewById(R.id.btnCancel);

		// set button
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

		EventListActivity.this.mStartSearchCalendar = Calendar.getInstance();
		EventListActivity.this.mStartSearchCalendar.set(Calendar.SECOND, 0);
		EventListActivity.this.mStopSearchCalendar = Calendar.getInstance();
		EventListActivity.this.mStopSearchCalendar.set(Calendar.SECOND, 0);

		btnStartDate.setText(dateFormat.format(EventListActivity.this.mStartSearchCalendar.getTime()));
		btnStartTime.setText(timeFormat.format(EventListActivity.this.mStartSearchCalendar.getTime()));
		btnStopDate.setText(dateFormat.format(EventListActivity.this.mStopSearchCalendar.getTime()));
		btnStopTime.setText(timeFormat.format(EventListActivity.this.mStopSearchCalendar.getTime()));

		// set spinner adapter & listener
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(EventListActivity.this, R.array.event_type,
				R.layout.search_event_myspinner);
		adapter.setDropDownViewResource(R.layout.search_event_myspinner);

		spinEventType.setAdapter(adapter);
		spinEventType.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

				EventListActivity.this.eventType = position;

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		final DatePickerDialog.OnDateSetListener startDateOnDateSetListener = new DatePickerDialog.OnDateSetListener() {

			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

				EventListActivity.this.mStartSearchCalendar.set(year, monthOfYear, dayOfMonth,
						EventListActivity.this.mStartSearchCalendar.get(Calendar.HOUR_OF_DAY),
						EventListActivity.this.mStartSearchCalendar.get(Calendar.MINUTE), 0);

				btnStartDate.setText(dateFormat.format(EventListActivity.this.mStartSearchCalendar.getTime()));

				// todo:
				// if start time > stop time , then stop time = start time
				//

				if (EventListActivity.this.mStartSearchCalendar.after(EventListActivity.this.mStopSearchCalendar)) {

					EventListActivity.this.mStopSearchCalendar.setTimeInMillis(EventListActivity.this.mStartSearchCalendar.getTimeInMillis());
					btnStopDate.setText(dateFormat.format(EventListActivity.this.mStopSearchCalendar.getTime()));
					btnStopTime.setText(timeFormat.format(EventListActivity.this.mStopSearchCalendar.getTime()));
				}
			}
		};

		final DatePickerDialog.OnDateSetListener stopDateOnDateSetListener = new DatePickerDialog.OnDateSetListener() {

			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

				// todo:
				// let tmp = after set stop time
				// if tmp time < start time, do nothing.
				//
				Calendar tmp = Calendar.getInstance();
				tmp.set(year, monthOfYear, dayOfMonth, EventListActivity.this.mStopSearchCalendar.get(Calendar.HOUR_OF_DAY),
						EventListActivity.this.mStopSearchCalendar.get(Calendar.MINUTE), 0);

				if (tmp.after(EventListActivity.this.mStartSearchCalendar) || tmp.equals(EventListActivity.this.mStartSearchCalendar)) {

					EventListActivity.this.mStopSearchCalendar.set(year, monthOfYear, dayOfMonth,
							EventListActivity.this.mStopSearchCalendar.get(Calendar.HOUR_OF_DAY),
							EventListActivity.this.mStopSearchCalendar.get(Calendar.MINUTE), 0);

					btnStopDate.setText(dateFormat.format(EventListActivity.this.mStopSearchCalendar.getTime()));

				}
			}
		};

		final TimePickerDialog.OnTimeSetListener startTimeOnTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

				EventListActivity.this.mStartSearchCalendar.set(EventListActivity.this.mStartSearchCalendar.get(Calendar.YEAR),
						EventListActivity.this.mStartSearchCalendar.get(Calendar.MONTH),
						EventListActivity.this.mStartSearchCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);

				btnStartTime.setText(timeFormat.format(EventListActivity.this.mStartSearchCalendar.getTime()));

				// todo:
				// if start time > stop time , then stop time = start time
				//
				if (EventListActivity.this.mStartSearchCalendar.after(EventListActivity.this.mStopSearchCalendar)) {

					EventListActivity.this.mStopSearchCalendar.setTimeInMillis(EventListActivity.this.mStartSearchCalendar.getTimeInMillis());
					btnStopTime.setText(timeFormat.format(EventListActivity.this.mStopSearchCalendar.getTime()));
				}
			}
		};

		final TimePickerDialog.OnTimeSetListener stopTimeOnTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

				// todo:
				// let tmp = after set stop time
				// if tmp time < start time, do nothing.
				//
				Calendar tmp = Calendar.getInstance();
				tmp.set(EventListActivity.this.mStopSearchCalendar.get(Calendar.YEAR),
						EventListActivity.this.mStopSearchCalendar.get(Calendar.MONTH),
						EventListActivity.this.mStopSearchCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute, 0);

				if (tmp.after(EventListActivity.this.mStartSearchCalendar) || tmp.equals(EventListActivity.this.mStartSearchCalendar)) {

					EventListActivity.this.mStopSearchCalendar.set(EventListActivity.this.mStopSearchCalendar.get(Calendar.YEAR),
							EventListActivity.this.mStopSearchCalendar.get(Calendar.MONTH),
							EventListActivity.this.mStopSearchCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);

					btnStopTime.setText(timeFormat.format(EventListActivity.this.mStopSearchCalendar.getTime()));
				}
			}
		};

		btnStartDate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Calendar cal = Calendar.getInstance();

				new DatePickerDialog(EventListActivity.this, startDateOnDateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
						.get(Calendar.DAY_OF_MONTH)).show();
			}
		});

		btnStartTime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Calendar cal = Calendar.getInstance();

				new TimePickerDialog(EventListActivity.this, startTimeOnTimeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
						false).show();
			}
		});

		btnStopDate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Calendar cal = Calendar.getInstance();

				new DatePickerDialog(EventListActivity.this, stopDateOnDateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
						.get(Calendar.DAY_OF_MONTH)).show();
			}
		});

		btnStopTime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Calendar cal = Calendar.getInstance();

				new TimePickerDialog(EventListActivity.this, stopTimeOnTimeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
						false).show();
			}
		});

		btnOK.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startTime = mStartSearchCalendar.getTimeInMillis();
				stopTime = mStopSearchCalendar.getTimeInMillis();

				if ((mCameraChannel + 1) == chList.size())
					searchALLEventList(startTime, stopTime, eventType);
				else
					searchEventList(startTime, stopTime, eventType, mCameraChannel);

				dlg.dismiss();
				
				btnMask.setEnabled(false);
				mShowPanel = false;

				Animation anim = AnimationUtils.loadAnimation(EventListActivity.this, R.anim.bottombar_slide_hide);
				anim.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						layoutMask.setVisibility(View.GONE);
					}
				});

				layoutSearch.startAnimation(anim);
				layoutSearch.setVisibility(View.GONE);
			}
		});

		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				dlg.dismiss();
			}
		});

		dlg.show();
	}

	@Override
	public void receiveIOCtrlData(final Camera camera, int sessionChannel, int avIOCtrlMsgType, byte[] data) {

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

	@Override
	public void receiveFrameData(final Camera camera, int sessionChannel, Bitmap bmp) {

	}

	@Override
	public void receiveFrameInfo(final Camera camera, int sessionChannel, long bitRate, int frameRate, int onlineNm, int frameCount,
			int incompleteFrameCount) {

	}

	@Override
	public void receiveSessionInfo(final Camera camera, int resultCode) {

		if (mCamera == camera) {
			Bundle bundle = new Bundle();
			Message msg = handler.obtainMessage();
			msg.what = resultCode;
			msg.setData(bundle);
			handler.sendMessage(msg);
		}
	}

	@Override
	public void receiveChannelInfo(final Camera camera, int sessionChannel, int resultCode) {

		if (mCamera == camera) {
			Bundle bundle = new Bundle();
			bundle.putInt("sessionChannel", sessionChannel);

			Message msg = handler.obtainMessage();
			msg.what = resultCode;
			msg.setData(bundle);
			handler.sendMessage(msg);
		}

	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			Bundle bundle = msg.getData();
			byte[] data = bundle.getByteArray("data");
			int sessionChannel = bundle.getInt("sessionChannel");

			switch (msg.what) {

			case Camera.CONNECTION_STATE_CONNECTING:
				break;
			case Camera.CONNECTION_STATE_WRONG_PASSWORD:
			case Camera.CONNECTION_STATE_CONNECT_FAILED:
			case Camera.CONNECTION_STATE_DISCONNECTED:
			case Camera.CONNECTION_STATE_UNKNOWN_DEVICE:
			case Camera.CONNECTION_STATE_TIMEOUT:

				if (eventListView.getFooterViewsCount() == 0) {
					list.clear();
					eventListView.addFooterView(offlineView);
					eventListView.setAdapter(adapter);
					searchBtn.setEnabled(false);
					btnCH.setEnabled(false);
				}

				break;

			case Camera.CONNECTION_STATE_CONNECTED:

				if (sessionChannel == 0) {
					eventListView.removeFooterView(offlineView);
					adapter.notifyDataSetChanged();
					searchBtn.setEnabled(true);
					btnCH.setEnabled(true);
				}

				break;

			case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_LISTFILE_RESP: Log.i("RRR", "GET");

				if (data.length >= 12 && mIsSearchingEvent) {

					// int idx = data[8];
					int end = data[9];
					int cnt = data[10];

					if (cnt > 0) {

						int pos = 12;
						int size = AVIOCTRLDEFs.SAvFile.getTotalSize();

						for (int i = 0; i < cnt; i++) {

							byte[] t = new byte[8];
							System.arraycopy(data, i * size + pos, t, 0, 8);
							STimeDay time = new STimeDay(t);

							byte event = data[i * size + pos + 8];
							byte status = data[i * size + pos + 9];
                            byte lebgth = data[i * size + pos + 10];
                            byte[] temp = new byte[72];
                            String tt = new String(data, i * size + pos + 11, lebgth);
                            Log.i("RRR", tt);

							// System.out.println(idx + ". Event:" + event + ", Status: " + status +
							// " -> " + time.year + "/" + time.month + "/" + time.day + " "
							// + time.hour + ":" + time.minute + ":" + time.second + ") " +
							// Camera.getHex(t, 8));

							// EventInfo evt = new EventInfo((int) event,
							// time.getTimeInMillis(), (int) status);
							// list.add(evt);
							list.add(new EventInfo((int) event, time, (int) status));
						}

						adapter.notifyDataSetChanged();
					}

					if (end == 1) {

						mIsSearchingEvent = false;

						eventListView.removeFooterView(loadingView);
						eventListView.removeFooterView(noResultView);

						if (list.size() == 0)
							Toast.makeText(EventListActivity.this, EventListActivity.this.getText(R.string.tips_search_event_no_result),
									Toast.LENGTH_SHORT).show();
					}
				}

				break;
			}

			super.handleMessage(msg);
		}
	};

	@Override
	public void receiveFrameDataForMediaCodec(Camera camera, int avChannel, byte[] buf, int length, int pFrmNo, byte[] pFrmInfoBuf, boolean isIframe, int codecId) {
		// TODO Auto-generated method stub

	}
}
