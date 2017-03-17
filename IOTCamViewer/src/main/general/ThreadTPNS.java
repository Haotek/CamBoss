package general;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.tutk.P2PCam264.DELUX.MultiViewActivity;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

public class ThreadTPNS extends Thread {

	private final int TYPE_REGISTER = 2;
	private final int TYPE_MAPPING = 1;
	public final static int MAPPING_REG = 2;
	public final static int MAPPING_UNREG = 1;
	public static final int INTERVAL_NO_LIMIT = 0;
	public static final int INTERVAL_1_MIN = 1;
	public static final int INTERVAL_3_MIN = 2;
	public static final int INTERVAL_5_MIN = 3;
	public static final int INTERVAL_10_MIN = 4;
	public static final int INTERVAL_30_MIN = 5;
	public static final int INTERVAL_OFF = 6;

	private IOTC_GCM_IntentService mIotc_GCM_IntentService;
	private Context mcontext = null;
	private String mRegistrationId = null;
	private int type = 0;
	private int mReg = 0; // 0 reg; 1 unreg;
	private String mUID = "";
	private Activity mActivity = null;
	public static boolean mSupportBaidu = false;

	public ThreadTPNS(Context context) {
		mcontext = context;
		mRegistrationId = DatabaseManager.s_GCM_token;
		type = TYPE_REGISTER;
		mReg = 0;
	}

	public ThreadTPNS(Activity activity, String uid, int reg) {
		mActivity = activity;
		mcontext = activity;
		mRegistrationId = DatabaseManager.s_GCM_token;
		type = TYPE_MAPPING;
		mReg = reg;
		mUID = uid;
	}

	public ThreadTPNS(Context context, String uid, int reg) {
		mcontext = context;
		mRegistrationId = DatabaseManager.s_GCM_token;
		type = TYPE_MAPPING;
		mReg = reg;
		mUID = uid;
	}

//	public ThreadTPNS(Activity activity, String uid) {
//		mActivity = activity;
//		mRegistrationId = general.DatabaseManager.s_GCM_token;
//		type = TYPE_MAPPING;
//		mReg = MAPPING_UNREG;
//		mUID = uid;
//	}
//
//	public ThreadTPNS(Context context, String uid) {
//		mcontext = context;
//		mRegistrationId = general.DatabaseManager.s_GCM_token;
//		type = TYPE_MAPPING;
//		mReg = MAPPING_UNREG;
//		mUID = uid;
//	}

	@Override
	public void run() {

		try {

			InetAddress[] inetAddr = InetAddress.getAllByName("push.tutk.com");

			String ipAddr = "";
			String[] ipList = new String[inetAddr.length];

			for (int j = 0; j < inetAddr.length; j++) {

				byte[] address = inetAddr[j].getAddress();
				for (int i = 0; i < address.length; i++) {

					if (i > 0) {
						ipAddr += ".";
					}
					ipAddr += address[i] & 0xFF;
				}
				ipList[j] = ipAddr;
				ipAddr = "";
			}
			System.out.println("IP Address  end" + ipList);

			for (int i = 0; i < ipList.length; i++) {
				boolean checkSend = false;
				// http��}
				String httpUrl = ipList[i];
				// HttpGet�s�u����
				try {

					DefaultHttpClient http = new DefaultHttpClient();
					HttpGet httpMethod = new HttpGet();
					httpMethod.setURI(new URI("http://" + httpUrl + "/apns/apns.php?cmd=hello"));
					HttpResponse response = http.execute(httpMethod);
					int responseCode = response.getStatusLine().getStatusCode();
					switch (responseCode) {
					case 200:
						HttpEntity entity = response.getEntity();
						if (entity != null) {
							String responseBody = EntityUtils.toString(entity);
							String udid = "";
							String appvar = "";
							if (mcontext != null) {
								udid = DatabaseManager.uid_Produce(mcontext);
								appvar = DatabaseManager.get_appver(mcontext);
							} else {
								udid = DatabaseManager.uid_Produce(mActivity);
								appvar = DatabaseManager.get_appver(mActivity);
							}
							if (responseBody.indexOf("hi") != -1) {
								if (type == TYPE_REGISTER) {
									mHttpGetTool("http://" + httpUrl + "/apns/apns.php?cmd=reg_client&token="
											+ DatabaseManager.s_GCM_token + "&appid="
											+ DatabaseManager.s_Package_name + "&dev=0" + "&udid=" + udid + "&os=android"
											+ "&lang=" + DatabaseManager.get_language() + "&osver="
											+ DatabaseManager.get_osver() + "&appver=" + appvar + "&model="
											+ DatabaseManager.get_model());

//									mHttpPostsyncApiTool("http://" + httpUrl + "/apns/apns.php", mcontext, udid, DatabaseManager.s_GCM_token,
//											DatabaseManager.s_Package_name, "android");
									
									mHttpGetTool("http://" + httpUrl + "/apns/apns.php?cmd=sync&token="
											+ DatabaseManager.s_GCM_token + "&appid="
											+ DatabaseManager.s_Package_name + "&udid=" + udid + "&os=android"
											+ "&map=" + getUIDList(mcontext));
									if (mSupportBaidu && IOTC_GCM_IntentService.user_id != null && IOTC_GCM_IntentService.channel_id != null) {
										mHttpGetTool("http://" + httpUrl + "/apns/apns.php?cmd=reg_client" + "&appid="
												+ DatabaseManager.s_Package_name_baidu + "&dev=0" + "&udid=" + udid + "&os=baidu"
												+ "&token=" + IOTC_GCM_IntentService.user_id + "@" + IOTC_GCM_IntentService.channel_id + "&lang="
												+ DatabaseManager.get_language() + "&osver="
												+ DatabaseManager.get_osver() + "&appver=" + appvar + "&model="
												+ DatabaseManager.get_model());
										mHttpPostsyncApiTool("http://" + httpUrl + "/apns/apns.php", mcontext, udid, IOTC_GCM_IntentService.user_id
												+ "@" + IOTC_GCM_IntentService.channel_id, DatabaseManager.s_Package_name_baidu, "android");
									}
								} else if (type == TYPE_MAPPING) {
									if (mReg == MAPPING_UNREG) {
										mHttpGetTool("http://" + httpUrl + "/apns/apns.php" + "?cmd=unreg_mapping" + "&appid="
												+ DatabaseManager.s_Package_name + "&uid=" + mUID + "&udid=" + udid
												+ "&os=android", mUID);

										if (mSupportBaidu && IOTC_GCM_IntentService.user_id != null && IOTC_GCM_IntentService.channel_id != null) {
											mHttpGetTool("http://" + httpUrl + "/apns/apns.php" + "?cmd=unreg_mapping" + "&appid="
													+ DatabaseManager.s_Package_name_baidu + "&uid=" + mUID + "&udid=" + udid
													+ "&os=baidu" + "&token=" + IOTC_GCM_IntentService.user_id + "@"
													+ IOTC_GCM_IntentService.channel_id, mUID);
										}
									}
									if (mReg == MAPPING_REG) {
										SharedPreferences settings = mcontext.getSharedPreferences("Interval", 0);
										int position = -1;
										int interval = -1;
										position = settings.getInt(mUID, -1);
										switch (position) {
										case INTERVAL_NO_LIMIT:
											interval = 0;
											break;
										case INTERVAL_1_MIN:
											interval = 60;
											break;
										case INTERVAL_3_MIN:
											interval = 180;
											break;
										case INTERVAL_5_MIN:
											interval = 300;
											break;
										case INTERVAL_10_MIN:
											interval = 600;
											break;
										case INTERVAL_30_MIN:
											interval = 1800;
											break;
										default:
											interval = 0;
											break;
										}

										mHttpGetTool("http://" + httpUrl + "/apns/apns.php" + "?cmd=reg_mapping" + "&appid="
												+ DatabaseManager.s_Package_name + "&uid=" + mUID + "&udid=" + udid
												+ "&os=android" + "&interval=" + interval);

										if (mSupportBaidu && IOTC_GCM_IntentService.user_id != null && IOTC_GCM_IntentService.channel_id != null) {
											mHttpGetTool("http://" + httpUrl + "/apns/apns.php" + "?cmd=reg_mapping" + "&appid="
													+ DatabaseManager.s_Package_name_baidu + "&uid=" + mUID + "&udid=" + udid
													+ "&os=baidu" + "&token=" + IOTC_GCM_IntentService.user_id + "@"
													+ IOTC_GCM_IntentService.channel_id + "&interval=" + interval);
										}
									}
								}
								checkSend = true;
							}
						}
						break;
					}

				} catch (ClientProtocolException e) {
					Log.i("tpns", "error" + e.toString());
					AddRemoveList();

				} catch (IOException e) {
					Log.i("tpns", "error" + e.toString());
					AddRemoveList();

				} catch (Exception e) {
					Log.i("tpns", "error" + e.toString());
					AddRemoveList();
				}

				if (checkSend == true)
					break;
			}

		} catch (UnknownHostException e) {
			System.out.println("Host not found: " + e.getMessage());
		}
	}

	public void AddRemoveList() {
		if (mReg == 1) {
			try {
				if (mcontext != null) {
					DatabaseManager manager = new DatabaseManager(mcontext);
					manager.add_remove_list(mUID);
				} else {
					DatabaseManager manager = new DatabaseManager(mActivity);
					manager.add_remove_list(mUID);
				}
			} catch (Exception e) {
				Log.i("tpns", "error" + e.toString());
			}
		}
	}

	public void mHttpGetTool(String from, String uid) {

		String response = null;
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(from);
		httpGet.addHeader("Content-Type", "application/x-www-form-urlencoded");
		ResponseHandler<String> responseHandler = new BasicResponseHandler();

		try {
			response = httpClient.execute(httpGet, responseHandler);
			if (from.indexOf("cmd=unreg_mapping") != -1) {
				if (response.indexOf("unregistered successfully.") == -1) {
					if (mcontext != null) {
						DatabaseManager manager = new DatabaseManager(mcontext);
						manager.add_remove_list(uid);
					} else {
						DatabaseManager manager = new DatabaseManager(mActivity);
						manager.add_remove_list(uid);
					}
				} else {
					if (mcontext != null) {
						DatabaseManager manager = new DatabaseManager(mcontext);
						manager.delete_remove_list(uid);
					} else {
						DatabaseManager manager = new DatabaseManager(mActivity);
						manager.delete_remove_list(uid);
					}
				}
			}

		} catch (Exception e) {
			response = null;
			System.out.println("error");
		}
	}

	public void mHttpGetTool(String from) {

		String response = null;
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(from);
		httpGet.addHeader("Content-Type", "application/x-www-form-urlencoded");
		ResponseHandler<String> responseHandler = new BasicResponseHandler();

		try {
			response = httpClient.execute(httpGet, responseHandler);

		} catch (Exception e) {
			response = null;
			System.out.println("error");
		}
	}

	public void mHttpPostsyncApiTool(String fromURL, Context context, String uid, String token, String appid, String os) {

		JSONObject SyncApiObject = getSyncApiObject(context, uid, token, appid, os);
		HttpPost httpRequest;
		httpRequest = new HttpPost(fromURL);
		httpRequest.addHeader("Content-Type", "application/x-www-form-urlencoded");
		StringEntity se = null;
		try {
			se = new StringEntity(SyncApiObject.toString());
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		httpRequest.setEntity(se);

		try {
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
			String retSrc = EntityUtils.toString(httpResponse.getEntity());
			Log.i("tpns", "syncapi" + " response " + retSrc);
		} catch (Exception e) {
			Log.i("tpns", "error" + e.toString());
			System.out.println("error");
		}
	}

	private JSONObject getSyncApiObject(Context mContext, String udid, String token, String appid, String os) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("cmd", "sync");
			jsonObject.put("udid", udid);
			jsonObject.put("token", token);
			jsonObject.put("appid", appid);
			jsonObject.put("os", os);

			JSONArray jsonArray = new JSONArray();

			DatabaseManager manager = new DatabaseManager(mContext);
			SQLiteDatabase db = manager.getReadableDatabase();
			Cursor cursor = db.query(DatabaseManager.TABLE_DEVICE, new String[] { "_id", "dev_uid" }, null, null, null, null, "_id LIMIT "
					+ MultiViewActivity.CAMERA_MAX_LIMITS);

			while (cursor.moveToNext()) {
				JSONObject muidObject = new JSONObject();
				muidObject.put("uid", cursor.getString(1));
				muidObject.put("interval", "0");
				jsonArray.put(muidObject);
			}
			cursor.close();
			db.close();
			jsonObject.put("map", jsonArray);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return jsonObject;
	}

	private String getUIDList(Context context) {
		JSONArray jsonArray = new JSONArray();

		DatabaseManager manager = new DatabaseManager(context);
		SQLiteDatabase db = manager.getReadableDatabase();
		Cursor cursor = db.query(DatabaseManager.TABLE_DEVICE, new String[] { "_id", "dev_uid" }, null, null, null, null, "_id LIMIT "
				+ MultiViewActivity.CAMERA_MAX_LIMITS);

		while (cursor.moveToNext()) {
			SharedPreferences settings = mcontext.getSharedPreferences("Interval", 0);
			int position = -1;
			int interval = -1;
			position = settings.getInt(cursor.getString(1), -1);
			switch (position) {
			case INTERVAL_NO_LIMIT:
				interval = 0;
				break;
			case INTERVAL_1_MIN:
				interval = 60;
				break;
			case INTERVAL_3_MIN:
				interval = 180;
				break;
			case INTERVAL_5_MIN:
				interval = 300;
				break;
			case INTERVAL_10_MIN:
				interval = 600;
				break;
			case INTERVAL_30_MIN:
				interval = 1800;
				break;
			default:
				interval = 0;
				break;
			}
			
			JSONObject muidObject = new JSONObject();
			
			try {
				muidObject.put("uid", cursor.getString(1));
				muidObject.put("interval", ""+interval);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			jsonArray.put(muidObject);
		}
		cursor.close();
		db.close();
		Base64 base64 = new Base64();
		String encodedText = new String(base64.encode(jsonArray.toString().getBytes())); 

		return encodedText;
	}
}
