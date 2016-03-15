package co.hmika.umichapi.thenewblue;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by dom on 3/13/16.
 */
public class NotificationDeleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("here", "herehere");

        SharedPreferences settings = context.getSharedPreferences(context.getString(R.string.preferenceFileKey), 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("keepServiceRunning", false);
        editor.commit();


        //context.getApplicationContext().stopService(((TheNewBlue)context.getApplicationContext()).backgroundService);
        //context.stopService(((TheNewBlue)context.getApplicationContext()).backgroundService);
        ((TheNewBlue)context.getApplicationContext()).stopNotificationUpdate();
    }
}