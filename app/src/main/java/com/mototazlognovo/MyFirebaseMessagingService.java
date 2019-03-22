package com.mototazlognovo;

import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by DEV on 29/08/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {


    private static final String TAG = "APP_MAPP";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // remoteMessage.getMessageType();

        Log.i(TAG, "MyFirebaseMessagingService  onMessageReceived()");

        //Log.d(TAG, "From:" + remoteMessage.getMessageType());
        //Log.d(TAG, "From:" + remoteMessage.getFrom());
        //Verifique se a mensagem contém uma carga útil de dados. - (DATA)
        if (remoteMessage.getData().size() > 0) {


            //pega dados payload na estrutura de dados Map<String,String>
            Map<String,String> jsonStr  = remoteMessage.getData();

            String tipoNotificacao = "";
            int id = 01;
            String contentTitle = jsonStr.get("titulo");

            Log.d(TAG, "notification_url: " + jsonStr.get("notification_url"));

            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (jsonStr.get("tipo") != null)
                tipoNotificacao = jsonStr.get("tipo");



            //String contentText = jsonStr.get("msg");
            /**
             * se main activity estiver ativa não
             */

            Intent intent = new Intent(this,MainActivity.class);

            intent.putExtra("idMensagem",jsonStr.get("idMensagem"));
            intent.putExtra("notificationUrl",jsonStr.get("notification_url"));
            intent.putExtra("actionNotification","leuMsg");
            intent.putExtra("msgNotificacao",jsonStr.get("msg"));
            intent.putExtra("tituloNotificacao",jsonStr.get("titulo"));

            /* Android 8 Oreo */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                new NotificationUtils(this).getAndroidChannelNotification(intent,
                        contentTitle, "",
                        id,NotificationUtils.ANDROID_CHANNEL_ID);

            }else{
                NotificationUtil.create(this,intent,contentTitle,"",id);
            }



            //-----------------------------------------
            Intent itNotificacao =  new Intent(getApplicationContext(), BroadcastPostService.class);

            itNotificacao.putExtra("idMensagem",jsonStr.get("idMensagem"));
            itNotificacao.putExtra("notificationUrl",jsonStr.get("notification_url"));
            itNotificacao.putExtra("actionNotification","recebeuMsg");
            getApplicationContext().sendBroadcast(itNotificacao);

            /*
            Log.d(TAG, "Message data payload: " + jsonStr.get("action"));

            if(jsonStr.get("action").equals("updateBalde")){

                //Intent it = new Intent(this,ServiceDownloadDb.class);
                //it.putExtra("url",jsonStr.get("url"));
                //it.putExtra("nome",jsonStr.get("nome"));
                //it.putExtra("tamanho",Integer.parseInt(jsonStr.get("tamanho")));
                //startService(it);

            }else if(jsonStr.get("action").equals("updatePublicidade")){

            }

            */


        }else {
            // se nao é notificação
            Log.d(TAG, "MyFirebaseMessagingService onMessageReceived() Message Body:" + remoteMessage.getNotification().getBody());

        }
    }
}
