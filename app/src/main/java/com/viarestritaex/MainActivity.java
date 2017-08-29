package com.viarestritaex;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.webkit.WebView;
import android.webkit.WebViewClient;




public class MainActivity extends AppCompatActivity  implements LocationListener{


    private WebView browser;
    private LocationManager locationManager;
    private AlertDialog alertaDialago;
    private AlertDialog alertaErrorInternet;
    private static final String CATEGORIA = "APP_HELD_TRANSPORTES";
    private String url ="";
    private final String ERROR_MSG_INTERNET = "App HeldTransportes requer conexão com a Internet";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        Log.i (CATEGORIA,  "Metodo onCreate Inicializado");

        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        browser = (WebView) findViewById(R.id.webView);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.getSettings().setBuiltInZoomControls(true);
        browser.getSettings().setSupportZoom(true);
        browser.getSettings().setDisplayZoomControls(false);

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
    protected void onResume() {
        super.onResume();

        Log.i (CATEGORIA,  "Metodo onResume Inicializado");

        if(! isConexaoInternet ( getApplicationContext () )){

            Log.i (CATEGORIA,  "Falha na Conexao com Internet");
            dialogoErrorInternet(ERROR_MSG_INTERNET);
        }


    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    @Override
    protected  void onDestroy(){
        Log.i (CATEGORIA,  "Metodo onDestroy Inicializado");
        super.onDestroy ();
        //finish ();
    }


    private String getUrl(){

        String ulrCliente = UrlClientes.url;
        url = ulrCliente+"mobilePrincipal.php?andStudio=S&destino=MN&versaoApp="+ConfiguracaoCliente.versaoApp+"AS&la=&lo=";
        //UrlClientes = "http://mototaxionline.com/mobilePrincipal.php?destino=MN&versao="+ConfiguracaoCliente.versaoApp+"&la=&lo=";
        return url;
    }

    private String getUrl(double latitude, double longitude){

        String ulrCliente = UrlClientes.url;

        url = ulrCliente+"mobilePrincipal.php?andStudio=S&destino=MN&versaoApp="+ConfiguracaoCliente.versaoApp+"AS&la="+latitude+"&lo="+longitude;
        //UrlClientes = "http://mototaxionline.com/mobilePrincipal.php?destino=MN&versao="+ConfiguracaoCliente.versaoApp+"&la="+latitude+"&lo="+longitude;
        return url;
    }

    private void startWebView(){

        Log.i (CATEGORIA,  getUrl());
        browser.loadUrl(getUrl());
        browser.setWebViewClient(new MyBrowser());
    }

    private void startWebView(double latitude, double longitude){
        Log.i (CATEGORIA,  getUrl(latitude,longitude));
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


        //CloudClient cloudClient = new CloudClient(7,this);

        //Verifica de GPS esta Ativado ou Nao Ativado
        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!enabled)
        {
            Log.i (CATEGORIA,  "GPS Desativado");
            dialogoPermissaoGps();
        }else
        {
            try
            {
                Log.i (CATEGORIA,  "GPS Ativado");
                locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER,
                        0,
                        0, this);
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if(lastLocation==null){

                    startWebView();
                }

            }catch (SecurityException ex)
            {
                Log.i (CATEGORIA,  "Error na hora de solicitar coordenadas GPS");
            }
        }
    }

    private  boolean isConexaoInternet(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean c = false;
        if (cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isAvailable()
                && cm.getActiveNetworkInfo().isConnected()) {
            c = true;
        }
        return c;
    }

    private void dialogoPermissaoGps(){

        Log.i (CATEGORIA,  "Metodo dialogoPermissaoGps inicializado");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alerta");
        builder.setMessage("Este aplicativo precisa do GPS  ativado. Deseja habilitar?");

        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface arg0, int arg1) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult ( intent, 10);

            }
        });

        builder.setNegativeButton("Nao", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface arg0, int arg1) {


                startWebView();

            }

        });

        alertaDialago = builder.create ();
        alertaDialago.show ();
    }

    private void dialogoErrorInternet(String msg){
        Log.i (CATEGORIA,  "Metodo dialogoErrorInternet inicializado");

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Alerta Error");

        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener () {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish ();
            }
        } );

        alertaErrorInternet = alertDialogBuilder.create ();
        alertaErrorInternet.show ();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i (CATEGORIA,  "Metodo onActivityResult inicializado");

        finish();
        startActivity(getIntent());

    }


    @Override
    public void onLocationChanged(Location location) {

        Log.i (CATEGORIA,  "Metodo onLocationChanged inicializado");
        startWebView(location.getLatitude(),location.getLongitude());


        try
        {
            locationManager.removeUpdates(this);

        }catch (SecurityException ex){
            Log.i (CATEGORIA,  "Error ao remover LocationManager ");
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

        Log.i (CATEGORIA,  "Metodo onStatusChanged inicializado");

    }

    @Override
    public void onProviderEnabled(String s) {
        Log.i (CATEGORIA,  "Metodo onProviderEnabled inicializado");
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.i (CATEGORIA,  "Metodo onProviderDisabled inicializado");
    }


    private class MyBrowser extends WebViewClient {


        //Quando você clicar em qualquer link  na  webview.
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.i (CATEGORIA,  "Metodo shouldOverrideUrlLoading - MyBrowser inicializado");
            view.loadUrl(url);
            return true;
        }



        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {

            Log.i (CATEGORIA,  "Metodo onReceivedError - MyBrowser inicializado : errorCode"+errorCode);
            view.loadUrl("about:blank");

            if(isConexaoInternet ( getApplicationContext () )) {

                switch (errorCode){
                    case -2:
                        Log.i (CATEGORIA,  "Metodo onReceivedError - MyBrowser (Servidor ou o nome do proxy de pesquisa falhou) : errorCode"+errorCode);

                        dialogoErrorInternet("Servidor ou o nome do proxy de pesquisa falhou! Favor reniciar Aplicativo novamente");
                        break;
                    case -6:
                        Log.i (CATEGORIA,  "Metodo onReceivedError - MyBrowser (Falhou ao conectar com o servidor) : errorCode"+errorCode);

                        dialogoErrorInternet("Falhou ao conectar com o servidor! Favor reniciar Aplicativo novamente ");
                        break;
                    case -8:
                        Log.i (CATEGORIA,  "Metodo onReceivedError - MyBrowser (Conexão expirou) : errorCode"+errorCode);

                        dialogoErrorInternet("Conexão expirou ! Favor reniciar Aplicativo novamente");
                        break;
                }

            }else {
                dialogoErrorInternet(ERROR_MSG_INTERNET);
            }

        }

    }
}
