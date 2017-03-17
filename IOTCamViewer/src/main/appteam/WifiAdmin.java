package appteam;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;

import com.tutk.Logger.Glog;

import java.util.ArrayList;
import java.util.List;

public class WifiAdmin {

	public static final int WIFI_STATE_DISABLING = 0;
	public static final int WIFI_STATE_ENABLED = 3;
	public static final int WIFI_STATE_ENABLING = 2;
	public static final int WIFI_STATE_UNKNOWN = 4;
	public static final int WIFICIPHER_NOPASS = 1;
	public static final int WIFICIPHER_WEP = 2;
	public static final int WIFICIPHER_WPA = 3;

	String TAG = "WifiAdmin";

	// 定義WifiManager對象
	private WifiManager mWifiManager;
	// 定義WifiInfo對象
	private WifiInfo mWifiInfo;
	// 掃描出的網络連接列表
	private List<ScanResult> mWifiList;
	// 網络連接列表
	private List<WifiConfiguration> mWifiConfiguration;
	// 定義一個WifiLock
	WifiLock mWifiLock;
	private ConnectivityManager connectManager;
	private Context mcontext;
	private MyListener mListener;
	private WifiReceiver mReceiverWifi;

	// 構造器
	public WifiAdmin(Context context) {
		// 取得WifiManager對象
		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		mcontext = context;
		// 取得WifiInfo對象
		mWifiInfo = mWifiManager.getConnectionInfo();
	}

	// 構造器
	public WifiAdmin(Context context, MyListener Listener) {
		// 取得WifiManager對象
		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		mcontext = context;
		mListener = Listener;
		// 取得WifiInfo對象
		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		mReceiverWifi = new WifiReceiver();
		mcontext.registerReceiver(mReceiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}

	// 打開WIFI
	public void openWifi() {
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
	}

	// 關閉WIFI
	public void closeWifi() {
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}
	}

	// 檢查當前WIFI狀態
	public int checkState() {
		return mWifiManager.getWifiState();
	}

	// 檢查當前WIFI是否連線
	public boolean isConnect() {
		boolean result = false;
		ConnectivityManager connManager = (ConnectivityManager) mcontext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connManager.getActiveNetworkInfo();
		if (info == null || !info.isConnected()) {
			result = false;
		} else {
			if (!info.isAvailable()) {
				result = false;
			} else {
				result = true;
			}
		}

		return result;
	}

	// 锁定WifiLock
	public void acquireWifiLock() {
		mWifiLock.acquire();
	}

	// 解锁WifiLock
	public void releaseWifiLock() {
		// 判斷時候锁定
		if (mWifiLock.isHeld()) {
			mWifiLock.acquire();
		}
	}

	// 創建一個WifiLock
	public void creatWifiLock() {
		mWifiLock = mWifiManager.createWifiLock("Test");
	}

	// 得到配置好的網络
	public List<WifiConfiguration> getConfiguration() {
		for (int i = 0; i < mWifiConfiguration.size(); i++) {
			Glog.D(TAG + "WifiConfiguration", "WifiConfiguration's SSID:" + mWifiConfiguration.get(i).SSID);
		}
		return mWifiConfiguration;
	}

	// 指定配置好的網络進行連接
	public void connectConfiguration(int index) {
		// 索引大於配置好的網络索引返回
		if (index > mWifiConfiguration.size()) {
			return;
		}
		// 連接配置好的指定ID的網络
		mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId, true);
	}

	public void startScan() {
		mWifiManager.startScan();
		// 得到掃描結果
		mWifiList = mWifiManager.getScanResults();
		// 得到配置好的網络連接
		mWifiConfiguration = mWifiManager.getConfiguredNetworks();
	}

	// 立刻刷新wifi
	public void scanWifi() {

		if (!mWifiManager.isWifiEnabled())
			mWifiManager.setWifiEnabled(true);

		mWifiManager.startScan();
		Glog.D(TAG, "Scan Waiting...");

	}

	class WifiReceiver extends BroadcastReceiver {

		private List<String> mSSIDList = new ArrayList<String>();

		public void onReceive(Context c, Intent intent) {
			Glog.D(TAG, "onReceive");
			mSSIDList.clear();
			List<ScanResult> resultList = new ArrayList<ScanResult>();
			resultList = mWifiManager.getScanResults();
			for (ScanResult scanresult : resultList) {

				Glog.D(TAG, "Scaned SSID: " + scanresult.SSID);
				mSSIDList.add(scanresult.SSID);

			}
			Glog.D(TAG, "onReceive mListener: " + mListener);
			mListener.onScanComplete(mSSIDList);
			mcontext.unregisterReceiver(mReceiverWifi);
		}

	}

	public boolean isWifi() {
		ConnectivityManager connectManager = (ConnectivityManager) mcontext.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectManager == null) {
			Glog.D(TAG, "get connectManager fail.");
			return false;
		}

		NetworkInfo wifiNetInfo = connectManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetInfo != null && (wifiNetInfo.getState() == NetworkInfo.State.CONNECTED/* || wifiNetInfo.getState() == NetworkInfo.State.CONNECTING*/)) {
			Glog.D(TAG, "is using wifi");
			return true;
		}

		Glog.D(TAG, "is NOT using wifi");
		return false;
	}

	// 得到當前網路加密模式
	public String getEncType(String SSid) {
		startScan();
		if (mWifiList != null) {
			for (int i = 0; i < mWifiList.size(); i++) {
				if (mWifiList.get(i).SSID.toString().replace("\"", "").equals(SSid)) {
					return mWifiList.get(i).capabilities;
				}
			}

		} else {
			return null;
		}
		return null;
	}

	// 得到網络列表
	public List<ScanResult> getWifiList() {
		for (int i = 0; i < mWifiList.size(); i++) {
			Glog.D(TAG + "SSID", "SSID:" + mWifiList.get(i).SSID);
		}

		return mWifiList;
	}

	// 得到網络列表裡的SSID
	public String[] getWifiListsSSID() {
		String[] SSID = new String[mWifiList.size()];
		for (int i = 0 ; i < mWifiList.size(); i++) {
			Glog.D(TAG + "SSID", "SSID:" + mWifiList.get(i).SSID);
			SSID[i] = mWifiList.get(i).SSID;
		}

		return SSID;
	}

	// 查看掃描結果
	public StringBuilder lookUpScan() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < mWifiList.size(); i++) {
			stringBuilder.append("Index_" + new Integer(i + 1).toString() + ":");
			// 將ScanResult信息轉換成一個字符串包
			// 其中把包括：BSSID、SSID、capabilities、frequency、level
			stringBuilder.append((mWifiList.get(i)).toString());
			stringBuilder.append("/n");
		}
		return stringBuilder;
	}

	// 掃描結果 interface
	public interface MyListener {
		void onScanComplete(List<String> ssidList);
	}

	// 得到MAC地址
	public String getMacAddress() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
	}

	// 得到接入點的BSSID
	public String getBSSID() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
	}

	// 得到IP地址
	public int getIPAddress() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
	}

	// 得到連接的ID
	public int getNetworkId() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
	}

	// 得到連接的SSID
	public String getSSID() {
		mWifiInfo = mWifiManager.getConnectionInfo();
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getSSID();
	}

	// 得到WifiInfo的所有信息包
	public String getWifiInfo() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
	}

	// get connection state
	public NetworkInfo.State getState(){
		ConnectivityManager connectManager = (ConnectivityManager) mcontext.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectManager == null) {
			Glog.D(TAG, "get connectManager fail.");
			return null;
		}

		NetworkInfo wifiNetInfo = connectManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetInfo != null) {
			return wifiNetInfo.getState();
		}

		return null;
	}

	public NetworkInfo.DetailedState getDetailedState (){
		ConnectivityManager connectManager = (ConnectivityManager) mcontext.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectManager == null) {
			Glog.D(TAG, "get connectManager fail.");
			return null;
		}

		NetworkInfo wifiNetInfo = connectManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetInfo != null) {
			return wifiNetInfo.getDetailedState();
		}

		return null;
	}

	// 添加一個網络並連接
	public boolean addNetwork(WifiConfiguration wcg) {
		int wcgID = mWifiManager.addNetwork(wcg);
		boolean b = mWifiManager.enableNetwork(wcgID, true);
		System.out.println("a--:" + wcgID);
		System.out.println("b--:" + b);
		return b;
	}

	// 斷開指定ID的網络
	public void disconnectWifi(int netId) {
		mWifiManager.disableNetwork(netId);
		mWifiManager.disconnect();
	}

//然後是一個實際應用方法，只驗證過沒有密碼的情況：

	public WifiConfiguration CreateWifiInfo(String SSID, String Password, int Type) {
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		// config.SSID = SSID;
		config.SSID = "\"" + SSID + "\"";

		WifiConfiguration tempConfig = this.IsExsits(SSID);
		if (tempConfig != null) {
			mWifiManager.removeNetwork(tempConfig.networkId);
		}

		if (Type == WIFICIPHER_NOPASS) // WIFICIPHER_NOPASS
		{
//        	  config.hiddenSSID = true; 
//        	  config.wepKeys[0] = "\"" + "\"";        	
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//              config.wepTxKeyIndex = 0;   
//               config.wepKeys[0] = ""; 
//               config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE); 
//               config.wepTxKeyIndex = 0; 
		}
		if (Type == WIFICIPHER_WEP) // WIFICIPHER_WEP
		{
			config.hiddenSSID = true;
			config.wepKeys[0] = "\"" + Password + "\"";
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		if (Type == WIFICIPHER_WPA) // WIFICIPHER_HAVEPASS
		{
			config.preSharedKey = "\"" + Password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			// config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.status = WifiConfiguration.Status.ENABLED;

		}
		return config;
	}

	private WifiConfiguration IsExsits(String SSID) {
		List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
		if(existingConfigs != null) {
			for (WifiConfiguration existingConfig : existingConfigs) {
				Glog.D(TAG, "existingConfig.SSID" + existingConfig.SSID);
				if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
					return existingConfig;
				}
			}
		}
		return null;
	}

}