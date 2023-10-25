package com.ligmototaxi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.location.LocationListener;
import android.os.Bundle;
import android.Manifest;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;



public class MainActivity extends AppCompatActivity implements LocationListener {


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
    static final int PERMISSAO_ACTIVITY_ALERTDIALOG = 156;
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000 * 60 * 1;
    private static final float LOCATION_DISTANCE =1; // Metros
    private Location location;
    private static final int MULTIPLE_PERMISSIONS = 11;
    private String[] permissionsQ = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private String[] permissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //getSupportActionBar().hide();

        Log.i (TAG,  "Metodo onCreate Inicializado");
        String registroId = "0";
        /* Android 8 Oreo */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            new NotificationUtils(this);

        }

        setContentView(R.layout.activity_main);
        String appMame = getResources().getString(R.string.app_name);
        this.errorMSgInternet ="App "+appMame+" requer conexão com a Internet";

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        browser = (WebView) findViewById(R.id.webView2);
        browser.setWebViewClient(new MyBrowser());

        browser.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 4.1.1; Galaxy Nexus Build/JRO03C) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19");

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
        registroId = getSharedPreferences("registroId","0",getApplicationContext());
        if(!registroId.equals("0")){
            browser.loadUrl(getUrl(registroId));
        }else{
            browser.loadUrl(getUrl());
        }

        progress = findViewById(R.id.progress);
        //progress.setVisibility(View.INVISIBLE);

        iconeCliente = (ImageView) findViewById(R.id.icone_cliente);
        //iconeCliente.setVisibility(View.INVISIBLE);

        //Caso tenha recebido notificacao push da alert aqui
        if(getIntent().getStringExtra("msgNotificacao") != null){
            Log.i (TAG,  "Metodo onCreate notificacao "+getIntent().getStringExtra("msgNotificacao"));
            dialogoNotificacaoPush(getIntent().getStringExtra("msgNotificacao"),
                    getIntent().getStringExtra("tituloNotificacao"));

            Intent it2 = new Intent(this, BroadcastPostService.class);
            it2.putExtra("idMensagem",getIntent().getStringExtra("idMensagem"));
            it2.putExtra("notificationUrl",getIntent().getStringExtra("notificationUrl"));
            it2.putExtra("actionNotification",getIntent().getStringExtra("actionNotification"));
            sendBroadcast(it2);
        }

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.v(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        String newToken = task.getResult();
                        Log.v(TAG, "token New  ==  "+newToken);

                        String registroId = getSharedPreferences("registroId","0",getApplicationContext());

                        if(registroId.equals("0")){
                            SharedPreferences.Editor editor = getSharedPreferences(myPrefsName, MODE_PRIVATE).edit();
                            if(newToken!=null){

                                editor.putString("registroId",newToken);
                                editor.putBoolean("install",true);
                                editor.apply();
                                browser.loadUrl(getUrl(newToken));

                            }

                        }

                    }
                });


    }

    private void getLastLocation() {

        if(location != null ){
            Log.i(TAG, "javascript:removePontoMapaNovo(1);pegarEnderecoPelaLaLoMobileNovo("+location.getLatitude()+","+location.getLongitude()+");");
            browser.loadUrl("javascript:removePontoMapaNovo(1);pegarEnderecoPelaLaLoMobileNovo("+location.getLatitude()+","+location.getLongitude()+");");
        }
    }

    private static String getSharedPreferences(String key,String valueDafault,Context context){

        SharedPreferences settings = context.getSharedPreferences("configAppCliente", MODE_PRIVATE);

        String value = settings.getString(key, valueDafault);

        return value;

    }
    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();

        String[] allPermissions = permissions;

        /* Android 10 ACCESS_BACKGROUND_LOCATION */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            allPermissions = permissionsQ;
            Log.i(TAG, "Android 10 " );

        }

        for (String p : allPermissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
                Log.i(TAG, "checkPermissions " + p);

            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @JavascriptInterface
    public void startSupportChat(String fone) {

        try {
            String headerReceiver = "";// Substitua por sua mensagem.
            String bodyMessageFormal = "";// Substitua por sua mensagem.
            String whatsappContain = headerReceiver + bodyMessageFormal;
            String trimToNumner = fone; //10 digit number
            Intent intent = new Intent ( Intent.ACTION_VIEW );
            intent.setData (Uri.parse("https://wa.me/55"+trimToNumner+"/?text="+""));
            startActivity (intent);
        } catch (Exception e) {

        }

    }

    @Override
    public void onBackPressed() {// botão voltar do celular

        super.onBackPressed();
        Log.d("BT", "Pressionou botão voltar no celular urlVoltar= " + urlVoltar);

        if (urlVoltar.equals("botaoHome")) {
            Log.d("BT", "Url é botaoHome " + urlVoltar);

            MainActivity.this.finish();
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
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

        } else if (urlVoltar.equals("home")) {
            numeroVezesHome++;
            Log.d("BT", "numeroVezesHome= " + numeroVezesHome);
            if (numeroVezesHome > 1) {
                Log.d("BT", "numeroVezesHome maior que 1 = " + numeroVezesHome + " fechar app");
                numeroVezesHome = 0;
                sair();

            } else {

                Toast.makeText(getApplicationContext(), "Pressione novamente para sair", Toast.LENGTH_LONG).show();
                // atualizar a tela
                //MainActivity.this.finish();
                //Intent intent = new Intent(MainActivity.this,MainActivity.class);
                //startActivity(intent);
            }
        } else if (urlVoltar.equals("PF")) {
            /**
             * estava em detalhes da viagem e volta para pedidos finalizados
             * enviar dados do android para html
             */

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

                browser.evaluateJavascript("javascript:voltarPF();", null);
            } else {
                browser.loadUrl("javascript:voltarPF();");
            }
        } else if (urlVoltar.equals("PG")) {
            /**
             * estava em detalhes da viagem e volta para pedidos finalizados
             * enviar dados do android para html
             */

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

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
        //browser.loadUrl(getUrl());
        MainActivity.this.finish();
        Intent intent = new Intent(MainActivity.this,MainActivity.class);
        startActivity(intent);
    }

    @JavascriptInterface
    public void carregarTelaChamarMototaxi() {
        Log.d(TAG, "vai carregar tela chamar mototaxi");
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

        // Verificar se a permissão de localização está concedida
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Registrar o LocationListener para receber atualizações de localização
                if(locationManager != null){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,60000 , 0, this);

                }
            } else {
                // Solicitar permissão de localização ao usuário
                //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(locationManager != null) {
            // Parar de receber atualizações de localização quando a atividade está em pausa
            locationManager.removeUpdates(this);
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
        super.onDestroy();
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

        //refreshedToken = getApplicationContext().getSharedPreferences(myPrefsName, MODE_PRIVATE).getString("registroId","0");

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

    private String  urlParametros(String token) {

        String retorno = "";

        SharedPreferences pref = getSharedPreferences(myPrefsName, this.MODE_PRIVATE);
        String id = pref.getString("idCliente","");
        //String empresa = pref.getString("empresa","");
        String nome = pref.getString("nome","");
        String email = pref.getString("email","");
        String senha = pref.getString("senha","");

        //refreshedToken = getApplicationContext().getSharedPreferences(myPrefsName, MODE_PRIVATE).getString("registroId","0");

        parameters =  new HashMap<String, String>();
        parameters.put("andStudio","S");
        parameters.put("destino","MLN");
        parameters.put("mobile","A");
        parameters.put("registreId",token);
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
        retorno = "andStudio=S&destino=MLN&mobile=A&registreId=" + token + "&login=" + email+ "&senha="+senha+ "&versaoApp=" + ConfiguracaoCliente.versaoApp + "AS&cpAndroid=A";;
        return retorno;
    }

    private String getUrl(){

        String ulrCliente = UrlClientes.url;

        String url = ulrCliente + "mobilePrincipalNv.php?" + urlParametros() + "&la=&lo=";

        if(location != null){
            url = ulrCliente + "mobilePrincipalNv.php?" + urlParametros() + "&la="+location.getLatitude()+"&lo="+location.getLongitude();
        }
        parameters.put("la","");
        parameters.put("lo","");

        Uri.Builder b = Uri.parse(ulrCliente).buildUpon();
        b.path("/mobilePrincipalNv.php");
        //b.path("/mobilePrincipal.php");

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            b.appendQueryParameter(entry.getKey(), entry.getValue());
        }

        String urlFull = b.build().toString();
        Log.i (TAG,  "Metodo getUrl() "+url);

        return  url;
    }

    private String getUrl(String token){

        String ulrCliente = UrlClientes.url;



        String url = ulrCliente + "mobilePrincipalNv.php?" + urlParametros(token) + "&la=&lo=";

        if(location != null){
            url = ulrCliente + "mobilePrincipalNv.php?" + urlParametros(token) + "&la="+location.getLatitude()+"&lo="+location.getLongitude();
        }

        parameters.put("la","");
        parameters.put("lo","");

        Uri.Builder b = Uri.parse(ulrCliente).buildUpon();
        b.path("/mobilePrincipalNv.php");
        //b.path("/mobilePrincipal.php");

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            b.appendQueryParameter(entry.getKey(), entry.getValue());
        }

        String urlFull = b.build().toString();
        Log.i (TAG,  "Metodo getUrl() "+url);

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
    private void startWebView(String token){

        Log.i (TAG,  getUrl(token));
        browser.loadUrl(getUrl(token));
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

        boolean validarPermissao = true;

        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {

                for (int i = 0, len = permissions.length; i < len; i++) {
                    String permission = permissions[i];

                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {

                        Log.i(TAG, "Permission  "+permission);
                        //validarPermissao = false;

                        Pattern pattern = Pattern.compile("android.permission.ACCESS_FINE_LOCATION", Pattern.CASE_INSENSITIVE);
                        if( pattern.matcher(permission).find()){
                            validarPermissao = false;
                        }

                    }else if(grantResults[i] == PackageManager.PERMISSION_GRANTED){



                    }else{
                        //Nunca pergunte novamente selecionado, ou política de dispositivo proíbe a aplicação de ter essa permissão.
                        Log.i(TAG, "Nunca pergunte ");
                    }
                }


            }
        }

        if (validarPermissao) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {

                browser.evaluateJavascript("javascript:hiddenViewGps();", null);
            } else {
                browser.loadUrl("javascript:hiddenViewGps();");
            }

        }



    }

    private void dialogoPermissaoApp(String mensage) {


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle(R.string.app_name);
        builder.setMessage(mensage);
        builder.setCancelable(false);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface arg0, int arg1) {

                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);


                startActivityForResult(intent, PERMISSAO_ACTIVITY_ALERTDIALOG);
            }
        });



        builder.create();
        builder.show();
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
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "Metodo onActivityResult inicializado");
        finish();
        startActivity(getIntent());

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        this.location = location;
        Log.i(TAG, "Localização autêntica: " + location.getLatitude() + ", " + location.getLongitude());

        getLastLocation();
    }


    //obter a localização atual do usuário


    private class MyBrowser extends WebViewClient {

        boolean timeout = true;

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            progress.setVisibility(View.VISIBLE);

            Log.i (TAG,  "Metodo onPageStarted - MyBrowser inicializado");
            Log.i (TAG,  "Metodo onPageStarted - "+url);

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
                    != PackageManager.PERMISSION_GRANTED)) {

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


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    dialogoPermissaoApp("As notificações estão desativadas.");
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
                    default:
                        dialogoErrorInternet("Desculpe houve algum erro ! Favor reniciar Aplicativo novamente");
                }


            }else {

                dialogoErrorInternet(errorMSgInternet);
            }


        }

    }


}