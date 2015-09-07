package com.pzf.liaotian;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;

import net.bither.util.ImageShower;
import net.bither.util.NativeUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pzf.liaotian.adapter.FaceAdapter;
import com.pzf.liaotian.adapter.FacePageAdeapter;
import com.pzf.liaotian.adapter.MessageAdapter;
import com.pzf.liaotian.album.AlbumHelper;
import com.pzf.liaotian.app.PushApplication;
import com.pzf.liaotian.baidupush.client.PushMessageReceiver;
import com.pzf.liaotian.bean.Message;
import com.pzf.liaotian.bean.MessageItem;
import com.pzf.liaotian.bean.RecentItem;
import com.pzf.liaotian.bean.User;
import com.pzf.liaotian.bean.album.ImageBucket;
import com.pzf.liaotian.bean.album.ImageTool;
import com.pzf.liaotian.common.util.HomeWatcher;
import com.pzf.liaotian.common.util.HomeWatcher.OnHomePressedListener;
import com.pzf.liaotian.common.util.L;
import com.pzf.liaotian.common.util.SendMsgAsyncTask;
import com.pzf.liaotian.common.util.SharePreferenceUtil;
import com.pzf.liaotian.common.util.SoundUtil;
import com.pzf.liaotian.common.util.T;
import com.pzf.liaotian.common.util.TimeUtil;
import com.pzf.liaotian.common.util.WebSocketConnectTool;
import com.pzf.liaotian.config.ConstantKeys;
import com.pzf.liaotian.db.MessageDB;
import com.pzf.liaotian.db.RecentDB;
import com.pzf.liaotian.db.UserDB;
import com.pzf.liaotian.view.CirclePageIndicator;
import com.pzf.liaotian.view.JazzyViewPager;
import com.pzf.liaotian.view.JazzyViewPager.TransitionEffect;
import com.pzf.liaotian.view.Util;
import com.pzf.liaotian.xlistview.MsgListView;
import com.pzf.liaotian.xlistview.MsgListView.IXListViewListener;
import com.zztj.chat.bean.EnterChatRoom;
import com.zztj.chat.bean.JsonMessageStruct;
import com.zztj.chat.bean.EnterChatRoom.Base_Info;

/**
 * 
 * @desc: 聊天界面主Activity
 * @author: pangzf
 * @date: 2014年11月3日 上午11:05:33
 * @blog:http://blog.csdn.net/pangzaifei/article/details/43023625
 * @github:https://github.com/pangzaifei/zfIMDemo
 * @qq:1660380990
 * @email:pzfpang451@163.com
 */
@SuppressLint("NewApi")
public class MainViewActivity extends Activity {
	
	Button button;
	Button button2;
	Button button3;
	Button button4;
	Button button5;
	
	EditText edit1;
	EditText edit2;
	SharePreferenceUtil mSpUtil;
	public static WebSocketConnectTool mConnection = WebSocketConnectTool.getInstance();
	private static final int CAMERA_WITH_DATA = 10;
	private String mTakePhotoFilePath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
//		 TouchImageView img = new TouchImageView(this);  
//	        setContentView(img);  
		this.setTheme(R.style.AppTheme);
		 setContentView(R.layout.main);
		 mSpUtil = PushApplication.getInstance().getSpUtil();
		 
		 edit1 = (EditText)findViewById(R.id.editText1);
		 edit2 = (EditText)findViewById(R.id.editText2);
		 edit1.setText("申述人");
		 edit2.setText("00000001");
		 
		 button = (Button)findViewById(R.id.button1);
		 button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent =new Intent(MainViewActivity.this,ChatRoomActivity.class);
				intent.putExtra("USER_NAME", edit1.getText());
				intent.putExtra("USER_ID", edit2.getText());
				Log.v("chat", edit1.getText().toString());
				intent.putExtra("USER_NAME", edit1.getText().toString());
				intent.putExtra("USER_ID", edit2.getText().toString());
				intent.putExtra("IS_PRIVATE_CHAT", 0);
				intent.putExtra("IS_ADMIN", 1);
				intent.putExtra("CHAT_ROOM_ID", "027");
				startActivity(intent);
			}
		});
		 
		 button2 = (Button)findViewById(R.id.button2);
		 button2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_TEXT, "有纠纷怎么办？去哪找调解机构？找哪家合适？人家什么时候上班？…别再纠结啦！下载海沧在线调解APP，在家就能调解，还能了解最新调解动态，在线咨询，让调解更简单。点击下载http://xxxxxx.com");
				sendIntent.setType("text/plain");
				startActivity(sendIntent);
			}
		});
		 
		 button3 = (Button)findViewById(R.id.button3);
		 button3.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(MainViewActivity.this,MainWebViewActivity.class);
				startActivity(intent);
				
			}
		});
		 
		 button4 = (Button)findViewById(R.id.button4);
		 button4.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(MainViewActivity.this,ConsultActivity.class);
				intent.putExtra("USER_NAME", edit1.getText());
				intent.putExtra("USER_ID", edit2.getText());
				Log.v("chat", edit1.getText().toString());
				intent.putExtra("USER_NAME", edit1.getText().toString());
				intent.putExtra("USER_ID", edit2.getText().toString());
				intent.putExtra("IS_PRIVATE_CHAT", 0);
				intent.putExtra("IS_ADMIN", 1);
				intent.putExtra("CHAT_ROOM_ID", "027");
				startActivity(intent);
			}
		});
		 
		 button5 = (Button)findViewById(R.id.button5);
		 button5.setOnClickListener(new OnClickListener() {
			
			 int cameraCount = 0;  
			 Camera c = null;  
			@Override
			public void onClick(View arg0) {
				
				takePicture();				
				
			}
		});
		 
		 
		
		 
	}
	
	
	
	
	  
	  private void takePicture() {
	    	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  
	    	intent.putExtra("android.intent.extras.CAMERA_FACING", 1); // 调用前置摄像头
	    	intent.putExtra("autofocus", true); // 自动对焦  
	    	intent.putExtra("fullScreen", false); // 全屏  
	    	intent.putExtra("showActionIcons", false);  
	    	 mTakePhotoFilePath = AlbumHelper.getHelper(MainViewActivity.this)
	                 .getFileDiskCache()
	                 + File.separator
	                 + System.currentTimeMillis() + ".jpg";
	         // mTakePhotoFilePath = getImageSavePath(String.valueOf(System
	         // .currentTimeMillis()) + ".jpg");
	         intent.putExtra(MediaStore.EXTRA_OUTPUT,
	                 Uri.fromFile(new File(mTakePhotoFilePath)));
	         startActivityForResult(intent, CAMERA_WITH_DATA);
		}
	  
	  @Override  
	    protected void onActivityResult(int requestCode, int resultCode,  
	                                       Intent intent) {  
	     
	     	switch (requestCode) {
	     		case CAMERA_WITH_DATA:
	     			hanlderTakePhotoData(intent);
	     			break;
	     	}
	     } 
	    
	
	   /**
     * 处理拍完照的data数据
     * 
     * @param data
     */
    private void hanlderTakePhotoData(Intent data) {

        if (data == null) {
            // 新建bitmap
            Bitmap newBitmap = ImageTool
                    .createImageThumbnail(mTakePhotoFilePath);
        } else {
            // 生成bitmap
            Bundle extras = data.getExtras();
            Bitmap bitmap = extras == null ? null : (Bitmap) extras.get("data");
            if (bitmap == null) {
                return;
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
		   
		   mTakePhotoFilePath = AlbumHelper.getHelper(MainViewActivity.this)
                   .getFileDiskCache()
                   + File.separator
                   + System.currentTimeMillis() + ".png";
		   
		   DataOutputStream to = null;
		try {
			to = new DataOutputStream(new FileOutputStream(mTakePhotoFilePath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		   try {
			baos.writeTo(to);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	 
        
        //发送照片
        mSpUtil.setIsCome(false);
        String filePath = mTakePhotoFilePath;
        new SendMsgAsyncTask(null, mSpUtil.getUserId(),filePath).send();
        
    }
    public void startChat(String json){
    	//进入聊天室，向服务器提交信息。
    	//组织要提交的json信息
    	
    		 Gson gson = new Gson();
             java.lang.reflect.Type type = new TypeToken<JsonMessageStruct>(){}.getType();
             JsonMessageStruct jsonBean = gson.fromJson(json, type);
             
        	EnterChatRoom enterRoom = new EnterChatRoom();
            enterRoom.setBaseInfo(new Base_Info());
            enterRoom.init("enter", jsonBean.base_info.session_id, jsonBean.base_info.room_id);
//            enterRoom.init("enter", "asdasdasd", 11111);
            
            String jsonStr = gson.toJson(enterRoom);
            Log.v("=============", jsonStr);
                        
			mSpUtil.setRoomName(jsonBean.base_info.room_name);
            //向服务器发送信息
            try {
    			mConnection.handleConnection(null,"enter",jsonStr,null);
    		} catch (JSONException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}   	
    }   
}
