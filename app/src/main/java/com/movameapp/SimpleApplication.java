package com.movameapp;

import android.app.Application;
import android.content.SharedPreferences;

import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Created by DEV on 30/08/2017.
 */

public class SimpleApplication extends Application {

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
    }
}
