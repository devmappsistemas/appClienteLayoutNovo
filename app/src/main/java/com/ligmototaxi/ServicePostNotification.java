package com.ligmototaxi;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


public class ServicePostNotification extends IntentService {


    private static final String TAG = "APP_MAPP";
    public static  final long INTERVAL_TWO_MINUTES = 2*60*1000;
    private String idMensagem;
    private String actionNotification;
    private String notificationUrl;
    private String myPrefsName = "configAppCliente";


    public ServicePostNotification() {
        super("ServicePostNotification");
    }

    @Override
    protected void onHandleIntent(Intent intent) {


        Log.i(TAG,  "Metodo onHandleIntent()  ServicePostNotification ");

        idMensagem = intent.getStringExtra("idMensagem");
        notificationUrl  = intent.getStringExtra("notificationUrl");
        actionNotification  = intent.getStringExtra("actionNotification");


        Log.i (TAG,  "Metodo onHandleIntent()  ServicePostNotification paramts "+idMensagem+"--"+actionNotification+"--"+notificationUrl);

        String refreshedToken = getApplicationContext().getSharedPreferences(myPrefsName, MODE_PRIVATE).getString("registroId","0");
        HashMap<String,String> postParams = new HashMap<String, String>();
        postParams.put("idMensagem",idMensagem);
        postParams.put("tipoApp","A");
        postParams.put("registroId",refreshedToken);


        Log.i (TAG,  "Metodo onHandleIntent()  ServicePostNotification paramts "+idMensagem+"--"+actionNotification+"--"+refreshedToken);

        String recebeuLeuMensagem = "";

        try{

            if(actionNotification.equals("recebeuMsg")){
                postParams.put("recebeuMsg","recebeuMsg");
                recebeuLeuMensagem = "recebeuMsg=recebeuMsg";
            }else{
                postParams.put("leuMsg","leuMsg");
                recebeuLeuMensagem="leuMsg=leuMsg";
            }

            notificationUrl +="?idMensagem="+idMensagem+"&tipoApp=A&registroId="+refreshedToken+"&"+recebeuLeuMensagem;


            postNotification(notificationUrl,postParams);

        }catch ( NullPointerException exception){

        }catch ( Exception exception){

        }


    }


    private void postNotification(String requestURL, HashMap<String, String> postDataParams){


        if(isConexaoInternet(getApplicationContext())) {

            URL url;
            HttpURLConnection conn = null;

            try {

                url = new URL(requestURL);

                conn = (HttpURLConnection) url.openConnection();

                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setChunkedStreamingMode(0);


                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.i(TAG, "ServicePostNotification - postNotification() Ok - " + responseCode);
                } else {
                    Log.i(TAG, "ServicePostNotification - postNotification() Error - " + responseCode);
                }

            } catch (IOException e) {

                Log.i(TAG, "ServicePostNotification - postNotification() IOException - " + e.getMessage());

                Intent it = new Intent(getApplicationContext(),BroadcastPostService.class);
                it.putExtra("idMensagem",idMensagem);
                it.putExtra("notificationUrl",notificationUrl);
                it.putExtra("actionNotification",actionNotification);

                AlarmUtil.schedule(getApplicationContext(),it,INTERVAL_TWO_MINUTES);
                stopSelf();

            }finally {
                Log.i(TAG, "ServicePostNotification - postNotification() finally  ");

                if(conn != null){
                    conn.disconnect();
                }

            }

        }else{

            Intent it = new Intent(getApplicationContext(),BroadcastPostService.class);
            it.putExtra("idMensagem",idMensagem);
            it.putExtra("notificationUrl",notificationUrl);
            it.putExtra("actionNotification",actionNotification);

            AlarmUtil.schedule(getApplicationContext(),it,INTERVAL_TWO_MINUTES);
            stopSelf();

        }


    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    private  boolean isConexaoInternet(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean c = false;
        if (cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isAvailable()
                && cm.getActiveNetworkInfo().isConnected()) {

            return  true;
        }
        return false;
    }
}

