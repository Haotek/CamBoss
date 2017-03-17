package com.tutk.P2PCam264;

import android.app.AlertDialog;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tutk.IOTC.AVAPIs;
import com.tutk.IOTC.IOTCAPIs;
import com.tutk.Kalay.general.R;

public class AboutDialog extends AlertDialog implements DialogInterface.OnClickListener {
	Context mContext;

	@SuppressWarnings("deprecation")
	public AboutDialog(Context context, String title, String appVersion) {
		super(context);

		mContext = context;

		LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = mInflater.inflate(R.layout.about, null);

		setView(view);
//		setTitle(title);
//		setButton(context.getText(R.string.ok), this);

		TextView txtVersion = (TextView) view.findViewById(R.id.txtVersion);
		TextView txtIOTCAPIs = (TextView) view.findViewById(R.id.txtIOTCAPIs);
		TextView txtAVAPIs = (TextView) view.findViewById(R.id.txtAVAPIs);
		Button btnOK = (Button) view.findViewById(R.id.btnOK);

		txtVersion.setText(appVersion);
		txtIOTCAPIs.setText(getIOTCAPis());
		txtAVAPIs.setText(getAVAPis());
		btnOK.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();

			}
		});

	}

	private String getIOTCAPis() {

		byte[] bytVer = new byte[4];
		int[] lVer = new int[1];
		int ver;

		IOTCAPIs.IOTC_Get_Version(lVer);

		ver = (int) lVer[0];

		StringBuffer sb = new StringBuffer();
		bytVer[3] = (byte) (ver);
		bytVer[2] = (byte) (ver >>> 8);
		bytVer[1] = (byte) (ver >>> 16);
		bytVer[0] = (byte) (ver >>> 24);
		sb.append((int) (bytVer[0] & 0xff));
		sb.append('.');
		sb.append((int) (bytVer[1] & 0xff));
		sb.append('.');
		sb.append((int) (bytVer[2] & 0xff));
		sb.append('.');
		sb.append((int) (bytVer[3] & 0xff));

		return sb.toString();
	}

	private String getAVAPis() {

		byte[] bytVer = new byte[4];
		int ver = AVAPIs.avGetAVApiVer();

		StringBuffer sb = new StringBuffer();
		bytVer[3] = (byte) (ver);
		bytVer[2] = (byte) (ver >>> 8);
		bytVer[1] = (byte) (ver >>> 16);
		bytVer[0] = (byte) (ver >>> 24);
		sb.append((int) (bytVer[0] & 0xff));
		sb.append('.');
		sb.append((int) (bytVer[1] & 0xff));
		sb.append('.');
		sb.append((int) (bytVer[2] & 0xff));
		sb.append('.');
		sb.append((int) (bytVer[3] & 0xff));

		return sb.toString();
	}

	@Override
	public void onClick(DialogInterface arg0, int arg1) {
		// TODO Auto-generated method stub
	}
}