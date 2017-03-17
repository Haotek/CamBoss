package appteam;

import com.tutk.IOTC.AVAPIs;
import com.tutk.IOTC.IOTCAPIs;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Util {

	public static boolean isNetworkAvailable(Context ctx) {
		ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

		return (activeNetworkInfo != null) ? true : false;
	}


	public static String getIOTCAPis() {
	
		byte[] bytVer = new byte[4];
		int[] lVer = new int[1];
		int ver;
	
		IOTCAPIs.IOTC_Get_Version(lVer);
	
		ver = (int) lVer[0];
	
		StringBuffer sb = new StringBuffer();
		bytVer[3] = (byte) (ver);
		bytVer[2] = (byte) (ver >>> 8);
		bytVer[1] = (byte) (ver >>> 16);
		bytVer[0] = (byte) (ver >>> 24);
		sb.append((int) (bytVer[0] & 0xff));
		sb.append('.');
		sb.append((int) (bytVer[1] & 0xff));
		sb.append('.');
		sb.append((int) (bytVer[2] & 0xff));
		sb.append('.');
		sb.append((int) (bytVer[3] & 0xff));
	
		return sb.toString();
	}
	
	public static String getAVAPis() {
	
		byte[] bytVer = new byte[4];
		int ver = AVAPIs.avGetAVApiVer();
	
		StringBuffer sb = new StringBuffer();
		bytVer[3] = (byte) (ver);
		bytVer[2] = (byte) (ver >>> 8);
		bytVer[1] = (byte) (ver >>> 16);
		bytVer[0] = (byte) (ver >>> 24);
		sb.append((int) (bytVer[0] & 0xff));
		sb.append('.');
		sb.append((int) (bytVer[1] & 0xff));
		sb.append('.');
		sb.append((int) (bytVer[2] & 0xff));
		sb.append('.');
		sb.append((int) (bytVer[3] & 0xff));
	
		return sb.toString();
	}

}