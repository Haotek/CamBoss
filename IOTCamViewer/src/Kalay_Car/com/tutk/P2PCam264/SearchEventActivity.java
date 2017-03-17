package com.tutk.P2PCam264;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.tutk.P2PCam264.DELUX.MultiViewActivity;
import com.tutk.Kalay.general.R;
import com.actionbarsherlock.app.SherlockActivity;

import general.DatabaseManager;

public class SearchEventActivity extends SherlockActivity {

	private static final int Build_VERSION_CODES_ICE_CREAM_SANDWICH = 14;

	private static final int SEARCH_WITHIN_AN_HOUR = 0;
	private static final int SEARCH_WITHIN_HALF_A_DAY = 1;
	private static final int SEARCH_WITHIN_A_DAY = 2;
	private static final int SEARCH_WITHIN_A_WEEK = 3;

	private Calendar mStartSearchCalendar;
	private Calendar mStopSearchCalendar;
	private int mSearchEventType;

	private String mDevUID;

	private List<CachedHistory> list = Collections.synchronizedList(new ArrayList<CachedHistory>());
	private SearchListAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
		actionBar.setCustomView(R.layout.titlebar);
		TextView tv = (TextView)this.findViewById(R.id.bar_text);
		tv.setText(getText(R.string.app_name));

		setContentView(R.layout.search_event);

		if (Build.VERSION.SDK_INT < Build_VERSION_CODES_ICE_CREAM_SANDWICH) {
			BitmapDrawable bg = (BitmapDrawable) getResources().getDrawable(R.drawable.bg_striped);
			bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
			getSupportActionBar().setBackgroundDrawable(bg);
		}

		Bundle bundle = this.getIntent().getExtras();
		mDevUID = bundle.getString("dev_uid");

		adapter = new SearchListAdapter(this);
		ListView listView = (ListView) findViewById(R.id.lstEventSearch);

		TextView txtView;

		View searchWithinAnHour = getLayoutInflater().inflate(R.layout.search_event_predefined_item, null);
		txtView = (TextView) searchWithinAnHour.findViewById(R.id.txtSearchPredefined);
		txtView.setText(getText(R.string.tips_search_within_an_hour));
		listView.addHeaderView(searchWithinAnHour);

		View searchWithinHalfDay = getLayoutInflater().inflate(R.layout.search_event_predefined_item, null);
		txtView = (TextView) searchWithinHalfDay.findViewById(R.id.txtSearchPredefined);
		txtView.setText(getText(R.string.tips_search_within_half_a_day));
		listView.addHeaderView(searchWithinHalfDay);

		View searchWithinADay = getLayoutInflater().inflate(R.layout.search_event_predefined_item, null);
		txtView = (TextView) searchWithinADay.findViewById(R.id.txtSearchPredefined);
		txtView.setText(getText(R.string.tips_search_within_a_day));
		listView.addHeaderView(searchWithinADay);

		View searchWithinAWeek = getLayoutInflater().inflate(R.layout.search_event_predefined_item, null);
		txtView = (TextView) searchWithinAWeek.findViewById(R.id.txtSearchPredefined);
		txtView.setText(getText(R.string.tips_search_within_a_week));
		listView.addHeaderView(searchWithinAWeek);

		View searchCustom = getLayoutInflater().inflate(R.layout.search_event_predefined_item, null);
		txtView = (TextView) searchCustom.findViewById(R.id.txtSearchPredefined);
		txtView.setText(getText(R.string.tips_search_custom));
		listView.addHeaderView(searchCustom);

		initCachedHistoryList();

		listView.setAdapter(adapter);
		listView.setOnItemClickListener(listViewOnItemClickListener);
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

	private AdapterView.OnItemClickListener listViewOnItemClickListener = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {

			Bundle extras = new Bundle();
			Intent intent = new Intent();

			Calendar calStart = Calendar.getInstance(TimeZone.getTimeZone("gmt"));
			Calendar calStop = Calendar.getInstance(TimeZone.getTimeZone("gmt"));

			if (position < 4) {

				switch (position) {
				case SEARCH_WITHIN_AN_HOUR:
					calStart.add(Calendar.HOUR, -1);
					break;

				case SEARCH_WITHIN_HALF_A_DAY:
					calStart.add(Calendar.HOUR_OF_DAY, -12);
					break;

				case SEARCH_WITHIN_A_DAY:
					calStart.add(Calendar.DAY_OF_YEAR, -1);
					break;

				case SEARCH_WITHIN_A_WEEK:
					calStart.add(Calendar.DAY_OF_YEAR, -7);
					break;
				}

				extras.putInt("event_type", 0);
				extras.putLong("start_time", calStart.getTimeInMillis());
				extras.putLong("stop_time", calStop.getTimeInMillis());

				intent.putExtras(extras);
				setResult(RESULT_OK, intent);
				finish();
				overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);

			} else if (position == 4) {
				showCustomSearch();
			} else {

				int idx = position - 5;
				CachedHistory cachedHistory = list.get(idx);

				calStart.setTimeInMillis(cachedHistory.StartTime);
				calStop.setTimeInMillis(cachedHistory.StopTime);

				extras.putInt("event_type", cachedHistory.EventType);
				extras.putLong("start_time", calStart.getTimeInMillis());
				extras.putLong("stop_time", calStop.getTimeInMillis());

				intent.putExtras(extras);
				setResult(RESULT_OK, intent);
				finish();
				overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
			}
		}
	};

	private void initCachedHistoryList() {

		DatabaseManager manager = new DatabaseManager(this);
		SQLiteDatabase db = manager.getReadableDatabase();
		Cursor cursor = db.query(DatabaseManager.TABLE_SEARCH_HISTORY, new String[] { "_id", "dev_uid", "search_event_type", "search_start_time",
				"search_stop_time" }, "dev_uid = '" + mDevUID + "'", null, null, null, "_id DESC LIMIT 10");

		while (cursor.moveToNext()) {

			// long db_id = cursor.getLong(0);
			// String dev_uid = cursor.getString(1);
			int event_type = cursor.getInt(2);
			long start_time = cursor.getLong(3);
			long stop_time = cursor.getLong(4);

			CachedHistory history = new CachedHistory(event_type, start_time, stop_time);
			list.add(history);
		}

		cursor.close();
		db.close();
	}

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

		SearchEventActivity.this.mStartSearchCalendar = Calendar.getInstance();
		SearchEventActivity.this.mStartSearchCalendar.set(Calendar.SECOND, 0);
		SearchEventActivity.this.mStopSearchCalendar = Calendar.getInstance();
		SearchEventActivity.this.mStopSearchCalendar.set(Calendar.SECOND, 0);

		btnStartDate.setText(dateFormat.format(SearchEventActivity.this.mStartSearchCalendar.getTime()));
		btnStartTime.setText(timeFormat.format(SearchEventActivity.this.mStartSearchCalendar.getTime()));
		btnStopDate.setText(dateFormat.format(SearchEventActivity.this.mStopSearchCalendar.getTime()));
		btnStopTime.setText(timeFormat.format(SearchEventActivity.this.mStopSearchCalendar.getTime()));

		// set spinner adapter & listener
		ArrayAdapter<CharSequence> adapter = ArrayAdapter
				.createFromResource(SearchEventActivity.this, R.array.event_type, R.layout.search_event_myspinner);
		adapter.setDropDownViewResource(R.layout.search_event_myspinner);
		
		
		spinEventType.setAdapter(adapter);
		spinEventType.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

				SearchEventActivity.this.mSearchEventType = position;

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		final DatePickerDialog.OnDateSetListener startDateOnDateSetListener = new DatePickerDialog.OnDateSetListener() {

			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

				SearchEventActivity.this.mStartSearchCalendar.set(year, monthOfYear, dayOfMonth,
						SearchEventActivity.this.mStartSearchCalendar.get(Calendar.HOUR_OF_DAY),
						SearchEventActivity.this.mStartSearchCalendar.get(Calendar.MINUTE), 0);

				btnStartDate.setText(dateFormat.format(SearchEventActivity.this.mStartSearchCalendar.getTime()));

				// todo:
				// if start time > stop time , then stop time = start time
				//

				if (SearchEventActivity.this.mStartSearchCalendar.after(SearchEventActivity.this.mStopSearchCalendar)) {

					SearchEventActivity.this.mStopSearchCalendar.setTimeInMillis(SearchEventActivity.this.mStartSearchCalendar.getTimeInMillis());
					btnStopDate.setText(dateFormat.format(SearchEventActivity.this.mStopSearchCalendar.getTime()));
					btnStopTime.setText(timeFormat.format(SearchEventActivity.this.mStopSearchCalendar.getTime()));
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
				tmp.set(year, monthOfYear, dayOfMonth, SearchEventActivity.this.mStopSearchCalendar.get(Calendar.HOUR_OF_DAY),
						SearchEventActivity.this.mStopSearchCalendar.get(Calendar.MINUTE), 0);

				if (tmp.after(SearchEventActivity.this.mStartSearchCalendar) || tmp.equals(SearchEventActivity.this.mStartSearchCalendar)) {

					SearchEventActivity.this.mStopSearchCalendar.set(year, monthOfYear, dayOfMonth,
							SearchEventActivity.this.mStopSearchCalendar.get(Calendar.HOUR_OF_DAY),
							SearchEventActivity.this.mStopSearchCalendar.get(Calendar.MINUTE), 0);

					btnStopDate.setText(dateFormat.format(SearchEventActivity.this.mStopSearchCalendar.getTime()));

				}
			}
		};

		final TimePickerDialog.OnTimeSetListener startTimeOnTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

				SearchEventActivity.this.mStartSearchCalendar.set(SearchEventActivity.this.mStartSearchCalendar.get(Calendar.YEAR),
						SearchEventActivity.this.mStartSearchCalendar.get(Calendar.MONTH),
						SearchEventActivity.this.mStartSearchCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);

				btnStartTime.setText(timeFormat.format(SearchEventActivity.this.mStartSearchCalendar.getTime()));

				// todo:
				// if start time > stop time , then stop time = start time
				//
				if (SearchEventActivity.this.mStartSearchCalendar.after(SearchEventActivity.this.mStopSearchCalendar)) {

					SearchEventActivity.this.mStopSearchCalendar.setTimeInMillis(SearchEventActivity.this.mStartSearchCalendar.getTimeInMillis());
					btnStopTime.setText(timeFormat.format(SearchEventActivity.this.mStopSearchCalendar.getTime()));
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
				tmp.set(SearchEventActivity.this.mStopSearchCalendar.get(Calendar.YEAR), SearchEventActivity.this.mStopSearchCalendar.get(Calendar.MONTH),
						SearchEventActivity.this.mStopSearchCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute, 0);

				if (tmp.after(SearchEventActivity.this.mStartSearchCalendar) || tmp.equals(SearchEventActivity.this.mStartSearchCalendar)) {

					SearchEventActivity.this.mStopSearchCalendar.set(SearchEventActivity.this.mStopSearchCalendar.get(Calendar.YEAR),
							SearchEventActivity.this.mStopSearchCalendar.get(Calendar.MONTH),
							SearchEventActivity.this.mStopSearchCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);

					btnStopTime.setText(timeFormat.format(SearchEventActivity.this.mStopSearchCalendar.getTime()));
				}
			}
		};

		btnStartDate.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				Calendar cal = Calendar.getInstance();

				new DatePickerDialog(SearchEventActivity.this, startDateOnDateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
						.get(Calendar.DAY_OF_MONTH)).show();
			}
		});

		btnStartTime.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				Calendar cal = Calendar.getInstance();

				new TimePickerDialog(SearchEventActivity.this, startTimeOnTimeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false)
						.show();
			}
		});

		btnStopDate.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				Calendar cal = Calendar.getInstance();

				new DatePickerDialog(SearchEventActivity.this, stopDateOnDateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
						.get(Calendar.DAY_OF_MONTH)).show();
			}
		});

		btnStopTime.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				Calendar cal = Calendar.getInstance();

				new TimePickerDialog(SearchEventActivity.this, stopTimeOnTimeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false)
						.show();
			}
		});

		btnOK.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
//20130725 chun 
//				DatabaseManager manager = new DatabaseManager(SearchEventActivity.this);
//				manager.addSearchHistory(SearchEventActivity.this.mDevUID, SearchEventActivity.this.mSearchEventType, mStartSearchCalendar.getTimeInMillis(),
//						mStopSearchCalendar.getTimeInMillis());

				Bundle extras = new Bundle();
				extras.putInt("event_type", SearchEventActivity.this.mSearchEventType);
				extras.putLong("start_time", mStartSearchCalendar.getTimeInMillis());
				extras.putLong("stop_time", mStopSearchCalendar.getTimeInMillis());

				Intent intent = new Intent();
				intent.putExtras(extras);
				setResult(RESULT_OK, intent);
				finish();
				overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);

				dlg.dismiss();
			}
		});

		btnCancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				dlg.dismiss();
			}
		});

		dlg.show();
	}

	private class CachedHistory {

		public int EventType;
		public long StartTime;
		public long StopTime;

		public CachedHistory(int type, long start_time, long stop_time) {
			EventType = type;
			StartTime = start_time;
			StopTime = stop_time;
		}
	}

	private class SearchListAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public SearchListAdapter(Context context) {

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

		public View getView(int position, View convertView, ViewGroup parent) {

			final CachedHistory cachedHistory = (CachedHistory) getItem(position);
			ViewHolder holder = null;

			if (convertView == null) {

				convertView = mInflater.inflate(R.layout.search_event_history_item, null);

				holder = new ViewHolder();
				holder.txtType = (TextView) convertView.findViewById(R.id.txtEventType);
				holder.txtStartTime = (TextView) convertView.findViewById(R.id.txtStartTime);
				holder.txtStopTime = (TextView) convertView.findViewById(R.id.txtStopTime);

				convertView.setTag(holder);

			} else {

				holder = (ViewHolder) convertView.getTag();
			}

			if (cachedHistory != null) {

				Calendar start = Calendar.getInstance(TimeZone.getTimeZone("gmt"));
				Calendar stop = Calendar.getInstance(TimeZone.getTimeZone("gmt"));

				start.setTimeInMillis(cachedHistory.StartTime);
				stop.setTimeInMillis(cachedHistory.StopTime);

				SimpleDateFormat format = new SimpleDateFormat();
				format.setTimeZone(TimeZone.getDefault());

				holder.txtType.setText(MultiViewActivity.getEventType(SearchEventActivity.this, cachedHistory.EventType, true));
				holder.txtStartTime.setText(format.format(start.getTime()));
				holder.txtStopTime.setText(format.format(stop.getTime()));
			}

			return convertView;
		}

		private final class ViewHolder {
			public TextView txtType;
			public TextView txtStartTime;
			public TextView txtStopTime;
		}

	}
}
