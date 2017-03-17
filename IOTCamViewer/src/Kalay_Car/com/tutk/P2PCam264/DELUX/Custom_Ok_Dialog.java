package com.tutk.P2PCam264.DELUX;

import com.tutk.Kalay.general.R;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Custom_Ok_Dialog extends AlertDialog {

	private Context mContext;
	private static DialogListener dialogListener;

	@SuppressWarnings("deprecation")
	public Custom_Ok_Dialog(Context context, String contentText, String buttonText) {
		super(context);

		mContext = context;

		LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = mInflater.inflate(R.layout.ok_dialog, null);

		setView(view);

		TextView tvText = (TextView) view.findViewById(R.id.tvText);
		Button btnSingle = (Button) view.findViewById(R.id.btnSingle);

		tvText.setText(contentText);
		btnSingle.setText(buttonText);
		btnSingle.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(dialogListener != null)
					dialogListener.click(-1);
				dismiss();
			}
		});

	}
	
	@SuppressWarnings("deprecation")
	public Custom_Ok_Dialog(Context context, String contentText, String buttonText, final int request) {
		super(context);

		mContext = context;

		LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = mInflater.inflate(R.layout.ok_dialog, null);

		setView(view);

		TextView tvText = (TextView) view.findViewById(R.id.tvText);
		Button btnSingle = (Button) view.findViewById(R.id.btnSingle);

		tvText.setText(contentText);
		btnSingle.setText(buttonText);
		btnSingle.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(dialogListener != null)
					dialogListener.click(request);
				dismiss();
			}
		});

	}

	public static void registDialogListener(DialogListener listener) {
		dialogListener = listener;
	}

	public interface DialogListener {
		public void click(int request);
	}
}
