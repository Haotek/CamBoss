package com.tutk.P2PCam264.DELUX;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.tutk.Kalay.general.R;

/**
 * Created by James Huang on 2015/4/3.
 */
public class CloudRecordingActivity extends SherlockActivity {
    @Override
    protected void onCreate (Bundle savedInstanceState) {

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.titlebar);
        TextView tv = (TextView)this.findViewById(R.id.bar_text);
        tv.setText(getText(R.string.txt_cloud_recording));

        super.onCreate(savedInstanceState);

        setContentView(R.layout.cloud_recording_activity);
    }

    @Override
    public boolean onKeyDown (int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                return false;
        }

        return super.onKeyDown(keyCode, event);
    }
}
