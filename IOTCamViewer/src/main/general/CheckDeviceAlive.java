package general;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;

import com.tutk.IOTC.Camera;
import com.tutk.IOTC.IRegisterIOTCListener;
import com.tutk.P2PCam264.MyCamera;

/**
 * Created by James Huang on 2015/7/16.
 */
public class CheckDeviceAlive implements IRegisterIOTCListener {

    private Context mContext;
    private MyCamera mCamera = null;
    private Handler handler = new Handler();
    private CheckDevListener mListener;

    private String mDeviceName;
    private String mDeviceUID;
    private String mDevicePWD;

    public CheckDeviceAlive (Context context, CheckDevListener listener, String name, String uid, String pwd) {
        mContext = context;
        mListener = listener;
        mDeviceName = name;
        mDeviceUID = uid;
        mDevicePWD = pwd;
    }

    public void startCheck () {

        mCamera = new MyCamera(mDeviceName, mDeviceUID, "admin", mDevicePWD);
        Thread mConnectThread = new Thread(new Runnable() {
            @Override
            public void run () {
                mCamera.registerIOTCListener(CheckDeviceAlive.this);
                mCamera.connect(mDeviceUID);
                mCamera.start(MyCamera.DEFAULT_AV_CHANNEL, "admin", mDevicePWD);
            }
        });

        mConnectThread.start();
    }

    public void quit (boolean isIntent) {

        if (mCamera != null) {
            mCamera.unregisterIOTCListener(CheckDeviceAlive.this);
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
                if (mCamera != null) {
                    mCamera.disconnect();
                }

                if (mListener != null) {
                    mListener.getCheckingErr(CheckDevListener.RESULT_UNKNOWN);
                }
            } else if (resultCode != Camera.CONNECTION_STATE_CONNECTING && resultCode != Camera.CONNECTION_STATE_CONNECTED) {
                if (mCamera != null) {
                    mCamera.disconnect();
                }

                if (mListener != null) {
                    mListener.getCheckingErr(CheckDevListener.RESULT_FAILED);
                }
            }
        }
    }

    @Override
    public void receiveChannelInfo (Camera camera, int avChannel, int resultCode) {
        if (mCamera == camera) {
            if (resultCode == Camera.CONNECTION_STATE_CONNECTED) {
                if (mListener != null && mCamera != null) {
                    mListener.getConnected(mCamera);
                }
            } else if (resultCode == Camera.CONNECTION_STATE_WRONG_PASSWORD) {
                if (mCamera != null) {
                    mCamera.disconnect();
                }

                if (mListener != null) {
                    mListener.getCheckingErr(CheckDevListener.RESULT_WRONG_PWD);
                }
            } else if (resultCode == Camera.CONNECTION_STATE_TIMEOUT) {
                if (mCamera != null) {
                    mCamera.disconnect();
                }

                if (mListener != null) {
                    mListener.getCheckingErr(CheckDevListener.RESULT_FAILED);
                }
            }
        }
    }

    @Override
    public void receiveIOCtrlData (Camera camera, int avChannel, int avIOCtrlMsgType, byte[] data) {

    }
}
