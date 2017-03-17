package addition.TUTK;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.URLUtil;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.Kalay.general.R;
import com.tutk.Logger.Glog;
import com.tutk.P2PCam264.DELUX.Custom_Ok_Dialog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import appteam.WifiAdmin;
import general.DatabaseManager;
import general.EasyWiFiSetting;
import general.ThreadTPNS;


public class AddDeviceActivity extends Activity implements OnClickListener {

    private final String TAG = "AddDeviceActivity";
    public final static int REQUEST_CODE_GETUID_BY_SCAN_BARCODE = 0;
    public final static int REQUEST_CODE_SELECT_AP = 1;
    public final static int REQUEST_CODE_CHECK_DEVICE = 2;
    public final static int REQUEST_CODE_NFC = 3;
    public final static int REQUEST_CODE_NFC_CONNECTING = 4;
    public final static int REQUEST_CODE_ADDUID_ONLY = 5;

    public static final int ADD_DEVICE_FROM_CLOUD = 0;
    public static final int ADD_DEVICE_FROM_WIRED = 1;
    public static final int ADD_DEVICE_FROM_WIRELESS = 2;

    private EditText mUIDEdtTxt;
    private EditText mSecurityEdtTxt;
    private EditText mNameEdtTxt;
    private LinearLayout layoutAP;
    private LinearLayout layoutWIFI;
    private LinearLayout layoutWIFISSID;
    private LinearLayout layoutWIFIPwd;
    private LinearLayout layoutWIFISettings;
    private RelativeLayout layoutSettings;
    private ImageView imgArrow;
    private TextView tvTips;
    private EditText edtWifiPWD;
    private TextView tvSSID;
    private EditText edtAPpwd;
    private EditText edtAPSSID;
    private ResultStateReceiver mResultStateReceiver;
    private ScrollView mScrollView;
    private Button btnOK;

    private int wifi_enc = 4;
    private String mUID = null;
    private String mDevAP = null;
    private String mDevAPPwd = null;
    private String mType = "IP Camera";
    private String mWifiSSID;
    private String mWifiPassword;
    private boolean mShowSettings = false;

    private Handler handler = new Handler();

    private WifiAdmin WifiAdmin;
//	private ConnectionChangeReceiver mNetworkStateReceiver;

    private List<SearchResult> list = new ArrayList<SearchResult>();

    private enum MyMode {
        LAN, SETWIFI
    }

    private MyMode mMode = MyMode.LAN;

    private int mMonitorIndex = 0;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMonitorIndex = getIntent().getExtras().getInt("MonitorIndex");

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.titlebar);
        TextView tv = (TextView) this.findViewById(R.id.bar_text);
        tv.setText(getText(R.string.txtAddCamera));

        setContentView(R.layout.add_device);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AddDeviceActivity.class.getName());
        mResultStateReceiver = new ResultStateReceiver();

        registerReceiver(mResultStateReceiver, intentFilter);
        mScrollView = (ScrollView) findViewById(R.id.uidlayout);

        mUIDEdtTxt = (EditText) findViewById(R.id.edtUID);
        if (mUID != null) {
            if (mUID.length() > 20) {
                String temp = "";

                for (int t = 0 ; t < mUID.length() ; t++) {
                    if (mUID.substring(t, t + 1).matches("[A-Z0-9]{1}")) {
                        temp += mUID.substring(t, t + 1);
                    }
                }
                mUID = temp;
            }

            mUIDEdtTxt.setText(mUID);
            mUIDEdtTxt.setEnabled(false);
        }

        mSecurityEdtTxt = (EditText) findViewById(R.id.edtSecurityCode);
        mNameEdtTxt = (EditText) findViewById(R.id.edtNickName);
        layoutAP = (LinearLayout) findViewById(R.id.layoutAP);
        layoutWIFI = (LinearLayout) findViewById(R.id.layoutWIFI);
        layoutWIFISSID = (LinearLayout) findViewById(R.id.layoutWIFISSID);
        layoutWIFIPwd = (LinearLayout) findViewById(R.id.layoutWIFIPwd);
        edtAPpwd = (EditText) findViewById(R.id.edtAPPWD);
        edtAPSSID = (EditText) findViewById(R.id.edtAPSSID);
        edtWifiPWD = (EditText) findViewById(R.id.edtWifiPWD);
        tvSSID = (TextView) findViewById(R.id.tvSSID);
        btnOK = (Button) findViewById(R.id.btnOK);
        layoutWIFISettings = (LinearLayout) findViewById(R.id.layoutWIFISettings);
        layoutSettings = (RelativeLayout) findViewById(R.id.layoutSettings);
        imgArrow = (ImageView) findViewById(R.id.imgArrow);
        tvTips = (TextView) findViewById(R.id.tvTips);

        mSecurityEdtTxt.setText("888888");
        setFocusOnEdit();
        setCheckOnEdit();
        layoutAP.setVisibility(View.INVISIBLE);
        layoutWIFI.setVisibility(View.INVISIBLE);
        layoutWIFISettings.setVisibility(View.INVISIBLE);
        btnOK.setOnClickListener(this);
        layoutSettings.setOnClickListener(this);

       /*
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            String mScan[] = bundle.getString("SCAN_RESULT").split("/");
            mUID = mScan[0];
            if (mScan.length > 2) {
                mDevAP = mScan[1];
                mDevAPPwd = mScan[2];
            } else if (mScan.length > 1) {
                mDevAP = mScan[1];
            }
        }

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.titlebar);
        TextView tv = (TextView) this.findViewById(R.id.bar_text);
        tv.setText(getText(R.string.txtAddCamera));

        setContentView(R.layout.add_device);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AddDeviceActivity.class.getName());
        mResultStateReceiver = new ResultStateReceiver();

        registerReceiver(mResultStateReceiver, intentFilter);
        mScrollView = (ScrollView) findViewById(R.id.uidlayout);

        mUIDEdtTxt = (EditText) findViewById(R.id.edtUID);
        if (mUID != null) {
            if (mUID.length() > 20) {
                String temp = "";

                for (int t = 0 ; t < mUID.length() ; t++) {
                    if (mUID.substring(t, t + 1).matches("[A-Z0-9]{1}")) {
                        temp += mUID.substring(t, t + 1);
                    }
                }
                mUID = temp;
            }

            mUIDEdtTxt.setText(mUID);
            mUIDEdtTxt.setEnabled(false);
        }

        mSecurityEdtTxt = (EditText) findViewById(R.id.edtSecurityCode);
        mNameEdtTxt = (EditText) findViewById(R.id.edtNickName);
        layoutAP = (LinearLayout) findViewById(R.id.layoutAP);
        layoutWIFI = (LinearLayout) findViewById(R.id.layoutWIFI);
        layoutWIFISSID = (LinearLayout) findViewById(R.id.layoutWIFISSID);
        layoutWIFIPwd = (LinearLayout) findViewById(R.id.layoutWIFIPwd);
        edtAPpwd = (EditText) findViewById(R.id.edtAPPWD);
        edtAPSSID = (EditText) findViewById(R.id.edtAPSSID);
        edtWifiPWD = (EditText) findViewById(R.id.edtWifiPWD);
        tvSSID = (TextView) findViewById(R.id.tvSSID);
        btnOK = (Button) findViewById(R.id.btnOK);
        layoutWIFISettings = (LinearLayout) findViewById(R.id.layoutWIFISettings);
        layoutSettings = (RelativeLayout) findViewById(R.id.layoutSettings);
        imgArrow = (ImageView) findViewById(R.id.imgArrow);
        tvTips = (TextView) findViewById(R.id.tvTips);

        setFocusOnEdit();
        setCheckOnEdit();
        layoutWIFI.setVisibility(View.GONE);
        layoutWIFISettings.setVisibility(View.GONE);
        layoutWIFI.setVisibility(View.INVISIBLE);
        layoutWIFISettings.setVisibility(View.INVISIBLE);
        btnOK.setOnClickListener(this);
        layoutSettings.setOnClickListener(this);

        if (mDevAP != null) {
            edtAPSSID.setText(mDevAP);
            layoutAP.setVisibility(View.GONE);
            if (mDevAPPwd != null) {
                edtAPpwd.setText(mDevAPPwd);
            }
        }

        WifiAdmin = new WifiAdmin(this);
        WifiAdmin.openWifi();
        if (WifiAdmin.isWifi()) {
            String ssid = WifiAdmin.getSSID().toString().replace("\"", "");
            tvSSID.setText(ssid);

            if (mDevAP != null) {
                if (! ssid.trim().equals(mDevAP.trim())) {
                    mMode = MyMode.SETWIFI;
//                    layoutWIFI.setVisibility(View.VISIBLE);
                    layoutWIFISettings.setVisibility(View.VISIBLE);
                    edtWifiPWD.setEnabled(true);
                } else {
                    tvSSID.setText("");
                }
            } else {
                mMode = MyMode.SETWIFI;
                layoutWIFI.setVisibility(View.VISIBLE);
                edtWifiPWD.setEnabled(true);
            }

            String enc = WifiAdmin.getEncType(ssid);
            if (enc.length() != 0) {
                if (enc.contains("WPA2")) {
                    wifi_enc = AVIOCTRLDEFs.AVIOTC_WIFIAPENC_WPA2_AES;
                } else {
                    wifi_enc = AVIOCTRLDEFs.AVIOTC_WIFIAPENC_WPA_AES;
                }
            } else {
                wifi_enc = AVIOCTRLDEFs.AVIOTC_WIFIAPENC_NONE;
            }
        }
*/
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void setFocusOnEdit () {
        edtAPSSID.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange (View v, boolean hasFocus) {
                if (hasFocus) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run () {
                            DisplayMetrics metrics = new DisplayMetrics();
                            getWindowManager().getDefaultDisplay().getMetrics(metrics);
                            mScrollView.scrollTo(0, metrics.heightPixels);
                        }
                    }, 350);
                }
            }
        });
        edtAPSSID.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick (View v) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run () {
                        DisplayMetrics metrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(metrics);
                        mScrollView.scrollTo(0, metrics.heightPixels);
                    }
                }, 350);
            }
        });
        edtAPpwd.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DisplayMetrics metrics = new DisplayMetrics();
                            getWindowManager().getDefaultDisplay().getMetrics(metrics);
                            mScrollView.scrollTo(0, metrics.heightPixels);
                        }
                    }, 350);
                }
            }
        });
        edtAPpwd.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        DisplayMetrics metrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(metrics);
                        mScrollView.scrollTo(0, metrics.heightPixels);
                    }
                }, 350);
            }
        });
        edtWifiPWD.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DisplayMetrics metrics = new DisplayMetrics();
                            getWindowManager().getDefaultDisplay().getMetrics(metrics);
                            mScrollView.scrollTo(0, metrics.heightPixels);
                        }
                    }, 350);
                }
            }
        });
        edtWifiPWD.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        DisplayMetrics metrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(metrics);
                        mScrollView.scrollTo(0, metrics.heightPixels);
                    }
                }, 350);
            }
        });
        tvSSID.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddDeviceActivity.this, com.tutk.P2PCam264.DELUX.SelectAPActivity.class);
                intent.putExtra("ssid", tvSSID.getText().toString());
                intent.putExtra("ap", mDevAP);
                startActivityForResult(intent, REQUEST_CODE_SELECT_AP);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    private void setCheckOnEdit () {
        btnOK.setEnabled(false);

        mUIDEdtTxt.addTextChangedListener(mTxtWatcher);
        mSecurityEdtTxt.addTextChangedListener(mTxtWatcher);
        mNameEdtTxt.addTextChangedListener(mTxtWatcher);
//        edtAPSSID.addTextChangedListener(mTxtWatcher);
    }

    private TextWatcher mTxtWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged (CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged (CharSequence s, int start, int before, int count) {
//            if (mUIDEdtTxt.getText().length() == 0 || mSecurityEdtTxt.getText().length() == 0 || mNameEdtTxt.getText().length() == 0 || edtAPSSID.getText().length() == 0) {
                if (mUIDEdtTxt.getText().length() == 0 || mSecurityEdtTxt.getText().length() == 0 || mNameEdtTxt.getText().length() == 0 ) {
                btnOK.setEnabled(false);
            } else {
                btnOK.setEnabled(true);
            }
        }

        @Override
        public void afterTextChanged (Editable s) {

        }
    };

    private void doScan () {

        Intent mintent = new Intent();
        mintent = mintent.setClass(AddDeviceActivity.this, qr_codeActivity.class);
        AddDeviceActivity.this.startActivityForResult(mintent, REQUEST_CODE_GETUID_BY_SCAN_BARCODE);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    private void doAddOnly () {

        Glog.I("P2PCamLive","doAddOnly");
        String dev_nickname = mNameEdtTxt.getText().toString();
        String dev_uid = mUIDEdtTxt.getText().toString().trim();
        String view_pwd = mSecurityEdtTxt.getText().toString().trim();

        if (dev_uid.length() != 20) {
            Custom_Ok_Dialog dlg = new Custom_Ok_Dialog(this, getText(R.string.tips_dev_uid_character).toString(), getText(R.string.ok).toString());
            dlg.setCanceledOnTouchOutside(false);
            Window window = dlg.getWindow();
            window.setWindowAnimations(R.style.setting_dailog_animstyle);
            dlg.show();
            return;
        }

        if (! dev_uid.matches("[a-zA-Z0-9]+")) {
            Custom_Ok_Dialog dlg = new Custom_Ok_Dialog(this, getText(R.string.tips_dev_uid_special_characters).toString(), getText(R.string.ok)
                    .toString());
            dlg.setCanceledOnTouchOutside(false);
            Window window = dlg.getWindow();
            window.setWindowAnimations(R.style.setting_dailog_animstyle);
            dlg.show();
            return;
        }

        ThreadTPNS TPNSThread = new ThreadTPNS(AddDeviceActivity.this, dev_uid, ThreadTPNS.MAPPING_REG);
        TPNSThread.start();

        int video_quality = 0;
        int channel = 0;

		/* add value to data base */
        DatabaseManager manager = new DatabaseManager(AddDeviceActivity.this);
        long db_id = manager.addDevice(dev_nickname, dev_uid, "", "", "admin", view_pwd, 3, channel, mDevAP, mDevAPPwd, mWifiSSID, mWifiPassword);

        Toast.makeText(AddDeviceActivity.this, getText(R.string.tips_add_camera_ok).toString(), Toast.LENGTH_SHORT).show();

		/* return value to main activity */
        Bundle extras = new Bundle();
        extras.putLong("db_id", db_id);
        extras.putString("dev_nickname", dev_nickname);
        extras.putString("dev_uid", dev_uid);
        extras.putString("dev_name", "");
        extras.putString("dev_pwd", "");
        extras.putString("dev_ap", mDevAP);
        extras.putString("dev_ap_pwd", mDevAPPwd);
        extras.putString("wifi_ssid", mWifiSSID);
        extras.putString("wifi_pwd", mWifiPassword);
        extras.putInt("wifi_enc", wifi_enc);
        extras.putString("view_acc", "admin");
        extras.putString("view_pwd", view_pwd);
        extras.putInt("video_quality", video_quality);
        extras.putInt("camera_channel", 0);
        extras.putInt("MonitorIndex",mMonitorIndex);

        Intent Intent = new Intent();
        Intent.putExtras(extras);
        AddDeviceActivity.this.setResult(RESULT_OK, Intent);
        AddDeviceActivity.this.finish();
    }

    private void doOK () {

        String dev_nickname = mNameEdtTxt.getText().toString();
        String dev_uid = mUIDEdtTxt.getText().toString().trim();
        String view_pwd = mSecurityEdtTxt.getText().toString().trim();

        if (dev_uid.length() != 20) {
            Custom_Ok_Dialog dlg = new Custom_Ok_Dialog(this, getText(R.string.tips_dev_uid_character).toString(), getText(R.string.ok).toString());
            dlg.setCanceledOnTouchOutside(false);
            Window window = dlg.getWindow();
            window.setWindowAnimations(R.style.setting_dailog_animstyle);
            dlg.show();
            return;
        }

        if (! dev_uid.matches("[a-zA-Z0-9]+")) {
            Custom_Ok_Dialog dlg = new Custom_Ok_Dialog(this, getText(R.string.tips_dev_uid_special_characters).toString(), getText(R.string.ok)
                    .toString());
            dlg.setCanceledOnTouchOutside(false);
            Window window = dlg.getWindow();
            window.setWindowAnimations(R.style.setting_dailog_animstyle);
            dlg.show();
            return;
        }

        ThreadTPNS TPNSThread = new ThreadTPNS(AddDeviceActivity.this, dev_uid, ThreadTPNS.MAPPING_REG);
        TPNSThread.start();

        int video_quality = 0;
        int channel = 0;

		/* add value to data base */
        DatabaseManager manager = new DatabaseManager(AddDeviceActivity.this);
        long db_id = manager.addDevice(dev_nickname, dev_uid, "", "", "admin", view_pwd, 3, channel, mDevAP, mDevAPPwd, mWifiSSID, mWifiPassword);

        Toast.makeText(AddDeviceActivity.this, getText(R.string.tips_add_camera_ok).toString(), Toast.LENGTH_SHORT).show();

		/* return value to main activity */
        Bundle extras = new Bundle();
        extras.putLong("db_id", db_id);
        extras.putString("dev_nickname", dev_nickname);
        extras.putString("dev_uid", dev_uid);
        extras.putString("dev_name", "");
        extras.putString("dev_pwd", "");
        extras.putString("dev_ap", mDevAP);
        extras.putString("dev_ap_pwd", mDevAPPwd);
        extras.putString("wifi_ssid", mWifiSSID);
        extras.putString("wifi_pwd", mWifiPassword);
        extras.putInt("wifi_enc", wifi_enc);
        extras.putString("view_acc", "admin");
        extras.putString("view_pwd", view_pwd);
        extras.putInt("video_quality", video_quality);
        extras.putInt("camera_channel", 0);

        Intent Intent = new Intent();
        Intent.putExtras(extras);
        AddDeviceActivity.this.setResult(RESULT_OK, Intent);
        AddDeviceActivity.this.finish();
    }

    private boolean doCheck () {
        mDevAP = edtAPSSID.getText().toString();
        mDevAPPwd = edtAPpwd.getText().toString();
        mWifiSSID = tvSSID.getText().toString();
        mWifiPassword = edtWifiPWD.getText().toString();
        String dev_uid = mUIDEdtTxt.getText().toString().trim();

        if (dev_uid.length() != 20) {
            Custom_Ok_Dialog dlg = new Custom_Ok_Dialog(this, getText(R.string.tips_dev_uid_character).toString(), getText(R.string.ok).toString());
            dlg.setCanceledOnTouchOutside(false);
            Window window = dlg.getWindow();
            window.setWindowAnimations(R.style.setting_dailog_animstyle);
            dlg.show();
            return false;
        }

        if (! dev_uid.matches("[a-zA-Z0-9]+")) {
            Custom_Ok_Dialog dlg = new Custom_Ok_Dialog(this, getText(R.string.tips_dev_uid_special_characters).toString(), getText(R.string.ok)
                    .toString());
            dlg.setCanceledOnTouchOutside(false);
            Window window = dlg.getWindow();
            window.setWindowAnimations(R.style.setting_dailog_animstyle);
            dlg.show();
            return false;
        }

        return true;
    }

    private void doCancel () {

        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    public void onActivityResult (int requestCode, int resultCode, Intent intent) {

        if (requestCode == REQUEST_CODE_GETUID_BY_SCAN_BARCODE) {

            if (resultCode == RESULT_OK) {

                String contents = intent.getStringExtra("SCAN_RESULT");
                // String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                if (null == contents) {
                    Bundle bundle = intent.getExtras();
                    if (null != bundle) {
                        /* String */
                        contents = bundle.getString("result");
                    }
                }
                if (null == contents) {
                    return;
                }

                if (contents.length() > 20) {
                    String temp = "";

                    for (int t = 0 ; t < contents.length() ; t++) {
                        if (contents.substring(t, t + 1).matches("[A-Z0-9]{1}")) {
                            temp += contents.substring(t, t + 1);
                        }
                    }
                    contents = temp;
                }

                mUIDEdtTxt.setText(contents);

                mSecurityEdtTxt.requestFocus();

            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
            }
        } else if (requestCode == REQUEST_CODE_SELECT_AP) {
            if (resultCode == RESULT_OK) {
                String WiFiSSID = intent.getStringExtra("SSID");
                wifi_enc = intent.getIntExtra("enc", AVIOCTRLDEFs.AVIOTC_WIFIAPENC_WPA2_AES);
                if (WiFiSSID != null) {
                    tvSSID.setText(WiFiSSID);
                }
            }
        } else if (requestCode == REQUEST_CODE_CHECK_DEVICE) {
            switch (resultCode) {
                case EasyWiFiSetting.RESP_CODE_ADD:
                    mWifiSSID = tvSSID.getText().toString();
                    mWifiPassword = edtWifiPWD.getText().toString();
                    doOK();
                    break;
                case EasyWiFiSetting.RESP_CODE_RECONNECT:
                    mWifiSSID = tvSSID.getText().toString();
                    mWifiPassword = edtWifiPWD.getText().toString();
                    reconnectWifi();
                    break;
                default:
                    break;
            }
        } else if (requestCode == REQUEST_CODE_ADDUID_ONLY ) {
            Glog.I("P2PCamLive", "addDevice: ");
        }
    }

    private boolean isSDCardValid () {

        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    private void startInstall (File tempFile) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(tempFile), "application/vnd.android.package-archive");

        startActivity(intent);
    }

    private void startDownload (String url) throws Exception {

        if (URLUtil.isNetworkUrl(url)) {

            URL myURL = new URL(url);

            HttpURLConnection conn = (HttpURLConnection) myURL.openConnection();
            // conn.connect();

            InputStream is = conn.getInputStream();

            if (is == null) {
                return;
            }

            BufferedInputStream bis = new BufferedInputStream(is);

            File myTempFile = File.createTempFile("BarcodeScanner", ".apk", Environment.getExternalStorageDirectory());
            FileOutputStream fos = new FileOutputStream(myTempFile);

            byte[] buffer = new byte[1024];
            int read = 0;
            while ((read = bis.read(buffer)) > 0) {
                fos.write(buffer, 0, read);
            }

            try {
                fos.flush();
                fos.close();
            } catch (Exception ex) {
                System.out.println("error: " + ex.getMessage());
            }

            startInstall(myTempFile);
        }
    }

    private class SearchResult {

        public String UID;
        public String IP;

        // public int Port;

        public SearchResult (String uid, String ip, int port) {

            UID = uid;
            IP = ip;
            // Port = port;
        }
    }

    private class SearchResultListAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public SearchResultListAdapter (LayoutInflater inflater) {

            this.mInflater = inflater;
        }

        public int getCount () {

            return list.size();
        }

        public Object getItem (int position) {

            return list.get(position);
        }

        public long getItemId (int position) {

            return position;
        }

        public View getView (int position, View convertView, ViewGroup parent) {

            final SearchResult result = (SearchResult) getItem(position);
            ViewHolder holder = null;

            if (convertView == null) {

                convertView = mInflater.inflate(R.layout.search_device_result, null);

                holder = new ViewHolder();
                holder.uid = (TextView) convertView.findViewById(R.id.uid);
                holder.ip = (TextView) convertView.findViewById(R.id.ip);

                convertView.setTag(holder);

            } else {

                holder = (ViewHolder) convertView.getTag();
            }

            holder.uid.setText(result.UID);
            holder.ip.setText(result.IP);
            // holder.port.setText(result.Port);

            return convertView;
        }// getView()

        public final class ViewHolder {
            public TextView uid;
            public TextView ip;
        }
    }

    protected void onDestroy () {
        super.onDestroy();
        unregisterReceiver(mResultStateReceiver);
    }

    private class ResultStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive (Context context, Intent intent) {
            // TODO Auto-generated method stub

        }
    }

    private void reconnectWifi () {

        WifiConfiguration WifiConfiguration = null;
        if (mWifiPassword.equals("")) {
            WifiConfiguration = WifiAdmin.CreateWifiInfo(mWifiSSID, mWifiPassword, 1);
        } else {
            WifiConfiguration = WifiAdmin.CreateWifiInfo(mWifiSSID, mWifiPassword, 3);
        }
        if (WifiConfiguration != null) {
            WifiAdmin.openWifi();
            WifiAdmin.addNetwork(WifiConfiguration);
        }
    }

//	public class ConnectionChangeReceiver extends BroadcastReceiver {
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			if (WifiAdmin.isWifi()) {
//				if (WifiAdmin.getSSID().equals("NULL") != true) {
//					if (WifiAdmin.isCommect()) {
//						Connect_UID();
//					}
//				}
//			}
//		}
//	}

    @Override
    public void onClick (View v) {

        switch (v.getId()) {
            case R.id.btnOK:
                doAddOnly();
                /*
                if (doCheck()) {
                    String dev_nickname = mNameEdtTxt.getText().toString();
                    String dev_uid = mUIDEdtTxt.getText().toString().trim();
                    String view_pwd = mSecurityEdtTxt.getText().toString().trim();
                    String ssid = tvSSID.getText().toString().trim();
                    String wifi_pwd = edtWifiPWD.getText().toString().trim();
                    String ap = edtAPSSID.getText().toString().trim();
                    String ap_pwd = edtAPpwd.getText().toString().trim();
                    Intent intent = new Intent(AddDeviceActivity.this, AddDeviceCheckActivity.class);
                    intent.putExtra("name", dev_nickname);
                    intent.putExtra("uid", dev_uid);
                    intent.putExtra("pwd", view_pwd);
                    intent.putExtra("ssid", ssid);
                    intent.putExtra("wifi_pwd", wifi_pwd);
                    intent.putExtra("enc", wifi_enc);
                    intent.putExtra("ap", ap);
                    intent.putExtra("ap_pwd", ap_pwd);
                    if (mMode == MyMode.LAN) {
                        intent.putExtra("mode", AddDeviceCheckActivity.ADD_MODE_AP);
                    } else {
                        intent.putExtra("mode", AddDeviceCheckActivity.ADD_MODE_SET_WIFI);
                    }
                    startActivityForResult(intent, REQUEST_CODE_CHECK_DEVICE);
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                }
                */
                break;

            case R.id.layoutSettings:
                if (mShowSettings) {
                    Animation anim = AnimationUtils.loadAnimation(this, R.anim.rotate_counterclockwise);
                    anim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart (Animation animation) {
                            imgArrow.setRotation(0);
                        }

                        @Override
                        public void onAnimationEnd (Animation animation) {

                        }

                        @Override
                        public void onAnimationRepeat (Animation animation) {

                        }
                    });
                    imgArrow.startAnimation(anim);
                    anim = AnimationUtils.loadAnimation(this, R.anim.topbar_slide_hide);
                    anim.setDuration(500);
                    anim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart (Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd (Animation animation) {
                            layoutWIFI.setVisibility(View.GONE);
                            tvTips.setVisibility(View.VISIBLE);
                            Animation anim = AnimationUtils.loadAnimation(AddDeviceActivity.this, R.anim.topbar_slide_show);
                            anim.setDuration(200);
                            tvTips.startAnimation(anim);
                        }

                        @Override
                        public void onAnimationRepeat (Animation animation) {

                        }
                    });
                    layoutWIFIPwd.startAnimation(anim);
                } else {
                    tvTips.setVisibility(View.GONE);
                    layoutWIFI.setVisibility(View.VISIBLE);
                    Animation anim = AnimationUtils.loadAnimation(this, R.anim.rotate_clockwise);
                    anim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart (Animation animation) {
                            imgArrow.setRotation(180);
                        }

                        @Override
                        public void onAnimationEnd (Animation animation) {

                        }

                        @Override
                        public void onAnimationRepeat (Animation animation) {

                        }
                    });
                    imgArrow.startAnimation(anim);
                    anim = AnimationUtils.loadAnimation(this, R.anim.topbar_slide_show);
                    anim.setDuration(500);
                    layoutWIFIPwd.startAnimation(anim);
                }
                mShowSettings = ! mShowSettings;
                break;
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
