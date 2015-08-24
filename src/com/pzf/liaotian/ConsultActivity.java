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
import com.pzf.liaotian.adapter.ConsultMessageAdapter;
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
import com.pzf.liaotian.db.ConsultMessageDB;
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



public class ConsultActivity extends Activity implements OnClickListener,
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
    public static ConsultMessageAdapter adapter;// 发送消息展示的adapter
    public static MsgListView mMsgListView;// 展示消息的
    public static ConsultMessageDB mCSMsgDB;// 保存消息的数据库
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
    public static TextView mHomeNotice;//公告
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
    public static ConsultActivity chatContext;
    private Boolean recodePermission;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setTheme(R.style.AppTheme);
        setContentView(R.layout.zf_consult_view);
        mParams = getWindow().getAttributes();
        chatContext = this;

        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mSpUtil = PushApplication.getInstance().getSpUtil();

        MSGPAGERNUM = 0;

        initView();
//
        mApplication.getNotificationManager().cancel(
                PushMessageReceiver.NOTIFY_ID);
        PushMessageReceiver.mNewNum = 0;
//
//        mUserDB = mApplication.getUserDB();
//        
        recodePermission = mSpUtil.getRecordPermission();
//
        initUserInfo();
//        
//        mConnection.handleConnection(null);
    
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
    	mSpUtil.setUserId(intent.getStringExtra("USER_ID"));
    	mSpUtil.setNick(intent.getStringExtra("USER_NAME"));
    	mSpUtil.setIsAdmin(intent.getIntExtra("IS_ADMIN", 0));
    	
    	int isprivatechat = intent.getIntExtra("IS_PRIVATE_CHAT", 0);
    	mSpUtil.setIsPrivateChat(0);
    	Log.v("chat", "isprivatechat = " + isprivatechat);
    	
    }

   
    private void initView() {
     
//     // 消息
        mApplication = PushApplication.getInstance();
        mCSMsgDB = mApplication.getConsultMessageDB();// 发送数据库
//        mRecentDB = mApplication.getRecentDB();// 接收消息数据库
//        mGson = mApplication.getGson();
//
        adapter = new ConsultMessageAdapter(this, initMsgData());
        mMsgListView = (MsgListView) findViewById(R.id.msg_listView);
////        
//        // 触摸ListView隐藏表情和输入法
        mMsgListView.setOnTouchListener(this);
        mMsgListView.setPullLoadEnable(false);
        mMsgListView.setXListViewListener(this);
        mMsgListView.setAdapter(adapter);
        mMsgListView.setSelection(adapter.getCount() - 1);
        mEtMsg = (EditText) findViewById(R.id.msg_et);
//        mEtMsgOnKeyListener();
    	
    	 mBtnSend = (Button) findViewById(R.id.send_btn);
         mBtnSend.setClickable(true);
         mBtnSend.setEnabled(true);
         mBtnSend.setOnClickListener(this);
      
        //标题
        titleTextView = (TextView)findViewById(R.id.tv_chat_title);
        titleTextView.setText("调解咨询");
                
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
        
        //申诉点
        shensuButton = (Button)findViewById(R.id.shensu_button);
        shensuButton.setVisibility(View.GONE);
        
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
//        mEtMsg.addTextChangedListener(new TextWatcher() {
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before,
//                    int count) {
//            }
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count,
//                    int after) {
//
//            }

//            @Override
//            public void afterTextChanged(Editable s) {
//                if (s.length() > 0) {
//                    mBtnSend.setEnabled(true);
//                    mIbMsgBtn.setVisibility(View.GONE);
//                    mBtnSend.setVisibility(View.VISIBLE);
//                } else {
//                    mBtnSend.setEnabled(false);
//                    mIbMsgBtn.setVisibility(View.VISIBLE);
//                    mBtnSend.setVisibility(View.GONE);
//                }
//            }
//        });

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

    public static ConsultMessageAdapter getMessageAdapter() {
        return adapter;
    }

    /**
     * 加载消息历史，从数据库中读出
     */
    private List<MessageItem> initMsgData() {
        List<MessageItem> list = mCSMsgDB
                .getMsg(mSpUtil.getUserId(), MSGPAGERNUM);
        List<MessageItem> msgList = new ArrayList<MessageItem>();// 消息对象数组
              
        if (list.size() > 0) {
        	
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
        	String str = "您好！我是海沧司法局的调解小秘书，有事您找我？我将在24小时内回复您。";
        	MessageItem  item = new MessageItem(MessageItem.MESSAGE_TYPE_TEXT,
                    "小秘书", System.currentTimeMillis(),
                    str, 1, true, 1,
                    0,mSpUtil.getIsPrivateChat(),0,0);
        	msgList.add(item);
        	mCSMsgDB.saveMsg(mSpUtil.getUserId(), item);// 保存数据库
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
        }
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
    	if (interval > 1000*60) {
    		Log.v("chat_time", "interval="+ interval);
			Log.v("chat_time", "interval="+ new java.util.Date(interval));
			mSpUtil.setSystemTime(newtime);
			return 0;
		} else {
			return 1;
		}
    	
    
    }
    
    public static void sendTextMessage(String message,Boolean isSystemMessage) {
    	String msg = message;

    	long currentTime = System.currentTimeMillis();
    	int isHide = isHideTimeLabel(currentTime);
    	
        MessageItem item = new MessageItem(
                MessageItem.MESSAGE_TYPE_TEXT, mSpUtil.getNick(),
                currentTime, msg, mSpUtil.getHeadIcon(),
                false, 0, 0,mSpUtil.getIsPrivateChat(),isHide,0);

        adapter.upDateMsg(item);
        mMsgListView.setSelection(adapter.getCount() - 1);
        mCSMsgDB.saveMsg(mSpUtil.getUserId(), item);// 消息保存数据库
        mEtMsg.setText("");
//        // ===发送消息到服务器
//        com.pzf.liaotian.bean.Message msgItem = new com.pzf.liaotian.bean.Message(
//                MessageItem.MESSAGE_TYPE_TEXT,
//                System.currentTimeMillis(), msg, "", 0);
//        
//        String filePath = chatContext.getExternalFilesDir(null).toString() +"/word/";
//        File file = new File(filePath);
//        if (!file.exists()) {
//			file.mkdirs();
//		}
//        
//        //TODO
//        String fileName = item.getDate()+".txt";
//        writeTxtToFile(msg, filePath, fileName);
//        mSpUtil.setIsCome(false);
//        if (isSystemMessage) {
//        	if (mConnection.isConnected()) {
//        		mConnection.sendTextMessage(msg);
//			}			
//		} else {
//			new SendMsgAsyncTask(mGson.toJson(msgItem), mSpUtil.getUserId(),filePath+fileName)
//            .send();
//		}
//        
//        // ===保存近期的消息
//
//        RecentItem recentItem = new RecentItem(
//                MessageItem.MESSAGE_TYPE_TEXT, mSpUtil.getUserId(),
//                0, mSpUtil.getNick(), msg, 0,
//                System.currentTimeMillis(), 0,mSpUtil.getIsPrivateChat());
//        mRecentDB.saveRecent(recentItem);
    }
    
    public void receiveMessageFormServer(String userName,String userID,String fileType,String Path,int voiceLength,int agreement) {

//            String userId = msgItem.getUser_id();
//            if (!userId.equals(mSpUtil.getUserId()))// 如果不是当前正在聊天对象的消息，不处理
//                return;
//
//            int headId = msgItem.getHead_id();
//            /*
//             * try { headId = Integer
//             * .parseInt(JsonUtil.getFromUserHead(message)); } catch
//             * (Exception e) { L.e("head is not integer  " + e); }
//             */
//            // ===接收的额数据，如果是record语音的话，用播放展示
            MessageItem item = null;
            RecentItem recentItem = null;
            if (mSpUtil == null) {
				mSpUtil = PushApplication.getInstance().getSpUtil();
			}
            mSpUtil.setIsCome(true);

            long currentTime = System.currentTimeMillis();
        	int isHide = isHideTimeLabel(currentTime);
        	
    		//图片
            if (fileType.equals(".jpg") || fileType.equals(".png")) {
                item = new MessageItem(MessageItem.MESSAGE_TYPE_IMG,
                        userName, currentTime,
                        Path,1,true,0,0,mSpUtil.getIsPrivateChat(),isHide,0);

                recentItem = new RecentItem(MessageItem.MESSAGE_TYPE_IMG,
                        userID, 1, userName,
                        Path, 0,
                        System.currentTimeMillis(), 0,mSpUtil.getIsPrivateChat());
            }
            else if (fileType.equals(".amr") || fileType.equals(".mp3")) {//语音
                item = new MessageItem(MessageItem.MESSAGE_TYPE_RECORD,

                		userName, currentTime,
                		Path, 1, true, 0,
                        voiceLength,mSpUtil.getIsPrivateChat(),isHide,0);

                recentItem = new RecentItem(
                        MessageItem.MESSAGE_TYPE_RECORD, userID, 1,
                       userName, Path, 0,
                        System.currentTimeMillis(), voiceLength,mSpUtil.getIsPrivateChat());
             }
             else if (fileType.equals(".txt")) {//文本
            	 //直接将文本内容存到数据库
            	 String str = ""; 
            	 try {  
                     File urlFile = new File(Path);  
                     InputStreamReader isr = new InputStreamReader(new FileInputStream(urlFile), "gbk");  
                     BufferedReader br = new BufferedReader(isr);    
                         
                     String mimeTypeLine = null ;  
                     while ((mimeTypeLine = br.readLine()) != null) {  
                       str = str+mimeTypeLine;  
                   }  
                     br.close();
                     isr.close();
            	 } catch (Exception e) {  
            		 e.printStackTrace();  
            	 }  
            	          	 
                item = new MessageItem(MessageItem.MESSAGE_TYPE_TEXT,

                        userName, currentTime,
                        str, 1, true, 1,
                        0,mSpUtil.getIsPrivateChat(),isHide,0);

                recentItem = new RecentItem(MessageItem.MESSAGE_TYPE_TEXT,
                        userID, 1, userName,
                        str, 0,
                        System.currentTimeMillis(), 0,mSpUtil.getIsPrivateChat());
            }
             else if (fileType.contains(".doc")) {//文档
            	 item = new MessageItem(MessageItem.MESSAGE_TYPE_FILE,

                 		userName, currentTime,
                 		Path, 0, true, 0,
                         voiceLength,mSpUtil.getIsPrivateChat(),isHide,0);

                 recentItem = new RecentItem(
                         MessageItem.MESSAGE_TYPE_FILE, userID, 0,
                        userName, Path, 0,
                         System.currentTimeMillis(), voiceLength,mSpUtil.getIsPrivateChat());
            }
             else if (agreement == 1) {//待确认-调解协议书
            	 item = new MessageItem(MessageItem.MESSAGE_TYPE_FILE,

                 		userName, currentTime,
                        Path, 0, true, 1,
                         voiceLength,mSpUtil.getIsPrivateChat(),isHide,agreement);

                 recentItem = new RecentItem(
                         MessageItem.MESSAGE_TYPE_FILE, userID, 0,
                        userName, Path, 0,
                         System.currentTimeMillis(), voiceLength,mSpUtil.getIsPrivateChat());
            }
            
//
            adapter.upDateMsg(item);// 更新界面
            mCSMsgDB.saveMsg(userID, item);// 保存数据库
            mRecentDB.saveRecent(recentItem);

            scrollToBottomListItem();
        }    
    

}
