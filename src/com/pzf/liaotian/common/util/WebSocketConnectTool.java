package com.pzf.liaotian.common.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream.PutField;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pzf.liaotian.MainWebViewActivity;
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
import com.zztj.chat.bean.ClientList;
import com.zztj.chat.bean.EnterChatRoom;
import com.zztj.chat.bean.JsonMessageStruct;
import com.zztj.chat.bean.EnterChatRoom.Base_Info;
import com.zztj.chat.bean.MessageInfo;

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

    public void handleConnection(File file,final String action,final String json,final Context mContent) throws JSONException {
    	mSpUtil = PushApplication.getInstance().getSpUtil();
    	
    	final String wsuri = mSpUtil.getServerIP();
    	final File _file = file;
        if (!SingletonHolder.websocket.isConnected()) {
			
	    	  try {
	    		  SingletonHolder.websocket.connect(wsuri, new WebSocketHandler() {
			        	
			            @Override
			            public void onOpen() {
			               Log.d("chat", "Status: Connected to " + wsuri);	
			               
			               if (action != null && action.equals("enter")) {
							SingletonHolder.websocket.sendTextMessage(json);
						} else if (_file != null) {
							try {
								sendMessage(_file);
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}	
						}
			               
			               //如果不是进入调解咨询页面 则不用发送这句话
//			               if (!mSpUtil.getIsConsult()) {
//				               PublicChatActivity.sendTextMessage(mSpUtil.getNick()+",进入聊天室",true);  
//			               }
//			               if (_file != null) {
//								sendMessage(_file);	
//							}
			       	    }
			            
			            @Override
						public void onTextMessage(String payload) {
			            	Log.d("chat", "Got echo: " + payload);
			            	
			              Gson gson = new Gson();
			              java.lang.reflect.Type type = new TypeToken<JsonMessageStruct>(){}.getType();
			              JsonMessageStruct jsonBean = gson.fromJson(payload, type);
			              

			              
			              if (jsonBean.type.equals("enter")) {
							//进入聊天室
			            	  Intent intent = new Intent(mContent,PublicChatActivity.class);
			            	  
				              
				              String userid = "";
				              for (int i = 0; i <= 1 ; i++) {
				            	  //如果是管理员的话 记住管理员的id
				            	  if (jsonBean.message_info.client_list.get(i).is_adjuster == 1) {
									mSpUtil.setAdminID(jsonBean.message_info.client_list.get(i).userid);
								} else {
									mSpUtil.setAdminID(0);
								}
				            	  
				            	  //记住其他人的userid
				            	  if (userid.equals("")) {
									userid = String.valueOf(jsonBean.message_info.client_list.get(i).userid);
								} else {
									userid = userid + "," + String.valueOf(jsonBean.message_info.client_list.get(i).userid);
								}
//				            	
							}
				              mSpUtil.setUserIDs(userid);
				       
				              			intent.putExtra("USER_NAME",jsonBean.base_info.from_username);
						        		intent.putExtra("USER_ID", jsonBean.base_info.from_client_id);
						        		intent.putExtra("IS_PRIVATE_CHAT", 0);
						        		intent.putExtra("IS_ADMIN", 0);
						        		intent.putExtra("CHAT_ROOM_ID", jsonBean.base_info.room_id);
						        		intent.putExtra("CONTENT", jsonBean.message_info.content);
						        		mContent.startActivity(intent);
						} else if (jsonBean.type.equals("say") && jsonBean.message_info.msg_type.equals("text")) {
							
							if ( mSpUtil.getRoomID() != jsonBean.base_info.room_id) {
								return ;
							}
							
							 UploadUtil.mUserName = jsonBean.base_info.from_username;
					            UploadUtil.mUserID = String.valueOf(jsonBean.base_info.from_client_id);
					            UploadUtil.mFileType = jsonBean.message_info.msg_type;
					            UploadUtil.mVoiceLength = 0;
					            UploadUtil.agreement = 0;
					            UploadUtil.isSystemMessage = 0;
					            UploadUtil.isPrivateChat = jsonBean.message_info.is_secret;
					            UploadUtil.mRoomID = jsonBean.base_info.room_id;
					            PublicChatActivity main = new PublicChatActivity();
								main.receiveMessageFormServer(UploadUtil.mUserName, UploadUtil.mUserID,UploadUtil.mFileType, jsonBean.message_info.content, UploadUtil.mVoiceLength, UploadUtil.agreement,UploadUtil.isSystemMessage,UploadUtil.isPrivateChat);
			
						} else if (jsonBean.type.equals("say")&&jsonBean.message_info.msg_type.equals("image")) {
							if (mSpUtil.getRoomID() != jsonBean.base_info.room_id) {
								return ;
							}
							
							UploadUtil.mUserName = jsonBean.base_info.from_username;
				            UploadUtil.mUserID = String.valueOf(jsonBean.base_info.from_client_id);
				            UploadUtil.mFileType = jsonBean.message_info.msg_type;
				            UploadUtil.mVoiceLength = 0;
				            UploadUtil.agreement = 0;
				            UploadUtil.isSystemMessage = 0;
				            UploadUtil.isPrivateChat = jsonBean.message_info.is_secret;
				            UploadUtil.mRoomID = jsonBean.base_info.room_id;
				            
				           
//				            String decodeString = new String(Base64.decode((String) jsonBean.message_info.src_url, Base64.NO_WRAP));
				            UploadUtil.handleMessage(jsonBean.message_info.src_url);
						} else if (jsonBean.type.equals("say")&&jsonBean.message_info.msg_type.equals("audio")){
							if (mSpUtil.getRoomID() != jsonBean.base_info.room_id) {
								return ;
							}
							
							UploadUtil.mUserName = jsonBean.base_info.from_username;
				            UploadUtil.mUserID = String.valueOf(jsonBean.base_info.from_client_id);
				            UploadUtil.mFileType = jsonBean.message_info.msg_type;
				            String time = jsonBean.message_info.content;
				            String newStr = time.substring(0,time.indexOf("秒"));
				            UploadUtil.mVoiceLength = Integer.parseInt(newStr);
				            UploadUtil.agreement = 0;
				            UploadUtil.isSystemMessage = 0;
				            UploadUtil.isPrivateChat = jsonBean.message_info.is_secret;
				            UploadUtil.mRoomID = jsonBean.base_info.room_id;
//				            String decodeString = new String(Base64.decode((String) jsonBean.message_info.src_url, Base64.NO_WRAP));
				            UploadUtil.handleMessage(jsonBean.message_info.src_url);
						} else if (jsonBean.type.equals("say")&&jsonBean.base_info.is_secret == 1){
							if (mSpUtil.getRoomID() != jsonBean.base_info.room_id) {
								return ;
							}
							
							UploadUtil.mUserName = jsonBean.base_info.from_username;
				            UploadUtil.mUserID = String.valueOf(jsonBean.base_info.from_client_id);
				            UploadUtil.mFileType = jsonBean.message_info.msg_type;
				           
				            UploadUtil.mVoiceLength = 0;
				            UploadUtil.agreement = 0;
				            UploadUtil.isSystemMessage = 0;
				            UploadUtil.isPrivateChat = jsonBean.message_info.is_secret;
				            UploadUtil.mRoomID = jsonBean.base_info.room_id;
				            PublicChatActivity main = new PublicChatActivity();
							main.receiveMessageFormServer(UploadUtil.mUserName, UploadUtil.mUserID,UploadUtil.mFileType, jsonBean.message_info.content, UploadUtil.mVoiceLength, UploadUtil.agreement,UploadUtil.isSystemMessage,UploadUtil.isPrivateChat);
		
						} else if (jsonBean.type.equals("say")&&jsonBean.message_info.msg_type.equals("file")){
							if ( mSpUtil.getRoomID() != jsonBean.base_info.room_id) {
								return ;
							}
							
							UploadUtil.mUserName = jsonBean.base_info.from_username;
				            UploadUtil.mUserID = String.valueOf(jsonBean.base_info.from_client_id);
				            UploadUtil.mFileType = jsonBean.message_info.msg_type;
				           
				            UploadUtil.mVoiceLength = 0;
				            UploadUtil.agreement = 0;
				            UploadUtil.isSystemMessage = 1;
				            UploadUtil.isPrivateChat = jsonBean.message_info.is_secret;
				            UploadUtil.mRoomID = jsonBean.base_info.room_id;
//				            String decodeString = new String(Base64.decode((String) jsonBean.message_info.src_url, Base64.NO_WRAP));
				            UploadUtil.handleMessage(jsonBean.message_info.src_url);
						} else if (mSpUtil.getAdminID() != 0 && UploadUtil.agreement == 1) {
							//调解协议书
							PublicChatActivity main = new PublicChatActivity();
							main.receiveMessageFormServer(UploadUtil.mUserName, UploadUtil.mUserID,UploadUtil.mFileType, "/Home/AdjOl/adjagreement.html?room_id="+mSpUtil.getRoomID(), UploadUtil.mVoiceLength, UploadUtil.agreement,UploadUtil.isSystemMessage,UploadUtil.isPrivateChat);
						}
			              
			            				            
			            }
			            
			            @Override
			            public void onClose(int code, String reason) {
			               Log.d("chat", "Connection lost.");
//			               if (_file != null) {
//			            	  PublicChatActivity.sendTextMessage("网络连接错误，消息发送失败",true);
//						} else {
//							 PublicChatActivity.sendTextMessage(mSpUtil.getNick()+",退出聊天室",true);	
//						}
			            }
			         });
			      } catch (WebSocketException e) {
			 
			         Log.d("chat", e.toString());
			      }
			} else {
				if (action != null && action.equals("enter")) {
					SingletonHolder.websocket.sendTextMessage(json);
				} else if (file != null) {
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
                    
                
         String filePath = file.toString();
         String suffixName = null;
         if (filePath.contains(".")) {
				suffixName = filePath.substring(filePath.length()-4, filePath.length());
			}
         
         String fileName = getFileName(filePath);
         
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
            String user_info = getUserJsonInfo(".jpg",encodeString,fileName);
            SingletonHolder.websocket.sendTextMessage(user_info);
		   } else {
			   String encodeString = Base64.encodeToString(in_b, Base64.NO_WRAP);
			   String user_info = getUserJsonInfo(suffixName,encodeString,fileName);
			   if (mSpUtil.getIsConsult()) {
				user_info = getConsultJsonInfo(suffixName, encodeString);
			}
			   SingletonHolder.websocket.sendTextMessage(user_info);
		   }
		           
        try {
				inputStream.close(); 
        } catch (IOException e) {
				e.printStackTrace();
        }   
	} 
    
    public static String getFileName(String pathandname){  
        
        int start=pathandname.lastIndexOf("/");  
        int end=pathandname.lastIndexOf(".");  
        if(start!=-1 && end!=-1){  
            return pathandname.substring(start+1,end);    
        }else{  
            return null;  
        }  
          
    }
    
    public static String getConsultJsonInfo(String filetype,String data) throws JSONException {
		JSONObject json = new JSONObject();   
	 	json.put("username", mSpUtil.getNick());
	 	json.put("userid", mSpUtil.getUserId());
	 	json.put("filetype", filetype);
	 	json.put("isconsult", mSpUtil.getIsConsult());
	 	json.put("data", data);
	 	return json.toString();
    }
	
	public static String getUserJsonInfo(String filetype,String data,String filename) throws JSONException {
		
		//组织要提交的json信息
    	JsonMessageStruct send = new JsonMessageStruct();
    	send.init();
    	
    	send.type = "say";
    	send.base_info.room_id = mSpUtil.getRoomID();
    	send.base_info.from_client_id = Integer.parseInt(mSpUtil.getUserId());
    	send.base_info.from_username = mSpUtil.getNick();
    	send.base_info.to_client_id = "ALL";
    	send.base_info.is_secret = 0;
    	
    	send.message_info.avatar = " ";
    	send.message_info.username = mSpUtil.getNick();
    	send.message_info.content = data;
    	
    	long time=System.currentTimeMillis();//long now = android.os.SystemClock.uptimeMillis();  
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
        Date d1=new Date(time);  
        String send_time=format.format(d1);  
        
    	send.message_info.time = send_time;
    	
    	if (filetype.equals(".jpg") || filetype.equals("png")) {
    		send.message_info.msg_type = "image";
    		send.message_info.filename = filename;
		} else if (filetype.equals(".txt") && mSpUtil.getIsPrivateChat() == 0) {
			send.message_info.msg_type = "text";
		} else if (filetype.equals(".mp3")) {
			send.message_info.msg_type = "audio";
			send.message_info.filename = mSpUtil.getVoiceTime() + "秒";
			
		} else if (filetype.equals(".doc")) {
			send.message_info.msg_type = "file";
			send.message_info.filename = filename+".doc";
		} else if (filetype.equals(".txt") && mSpUtil.getIsPrivateChat() == 1) { //悄悄话
			send.base_info.is_secret = 1;
			send.base_info.to_client_id = String.valueOf(mSpUtil.getAdminID());
			send.message_info.msg_type = "text";
		}
    	
        Gson gson = new Gson();
        String jsonStr = gson.toJson(send);
        Log.v("=============", jsonStr);
       
		 	return jsonStr;
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
	
	

    
    
}






