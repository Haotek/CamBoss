package com.tutk.P2PCam264.DELUX;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tutk.IOTC.IRegisterIOTCListener;
import com.tutk.P2PCam264.MyCamera;
import com.tutk.Kalay.general.R;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

import general.DatabaseManager;

public class MultiViewFragment extends Fragment {

	public static int NEW_PAGE = 1;
	public static int NORMAL_PAGE = -1;
	public static int HAS_PAGE = 2;
	private static final int OnePageMultiView_MAX_NUM = 4;

	public static final boolean SupportMultiPage = false;
	public static livaviewFragmentAdapter mAdapter;
	private static IRegisterIOTCListener IRegisterIOTCListener;

	public static ViewPager mPager;
	public static CirclePageIndicator mIndicator;

	private static Context mContext;
	private static List<MyCamera> CameraList;
	private static boolean IsAddpage = false; // pageview無法重復新增

	public MultiViewFragment() {
		// prevent for InstantiationException
		MultiViewActivity.mNeedToRebuild = true;
	}

	public MultiViewFragment(Context context, List<MyCamera> cameraList, livaviewFragmentAdapter adapter) {
		mContext = context;
		CameraList = cameraList;
//		mAdapter = adapter;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.multi_view_fragment, container, false);

//		if(livaviewFragmentAdapter.tempFragmentManager == null)
		mAdapter = new livaviewFragmentAdapter(((MultiViewActivity) mContext).getSupportFragmentManager());
//		else
//			mAdapter = ;

		mPager = (ViewPager) view.findViewById(R.id.liveviewpager);
		mPager.setAdapter(mAdapter);
		mIndicator = (CirclePageIndicator) view.findViewById(R.id.indicator);		

		InitMutliMonitor();

		return view;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	public static void InitMutliMonitor() {

		ArrayList<ArrayList<Chaanel_to_Monitor_Info>> mChaanel_Info = new ArrayList<ArrayList<Chaanel_to_Monitor_Info>>();
		int LinkMonitorCount = 0;
		int PageCount = 0;
		ArrayList<Chaanel_to_Monitor_Info> BasicsubInfo = new ArrayList<Chaanel_to_Monitor_Info>();
		mChaanel_Info.add(BasicsubInfo);
		for (MyCamera camera : CameraList) {
			if (camera != null) {

				DatabaseManager manager = new DatabaseManager(mContext);
				SQLiteDatabase db = manager.getReadableDatabase();
				Cursor cursor = db.query(DatabaseManager.TABLE_DEVICE_CHANNEL_ALLOCATION_TO_MONITOR, new String[] { "dev_uid", "dev_channel_Index",
						"Monitor_Index" }, "dev_uid = ?", new String[] { camera.getUID() }, null, null, "Monitor_Index" + " ASC");
				while (cursor.moveToNext()) {
					int ChannelIndex = cursor.getInt(1);
					int index = cursor.getInt(2);
					int monitorindex = index % 4;

					PageCount = index / 4;
					while (mChaanel_Info.size() < (PageCount + 1)) {
						ArrayList<Chaanel_to_Monitor_Info> subInfo = new ArrayList<Chaanel_to_Monitor_Info>();
						mChaanel_Info.add(subInfo);
					}

					mChaanel_Info.get(PageCount).add(
							new Chaanel_to_Monitor_Info(camera.getUUID(), camera.getName(), camera.getUID(), ChannelIndex, "CH" + ChannelIndex,
									monitorindex));
				}
				cursor.close();
			}
		} 

		PageCount = mChaanel_Info.size() - 1;
		if (mChaanel_Info.get(PageCount).size() == OnePageMultiView_MAX_NUM && SupportMultiPage) {
			ArrayList<Chaanel_to_Monitor_Info> subInfo = new ArrayList<Chaanel_to_Monitor_Info>();
			mChaanel_Info.add(subInfo);
			PageCount++;
		}

		if (PageCount != 0) {
			if (SupportMultiPage) {
				mIndicator.setViewPager(mPager);
				mIndicator.setOnPageChangeListener(mAdapter);
			}
		}
		mAdapter.SetChannelInfo(mChaanel_Info);
		mAdapter.notifyDataSetChanged();
	}

	public static void reflash_Status() {

		if (mAdapter != null)
			mAdapter.reflash_Status();
	}

	public static void ReflashChannelInfo() {

		if (mAdapter != null)
			mAdapter.ReflashChannelInfo();
	}
	
	public static void ClearNewIcon(int MonitorIndex){
		mAdapter.ClearNewIcon(MonitorIndex);
	}

	public static boolean ChangeMutliMonitor(Chaanel_to_Monitor_Info Chaanel_Info, int MonitorIndex, boolean isNew) {

		int result = mAdapter.ChangeChannelInfo(Chaanel_Info, MonitorIndex, isNew);

		if (result > 0 && !IsAddpage) {
			if (SupportMultiPage) {
				mIndicator.setViewPager(mPager);
				mIndicator.setOnPageChangeListener(mAdapter);
			}
		}
		mAdapter.notifyDataSetChanged();

		if (result == HAS_PAGE)
			return false;
		else
			return true;
	}

	public static void updateView() {
		mAdapter.notifyDataSetChanged();
	}

	public static void removeFromMultiView(String uid, String uuid) {

		if (mAdapter != null)
			mAdapter.remove_uid(uid, uuid);
	}

	public static void connected_Status(String UID) {

		if (mAdapter != null)
			mAdapter.connected_Status(UID);
	}

	public static void showDelelteLayout() {
		if (mAdapter != null)
			mAdapter.showDelelteLayout();
	}

	public static void hideDelelteLayout() {
		if (mAdapter != null)
			mAdapter.hideDelelteLayout();
	}
	
	public static boolean checkDelete(){
		if (mAdapter != null){
			if (mAdapter.checkDelete()){
				return true;
			}
		}
		return false;
	}

	public static void doDelete() {
		if (mAdapter != null)
			mAdapter.doDelete();
	}

	public static ArrayList<Integer> getMonitorList(int MonitorIndex) {
		if (mAdapter != null)
			return mAdapter.getMonitorList(MonitorIndex);
		else
			return null;
	}

	public static void viewOnResume() {
		if (mAdapter != null)
			mAdapter.viewOnResume();
	}
	
	public static void startView(){
		if (mAdapter != null)
			mAdapter.startView();
	}
	
	public static void clearView(){
		if (mAdapter != null)
			mAdapter.clearView();
	}

	public void checkPlayingChannel(String uid){
		if(mAdapter != null){
			mAdapter.checkPlayingChannel(uid);
		}
	}
}
