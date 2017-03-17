package com.tutk.P2PCam264.DELUX;

import com.tutk.IOTC.Monitor;
import com.tutk.P2PCam264.DeviceInfo;
import com.tutk.Kalay.general.R;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MultiViewStatusBar {

	View Layout = null;
	ImageView icon_status;
	// Button Settingbutton;
	TextView txt_name;
	TextView txt_unline;

	public MultiViewStatusBar(View view) {
		if (view == null)
			return;
		Layout = view;
		icon_status = (ImageView) Layout.findViewById(R.id.icon_status);
		// Settingbutton = (Button) Layout.findViewById(R.id.Settingbutton);
		txt_name = (TextView) Layout.findViewById(R.id.txt_name);
		txt_unline = (TextView) Layout.findViewById(R.id.txt_unline);

	}

	public void InitStatusBar(int Index, DeviceInfo device, View.OnClickListener ClickListener) {
		if (Layout == null)
			return;
		// �|���j�wcamera�ܤ��εe��
		if (device == null) {
			Layout.setVisibility(View.GONE);
			return;
		}
		if (txt_name != null) {
			// Settingbutton.setTag((Object)Index);
			// Settingbutton.setOnClickListener(ClickListener);
			txt_name.setText(device.NickName);
			txt_unline.setText(device.Status);
		}
		
		if(icon_status != null){
			if (device.Online) {
				icon_status.setBackgroundResource(R.drawable.ic_name_g);
			}else {
				icon_status.setBackgroundResource(R.drawable.ic_name_gr);
			}
		}

	}

	public void ChangeStatusBar(int Index, DeviceInfo device, Monitor monitor, LiveviewFragment liveviewFragment) {
		if (Layout == null )
			return;
		if (icon_status != null && txt_name != null && txt_unline != null) { 
			// �|���j�wcamera�ܤ��εe��
			if (device == null) { 
				Layout.setVisibility(View.GONE);
				return;
			} else {
				Layout.setVisibility(View.VISIBLE);
			}
			txt_name.setText(device.NickName);
			if (device.Online) {
				icon_status.setBackgroundResource(R.drawable.ic_name_g);
				// Settingbutton.setVisibility(View.VISIBLE);
				txt_unline.setVisibility(View.GONE);
				txt_unline.setText("");
				if (device != null) {
					monitor.setVisibility(View.VISIBLE);
				}
			} else { 
				if (device.Status.equals(liveviewFragment.getString(R.string.connstus_wrong_password))
						|| device.Status.equals(liveviewFragment.getString(R.string.connstus_unknown_device)))
					icon_status.setBackgroundResource(R.drawable.ic_name_r);
				else
					icon_status.setBackgroundResource(R.drawable.ic_name_gr);
				// Settingbutton.setVisibility(View.GONE);
				txt_unline.setVisibility(View.VISIBLE);
				txt_unline.setText(device.Status);
				if (monitor != null) {
					monitor.setVisibility(View.GONE);
				}
			}
		}

	}
}
