package com.vamosja;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmUtil {

    private static final String TAG = "APP_MAPP";


    public static void schedule(Context context, Intent intent, int  timeout){

        Log.v(TAG,"AlarmUtil -- schedule(Context context, Intent intent, int  timeout) ...");

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeout, alarmIntent);

        Log.v(TAG,"AlarmUtil -- schedule() Alarme agendado com sucesso ");

    }

    public static void schedule(Context context, Intent intent, long  timeout){

        Log.v(TAG,"AlarmUtil -- schedule(Context context, Intent intent, long  timeout) ...");

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeout, alarmIntent);

        Log.v(TAG,"AlarmUtil -- schedule() Alarme agendado com sucesso ");

    }
    public static void scheduleTimeStop(Context context, Intent intent, long  timeout){

        Log.v(TAG,"AlarmUtil -- scheduleStop(Context context, Intent intent, long  timeout) ...");

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, timeout, alarmIntent);

        Log.v(TAG,"AlarmUtil -- scheduleStop() Alarme agendado com sucesso ");

    }

    public static void scheduleRepeat(Context context, Intent intent, long timeout, long intervalMills){

        Log.v(TAG,"AlarmUtil -- scheduleRepeat(Context context, Intent intent, long timeout, long intervalMills) ...");

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP,timeout,intervalMills,alarmIntent);

        Log.v(TAG,"AlarmUtil -- schedule() Alarme agendado com sucesso com repeat.");

    }

    public static void cancel(Context context, Intent intent){

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(alarmIntent);

        Log.v(TAG,"AlarmUtil -- schedule() Alarme cancelado com sucesso ");
    }

}
