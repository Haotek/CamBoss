package com.tutk.P2PCam264;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import android.graphics.Bitmap;
import android.util.Log;

import com.tutk.IOTC.AVFrame;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.AVIOCTRLDEFs.SStreamDef;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.Packet;

public class MyCamera extends Camera implements com.tutk.IOTC.IRegisterIOTCListener {

	public int LastAudioMode;

	private String mName;
	public String mUID;
	private String mAcc;
	private String mPwd;

	private int mMonitorIndex = -1;
	private int mEventCount = 0;
	private int cbSize = 0;
	private int nIsSupportTimeZone = 0;
	private int nGMTDiff = 0;
	private byte[] szTimeZoneString = new byte[256];

	private boolean bIsMotionDetected;
	private boolean bIsIOAlarm;

	private UUID mUUID = UUID.randomUUID();
	private List<SStreamDef> mStreamDefs = Collections.synchronizedList(new ArrayList<SStreamDef>());

	public MyCamera(String name, String uid, String acc, String pwd) {
		mName = name;
		if (uid != null) {
			mUID = uid;
		}
		mAcc = acc;
		mPwd = pwd;
		this.registerIOTCListener(this);
	}

	@Override
	public void connect(String uid) {
		// TODO Auto-generated method stub
		super.connect(uid);
		if (uid != null) {
			mUID = uid;
		}
	}

	@Override
	public void connect(String uid, String pwd) {
		super.connect(uid, pwd);
		if (uid != null) {
			mUID = uid;
		}
	}

	@Override
	public void disconnect() {
		super.disconnect();
		mStreamDefs.clear();
	}

	public int getMonitorIndex() {
		return mMonitorIndex;
	}

	public void setMonitorIndex(int MonitorIndex) {
		mMonitorIndex = MonitorIndex;
	}

	public String getUUID() {
		return mUUID.toString();
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getUID() {
		return mUID;
	}

	public String getPassword() {
		return mPwd;
	}

	public void setPassword(String pwd) {
		mPwd = pwd;
	}

	public void resetEventCount() {
		mEventCount = 0;
	}

	public int getEventCount() {
		return mEventCount;
	}

	public int getIsSupportTimeZone() {// 0 = flase, 1 = true;
		return nIsSupportTimeZone;
	}

	public int getGMTDiff() {
		return nGMTDiff;
	}

	public byte[] getTimeZoneString() {

		int stringLength = 0;
		for (int i = 0; i < szTimeZoneString.length; i++) {
			if (szTimeZoneString[i] == 0) {
				stringLength = i;
				break;
			}
		}

		byte[] TimeZone = new byte[stringLength];
		System.arraycopy(szTimeZoneString, 0, TimeZone, 0, stringLength);

		return TimeZone;
	}

	public SStreamDef[] getSupportedStream() {
		SStreamDef[] result = new SStreamDef[mStreamDefs.size()];

		for (int i = 0; i < result.length; i++)
			result[i] = mStreamDefs.get(i);

		return result;
	}

	public boolean getAudioInSupported(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 1) == 0;
	}

	public boolean getAudioOutSupported(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 2) == 0;
	}

	public boolean getPanTiltSupported(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 4) == 0;
	}

	public boolean getEventListSupported(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 8) == 0;
	}

	public boolean getPlaybackSupported(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 16) == 0;
	}

	public boolean getWiFiSettingSupported(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 32) == 0;
	}

	public boolean getEventSettingSupported(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 64) == 0;
	}

	public boolean getRecordSettingSupported(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 128) == 0;
	}

	public boolean getSDCardFormatSupported(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 256) == 0;
	}

	public boolean getVideoFlipSupported(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 512) == 0;
	}

	public boolean getEnvironmentModeSupported(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 1024) == 0;
	}

	public boolean getMultiStreamSupported(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 2048) == 0;
	}

	public int getAudioOutEncodingFormat(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 4096) == 0 ? AVFrame.MEDIA_CODEC_AUDIO_SPEEX : AVFrame.MEDIA_CODEC_AUDIO_ADPCM;
	}

	public boolean getVideoQualitySettingSupport(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 8192) == 0;
	}

	public boolean getDeviceInfoSupport(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 16384) == 0;
	}

	public boolean getSyncToCloudSupport(int avChannel) {
		return (this.getChannelServiceType(avChannel) & 32768) == 0;
	}

	public boolean getTimeZone(int avChannel) {
		return (this.getChannelServiceType(avChannel) & (2 << 15)) == 0;
	}

	@Override
	public void receiveChannelInfo(Camera arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void receiveFrameData(Camera arg0, int arg1, Bitmap arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void receiveFrameInfo(Camera arg0, int arg1, long arg2, int arg3, int arg4, int arg5, int arg6) {
		// TODO Auto-generated method stub

	}

	@Override
	public void receiveIOCtrlData(final Camera camera, final int avChannel, int avIOCtrlMsgType, final byte[] data) {

		if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETSUPPORTSTREAM_RESP) {

			mStreamDefs.clear();

			int num = Packet.byteArrayToInt_Little(data, 0);

			if (avChannel == 0 && this.getMultiStreamSupported(0)) {

				for (int i = 0; i < num; i++) {

					byte[] buf = new byte[8];
					System.arraycopy(data, i * 8 + 4, buf, 0, 8);
					SStreamDef streamDef = new SStreamDef(buf);
					mStreamDefs.add(streamDef);

					camera.start(streamDef.channel, mAcc, mPwd);
				}
			}

		} else if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_EVENT_REPORT) {

			int evtType = Packet.byteArrayToInt_Little(data, 12);

			if (evtType == AVIOCTRLDEFs.AVIOCTRL_EVENT_MOTIONDECT) {
				if (!bIsMotionDetected)
					mEventCount++;
				bIsMotionDetected = true;
			} else if (evtType == AVIOCTRLDEFs.AVIOCTRL_EVENT_MOTIONPASS) {
				bIsMotionDetected = false;
			} else if (evtType == AVIOCTRLDEFs.AVIOCTRL_EVENT_IOALARM) {
				if (!bIsIOAlarm)
					mEventCount++;
				bIsIOAlarm = true;
			} else if (evtType == AVIOCTRLDEFs.AVIOCTRL_EVENT_IOALARMPASS) {
				bIsIOAlarm = false;
			}
		} else if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIMEZONE_RESP) {

			byte[] bcbSize = new byte[4];
			byte[] bIsSupportTimeZone = new byte[4];
			byte[] bGMTDiff = new byte[4];

			System.arraycopy(data, 0, bcbSize, 0, 4);
			cbSize = Packet.byteArrayToInt_Little(bcbSize);

			System.arraycopy(data, 4, bIsSupportTimeZone, 0, 4);
			nIsSupportTimeZone = Packet.byteArrayToInt_Little(bIsSupportTimeZone);

			System.arraycopy(data, 8, bGMTDiff, 0, 4);
			nGMTDiff = Packet.byteArrayToInt_Little(bGMTDiff);

			System.arraycopy(data, 12, szTimeZoneString, 0, 256);
			try {
				Log.i("szTimeZoneString", new String(szTimeZoneString, 0, szTimeZoneString.length, "utf-8"));
				Log.i("szTimeZoneString", new String(szTimeZoneString, 0, szTimeZoneString.length, "utf-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_TIMEZONE_RESP) {

			byte[] bcbSize = new byte[4];
			byte[] bIsSupportTimeZone = new byte[4];
			byte[] bGMTDiff = new byte[4];

			System.arraycopy(data, 0, bcbSize, 0, 4);
			cbSize = Packet.byteArrayToInt_Little(bcbSize);

			System.arraycopy(data, 4, bIsSupportTimeZone, 0, 4);
			nIsSupportTimeZone = Packet.byteArrayToInt_Little(bIsSupportTimeZone);

			System.arraycopy(data, 8, bGMTDiff, 0, 4);
			nGMTDiff = Packet.byteArrayToInt_Little(bGMTDiff);

			System.arraycopy(data, 12, szTimeZoneString, 0, 256);

//			byte[] bcbSize = null;
//			
//			System.arraycopy(data, 0, bcbSize, 0, 4);	
//			cbSize = Packet.byteArrayToInt_Little(bcbSize);
//
//			if(cbSize == data.length)
//			{
//				Log.i("IOTYPE_USER_IPCAM_SET_TIMEZONE_RESP ", "cbSize = " + cbSize);
//			}
		}
	}

	@Override
	public void receiveSessionInfo(Camera arg0, int arg1) {

	}

	@Override
	public void receiveFrameDataForMediaCodec(Camera camera, int avChannel, byte[] buf, int length, int pFrmNo, byte[] pFrmInfoBuf, boolean isIframe,
			int codecId) {
		// TODO Auto-generated method stub

	}
}
