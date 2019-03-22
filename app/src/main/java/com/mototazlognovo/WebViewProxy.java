package com.mototazlognovo;

import android.webkit.WebView;

/**
 * Created by DEV on 08/02/2018.
 */

public class WebViewProxy {

    private String url;
    private WebView webView;
    private String javaScript;


    public void onGetWebViewUrl(String urlSite, WebView view) {

        this.url = urlSite;
        this.webView = view;

        webView.post(new Runnable() {
            public void run() {

                webView.post(new Runnable() {

                    @Override
                    public void run() {

                        webView.loadUrl(url);


                    }
                });
            }
        });
    }

    public void onGetWebViewJavasScript(String funcJavaScript, WebView view) {

        this.javaScript = funcJavaScript;
        this.webView = view;


        webView.post(new Runnable() {

            @Override
            public void run() {

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {

                    webView.evaluateJavascript(javaScript,null);
                }
                else
                {
                    webView.loadUrl(javaScript);
                }


            }
        });

    }
}
