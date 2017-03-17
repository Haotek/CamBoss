package addition.TUTK;

import android.app.ActionBar;
import android.app.Activity;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.TextView;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.Logger.Glog;
import com.tutk.P2PCam264.DELUX.Custom_Ok_Dialog;
import com.tutk.P2PCam264.DELUX.Custom_Ok_Dialog.DialogListener;
import com.tutk.P2PCam264.MyCamera;
import com.tutk.Kalay.general.R;

import appteam.WifiAdmin;
import general.CheckDevListener;
import general.EasyWiFiSetting;

public class AddDeviceCheckActivity extends Activity implements DialogListener, CheckDevListener {

    private final int DIALOG_CHANGE_WIFI = 0;
    private final int DIALOG_BACK = 1;
    public final static int ADD_MODE_AP = 0;
    public final static int ADD_MODE_SET_WIFI = 1;
    private final static String TAG = "EasyWiFi";

    private EasyWiFiSetting mEasyWiFi;
    private MyCamera mCamera = null;
    private WifiAdmin WifiAdmin;
    private Handler handler = new Handler();

    private String mDeviceName;
    private String mDeviceUID;
    private String mDevicePWD;
    private String mDevAP;
    private String mDevAPPwd;
    private String mWifiSSID;
    private String mWifiPassword;
    private boolean mSetWiFi = false;
    private boolean mIsItent = false;
    private int wifi_enc = 4;
    private int add_mode = 0;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.titlebar);
        TextView tv = (TextView) this.findViewById(R.id.bar_text);
        tv.setText(getText(R.string.txtAddCamera));

        setContentView(R.layout.add_device_check_wifi_setting);

        mDeviceName = getIntent().getStringExtra("name");
        mDeviceUID = getIntent().getStringExtra("uid");
        mDevicePWD = getIntent().getStringExtra("pwd");
        mWifiSSID = getIntent().getStringExtra("ssid");
        mWifiPassword = getIntent().getStringExtra("wifi_pwd");
        wifi_enc = getIntent().getIntExtra("enc", AVIOCTRLDEFs.AVIOTC_WIFIAPENC_WPA_AES);
        mDevAP = getIntent().getStringExtra("ap");
        mDevAPPwd = getIntent().getStringExtra("ap_pwd");
        add_mode = getIntent().getIntExtra("mode", ADD_MODE_AP);

        CheckDevListener listener = this;
        mEasyWiFi = new EasyWiFiSetting(this, listener, mDeviceName, mDeviceUID, mDevicePWD, mDevAP, mDevAPPwd, mWifiSSID, mWifiPassword);
        WifiAdmin = new WifiAdmin(this);
        if (WifiAdmin.checkState() != WifiManager.WIFI_STATE_ENABLED) {
            Glog.I(TAG, "open WiFi");
            WifiAdmin.openWifi();
        }


        handler.postDelayed(new Runnable() {
            @Override
            public void run () {
                if(WifiAdmin.checkState() == WifiManager.WIFI_STATE_ENABLED){
                    if (add_mode == ADD_MODE_AP) {
                        addInAPMode();
                    } else {
                        mEasyWiFi.startSetWiFi();
                    }
                }else{
                    handler.postDelayed(this, 2000);
                }
            }
        }, 5000);

    }

    private void addInAPMode () {
        if (ChangeWiFi()) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run () {
                    if(WifiAdmin.isConnect()){
                        setResult(EasyWiFiSetting.RESP_CODE_ADD);
                        finish();
                        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                    }else{
                        handler.postDelayed(this, 2000);
                    }
                }
            }, 5000);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run () {
                    Custom_Ok_Dialog dlg = new Custom_Ok_Dialog(AddDeviceCheckActivity.this, getText(R.string.tips_cant_connect_AP).toString(),
                            getText(R.string.ok).toString());
                    dlg.setCanceledOnTouchOutside(false);
                    Window window = dlg.getWindow();
                    window.setWindowAnimations(R.style.setting_dailog_animstyle);
                    dlg.show();
                }
            });
        }
    }

    @Override
    protected void onResume () {
        super.onResume();
        Custom_Ok_Dialog.registDialogListener(this);
    }

    @Override
    protected void onPause () {
        super.onPause();
        if (mEasyWiFi != null) {
            mEasyWiFi.quit(mIsItent);
        }
    }

    private boolean ChangeWiFi () {

        Glog.I(TAG, "Change WiFi = " + mDevAP + "pwd = " + mDevAPPwd);
        boolean hasWifi = false;
        WifiAdmin.startScan();
        for (String search : WifiAdmin.getWifiListsSSID()) {
            if (search.equals(mDevAP)) {
                hasWifi = true;
                break;
            }
        }

        if (! hasWifi) {
            Glog.I(TAG, "Can not scan the SSID");
            return false;
        }

        WifiConfiguration WifiConfiguration = null;

        if (mDevAPPwd.length() == 0) {
            WifiConfiguration = WifiAdmin.CreateWifiInfo(mDevAP, "", 1);
        } else {
            WifiConfiguration = WifiAdmin.CreateWifiInfo(mDevAP, mDevAPPwd, 3);
        }
        if (WifiConfiguration != null) {
            WifiAdmin.openWifi();
            if (! WifiAdmin.addNetwork(WifiConfiguration)) {
                Glog.I(TAG, "Can not ADD the SSID");
                return false;
            } else {
                Glog.I(TAG, "Change WiFi success");
                return true;
            }
        } else {
            Glog.I(TAG, "WiFiAdmin err");
            return false;
        }
    }

    @Override
    public void click (int request) {
        if (request == DIALOG_CHANGE_WIFI) {
            setResult(EasyWiFiSetting.RESP_CODE_RECONNECT);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
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

    @Override
    public void getCheckingErr (int result) {
        final int fResult = result;
        runOnUiThread(new Runnable() {
            @Override
            public void run () {
                switch (fResult) {
                    case CheckDevListener.RESULT_UNKNOWN:
                        Custom_Ok_Dialog dlg = new Custom_Ok_Dialog(AddDeviceCheckActivity.this, getText(R.string.tips_check_unknown).toString(),
                                getText(R.string.ok).toString(), EasyWiFiSetting.DIALOG_BACK);
                        dlg.setCanceledOnTouchOutside(false);
                        Window window = dlg.getWindow();
                        window.setWindowAnimations(R.style.setting_dailog_animstyle);
                        dlg.show();
                        break;

                    case CheckDevListener.RESULT_FAILED:
                        dlg = new Custom_Ok_Dialog(AddDeviceCheckActivity.this, getText(R.string.tips_check_failed).toString(), getText(R.string
                                .ok).toString(), EasyWiFiSetting.DIALOG_BACK);
                        dlg.setCanceledOnTouchOutside(false);
                        window = dlg.getWindow();
                        window.setWindowAnimations(R.style.setting_dailog_animstyle);
                        dlg.show();
                        break;

                    case CheckDevListener.RESULT_WRONG_PWD:
                        dlg = new Custom_Ok_Dialog(AddDeviceCheckActivity.this, getText(R.string.tips_pws_not_correct).toString(), getText(R.string
                                .ok).toString(), EasyWiFiSetting.DIALOG_CHANGE_WIFI);
                        dlg.setCanceledOnTouchOutside(false);
                        window = dlg.getWindow();
                        window.setWindowAnimations(R.style.setting_dailog_animstyle);
                        dlg.show();
                        break;

                    case CheckDevListener.RESULT_FAILED_TO_SET:
                        dlg = new Custom_Ok_Dialog(AddDeviceCheckActivity.this, getText(R.string.tips_failed_set_wifi).toString(), getText(R.string
                                .ok).toString(), EasyWiFiSetting.DIALOG_BACK);
                        dlg.setCanceledOnTouchOutside(false);
                        window = dlg.getWindow();
                        window.setWindowAnimations(R.style.setting_dailog_animstyle);
                        dlg.show();
                        break;

                    case CheckDevListener.RESULT_FAILED_IN_LAN:
                    case CheckDevListener.RESULT_NETWORK_UNREACHABLE:
                        dlg = new Custom_Ok_Dialog(AddDeviceCheckActivity.this, getText(R.string.tips_failed_create_session).toString(), getText(R
                                .string.ok).toString(), EasyWiFiSetting.DIALOG_CHANGE_WIFI);
                        dlg.setCanceledOnTouchOutside(false);
                        window = dlg.getWindow();
                        window.setWindowAnimations(R.style.setting_dailog_animstyle);
                        dlg.show();
                        break;

                    case CheckDevListener.RESULT_CANNOT_CHANGE_TO_AP:
                        dlg = new Custom_Ok_Dialog(AddDeviceCheckActivity.this, getText(R.string.tips_cant_connect_AP).toString(), getText(R.string
                                .ok).toString(), EasyWiFiSetting.DIALOG_CHANGE_WIFI);
                        dlg.setCanceledOnTouchOutside(false);
                        window = dlg.getWindow();
                        window.setWindowAnimations(R.style.setting_dailog_animstyle);
                        dlg.show();
                        break;

                    case CheckDevListener.RESULT_FAILED_TO_CREATE_CHANNEL:
                        dlg = new Custom_Ok_Dialog(AddDeviceCheckActivity.this, getText(R.string.tips_failed_create_channel).toString(), getText(R
                                .string.ok).toString(), EasyWiFiSetting.DIALOG_CHANGE_WIFI);
                        dlg.setCanceledOnTouchOutside(false);
                        window = dlg.getWindow();
                        window.setWindowAnimations(R.style.setting_dailog_animstyle);
                        dlg.show();
                        break;

                    case CheckDevListener.RESULT_CANNOT_GET_WIFI_LIST:
                        dlg = new Custom_Ok_Dialog(AddDeviceCheckActivity.this, getText(R.string.tips_failed_get_wifilist).toString(), getText(R
                                .string.ok).toString(), EasyWiFiSetting.DIALOG_CHANGE_WIFI);
                        dlg.setCanceledOnTouchOutside(false);
                        window = dlg.getWindow();
                        window.setWindowAnimations(R.style.setting_dailog_animstyle);
                        dlg.show();
                        break;
                }
            }
        });
    }

    @Override
    public void getConnected (MyCamera camera) {
        camera.disconnect();

        handler.postDelayed(new Runnable() {
            @Override
            public void run () {
                setResult(EasyWiFiSetting.RESP_CODE_ADD);
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
            }
        }, 1500);
    }
}
