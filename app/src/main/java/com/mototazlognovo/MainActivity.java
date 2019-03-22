package com.mototazlognovo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.HashMap;
import java.util.Map;

//import com.facebook.appevents.AppEventsConstants;
//import com.facebook.appevents.AppEventsLogger;


public class MainActivity extends AppCompatActivity {


    private WebView browser;
    private LocationManager locationManager;
    private static final String TAG = "APP_MAPP";
    private String url ="";
    private String errorMSgInternet;
    private View progress;
    private ImageView iconeCliente;
    private String refreshedToken = "0";
    private String idCliente = "";
    private String myPrefsName = "configAppCliente";
    //private AppEventsLogger logger;
    private Criteria criteria;
    private String provider;
    private double latitude;
    private double longitude;
    private GoogleLocation googleLocation;
    private Map<String, String> parameters = null;
    private boolean firstStart = true;
    private AlertDialog alertDialog = null;
    private String urlVoltar = "";
    private int numeroVezesHome = 0;

    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000 * 60 * 1;
    private static final float LOCATION_DISTANCE =1; // Metros
    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;

        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(TAG, "onLocationChanged: " + location);
            Log.e(TAG, "onLocationChanged: " + location.getProvider());

            latitude = location.getLatitude();
            longitude = location.getLongitude();

            if (latitude != 0 && longitude != 0) {

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {

                    browser.evaluateJavascript("javascript:removePontoMapaNovo(1);pegarEnderecoPelaLaLoMobileNovo("+latitude+","+longitude+");",null);
                } else {
                    browser.loadUrl("javascript:removePontoMapaNovo(1);pegarEnderecoPelaLaLoMobileNovo("+latitude+","+longitude+");");
                }
            }



            mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }
    private MainActivity.LocationListener[] mLocationListeners = null;

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");

        if (mLocationManager == null) {

            mLocationListeners = new MainActivity.LocationListener[] {
                    new MainActivity.LocationListener(LocationManager.GPS_PROVIDER),
                    new MainActivity.LocationListener(LocationManager.NETWORK_PROVIDER)
            };
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            try {
                Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                mLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        mLocationListeners[1]);
            } catch (java.lang.SecurityException ex) {
                Log.i(TAG, "não solicitar a atualização de localização, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "provedor de rede não existe, " + ex.getMessage());
            }
            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        mLocationListeners[0]);
            } catch (java.lang.SecurityException ex) {
                Log.i(TAG, "não solicitar a atualização de localização, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "provedor de GPS não existe " + ex.getMessage());
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        Log.i (TAG,  "Metodo onCreate Inicializado");

        //logger = AppEventsLogger.newLogger(this);

        /* Android 8 Oreo */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            new NotificationUtils(this);

        }

        setContentView(R.layout.activity_webview);
        String appMame = getResources().getString(R.string.app_name);
        this.errorMSgInternet ="App "+appMame+" requer conexão com a Internet";



        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);



        browser = (WebView) findViewById(R.id.webView2);
        browser.setWebViewClient(new MyBrowser());
        browser.loadUrl(getUrl());
        browser.getSettings().setJavaScriptEnabled(true);
        browser.getSettings().setDomStorageEnabled(true);
        browser.setVerticalScrollBarEnabled(false);

        browser.getSettings().setBuiltInZoomControls(true);
        browser.getSettings().setSupportZoom(true);
        browser.getSettings().setDisplayZoomControls(false);
        browser.addJavascriptInterface(this, "chaveMain");
        browser.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                android.util.Log.d(TAG, consoleMessage.message());
                return true;
            }
        });

        progress = findViewById(R.id.progress);
        //progress.setVisibility(View.INVISIBLE);

        iconeCliente = (ImageView) findViewById(R.id.icone_cliente);
        //iconeCliente.setVisibility(View.INVISIBLE);

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


        boolean permissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (permissionGranted) {
            updateLocation(getApplicationContext(), this);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }





    }

    @Override
    public void onBackPressed() {// botão voltar do celular

        Log.d("BT", "Pressionou botão voltar no celular urlVoltar= "+urlVoltar);

        if(urlVoltar.equals("botaoHome")){
            Log.d("BT", "Url é botaoHome "+urlVoltar);

            MainActivity.this.finish();
            Intent intent = new Intent(MainActivity.this,MainActivity.class);
            startActivity(intent);

            /**
             * enviar dados do android para html
             */
            /*
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {

                browser.evaluateJavascript("javascript:botaoHome();", null);
            } else {
                browser.loadUrl("javascript:botaoHome();");
            }
            */

        }else if(urlVoltar.equals("home")){
            numeroVezesHome++;
            Log.d("BT", "numeroVezesHome= "+numeroVezesHome);
            if(numeroVezesHome > 1){
                Log.d("BT", "numeroVezesHome maior que 1 = "+numeroVezesHome+" fechar app");
                numeroVezesHome = 0;
               sair();

            }else {

                Toast.makeText(getApplicationContext(),"Precione novamente para sair",Toast.LENGTH_LONG).show();
                // atualizar a tela
                //MainActivity.this.finish();
                //Intent intent = new Intent(MainActivity.this,MainActivity.class);
                //startActivity(intent);
            }
        }else if(urlVoltar.equals("PF")){
            /**
             * estava em detalhes da viagem e volta para pedidos finalizados
             * enviar dados do android para html
             */

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {

                browser.evaluateJavascript("javascript:voltarPF();", null);
            } else {
                browser.loadUrl("javascript:voltarPF();");
            }
        }else if(urlVoltar.equals("PG")){
            /**
             * estava em detalhes da viagem e volta para pedidos finalizados
             * enviar dados do android para html
             */

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {

                browser.evaluateJavascript("javascript:voltarPG();", null);
            } else {
                browser.loadUrl("javascript:voltarPG();");
            }

        }

    }

    @JavascriptInterface
    public void urlVoltar(String urlAnterior) {

        urlVoltar = urlAnterior;

        Log.d("BT", "Url urlVoltar "+urlVoltar);



    }

    @JavascriptInterface
    public void redirecionarIndex() {
            browser.loadUrl(getUrl());
    }

    @JavascriptInterface
    public void carregarTelaChamarMototaxi() {



        Log.d(TAG, "vai carregar tela chamar mototaxi");


       // Log.d(TAG, " la = "+la+" lo="+lo);

/**

       // pegarEnderecoPelaLaLoMobileNovo("+la+","+lo+");",null);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            Log.d(TAG, " la1 = "+la+" lo="+lo);
           // browser.evaluateJavascript("removePontoMapaNovo(1);pegarEnderecoPelaLaLoMobileNovo("+location.getLatitude()+","+location.getLongitude()+");",null);
            browser.evaluateJavascript("removePontoMapaNovo(1);pegarEnderecoPelaLaLoMobileNovo("+la+","+lo+");",new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String s) {
                    Log.d(TAG, s); //s is Java null
                }
            });
        }else{
            Log.d("CC", " la2 = "+la+" lo="+lo);
            browser.loadUrl("javascript:removePontoMapaNovo(1);pegarEnderecoPelaLaLoMobileNovo("+la+","+lo+");");
        }
        Log.d("CC", " 2 - la = "+la+" lo="+lo);
*/

    }

    @JavascriptInterface
    public void sair() {

        Log.d(TAG, " apertou botão sair");

        Toast.makeText(getApplicationContext(),"Até breve...",Toast.LENGTH_LONG).show();

        finish();
    }

    @JavascriptInterface
    public void recebeIdSolicitanteCadastro(String idClienteTela,String nome, String email, String senha) {

        Log.d(TAG, " idSolicitante = "+idClienteTela);

        Toast.makeText(getApplicationContext(),"Sucesso",Toast.LENGTH_LONG).show();

        SharedPreferences.Editor editor = getSharedPreferences(myPrefsName, MODE_PRIVATE).edit();

        editor.putString("idCliente",idClienteTela);
        editor.putString("nomeCliente",nome);
        editor.putString("emailCliente",email);
        editor.putString("senhaCliente",senha);
        editor.apply();

        startWebView();
        //idCliente = getApplicationContext().getSharedPreferences(myPrefsName, MODE_PRIVATE).getString("idCliente","0");

        //Toast.makeText(getApplicationContext(),"idRecuperado shared ="+idCliente,Toast.LENGTH_LONG).show();
    }

    @JavascriptInterface
    public void recebeIdSolicitanteLogin(String idClienteTela, String email, String senha) {

        Log.d(TAG, " idSolicitante = "+idClienteTela);

        Toast.makeText(getApplicationContext(),"Sucesso",Toast.LENGTH_LONG).show();

        SharedPreferences.Editor editor = getSharedPreferences(myPrefsName, MODE_PRIVATE).edit();

        editor.putString("idCliente",idClienteTela);
        editor.putString("nomeCliente",email);
        editor.putString("emailCliente",email);
        editor.putString("senhaCliente",senha);
        editor.apply();


        //idCliente = getApplicationContext().getSharedPreferences(myPrefsName, MODE_PRIVATE).getString("idCliente","0");

        //Toast.makeText(getApplicationContext(),"idRecuperado shared ="+idCliente,Toast.LENGTH_LONG).show();
    }

    @JavascriptInterface
    public void abrirCompartilhamento(String titulo, String mensagem, String auxiliar) {

        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, titulo);
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, mensagem);
        startActivity(Intent.createChooser(shareIntent, titulo));

    }

    @JavascriptInterface
    public void createSharePreferencesUser(String nome,String email,String senha,String idSolicitante) {


        Log.i(TAG, " nome = "+nome + "email "+email+" senha "+" "+senha+"  idSolicitante"+idSolicitante);

        SharedPreferences.Editor editor = getSharedPreferences(myPrefsName, MODE_PRIVATE).edit();
        editor.putString("idCliente",idSolicitante);
        //editor.putString("empresa",empresa);
        editor.putString("email",email);
        editor.putString("senha",senha);
        editor.putString("nome",nome);
        editor.apply();
        /*
        String msg = "Id :"+idSolicitante+" nome :"+nome+" email :"+email +" senha :  "+senha;
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
        */
        Log.i(TAG, " nome = "+nome + "email "+email+" senha "+" "+senha+"  idSolicitante"+idSolicitante);
    }
    @JavascriptInterface
    public void permissionLocation() {


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},0);

        }else{

            //Verifica de GPS esta Ativado ou Nao Ativado
            boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (!enabled)
            {
                Log.i (TAG,  "GPS Desativado");
                dialogoPermissaoGps(false);
            }
        }

    }

    @JavascriptInterface
    public void loginUserSession() {


        /*
        SharedPreferences pref = getSharedPreferences(myPrefsName, this.MODE_PRIVATE);
        String id = pref.getString("idCliente","");
        String nome = pref.getString("nome","");
        String email = pref.getString("email","");
        String senha = pref.getString("senha","");



        refreshedToken = pref.getString("registroId","0");

        parameters =  new HashMap<String, String>();
        parameters.put("andStudio","S");
        parameters.put("destino","MLN");
        parameters.put("mobile","A");
        parameters.put("registreId",refreshedToken);
        parameters.put("versaoApp",ConfiguracaoCliente.versaoApp+"AS");
        parameters.put("cpAndroid","A");

        if(id != "" ){

            parameters.put("idCliente",id);
            // parameters.put("empresa",empresa);
            parameters.put("nome",nome);
            parameters.put("email",email);
            parameters.put("senha",senha);



        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (enabled)
            {

                parameters.put("la",String.valueOf(latitude));
                parameters.put("lo",String.valueOf(longitude));

                getUrl(latitude,longitude);
            }

        }else{

            parameters.put("la","");
            parameters.put("lo","");


        }


        Uri.Builder b = Uri.parse("").buildUpon();

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            b.appendQueryParameter(entry.getKey(), entry.getValue());
        }

        String urlFull = b.build().toString();


        Log.i (TAG,  "Metodo loginUserSession()  "+urlFull);

       //new WebViewProxy().onGetWebViewJavasScript("javascript:redirectUserSession('"+nome+"','"+email+"','"+senha+"','"+id+"','"+urlFull+"');",browser);



    */

    }
    @Override
    protected void onResume() {
        super.onResume();
        //logger.logEvent(AppEventsConstants.EVENT_NAME_ACTIVATED_APP);
        if(googleLocation != null){
            googleLocation.startLocationUpdates();
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        if(googleLocation != null){
            googleLocation.stopLocationUpdates();
        }

        if(alertDialog !=null){
            alertDialog.dismiss();
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

        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "não conseguir remover os lististas de locais, ignore", ex);
                }
            }
        }
        //finish ();
    }

    private String  urlParametros(){

        String retorno = "";



        SharedPreferences pref = getSharedPreferences(myPrefsName, this.MODE_PRIVATE);
        String id = pref.getString("idCliente","");
        //String empresa = pref.getString("empresa","");
        String nome = pref.getString("nome","");
        String email = pref.getString("email","");
        String senha = pref.getString("senha","");

        refreshedToken = getApplicationContext().getSharedPreferences(myPrefsName, MODE_PRIVATE).getString("registroId","0");

        parameters =  new HashMap<String, String>();
        parameters.put("andStudio","S");
        parameters.put("destino","MLN");
        parameters.put("mobile","A");
        parameters.put("registreId",refreshedToken);
        parameters.put("versaoApp",ConfiguracaoCliente.versaoApp+"AS");
        parameters.put("cpAndroid","A");

        if(id != "" ){

            parameters.put("idCliente",id);
           // parameters.put("empresa",empresa);
            parameters.put("nome",nome);
            parameters.put("email",email);
            parameters.put("senha",senha);



        }

        //retorno = "andStudio=S&destino=MLN&mobile=A&registreId=" + refreshedToken + "&versaoApp=" + ConfiguracaoCliente.versaoApp + "AS&cpAndroid=A";
        retorno = "andStudio=S&destino=MLN&mobile=A&registreId=" + refreshedToken + "&login=" + email+ "&senha="+senha+ "&versaoApp=" + ConfiguracaoCliente.versaoApp + "AS&cpAndroid=A";;
        return retorno;
    }

    private String getUrl(){

        String ulrCliente = UrlClientes.url;

        String url = ulrCliente + "mobilePrincipalNv.php?" + urlParametros() + "&la=&lo=";

        parameters.put("la","");
        parameters.put("lo","");


        Uri.Builder b = Uri.parse(ulrCliente).buildUpon();
        b.path("/mobilePrincipalNv.php");
        //b.path("/mobilePrincipal.php");

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            b.appendQueryParameter(entry.getKey(), entry.getValue());
        }

        String urlFull = b.build().toString();
        //Log.i (TAG,  "Metodo getUrl() "+url);

        return  url;
    }

    private String getUrl(double latitude, double longitude){

        String ulrCliente = UrlClientes.url;

        String url = ulrCliente + "mobilePrincipalNv.php?" + urlParametros() + "&la=" + latitude + "&lo=" + longitude + "";

        parameters.put("la",String.valueOf(latitude));
        parameters.put("lo",String.valueOf(longitude));


        Uri.Builder b = Uri.parse(ulrCliente).buildUpon();
        b.path("/mobilePrincipalNv.php");
        //b.path("/mobilePrincipal.php");

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            b.appendQueryParameter(entry.getKey(), entry.getValue());
        }

        String urlFull = b.build().toString();
        //Log.i (TAG,  "Metodo getUrl(latitude,longitude) "+urlFull);


        return  url;
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

        switch (requestCode) {
            case 200: {
                for (int result : grantResults) {

                    if (result == PackageManager.PERMISSION_DENIED) {
                        Log.i(TAG, "PackageManager.PERMISSION_DENIED");
                        // Alguma permissão foi negada
                       // browser.loadUrl(getUrl());
                    } else if (result == PackageManager.PERMISSION_GRANTED) {
                        Log.i(TAG, "PackageManager.PERMISSION_GRANTED");

                        updateLocation(getApplicationContext(), this);
                    } else {
                        //Nunca pergunte novamente selecionado, ou política de dispositivo proíbe a aplicação de ter essa permissão.
                        Log.i(TAG, "Nunca pergunte ");
                    }
                }
            }
        }

    }

    private void startApp(){


        //Verifica de GPS esta Ativado ou Nao Ativado
        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!enabled)
        {
            Log.i (TAG,  "GPS Desativado");
            dialogoPermissaoGps(true);
        }else
        {


            updateLocation(getApplicationContext(),this);
            //startWebView();

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

    private void dialogoPermissaoGps(final boolean loadUrl){

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

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        if(loadUrl){
                            progress.setVisibility(View.VISIBLE);
                            iconeCliente.setVisibility(View.VISIBLE);
                            startWebView();
                        }

                    }
                });

            }

        });

        builder.create();
        builder.show();

        alertDialog = builder.create();


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
        alertDialog = builder.create();
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
        alertDialog = builder.create();
        builder.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i (TAG,  "Metodo onActivityResult inicializado");

        finish();
        startActivity(getIntent());

    }


    //obter a localização atual do usuário
    private void updateLocation(Context context, Activity activity) {


        googleLocation = new GoogleLocation(context, activity, new LocationUpdateListener() {

            @Override
            public void updateLocation(Location location) {

                if(location != null){
                    Log.i (TAG,  "updateLocation LA"+location.getLatitude()+" LO"+location.getLongitude());

                    latitude = location.getLatitude();
                    longitude = location.getLongitude();



                    if(latitude != 0 && longitude != 0 ){

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {

                            browser.evaluateJavascript("javascript:removePontoMapaNovo(1);pegarEnderecoPelaLaLoMobileNovo("+latitude+","+longitude+");",null);
                        }
                        else
                        {
                            browser.loadUrl("javascript:removePontoMapaNovo(1);pegarEnderecoPelaLaLoMobileNovo("+latitude+","+longitude+");");
                        }
                    }
                }
            }

            @Override
            public void lastLocation(Location location) {

                if(location != null){
                    Log.i (TAG,  "lastLocation LA"+location.getLatitude()+" LO"+location.getLongitude());

                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    //startWebView(location.getLatitude(),location.getLongitude());

                }
            }

            @Override
            public void errorLocation(int statusCode) {

                switch (statusCode) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "As configurações de localização não estão satisfeitas. ");
                        initializeLocationManager();
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        String errorMessage = "As configurações de localização são inadequadas.";
                        Log.e(TAG, errorMessage);
                        initializeLocationManager();
                        break;
                }
            }
        });

    }



    private class MyBrowser extends WebViewClient {

        boolean timeout = true;

        //Quando você clicar em qualquer link  na  webview.
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.i (TAG,  "Metodo shouldOverrideUrlLoading - MyBrowser inicializado");
            Log.i (TAG,  "Metodo shouldOverrideUrlLoading - "+url);

            view.loadUrl(url);
            return true;
        }


        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            progress.setVisibility(View.VISIBLE);

            Log.i (TAG,  "Metodo onPageStarted - MyBrowser inicializado");
            Log.i (TAG,  "Metodo onPageStarted - "+url);

            /**
            final WebView v = view;
            Runnable run = new Runnable() {
                public void run() {
                    Log.i (TAG,  "Runnable - onPageStarted()");
                    if(timeout) {
                        dialogoErrorInternet("Desculpe houve algum erro na rede, e não foi possível abrir app. Favor tentar novamente. ");
                        v.loadUrl("about:blank");
                    }
                }
            };
            /* Codigo para timeout d 30 segundos caso ocorra error na conexão com a rede.*/
            /**
            Handler myHandler = new Handler(Looper.myLooper());
            myHandler.postDelayed(run, 30000);
             */

        }

        @Override
        public void onPageFinished(WebView view, String url) {


            super.onPageFinished(view, url);
            progress.setVisibility(View.INVISIBLE);
            iconeCliente.setVisibility(View.INVISIBLE);

            timeout = false;

            if(latitude != 0 && longitude != 0 ){

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {

                    browser.evaluateJavascript("javascript:removePontoMapaNovo(1);pegarEnderecoPelaLaLoMobileNovo("+latitude+","+longitude+");",null);
                }
                else
                {
                    browser.loadUrl("javascript:removePontoMapaNovo(1);pegarEnderecoPelaLaLoMobileNovo("+latitude+","+longitude+");");
                }
            }



            boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) ||
                    !enabled) {

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {

                    browser.evaluateJavascript("javascript:showViewGps();",null);
                }
                else
                {
                    browser.loadUrl("javascript:showViewGps()");
                }

            }else{
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {

                    browser.evaluateJavascript("javascript:showViwEndereco();",null);
                }
                else
                {
                    browser.loadUrl("javascript:showViwEndereco()");
                }
            }


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

                        dialogoErrorInternet("Desculpe houve algum erro em sua rede, favor refazer");
                        break;
                    case -6:
                        Log.i (TAG,  "Metodo onReceivedError - MyBrowser (Falhou ao conectar com o servidor) : errorCode"+errorCode);

                        dialogoErrorInternet("Falhou ao conectar! Favor reniciar Aplicativo novamente ");
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