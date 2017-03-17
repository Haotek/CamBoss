package general;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.wifi.WifiConfiguration;
import android.os.Handler;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.IOTCAPIs;
import com.tutk.IOTC.IRegisterIOTCListener;
import com.tutk.IOTC.Packet;
import com.tutk.Logger.Glog;
import com.tutk.P2PCam264.MyCamera;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by James Huang on 2015/3/26.
 */
public class EasyWiFiSetting implements IRegisterIOTCListener {

    public final static int DIALOG_CHANGE_WIFI = 0;
    public final static int DIALOG_BACK = 1;
    public final static int RESP_CODE_SET_WIFI = 78;
    public final static int RESP_CODE_ADD = 79;
    public final static int RESP_CODE_RECONNECT = 80;
    public final static int RESP_CODE_ADD_WITH_CHANGE_PWD = 81;
    private final static String TAG = "EasyWiFi";

    private MyCamera mCamera = null;
    private appteam.WifiAdmin WifiAdmin;
    private Handler handler = new Handler();
    private Context mContext;
    private CheckDevListener mListener;
    private Timer timer = new Timer();
    private TimerTask timerTask;

    private String mDeviceName;
    private String mDeviceUID;
    private String mDevicePWD;
    private String mDevAP;
    private String mDevAPPwd;
    private String mWifiSSID;
    private String mWifiPassword;
    private boolean mSetWiFi = false;
    private boolean mChangeWiFi = false;
    private boolean firstSuccess = true;
    private boolean mHasNoNetwork = false;
    private int wifi_enc = 4;
    private int wifi_list_times = 0;

    public EasyWiFiSetting (Context context, CheckDevListener listener, String name, String uid, String pwd, String ap, String ap_pwd, String ssid,
                            String ssid_pwd) {
        mContext = context;
        mListener = listener;
        mDeviceName = name;
        mDeviceUID = uid;
        mDevicePWD = pwd;
        mDevAP = ap;
        mDevAPPwd = ap_pwd;
        mWifiSSID = ssid;
        mWifiPassword = ssid_pwd;
    }

    public void startSetWiFi () {

        mCamera = new MyCamera(mDeviceName, mDeviceUID, "admin", mDevicePWD);
        Thread mConnectThread = new Thread(new Runnable() {
            @Override
            public void run () {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(System.currentTimeMillis());
                Glog.D(TAG, "============================" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.DAY_OF_MONTH) + " " + c.get
                        (Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE));
                Glog.D(TAG, "Connect with UID");
                mCamera.registerIOTCListener((IRegisterIOTCListener) EasyWiFiSetting.this);
                mCamera.connect(mDeviceUID);
                mCamera.start(MyCamera.DEFAULT_AV_CHANNEL, "admin", mDevicePWD);
            }
        });

        mConnectThread.start();

        WifiAdmin = new appteam.WifiAdmin(mContext);
    }

    public void quit (boolean isIntent) {

        if (mCamera != null) {
            mCamera.unregisterIOTCListener(EasyWiFiSetting.this);
            if (! isIntent) {
                mCamera.disconnect();
            }
        }

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        if (mListener != null) {
            mListener = null;
        }
    }

    private boolean ChangeWiFi () {

        Glog.D(TAG, "Change WiFi = " + mDevAP + " pwd = " + mDevAPPwd);
        boolean hasWifi = false;
        WifiAdmin.startScan();
        for (String search : WifiAdmin.getWifiListsSSID()) {
            if (search.equals(mDevAP)) {
                hasWifi = true;
                break;
            }
        }

        if (! hasWifi) {
            Glog.D(TAG, "Can not scan the SSID");
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
                Glog.D(TAG, "Can not ADD the SSID");
                return false;
            } else {
                Glog.D(TAG, "Change WiFi success");
                return true;
            }
        } else {
            Glog.D(TAG, "WiFiAdmin err");
            return false;
        }
    }

    private void reconnectWifi () {
        Glog.D(TAG, "Reconnect Wifi = " + mWifiSSID + " pwd = " + mWifiPassword);

        WifiConfiguration WifiConfiguration = null;
        if (mWifiPassword.length() == 0) {
            WifiConfiguration = WifiAdmin.CreateWifiInfo(mWifiSSID, mWifiPassword, 1);
        } else {
            WifiConfiguration = WifiAdmin.CreateWifiInfo(mWifiSSID, mWifiPassword, 3);
        }
        if (WifiConfiguration != null) {
            WifiAdmin.openWifi();
            WifiAdmin.addNetwork(WifiConfiguration);
        }
    }

    private static String getString (byte[] data) {

        StringBuilder sBuilder = new StringBuilder();

        for (int i = 0 ; i < data.length ; i++) {

            if (data[i] == 0x0) {
                break;
            }

            sBuilder.append((char) data[i]);
        }

        return sBuilder.toString();
    }

    private void connectionFailed () {
        Glog.D(TAG, "Connect FAILED with UID");
        firstSuccess = false;
        if (mSetWiFi) {
            if (mHasNoNetwork) {
                if (mListener != null) {
                    mListener.getCheckingErr(CheckDevListener.RESULT_NETWORK_UNREACHABLE);
                }
                return;
            }

            if (timerTask != null) {
                timerTask.cancel();
            }

            timerTask = new TimerTask() {
                @Override
                public void run () {
                    if (mCamera != null) {
                        mCamera.disconnect();
                    }

                    if (mListener != null) {
                        mListener.getCheckingErr(CheckDevListener.RESULT_FAILED_TO_SET);
                    }
                }
            };

            timer.schedule(timerTask, 5000);

        } else if (mChangeWiFi) {
            if (mCamera != null) {
                mCamera.disconnect();
            }

            if (mListener != null) {
                mListener.getCheckingErr(CheckDevListener.RESULT_FAILED_IN_LAN);
            }
        } else {
            if (mCamera != null) {
                mCamera.disconnect();
            }
            if (ChangeWiFi()) {

                final Thread mConnectThread = new Thread(new Runnable() {
                    @Override
                    public void run () {
                        Glog.D(TAG, "Connect with UID");
                        mCamera.connect(mDeviceUID);
                        mCamera.start(MyCamera.DEFAULT_AV_CHANNEL, "admin", mDevicePWD);
                        mChangeWiFi = true;
                    }
                });

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run () {
                        Glog.I(TAG, "" + WifiAdmin.getDetailedState());

                        if (WifiAdmin.isWifi()) {
                            mConnectThread.start();
                        } else {
                            handler.postDelayed(this, 2000);
                        }
                    }
                }, 2000);

            } else {
                if (mListener != null) {
                    mListener.getCheckingErr(CheckDevListener.RESULT_CANNOT_CHANGE_TO_AP);
                }
            }
        }

        mHasNoNetwork = false;
    }

    @Override
    public void receiveFrameData (Camera camera, int avChannel, Bitmap bmp) {

    }

    @Override
    public void receiveFrameDataForMediaCodec (Camera camera, int avChannel, byte[] buf, int length, int pFrmNo, byte[] pFrmInfoBuf, boolean
            isIframe, int codecId) {

    }

    @Override
    public void receiveFrameInfo (Camera camera, int avChannel, long bitRate, int frameRate, int onlineNm, int frameCount, int incompleteFrameCount) {

    }

    @Override
    public void receiveSessionInfo (Camera camera, int resultCode) {
        if (camera == mCamera) {
            if (resultCode == Camera.CONNECTION_STATE_UNKNOWN_DEVICE) {

                connectionFailed();

            } else if (resultCode == Camera.CONNECTION_STATE_CONNECTED) {
                Glog.D(TAG, "Connect SUCCESS with UID");
                if (mSetWiFi || firstSuccess) {
                    if (timerTask != null) {
                        timerTask.cancel();
                    }

                    if (mListener != null && mCamera != null) {
                        mListener.getConnected(mCamera);
                    }
                }

            } else if (resultCode != Camera.CONNECTION_STATE_CONNECTING) {
                if (mCamera != null) {
                    if (mCamera.getMSID() == IOTCAPIs.IOTC_ER_NETWORK_UNREACHABLE) {
                        Glog.D(TAG, "IOTC_ER_NETWORK_UNREACHABLE");
                        mHasNoNetwork = true;
                        connectionFailed();
                        return;
                    }
                }

                Glog.D(TAG, "Connect ELSE with UID");
                if (mChangeWiFi) {
                    if (mListener != null) {
                        mListener.getCheckingErr(CheckDevListener.RESULT_FAILED_IN_LAN);
                    }
                } else {
                    if (mListener != null && mCamera != null) {
                        mListener.getConnected(mCamera);
                    }
                }
            }
        }
    }

    @Override
    public void receiveChannelInfo (Camera camera, int avChannel, int resultCode) {
        if (mCamera == camera) {
            if (mSetWiFi) {
                if (resultCode == Camera.CONNECTION_STATE_TIMEOUT) {
                    if (mCamera != null) {
                        mCamera.disconnect();
                    }

                    if (mListener != null) {
                        mListener.getCheckingErr(CheckDevListener.RESULT_FAILED_TO_CREATE_CHANNEL);
                    }
                }
            } else {
                if (resultCode == Camera.CONNECTION_STATE_CONNECTED) {
                    if (! mSetWiFi && ! firstSuccess) {
                        timerTask = new TimerTask() {
                            @Override
                            public void run () {
                                if (wifi_list_times < 3) {
                                    mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_LISTWIFIAP_REQ, AVIOCTRLDEFs
                                            .SMsgAVIoctrlListWifiApReq.parseContent());
                                    wifi_list_times++;
                                } else {
                                    timer.cancel();
                                    if (mCamera != null) {
                                        mCamera.disconnect();
                                    }

                                    if (mListener != null) {
                                        mListener.getCheckingErr(CheckDevListener.RESULT_CANNOT_GET_WIFI_LIST);
                                    }
                                }
                            }
                        };

                        timer.schedule(timerTask, 5000, 5000);

                        mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_LISTWIFIAP_REQ, AVIOCTRLDEFs
                                .SMsgAVIoctrlListWifiApReq.parseContent());
                        wifi_list_times++;
                    }
                } else if (resultCode == Camera.CONNECTION_STATE_WRONG_PASSWORD) {
                    if (mListener != null) {
                        mListener.getCheckingErr(CheckDevListener.RESULT_WRONG_PWD);
                    }
                }
            }
        }
    }

    @Override
    public void receiveIOCtrlData (Camera camera, int avChannel, int avIOCtrlMsgType, byte[] data) {
        if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_LISTWIFIAP_RESP) {

            if (timerTask != null) {
                timerTask.cancel();
            }

            int cnt = Packet.byteArrayToInt_Little(data, 0);
            int size = AVIOCTRLDEFs.SWifiAp.getTotalSize();

            if (cnt > 0 && data.length >= 40) {

                int pos = 4;
                for (int i = 0 ; i < cnt ; i++) {
                    byte[] ssid = new byte[32];
                    System.arraycopy(data, i * size + pos, ssid, 0, 32);

                    byte mode = data[i * size + pos + 32];
                    byte enctype = data[i * size + pos + 33];
                    byte signal = data[i * size + pos + 34];
                    byte status = data[i * size + pos + 35];

                    if (mWifiSSID.equals(getString(ssid))) {
                        wifi_enc = enctype;
                        Glog.D(TAG, "Get same SSID from WiFiList");
                        break;
                    }
                }
            }

            Glog.D(TAG, "Set WiFi to device");
            if (mWifiPassword.equals("")) {
                camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETWIFI_REQ, AVIOCTRLDEFs.SMsgAVIoctrlSetWifiReq
                        .parseContent(mWifiSSID.getBytes(), mWifiPassword.getBytes(), (byte) 1, (byte) AVIOCTRLDEFs.AVIOTC_WIFIAPENC_NONE));
            } else {
                camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETWIFI_REQ, AVIOCTRLDEFs.SMsgAVIoctrlSetWifiReq
                        .parseContent(mWifiSSID.getBytes(), mWifiPassword.getBytes(), (byte) 1, (byte) wifi_enc));
            }

            handler.postDelayed(new Runnable() {
                @Override
                public void run () {
                    mCamera.disconnect();
                    reconnectWifi();
                    final Thread mConnectThread = new Thread(new Runnable() {
                        @Override
                        public void run () {
                            Glog.D(TAG, "Connect with UID");
                            mCamera.connect(mDeviceUID);
                            mCamera.start(MyCamera.DEFAULT_AV_CHANNEL, "admin", mDevicePWD);
                            mSetWiFi = true;
                        }
                    });

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run () {
                            Glog.I(TAG, "" + WifiAdmin.getDetailedState());

                            if (WifiAdmin.isWifi()) {
                                mConnectThread.start();
                            } else {
                                handler.postDelayed(this, 2000);
                            }
                        }
                    }, 8000);
                }
            }, 1000);
        }
    }

}
