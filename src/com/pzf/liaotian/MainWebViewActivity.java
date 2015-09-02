package com.pzf.liaotian;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.pzf.liaotian.app.PushApplication;
import com.pzf.liaotian.common.util.SharePreferenceUtil;
import com.pzf.liaotian.common.util.WebSocketConnectTool;
import com.zztj.chat.bean.EnterChatRoom;
import com.zztj.chat.bean.EnterChatRoom.Base_Info;
import com.zztj.chat.bean.EnterChatRoomServer;
import com.zztj.chat.bean.JsonMessageStruct;
import com.zztj.chat.bean.User;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;

public class MainWebViewActivity extends Activity{

	private WebView myWebView = null;
	private Button backButton;
	private static SharePreferenceUtil mSpUtil;
	public static WebSocketConnectTool mConnection = WebSocketConnectTool.getInstance();
	public MainWebViewActivity mContent;

	private Vector v;
	private int index = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_webview);
        
        mContent = this;
        v = new Vector();
        mSpUtil = PushApplication.getInstance().getSpUtil();

//        mSpUtil.setServerIP("ws://192.168.0.228:8484");
        mSpUtil.setServerIP("ws://172.17.5.228:7272");
//        mSpUtil.setServerIP("ws://weixin.bizcn.com:7272");
//        mSpUtil.setServerIP("ws://weixin.bizcn.com:7272");
        
        // 打开网页
        myWebView = (WebView) findViewById(R.id.main_webview);
        myWebView.addJavascriptInterface(MainWebViewActivity.this, "ChatRoom");
        
        String path = "http://www.baidu.com";
//        myWebView.loadUrl(path);// 百度链接
        
//        myWebView.loadUrl("file:///android_asset/demo.html");
//        myWebView.loadUrl("http://hcjd.dev.bizcn.com/Home/index.html");
        myWebView.loadUrl("http://hcjd.cdncache.com/Home/index.html");
//        myWebView.loadUrl(path);

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
       
         myWebView.setWebChromeClient(new WebChromeClient() {
        	 
         });
         
        myWebView.setWebViewClient(new WebViewClient() {  
            //点击网页中按钮时，让其还在原页面打开  
            public boolean shouldOverrideUrlLoading(WebView view, String url) {  
               
              //调用拨号程序
                if (url.startsWith("mailto:") || url.startsWith("geo:") ||url.startsWith("tel:")) {
                	if(hasDigit(url)) {
                		 Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                		 startActivity(intent);
                	} else {
                		Toast.makeText(MainWebViewActivity.this, "电话号码为空", Toast.LENGTH_SHORT).show();
    					return false;
    				}
                                      
                  } else {
                	  view.loadUrl(url);  
				}

                return true;  
            }  
            
            public boolean hasDigit(String content) {

            	boolean flag = false;

            	Pattern p = Pattern.compile(".*\\d+.*");

            	Matcher m = p.matcher(content);

            	if (m.matches())

            	flag = true;

            	return flag;

            	}
            
            public void onPageFinished(WebView view, String url) {
//                CookieManager cookieManager = CookieManager.getInstance();
//                String CookieStr = cookieManager.getCookie(url);
//                Log.e("sunzn", "Cookies = " + CookieStr);
//                super.onPageFinished(view, url);
            	
            	 String CurrentUrl = myWebView.getUrl();
             	Log.v("chat", CurrentUrl);
//            	oldUrl[index] = myWebView.getUrl().toString();
            	
            	
            }
           
        });       

//      startChat("");
      
        
    }
    
    @Override  
    protected void onStart() {  
        super.onStart();  
      index = 0;
    } 
    @JavascriptInterface
    public void startChat(String json){
    	//进入聊天室，向服务器提交信息。
    	//组织要提交的json信息
    	
    	 Gson gson = new Gson();
         java.lang.reflect.Type type = new TypeToken<JsonMessageStruct>(){}.getType();
         JsonMessageStruct jsonBean = gson.fromJson(json, type);
         
    	EnterChatRoom enterRoom = new EnterChatRoom();
        enterRoom.setBaseInfo(new Base_Info());
        enterRoom.init("enter", jsonBean.base_info.session_id, jsonBean.base_info.room_id);
//        enterRoom.init("enter", "asdasdasd", 11111);
        String jsonStr = gson.toJson(enterRoom);
        Log.v("=============", jsonStr);
        
        //向服务器发送信息
        try {
			mConnection.handleConnection(null,"enter",jsonStr,mContent);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
    
    @JavascriptInterface
    public void toShare(){
    	Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, "有纠纷怎么办？去哪找调解机构？找哪家合适？人家什么时候上班？…别再纠结啦！下载海沧在线调解APP，在家就能调解，还能了解最新调解动态，在线咨询，让调解更简单。点击下载http://xxxxxx.com");
		sendIntent.setType("text/plain");
		startActivity(sendIntent);
    }
    
    @JavascriptInterface
    public String getDeviceID(){
    	SharedPreferences settings = getSharedPreferences("global_id", 0);
        String id = settings.getString("device_id","");
        return id;
    }
    
    @Override  
    protected void onDestroy() {  
        super.onDestroy();  
        mSpUtil.setUserIDs("");
    } 
    
    private long exitTime = 0;
    public boolean onKeyDown(int keyCode, KeyEvent event) { 
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { 
        	//首页五个子页面的地址
        	String urls[] = {
        			"http://hcjd.cdncache.com/Home/Index/index.html",
        			"http://hcjd.cdncache.com/Home/index.html",
        			"http://hcjd.cdncache.com/Home/Art/deptlist.html",
        			"http://hcjd.cdncache.com/Home/Art/adjlist.html",
        			"http://hcjd.cdncache.com/Home/Art/index.html",
        			"http://hcjd.cdncache.com/Home/User/index.html"};
        	
        	if (!useList(urls, myWebView.getUrl())) {
        		myWebView.goBack();
			} else {
				if((System.currentTimeMillis()-exitTime) > 2000){  
		              Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();                                
		              exitTime = System.currentTimeMillis();   
		          } else {
		              finish();
		              System.exit(0);
		          }
			}
        	
        	
        	
            return false; 
        } 
        
        return false; 
    }
    
    public static boolean useList(String[] arr, String targetValue) {
        return Arrays.asList(arr).contains(targetValue);
    }

}
