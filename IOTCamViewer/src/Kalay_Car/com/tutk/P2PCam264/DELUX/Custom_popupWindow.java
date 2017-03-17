package com.tutk.P2PCam264.DELUX;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.tutk.Kalay.general.R;

import java.util.HashMap;

public class Custom_popupWindow implements View.OnClickListener {

	public final static int ORG = 0;
	public final static int LIVEVIEW_QUALITY = 1;
	public final static int LIVEVIEW_CH = 2;
	public final static int LIVEVIEW_ENV = 3;
	public final static int DEVICES_CH = 4;
	public final static int EVENT_CH = 5;
    public final static int PHOTO = 6;

	public boolean auto_dismis = true;
	private static PopupWindow popupWindow;
	On_PopupWindow_click_Listener On_PopupWindow_click_Listener;

	private int width = 0;
	private int height = 0;
	private HashMap<Integer, Boolean> mAddMap = new HashMap<Integer, Boolean>();
	private int mDeviceNo = 0;

	private static int[] channelList = { R.id.layoutCH1, R.id.layoutCH2, R.id.layoutCH3, R.id.layoutCH4, R.id.layoutCH5, R.id.layoutCH6,
			R.id.layoutCH7, R.id.layoutCH8, R.id.layoutCH9, R.id.layoutCH10, R.id.layoutCH11, R.id.layoutCH12, R.id.layoutCH13, R.id.layoutCH14,
			R.id.layoutCH15, R.id.layoutCH16, R.id.layoutALL };
	private static int[] channelButtonList = { R.id.btnCH1, R.id.btnCH2, R.id.btnCH3, R.id.btnCH4, R.id.btnCH5, R.id.btnCH6, R.id.btnCH7,
			R.id.btnCH8, R.id.btnCH9, R.id.btnCH10, R.id.btnCH11, R.id.btnCH12, R.id.btnCH13, R.id.btnCH14, R.id.btnCH15, R.id.btnCH16, R.id.btnALL };
	private static int[] Device_channelButtonList = { R.id.btnCH1_D, R.id.btnCH2_D, R.id.btnCH3_D, R.id.btnCH4_D, R.id.btnCH5_D, R.id.btnCH6_D,
			R.id.btnCH7_D, R.id.btnCH8_D, R.id.btnCH9_D, R.id.btnCH10_D, R.id.btnCH11_D, R.id.btnCH12_D, R.id.btnCH13_D, R.id.btnCH14_D,
			R.id.btnCH15_D, R.id.btnCH16_D };

	public static PopupWindow Menu_PopupWindow_newInstance(Context Context, ViewGroup layout) {
		Custom_popupWindow Menu_popupWindow = new Custom_popupWindow(Context);
		return Menu_popupWindow.Init_popupWindow(Context, layout);
	}

	public static PopupWindow Menu_PopupWindow_newInstance(Context Context, ViewGroup layout,
			On_PopupWindow_click_Listener On_PopupWindow_click_Listener, int situation, int items) {
		Custom_popupWindow Menu_popupWindow = new Custom_popupWindow(Context);
		return Menu_popupWindow.Init_popupWindow(Context, layout, On_PopupWindow_click_Listener, situation, items, -1);
	}

	public static PopupWindow Menu_PopupWindow_newInstance(Context Context, ViewGroup layout,
			On_PopupWindow_click_Listener On_PopupWindow_click_Listener, int situation, int items, int DeviceNo) {
		Custom_popupWindow Menu_popupWindow = new Custom_popupWindow(Context);
		return Menu_popupWindow.Init_popupWindow(Context, layout, On_PopupWindow_click_Listener, situation, items, DeviceNo);
	}

	private Custom_popupWindow(Context Context) {

		super();

	}

	private PopupWindow Init_popupWindow(Context Context, ViewGroup layout) {
		return Init_popupWindow(Context, layout, null, -1, -1, -1);
	}

	private PopupWindow Init_popupWindow(Context Context, ViewGroup layout, On_PopupWindow_click_Listener On_PopupWindow_click_Listener,
			int situation, int items, int position) {
		switch (situation) {
		case LIVEVIEW_QUALITY:
			popupWindow = new PopupWindow(layout);
			if (On_PopupWindow_click_Listener != null) {
				this.On_PopupWindow_click_Listener = On_PopupWindow_click_Listener;
			}
			Button btnMAX = (Button) layout.findViewById(R.id.btnMAX);
			Button btnMID = (Button) layout.findViewById(R.id.btnMID);
			Button btnMIN = (Button) layout.findViewById(R.id.btnMIN);
			btnMAX.setOnClickListener(this);
			btnMID.setOnClickListener(this);
			btnMIN.setOnClickListener(this);

			popupWindow.setBackgroundDrawable(new BitmapDrawable());
			width = Context.getResources().getDimensionPixelSize(R.dimen.bubble_qvga_w);
			height = Context.getResources().getDimensionPixelSize(R.dimen.bubble_qvga_h);
			popupWindow.setWidth(width);
			popupWindow.setHeight(height);
			popupWindow.setOutsideTouchable(true);
			popupWindow.setFocusable(true);
			break;

		case LIVEVIEW_CH:
			popupWindow = new PopupWindow(layout);
			if (On_PopupWindow_click_Listener != null) {
				this.On_PopupWindow_click_Listener = On_PopupWindow_click_Listener;
			}
			for (int i = 0; i < items; i++) {
				LinearLayout mlayout = (LinearLayout) layout.findViewById(channelList[i]);
				mlayout.setVisibility(View.VISIBLE);
				Button btnCH = (Button) layout.findViewById(channelButtonList[i]);
				btnCH.setOnClickListener(this);
			}

			popupWindow.setBackgroundDrawable(new BitmapDrawable());
			width = Context.getResources().getDimensionPixelSize(R.dimen.bubble_ch_w);
			if (items < 5)
				height = LayoutParams.WRAP_CONTENT;
			else
				height = Context.getResources().getDimensionPixelSize(R.dimen.bubble_ch_h);
			popupWindow.setWidth(width);
			popupWindow.setHeight(height);
			popupWindow.setOutsideTouchable(true);
			popupWindow.setFocusable(true);
			break;

		case LIVEVIEW_ENV:
			popupWindow = new PopupWindow(layout);
			if (On_PopupWindow_click_Listener != null) {
				this.On_PopupWindow_click_Listener = On_PopupWindow_click_Listener;
			}
			Button btnEnv50 = (Button) layout.findViewById(R.id.btnEnv50);
			Button btnEnv60 = (Button) layout.findViewById(R.id.btnEnv60);
			Button btnEnvOut = (Button) layout.findViewById(R.id.btnEnvOut);
			Button btnEnvNight = (Button) layout.findViewById(R.id.btnEnvNight);
			btnEnv50.setOnClickListener(this);
			btnEnv60.setOnClickListener(this);
			btnEnvOut.setOnClickListener(this);
			btnEnvNight.setOnClickListener(this);

			popupWindow.setBackgroundDrawable(new BitmapDrawable());
			width = Context.getResources().getDimensionPixelSize(R.dimen.bubble_env_w);
			height = Context.getResources().getDimensionPixelSize(R.dimen.bubble_env_h);
			popupWindow.setWidth(width);
			popupWindow.setHeight(height);
			popupWindow.setOutsideTouchable(true);
			popupWindow.setFocusable(true);
			break;
		case DEVICES_CH:
			popupWindow = new PopupWindow(layout);
			mDeviceNo = position;
			auto_dismis = false;
			mAddMap.clear();
			if (On_PopupWindow_click_Listener != null) {
				this.On_PopupWindow_click_Listener = On_PopupWindow_click_Listener;
			}
			for (int i = 0; i < items; i++) {
				RelativeLayout mlayout = (RelativeLayout) layout.findViewById(channelList[i]);
				mlayout.setVisibility(View.VISIBLE);
				ImageButton btnCH = (ImageButton) layout.findViewById(Device_channelButtonList[i]);
				btnCH.setOnClickListener(this);

				mAddMap.put(i, false);
			}
			Button txtCancel = (Button) layout.findViewById(R.id.btnCancel);
			Button txtOK = (Button) layout.findViewById(R.id.btnOK);
			ScrollView scrollview = (ScrollView) layout.findViewById(R.id.scrollview);
			txtCancel.setOnClickListener(this);
			txtOK.setOnClickListener(this);

			popupWindow.setBackgroundDrawable(new BitmapDrawable());
			width = Context.getResources().getDimensionPixelSize(R.dimen.bubble_dev_w);
			if (items < 6) {
				height = Context.getResources().getDimensionPixelSize(R.dimen.bubble_dev_row_1);
				scrollview.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			} else if (items < 11) {
				height = Context.getResources().getDimensionPixelSize(R.dimen.bubble_dev_row_2);
				scrollview.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			} else {
				height = Context.getResources().getDimensionPixelSize(R.dimen.bubble_dev_row_3);
				scrollview.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, Context.getResources().getDimensionPixelSize(
						R.dimen.bubble_dev_scroll_row_3)));
			}
			popupWindow.setWidth(width);
			popupWindow.setHeight(height);
			popupWindow.setOutsideTouchable(true);
			popupWindow.setFocusable(true);
			break;

//		case EVENT_CH:
//			popupWindow = new PopupWindow(layout);
//			if (On_PopupWindow_click_Listener != null) {
//				this.On_PopupWindow_click_Listener = On_PopupWindow_click_Listener;
//			}
//			for (int i = 0; i < items; i++) {
//				LinearLayout mlayout = (LinearLayout) layout.findViewById(channelList[i]);
//				mlayout.setVisibility(View.VISIBLE);
//				Button btnCH = (Button) layout.findViewById(channelButtonList[i]);
//				btnCH.setOnClickListener(this);
//			}
//			LinearLayout mlayout_foot = (LinearLayout) layout.findViewById(channelList[16]);
//			mlayout_foot.setVisibility(View.VISIBLE);
//			Button btnCH_foot = (Button) layout.findViewById(channelButtonList[16]);
//			btnCH_foot.setOnClickListener(this);
//
//			popupWindow.setBackgroundDrawable(new BitmapDrawable());
//			// px
//			width = (int) (CH_WIDTH * metrics.density);
//			popupWindow.setWidth(width);
//			popupWindow.setHeight(LayoutParams.WRAP_CONTENT);
//			popupWindow.setOutsideTouchable(true);
//			popupWindow.setFocusable(true);
//			break;
		}
		return popupWindow;
	}

	public interface On_PopupWindow_click_Listener {
		public void btn_onDropbox_click(final PopupWindow PopupWindow);

		public void btn_infomation_click(final PopupWindow PopupWindow);

		public void btn_log_in_out_click(final PopupWindow PopupWindow);

		public void btn_change_ch(int channel);

		public void btn_change_quality(int level);

		public void btn_change_env(int mode);

		public void btn_add_monitor(HashMap<Integer, Boolean> AddMap, int DeviceNo);

        public void btn_photo(int value);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (this == null || On_PopupWindow_click_Listener == null) {
			popupWindow.dismiss();
			return;
		}
		switch (v.getId()) {
		case R.id.btnCH1:
			On_PopupWindow_click_Listener.btn_change_ch(0);
			break;
		case R.id.btnCH2:
			On_PopupWindow_click_Listener.btn_change_ch(1);
			break;
		case R.id.btnCH3:
			On_PopupWindow_click_Listener.btn_change_ch(2);
			break;
		case R.id.btnCH4:
			On_PopupWindow_click_Listener.btn_change_ch(3);
			break;
		case R.id.btnCH5:
			On_PopupWindow_click_Listener.btn_change_ch(4);
			break;
		case R.id.btnCH6:
			On_PopupWindow_click_Listener.btn_change_ch(5);
			break;
		case R.id.btnCH7:
			On_PopupWindow_click_Listener.btn_change_ch(6);
			break;
		case R.id.btnCH8:
			On_PopupWindow_click_Listener.btn_change_ch(7);
			break;
		case R.id.btnCH9:
			On_PopupWindow_click_Listener.btn_change_ch(8);
			break;
		case R.id.btnCH10:
			On_PopupWindow_click_Listener.btn_change_ch(9);
			break;
		case R.id.btnCH11:
			On_PopupWindow_click_Listener.btn_change_ch(10);
			break;
		case R.id.btnCH12:
			On_PopupWindow_click_Listener.btn_change_ch(11);
			break;
		case R.id.btnCH13:
			On_PopupWindow_click_Listener.btn_change_ch(12);
			break;
		case R.id.btnCH14:
			On_PopupWindow_click_Listener.btn_change_ch(13);
			break;
		case R.id.btnCH15:
			On_PopupWindow_click_Listener.btn_change_ch(14);
			break;
		case R.id.btnCH16:
			On_PopupWindow_click_Listener.btn_change_ch(15);
			break;
		case R.id.btnALL:
			On_PopupWindow_click_Listener.btn_change_ch(777);
			break;
		case R.id.btnMAX:
			On_PopupWindow_click_Listener.btn_change_quality(1);
			break;
		case R.id.btnMID:
			On_PopupWindow_click_Listener.btn_change_quality(3);
			break;
		case R.id.btnMIN:
			On_PopupWindow_click_Listener.btn_change_quality(5);
			break;
		case R.id.btnEnv50:
			On_PopupWindow_click_Listener.btn_change_env(0);
			break;
		case R.id.btnEnv60:
			On_PopupWindow_click_Listener.btn_change_env(1);
			break;
		case R.id.btnEnvOut:
			On_PopupWindow_click_Listener.btn_change_env(2);
			break;
		case R.id.btnEnvNight:
			On_PopupWindow_click_Listener.btn_change_env(3);
			break;
		case R.id.btnCH2_D:
			if (mAddMap.get(1)) {
				v.setBackgroundResource(R.drawable.btn_multi_ch);
			} else {
				v.setBackgroundResource(R.drawable.btn_channel_h);
			}
			mAddMap.put(1, !mAddMap.get(1));
			break;
		case R.id.btnCH3_D:
			if (mAddMap.get(2)) {
				v.setBackgroundResource(R.drawable.btn_multi_ch);
			} else {
				v.setBackgroundResource(R.drawable.btn_channel_h);
			}
			mAddMap.put(2, !mAddMap.get(2));
			break;
		case R.id.btnCH4_D:
			if (mAddMap.get(3)) {
				v.setBackgroundResource(R.drawable.btn_multi_ch);
			} else {
				v.setBackgroundResource(R.drawable.btn_channel_h);
			}
			mAddMap.put(3, !mAddMap.get(3));
			break;
		case R.id.btnCH5_D:
			if (mAddMap.get(4)) {
				v.setBackgroundResource(R.drawable.btn_multi_ch);
			} else {
				v.setBackgroundResource(R.drawable.btn_channel_h);
			}
			mAddMap.put(4, !mAddMap.get(4));
			break;
		case R.id.btnCH6_D:
			if (mAddMap.get(5)) {
				v.setBackgroundResource(R.drawable.btn_multi_ch);
			} else {
				v.setBackgroundResource(R.drawable.btn_channel_h);
			}
			mAddMap.put(5, !mAddMap.get(5));
			break;
		case R.id.btnCH7_D:
			if (mAddMap.get(6)) {
				v.setBackgroundResource(R.drawable.btn_multi_ch);
			} else {
				v.setBackgroundResource(R.drawable.btn_channel_h);
			}
			mAddMap.put(6, !mAddMap.get(6));
			break;
		case R.id.btnCH8_D:
			if (mAddMap.get(7)) {
				v.setBackgroundResource(R.drawable.btn_multi_ch);
			} else {
				v.setBackgroundResource(R.drawable.btn_channel_h);
			}
			mAddMap.put(7, !mAddMap.get(7));
			break;
		case R.id.btnCH9_D:
			if (mAddMap.get(8)) {
				v.setBackgroundResource(R.drawable.btn_multi_ch);
			} else {
				v.setBackgroundResource(R.drawable.btn_channel_h);
			}
			mAddMap.put(8, !mAddMap.get(8));
			break;
		case R.id.btnCH10_D:
			if (mAddMap.get(9)) {
				v.setBackgroundResource(R.drawable.btn_multi_ch);
			} else {
				v.setBackgroundResource(R.drawable.btn_channel_h);
			}
			mAddMap.put(9, !mAddMap.get(9));
			break;
		case R.id.btnCH11_D:
			if (mAddMap.get(10)) {
				v.setBackgroundResource(R.drawable.btn_multi_ch);
			} else {
				v.setBackgroundResource(R.drawable.btn_channel_h);
			}
			mAddMap.put(10, !mAddMap.get(10));
			break;
		case R.id.btnCH12_D:
			if (mAddMap.get(11)) {
				v.setBackgroundResource(R.drawable.btn_multi_ch);
			} else {
				v.setBackgroundResource(R.drawable.btn_channel_h);
			}
			mAddMap.put(11, !mAddMap.get(11));
			break;
		case R.id.btnCH13_D:
			if (mAddMap.get(12)) {
				v.setBackgroundResource(R.drawable.btn_multi_ch);
			} else {
				v.setBackgroundResource(R.drawable.btn_channel_h);
			}
			mAddMap.put(12, !mAddMap.get(12));
			break;
		case R.id.btnCH14_D:
			if (mAddMap.get(13)) {
				v.setBackgroundResource(R.drawable.btn_multi_ch);
			} else {
				v.setBackgroundResource(R.drawable.btn_channel_h);
			}
			mAddMap.put(13, !mAddMap.get(13));
			break;
		case R.id.btnCH15_D:
			if (mAddMap.get(14)) {
				v.setBackgroundResource(R.drawable.btn_multi_ch);
			} else {
				v.setBackgroundResource(R.drawable.btn_channel_h);
			}
			mAddMap.put(14, !mAddMap.get(14));
			break;
		case R.id.btnCH16_D:
			if (mAddMap.get(15)) {
				v.setBackgroundResource(R.drawable.btn_multi_ch);
			} else {
				v.setBackgroundResource(R.drawable.btn_channel_h);
			}
			mAddMap.put(15, !mAddMap.get(15));
			break;
		case R.id.btnCH1_D:
			if (mAddMap.get(0)) {
				v.setBackgroundResource(R.drawable.btn_multi_ch);
			} else {
				v.setBackgroundResource(R.drawable.btn_channel_h);
			}
			mAddMap.put(0, !mAddMap.get(0));
			break;
		case R.id.btnOK:
			On_PopupWindow_click_Listener.btn_add_monitor(mAddMap, mDeviceNo);
			break;
		case R.id.btnCancel:
			auto_dismis = true;
			break;
		}
		if (auto_dismis)
			popupWindow.dismiss();
	}

	public static void clearWindow(){
		if(popupWindow != null) {
			popupWindow.dismiss();
		}
	}

}
