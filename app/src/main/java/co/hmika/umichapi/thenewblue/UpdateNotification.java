package co.hmika.umichapi.thenewblue;

import android.app.Notification;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

/**
 * Created by dom on 3/12/16.
 */
public class UpdateNotification extends AsyncTask<Void, Void, Void>{
    Context context;
    NotificationCompat.Builder mBuilder;

    UpdateNotification(Context context, NotificationCompat.Builder mBuilder){
        this.context = context;
        this.mBuilder = mBuilder;
    }

    @Override
    protected Void doInBackground(Void... params) {
        return null;
    }
}
