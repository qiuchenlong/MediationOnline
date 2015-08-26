package com.pzf.liaotian;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.pzf.liaotian.app.PushApplication;
import com.pzf.liaotian.common.util.SharePreferenceUtil;

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
	private static SharePreferenceUtil mSpUtil;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_webview);
        
        mSpUtil = PushApplication.getInstance().getSpUtil();
        mSpUtil.setServerIP("ws://192.168.0.228:8484");
        
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
    public void startChat(String json){
    	Intent intent = new Intent(MainWebViewActivity.this,PublicChatActivity.class);
    	Map<String, Object> personMap = chatJSONString(json);
 
    	intent.putExtra("USER_NAME", (String) personMap.get("username"));
		intent.putExtra("USER_ID", (String) personMap.get("userid"));
		intent.putExtra("IS_PRIVATE_CHAT", 0);
		intent.putExtra("IS_ADMIN", 0);
		intent.putExtra("CHAT_ROOM_ID", "027");
		startActivity(intent);
    }
    
    @JavascriptInterface
    public void startConsult(String json){
    	Intent intent = new Intent(MainWebViewActivity.this,ConsultActivity.class);
    	Map<String, Object> personMap = chatJSONString(json);
 
    	intent.putExtra("USER_NAME", (String) personMap.get("username"));
		intent.putExtra("USER_ID", (String) personMap.get("userid"));
		intent.putExtra("IS_PRIVATE_CHAT", 0);
		intent.putExtra("IS_ADMIN", 0);
		intent.putExtra("CHAT_ROOM_ID", "027");
		startActivity(intent);
    }
    
    @JavascriptInterface
    public void toShare(){
    	Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, "有纠纷怎么办？去哪找调解机构？找哪家合适？人家什么时候上班？…别再纠结啦！下载海沧在线调解APP，在家就能调解，还能了解最新调解动态，在线咨询，让调解更简单。点击下载http://xxxxxx.com");
		sendIntent.setType("text/plain");
		startActivity(sendIntent);
    }
    
    private Map<String, Object> chatJSONString(String JSONString) {  
        Map<String, Object> resultMap = new HashMap<String, Object>();  
        try {  
            // 直接把JSON字符串转化为一个JSONObject对象  
            JSONObject chat = new JSONObject(JSONString);  

            resultMap.put("username", chat.getString("username"));         
            resultMap.put("userid", chat.getString("userid"));              
            resultMap.put("filetype", chat.getString("filetype")); 
            resultMap.put("voicetime", chat.getInt("voicetime")); 
            resultMap.put("roomid", chat.getInt("roomid")); 
            resultMap.put("isPrivateChat", chat.getBoolean("isPrivateChat")); 
            resultMap.put("isSystemMessage", chat.getBoolean("isSystemMessage")); 
            resultMap.put("isagreement", chat.getBoolean("isagreement")); 
            resultMap.put("isadmin", chat.getBoolean("isadmin"));
             
            resultMap.put("data", chat.getString("data"));
        } catch (JSONException e) {  
            e.printStackTrace();  
        }  
        return resultMap;  
    } 

}
