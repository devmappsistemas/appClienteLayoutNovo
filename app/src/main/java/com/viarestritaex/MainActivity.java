package com.viarestritaex;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;




public class MainActivity extends AppCompatActivity  implements LocationListener{


    private WebView browser;
    private LocationManager locationManager;
    private static final String TAG = "APP_MAPP";
    private String url ="";
    private String errorMSgInternet;
    private View progress;
    private ImageView iconeCliente;
    private String refreshedToken = "0";
    private String myPrefsName = "configAppCliente";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        Log.i (TAG,  "Metodo onCreate Inicializado");

        setContentView(R.layout.activity_webview);
        String appMame = getResources().getString(R.string.app_name);
        this.errorMSgInternet ="App "+appMame+" requer conexão com a Internet";

/**
 * teset git
 */

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);



        browser = (WebView) findViewById(R.id.webView2);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.getSettings().setBuiltInZoomControls(true);
        browser.getSettings().setSupportZoom(true);
        browser.getSettings().setDisplayZoomControls(false);
        progress = findViewById(R.id.progress);
        progress.setVisibility(View.INVISIBLE);

        iconeCliente = (ImageView) findViewById(R.id.icone_cliente);
        iconeCliente.setVisibility(View.INVISIBLE);

        //Caso tenha recebido notificacao push da alert aqui
        if(getIntent().getStringExtra("msgNotificacao") != null){
            Log.i (TAG,  "Metodo onCreate notificacao "+getIntent().getStringExtra("msgNotificacao"));
            dialogoNotificacaoPush(getIntent().getStringExtra("msgNotificacao"),
                    getIntent().getStringExtra("tituloNotificacao"));

            Intent it2 = new Intent(this,BroadcastPostService.class);
            it2.putExtra("idMensagem",getIntent().getStringExtra("idMensagem"));
            it2.putExtra("notificationUrl",getIntent().getStringExtra("notificationUrl"));
            it2.putExtra("actionNotification",getIntent().getStringExtra("actionNotification"));
            sendBroadcast(it2);
        }




        /*
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.READ_PHONE_STATE},0);
        }else
        {
            // Tudo OK, podemos prosseguir.
            startApp();
        }
        */


        // Se não possui permissão
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},0);

        }
        else
        {
            // Tudo OK, podemos prosseguir.
            startApp();

        }





    }



    @Override
    protected void onStop(){
        super.onStop();
    }

    @Override
    protected  void onDestroy(){
        Log.i (TAG,  "Metodo onDestroy Inicializado");
        super.onDestroy ();
        //finish ();
    }


    private String getUrl(){

        String ulrCliente = UrlClientes.url;

        refreshedToken = getApplicationContext().getSharedPreferences(myPrefsName, MODE_PRIVATE).getString("registroId","0");

        url = ulrCliente+"mobilePrincipal.php?andStudio=S&destino=MN&mobile=A&registreId="+refreshedToken+"&versaoApp="+ConfiguracaoCliente.versaoApp+"AS&la=&lo=";
        //UrlClientes = "http://mototaxionline.com/mobilePrincipal.php?destino=MN&versao="+ConfiguracaoCliente.versaoApp+"&la=&lo=";
        return url;
    }

    private String getUrl(double latitude, double longitude){

        String ulrCliente = UrlClientes.url;

        refreshedToken = getApplicationContext().getSharedPreferences(myPrefsName, MODE_PRIVATE).getString("registroId","0");

        url = ulrCliente+"mobilePrincipal.php?andStudio=S&destino=MN&mobile=A&registreId="+refreshedToken+"&versaoApp="+ConfiguracaoCliente.versaoApp+"AS&la="+latitude+"&lo="+longitude;
        //UrlClientes = "http://mototaxionline.com/mobilePrincipal.php?destino=MN&versao="+ConfiguracaoCliente.versaoApp+"&la="+latitude+"&lo="+longitude;
        return url;
    }

    private void startWebView(){

        Log.i (TAG,  getUrl());
        browser.loadUrl(getUrl());
        browser.setWebViewClient(new MyBrowser());

    }

    private void startWebView(double latitude, double longitude){
        Log.i (TAG,  getUrl(latitude,longitude));
        browser.loadUrl(getUrl(latitude,longitude));
        browser.setWebViewClient(new MyBrowser());


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int result : grantResults) {

            if (result == PackageManager.PERMISSION_DENIED) {
                // Alguma permissão foi negada
                startWebView();

            }else if (result == PackageManager.PERMISSION_GRANTED){
                startApp();
            }else {
                //Nunca pergunte novamente selecionado, ou política de dispositivo proíbe a aplicação de ter essa permissão.
                startWebView();
            }
        }

    }

    private void startApp(){


        //Verifica de GPS esta Ativado ou Nao Ativado
        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!enabled)
        {
            Log.i (TAG,  "GPS Desativado");
            dialogoPermissaoGps();
        }else
        {
            progress.setVisibility(View.VISIBLE);
            iconeCliente.setVisibility(View.VISIBLE);
            try
            {
                Log.i (TAG,  "GPS Ativado");
                locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER,
                        0,
                        0, this);
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if(lastLocation==null){

                    startWebView();
                }

            }catch (SecurityException ex)
            {
                Log.i (TAG,  "Error na hora de solicitar coordenadas GPS");
            }
        }
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

    private void dialogoPermissaoGps(){

        Log.i (TAG,  "Metodo dialogoPermissaoGps inicializado");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle(R.string.app_name);
        builder.setMessage("Este aplicativo precisa do GPS  ativado. Deseja habilitar?");

        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface arg0, int arg1) {


                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult ( intent, 10);

            }
        });

        builder.setNegativeButton("Nao", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface arg0, int arg1) {

                progress.setVisibility(View.VISIBLE);
                iconeCliente.setVisibility(View.VISIBLE);
                startWebView();

            }

        });

        builder.create();
        builder.show();
    }

    private void dialogoErrorInternet(String msg){

        Log.i (TAG,  "Metodo dialogoErrorInternet inicializado");


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle(R.string.app_name);
        builder.setMessage(msg);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener () {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish ();
            }
        } );
        builder.create();
        builder.show();

    }

    private void dialogoNotificacaoPush(String msg, String titulo){

        Log.i (TAG,  "Metodo dialogoNotificacaoPush  inicializado");


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle(titulo);
        builder.setMessage(msg);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener () {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        } );
        builder.create();
        builder.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i (TAG,  "Metodo onActivityResult inicializado");

        finish();
        startActivity(getIntent());

    }


    @Override
    public void onLocationChanged(Location location) {

        Log.i (TAG,  "Metodo onLocationChanged inicializado");
        startWebView(location.getLatitude(),location.getLongitude());

        try
        {
            locationManager.removeUpdates(this);

        }catch (SecurityException ex){
            Log.i (TAG,  "Error ao remover LocationManager ");
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

        Log.i (TAG,  "Metodo onStatusChanged inicializado");

    }

    @Override
    public void onProviderEnabled(String s) {
        Log.i (TAG,  "Metodo onProviderEnabled inicializado");
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.i (TAG,  "Metodo onProviderDisabled inicializado");
    }



    private class MyBrowser extends WebViewClient {


        //Quando você clicar em qualquer link  na  webview.
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.i (TAG,  "Metodo shouldOverrideUrlLoading - MyBrowser inicializado");
            if(url== null){

            }
            view.loadUrl(url);
            return true;
        }


        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {

            progress.setVisibility(View.INVISIBLE);
            iconeCliente.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {

            Log.i (TAG,  "Metodo onReceivedError - MyBrowser inicializado : errorCode"+errorCode);
            view.loadUrl("about:blank");





            if(isConexaoInternet ( getApplicationContext () )) {
                view.setVisibility(View.INVISIBLE);


                switch (errorCode){

                    case -2:
                        Log.i (TAG,  "Metodo onReceivedError - MyBrowser (Servidor ou o nome do proxy de pesquisa falhou) : errorCode"+errorCode);

                        dialogoErrorInternet("Servidor ou o nome do proxy de pesquisa falhou! Favor reniciar Aplicativo novamente");
                        break;
                    case -6:
                        Log.i (TAG,  "Metodo onReceivedError - MyBrowser (Falhou ao conectar com o servidor) : errorCode"+errorCode);

                        dialogoErrorInternet("Falhou ao conectar com o servidor! Favor reniciar Aplicativo novamente ");
                        break;
                    case -8:
                        Log.i (TAG,  "Metodo onReceivedError - MyBrowser (Conexão expirou) : errorCode"+errorCode);

                        dialogoErrorInternet("Conexão expirou ! Favor reniciar Aplicativo novamente");
                        break;
                }


            }else {

                dialogoErrorInternet(errorMSgInternet);
            }


        }

    }


}