package com.ligmototaxi;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;



/**
 * Created by DEV on 01/09/2017.
 */

public class NotificationUtil {


    private static PendingIntent getPendingIntent(Context context, Intent intent, int id) {

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(intent.getComponent());
        stackBuilder.addNextIntent(intent);
        PendingIntent p = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            p = stackBuilder.getPendingIntent(id, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            p = stackBuilder.getPendingIntent(id, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return p;

    }

    public static void create(Context context, Intent intent, String contentTitle,
                              String contentText, int id) {

        PendingIntent p = getPendingIntent(context, intent, id);

        NotificationCompat.Builder b = new NotificationCompat.Builder(context);
        b.setDefaults(Notification.DEFAULT_ALL);
        b.setSmallIcon(R.drawable.ic_notificacao);
        b.setContentTitle(contentTitle);
        b.setContentText(contentText);
        //b.setDeleteIntent();
        b.setContentIntent(p);
        b.setAutoCancel(true);

        NotificationManagerCompat nm = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        nm.notify(id, b.build());

    }

    public static void cancell(Context context, int id){

        NotificationManagerCompat nm = NotificationManagerCompat.from(context);
        nm.cancel(id);

    }

    public static void cancellAll(Context context){
        NotificationManagerCompat nm = NotificationManagerCompat.from(context);
        nm.cancelAll();
    }

}

