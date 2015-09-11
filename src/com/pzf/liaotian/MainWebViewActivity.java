package com.pzf.liaotian;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.bither.util.NativeUtil;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.pzf.liaotian.album.AlbumHelper;
import com.pzf.liaotian.app.PushApplication;
import com.pzf.liaotian.bean.Message;
import com.pzf.liaotian.bean.MessageItem;
import com.pzf.liaotian.bean.RecentItem;
import com.pzf.liaotian.bean.album.ImageBucket;
import com.pzf.liaotian.bean.album.ImageTool;
import com.pzf.liaotian.common.util.AudioRecorder2Mp3Util;
import com.pzf.liaotian.common.util.SendMsgAsyncTask;
import com.pzf.liaotian.common.util.SharePreferenceUtil;
import com.pzf.liaotian.common.util.TimeUtil;
import com.pzf.liaotian.common.util.WebSocketConnectTool;
import com.pzf.liaotian.config.ConstantKeys;
import com.zztj.chat.bean.AudioStruct;
import com.zztj.chat.bean.EnterChatRoom;
import com.zztj.chat.bean.EnterChatRoom.Base_Info;
import com.zztj.chat.bean.EnterChatRoomServer;
import com.zztj.chat.bean.JsonMessageStruct;
import com.zztj.chat.bean.User;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
@SuppressLint("NewApi")
public class MainWebViewActivity extends Activity{

	public static WebView myWebView = null;
	private Button backButton;
	private static SharePreferenceUtil mSpUtil;
	public static WebSocketConnectTool mConnection = WebSocketConnectTool.getInstance();
	public MainWebViewActivity mContent;

	private Vector v;
	private int index = 0;
	private static final int CAMERA_WITH_FRONT = 10;
	private static final int CAMERA_WITH_BACK = 11;
	private static final int POLL_INTERVAL = 300;
	
	private ValueCallback<Uri> mUploadMessage;    
	private final static int FILECHOOSER_RESULTCODE=1;
	ProgressBar progressBar; 
	private String mTakePhotoFilePath;
	AudioRecorder2Mp3Util util = null;
	//录音层
	private View mChatUIWindow;
	
	private Handler mHandler = new Handler();
	private ImageView volume;
	// 录制的时间
	private TextView mTvVoiceRecorderTime;
    private Boolean recodePermission;
    // 录制时间过短
    private LinearLayout mLlVoiceShort;
    private boolean isShosrt = false;
    private LinearLayout mLlVoiceRcding;
    private String mRecordTime;
    private int mRcdStartTime = 0;// 录制的开始时间
    private int mRcdVoiceDelayTime = 1000;
    private int mRcdVoiceStartDelayTime = 300;
    private boolean isCancelVoice;// 不显示语音
    private LinearLayout mLlVoiceLoading;// 加载录制loading
    private ImageView mIvDelete;// 语音弹出框的差号按钮
    private LinearLayout mLLDelete;
    private ImageView mIvBigDeleteIcon;
    private VoiceRcdTimeTask mVoiceRcdTimeTask;
    private ScheduledExecutorService mExecutor;// 录制计时器
    private int mVioceTime;
    private long mStartRecorderTime;
    private long mEndRecorderTime;
    private long mAudioStopTime;
    private long mAudioStartTime;
    
    private static long lastClickTime;
    
    private AlbumHelper albumHelper = null;// 相册管理类
    private static List<ImageBucket> albumList = null;// 相册数据list
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_webview);
        mSpUtil = PushApplication.getInstance().getSpUtil();

        //配置录音
        initRecorderView();
        recodePermission = mSpUtil.getRecordPermission();
        if (recodePermission == false) {
        	PackageManager pm = getPackageManager(); 
        	boolean permission = (PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.RECORD_AUDIO", "packageName")); 
        	if (!permission) { 
        		recodePermission = true;
        	}
		}
        
        String fullPath = this.getExternalFilesDir(null).toString() + "/voice";
        runCommand("chmod 777 " + fullPath);
        
        initAlbumData();
        
        mContent = this;
        v = new Vector();
        
        mSpUtil.setServerIP("ws://192.168.0.43:8484");
//        mSpUtil.setServerIP("ws://218.5.80.211:7272");
//        mSpUtil.setServerIP("ws://172.17.5.228:7274");
//        mSpUtil.setServerIP("ws://hcjd.cdncache.com:7272");
//        mSpUtil.setServerIP("ws://weixin.bizcn.com:7272");
        
        // 打开网页
        myWebView = (WebView) findViewById(R.id.main_webview);
        myWebView.addJavascriptInterface(MainWebViewActivity.this, "ChatRoom");
//        myWebView.addJavascriptInterface(MainWebViewActivity.this, "AskRoom");
        String path = "http://www.baidu.com";
//        myWebView.loadUrl(path);// 百度链接
        
//        myWebView.loadUrl("file:///android_asset/demo.html");
//        myWebView.loadUrl("http://hcjd.dev.bizcn.com/Home/index.html");
//        Intent intent = getIntent();
//        myWebView.loadUrl("http://hcjd.cdncache.com/Home/Ask/test.html");
        myWebView.loadUrl("http://hcjd.cdncache.com/Home/index.html");
//        myWebView.loadUrl(path);

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        
//        myWebView.setOnLongClickListener(new OnLongClickListener() {
//			
//			public boolean onLongClick(View arg0) {
//				// TODO Auto-generated method stub
//				return true;
//			}
//		});
        
        
        myWebView.setWebViewClient(new myWebClient());
        myWebView.setWebChromeClient(new WebChromeClient()  
        {  
        	
            public void onReceivedTitle(WebView view, String title) {  
                super.onReceivedTitle(view, title);  
                Log.d("ANDROID_LAB", "TITLE=" + title);  
            } 
            
        	//用户注册时候上传照片
            // For Android 3.0-
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {  
                mUploadMessage = uploadMsg;  
                Log.v("3.0+", "uploadMsg="+uploadMsg);
                takePicturesWithFront();
               }

            // For Android 3.0+
               public void openFileChooser( ValueCallback uploadMsg, String accept ) {
               mUploadMessage = uploadMsg;
               takePicturesWithFront();
               Log.v("3.0+2", "uploadMsg="+uploadMsg);
               Log.v("3.0+2", "acceptType="+accept);
               }

            //For Android 4.1
               public void openFileChooser(ValueCallback<Uri> uploadMsg, String accept, String capture){
                   mUploadMessage = uploadMsg;  
                  
                   Log.v("4.1", "uploadMsg="+uploadMsg);
                   Log.v("4.1", "acceptType="+accept);
                   Log.v("4.1", "capture="+capture);
                   takePicturesWithFront();
               }

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
                CookieManager cookieManager = CookieManager.getInstance();
                String CookieStr = cookieManager.getCookie(url);
                Log.e("sunzn", "Cookies = " + CookieStr);
                mSpUtil.setCookie(CookieStr);
                super.onPageFinished(view, url);
            	
            	 String CurrentUrl = myWebView.getUrl();
             	Log.v("chat", CurrentUrl);            	
            	
            }
           
        });       

//      startChat("");
        
    }
    
    
    @Override
	public ComponentName getComponentName() {
		// TODO Auto-generated method stub
		return super.getComponentName();
	}


	/**
     * 录音功能
     * 
     * @param data
     */
    @JavascriptInterface
    public String audioStart() {
    	
        isShosrt = false;

    	if (isFastClick()) {
    		if (util!=null && util.isRecording) {
    			util.stopRecording();
				util.close();
				util = null;
			}
            isShosrt = true;
    		Toast.makeText(MainWebViewActivity.this, "发送语音间隔时间太短",
                     Toast.LENGTH_SHORT).show();
    		return "{ status : 1,info : \"间隔时间太短\"}";
		}
    	
    	 if (!Environment.getExternalStorageDirectory().exists()) {
             Toast.makeText(MainWebViewActivity.this, "No SDCard",
                     Toast.LENGTH_LONG).show();
             return "{ status : 1,info : \"没有SD卡\"}";
         }
    	     	
    	 String fileName = "/" + mSpUtil.getUserId() +System.currentTimeMillis();
	     String fullPath = this.getExternalFilesDir(null).toString() + "/voice";
	     File f = new File(fullPath);
	     if (!f.exists()) {
	      f.mkdirs();
	     } 
	     
	     if (util != null) {
			util.close();
			util = null;
		}
		util = new AudioRecorder2Mp3Util(null,
						fullPath+fileName+".raw",
						fullPath+fileName+".mp3");
	     
	     //判断是否有录音权限
	     if (recodePermission == false) {
	        util.startRecording(); 
	        util.stopRecording();
	        util.cleanFile(AudioRecorder2Mp3Util.RAW);
	        util.cleanFile(AudioRecorder2Mp3Util.MP3);
		    recodePermission = true;
		    mSpUtil.setRecordPermission(true);
	     } else {
	    	 	 
	    	 stopRecord();
	    	 mStartRecorderTime = System.currentTimeMillis();
	    	                 
                  mHandler.postDelayed(new Runnable() {
                      public void run() {
                    	  mLlVoiceLoading.setVisibility(View.GONE);
                          mLlVoiceRcding.setVisibility(View.VISIBLE);
                          mLlVoiceShort.setVisibility(View.GONE);
                          mChatUIWindow.setVisibility(View.VISIBLE); 
                          mLLDelete.setVisibility(View.GONE);
                      }
                  }, 0);
                  new Thread()
                  {
                      public void run()
                      {
                    	  Log.v("==11======", "startRecord");
                    	  startRecord();
                      }
                  }.start();          
                
		}
	     return "{ status : 1,info : \"录音器启动成功\"}";
    }
    
    @JavascriptInterface
    public String audioStop() {
       
    	Log.v("=======", "audioStop");
      mHandler.postDelayed(new Runnable() {
      public void run() {
     	 mLlVoiceRcding.setVisibility(View.GONE); 
     	 isShosrt = true;
//          mLlVoiceLoading.setVisibility(View.GONE);
          mLlVoiceRcding.setVisibility(View.GONE);
//          mLlVoiceShort.setVisibility(View.VISIBLE);
          mChatUIWindow.setVisibility(View.GONE);
      	}
      }, 0);
    	
      	mAudioStopTime = System.currentTimeMillis();
      	
      	if (!isShosrt) {
      		try {
            	stopRecordAndConvertFile();
            } catch (IllegalStateException e) {
                Toast.makeText(MainWebViewActivity.this, "麦克风不可用", 0).show();
                isCancelVoice = true;
                return "{ status : 0,info : \"麦克风不可用\"}";
            }
            mEndRecorderTime = System.currentTimeMillis();
            int mVoiceTime = (int) ((mEndRecorderTime - mStartRecorderTime) / 1000);
            if (mVoiceTime < 1) {
            
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        mLlVoiceShort.setVisibility(View.GONE);
                       
                    }
                }, 500);
           	 
            }
            
            String json = sendJsonToServer();
            if (json == null) {
            	Toast.makeText(MainWebViewActivity.this, "录音失败，请重新录一次", 0).show();
            	return "{ status : 0,info : \"录音失败\"}";
			}
            Log.v("=============", json);
            return json;
		} else {
			return "{ status : 0,info : \"录音间隔太短\"}";
		}
        
    }
    
    private String sendJsonToServer() {
    	
    	InputStream inputStream = null;
		   
		try {
			inputStream = new FileInputStream(new File(util.getFilePath(util.MP3)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}  
		
     ByteArrayOutputStream swapStream = new ByteArrayOutputStream(); 
     byte[] buff = new byte[100]; //buff用于存放循环读取的临时数据 
     int rc = 0; 
     
     if (inputStream == null) {
		return null;
	}
     try {
			while ((rc = inputStream.read(buff, 0, 100)) > 0) { 
			   swapStream.write(buff, 0, rc); 
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
     
     byte[] in_b = swapStream.toByteArray(); //in_b为转换之后的结果 
     String encodeString = Base64.encodeToString(in_b, Base64.NO_WRAP);
     AudioStruct audioStruct = new AudioStruct();
     String time = mVioceTime + "秒";
     if (mVioceTime < 1) {
    	 audioStruct.status = 0;
	} else {
		 audioStruct.status = 1;
	}
    
     audioStruct.seconds = time;
     audioStruct.base64Content = encodeString;
     
     Gson gson = new Gson();
     String jsonStr = gson.toJson(audioStruct);
     
     return jsonStr;
    }
    
    /**
     * 开始录音
     */
    private void startRecord() {

    	 String fileName = "/" + mSpUtil.getUserId() +System.currentTimeMillis();
	     String fullPath = this.getExternalFilesDir(null).toString() + "/voice";
	     File f = new File(fullPath);
	     if (!f.exists()) {
	      f.mkdirs();
	     } 
 
			util = new AudioRecorder2Mp3Util(null,
						fullPath+fileName+".raw",
						fullPath+fileName+".mp3");
			
            util.startRecording();
            
			util.cleanFile(AudioRecorder2Mp3Util.RAW);
            
            mHandler.postDelayed(mPollTask, POLL_INTERVAL);
            volume.setVisibility(View.VISIBLE);
            mVoiceRcdTimeTask = new VoiceRcdTimeTask(mRcdStartTime);
            if (mExecutor == null) {
                mExecutor = Executors.newSingleThreadScheduledExecutor();
                mExecutor.scheduleAtFixedRate(mVoiceRcdTimeTask,
                        mRcdVoiceStartDelayTime, mRcdVoiceDelayTime,
                        TimeUnit.MILLISECONDS);
            }
    }
    
    private void stopRecord() {
    	util.stopRecording();

    	 mHandler.removeCallbacks(mSleepTask);
         mHandler.removeCallbacks(mPollTask);
 
         mHandler.postDelayed(new Runnable() {
             public void run() {
             	 volume.setImageResource(R.drawable.amp1);
             }
         }, 0);
        
         if (mExecutor != null && !mExecutor.isShutdown()) {
             mExecutor.shutdown();
             mExecutor = null;
         }
    }
    
    /**
     * 结束录音
     */
    private void stopRecordAndConvertFile() throws IllegalStateException {
    	
    	if (util != null) {
    		util.stopRecordingAndConvertFile();
////		Toast.makeText(this, "ok", 0).show();
		util.cleanFile(AudioRecorder2Mp3Util.RAW);
		}
    	
//		// 如果要关闭可以
//		util.close();
//		util = null;
		
        mHandler.removeCallbacks(mSleepTask);
        mHandler.removeCallbacks(mPollTask);
//
        mHandler.postDelayed(new Runnable() {
            public void run() {
            	 volume.setImageResource(R.drawable.amp1);
            }
        }, 0);
       
        if (mExecutor != null && !mExecutor.isShutdown()) {
            mExecutor.shutdown();
            mExecutor = null;
        }

    }
    
    private Runnable mSleepTask = new Runnable() {
        public void run() {
        	stopRecordAndConvertFile();
        }
    };
    

    
    private Runnable mPollTask = new Runnable() {
        public void run() {
        	double amp = (double)(Math.random() * 11) ;
//            Log.e("fff", "音量:" + amp);
            updateDisplay(amp);
            mHandler.postDelayed(mPollTask, POLL_INTERVAL);
        }
    };
    
    /**
     * 初始化语音布局
     */
    private void initRecorderView() {

        // include包含的布局语音模块
        mIvDelete = (ImageView) this.findViewById(R.id.img1);
        mLLDelete = (LinearLayout) this.findViewById(R.id.del_re);
        mIvBigDeleteIcon = (ImageView) this.findViewById(R.id.sc_img1);
        mChatUIWindow = this.findViewById(R.id.window_chat);
        mChatUIWindow.setVisibility(View.GONE);
        mChatUIWindow.bringToFront();
        mLlVoiceRcding = (LinearLayout) this
                .findViewById(R.id.voice_rcd_hint_rcding);
        mLlVoiceLoading = (LinearLayout) this
                .findViewById(R.id.voice_rcd_hint_loading);
        mLlVoiceShort = (LinearLayout) this
                .findViewById(R.id.voice_rcd_hint_tooshort);
        volume = (ImageView) this.findViewById(R.id.volume);
        mTvVoiceRecorderTime = (TextView) this
                .findViewById(R.id.tv_voice_rcd_time);
    }
    
    /**
     * 变换语音量的图片
     * 
     * @param signalEMA
     */
    private void updateDisplay(double signalEMA) {

        switch ((int) signalEMA) {
            case 0:
            case 1:
                volume.setImageResource(R.drawable.amp1);
                break;
            case 2:
            case 3:
                volume.setImageResource(R.drawable.amp2);

                break;
            case 4:
            case 5:
                volume.setImageResource(R.drawable.amp3);
                break;
            case 6:
            case 7:
                volume.setImageResource(R.drawable.amp4);
                break;
            case 8:
            case 9:
                volume.setImageResource(R.drawable.amp5);
                break;
            case 10:
            case 11:
                volume.setImageResource(R.drawable.amp6);
                break;
            default:
                volume.setImageResource(R.drawable.amp7);
                break;
        }
    }
    
    @Override  
    protected void onActivityResult(int requestCode, int resultCode,  
                                       Intent intent) {  
    	if (intent == null) {
			return;
		}
    	
     	switch (requestCode) {
     		case CAMERA_WITH_FRONT:
     			hanlderTakePhotoData(intent);
     			if (null == mUploadMessage) return;  
                Uri result = intent == null || resultCode != RESULT_OK ? null  
                        : intent.getData();  
                
                File file = new File(mTakePhotoFilePath);  
                Uri fileUri = Uri.fromFile(file); 
                mUploadMessage.onReceiveValue(fileUri);  
                mUploadMessage = null;  
     			break;
     		case CAMERA_WITH_BACK:
     		{
     			InputStream inputStream = null;
     			   
     			try {
     				inputStream = new FileInputStream(mTakePhotoFilePath);
     			} catch (FileNotFoundException e) {
     				e.printStackTrace();
     			}  
     			
     			ByteArrayOutputStream swapStream = new ByteArrayOutputStream(); 
     			byte[] buff = new byte[100]; //buff用于存放循环读取的临时数据 
     			int rc = 0; 
              
     			try {
     				while ((rc = inputStream.read(buff, 0, 100)) > 0) { 
     				   swapStream.write(buff, 0, rc); 
     				}
     			} catch (IOException e) {
     				e.printStackTrace();
     			} 
              
     			byte[] in_b = swapStream.toByteArray(); //in_b为转换之后的结果 
     			String encodeString = Base64.encodeToString(in_b, Base64.NO_WRAP);
     		}
     	}
     } 
    
    /**
     * 处理拍完照的data数据
     * 
     * @param data
     */
    private String hanlderTakePhotoData(Intent data) {

        if (data == null) {
            // 新建bitmap
            Bitmap newBitmap = ImageTool
                    .createImageThumbnail(mTakePhotoFilePath);
        } else {
            // 生成bitmap
            Bundle extras = data.getExtras();
            Bitmap bitmap = extras == null ? null : (Bitmap) extras.get("data");
            if (bitmap == null) {
                return "";
            }
        }

        InputStream inputStream = null;
        try {
			   inputStream = new FileInputStream(mTakePhotoFilePath);
		   } catch (FileNotFoundException e) {
			   e.printStackTrace();
		   }  
		 
		   Bitmap btp = BitmapFactory.decodeStream(inputStream); 
		   btp = NativeUtil.compressBitmap(btp, 50,null, true);
		   ByteArrayOutputStream baos = new ByteArrayOutputStream();    
		   btp.compress(Bitmap.CompressFormat.JPEG, 40, baos); 
		   String encodeString = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
		   return encodeString;
		  
    }
    
    //调用前置摄像头
    private void takePicturesWithFront() {
    	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  
        
    	intent.putExtra("android.intent.extras.CAMERA_FACING", 1); // 调用前置摄像头
    	intent.putExtra("autofocus", true); // 自动对焦  
    	intent.putExtra("fullScreen", false); // 全屏  
    	intent.putExtra("showActionIcons", false);  
    	 mTakePhotoFilePath = AlbumHelper.getHelper(MainWebViewActivity.this)
                 .getFileDiskCache()
                 + File.separator
                 + System.currentTimeMillis() + ".jpg";
         // mTakePhotoFilePath = getImageSavePath(String.valueOf(System
         // .currentTimeMillis()) + ".jpg");
         intent.putExtra(MediaStore.EXTRA_OUTPUT,
                 Uri.fromFile(new File(mTakePhotoFilePath)));
         startActivityForResult(intent, CAMERA_WITH_FRONT);
	}
    
    //调用后置摄像头
    @JavascriptInterface
    private void takePictures() {
    	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  
        
    	intent.putExtra("autofocus", true); // 自动对焦  
    	intent.putExtra("fullScreen", false); // 全屏  
    	intent.putExtra("showActionIcons", false);  
    	 mTakePhotoFilePath = AlbumHelper.getHelper(MainWebViewActivity.this)
                 .getFileDiskCache()
                 + File.separator
                 + System.currentTimeMillis() + ".jpg";
         // mTakePhotoFilePath = getImageSavePath(String.valueOf(System
         // .currentTimeMillis()) + ".jpg");
         intent.putExtra(MediaStore.EXTRA_OUTPUT,
                 Uri.fromFile(new File(mTakePhotoFilePath)));
         startActivityForResult(intent, CAMERA_WITH_BACK);
         
	}
    
    //选择照片
    @JavascriptInterface
    private void selectePhotos() {
    	// 相册
        if (albumList.size() < 1) {
            Toast.makeText(MainWebViewActivity.this, "相册中没有图片",
                    Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(MainWebViewActivity.this,
                PickPhotoActivity.class);
        intent.putExtra(ConstantKeys.EXTRA_CHAT_USER_ID,
                mSpUtil.getUserId());
        startActivityForResult(intent, ConstantKeys.ALBUM_BACK_DATA);
        MainWebViewActivity.this.overridePendingTransition(
                R.anim.zf_album_enter, R.anim.zf_stay);
        
	}
    
    //上传文件
    @JavascriptInterface
    private void uploadFile() {
    	
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
//            enterRoom.init("enter", jsonBean.base_info.session_id, jsonBean.base_info.room_id);
            enterRoom.init("enter", "asdasdasd", 11111);
            
            String jsonStr = gson.toJson(enterRoom);
            Log.v("=============", jsonStr);
            
//            mSpUtil.setRoomName(jsonBean.base_info.room_name);
            mSpUtil.setRoomName("房间");
            //向服务器发送信息
            try {
    			mConnection.handleConnection(null,"enter",jsonStr,mContent);
    		} catch (JSONException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}   	
    }
    
    
    public synchronized static boolean isFastClick() {
        long time = System.currentTimeMillis();   
        if ( time - lastClickTime < 2000) {   
            return true;   
        }   
        lastClickTime = time;   
        return false;   
    }
    
    @JavascriptInterface
    public void toShare(){
    	Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, "有纠纷怎么办？去哪找调解机构？找哪家合适？人家什么时候上班？…别再纠结啦！下载海沧在线调解APP，在家就能调解，还能了解最新调解动态，在线咨询，让调解更简单。点击下载http://xxxxxx.com");
		sendIntent.setType("text/plain");
		startActivity(sendIntent);
    }
    
    /**
     * 录制语音计时器
     * 
     * @desc:
     * @author: pangzf
     * @date: 2014年11月10日 下午3:46:46
     */
    private class VoiceRcdTimeTask implements Runnable {
        int time = 0;
        
        public VoiceRcdTimeTask(int startTime) {
            time = startTime;
        }

        @Override
        public void run() {
            time++;

            updateTimes(time);
        }
    }
    
    /**
     * 更新文本内容
     * 
     * @param time
     */
    public void updateTimes(final int time) {
    	mSpUtil.setVoiceTime(time);
//        Log.e("fff", "时间:" + time);
        mVioceTime = time;
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mTvVoiceRecorderTime.setText(TimeUtil
                        .getVoiceRecorderTime(time));
            }
        });

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
    
    
    public class myWebClient extends WebViewClient
    {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO Auto-generated method stub

            view.loadUrl(url);
            return true;

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            // TODO Auto-generated method stub
            super.onPageFinished(view, url);

//            progressBar.setVisibility(View.GONE);
        }
    }

    //flipscreen not loading again
    @Override
    public void onConfigurationChanged(Configuration newConfig){        
        super.onConfigurationChanged(newConfig);
    }

    /**
	 * 执行cmd命令，并等待结果
	 * 
	 * @param command
	 *            命令
	 * @return 是否成功执行
	 */
	private boolean runCommand(String command) {
		boolean ret = false;
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(command);
			process.waitFor();
			ret = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				process.destroy();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	/**
     * @Description 初始化相册数据
     */
    
    private void initAlbumData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                albumHelper = AlbumHelper.getHelper(MainWebViewActivity.this);
                albumList = albumHelper.getImagesBucketList(false);
            }
        }).start();
    }

}






