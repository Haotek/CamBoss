package com.tutk.P2PCam264.DELUX;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.tutk.Logger.Glog;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class livaviewFragmentAdapter extends FragmentPagerAdapter implements OnPageChangeListener {
	private static final String TAG = "MultiViewActivity";
	private ArrayList<ArrayList<Chaanel_to_Monitor_Info>> mChaanel_Info;
	private ArrayList<LiveviewFragment> mLiveviewFragment = new ArrayList<LiveviewFragment>();
	private static final int OnePageMultiView_MAX_NUM = 4;
	public ArrayList<ArrayList<Boolean>> mDelList = new ArrayList<ArrayList<Boolean>>();
	public ArrayList<Integer> mMonitorList = new ArrayList<Integer>();
	private Timer timer = new Timer();
	private TimerTask timerTask;

	public static int page = 0;
	public static FragmentManager tempFragmentManager;

	public livaviewFragmentAdapter(FragmentManager fm) {
		super(fm);
		tempFragmentManager = fm;
	}

	// 當有需要更動連線狀態時
	public void reflash_Status() {
		if (mLiveviewFragment == null)
			return;
		for (LiveviewFragment LiveviewFragment : mLiveviewFragment) {
			if (LiveviewFragment != null)
				LiveviewFragment.reflash_Status();
		}
	}

	// 移除特定UID時
	public void remove_uid(String uid, String uuid) {
		if (mLiveviewFragment == null)
			return;
		for (LiveviewFragment LiveviewFragment : mLiveviewFragment) {
			if (LiveviewFragment != null)
				LiveviewFragment.DeleteAllMonitor(uid, uuid);
		}
	}

	// 連線成功時 要重新撥放monitor
	public void connected_Status(String UID) {
		if (mLiveviewFragment == null)
			return;
		for (LiveviewFragment LiveviewFragment : mLiveviewFragment) {
			if (LiveviewFragment != null)
				LiveviewFragment.connected_Status(UID);
		}
	}

	public void showDelelteLayout() {
		if (mLiveviewFragment == null)
			return;

		mDelList.clear();
		for (LiveviewFragment LiveviewFragment : mLiveviewFragment) {
			if (LiveviewFragment != null) {
				mDelList.add(LiveviewFragment.showDelelteLayout());
			}
		} 
	}

	public void hideDelelteLayout() {
		if (mLiveviewFragment == null)
			return;
		for (LiveviewFragment LiveviewFragment : mLiveviewFragment) {
			if (LiveviewFragment != null)
				LiveviewFragment.hideDelelteLayout();
		}
	}
	
	public boolean checkDelete(){
		if (mLiveviewFragment == null)
			return false;
		
		for (LiveviewFragment LiveviewFragment : mLiveviewFragment) {
			if (LiveviewFragment != null){
				if (LiveviewFragment.checkDelete()){
					return true;
				}
			}
		}
		
		return false;
	}

	public void doDelete() {
		if (mLiveviewFragment == null)
			return;
		
		int mPage = 0;
		for (LiveviewFragment LiveviewFragment : mLiveviewFragment) {
			if (LiveviewFragment != null)
				LiveviewFragment.doDelete(mPage);
			mPage ++;
		}
	}

	public void SetChannelInfo(ArrayList<ArrayList<Chaanel_to_Monitor_Info>> Chaanel_Info) {
		mChaanel_Info = Chaanel_Info;
		for (int i = 0; i < mChaanel_Info.size(); i++) {
			mLiveviewFragment.add(LiveviewFragment.newInstance(mChaanel_Info.get(i), i));
		}
	}

	public ArrayList<Integer> getMonitorList(int MonitorIndex) {
		if (mLiveviewFragment == null)
			return null;

		mMonitorList.clear();
		int mPage = 0;
		int mStart = MonitorIndex / 4 + 1;
		for (LiveviewFragment LiveviewFragment : mLiveviewFragment) {
			if (LiveviewFragment != null) {
				mMonitorList.addAll(LiveviewFragment.getMonitorList(mPage));
			}
			mPage++;
		}

		while (mMonitorList.size() < mStart * 4 * 4) {
			ArrayList<Integer> emp = new ArrayList<Integer>();
			emp.add(-1);
			emp.add(-1);
			emp.add(-1);
			emp.add(-1);
			mMonitorList.addAll(emp);
		}
		return mMonitorList;
	}
	
	public void clearView() {
		for (LiveviewFragment LiveviewFragment : mLiveviewFragment) { 
			if (LiveviewFragment != null) {
				LiveviewFragment.clearView();
			};
		}
	}

	// 機器被修改完成時 用來更新ui資料
	public void ReflashChannelInfo() {
		mLiveviewFragment.get(page).reflash_Status();
	}
	
	public void ClearNewIcon(int MonitorIndex){
		if(MonitorIndex == MultiViewActivity.CLEAR_ALL){
			for(int i=0;i < mLiveviewFragment.size();i++){
				mLiveviewFragment.get(i).ClearNewIcon(MonitorIndex);
			}
		}else{
			int mPage = MonitorIndex / 4;
			int index = MonitorIndex % 4;
			mLiveviewFragment.get(mPage).ClearNewIcon(index);
		}
	}

	// 回傳是否新增1頁
	public int ChangeChannelInfo(Chaanel_to_Monitor_Info Chaanel_Info, int MonitorIndex, boolean isNew) {
		int mPage = MonitorIndex / 4;
		int index = MonitorIndex % 4;
		int result = -1;

		if (mLiveviewFragment.size() < (mPage + 1)) {
			mChaanel_Info.add(new ArrayList<Chaanel_to_Monitor_Info>());
			mLiveviewFragment.add(LiveviewFragment.newInstance(mChaanel_Info.get(mChaanel_Info.size() - 1), mPage));
			return MultiViewFragment.HAS_PAGE;
		}

		mLiveviewFragment.get(mPage).SetMonitor(Chaanel_Info, index, isNew);
		// 搜尋陣列有無綁定同一格monitor的資料 沒有則新增 有則取代
		boolean same_MonitorIndex = false;
		for (int i = 0; i < mChaanel_Info.get(mPage).size(); i++) {
			if (mChaanel_Info != null) {
				if (mChaanel_Info.get(mPage).get(i) != null) {
					if (mChaanel_Info.get(mPage).get(i).MonitorIndex == index) {
						if (i < mChaanel_Info.get(mPage).size()) {
							mChaanel_Info.get(mPage).set(i, Chaanel_Info);
							same_MonitorIndex = true;
						}
						break;
					}
				}
			}
		}
		if (!same_MonitorIndex) {
			mChaanel_Info.get(mPage).add(Chaanel_Info);
		}

		if (mChaanel_Info.get(mPage).size() == OnePageMultiView_MAX_NUM && mChaanel_Info.size() == (mPage + 1)) {
			if (MultiViewActivity.SupportMultiPage) {
				mChaanel_Info.add(new ArrayList<Chaanel_to_Monitor_Info>());
				mLiveviewFragment.add(LiveviewFragment.newInstance(mChaanel_Info.get(mChaanel_Info.size() - 1), mPage + 1)); 
			}
			result = MultiViewFragment.NEW_PAGE;
		} else {
			result = MultiViewFragment.NORMAL_PAGE;
		}

		return result;
	}

	public livaviewFragmentAdapter getAdapter() {
		return this;
	}

	public void viewOnResume() {
		mLiveviewFragment.get(page).viewOnResume();
	}
	
	public void startView(){		
		mLiveviewFragment.get(page).start();
	}

	public void checkPlayingChannel(String uid){
		for(int i = 0; i < mLiveviewFragment.size(); i++){
			mLiveviewFragment.get(i).checkPlayingChannel(uid);
		}
	}

//	@Override
//	public int getItemPosition(Object object) {
//		return POSITION_NONE;
//	}

	@Override
	public Fragment getItem(int position) {  

		Glog.D(TAG, "Fragment.getItem( " + position + " )...");
		if (position != page) {
			mLiveviewFragment.get(position).stop();
		}
		return mLiveviewFragment.get(position);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (mChaanel_Info != null) {
			return mChaanel_Info.size();
		} else {
			return 0;
		}
	}

	@Override
	public void onPageScrollStateChanged(int position) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(final int position) {
		// TODO Auto-generated method stub
		Glog.D(TAG, "onPageSelected( " + position + " )...");
		mLiveviewFragment.get(page).stop();
		if (timerTask != null)
			timerTask.cancel();
		timerTask = new TimerTask() {
			@Override
			public void run() {
				mLiveviewFragment.get(position).start();
			}
		};
		timer.schedule(timerTask, 2000);
		
		page = position;
	}

}
