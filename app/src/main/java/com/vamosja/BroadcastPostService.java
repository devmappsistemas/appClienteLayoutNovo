package com.vamosja;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BroadcastPostService extends BroadcastReceiver {

    private static final String TAG = "APP_MAPP";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i (TAG,  "Metodo onReceive  BroadcastPostService ");

        String idMensagem = intent.getStringExtra("idMensagem");
        String notificationUrl  = intent.getStringExtra("notificationUrl");
        String actionNotification  = intent.getStringExtra("actionNotification");

        Intent it = new Intent(context,ServicePostNotification.class);
        it.putExtra("idMensagem",idMensagem);
        it.putExtra("notificationUrl",notificationUrl);
        it.putExtra("actionNotification",actionNotification);

        context.startService(it);


        Log.i (TAG,  "Metodo onReceive  BroadcastPostService paramts"+idMensagem+"--"+actionNotification+"--"+notificationUrl);

    }
}
