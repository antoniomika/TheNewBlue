package co.hmika.umichapi.thenewblue;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by dom on 3/13/16.
 */
public class NotificationBackgroundService extends Service {
    int notificationID = 310711;
    NotificationCompat.Builder notif;
    Timer timer;

    int currentNotifETA, currentNotifStopID, currentNotifRouteId;
    String routeName = "hi", stopName = "asdf";


    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            SharedPreferences settings = getSharedPreferences(((TheNewBlue)getApplicationContext()).getString(R.string.preferenceFileKey), 0);
            currentNotifETA = settings.getInt("currentNotifETA", 0);
            routeName = settings.getString("routeName", "");
            currentNotifStopID = settings.getInt("currentNotifStopID", 0);
            stopName = settings.getString("stopName", "");
            currentNotifRouteId = settings.getInt("currentNotifRouteId", 0);
        } catch(Exception e){
            e.printStackTrace();
        }





        /*NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.android_notify);

        mBuilder.setSmallIcon(R.drawable.android_notify);
        mBuilder.setContentTitle(currentNotifETA + " Minutes Until Arrival!");
        mBuilder.setContentText("Route: " + routeName + "\nStop: " + stopName);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notificationID, mBuilder.build());
        notif = mBuilder;*/

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(NotificationBackgroundService.this);
        notif = mBuilder;

        Log.e("array", "Created");


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {

            currentNotifETA = intent.getExtras().getInt("currentNotifETA");
            routeName = intent.getExtras().getString("routeName");
            currentNotifStopID = intent.getExtras().getInt("currentNotifStopID");
            stopName = intent.getExtras().getString("stopName");
            currentNotifRouteId = intent.getExtras().getInt("currentNotifRouteId");

            SharedPreferences settings = getSharedPreferences(((TheNewBlue)getApplicationContext()).getString(R.string.preferenceFileKey), 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("currentNotifETA", currentNotifETA);
            editor.putString("routeName", routeName);
            editor.putInt("currentNotifStopID", currentNotifStopID);
            editor.putString("stopName", stopName);
            editor.putInt("currentNotifRouteId", currentNotifRouteId);

            editor.commit();

        } catch(Exception e){
            e.printStackTrace();
        }



        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                try {
                    URL url = new URL("https://mbus.doublemap.com/map/v2/eta?stop=" + currentNotifStopID);


                    HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Accept-Charset", "UTF-8");

                    InputStream stops = urlConnection.getInputStream();

                    StringWriter writer = new StringWriter();
                    IOUtils.copy(stops, writer);
                    String tempString = writer.toString();

                    urlConnection.disconnect();

                    //JSONArray newArrayOfStops = new JSONArray(tempString);
                    JSONObject actualAPIJsonReturn = new JSONObject(tempString);

                    /* JSONObject newestStopVals = new JSONObject();
                    for(int i = 0; i < newArrayOfStops.length(); ++i){
                        if(newArrayOfStops.getJSONObject(i).getInt("id") == currentNotifStopID){
                            newestStopVals = newArrayOfStops.getJSONObject(i);
                            break;
                        }
                    }*/



                    //Now we get the newest ETA
                    JSONArray newestEtas = actualAPIJsonReturn.getJSONObject("etas").getJSONObject(Integer.toString(currentNotifStopID)).getJSONArray("etas");
                    Log.e("array", newestEtas.toString() + "cheese");
                    int newestETA = currentNotifETA;
                    for (int i = 0; i < newestEtas.length(); ++i) {
                        //Log.e("currentRouteInt", Integer.toString(currentNotifRouteId));
                        if (currentNotifRouteId == newestEtas.getJSONObject(i).getInt("route")) {
                            //Log.e("aasdf", "asdfasdf");
                            //ETA hasnt changed
                            if (newestEtas.getJSONObject(i).getInt("avg") != newestETA) {
                                newestETA = newestEtas.getJSONObject(i).getInt("avg");
                            }

                            break;

                        }
                    }
                    if (newestETA != currentNotifETA) {
                        //Log.e("asdf", "penis cheese");
                        currentNotifETA = newestETA;


                        notif.setSmallIcon(R.drawable.ic_launcher);

                        notif.setContentTitle(currentNotifETA + " Minutes Until Arrival!");
                        notif.setContentText("Route: " + routeName + "\nStop: " + stopName);

                        Intent notifDeleted = new Intent(NotificationBackgroundService.this, NotificationDeleteReceiver.class);
                        PendingIntent notifDeletedPending = PendingIntent.getBroadcast(NotificationBackgroundService.this, 0, notifDeleted, 0);

                        notif.setDeleteIntent(notifDeletedPending);

                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.notify(notificationID, notif.build());
                        notif = notif;
                    }



                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, 1000, 15000);
        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        /*NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationID);*/

        timer.cancel();

        Log.e("array:", "Destroyed");

        SharedPreferences settings = getSharedPreferences(((TheNewBlue)getApplicationContext()).getString(R.string.preferenceFileKey), 0);
        if(settings.getBoolean("keepServiceRunning", true)){
            Intent intentService = new Intent(this, NotificationBackgroundService.class);
            this.startService(intentService);
        }



        super.onDestroy();
    }
}