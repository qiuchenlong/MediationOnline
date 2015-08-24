package com.pzf.liaotian;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class MainWebViewActivity extends Activity{

	private WebView myWebView = null;
	private Button backButton;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_webview);

        // 打开网页
        myWebView = (WebView) findViewById(R.id.main_webview);
        myWebView.addJavascriptInterface(MainWebViewActivity.this, "ChatRoom");
        
        String path = "http://www.baidu.com";
//        myWebView.loadUrl(path);// 百度链接
        
        myWebView.loadUrl("file:///android_asset/demo.html");

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
 
        
         myWebView.setWebChromeClient(new WebChromeClient() {
        	 
         });
         
        myWebView.setWebViewClient(new WebViewClient() {  
            //点击网页中按钮时，让其还在原页面打开  
            public boolean shouldOverrideUrlLoading(WebView view, String url) {  
                view.loadUrl(url);  
                return true;  
            }  
            
            public void onPageFinished(WebView view, String url) {
//                CookieManager cookieManager = CookieManager.getInstance();
//                String CookieStr = cookieManager.getCookie(url);
//                Log.e("sunzn", "Cookies = " + CookieStr);
//                super.onPageFinished(view, url);
            }
           
        });
    }
    
    @JavascriptInterface
    public void startChat(){
    	Intent intent = new Intent(MainWebViewActivity.this,PublicChatActivity.class);
    	startActivity(intent);
//    	AlertDialog.Builder ab=new AlertDialog.Builder(MainWebViewActivity.this);
//		ab.setTitle("提示");
//		ab.setMessage("通过js 调用了 java 中的方法");
//		ab.setPositiveButton("确定", new DialogInterface.OnClickListener()
//		{
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				dialog.dismiss();
//			}
//		});
//		ab.create().show();
    }

}