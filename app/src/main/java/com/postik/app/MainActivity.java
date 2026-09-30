package com.postik.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView webView;

    // Open the freshly updated POSTIK website and bypass the old WebView cache.
    private static final String URL = "https://nizzarr-aj.github.io/postik/?v=23";

    private WebView makeWebView() {
        WebView w = new WebView(this);
        w.setBackgroundColor(Color.rgb(7, 7, 17));
        w.setHorizontalScrollBarEnabled(false);
        w.setVerticalScrollBarEnabled(false);
        w.setOverScrollMode(View.OVER_SCROLL_NEVER);

        WebSettings s = w.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setSupportMultipleWindows(false);

        // Always request the newest web assets.
        s.setCacheMode(WebSettings.LOAD_NO_CACHE);

        s.setMediaPlaybackRequiresUserGesture(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setSupportZoom(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            w.setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_IMPORTANT, true);
        }

        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(w, true);

        w.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        w.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                CookieManager.getInstance().flush();

                // Keep the RTL page pinned horizontally at the left edge.
                view.evaluateJavascript(
                    "try{document.documentElement.scrollLeft=0;document.body.scrollLeft=0;}catch(e){}",
                    null
                );

                view.setVisibility(View.VISIBLE);
                view.invalidate();
            }

            @Override
            public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                recreateWebView();
                return true;
            }
        });

        w.setWebChromeClient(new WebChromeClient());
        return w;
    }

    private void recreateWebView() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }

        webView = makeWebView();
        setContentView(webView);
        webView.loadUrl(URL);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = makeWebView();
        setContentView(webView);
        webView.loadUrl(URL);
    }

    @Override
    protected void onPause() {
        if (webView != null) webView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
            webView.postDelayed(() -> {
                if (webView != null) {
                    webView.invalidate();
                    webView.evaluateJavascript(
                        "try{document.documentElement.scrollLeft=0;document.body.scrollLeft=0;}catch(e){}",
                        null
                    );
                }
            }, 120);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
