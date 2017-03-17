package addition.TUTK;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.tutk.Kalay.general.R;
import com.tutk.P2PCam264.DELUX.Custom_Ok_Dialog;
import com.tutk.P2PCam264.DELUX.MultiViewActivity;
import com.tutk.P2PCam264.MyCamera;
import com.tutk.zxing.CameraManager;
import com.tutk.zxing.CaptureActivityHandler;
import com.tutk.zxing.InactivityTimer;
import com.tutk.zxing.ViewfinderView;

import java.io.IOException;
import java.util.Vector;

@SuppressWarnings("deprecation")
public class qr_codeActivity extends Activity implements Callback {

	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private SurfaceHolder surfaceHolder;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;
	private SurfaceView surfaceView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.titlebar);
		TextView tv = (TextView) this.findViewById(R.id.bar_text);
		tv.setText(getText(R.string.txtAddCamera));

		super.onCreate(savedInstanceState);
		
//		NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
//		if(mNfcAdapter == null){
//			//  device doesn't have NFC
//			setupOrigLayout();
//		}else{
//			setupNFCLayout();
//		}
        setupOrigLayout();

		CameraManager.init(getApplication());
		inactivityTimer = new InactivityTimer(this);
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		
		hasSurface = false;
	}

	private void setupOrigLayout(){
		setContentView(R.layout.qr_code_activity);
		
		TextView tvNext= (TextView) findViewById(R.id.tvNext);
		tvNext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent = intent.setClass(qr_codeActivity.this, AddDeviceActivity.class);
				startActivityForResult(intent, AddDeviceActivity.REQUEST_CODE_GETUID_BY_SCAN_BARCODE);
				overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			}
		});
	}

	// <----------------------------------------------------------Following are zxing originally
	// source code--------------------------------------------------------->//
	@Override
	protected void onResume() {
		super.onResume();
		surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {// Scan again
			handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {

//		Rect frame = CameraManager.get().getFramingRect();		
//		surfaceView.setX(frame.left);
//		surfaceView.setY(frame.top);
//		surfaceHolder.setFixedSize(frame.width(), frame.height());

		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}

	public void handleDecode(Result obj, Bitmap barcode) { // After scan completed
		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();// Play beep and vibrate
		
		boolean duplicated = false;
		String mUID;
        String mAP = null;
		String mScan[] = obj.getText().split("/");
		mUID = mScan[0];
        if(mScan.length > 1) {
            mAP = mScan[1];
        }
		for (MyCamera camera : MultiViewActivity.CameraList) {

			if (mUID.equalsIgnoreCase(camera.getUID())) {
				duplicated = true;
				break;
			}
		}

		if (duplicated) { 
			Custom_Ok_Dialog dlg = new Custom_Ok_Dialog(this, getText(R.string.tips_add_camera_duplicated).toString(), getText(R.string.ok).toString());
			dlg.setCanceledOnTouchOutside(false);
			Window window = dlg.getWindow();
			window.setWindowAnimations(R.style.setting_dailog_animstyle);
			dlg.show();
			return;
		}

        if(mAP == null){
            Custom_Ok_Dialog dlg = new Custom_Ok_Dialog(this, getText(R.string.txt_qrcode_not_support).toString(), getText(R.string.ok).toString());
            dlg.setCanceledOnTouchOutside(false);
            Window window = dlg.getWindow();
            window.setWindowAnimations(R.style.setting_dailog_animstyle);
            dlg.show();
            return;
        }
		
		viewfinderView.drawResultBitmap(barcode);// When scan complete,show capture picture on
		// scanner		

		Intent intent = new Intent();
		intent = intent.setClass(qr_codeActivity.this, AddDeviceActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("SCAN_RESULT", obj.getText());
		// bundle.putString("tabhost", "scan_qrcode");
		intent.putExtras(bundle);
//		qr_codeActivity.this.setResult(RESULT_OK, intent); v1
//		qr_codeActivity.this.finish();
		
		startActivityForResult(intent, AddDeviceActivity.REQUEST_CODE_GETUID_BY_SCAN_BARCODE);
		overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

	@Override
	public void onBackPressed() {
		qr_codeActivity.this.finish();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (resultCode) {
		case RESULT_OK:

			Bundle extras = data.getExtras();
			Intent Intent = new Intent();
			Intent.putExtras(extras);
			setResult(RESULT_OK, Intent);
			finish();

			break;
		}
	}
	
}