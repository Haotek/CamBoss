package com.tutk.P2PCam264.DELUX;

import com.tutk.Kalay.general.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

public class ServiceDialog extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		
		this.getWindow().addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		this.getWindow().addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD);
		this.getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);		
		this.getWindow().addFlags(LayoutParams.FLAG_TURN_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
//		Custom_OkCancle_Dialog dlg = new Custom_OkCancle_Dialog(ServiceDialog.this, getText(R.string.tips_remove_camera_monitor_confirm)
//				.toString());
//		dlg.setCanceledOnTouchOutside(true);
//		Window window = dlg.getWindow();
////		window.setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
//		window.setWindowAnimations(R.style.setting_dailog_animstyle);
//		window.addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD);
//		window.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
//		window.addFlags(LayoutParams.FLAG_TURN_SCREEN_ON);
//		dlg.show();
		
		setContentView(R.layout.ok_cancle_dialog);

		TextView tvText = (TextView) findViewById(R.id.tvText);
		Button btnOK = (Button) findViewById(R.id.btnOK);
		Button btnCancel = (Button) findViewById(R.id.btnCancel);

		tvText.setText("XXX");
		
		btnOK.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(ServiceDialog.this, MultiViewActivity.class));
				overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			}
		});
//		btnCancel.setOnClickListener(new Button.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				dismiss();
//				if (dialogListener != null) {
//					dialogListener.cancel();
//				}				
//			}
//		});
	}

}
