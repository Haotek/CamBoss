package com.tutk.P2PCam264;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.tutk.Logger.Glog;
import com.tutk.P2PCam264.DELUX.Custom_OkCancle_Dialog;
import com.tutk.P2PCam264.DELUX.Custom_OkCancle_Dialog.OkCancelDialogListener;
import com.tutk.P2PCam264.DELUX.NicknameListActivity;
import com.tutk.P2PCam264.object.VideoFile;
import com.tutk.Kalay.general.R;
import com.tutk.Kalay.general.R.drawable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class GridViewGalleryActivity extends SherlockActivity implements OkCancelDialogListener {

    private String TAG = "GridViewGalleryActivity";

    private String imagesPath;
    private String videosPath;
    private ListView lvChannel;
    private RelativeLayout bottombar;
    private RelativeLayout layoutNull;

    private Button btnEdit;
    private ImageButton btnDel;
    private ImageView imgNull;
    private TextView txtNull;
    RelativeLayout btn_change_mode;
    Button btnPhoto;
    Button btnVideo;

    private boolean mThumbnaillDone = false;
    private boolean mIsLongPress = false;
    private boolean imageNeedToFresh = true;
    private boolean videoNeedToFresh = true;
    private File LongPressFile;

    private enum MyMode {
        PHOTO, VIDEO
    }

    private MyMode mMode = MyMode.PHOTO;
    private Time time = new Time();

    /**
     * The Constant DEFAULT_LIST_SIZE.
     */
    private static final int DEFAULT_LIST_SIZE = 1;
    /**
     * The Constant IMAGE_RESOURCE_IDS.
     */
    final List<String> IMAGE_FILES = new ArrayList<String>(DEFAULT_LIST_SIZE);
    private List<String> imageChannelList = new ArrayList<String>(DEFAULT_LIST_SIZE);
    private List<String> videoChannelList = new ArrayList<String>(DEFAULT_LIST_SIZE);
    private List<VideoFile> videoFiles = new ArrayList<VideoFile>(DEFAULT_LIST_SIZE);
    private List<Boolean> multiDel_photo = new ArrayList<Boolean>();
    private List<Boolean> multiDel_video = new ArrayList<Boolean>();
    private HashMap<Integer, List<String>> mapImagePath = new HashMap<Integer, List<String>>();
    private HashMap<Integer, List<VideoFile>> mapVideoFile = new HashMap<Integer, List<VideoFile>>();
    private HashMap<Integer, List<Boolean>> map_multiDel_image = new HashMap<Integer, List<Boolean>>();
    private HashMap<Integer, List<Boolean>> map_multiDel_video = new HashMap<Integer, List<Boolean>>();
    private ChListAdapter adapterChannel;
    private ImageAdapter imageAdapter;
    private VideoAdapter adapterVideo;
    private Object mLock = new Object();
    private Handler handler = new Handler();
    private boolean mIsEdit = false;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.titlebar);
        TextView tv = (TextView) this.findViewById(R.id.bar_text);
        btnEdit = (Button) findViewById(R.id.bar_right_btn);
        btn_change_mode = (RelativeLayout) findViewById(R.id.bar_gallery);
        btnPhoto = (Button) findViewById(R.id.bar_btn_photo);
        btnVideo = (Button) findViewById(R.id.bar_btn_video);

        tv.setVisibility(View.GONE);
        btnEdit.setText(R.string.txt_edit);
        btnEdit.setTextColor(Color.WHITE);
        btnEdit.setVisibility(View.VISIBLE);
        btn_change_mode.setVisibility(View.VISIBLE);
        btnPhoto.setBackgroundResource(drawable.btn_tabl_h);
        btnPhoto.setTextColor(Color.BLACK);

        btnEdit.setOnClickListener(btnEditClick);
        btnPhoto.setOnClickListener(mode_change);
        btnVideo.setOnClickListener(mode_change);

        super.onCreate(savedInstanceState);
        System.gc();
        Bundle extras = this.getIntent().getExtras();
        imagesPath = extras.getString("images_path"); // XXX: extras may be null and data stored in
        // intent directly. WTF
        videosPath = extras.getString("videos_path");
        int enterMode = extras.getInt("mode");

        setContentView(R.layout.gridviewgalleryactivity);
        bottombar = (RelativeLayout) findViewById(R.id.gridview_bottom);
        layoutNull = (RelativeLayout) findViewById(R.id.layoutNull);
        imgNull = (ImageView) findViewById(R.id.imgNull);
        txtNull = (TextView) findViewById(R.id.txtNull);
        btnDel = (ImageButton) findViewById(R.id.gridview_btn_delete);
        btnDel.setOnClickListener(btnDelClick);
        removeCorruptImage();
        setImageRootFolder(imagesPath);
        setVideoRootFolder(videosPath);
        setPhotoData();

        adapterChannel = new ChListAdapter(this);
        lvChannel = (ListView) findViewById(R.id.list_ch);
        lvChannel.setAdapter(adapterChannel);

        boolean hasFile = false;
        for (int i = 0 ; i < mapImagePath.size() ; i++) {
            if (mapImagePath.get(i).size() != 0) {
                hasFile = true;
                break;
            }
        }
        if (hasFile) {
            lvChannel.setVisibility(View.VISIBLE);
            layoutNull.setVisibility(View.GONE);
        } else {
            lvChannel.setVisibility(View.GONE);
            layoutNull.setVisibility(View.VISIBLE);
            imgNull.setBackgroundResource(drawable.ic_noimage);
            txtNull.setText(getText(R.string.txt_no_photos));
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run () {
                adapterChannel.notifyDataSetChanged();
            }
        }, 200);

        if (enterMode == NicknameListActivity.GALLERY_VIDEO) {
            mMode = MyMode.VIDEO;
            mode_change();
        }

    }

    private void setImageRootFolder (String path) {

        imageChannelList.clear();
        File folder = new File(path);
        File[] filesByChannels = folder.listFiles();

        if (filesByChannels != null) {
            if (filesByChannels.length > 0) {
                for (File mChFile : filesByChannels) {
                    if (mChFile.isDirectory() && mChFile.getName().startsWith("CH")) {
                        imageChannelList.add(mChFile.getName());
                    }
                }
            }
            Collections.sort(imageChannelList);
        }
    }

    private void setPhotoData () {

        for (int position = 0 ; position < imageChannelList.size() ; position++) {
            setImagesPath(imagesPath + "/" + imageChannelList.get(position));

            List<String> tempList = new ArrayList<String>();
            tempList.addAll(IMAGE_FILES);
            mapImagePath.put(position, tempList);

            List<Boolean> tempDel = new ArrayList<Boolean>();
            tempDel.addAll(multiDel_photo);
            map_multiDel_image.put(position, tempDel);
        }
        imageNeedToFresh = false;
    }

    public final synchronized void setImagesPath (String path) {
        IMAGE_FILES.clear();
        multiDel_photo.clear();
        File folder = new File(path);
        String[] imageFiles = folder.list();

        if (imageFiles != null && imageFiles.length > 0) {
            Arrays.sort(imageFiles);
            for (String imageFile : imageFiles) {
                IMAGE_FILES.add(path + "/" + imageFile);
                multiDel_photo.add(false);
            }
            Collections.reverse(IMAGE_FILES);
        }
    }

    private void setVideoRootFolder (String path) {

        videoChannelList.clear();
        File folder = new File(path);
        File[] filesByChannels = folder.listFiles();

        if (filesByChannels != null) {
            if (filesByChannels.length > 0) {
                for (File mChFile : filesByChannels) {
                    if (mChFile.isDirectory() && mChFile.getName().startsWith("CH")) {
                        videoChannelList.add(mChFile.getName());
                    }
                }
            }
            Collections.sort(videoChannelList);
        }
    }

    private void setVideoData () {
        Thread mThread = new Thread(new Runnable() {
            @Override
            public void run () {
                setVideoImage(videosPath);
            }
        });

        for (int position = 0 ; position < videoChannelList.size() ; position++) {
            setVideoPath(videosPath + "/" + videoChannelList.get(position));
            List<VideoFile> tempFileList = new ArrayList<VideoFile>();
            tempFileList.addAll(videoFiles);
            mapVideoFile.put(position, tempFileList);
            List<Boolean> tempDel = new ArrayList<Boolean>();
            tempDel.addAll(multiDel_video);
            map_multiDel_video.put(position, tempDel);
        }

        if (! mThumbnaillDone) {
            mThread.start();
        }
        videoNeedToFresh = false;
    }

    private void setVideoPath (final String path) {

        videoFiles.clear();

        multiDel_video.clear();
        File folder = new File(path);
        String[] mFiles = folder.list();

        if (mFiles != null && mFiles.length > 0) {
            Arrays.sort(mFiles);
            for (final String videofile : mFiles) {
                VideoFile file = new VideoFile();
                file.setPath(path + "/" + videofile);
                file.setName(videofile);
                videoFiles.add(file);

                multiDel_video.add(false);
            }
        }
    }

    private void setVideoImage (final String path) {
        Glog.I(TAG, "Start to build the thumnail");
        File folder;
        String[] videoFiles = null;

        for (int i = 0 ; i < videoChannelList.size() ; i++) {
            folder = new File(path + "/" + videoChannelList.get(i));
            videoFiles = folder.list();
            int j = 0;

            if (videoFiles != null && videoFiles.length > 0) {
                Arrays.sort(videoFiles);
                for (final String videofile : videoFiles) {
                    Bitmap bmp = ThumbnailUtils.createVideoThumbnail(path + "/" + videoChannelList.get(i) + "/" + videofile,
                            MediaStore.Video.Thumbnails.MINI_KIND);
                    mapVideoFile.get(i).get(j).setImage(bmp);

                    File mVideo = new File(path + "/" + videoChannelList.get(i) + "/" + videofile);
                    Uri uri = Uri.fromFile(mVideo);
                    MediaPlayer mp = MediaPlayer.create(GridViewGalleryActivity.this, uri);
                    if (mp != null) {
                        time.set(mp.getDuration());
                        mapVideoFile.get(i).get(j).setDuration(time.format("%M:%S"));
                        mp.release();
                    }

                    j++;
                }
                Glog.I(TAG, "Thumbnaill done");
            }
        }

        mThumbnaillDone = true;

        if (mMode == MyMode.VIDEO) {
            runOnUiThread(new Runnable() {
                @Override
                public void run () {
                    adapterChannel.notifyDataSetChanged();
                }
            });
        }
    }

    public final void removeCorruptImage () {
        Iterator<String> it = IMAGE_FILES.iterator();
        while (it.hasNext()) {
            String path = it.next();
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            // XXX: CA's hack, snapshot may fail and create corrupted bitmap
            if (bitmap == null) {
                it.remove();
            }
        }
    }

    public class ChListAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private Context mContext;

        public ChListAdapter (Context c) {
            this.mInflater = LayoutInflater.from(c);
            mContext = c;
        }

        public int getCount () {
            if (mMode == MyMode.PHOTO) {
                return imageChannelList.size();
            } else {
                return videoChannelList.size();
            }
        }

        public Object getItem (int position) {
            return null;
        }

        public long getItemId (int position) {
            return position;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView (final int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            convertView = new RelativeLayout(mContext);
            holder = new ViewHolder();

            View view = mInflater.inflate(R.layout.listview_gallery_channel, null);
            view.setPadding(8, 8, 8, 8);
            ((ViewGroup) convertView).addView(view);
            holder.txtCh = (TextView) convertView.findViewById(R.id.txtChannel);
            holder.gridview = (GridView) convertView.findViewById(R.id.gridview);

            if (holder != null) {
                if (mMode == MyMode.PHOTO) {
                    holder.txtCh.setText(imageChannelList.get(position));
                    imageAdapter = new ImageAdapter(GridViewGalleryActivity.this, mapImagePath.get(position));
                    holder.gridview.setId(position);
                    holder.gridview.setAdapter(imageAdapter);
                } else {
                    holder.txtCh.setText(videoChannelList.get(position));
                    adapterVideo = new VideoAdapter(GridViewGalleryActivity.this, mapVideoFile.get(position));
                    holder.gridview.setId(position);
                    holder.gridview.setAdapter(adapterVideo);
                }
            }

            return convertView;
        }

        public final class ViewHolder {
            public TextView txtCh;
            public GridView gridview;
        }
    }

    public class ImageAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private Context mContext;
        private List<String> FILES = new ArrayList<String>();

        public ImageAdapter (Context c) {
            this.mInflater = LayoutInflater.from(c);
            mContext = c;
        }

        public ImageAdapter (Context c, List<String> files) {
            this.mInflater = LayoutInflater.from(c);
            mContext = c;
            FILES = files;
        }

        public int getCount () {
            return FILES.size();
        }

        public Object getItem (int position) {
            return null;
        }

        public long getItemId (int position) {
            return position;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView (final int position, View convertView, final ViewGroup parent) {
            ViewHolder holder = null;

            if (convertView == null) {
                convertView = new RelativeLayout(mContext);
                holder = new ViewHolder();
                View view = mInflater.inflate(R.layout.gridview_photo_item, null);
//				view.setPadding(8, 8, 8, 8);
//				view.setPadding(50, 0, 50, 0);
                ((ViewGroup) convertView).addView(view);
                holder.del_check_img = (RelativeLayout) convertView.findViewById(R.id.video_image_check);
                holder.photo_thumb_img = (ImageView) convertView.findViewById(R.id.video_image);

                if (holder.photo_thumb_img != null) {
                    BitmapFactory.Options bfo = new BitmapFactory.Options();
                    bfo.inSampleSize = 4;

                    Bitmap bitmap = BitmapFactory.decodeFile(FILES.get(position), bfo);

                    // XXX: CA's hack, snapshot may fail and create corrupted bitmap
                    if (bitmap == null) {
                        for (int i = this.getCount() - 1 ; i >= 0 ; i--) {
                            bitmap = BitmapFactory.decodeFile(FILES.get(i), bfo);
                            if (bitmap != null) {
                                break;
                            }
                        }
                    }
                    int bmpHeight = bitmap.getHeight();
                    int bmpWidth = bitmap.getWidth();
                    int bmpCenterOffset = (bmpWidth - bmpHeight) / 2;
                    Bitmap square = Bitmap.createBitmap(bitmap, bmpCenterOffset, 0, bmpHeight, bmpHeight);
                    BitmapDrawable bd = new BitmapDrawable(getResources(), square);

//					holder.photo_thumb_img.setImageBitmap(bitmap);
                    holder.photo_thumb_img.setBackground(bd);
                    holder.photo_thumb_img.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick (View v) {
                            if (! mIsEdit) {
                                List<String> files = (List<String>) mapImagePath.get(parent.getId());
                                Intent intent = new Intent(GridViewGalleryActivity.this, PhotoViewerActivity.class);
                                String fileName = files.get(position);
                                int size = files.size();
                                Bundle bundle = new Bundle();
                                bundle.putStringArrayList("files", (ArrayList<String>) files);
                                bundle.putString("filename", fileName);
                                bundle.putInt("size", size);
                                bundle.putInt("pos", position);
                                intent.putExtras(bundle);
                                startActivity(intent);
                                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

                            } else {
                                map_multiDel_image.get(parent.getId()).set(position, ! map_multiDel_image.get(parent.getId()).get(position));
                                adapterChannel.notifyDataSetChanged();
                            }
                        }
                    });
                    holder.photo_thumb_img.setOnLongClickListener(new OnLongClickListener() {

                        @Override
                        public boolean onLongClick (View v) {
                            Custom_OkCancle_Dialog dlg = new Custom_OkCancle_Dialog(GridViewGalleryActivity.this,
                                    getText(R.string.dlgAreYouSureToDeleteThisSnapshot).toString());
                            dlg.setCanceledOnTouchOutside(false);
                            Window window = dlg.getWindow();
                            window.setWindowAnimations(R.style.setting_dailog_animstyle);
                            dlg.show();
                            mIsLongPress = true;
                            LongPressFile = new File(mapImagePath.get(parent.getId()).get(position));
                            return true;
                        }
                    });

                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                List<Boolean> delList = map_multiDel_image.get(parent.getId());
                if (delList.get(position)) {
                    holder.del_check_img.setVisibility(View.VISIBLE);
                } else {
                    holder.del_check_img.setVisibility(View.GONE);
                }
            }

            return convertView;
        }

        public final boolean deleteImageAtPosition (int position) {
            File file = new File(IMAGE_FILES.get(position));
            boolean deleted = file.delete();
            IMAGE_FILES.remove(position);
            this.notifyDataSetChanged();
            return deleted;
        }

        public final class ViewHolder {
            public ImageView photo_thumb_img;
            public RelativeLayout del_check_img;
        }
    }

    public class VideoAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private Context mContext;
        private List<VideoFile> FILES = new ArrayList<VideoFile>();
        private Time time = new Time();

        public VideoAdapter (Context c) {
            this.mInflater = LayoutInflater.from(c);
            mContext = c;
        }

        public VideoAdapter (Context c, List<VideoFile> files) {
            this.mInflater = LayoutInflater.from(c);
            mContext = c;
            FILES = files;
        }

        public int getCount () {
            return FILES.size();
        }

        public Object getItem (int position) {
            return null;
        }

        public long getItemId (int position) {
            return position;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView (final int position, View convertView, final ViewGroup parent) {

            ViewHolder holder = null;
            Bitmap bitmap = null;
            convertView = new RelativeLayout(mContext);
            holder = new ViewHolder();

            View view = mInflater.inflate(R.layout.gridview_video_item, null);
//			view.setPadding(8, 8, 8, 8);
            ((ViewGroup) convertView).addView(view);
            holder.del_check_img = (RelativeLayout) convertView.findViewById(R.id.video_image_check);
            holder.video_thumb_img = (ImageView) convertView.findViewById(R.id.video_image);
            holder.video_dur = (TextView) convertView.findViewById(R.id.txt_video_dur);

            if (holder != null) {
                if (mThumbnaillDone) {
                    if (mapVideoFile.get(parent.getId()).get(position).getImage() != null) {
                        bitmap = mapVideoFile.get(parent.getId()).get(position).getImage();
                        int bmpHeight = bitmap.getHeight();
                        int bmpWidth = bitmap.getWidth();
                        int bmpCenterOffset = (bmpWidth - bmpHeight) / 2;
                        Bitmap square = Bitmap.createBitmap(bitmap, bmpCenterOffset, 0, bmpHeight, bmpHeight);
                        BitmapDrawable bd = new BitmapDrawable(getResources(), square);

                        holder.video_thumb_img.setBackground(bd);
                    } else {
                        holder.video_thumb_img.setBackgroundResource(R.color.video_bg);
                    }

                    if (mapVideoFile.get(parent.getId()).get(position).getDuration() != null) {
                        holder.video_dur.setText(mapVideoFile.get(parent.getId()).get(position).getDuration());
                    }
                } else {
                    holder.video_thumb_img.setBackgroundResource(R.color.video_bg);
                }

                holder.video_thumb_img.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick (View v) {
                        if (! mIsEdit) {
                            List<String> files = new ArrayList<String>(DEFAULT_LIST_SIZE);
                            for (int i = 0 ; i < mapVideoFile.get(parent.getId()).size() ; i++) {
                                files.add(mapVideoFile.get(parent.getId()).get(i).getPath());
                            }

                            Intent intent = new Intent(GridViewGalleryActivity.this, LocalPlaybackActivity.class);
                            int mSize = files.size();
                            Bundle bundle = new Bundle();

                            bundle.putStringArrayList("videos", (ArrayList<String>) files);
                            bundle.putInt("position", position);
                            bundle.putInt("size", mSize);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

                        } else {
                            map_multiDel_video.get(parent.getId()).set(position, ! map_multiDel_video.get(parent.getId()).get(position));
                            adapterChannel.notifyDataSetChanged();
                        }
                    }
                });
                holder.video_thumb_img.setOnLongClickListener(new OnLongClickListener() {

                    @Override
                    public boolean onLongClick (View v) {
                        Custom_OkCancle_Dialog dlg = new Custom_OkCancle_Dialog(GridViewGalleryActivity.this,
                                getText(R.string.dlgAreYouSureToDeleteThisRecord).toString());
                        dlg.setCanceledOnTouchOutside(false);
                        Window window = dlg.getWindow();
                        window.setWindowAnimations(R.style.setting_dailog_animstyle);
                        dlg.show();
                        mIsLongPress = true;
                        LongPressFile = new File(mapVideoFile.get(parent.getId()).get(position).getPath());
                        return true;
                    }
                });

                List<Boolean> delList = map_multiDel_video.get(parent.getId());
                if (delList.get(position)) {
                    holder.del_check_img.setVisibility(View.VISIBLE);
                } else {
                    holder.del_check_img.setVisibility(View.GONE);
                }
            }

            return convertView;
        }

        public final class ViewHolder {
            public ImageView video_thumb_img;
            public RelativeLayout del_check_img;
            public TextView video_dur;
        }
    }

    private OnClickListener mode_change = new OnClickListener() {
        @Override
        public void onClick (View v) {
            switch (v.getId()) {
                case R.id.bar_btn_photo:
                    mMode = MyMode.PHOTO;
                    mode_change();
                    break;

                case R.id.bar_btn_video:
                    mMode = MyMode.VIDEO;
                    mode_change();
                    break;
            }
        }
    };

    private void mode_change () {
        if (mMode == MyMode.VIDEO) {

            if (videoNeedToFresh) {
                setVideoData();
            }

            btnPhoto.setBackgroundResource(drawable.btn_photo);
            try {
                btnPhoto.setTextColor(ColorStateList.createFromXml(getResources(), getResources().getXml(drawable.txt_color_gallery)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            btnVideo.setBackgroundResource(drawable.btn_tabr_h);
            btnVideo.setTextColor(Color.BLACK);

            boolean hasFile = false;
            for (int i = 0 ; i < mapVideoFile.size() ; i++) {
                if (mapVideoFile.get(i).size() != 0) {
                    hasFile = true;
                    break;
                }
            }
            if (hasFile) {
                lvChannel.setVisibility(View.VISIBLE);
                layoutNull.setVisibility(View.GONE);
            } else {
                lvChannel.setVisibility(View.GONE);
                layoutNull.setVisibility(View.VISIBLE);
                imgNull.setBackgroundResource(drawable.ic_novideo);
                txtNull.setText(getText(R.string.txt_no_videos));
            }

            adapterChannel.notifyDataSetChanged();
        } else {
            if (imageNeedToFresh) {
                setPhotoData();
            }

            btnVideo.setBackgroundResource(drawable.btn_video);
            try {
                btnVideo.setTextColor(ColorStateList.createFromXml(getResources(), getResources().getXml(drawable.txt_color_gallery)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            btnPhoto.setBackgroundResource(drawable.btn_tabl_h);
            btnPhoto.setTextColor(Color.BLACK);

            removeCorruptImage();

            boolean hasFile = false;
            for (int i = 0 ; i < mapImagePath.size() ; i++) {
                if (mapImagePath.get(i).size() != 0) {
                    hasFile = true;
                    break;
                }
            }
            if (hasFile) {
                lvChannel.setVisibility(View.VISIBLE);
                layoutNull.setVisibility(View.GONE);
            } else {
                lvChannel.setVisibility(View.GONE);
                layoutNull.setVisibility(View.VISIBLE);
                imgNull.setBackgroundResource(drawable.ic_noimage);
                txtNull.setText(getText(R.string.txt_no_photos));
            }

            adapterChannel.notifyDataSetChanged();
        }
    }

    private OnClickListener btnEditClick = new OnClickListener() {

        @Override
        public void onClick (View v) {
            if (! mIsEdit) {
                bottombar.startAnimation(AnimationUtils.loadAnimation(GridViewGalleryActivity.this, R.anim.bottombar_slide_show));
                bottombar.setVisibility(View.VISIBLE);
                btnEdit.setText(getString(R.string.cancel));
                mIsEdit = true;
            } else {
                bottombar.startAnimation(AnimationUtils.loadAnimation(GridViewGalleryActivity.this, R.anim.bottombar_slide_hide));
                bottombar.setVisibility(View.GONE);
                btnEdit.setText(R.string.txt_edit);

                HashMap<Integer, List<Boolean>> map_multiDel;
                if (mMode == MyMode.PHOTO) {
                    map_multiDel = map_multiDel_image;
                } else {
                    map_multiDel = map_multiDel_video;
                }

                for (int i = 0 ; i < map_multiDel.size() ; i++) {
                    for (int j = 0 ; j < map_multiDel.get(i).size() ; j++) {
                        map_multiDel.get(i).set(j, false);
                    }
                }

                adapterChannel.notifyDataSetChanged();
                mIsEdit = false;
            }

        }
    };

    private boolean checkDelList () {
        HashMap<Integer, List<Boolean>> map_multiDel;
        if (mMode == MyMode.PHOTO) {
            map_multiDel = map_multiDel_image;
        } else {
            map_multiDel = map_multiDel_video;
        }

        for (int i = 0 ; i < map_multiDel.size() ; i++) {
            for (int j = 0 ; j < map_multiDel.get(i).size() ; j++) {
                if (map_multiDel.get(i).get(j)) {
                    return true;
                }
            }
        }
        return false;
    }

    private OnClickListener btnDelClick = new OnClickListener() {

        @Override
        public void onClick (View v) {
            if (checkDelList()) {
                if (mMode == MyMode.PHOTO) {
                    Custom_OkCancle_Dialog dlg = new Custom_OkCancle_Dialog(GridViewGalleryActivity.this,
                            getText(R.string.dlgAreYouSureToDeleteThisSnapshot).toString());
                    dlg.setCanceledOnTouchOutside(false);
                    Window window = dlg.getWindow();
                    window.setWindowAnimations(R.style.setting_dailog_animstyle);
                    dlg.show();
                    mIsLongPress = false;

                } else {
                    Custom_OkCancle_Dialog dlg = new Custom_OkCancle_Dialog(GridViewGalleryActivity.this,
                            getText(R.string.dlgAreYouSureToDeleteThisRecord).toString());
                    dlg.setCanceledOnTouchOutside(false);
                    Window window = dlg.getWindow();
                    window.setWindowAnimations(R.style.setting_dailog_animstyle);
                    dlg.show();
                    mIsLongPress = false;
                }
            }

        }
    };

    @Override
    public void ok () {

        mThumbnaillDone = false;

        if (mIsLongPress) {
            if (mMode == MyMode.PHOTO) {
                GridViewGalleryActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run () {
                        File file = LongPressFile;
                        file.delete();

                        setPhotoData();

                        boolean hasFile = false;
                        for (int i = 0 ; i < mapImagePath.size() ; i++) {
                            if (mapImagePath.get(i).size() != 0) {
                                hasFile = true;
                                break;
                            }
                        }
                        if (hasFile) {
                            lvChannel.setVisibility(View.VISIBLE);
                            layoutNull.setVisibility(View.GONE);
                        } else {
                            lvChannel.setVisibility(View.GONE);
                            layoutNull.setVisibility(View.VISIBLE);
                            imgNull.setBackgroundResource(drawable.ic_noimage);
                            txtNull.setText(getText(R.string.txt_no_photos));
                        }

                        adapterChannel.notifyDataSetChanged();
                    }
                });
            } else {
                GridViewGalleryActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run () {
                        File file = LongPressFile;
                        file.delete();

                        if (file.exists()) {
                            try {
                                mLock.wait(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        setVideoData();

                        boolean hasFile = false;
                        for (int i = 0 ; i < mapVideoFile.size() ; i++) {
                            if (mapVideoFile.get(i).size() != 0) {
                                hasFile = true;
                                break;
                            }
                        }
                        if (hasFile) {
                            lvChannel.setVisibility(View.VISIBLE);
                            layoutNull.setVisibility(View.GONE);
                        } else {
                            lvChannel.setVisibility(View.GONE);
                            layoutNull.setVisibility(View.VISIBLE);
                            imgNull.setBackgroundResource(drawable.ic_novideo);
                            txtNull.setText(getText(R.string.txt_no_videos));
                        }

                        adapterChannel.notifyDataSetChanged();
                    }
                });
            }
        } else {
            if (mMode == MyMode.PHOTO) {
                for (int i = 0 ; i < map_multiDel_image.size() ; i++) {
                    for (int j = 0 ; j < map_multiDel_image.get(i).size() ; j++) {
                        if (map_multiDel_image.get(i).get(j)) {
                            File file = new File(mapImagePath.get(i).get(j));
                            file.delete();

                            if (file.exists()) {
                                try {
                                    mLock.wait(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                setPhotoData();

                boolean hasFile = false;
                for (int i = 0 ; i < mapImagePath.size() ; i++) {
                    if (mapImagePath.get(i).size() != 0) {
                        hasFile = true;
                        break;
                    }
                }
                if (hasFile) {
                    lvChannel.setVisibility(View.VISIBLE);
                    layoutNull.setVisibility(View.GONE);
                } else {
                    lvChannel.setVisibility(View.GONE);
                    layoutNull.setVisibility(View.VISIBLE);
                    imgNull.setBackgroundResource(drawable.ic_noimage);
                    txtNull.setText(getText(R.string.txt_no_photos));
                }
            } else {
                for (int i = 0 ; i < map_multiDel_video.size() ; i++) {
                    for (int j = 0 ; j < map_multiDel_video.get(i).size() ; j++) {
                        if (map_multiDel_video.get(i).get(j)) {
                            File file = new File(mapVideoFile.get(i).get(j).getPath());
                            file.delete();

                            if (file.exists()) {
                                try {
                                    mLock.wait(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                setVideoData();

                boolean hasFile = false;
                for (int i = 0 ; i < mapVideoFile.size() ; i++) {
                    if (mapVideoFile.get(i).size() != 0) {
                        hasFile = true;
                        break;
                    }
                }
                if (hasFile) {
                    lvChannel.setVisibility(View.VISIBLE);
                    layoutNull.setVisibility(View.GONE);
                } else {
                    lvChannel.setVisibility(View.GONE);
                    layoutNull.setVisibility(View.VISIBLE);
                    imgNull.setBackgroundResource(drawable.ic_novideo);
                    txtNull.setText(getText(R.string.txt_no_videos));
                }
            }

            adapterChannel.notifyDataSetChanged();

            bottombar.startAnimation(AnimationUtils.loadAnimation(GridViewGalleryActivity.this, R.anim.bottombar_slide_hide));
            bottombar.setVisibility(View.GONE);
            btnEdit.setText(R.string.txt_edit);
            mIsEdit = false;
        }
    }

    @Override
    public void cancel () {

    }

    @Override
    protected void onResume () {
        super.onResume();
        Custom_OkCancle_Dialog.SetDialogListener(this);
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
