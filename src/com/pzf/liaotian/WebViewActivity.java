package com.pzf.liaotian;

import com.pzf.liaotian.app.PushApplication;
import com.pzf.liaotian.common.util.SharePreferenceUtil;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
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
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class WebViewActivity extends Activity{

	private WebView myWebView = null;
	private Button backButton;
	private RelativeLayout layout;
	public static SharePreferenceUtil mSpUtil;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.protocol_webview);
        mSpUtil = PushApplication.getInstance().getSpUtil();
        // 打开网页
        myWebView = (WebView) findViewById(R.id.protocol_confirm_webview);
        Intent intent = getIntent();
        String path = intent.getStringExtra("URL_PATH");
      
        myWebView.loadUrl(path);
        

//        WebSettings webSettings = myWebView.getSettings();
//        webSettings.setJavaScriptEnabled(true);
        
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
        
        
        synCookies(this,path);
       
        

       backButton = (Button)findViewById(R.id.protocol_webview_back_button);
       backButton.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			finish();
		}
	});
       
       

    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) { 
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { 
    
        	myWebView.goBack();
        }
        return false;
    }
    
    /** 
     * 同步一下cookie 
     */  
    public static void synCookies(Context context, String url) {  
        CookieSyncManager.createInstance(context);  
        CookieManager cookieManager = CookieManager.getInstance();  
        cookieManager.setAcceptCookie(true);  
        cookieManager.removeSessionCookie();//移除  
        cookieManager.setCookie(url,mSpUtil.getCookie());//cookies是在HttpClient中获得的cookie  
        CookieSyncManager.getInstance().sync();  
    } 
}
