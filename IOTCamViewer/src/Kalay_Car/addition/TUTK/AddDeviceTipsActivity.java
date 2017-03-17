package addition.TUTK;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.P2PCam264.DELUX.MultiViewActivity;
import com.tutk.P2PCam264.DeviceInfo;
import com.tutk.P2PCam264.MyCamera;
import com.tutk.Kalay.general.R;

/**
 * Created by James Huang on 2015/3/22.
 */
public class AddDeviceTipsActivity extends Activity {

    public static final int REQUEST_CODE_CAMERA_ADD = 0;

    private int mMonitorIndex = 0;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.titlebar);
        TextView tv = (TextView) this.findViewById(R.id.bar_text);
        tv.setText(getText(R.string.txtAddCamera));

        setContentView(R.layout.add_device_tips);

        mMonitorIndex = getIntent().getExtras().getInt("MonitorIndex");

        Button btnOK = (Button) findViewById(R.id.btnOK);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                Intent intent = new Intent(AddDeviceTipsActivity.this, qr_codeActivity.class);
                startActivityForResult(intent, REQUEST_CODE_CAMERA_ADD);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CAMERA_ADD) {

            switch (resultCode) {
                case RESULT_OK:

                    Bundle extras = data.getExtras();
                    long db_id = extras.getLong("db_id");
                    String dev_nickname = extras.getString("dev_nickname");
                    String dev_uid = extras.getString("dev_uid");
                    String view_acc = extras.getString("view_acc");
                    String view_pwd = extras.getString("view_pwd");
                    String dev_ap = extras.getString("dev_ap");
                    String dev_ap_pwd = extras.getString("dev_ap_pwd");
                    String wifi = extras.getString("wifi_ssid");
                    String wifi_pwd = extras.getString("wifi_pwd");
                    int event_notification = 3;
                    int channel = extras.getInt("camera_channel");

                    MyCamera camera = new MyCamera(dev_nickname, dev_uid, view_acc, view_pwd);
                    DeviceInfo dev = new DeviceInfo(db_id, camera.getUUID(), dev_nickname, dev_uid, view_acc, view_pwd, "", event_notification, channel,
                            null, dev_ap, dev_ap_pwd, wifi, wifi_pwd);
                    MultiViewActivity.DeviceList.add(dev);

                    camera.registerIOTCListener(MultiViewActivity.getMultiViewActivityIRegisterIOTCListener());
                    camera.connect(dev_uid);
                    camera.start(MyCamera.DEFAULT_AV_CHANNEL, view_acc, view_pwd);
                    camera.sendIOCtrl(MyCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_REQ,
                            AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoReq.parseContent());
                    camera.sendIOCtrl(MyCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETSUPPORTSTREAM_REQ,
                            AVIOCTRLDEFs.SMsgAVIoctrlGetSupportStreamReq.parseContent());
                    camera.sendIOCtrl(MyCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETAUDIOOUTFORMAT_REQ,
                            AVIOCTRLDEFs.SMsgAVIoctrlGetAudioOutFormatReq.parseContent());
                    camera.sendIOCtrl(MyCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIMEZONE_REQ,
                            AVIOCTRLDEFs.SMsgAVIoctrlTimeZone.parseContent());

                    camera.LastAudioMode = 1;

                    MultiViewActivity.CameraList.add(camera);

                    Bundle NEWextras = new Bundle();
                    NEWextras.putString("dev_uid", dev.UID);
                    NEWextras.putString("dev_uuid", dev.UUID);
                    NEWextras.putString("dev_nickname", dev.NickName);
                    NEWextras.putString("conn_status", dev.Status);
                    NEWextras.putString("view_acc", dev.View_Account);
                    NEWextras.putString("view_pwd", dev.View_Password);
                    // ADD DEVICE CLANNEL IS ALWAYS 0
                    NEWextras.putInt("camera_channel", MyCamera.DEFAULT_AV_CHANNEL);
                    NEWextras.putInt("MonitorIndex", mMonitorIndex);
                    Intent Intent = new Intent();
                    Intent.putExtras(NEWextras);
                    setResult(RESULT_OK, Intent);
                    this.finish();

                    break;
            }
        }
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
