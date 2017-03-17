package com.tutk.P2PCam264.DELUX;

import com.tutk.Kalay.general.R;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Custom_OkCancle_Dialog extends AlertDialog {
	private Context mContext;
	private static OkCancelDialogListener dialogListener;

	@SuppressWarnings("deprecation")
	public Custom_OkCancle_Dialog(Context context, String contentText) {
		super(context);

		mContext = context;

		LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = mInflater.inflate(R.layout.ok_cancle_dialog, null);

		setView(view);

		TextView tvText = (TextView) view.findViewById(R.id.tvText);
		Button btnOK = (Button) view.findViewById(R.id.btnOK);
		Button btnCancel = (Button) view.findViewById(R.id.btnCancel);

		tvText.setText(contentText);
		btnOK.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
				if (dialogListener != null) {
					dialogListener.ok();
				}				
			}
		});
		btnCancel.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
				if (dialogListener != null) {
					dialogListener.cancel();
				}				
			}
		});
	}

	public static void SetDialogListener(OkCancelDialogListener listener) {
		dialogListener = listener;
	}

	public interface OkCancelDialogListener {
		public void ok();

		public void cancel();
	}
}
