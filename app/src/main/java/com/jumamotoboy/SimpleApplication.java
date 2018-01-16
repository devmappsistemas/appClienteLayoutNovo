package com.jumamotoboy;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Created by DEV on 30/08/2017.
 */

public class SimpleApplication extends Application {
    private static final String TAG = "APP_MAPP";

    @Override
    public void onCreate() {
        super.onCreate();

        String myPrefsName = "configAppCliente";

        if(!getApplicationContext().getSharedPreferences(myPrefsName, MODE_PRIVATE).getBoolean("install",false)){

            String refreshedToken = FirebaseInstanceId.getInstance().getToken();

            SharedPreferences.Editor editor = getSharedPreferences(myPrefsName, MODE_PRIVATE).edit();
            if(refreshedToken!=null){

                editor.putString("registroId",refreshedToken);
                editor.putBoolean("install",true);
                editor.apply();
            }else{
                editor.putString("registroId","0");
                editor.apply();
            }
        }

        AppEventsLogger.activateApp(this);
        Log.i (TAG,  "SimpleApplication - Metodo onCreate() - Inicializado");
    }
}
