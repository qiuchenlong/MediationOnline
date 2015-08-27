package com.pzf.liaotian;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import net.bither.util.NativeUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;

import com.pzf.liaotian.app.PushApplication;
import com.pzf.liaotian.common.util.SharePreferenceUtil;
import com.pzf.liaotian.common.util.WebSocketConnectTool;
import com.pzf.liaotian.db.ConsultMessageDB;


import android.R.integer;
import android.app.Activity;
import android.app.Application;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

import android.util.Log;
public class UploadUtil {
    private static final String TAG = "uploadFile";
    private static final int TIME_OUT = 10*1000;   //超时时间
    private static final String CHARSET = "utf-8"; //设置编码
    
      /**
     * android上传文件到服务器
     * @param file  需要上传的文件
     * @param RequestURL  请求的rul
     * @return  返回响应的内容
     */
    private static SharePreferenceUtil mSpUtil;
    
	 String res ="";
	 static File _file;
	 FileInputStream fis;
//	 public final static WebSocketConnection mConnection = new WebSocketConnection();
	 private static WebSocketConnectTool mConnection = WebSocketConnectTool.getInstance();
	 private static String downPathString;
	 
	 public static String mUserID;
	 public static String mFileType;
	 public static String mUserName;
	 public static String mFilePath;
	 public static int mVoiceLength;
	 public static Boolean isCome;
	 public static int agreement;
	 public static int isSystemMessage;
	 public static int isAdmin;
	 public static int isPrivateChat;
	 public static int isConsult;
	 public static int mRoomID;
	 	
		public static  void handleMessage(String imgPath) {
			new MYTask().execute(imgPath);
		}
	  	 
		public static class MYTask extends AsyncTask<String, Void, byte[]> {
	        /**
	         * 表示任务执行之前的操作
	         */
	        @Override
	        protected void onPreExecute() {
	            // TODO Auto-generated method stub
	            super.onPreExecute();
	        }

	        /**
	         * 主要是完成耗时的操作
	         */
	        @Override
	        protected byte[] doInBackground(String... arg0) {
	            // TODO Auto-generated method stub
	            // 使用网络连接类HttpClient类王城对网络数据的提取
	            HttpClient httpClient = new DefaultHttpClient();
	            HttpGet httpGet = new HttpGet(arg0[0]);
	            byte[] data = null;
	            try {
	                HttpResponse httpResponse = httpClient.execute(httpGet);
	                if (httpResponse.getStatusLine().getStatusCode() == 200) {
	                	HttpEntity httpEntity = httpResponse.getEntity();
	                    data = EntityUtils.toByteArray(httpEntity); 
	                }
	            } catch (Exception e) {
	                // TODO: handle exception
	            }
	            return data;
	        }

	        /**
	         * 主要是更新UI的操作
	         */
	        @Override
	        protected void onPostExecute(byte[] result) {
	            // TODO Auto-generated method stub
	            super.onPostExecute(result);
	            mFilePath = saveFile(result);
	            
	            if (mSpUtil.getIsConsult()) {
					ConsultActivity consult = new ConsultActivity();
					consult.mApplication = PushApplication.getInstance();
					consult.mCSMsgDB = consult.mApplication.getConsultMessageDB();
					consult.receiveMessageFormServer(mUserName, mUserID, mFileType, mFilePath,isConsult);
					
				} else {
					PublicChatActivity main = new PublicChatActivity();
		            main.mApplication = PushApplication.getInstance();
		            main.mMsgDB = main.mApplication.getMessageDB();// 发送数据库
		            main.mRecentDB = main.mApplication.getRecentDB();// 接收消息数据库
		            main.receiveMessageFormServer(mUserName,mUserID,mFileType,mFilePath,mVoiceLength,agreement,isSystemMessage,isPrivateChat);
				}
	            
	        }

	    }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////	 
///////////////////////////////////////////////////////////////////////////////////////////////////////////////	 
	
	 
	    /** 保存方法 */
	    public static String saveFile(byte[] data) {
	     Log.e(TAG, "保存文件");
	     
	     String type = "";
	     String subPath = "";
	     if (mFileType.equals(".jpg") || mFileType.equals(".png")) {
			subPath = "/photo";
		} else if(mFileType.equals(".amr")){
			subPath = "/voice";
		} else if (mFileType.contains(".txt")) {
			subPath = "/word";
			mFileType = ".txt";
		} else if (mFileType.contains(".doc")) {
			subPath = "/world";
			mFileType = ".doc";
		} else {
			subPath = "/other";
		}
	     
	     String fileName = "/" + mUserID +System.currentTimeMillis() + mFileType;
	     String fullPath = null;
	     if (mSpUtil.getIsConsult()) {
	    	  fullPath = ConsultActivity.chatContext.getExternalFilesDir(null).getPath() + subPath;
		} else {
			 fullPath = PublicChatActivity.chatContext.getExternalFilesDir(null).getPath() + subPath;
		}
	     
	     File f = new File(fullPath);
	     if (!f.exists()) {
	      f.mkdirs();
	     } 
	     f = new File(fullPath,fileName);
	     if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	     
	     try {
	    	 
	    	 //TODO 如果数据为空要重新发送一次消息
	      FileOutputStream out = new FileOutputStream(f);
	      
	      if (data == null) {
//	    	  uploadFile(_file,null);
	    	  return null;
		}
	      
	      if (mFileType.equals(".jpg") || mFileType.equals(".png")) {
	    	  Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
		    bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
		} else {
			  out.write(data);
		}
	        
	      out.flush();
	      out.close();
	      Log.i(TAG, "已经保存");
	     } catch (FileNotFoundException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	     } catch (IOException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	     }
	     return fullPath+fileName;
	    }
	
    public static String uploadFile(File file,String RequestURL)
    {
    	_file = file;
    	mSpUtil = PushApplication.getInstance().getSpUtil();
    	  
		final String wsuri = mSpUtil.getServerIP();
		try {
			mConnection.getInstance().handleConnection(_file,null,null);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";

    }
}