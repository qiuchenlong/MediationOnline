package com.pzf.liaotian;

import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class WebViewActivity extends Activity{

	private WebView myWebView = null;
	private Button backButton;
	private RelativeLayout layout;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.protocol_webview);

        // 打开网页
        myWebView = (WebView) findViewById(R.id.protocol_confirm_webview);
        Intent intent = getIntent();
        String path = intent.getStringExtra("URL_PATH");
      
        myWebView.loadUrl(path);

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        
        myWebView.setWebViewClient(new WebViewClient() {  
            //点击网页中按钮时，让其还在原页面打开  
            public boolean shouldOverrideUrlLoading(WebView view, String url) {  
                view.loadUrl(url);  
                return true;  
            }  
            
            public void onPageFinished(WebView view, String url) {
                CookieManager cookieManager = CookieManager.getInstance();
                String CookieStr = cookieManager.getCookie(url);
                Log.e("sunzn", "Cookies = " + CookieStr);
                super.onPageFinished(view, url);
            }
           
        });
        
        
        
       
        

       backButton = (Button)findViewById(R.id.protocol_webview_back_button);
       backButton.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			finish();
		}
	});
       
       

    }

}
