package com.baidu.push;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.frontia.api.FrontiaPushMessageReceiver;
import general.IOTC_GCM_IntentService;
import general.ThreadTPNS;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.tutk.P2PCam264.DELUX.MultiViewActivity;

/**
 * the receiver. Please write the callback that you need, onBind is necessary, to handle startWork's return value；
 * onMessage is used to receive the passthrough message； onSetTags、onDelTags、onListTags are about to tag's callback；
 * onNotificationClicked will call with user click the notification； onUnbind is interface stopWork's callback.
 * 
 * errorCode:
 *  0 - Success
 *  10001 - Network Problem
 *  30600 - Internal Server Error
 *  30601 - Method Not Allowed 
 *  30602 - Request Params Not Valid
 *  30603 - Authentication Failed 
 *  30604 - Quota Use Up Payment Required 
 *  30605 - Data Required Not Found 
 *  30606 - Request Time Expires Timeout 
 *  30607 - Channel Token Timeout 
 *  30608 - Bind Relation Not Found 
 *  30609 - Bind Number Too Many
 * 
 * If the error code mentioned above can't explain your problem, please use the response's requestId and errorCode to contact with us.
 */
public class MyPushMessageReceiver extends FrontiaPushMessageReceiver {
    /** TAG to Log */
    public static final String TAG = MyPushMessageReceiver.class
            .getSimpleName();
    
    @Override
    public void onBind(Context context, int errorCode, String appid,
            String userId, String channelId, String requestId) {
        String responseString = "onBind errorCode=" + errorCode + " appid="
                + appid + " userId=" + userId + " channelId=" + channelId
                + " requestId=" + requestId;
        
        IOTC_GCM_IntentService.user_id = userId; 
        IOTC_GCM_IntentService.channel_id= channelId;
        ThreadTPNS thread = new ThreadTPNS(context);
		thread.start();
		MultiViewActivity.check_mapping_list(context);
        Log.d(TAG, responseString);

        // Success binding, set the flag
        if (errorCode == 0) {
            Utils.setBind(context, true);
        }
        // Demo use to update UI
        updateContent(context, responseString);
    }

    @Override
    public void onMessage(Context context, String message,
            String customContentString) {
        String messageString = "passthrough message=\"" + message
                + "\" customContentString=" + customContentString;
        Log.d(TAG, messageString);
 
        if (!TextUtils.isEmpty(customContentString)) {
            JSONObject customJson = null;
            try {
                customJson = new JSONObject(customContentString);
                String myvalue = null;
                if (customJson.isNull("mykey")) {
                    myvalue = customJson.getString("mykey");
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // Demo use to update UI
        updateContent(context, messageString);
    }

    @Override
    public void onNotificationClicked(Context context, String title,
            String description, String customContentString) {
        String notifyString = "click title=\"" + title + "\" description=\""
                + description + "\" customContent=" + customContentString;
        Log.d(TAG, notifyString);
      
        if (!TextUtils.isEmpty(customContentString)) {
            JSONObject customJson = null;
            try {
                customJson = new JSONObject(customContentString);
                String myvalue = null;
                if (customJson.isNull("mykey")) {
                    myvalue = customJson.getString("mykey");
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // Demo use to update UI
        updateContent(context, notifyString);
    }

    @Override
    public void onSetTags(Context context, int errorCode,
            List<String> sucessTags, List<String> failTags, String requestId) {
        String responseString = "onSetTags errorCode=" + errorCode
                + " sucessTags=" + sucessTags + " failTags=" + failTags
                + " requestId=" + requestId;
        Log.d(TAG, responseString);

        // Demo use to update UI
        updateContent(context, responseString);
    }

    @Override
    public void onDelTags(Context context, int errorCode,
            List<String> sucessTags, List<String> failTags, String requestId) {
        String responseString = "onDelTags errorCode=" + errorCode
                + " sucessTags=" + sucessTags + " failTags=" + failTags
                + " requestId=" + requestId;
        Log.d(TAG, responseString);

        // Demo use to update UI
        updateContent(context, responseString);
    }

    @Override
    public void onListTags(Context context, int errorCode, List<String> tags,
            String requestId) {
        String responseString = "onListTags errorCode=" + errorCode + " tags="
                + tags;
        Log.d(TAG, responseString);

        // Demo use to update UI
        updateContent(context, responseString);
    }

    @Override
    public void onUnbind(Context context, int errorCode, String requestId) {
        String responseString = "onUnbind errorCode=" + errorCode
                + " requestId = " + requestId;
        Log.d(TAG, responseString);
     
        if (errorCode == 0) {
            Utils.setBind(context, false);
        }
        // Demo use to update UI
        updateContent(context, responseString);
    }

    private void updateContent(Context context, String content) {
        Log.d(TAG, "updateContent");
        String logText = "" + Utils.logStringCache;

        if (!logText.equals("")) {
            logText += "\n";
        }

        SimpleDateFormat sDateFormat = new SimpleDateFormat("HH-mm-ss");
        logText += sDateFormat.format(new Date()) + ": ";
        logText += content;

        Utils.logStringCache = logText;

//        Intent intent = new Intent();
//        intent.setClass(context.getApplicationContext(), PushDemoActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.getApplicationContext().startActivity(intent);
    }

}
