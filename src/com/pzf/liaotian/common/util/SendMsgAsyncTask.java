package com.pzf.liaotian.common.util;

import java.io.File;
import java.util.List;

import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;

import com.pzf.liaotian.R;
import com.pzf.liaotian.UploadUtil;
import com.pzf.liaotian.app.PushApplication;
import com.pzf.liaotian.baidupush.server.BaiduPush;

/**
 * @desc:发送消息到服务器
 * @author:pangzf
 * @blog:http://blog.csdn.net/pangzaifei/article/details/43023625
 * @github:https://github.com/pangzaifei/zfIMDemo
 * @qq:1660380990
 * @email:pzfpang451@163.com  
 */
public class SendMsgAsyncTask {
    private BaiduPush mBaiduPush;
    private String mMessage;
    private Handler mHandler;
    private MyAsyncTask mTask;
    private String mUserId;
    private String mFile;
    private OnSendScuessListener mListener;
    private List<String> mMessageList;

    public interface OnSendScuessListener {
        void sendScuess();
    }

    public void setOnSendScuessListener(OnSendScuessListener listener) {
        this.mListener = listener;
    }

    Runnable reSend = new Runnable() {

        @Override
        public void run() {
            L.i("resend msg...");
            send();// 重发
        }
    };

    public SendMsgAsyncTask(String jsonMsg, String useId,String filePath) {
//        mBaiduPush = PushApplication.getInstance().getBaiduPush();
        mMessage = jsonMsg;
        mUserId = useId;
        mFile = filePath;
        mHandler = new Handler();
    }

    public SendMsgAsyncTask(List<String> messageList, String useId) {
        mBaiduPush = PushApplication.getInstance().getBaiduPush();
        mMessageList = messageList;
        mUserId = useId;
        mHandler = new Handler();
    }

    // 发送
    public void send() {
        if (NetUtil.isNetConnected(PushApplication.getInstance())) {// 如果网络可用
            mTask = new MyAsyncTask();
            mTask.execute();
        } else {
            T.showLong(PushApplication.getInstance(), R.string.net_error_tip);
        }
    }

    // 停止
    public void stop() {
        if (mTask != null)
            mTask.cancel(true);
    }

    class MyAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... message) {
        	
        	return UploadUtil.uploadFile(new File(mFile), "http://10.0.2.2:8484");

        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            L.i("send msg result:" + result);
        }
    }
}
