package general;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GCMReceiver extends BroadcastReceiver
{


    @Override
    public final void onReceive(Context context, Intent intent) 
    {
        IOTC_GCM_IntentService.runIntentInService(context, intent);
        setResult(Activity.RESULT_OK, null, null);
    }


}
	