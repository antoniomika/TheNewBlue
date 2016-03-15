package co.hmika.umichapi.thenewblue;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;

public class TheNewBlue extends Application {
    Intent backgroundService;
    //boolean started;

    @Override
    public void onCreate() {
        super.onCreate();
        backgroundService = new Intent(getBaseContext(), NotificationBackgroundService.class);
        //started = false;
    }

    public void startNotificationUpdate(String routeName, int currentNotifStopID, int currentNotifETA, String stopName, int currentNotifRouteId) {
        stopService(backgroundService);

        backgroundService.putExtra("routeName", routeName);
        backgroundService.putExtra("currentNotifStopID", currentNotifStopID);
        backgroundService.putExtra("currentNotifETA", currentNotifETA);
        backgroundService.putExtra("stopName", stopName);
        backgroundService.putExtra("currentNotifRouteId", currentNotifRouteId);

        //started = true;

        startService(backgroundService);

        SharedPreferences settings = getSharedPreferences(((TheNewBlue)getApplicationContext()).getString(R.string.preferenceFileKey), 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("keepServiceRunning", true);
        editor.commit();
    }

    public void stopNotificationUpdate(){
        stopService(backgroundService);
        //started = false;
    }
}