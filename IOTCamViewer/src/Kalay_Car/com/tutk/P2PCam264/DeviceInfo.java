package com.tutk.P2PCam264;

import android.graphics.Bitmap;

public class DeviceInfo {

	public long DBID;
	public String UUID;
	public String NickName;
	public String UID;
	public String View_Account;
	public String View_Password;
	public String Status;
    public String AP;
    public String AP_Pwd;
    public String WIFI;
    public String WIFI_Pwd;
	public int connect_count=0;
	public int n_gcm_count=0;
	public int Mode;
	public Bitmap Snapshot;
	public int EventNotification;
	public int ChannelIndex;
	public boolean ChangePassword=false;
	public boolean Online;
	public boolean ShowTipsForFormatSDCard;
	public int mMonitorIndex=-1;
	public short nSupportDropbox=0;
	public short nLinked=0;

	public DeviceInfo(long db_id, String uuid, String nickname, String uid, String view_acc, String view_pwd, String status, int event_notification,
			int camera_channel, Bitmap snapshot, String ap, String ap_pwd, String wifi, String wifi_pwd) {

		DBID = db_id;
		UUID = uuid;
		NickName = nickname;
		UID = uid;
		View_Account = view_acc;
		View_Password = view_pwd;
		Status = status;
        AP = ap;
        AP_Pwd = ap_pwd;
        WIFI = wifi;
        WIFI_Pwd = wifi_pwd;
		EventNotification = event_notification;
		ChannelIndex = camera_channel;
		Snapshot = snapshot;
		Online = false;
		ShowTipsForFormatSDCard = true;
	}
}
