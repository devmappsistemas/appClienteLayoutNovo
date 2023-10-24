package com.vamosja;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;

public class Util {

    private static final String TAG = "APP_MAPP";
    public static final String USERDATA = "PREFS_PRIVATE";

    public static String getProperty(String key, Context context) {

        Properties properties = new Properties();
        try{

            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("config.properties");
            properties.load(inputStream);
            inputStream.close();

        }catch (IOException e){
            Log.e(TAG,"Util --  IOException " +e.getMessage());
        }


        return properties.getProperty(key);


    }

    public static int getRandomNumber(int min,int max) {
        return (new Random()).nextInt((max - min) + 1) + min;
    }

    public static String geraStringRandom(){
        char[] chars1 = "ABCDEF012GHIJKL345MNOPQR678STUVWXYZ9".toCharArray();
        StringBuilder sb1 = new StringBuilder();
        Random random1 = new Random();
        for (int i = 0; i < 6; i++)
        {
            char c1 = chars1[random1.nextInt(chars1.length)];
            sb1.append(c1);
        }
        String random_string = sb1.toString();

        return random_string;
    }
}
