package com.zipmarvlog;

import android.content.SharedPreferences;
import android.util.Log;


import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;



/**
 * Created by DEV on 29/08/2017.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {



    private static final String TAG = "APP_MAPP";


    public void onTokenRefresh() {


        super.onTokenRefresh();
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();


        String myPrefsName = "configAppCliente";

        if(!getApplicationContext().getSharedPreferences(myPrefsName, MODE_PRIVATE).getBoolean("install",false)){


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


        Log.v(TAG, "Refreshed token: " + refreshedToken);




    }
}
