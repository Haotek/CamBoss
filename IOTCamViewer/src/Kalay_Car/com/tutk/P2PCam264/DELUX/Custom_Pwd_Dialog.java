package com.tutk.P2PCam264.DELUX;

import com.tutk.Kalay.general.R;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Custom_Pwd_Dialog extends AlertDialog {
	private Context mContext;
	private static PwdDialogListener dialogListener;

	@SuppressWarnings("deprecation")
	public Custom_Pwd_Dialog(Context context, String contentText) {
		super(context);

		mContext = context;

		LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = mInflater.inflate(R.layout.pwd_dialog, null);

		setView(view);

		TextView tvText = (TextView) view.findViewById(R.id.tvText);
		final EditText etPwd = (EditText) view.findViewById(R.id.etPwd);
		Button btnOK = (Button) view.findViewById(R.id.btnOK);
		Button btnCancel = (Button) view.findViewById(R.id.btnCancel);

		tvText.setText(contentText);
		btnOK.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dialogListener != null) {
					dialogListener.ok(etPwd.getText().toString());
				}
				dismiss();
			}
		});
		btnCancel.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}

	public static void SetDialogListener(PwdDialogListener listener) {
		dialogListener = listener;
	}

	public interface PwdDialogListener {
		public void ok(String password);

	}
}
