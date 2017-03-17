package com.tutk.P2PCam264.DELUX;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.Kalay.general.R;
import com.tutk.P2PCam264.DeviceInfo;
import com.tutk.P2PCam264.EditDeviceActivity;
import com.tutk.P2PCam264.LiveViewActivity;
import com.tutk.P2PCam264.MyCamera;

import java.io.File;
import java.util.ArrayList;

import addition.TUTK.qr_codeActivity;
import general.DatabaseManager;
import general.ThreadTPNS;

public class DeviceListFragment extends Fragment {

    public static int nShowMessageCount = 0;
    public static final int CAMERA_MAX_LIMITS = 4;

    private static Context mContext;
    private static ListView mCameraList;
    private static DeviceListAdapter mAdapter;
    private static View addDeviceView;

    private static boolean mIsRemovable = false;
    private static ArrayList<Boolean> mRemoveList = new ArrayList<Boolean>();

    public DeviceListFragment() {
        // prevent for InstantiationException
    }

    public DeviceListFragment (Context context) {
        mContext = context;
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.device_list_fragment, container, false);

        mCameraList = (ListView) view.findViewById(R.id.lstCameraList);
        addDeviceView = ((Activity) mContext).getLayoutInflater().inflate(R.layout.add_device_row, null);
        mAdapter = new DeviceListAdapter(mContext);
        mCameraList.addFooterView(addDeviceView);
        mCameraList.setAdapter(mAdapter);
        mCameraList.setOnItemClickListener(listViewOnItemClickListener);

        verifyCameraLimit();

        return view;
    }

    private class DeviceListAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public DeviceListAdapter (Context context) {

            this.mInflater = LayoutInflater.from(context);
        }

        public int getCount () {

            return MultiViewActivity.DeviceList.size();
        }

        public Object getItem (int position) {

            return MultiViewActivity.DeviceList.get(position);
        }

        public long getItemId (int position) {

            return position;
        }

        public View getView (final int position, View convertView, ViewGroup parent) {

            final DeviceInfo dev = MultiViewActivity.DeviceList.get(position);
            final MyCamera cam = MultiViewActivity.CameraList.get(position);

            if (dev == null || cam == null) {
                return null;
            }

            ViewHolder holder = null;

            if (convertView == null) {

                convertView = mInflater.inflate(R.layout.device_list, null);

                holder = new ViewHolder();
                holder.img = (ImageView) convertView.findViewById(R.id.img);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.info = (TextView) convertView.findViewById(R.id.info);
                holder.status = (TextView) convertView.findViewById(R.id.status);
                holder.eventLayout = (FrameLayout) convertView.findViewById(R.id.eventLayout);
                holder.GCM_Prompt = (TextView) convertView.findViewById(R.id.GCM_Prompt);
                holder.more = (ImageView) convertView.findViewById(R.id.more);
                holder.btnStatus = (ImageButton) convertView.findViewById(R.id.btnStatus);
                convertView.setTag(holder);

            } else {

                holder = (ViewHolder) convertView.getTag();
            }

            if (holder != null) {

                holder.eventLayout.setVisibility(View.VISIBLE);
                if (mIsRemovable) {
                    holder.eventLayout.setVisibility(View.GONE);
                    if (mRemoveList.get(position)) {
                        holder.btnStatus.setBackgroundResource(R.drawable.btn_delete_h);
                    } else {
                        holder.btnStatus.setBackgroundResource(R.drawable.btn_delete);
                    }
                    holder.btnStatus.setVisibility(View.VISIBLE);
                    holder.btnStatus.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick (View v) {
                            mRemoveList.set(position, ! mRemoveList.get(position));
                            notifyDataSetChanged();
                        }
                    });
                } else {
                    if (dev.Status.equals(getText(R.string.connstus_connected).toString())) {
                        holder.btnStatus.setVisibility(View.GONE);
                    } else if (dev.Status.equals(getText(R.string.connstus_unknown_device).toString()) || dev.Status.equals(getText(R.string
                            .connstus_wrong_password).toString())) {
                        holder.btnStatus.setBackgroundResource(R.drawable.btn_error);
                        holder.btnStatus.setVisibility(View.VISIBLE);
                    } else {
                        holder.btnStatus.setBackgroundResource(R.drawable.btn_refresh_switch);
                        holder.btnStatus.setVisibility(View.VISIBLE);
                        holder.btnStatus.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick (View v) {
                                if ((dev.Status.equals(getText(R.string.connstus_unknown_device).toString()) || dev.Status.equals(getText(R.string
                                        .connstus_disconnect).toString()) || dev.Status.equals(getText(R.string.connstus_connection_failed)
                                        .toString()))) {
                                    MyCamera camera = MultiViewActivity.CameraList.get(position);
                                    camera.disconnect();
                                    camera.connect(dev.UID);
                                    camera.start(MyCamera.DEFAULT_AV_CHANNEL, dev.View_Account, dev.View_Password);
                                    camera.sendIOCtrl(MyCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_REQ,
                                            AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoReq.parseContent());
                                    camera.sendIOCtrl(MyCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETSUPPORTSTREAM_REQ,
                                            AVIOCTRLDEFs.SMsgAVIoctrlGetSupportStreamReq.parseContent());
                                    camera.sendIOCtrl(MyCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETAUDIOOUTFORMAT_REQ,
                                            AVIOCTRLDEFs.SMsgAVIoctrlGetAudioOutFormatReq.parseContent());
                                    camera.sendIOCtrl(MyCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIMEZONE_REQ,
                                            AVIOCTRLDEFs.SMsgAVIoctrlTimeZone.parseContent());
                                }
                            }
                        });
                    }
                }

                holder.img.setImageBitmap(dev.Snapshot);
                holder.title.setText(dev.NickName);
                holder.info.setText(dev.UID);
                if (dev.n_gcm_count == 0) {
                    holder.GCM_Prompt.setVisibility(View.GONE);
                } else {
                    holder.GCM_Prompt.setVisibility(View.VISIBLE);
                    holder.GCM_Prompt.setText(Integer.toString(dev.n_gcm_count));
                }
                if (nShowMessageCount == 0) {
                    holder.status.setText(dev.Status);
                } else {
                    holder.status.setText(dev.Status + " " + cam.gettempAvIndex());
                }

                holder.more.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick (View v) {
                        Bundle extras = new Bundle();
                        extras.putString("dev_uid", dev.UID);
                        extras.putString("dev_uuid", dev.UUID);
                        extras.putString("dev_nickname", dev.NickName);
                        extras.putString("conn_status", dev.Status);
                        extras.putString("view_acc", dev.View_Account);
                        extras.putString("view_pwd", dev.View_Password);
                        extras.putInt("camera_channel", 0);
                        Intent intent = new Intent();
                        intent.putExtras(extras);
                        intent.setClass(getActivity(), EditDeviceActivity.class);
                        getActivity().startActivityForResult(intent, MultiViewActivity.REQUEST_CODE_CAMERA_EDIT);
                        getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    }
                });

            }

            return convertView;

        }

        public final class ViewHolder {
            public ImageView img;
            public TextView title;
            public TextView info;
            public TextView status;
            public TextView GCM_Prompt;
            public FrameLayout eventLayout;
            public ImageView more;
            public ImageButton btnStatus;
        }

        private void removeItemFromList (int position) {

            DatabaseManager manager = new DatabaseManager(mContext);
            SQLiteDatabase db = manager.getReadableDatabase();
            Cursor cursor = db.query(DatabaseManager.TABLE_SNAPSHOT, new String[] {"_id", "dev_uid", "file_path", "time"},
                    "dev_uid = '" + MultiViewActivity.DeviceList.get(position).UID + "'", null, null, null,
                    "_id LIMIT " + MultiViewActivity.CAMERA_MAX_LIMITS);
            while (cursor.moveToNext()) {
                String file_path = cursor.getString(2);
                File file = new File(file_path);
                if (file.exists()) {
                    file.delete();
                }
            }
            cursor.close();
            db.close();

            ThreadTPNS threadTPNS = new ThreadTPNS(mContext, MultiViewActivity.DeviceList.get(position).UID, ThreadTPNS.MAPPING_UNREG);
            threadTPNS.start();

            manager.removeSnapshotByUID(MultiViewActivity.DeviceList.get(position).UID);
            manager.removeDeviceByUID(MultiViewActivity.DeviceList.get(position).UID);
            MyCamera myCamera = MultiViewActivity.CameraList.get(position);
            DeviceInfo deviceInfo = MultiViewActivity.DeviceList.get(position);

            myCamera.stop(Camera.DEFAULT_AV_CHANNEL);
            myCamera.disconnect();
//			myCamera.unregisterIOTCListener(mActivity);

            MultiViewActivity.DeviceList.remove(position);
            MultiViewActivity.CameraList.remove(position);
            this.notifyDataSetChanged();
            MultiViewActivity.removeFromMultiView(deviceInfo.UID, deviceInfo.UUID);
            verifyCameraLimit();
        }
    }

    private AdapterView.OnItemClickListener listViewOnItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick (AdapterView<?> arg0, View v, int position, long id) {

            if (position < MultiViewActivity.DeviceList.size()) {

                if (MultiViewActivity.DeviceList.get(position) == null) {
                    return;
                }

                Bundle extras = new Bundle();
                extras.putString("dev_uid", MultiViewActivity.DeviceList.get(position).UID);
                extras.putString("dev_uuid", MultiViewActivity.DeviceList.get(position).UUID);
                extras.putString("dev_nickname", MultiViewActivity.DeviceList.get(position).NickName);
                extras.putString("conn_status", MultiViewActivity.DeviceList.get(position).Status);
                extras.putString("view_acc", MultiViewActivity.DeviceList.get(position).View_Account);
                extras.putString("view_pwd", MultiViewActivity.DeviceList.get(position).View_Password);
                extras.putInt("camera_channel", 0);
                extras.putInt("MonitorIndex", - 1);

                extras.putString("OriginallyUID", MultiViewActivity.DeviceList.get(position).UID);
                extras.putInt("OriginallyChannelIndex", 0);
                Intent intent = new Intent();
                intent.putExtras(extras);
                intent.setClass(getActivity(), LiveViewActivity.class);
                getActivity().startActivityForResult(intent, MultiViewActivity.REQUEST_CODE_CAMERA_VIEW);
                getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            } else {
                if (MultiViewActivity.CameraList.size() < CAMERA_MAX_LIMITS) {

                    Intent intent = new Intent();
                    intent.setClass(mContext, qr_codeActivity.class);
                    startActivityForResult(intent, MultiViewActivity.REQUEST_CODE_CAMERA_ADD);
                    getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                }
            }
        }
    };

    private static void verifyCameraLimit () {

        if (MultiViewActivity.DeviceList.size() < CAMERA_MAX_LIMITS) {

            if (mCameraList.getFooterViewsCount() == 0) {
                mCameraList.addFooterView(addDeviceView);
                mAdapter.notifyDataSetChanged();
            }

        } else {

            if (mCameraList.getFooterViewsCount() > 0) {
                mCameraList.removeFooterView(addDeviceView);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    public static void showHideDelelteLayout () {
        if (mIsRemovable) {
            mIsRemovable = ! mIsRemovable;
            mAdapter.notifyDataSetChanged();

        } else {
            mIsRemovable = ! mIsRemovable;
            mRemoveList.clear();
            for (int i = 0 ; i < MultiViewActivity.DeviceList.size() ; i++) {
                mRemoveList.add(false);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    public static boolean checkDelete () {
        for (int i = 0 ; i < mRemoveList.size() ; i++) {
            if (mRemoveList.get(i)) {
                return true;
            }
        }
        return false;
    }

    public static void doDelete () {
        if (mIsRemovable) {
            for (int i = 0 ; i < mRemoveList.size() ; i++) {
                if (mRemoveList.get(i)) {
                    mAdapter.removeItemFromList(i);
                    mRemoveList.remove(i);
                    i--;
                }
            }
        }
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MultiViewActivity.REQUEST_CODE_CAMERA_ADD) {

            switch (resultCode) {
                case MultiViewActivity.RESULT_OK:
                    Bundle extras = data.getExtras();
                    long db_id = extras.getLong("db_id");
                    String dev_nickname = extras.getString("dev_nickname");
                    String dev_uid = extras.getString("dev_uid");
                    String view_acc = extras.getString("view_acc");
                    String view_pwd = extras.getString("view_pwd");
                    int event_notification = 3;
                    int channel = extras.getInt("camera_channel");

                    MyCamera camera = new MyCamera(dev_nickname, dev_uid, view_acc, view_pwd);
                    DeviceInfo dev = new DeviceInfo(db_id, camera.getUUID(), dev_nickname, dev_uid, view_acc, view_pwd, "", event_notification, channel, null, null, null, null, null);
                    MultiViewActivity.DeviceList.add(dev);

//				camera.registerIOTCListener(this);
                    camera.registerIOTCListener(MultiViewActivity.getMultiViewActivityIRegisterIOTCListener());
                    camera.connect(dev_uid);
                    camera.start(MyCamera.DEFAULT_AV_CHANNEL, view_acc, view_pwd);
                    camera.sendIOCtrl(MyCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_REQ, AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoReq.parseContent());
                    camera.sendIOCtrl(MyCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETSUPPORTSTREAM_REQ, AVIOCTRLDEFs.SMsgAVIoctrlGetSupportStreamReq.parseContent());
                    camera.sendIOCtrl(MyCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETAUDIOOUTFORMAT_REQ, AVIOCTRLDEFs.SMsgAVIoctrlGetAudioOutFormatReq.parseContent());
                    camera.sendIOCtrl(MyCamera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIMEZONE_REQ, AVIOCTRLDEFs.SMsgAVIoctrlTimeZone.parseContent());

                    if (MultiViewActivity.SupportEasyWiFiSetting) {

                        // 簡易wifi設定
                        if (extras.getString("wifi_ssid") != null && extras.getString("wifi_password") != null) {
                            SharedPreferences settings = mContext.getSharedPreferences("WiFi Setting", 0);
                            settings.edit().putString("wifi_uid", dev_uid).commit();
                            settings.edit().putString("wifi_ssid", extras.getString("wifi_ssid")).commit();
                            settings.edit().putString("wifi_password", extras.getString("wifi_password")).commit();
                            settings.edit().putInt("wifi_enc", extras.getInt("wifi_enc")).commit();
                        }
                    }

                    camera.LastAudioMode = 1;

                    MultiViewActivity.CameraList.add(camera);
                    DeviceListFragment.notifyData();
                    break;
            }
        }
    }

    public static void notifyData () {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        verifyCameraLimit();
    }

}
