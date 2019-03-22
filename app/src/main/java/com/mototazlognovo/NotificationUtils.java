package com.mototazlognovo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;


public class NotificationUtils extends ContextWrapper {

    private static final String TAG = "App";
    private NotificationManager mManager;

    public static final String ANDROID_CHANNEL_ID = BuildConfig.APPLICATION_ID+".ANDROID";
    public static final String ANDROID_CHANNEL_NAME = "ANDROID CHANNEL";


    public NotificationUtils(Context base) {
        super(base);
        createChannels();
    }


    public void createChannels() {

        mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        /* Android 8 Oreo */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {



            /*
            Uri sms   = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.sms);
            Uri toquetelefone = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.toquetelefone);
            Uri fechou = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.fechou);
            Uri ganhouimm = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.ganhouimm);
            //Uri perdeuimm = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.perdeuimm);
            Uri recebeimm = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.recebeimm);
            Uri som = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.som);
            Uri sommsg = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.sommsg);

            AudioAttributes att = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();
*/


            NotificationChannel androidChannel = new NotificationChannel(ANDROID_CHANNEL_ID,
                    ANDROID_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            // Sets whether notifications posted to this channel should display notification lights
            androidChannel.enableLights(true);
            // Sets whether notification posted to this channel should vibrate.
            androidChannel.enableVibration(true);
            // Sets the notification light color for notifications posted to this channel
            androidChannel.setLightColor(Color.GREEN);
            // Sets whether notifications posted to this channel appear on the lockscreen or not
            androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            mManager.createNotificationChannel(androidChannel);


/*
            // create android channel
            NotificationChannel androidChannelCorridaMototaxi = new NotificationChannel(ANDROID_CHANNEL_CORRIDAMOTOTAXI,
                    ANDROID_CHANNEL_NAME_CORRIDAMOTOTAXI, NotificationManager.IMPORTANCE_HIGH);
            // Sets whether notifications posted to this channel should display notification lights
            androidChannelCorridaMototaxi.enableLights(true);
            // Sets whether notification posted to this channel should vibrate.
            androidChannelCorridaMototaxi.enableVibration(true);
            // Sets the notification light color for notifications posted to this channel
            androidChannelCorridaMototaxi.setLightColor(Color.GREEN);
            // Sets whether notifications posted to this channel appear on the lockscreen or not
            androidChannelCorridaMototaxi.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            if("immediatomotoboy".equals(nomeCliente)){
                androidChannelCorridaMototaxi.setSound(ganhouimm,att);
            }else {
                androidChannelCorridaMototaxi.setSound(sms,att);
            }
            mManager.createNotificationChannel(androidChannelCorridaMototaxi);

*/
        }


    }

    private static PendingIntent getPendingIntent(Context context, Intent intent, int id){

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(intent.getComponent());
        stackBuilder.addNextIntent(intent);
        PendingIntent p = stackBuilder.getPendingIntent(id,PendingIntent.FLAG_UPDATE_CURRENT);
        return  p;

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void getAndroidChannelNotification(Intent intent, String contentTitle,
                                              String contentText, int id, String channel) {

        PendingIntent p = getPendingIntent(getApplicationContext(),intent,id);
        Notification.Builder nb =  new Notification.Builder(getApplicationContext(), channel)
                .setContentIntent(p)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_notificacao)
                .setAutoCancel(true);

        mManager.notify(id, nb.build());

        /*return new Notification.Builder(getApplicationContext(), ANDROID_CHANNEL_ID)
                .setContentIntent(p)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_notificacao)
                .setAutoCancel(true); */

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void getAndroidChannelNotification(PendingIntent pendingIntent, String contentTitle,
                                              String contentText, long[] pattern, int id, String channel) {
        Notification.Builder nb  =new Notification.Builder(getApplicationContext(), channel)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setVibrate(pattern)
                .setSmallIcon(R.drawable.ic_notificacao)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        mManager.notify(id, nb.build());




    }




}
