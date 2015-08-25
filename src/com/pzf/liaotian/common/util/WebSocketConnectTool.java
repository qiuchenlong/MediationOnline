package com.pzf.liaotian.common.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream.PutField;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.TunnelRefusedException;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.bither.util.NativeUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.pzf.liaotian.PublicChatActivity;
import com.pzf.liaotian.UploadUtil;
import com.pzf.liaotian.UploadUtil.MYTask;
import com.pzf.liaotian.adapter.MessageAdapter;
import com.pzf.liaotian.app.PushApplication;
import com.pzf.liaotian.bean.MessageItem;
import com.pzf.liaotian.bean.RecentItem;
import com.pzf.liaotian.db.MessageDB;
import com.pzf.liaotian.db.RecentDB;
import com.pzf.liaotian.xlistview.MsgListView;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;


public class WebSocketConnectTool extends WebSocketConnection {

	private static SharePreferenceUtil mSpUtil;
	static File _file;
	 public static String mUserID;
	 public static String mFileType;
	 public static String mUserName;
	 public static String mFilePath;
	 public static int mVoiceLength;
	 public static Boolean isCome;
	 public static int agreement;
	 
	 public static int isPrivateChat;
	
    /**
     * 内部类实现单例模式
     * 延迟加载，减少内存开销
     * 
     * @author xuzhaohu
     * 
     */
    private static class SingletonHolder {
        private static WebSocketConnectTool websocket = new WebSocketConnectTool();
    }

    /**
     * 私有的构造函数
     */
    private WebSocketConnectTool() {

    }

    public static WebSocketConnectTool getInstance() {
        return SingletonHolder.websocket;
    }

    public void handleConnection(File file) throws JSONException {
    	mSpUtil = PushApplication.getInstance().getSpUtil();
    	
    	final String wsuri = mSpUtil.getServerIP();
    	final File _file = file;
        if (!SingletonHolder.websocket.isConnected()) {
			
	    	  try {
	    		  SingletonHolder.websocket.connect(wsuri, new WebSocketHandler() {
			        	
			            @Override
			            public void onOpen() {
			               Log.d("chat", "Status: Connected to " + wsuri);	
			               PublicChatActivity.sendTextMessage(mSpUtil.getNick()+",进入聊天室",true);
//			               if (_file != null) {
//								sendMessage(_file);	
//							}
			       	    }
			            
			            @Override
						public void onTextMessage(String payload) {
			            	Log.d("chat", "Got echo: " + payload);
			            	
			            	// 解析得到一个Map对象  
			                Map<String, Object> personMap = parseJSONString(payload); 
			            	Log.d("debug",  
			                        "username:" + personMap.get("username") + "\n" + "userid:" + personMap.get("userid") + "\n"  
			                                + "filetype:" + personMap.get("filetype") + "\n" + "isPrivateChat:"  
			                                + personMap.get("isPrivateChat") + "\n" + "voicetime:" + personMap.get("voicetime")
			                                + "\n" + "data:" + personMap.get("data") +"\n" + "isSystemMessage:" + personMap.get("isSystemMessage")
			                                + "\n" + "isadmin:" + personMap.get("isadmin")
			                                + "\n" + "isPrivateChat:" + personMap.get("isPrivateChat"));  
			            	
			            	String decodeString = new String(Base64.decode((String) personMap.get("data"), Base64.NO_WRAP));
			            	Log.d("debug", decodeString); 
			            	
			            	
			            	
//				            String filetype = null;
//				            if (payload.contains(".")) {
//				            	filetype = payload.substring(payload.lastIndexOf("."));
//								Log.d("chat", "Got echo filetype: " + filetype);
//					               
					            UploadUtil.mUserName = (String) personMap.get("username");
					            UploadUtil.mUserID = (String) personMap.get("userid");
					            UploadUtil.mFileType = (String) personMap.get("filetype");
					            UploadUtil.mVoiceLength = (Integer) personMap.get("voicetime");
					            UploadUtil.isSystemMessage = boolTransformInt((Boolean)personMap.get("isSystemMessage"));
					            UploadUtil.agreement = boolTransformInt((Boolean) personMap.get("isagreement"));
					            UploadUtil.isAdmin = boolTransformInt((Boolean) personMap.get("isadmin"));
					            UploadUtil.isPrivateChat = boolTransformInt((Boolean) personMap.get("isPrivateChat"));
					          
//					            
//					            String path = "http://www.baidu.com";
//					               //如果是私聊则不接受消息，因为只有协调员可以看到
					            if (UploadUtil.isPrivateChat == 1) {
									return;
								}
					            //返回的信息处理
					            if (decodeString.contains(UploadUtil.mUserName+",进入聊天室")) {
					            	PublicChatActivity main = new PublicChatActivity();
									main.receiveMessageFormServer(UploadUtil.mUserName, UploadUtil.mUserID,UploadUtil.mFileType, decodeString, UploadUtil.mVoiceLength, UploadUtil.agreement,UploadUtil.isSystemMessage,UploadUtil.isPrivateChat);
								} else if (mSpUtil.getIsAdmin() == 1 && UploadUtil.agreement == 1) {
									//调解协议书
									PublicChatActivity main = new PublicChatActivity();
//									main.receiveMessageFormServer(UploadUtil.mUserName, UploadUtil.mUserID,"", path, 0, UploadUtil.agreement);
								} else {
									 //处理下载链接
						            UploadUtil.handleMessage(decodeString);
								}					            
//				            }
			            }
			            
			            @Override
			            public void onClose(int code, String reason) {
			               Log.d("chat", "Connection lost.");
			               if (_file != null) {
			            	  PublicChatActivity.sendTextMessage("网络连接错误，消息发送失败",true);
						} else {
							 PublicChatActivity.sendTextMessage(mSpUtil.getNick()+",退出聊天室",true);	
						}
			            }
			         });
			      } catch (WebSocketException e) {
			 
			         Log.d("chat", e.toString());
			      }
			} else {
				if (file != null) {
					sendMessage(file);	
				}						     					
			}
    }
    
    public static void sendMessage(File file) throws JSONException {
		
		   InputStream inputStream = null;
		   
			try {
				inputStream = new FileInputStream(file);
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
                    
                
         String fileName = file.toString();
         String suffixName = null;
         if (fileName.contains(".")) {
				suffixName = fileName.substring(fileName.length()-4, fileName.length());
			}
         
         
        //判断要传送文件的格式  		   
		   if (file.toString().contains(".jpg") || file.toString().contains(".png")) {
			   //发送图片 
			   
			   try {
				   inputStream = new FileInputStream(file);
			   } catch (FileNotFoundException e) {
				   e.printStackTrace();
			   }  
			 
			   Bitmap btp = BitmapFactory.decodeStream(inputStream); 
            btp = NativeUtil.compressBitmap(btp, 50,null, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();    
            btp.compress(Bitmap.CompressFormat.JPEG, 40, baos); 
                           
            //需要发送的信息：姓名、用户ID、文件类型、是否是悄悄话、音频长度
            String encodeString = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
            String user_info = getUserJsonInfo(".jpg",encodeString);
            SingletonHolder.websocket.sendTextMessage(user_info);
		   } else {
			   String encodeString = Base64.encodeToString(in_b, Base64.NO_WRAP);
			   String user_info = getUserJsonInfo(suffixName,encodeString);
			   SingletonHolder.websocket.sendTextMessage(user_info);
		   }
		           
        try {
				inputStream.close(); 
        } catch (IOException e) {
				e.printStackTrace();
        }   
	} 
	
	public static String getUserJsonInfo(String filetype,String data) throws JSONException {
			JSONObject json = new JSONObject();   
		 	json.put("username", mSpUtil.getNick());
		 	json.put("userid", mSpUtil.getUserId());
		 	json.put("filetype", filetype);
		 	json.put("isPrivateChat", intTransformBool(mSpUtil.getIsPrivateChat()));
		 	json.put("voicetime", mSpUtil.getVoiceTime());
		 	json.put("isSystemMessage", intTransformBool(mSpUtil.getIsSystemMessage()));
		 	json.put("isagreement", intTransformBool(mSpUtil.getIsAgreement()));
		 	json.put("isadmin", intTransformBool(mSpUtil.getIsAdmin()));
		 	json.put("data", data);
		 	return json.toString();
//		
//		return "{"+ 
//				"\"username\":"+
//				"\"" +  mSpUtil.getNick() + "\"," +
//				"\"userid\":" +
//				"\"" + mSpUtil.getUserId() + "\"," + 
//				"\"filetype\":" + 
//				"\"" + filetype + "\"," +
//				"\"isprivatechat\":" + 
//				mSpUtil.getIsPrivateChat() + "," +
//				"\"voicetime\":" + 
//				 mSpUtil.getVoiceTime() + "," +
//				 "\"data\":" + 
//				 "\"" +data + "\"" +
//			  "}";
	}
	
	public static Boolean intTransformBool(int data){
		if (data == 0) {
			return false;
		} else {
			return true;
		}
	}
	
	public static int boolTransformInt(Boolean data) {
		if (data == true) {
			return 1;
		} else {
			return 0;
		}
	}
	
	/** 
     * JSON解析 
     *  
     * @param JSONString 
     * @return 
     */  
    private Map<String, Object> parseJSONString(String JSONString) {  
        Map<String, Object> resultMap = new HashMap<String, Object>();  
        try {  
            // 直接把JSON字符串转化为一个JSONObject对象  
            JSONObject person = new JSONObject(JSONString);  
           
            resultMap.put("username", person.getString("username"));         
            resultMap.put("userid", person.getString("userid"));              
            resultMap.put("filetype", person.getString("filetype")); 
            resultMap.put("voicetime", person.getInt("voicetime")); 

            resultMap.put("isPrivateChat", person.getBoolean("isPrivateChat")); 
            resultMap.put("isSystemMessage", person.getBoolean("isSystemMessage")); 
            resultMap.put("isagreement", person.getBoolean("isagreement")); 
            resultMap.put("isadmin", person.getBoolean("isadmin"));
            
            
            resultMap.put("data", person.getString("data"));
        } catch (JSONException e) {  
            e.printStackTrace();  
        }  
        return resultMap;  
    }  
    
    
    
}






