package com.tutk.P2PCam264.DELUX;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Switch;
import android.widget.TextView;

import com.tutk.Kalay.general.R;

public class PushSettingActivity extends Activity {
	
	private Switch swPush;
	private SharedPreferences settings;
	private boolean orgSettings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
		actionBar.setCustomView(R.layout.titlebar);
		TextView tv = (TextView)this.findViewById(R.id.bar_text);
		tv.setText(getText(R.string.txt_Noti_settings));
		
		setContentView(R.layout.push_setting);
		
		swPush = (Switch) findViewById(R.id.swPush);
		
		settings = this.getSharedPreferences("Push Setting", 0);
		orgSettings = settings.getBoolean("settings", false);
		swPush.setChecked(orgSettings);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode){
		case KeyEvent.KEYCODE_BACK:				
			if(orgSettings != swPush.isChecked()){
				settings.edit().putBoolean("settings", swPush.isChecked()).commit();
				
				Intent intent = new Intent();
				intent.putExtra("settings", swPush.isChecked());
				setResult(RESULT_OK, intent);
				finish();
				overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
			}else {				
				setResult(RESULT_CANCELED);
				finish();
				overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
			}			
			break;
		}		
		return super.onKeyDown(keyCode, event);
	}
}
