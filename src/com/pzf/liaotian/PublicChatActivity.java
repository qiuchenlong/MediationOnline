package com.pzf.liaotian;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;

import android.R.integer;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.pzf.liaotian.common.util.AudioRecorder2Mp3Util;
import com.google.gson.Gson;
import com.huneng.fileexplorer.UploadView;
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

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

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
public class PublicChatActivity extends Activity implements OnClickListener,
        PushMessageReceiver.EventHandler, OnTouchListener, IXListViewListener,
        OnHomePressedListener {

    public static final int NEW_MESSAGE = 0x001;// 收到消息
    public static int MSGPAGERNUM;
    private static final int POLL_INTERVAL = 300;
    private static final long DELAY_VOICE = 1000;// 语音录制计时
    private static final int CAMERA_WITH_DATA = 10;
    private static final int FILE_PATH = 11;
    private static SharePreferenceUtil mSpUtil;

    private InputMethodManager mInputMethodManager;
    public static EditText mEtMsg;

    public PushApplication mApplication;

    private Button mBtnSend;// 发送消息按钮
    public static MessageAdapter adapter;// 发送消息展示的adapter
    public static MsgListView mMsgListView;// 展示消息的
    public static MessageDB mMsgDB;// 保存消息的数据库
    public static RecentDB mRecentDB;
    private static Gson mGson;
    private WindowManager.LayoutParams mParams;

    private HomeWatcher mHomeWatcher;// home键

    // 接受数据
    public UserDB mUserDB;
    private SendMsgAsyncTask mSendTask;
    private TextView mTvVoiceBtn;// 语音按钮
    private ImageButton mIbMsgBtn;// 文字按钮
    private View mViewVoice;// 语音界面
    private View mViewInput;
    private SoundUtil mSoundUtil;
    private ImageButton mIbVoiceBtn;

    private ImageView mIvDelete;// 语音弹出框的差号按钮
    private LinearLayout mLLDelete;
    private ImageView mIvBigDeleteIcon;
    private View mChatPopWindow;
    private LinearLayout mLlVoiceLoading;// 加载录制loading
    private LinearLayout mLlVoiceRcding;
    private LinearLayout mLlVoiceShort;// 录制时间过短
    private Handler mHandler = new Handler();
    private int flag = 1;
    private boolean isShosrt = false;

    private long mStartRecorderTime;
    private long mEndRecorderTime;

    AudioRecorder2Mp3Util util = null;
    private ImageView volume;
    private String mRecordTime;
    private TextView mTvVoiceRecorderTime;// 录制的时间
    private int mRcdStartTime = 0;// 录制的开始时间
    private int mRcdVoiceDelayTime = 1000;
    private int mRcdVoiceStartDelayTime = 300;
    private boolean isCancelVoice;// 不显示语音
    
    private Button backButton;//返回按钮
    private TextView titleTextView;//标题
//    public static TextView mHomeNotice;//公告
    private int is_admin;//是否是管理员
    
    private Button quit_privatechat_button;//退出悄悄话
    private RelativeLayout private_chat_view;
    //悄悄话
    private TextView private_chat_to_mediator;
    //上传文件按钮
    private TextView uploadView;
    //申述点 按钮
    private Button shensuButton;
    
    private int mVioceTime;
	public static WebSocketConnectTool mConnection = WebSocketConnectTool.getInstance();

    private Runnable mSleepTask = new Runnable() {
        public void run() {
            stopRecord();
        }
    };

    private Runnable mPollTask = new Runnable() {
        public void run() {
        	double amp = (double)(Math.random() * 11) ;
            Log.e("fff", "音量:" + amp);
            updateDisplay(amp);
            mHandler.postDelayed(mPollTask, POLL_INTERVAL);
        }
    };

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
     * @Description 滑动到列表底部
     */
    private void scrollToBottomListItem() {

        // todo eric, why use the last one index + 2 can real scroll to the
        // bottom?
        if (mMsgListView != null) {
            mMsgListView.setSelection(adapter.getCount() + 1);
        }
    }

    private VoiceRcdTimeTask mVoiceRcdTimeTask;
    private ScheduledExecutorService mExecutor;// 录制计时器
    public ImageButton mBtnAffix;
    public ImageButton mBtnAffix2;
    
    private LinearLayout mLlAffix;
    private TextView mTvTakPicture;// 拍照
    private String mTakePhotoFilePath;
    private TextView mIvAffixAlbum;// 相册
    private AlbumHelper albumHelper = null;// 相册管理类
    private static List<ImageBucket> albumList = null;// 相册数据list
    private TextView mTvChatTitle;
    public static PublicChatActivity chatContext;
    private Boolean recodePermission;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setTheme(R.style.AppTheme);
        setContentView(R.layout.zf_chat_main);
        mParams = getWindow().getAttributes();
        chatContext = this;

        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mSpUtil = PushApplication.getInstance().getSpUtil();
        Set<String> keySet = PushApplication.getInstance().getFaceMap()
                .keySet();

        MSGPAGERNUM = 0;
        mSoundUtil = SoundUtil.getInstance();
//
        initView();

        mApplication.getNotificationManager().cancel(
                PushMessageReceiver.NOTIFY_ID);
        PushMessageReceiver.mNewNum = 0;

        mUserDB = mApplication.getUserDB();
        
        recodePermission = mSpUtil.getRecordPermission();

        initUserInfo();
//        
//        try {
//			mConnection.handleConnection(null,null,null,null);
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
    
    }
    
    @Override  
    protected void onStart() {  
        super.onStart();  
       mSpUtil.setIsConsult(false);
    } 

    /**
     * 更新文本内容
     * 
     * @param time
     */
    public void updateTimes(final int time) {
    	mSpUtil.setVoiceTime(time);
        Log.e("fff", "时间:" + time);
        mVioceTime = time;
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mTvVoiceRecorderTime.setText(TimeUtil
                        .getVoiceRecorderTime(time));
            }
        });

    }

    /**
     * 初始化用户信息
     */
    
    private void initUserInfo() {
    	 mSpUtil.setServerIP("ws://192.168.0.228:8484");
    	Intent intent = getIntent();
    	
    	String userid = intent.getStringExtra("USER_ID");
    	if (userid == null || userid.equals("")) {
			userid = "0";
		}
    	mSpUtil.setUserId(userid);
    	
    	mSpUtil.setNick(intent.getStringExtra("USER_NAME"));
    	mSpUtil.setIsAdmin(intent.getIntExtra("IS_ADMIN", 0));
    	
    	int isprivatechat = intent.getIntExtra("IS_PRIVATE_CHAT", 0);
    	mSpUtil.setIsPrivateChat(0);
    	Log.v("chat", "isprivatechat = " + isprivatechat);
    	
    	int chatRoomId = intent.getIntExtra("CHAT_ROOM_ID",0);
    	mSpUtil.setRoomID(chatRoomId);
		mTvChatTitle.setText("在线调解会议室"+chatRoomId);
		
		MessageItem item = new MessageItem(MessageItem.MESSAGE_TYPE_TEXT,
          		mSpUtil.getNick(), System.currentTimeMillis(),
                intent.getStringExtra("CONTENT"), 0, true, 1,
                  0,0,0,0,MessageItem.SYSTEM_MESSAGE);
		adapter.upDateMsg(item);// 更新界面
    	
    }

    /**
     * @Description 初始化相册数据
     */
    
    private void initAlbumData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                albumHelper = AlbumHelper.getHelper(PublicChatActivity.this);
                albumList = albumHelper.getImagesBucketList(false);
            }
        }).start();
    }

    private void initView() {
        initAlbumData();
        
        mTvChatTitle = (TextView) findViewById(R.id.tv_chat_title);
        // 图片附件   
        mBtnAffix = (ImageButton) findViewById(R.id.btn_chat_affix);
        mBtnAffix2 = (ImageButton) findViewById(R.id.btn_chat_affix2);
        mLlAffix = (LinearLayout) findViewById(R.id.ll_chatmain_affix);      
        mTvTakPicture = (TextView) findViewById(R.id.tv_chatmain_affix_take_picture);
        mBtnAffix.setOnClickListener(this);
        mBtnAffix2.setOnClickListener(this);
        mTvTakPicture.setOnClickListener(this);
        
     // 相册
        mIvAffixAlbum = (TextView) findViewById(R.id.tv_chatmain_affix_album);
        mIvAffixAlbum.setOnClickListener(this);

        mEtMsg = (EditText) findViewById(R.id.msg_et);

        mBtnSend = (Button) findViewById(R.id.send_btn);
        mBtnSend.setClickable(true);
        mBtnSend.setEnabled(true);
        mBtnSend.setOnClickListener(this);
        
     // 消息
        mApplication = PushApplication.getInstance();
        mMsgDB = mApplication.getMessageDB();// 发送数据库
        mRecentDB = mApplication.getRecentDB();// 接收消息数据库
        mGson = mApplication.getGson();

        adapter = new MessageAdapter(this, initMsgData());
        mMsgListView = (MsgListView) findViewById(R.id.msg_listView);
        
        // 触摸ListView隐藏表情和输入法
        mMsgListView.setOnTouchListener(this);
        mMsgListView.setPullLoadEnable(false);
        mMsgListView.setXListViewListener(this);
        mMsgListView.setAdapter(adapter);
        mMsgListView.setSelection(adapter.getCount() - 1);

        mEtMsgOnKeyListener();

        //公告
//        mHomeNotice = (TextView)findViewById(R.id.zxtj_home_notice);
        //标题
        titleTextView = (TextView)findViewById(R.id.tv_chat_title);
        
        //上传文件按钮
        uploadView = (TextView)findViewById(R.id.upload_file_textview);
        uploadView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(PublicChatActivity.this,UploadView.class);
				startActivityForResult(intent, FILE_PATH);
				
			}
		});
        
        
        //悄悄话
        private_chat_view = (RelativeLayout)findViewById(R.id.private_chat_view);      
        private_chat_to_mediator = (TextView)findViewById(R.id.speak_to_mediator); 
        quit_privatechat_button = (Button)findViewById(R.id.private_chat_quit_button);
        private_chat_to_mediator.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				private_chat_view.setVisibility(View.VISIBLE);
				mSpUtil.setIsPrivateChat(1);//开启悄悄话
				mLlAffix.setVisibility(View.GONE);//关闭弹出
				mBtnAffix.setBackgroundResource(R.drawable.zztj_add);
				mBtnAffix2.setBackgroundResource(R.drawable.zztj_add);
//				if (mHomeNotice.VISIBLE == View.VISIBLE) {
//					mHomeNotice.setVisibility(View.GONE);
//				}
			}
		});
              
        quit_privatechat_button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				private_chat_view.setVisibility(View.GONE);
				mSpUtil.setIsPrivateChat(0);//关闭悄悄话
			}
		});
        
        //返回按钮
        backButton = (Button)findViewById(R.id.private_chat_back_button);
        backButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.v("chat", "back back");
				
				mSpUtil.setIsPrivateChat(0);
				finish();
			}
		});
        
        // 语音
        initRecorderView();

        mIvDelete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 如果是删除按键,结束录音，还原按钮，删除文件
                stopRecord();
            }
        });
        mTvVoicePreeListener();// 按住录音按钮的事件
        
        //申诉点
        shensuButton = (Button)findViewById(R.id.shensu_button);
        shensuButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				 Intent intent =new Intent(PublicChatActivity.this,WebViewActivity.class);  
                 intent.putExtra("URL_PATH", "http://www.csdn.net");
                 startActivity(intent);
				
			}
		});
    }
    
    

    /**
     * 按住录音按钮的事件
     */
    private void mTvVoicePreeListener() {
        // 按住录音添加touch事件
        mTvVoiceBtn.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!Environment.getExternalStorageDirectory().exists()) {
                    Toast.makeText(PublicChatActivity.this, "No SDCard",
                            Toast.LENGTH_LONG).show();
                    return false;
                }
               
                int[] location = new int[2];
                mTvVoiceBtn.getLocationInWindow(location); // 获取在当前窗口内的绝对坐标
                int[] del_location = new int[2];
                mLLDelete.getLocationInWindow(del_location);
                int del_Y = del_location[1];
                int del_x = del_location[0];
                
              
        		
        		if (recodePermission == false) {
        			 String fileName = "/" + mSpUtil.getUserId() +System.currentTimeMillis();
        		     String fullPath = chatContext.getExternalFilesDir(null).toString() + "/voice";
        		     File f = new File(fullPath);
        		     if (!f.exists()) {
        		      f.mkdirs();
        		     } 
        		     
        		     mStartRecorderTime = System.currentTimeMillis();

     	            if (util == null) {
     					util = new AudioRecorder2Mp3Util(null,
     							fullPath+fileName+".raw",
     							fullPath+fileName+".mp3");
     				}
     	            util.startRecording();        
     	            util.cleanFile(AudioRecorder2Mp3Util.RAW);
     	            util.cleanFile(AudioRecorder2Mp3Util.MP3);
        		    util.stopRecordingAndConvertFile();
        		    
        		    recodePermission = true;
        		    mSpUtil.setRecordPermission(true);
        		} else {
					
        			 if (event.getAction() == MotionEvent.ACTION_DOWN && flag == 1) {
     					Log.v("chat", "action down");
                         if (!Environment.getExternalStorageDirectory().exists()) {
                             Toast.makeText(PublicChatActivity.this, "No SDCard",
                                     Toast.LENGTH_LONG).show();
                             return false;
                         }
                         // 判断手势按下的位置是否是语音录制按钮的范围内
//                         mTvVoiceBtn.setBackgroundResource(R.drawable.voice_rcd_btn_pressed);
                         mTvVoiceBtn.setBackgroundColor(Color.rgb(251,64,71));
                         mChatPopWindow.setVisibility(View.VISIBLE);
                         mLlVoiceLoading.setVisibility(View.VISIBLE);
                         mLlVoiceRcding.setVisibility(View.GONE);
                         mLlVoiceShort.setVisibility(View.GONE);
                         mHandler.postDelayed(new Runnable() {
                             public void run() {
                                 if (!isShosrt) {
                                     mLlVoiceLoading.setVisibility(View.GONE);
                                     mLlVoiceRcding.setVisibility(View.VISIBLE);
                                 }
                             }
                         }, 300);
                         
                         mLLDelete.setVisibility(View.GONE);
                         startRecord();
                         flag = 2;
                     } else if (event.getAction() == MotionEvent.ACTION_UP
                             && flag == 2) {// 松开手势时执行录制完成
                     	Log.v("chat", "up");
                         System.out.println("4");
//                         mTvVoiceBtn.setBackgroundResource(R.drawable.zztj_button_background_color);
                         mTvVoiceBtn.setBackgroundColor(Color.rgb(254,191,192));
                         mLlVoiceRcding.setVisibility(View.GONE);
                         
                         try {
                             stopRecord();
                         } catch (IllegalStateException e) {
                             Toast.makeText(PublicChatActivity.this, "麦克风不可用", 0).show();
                             isCancelVoice = true;
                         }
                         mEndRecorderTime = System.currentTimeMillis();
                         flag = 1;
                         int mVoiceTime = (int) ((mEndRecorderTime - mStartRecorderTime) / 1000);
                         if (mVoiceTime < 1) {
                             isShosrt = true;
                             mLlVoiceLoading.setVisibility(View.GONE);
                             mLlVoiceRcding.setVisibility(View.GONE);
                             mLlVoiceShort.setVisibility(View.VISIBLE);
                             mHandler.postDelayed(new Runnable() {
                                 public void run() {
                                     mLlVoiceShort.setVisibility(View.GONE);
                                     mChatPopWindow.setVisibility(View.GONE);
                                     isShosrt = false;
                                 }
                             }, 500);

                             return false;
                         }
                         // ===发送出去,界面展示
                         if (!isCancelVoice) {
                             showVoice(mVoiceTime);
                         }
                     }
        			 
				}
               
                return false;

            }
        });
    }

    /**
     * 输入框key监听事件
     */
    private void mEtMsgOnKeyListener() {
        mEtMsg.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                }
                return false;
            }
        });
        mEtMsg.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    mBtnSend.setEnabled(true);
                    mIbMsgBtn.setVisibility(View.GONE);
                    mBtnSend.setVisibility(View.VISIBLE);
                } else {
                    mBtnSend.setEnabled(false);
                    mIbMsgBtn.setVisibility(View.VISIBLE);
                    mBtnSend.setVisibility(View.GONE);
                }
            }
        });

    }

    /**
     * 初始化语音布局
     */
    private void initRecorderView() {
        mIbMsgBtn = (ImageButton) findViewById(R.id.ib_chatmain_msg);
        mIbMsgBtn.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				switch(arg1.getAction()){
                case MotionEvent.ACTION_DOWN:
                	mIbMsgBtn.setBackgroundResource(R.drawable.zztj_voice_button);
                    break;
                case MotionEvent.ACTION_UP:
                	mIbMsgBtn.setBackgroundResource(R.drawable.zztj_voice_unpress);
                    break;
                }
				return false;
			}
		});
        
        mViewVoice = findViewById(R.id.ll_chatmain_voice);
        mIbVoiceBtn = (ImageButton) findViewById(R.id.ib_chatmain_voice);
        mIbVoiceBtn.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				switch(arg1.getAction()){
                case MotionEvent.ACTION_DOWN:
                	mIbVoiceBtn.setBackgroundResource(R.drawable.zztj_keyboard_press);
                    break;
                case MotionEvent.ACTION_UP:
                	mIbVoiceBtn.setBackgroundResource(R.drawable.zztj_keyboard_button);
                    break;
                }				return false;
			}
		});
        
        mViewInput = findViewById(R.id.ll_chatmain_input);
        mTvVoiceBtn = (TextView) findViewById(R.id.tv_chatmain_press_voice);
        
        mIbMsgBtn.setOnClickListener(this);
        mTvVoiceBtn.setOnClickListener(this);
        mIbVoiceBtn.setOnClickListener(this);

        // include包含的布局语音模块
        mIvDelete = (ImageView) this.findViewById(R.id.img1);
        mLLDelete = (LinearLayout) this.findViewById(R.id.del_re);
        mIvBigDeleteIcon = (ImageView) this.findViewById(R.id.sc_img1);
        mChatPopWindow = this.findViewById(R.id.rcChat_popup);
        mChatPopWindow.bringToFront();
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
     * 是否是删除按钮，暂无用
     * 
     * @param deleteImage
     * @param event
     * @return
     */
    protected boolean isDelete(ImageView deleteImage, MotionEvent event) {
        int[] location = new int[2];
        deleteImage.getLocationInWindow(location);
        int width = deleteImage.getWidth();
        int height = deleteImage.getHeight();
        float upY = event.getY();
        float upX = event.getX();
        int imageY = location[1];
        int imageX = location[0];
        if (upY >= imageY && upY <= height + imageY && upX >= imageX
                && upX <= imageX + width) {
            Log.e("fff", "删除");

        }

        return false;
    }

    /**
     * 语音界面展示
     * 
     * @param mVoiceTime
     */
    protected void showVoice(int mVoiceTime) {
	
//    	mHomeNotice.setVisibility(View.INVISIBLE);
    	String filePath = util.getFilePath(AudioRecorder2Mp3Util.MP3);

    	long currentTime = System.currentTimeMillis();
    	int isHide = isHideTimeLabel(currentTime);
        MessageItem item = new MessageItem(MessageItem.MESSAGE_TYPE_RECORD,
                mSpUtil.getNick(), currentTime, filePath,
                mSpUtil.getHeadIcon(), false, 0, mVioceTime,mSpUtil.getIsPrivateChat(),isHide,0,MessageItem.NOT_SYSTEM_MESSAGE);
        adapter.upDateMsg(item);
        mMsgListView.setSelection(adapter.getCount() - 1);
        mMsgDB.saveMsg(mSpUtil.getUserId(), item);// 消息保存数据库
        mSpUtil.setIsCome(false);
        //发送语音
        new SendMsgAsyncTask(null, mSpUtil.getUserId(),filePath).send();// push发送消息到服务器
        // ===保存近期的消息
       
        RecentItem recentItem = new RecentItem(MessageItem.MESSAGE_TYPE_RECORD,
                mSpUtil.getUserId(), 0, mSpUtil.getNick(),filePath, 0, System.currentTimeMillis(),
                item.getVoiceTime(),mSpUtil.getIsPrivateChat());
        mRecentDB.saveRecent(recentItem);
        scrollToBottomListItem();
    }

    
    @Override
    protected void onResume() {
        super.onResume();
        mHomeWatcher = new HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(this);
        mHomeWatcher.startWatch();
        PushMessageReceiver.ehList.add(this);// 监听推送的消息

    }

    @Override
    protected void onPause() {
        mInputMethodManager.hideSoftInputFromWindow(mEtMsg.getWindowToken(), 0);
//        mllFace.setVisibility(View.GONE);
//        isFaceShow = false;
        super.onPause();
        mHomeWatcher.setOnHomePressedListener(null);
        mHomeWatcher.stopWatch();
        PushMessageReceiver.ehList.remove(this);// 移除监听
    }

    public static MessageAdapter getMessageAdapter() {
        return adapter;
    }

    /**
     * 加载消息历史，从数据库中读出
     */
    private List<MessageItem> initMsgData() {
        List<MessageItem> list = mMsgDB
                .getMsg(mSpUtil.getUserId(), MSGPAGERNUM);
        List<MessageItem> msgList = new ArrayList<MessageItem>();// 消息对象数组
        
//        if (mHomeNotice == null) {
//			mHomeNotice = (TextView)findViewById(R.id.zxtj_home_notice);
//		}
//        if (list.size() > 5) {
//        	mHomeNotice.setVisibility(View.GONE);
//		} else {
//			mHomeNotice.setVisibility(View.VISIBLE);
//		}
        
        if (list.size() > 0) {
//        	mHomeNotice.setVisibility(View.GONE);
        	
            for (MessageItem entity : list) {
                if (entity.getName().equals("")) {
                    entity.setName(mSpUtil.getNick());
                }
                if (entity.getHeadImg() < 0) {
                    entity.setHeadImg(0);
                }
                
                if (entity.getIsPrivateChat() == 1) {
                	msgList.add(entity);
                	continue;
				}
                msgList.add(entity);
               
            }
        } else {
        	String str = "";
        	MessageItem  item = new MessageItem(MessageItem.MESSAGE_TYPE_TEXT,
                    "", 0,
                    str, 1, true, 1,
                    0,0,0,0,MessageItem.NOT_SYSTEM_MESSAGE);
        	msgList.add(item);
        	mMsgDB.saveMsg(mSpUtil.getUserId(), item);// 保存数据库
//        	mHomeNotice.setVisibility(View.VISIBLE);
        }
        return msgList;

    }
    
    private void initData() {
        String filePath = "/sdcard/Test/";
        String fileName = "log.txt";
        
        writeTxtToFile("txt content", filePath, fileName);
    }
    
 // 将字符串写入到文本文件中
    public static void writeTxtToFile(String strcontent, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);
        
        String strFilePath = filePath+fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }
     
    // 生成文件
    public static File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
     
    // 生成文件夹
    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e+"");
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        	//发送文本
            case R.id.send_btn: {
            	  //隐藏公告
//                mHomeNotice.setVisibility(View.INVISIBLE);
                // 发送消息
                String msg = mEtMsg.getText().toString();
                sendTextMessage(msg,false);
              
                break;
            }

            case R.id.ib_chatmain_msg: {
                // 切换文字按钮
                if (!mViewVoice.isShown()) {
                    mViewVoice.setVisibility(View.VISIBLE);
                    mViewInput.setVisibility(View.GONE);
                } else {
                    mViewVoice.setVisibility(View.GONE);
                    mViewInput.setVisibility(View.VISIBLE);
                }

                break;
            }

            case R.id.ib_chatmain_voice: {
                // 切换语音按钮
                if (!mViewVoice.isShown()) {
                    mViewVoice.setVisibility(View.VISIBLE);
                    mViewInput.setVisibility(View.GONE);
                } else {
                    mViewVoice.setVisibility(View.GONE);
                    mViewInput.setVisibility(View.VISIBLE);
                }
                break;
            }

            case R.id.tv_chatmain_press_voice: {
                // 按住说话
                // 弹出音量框

                break;
            }
            case R.id.btn_chat_affix: {
                // 图片附件
                if (mLlAffix.isShown()) {
                    mLlAffix.setVisibility(View.GONE);
                    mBtnAffix.setBackgroundResource(R.drawable.zztj_add);
                } else {
                    mLlAffix.setVisibility(View.VISIBLE);
                    mBtnAffix.setBackgroundResource(R.drawable.zztj_add_press);
                }
                break;
            }
            case R.id.btn_chat_affix2: {
                // 图片附件
                if (mLlAffix.isShown()) {
                    mLlAffix.setVisibility(View.GONE);
                    mBtnAffix2.setBackgroundResource(R.drawable.zztj_add);
                } else {
                    mLlAffix.setVisibility(View.VISIBLE);
                    mBtnAffix2.setBackgroundResource(R.drawable.zztj_add_press);
                }
                break;
            }
            
            case R.id.tv_chatmain_affix_take_picture: {
 
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                mTakePhotoFilePath = AlbumHelper.getHelper(PublicChatActivity.this)
                        .getFileDiskCache()
                        + File.separator
                        + System.currentTimeMillis() + ".jpg";
                // mTakePhotoFilePath = getImageSavePath(String.valueOf(System
                // .currentTimeMillis()) + ".jpg");
                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(new File(mTakePhotoFilePath)));
                startActivityForResult(intent, CAMERA_WITH_DATA);
                mLlAffix.setVisibility(View.GONE);
                mBtnAffix.setBackgroundResource(R.drawable.zztj_add);
           	 	mBtnAffix2.setBackgroundResource(R.drawable.zztj_add);
                break;
            }
            case R.id.tv_chatmain_affix_album: {
                // 相册
                if (albumList.size() < 1) {
                    Toast.makeText(PublicChatActivity.this, "相册中没有图片",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(PublicChatActivity.this,
                        PickPhotoActivity.class);
                intent.putExtra(ConstantKeys.EXTRA_CHAT_USER_ID,
                        mSpUtil.getUserId());
                startActivityForResult(intent, ConstantKeys.ALBUM_BACK_DATA);
                PublicChatActivity.this.overridePendingTransition(
                        R.anim.zf_album_enter, R.anim.zf_stay);
                mLlAffix.setVisibility(View.GONE);
                mBtnAffix.setBackgroundResource(R.drawable.zztj_add);
                mBtnAffix2.setBackgroundResource(R.drawable.zztj_add);
                scrollToBottomListItem();
                break;
            }

        }
    }

    public static String getImageSavePath(String fileName) {

        if (TextUtils.isEmpty(fileName)) {
            return null;
        }

        final File folder = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath()
                + File.separator
                + "PngZaiFei-IM"
                + File.separator
                + "images");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        return folder.getAbsolutePath() + File.separator + fileName;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("fff", "结果:" + resultCode);
        if (0 == resultCode) {
            return;
        }
        
        if (mLlAffix.VISIBLE == View.GONE) {
        	 mBtnAffix.setBackgroundResource(R.drawable.zztj_add);
        	 mBtnAffix2.setBackgroundResource(R.drawable.zztj_add);
		}
        
        switch (requestCode) {
            case CAMERA_WITH_DATA:
                hanlderTakePhotoData(data);
               
                break;
            case FILE_PATH:
            	String path = data.getStringExtra("FilePath");
            	Log.v("chat", path);
            	String msg = path.substring(path.lastIndexOf('/')+1);
            	
            	if (!path.contains(".doc")) {
            		Toast.makeText(this, "只能发送.doc文件", Toast.LENGTH_SHORT).show();
            		return;
				}
            	long currentTime = System.currentTimeMillis();
            	int isHide = isHideTimeLabel(currentTime);
                MessageItem item = new MessageItem(
                         MessageItem.MESSAGE_TYPE_FILE, mSpUtil.getNick(),
                         currentTime, msg, mSpUtil.getHeadIcon(),
                         false, 0, 0,mSpUtil.getIsPrivateChat(),isHide,0,MessageItem.NOT_SYSTEM_MESSAGE);

                 adapter.upDateMsg(item);
                 mMsgListView.setSelection(adapter.getCount() - 1);
                 mMsgDB.saveMsg(mSpUtil.getUserId(), item);// 消息保存数据库
                 mEtMsg.setText("");
                 
                 if (mLlAffix.isShown()) {
                     mLlAffix.setVisibility(View.GONE);
                 }
                 mSpUtil.setIsCome(false);
                 //发送文件
                 new SendMsgAsyncTask(null, null,path).send();// push发送消息到服务器
                
            	break;
            default:
            	return;
        }

    }

    /**
     * 处理拍完照的data数据
     * 
     * @param data
     */
    private void hanlderTakePhotoData(Intent data) {

//    	mHomeNotice.setVisibility(View.INVISIBLE);
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

        long currentTime = System.currentTimeMillis();
    	int isHide = isHideTimeLabel(currentTime);
        // listview展示
        MessageItem item = new MessageItem(MessageItem.MESSAGE_TYPE_IMG,
                mSpUtil.getNick(), currentTime,
                mTakePhotoFilePath, mSpUtil.getHeadIcon(), false, 0, 0,mSpUtil.getIsPrivateChat(),isHide,0,MessageItem.NOT_SYSTEM_MESSAGE);
        adapter.upDateMsg(item);

        // 保存到数据库中
        MessageItem messageItem = new MessageItem(MessageItem.MESSAGE_TYPE_IMG,
                mSpUtil.getNick(), currentTime,
                mTakePhotoFilePath, mSpUtil.getHeadIcon(), false, 0, 0,mSpUtil.getIsPrivateChat(),isHide,0,MessageItem.NOT_SYSTEM_MESSAGE);

        mMsgDB.saveMsg(mSpUtil.getUserId(), messageItem);

        // 保存到最近数据库中
        RecentItem recentItem = new RecentItem(MessageItem.MESSAGE_TYPE_IMG,
                mSpUtil.getUserId(), mSpUtil.getHeadIcon(), mSpUtil.getNick(),
                mTakePhotoFilePath, 0, System.currentTimeMillis(), 0,mSpUtil.getIsPrivateChat());
        mRecentDB.saveRecent(recentItem);
        // 发送push
        Message message = new Message(MessageItem.MESSAGE_TYPE_IMG,
                System.currentTimeMillis(), messageItem.getMessage(), "", 0);
        if ("".equals(mSpUtil.getUserId())) {
            Log.e("fff", "用户id为空4");
        }
        
        
        //发送照片
        mSpUtil.setIsCome(false);
        String filePath = mTakePhotoFilePath;
        new SendMsgAsyncTask(mGson.toJson(message), mSpUtil.getUserId(),filePath).send();
        
    }
    

    /**
     * 结束录音
     */
    private void stopRecord() throws IllegalStateException {
    	
    	util.stopRecordingAndConvertFile();
////		Toast.makeText(this, "ok", 0).show();
		util.cleanFile(AudioRecorder2Mp3Util.RAW);
//		// 如果要关闭可以
//		util.close();
//		util = null;
		
        mHandler.removeCallbacks(mSleepTask);
        mHandler.removeCallbacks(mPollTask);
//
        volume.setImageResource(R.drawable.amp1);
        if (mExecutor != null && !mExecutor.isShutdown()) {
            mExecutor.shutdown();
            mExecutor = null;
        }
        if (mSoundUtil != null) {
//            mSoundUtil.stopRecord();
        }
    }

    /**
     * 开始录音
     */
    private void startRecord() {
        // ===录音格式：用户id_时间戳_send_sound
        // SoundUtil.getInstance().startRecord(PublicChatActivity.this,
        // id_time_send_sound);
    	 String fileName = "/" + mSpUtil.getUserId() +System.currentTimeMillis();
	     String fullPath = chatContext.getExternalFilesDir(null).toString() + "/voice";
	     File f = new File(fullPath);
	     if (!f.exists()) {
	      f.mkdirs();
	     } 

	     
        mStartRecorderTime = System.currentTimeMillis();
        if (mSoundUtil != null) {
//            mRecordTime = mSoundUtil.getRecordFileName();
//            
            if (util == null) {
				util = new AudioRecorder2Mp3Util(null,
						fullPath+fileName+".raw",
						fullPath+fileName+".mp3");
			}
            util.startRecording();
            
//			if (canClean) {
				util.cleanFile(AudioRecorder2Mp3Util.RAW);
//			}
            
//            mSoundUtil.startRecord(PublicChatActivity.this, fullPath+fileName+".amr");
            mHandler.postDelayed(mPollTask, POLL_INTERVAL);
//
            mVoiceRcdTimeTask = new VoiceRcdTimeTask(mRcdStartTime);
//
            if (mExecutor == null) {
                mExecutor = Executors.newSingleThreadScheduledExecutor();
                mExecutor.scheduleAtFixedRate(mVoiceRcdTimeTask,
                        mRcdVoiceStartDelayTime, mRcdVoiceDelayTime,
                        TimeUnit.MILLISECONDS);
            }

        }

    }

    // 防止乱pageview乱滚动
    private OnTouchListener forbidenScroll() {
        return new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    return true;
                }
                return false;
            }
        };
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
    public void onMessage(Message message) {

    }

    @Override
    public void onBind(String method, int errorCode, String content) {
        if (errorCode == 0) {// 如果绑定账号成功，由于第一次运行，给同一tag的人推送一条新人消息
            User u = new User(mSpUtil.getUserId(), mSpUtil.getChannelId(),
                    mSpUtil.getNick(), mSpUtil.getHeadIcon(), 0);
            mUserDB.addUser(u);// 把自己添加到数据库
            // com.way.bean.Message msgItem = new com.way.bean.Message(
            // System.currentTimeMillis(), " ", mSpUtil.getTag());
            // new SendMsgAsyncTask(mGson.toJson(msgItem), "").send();;
        }

    }

    @Override
    public void onNotify(String title, String content) {

    }

    @Override
    public void onNetChange(boolean isNetConnected) {
        if (!isNetConnected)
            T.showShort(this, "网络连接已断开");

    }

    @Override
    public void onNewFriend(User u) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (v.getId()) {
//            case R.id.msg_listView:
//                mInputMethodManager.hideSoftInputFromWindow(
//                        mEtMsg.getWindowToken(), 0);
//                break;
//            case R.id.msg_et:
//                mInputMethodManager.showSoftInput(mEtMsg, 0);
//                break;

            default:
                break;
        }
        return false;
    }

    @Override
    public void onRefresh() {
        MSGPAGERNUM++;
        List<MessageItem> msgList = initMsgData();
        int position = adapter.getCount();
        adapter.setmMsgList(msgList);
        mMsgListView.stopRefresh();
        mMsgListView.setSelection(adapter.getCount() - position - 1);
        L.i("MsgPagerNum = " + MSGPAGERNUM + ", adapter.getCount() = "
                + adapter.getCount());
    }

    @Override
    public void onLoadMore() {

    }

    @Override
    public void onHomePressed() {
        mApplication.showNotification();
    }

    @Override
    public void onHomeLongPressed() {

    }
    
    private static int isHideTimeLabel(long newtime) {
    	long oldtime = mSpUtil.getsystemtime();
    	
    	long interval = newtime - oldtime;
    	if (interval > 1000*5) {
    		Log.v("chat_time", "interval="+ interval);
			Log.v("chat_time", "interval="+ new java.util.Date(interval));
			mSpUtil.setSystemTime(newtime);
			return 0;
		} else {
			return 1;
		}
    	
    
    }
    
    public static void sendTextMessage(String message,Boolean isSystemMessage) {
//    	mHomeNotice.setVisibility(View.INVISIBLE);
    	String msg = message;

    	long currentTime = System.currentTimeMillis();
    	int isHide = isHideTimeLabel(currentTime);
    	
    	int isSystem;
    	if (isSystemMessage == true) {
    		mSpUtil.setIsSystemMessage(MessageItem.SYSTEM_MESSAGE);
			isSystem = MessageItem.SYSTEM_MESSAGE;
		} else {
			mSpUtil.setIsSystemMessage(MessageItem.NOT_SYSTEM_MESSAGE);
			isSystem = MessageItem.NOT_SYSTEM_MESSAGE;
		}
    	
        MessageItem item = new MessageItem(
                MessageItem.MESSAGE_TYPE_TEXT, mSpUtil.getNick(),
                currentTime, msg, mSpUtil.getHeadIcon(),
                false, 0, 0,mSpUtil.getIsPrivateChat(),isHide,0,isSystem);

        adapter.upDateMsg(item);
        mMsgListView.setSelection(adapter.getCount() - 1);
        mMsgDB.saveMsg(mSpUtil.getUserId(), item);// 消息保存数据库
        mEtMsg.setText("");
        // ===发送消息到服务器
        com.pzf.liaotian.bean.Message msgItem = new com.pzf.liaotian.bean.Message(
                MessageItem.MESSAGE_TYPE_TEXT,
                System.currentTimeMillis(), msg, "", 0);
        
        String filePath = chatContext.getExternalFilesDir(null).toString() +"/word/";
        File file = new File(filePath);
        if (!file.exists()) {
			file.mkdirs();
		}
        
        //TODO
        String fileName = item.getDate()+".txt";
        writeTxtToFile(msg, filePath, fileName);
        mSpUtil.setIsCome(false);
//        if (isSystemMessage) {
//        	if (mConnection.isConnected()) {
//        		mConnection.sendTextMessage(msg);
//			}			
//		} else {
        //发送文本
			new SendMsgAsyncTask(mGson.toJson(msgItem), mSpUtil.getUserId(),filePath+fileName)
            .send();
//		}
        
        // ===保存近期的消息

        RecentItem recentItem = new RecentItem(
                MessageItem.MESSAGE_TYPE_TEXT, mSpUtil.getUserId(),
                0, mSpUtil.getNick(), msg, 0,
                System.currentTimeMillis(), 0,mSpUtil.getIsPrivateChat());
        mRecentDB.saveRecent(recentItem);
    }
    
    public void receiveMessageFormServer(String userName,String userID,String fileType,String Path,int voiceLength,int agreement,int isSystemMessage,int isPrivateChat) {

            MessageItem item = null;
            RecentItem recentItem = null;
            if (mSpUtil == null) {
				mSpUtil = PushApplication.getInstance().getSpUtil();
			}
            mSpUtil.setIsCome(true);

            long currentTime = System.currentTimeMillis();
        	int isHide = isHideTimeLabel(currentTime);
        	
    		//图片
            if (fileType.equals(".jpg") || fileType.equals(".png") || fileType.equals("image")) {
                item = new MessageItem(MessageItem.MESSAGE_TYPE_IMG,
                        userName, currentTime,
                        Path,1,true,0,0,isPrivateChat,isHide,0,MessageItem.NOT_SYSTEM_MESSAGE);

                recentItem = new RecentItem(MessageItem.MESSAGE_TYPE_IMG,
                        userID, 1, userName,
                        Path, 0,
                        System.currentTimeMillis(), 0,isPrivateChat);
            }
            else if (fileType.equals(".amr") || fileType.equals(".mp3") || fileType.equals("audio")) {//语音
                item = new MessageItem(MessageItem.MESSAGE_TYPE_RECORD,

                		userName, currentTime,
                		Path, 1, true, 0,
                        voiceLength,isPrivateChat,isHide,0,MessageItem.NOT_SYSTEM_MESSAGE);

                recentItem = new RecentItem(
                        MessageItem.MESSAGE_TYPE_RECORD, userID, 1,
                       userName, Path, 0,
                        System.currentTimeMillis(), voiceLength,isPrivateChat);
             }
             else if ((fileType.equals(".txt") || fileType.equals("text"))  && isSystemMessage == 0 && agreement == 0) {//文本
            	 
            	
            	 //直接将文本内容存到数据库
//            	 String str = ""; 
//            	 try {  
//                     File urlFile = new File(Path);  
//                     InputStreamReader isr = new InputStreamReader(new FileInputStream(urlFile), "gbk");  
//                     BufferedReader br = new BufferedReader(isr);    
//                         
//                     String mimeTypeLine = null ;  
//                     while ((mimeTypeLine = br.readLine()) != null) {  
//                       str = str+mimeTypeLine;  
//                   }  
//                     br.close();
//                     isr.close();
//            	 } catch (Exception e) {  
//            		 e.printStackTrace();  
//            	 }  
            	          	 
                item = new MessageItem(MessageItem.MESSAGE_TYPE_TEXT,

                        userName, currentTime,
                        Path, 1, true, 1,
                        0,isPrivateChat,isHide,0,MessageItem.NOT_SYSTEM_MESSAGE);

                recentItem = new RecentItem(MessageItem.MESSAGE_TYPE_TEXT,
                        userID, 1, userName,
                        Path, 0,
                        System.currentTimeMillis(), 0,isPrivateChat);
            } else if ((fileType.equals(".txt") || fileType.equals("text")) && isSystemMessage == 1) {
            	 item = new MessageItem(MessageItem.MESSAGE_TYPE_TEXT,

                         userName, currentTime,
                         Path, 1, true, 1,
                         0,isPrivateChat,isHide,0,MessageItem.SYSTEM_MESSAGE);
            }
             else if (fileType.contains(".doc") || fileType.contains("file")) {//文档
            	 item = new MessageItem(MessageItem.MESSAGE_TYPE_FILE,

                 		userName, currentTime,
                 		Path, 0, true, 0,
                         voiceLength,isPrivateChat,isHide,0,MessageItem.NOT_SYSTEM_MESSAGE);

                 recentItem = new RecentItem(
                         MessageItem.MESSAGE_TYPE_FILE, userID, 0,
                        userName, Path, 0,
                         System.currentTimeMillis(), voiceLength,isPrivateChat);
            }
             else if (agreement == 1) {//待确认-调解协议书
            	 item = new MessageItem(MessageItem.MESSAGE_TYPE_FILE,

                 		userName, currentTime,
                        "待确认-调解协议书", 0, true, 1,
                         voiceLength,isPrivateChat,isHide,agreement,MessageItem.NOT_SYSTEM_MESSAGE);

                 recentItem = new RecentItem(
                         MessageItem.MESSAGE_TYPE_FILE, userID, 0,
                        userName, Path, 0,
                         System.currentTimeMillis(), voiceLength,isPrivateChat);
            }
            
//
            adapter.upDateMsg(item);// 更新界面
            mMsgDB.saveMsg(userID, item);// 保存数据库
//            mRecentDB.saveRecent(recentItem);

            scrollToBottomListItem();
        }    
    

}
