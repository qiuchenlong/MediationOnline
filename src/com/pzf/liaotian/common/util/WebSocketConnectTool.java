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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pzf.liaotian.MainWebViewActivity;
import com.pzf.liaotian.ChatRoomActivity;
import com.pzf.liaotian.activity.UploadUtil;
import com.pzf.liaotian.activity.UploadUtil.MYTask;
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
	 private static boolean isConnect;
	 
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

    public void handleConnection(File file,final String action,final String json,final Context mContext) throws JSONException {
    	mSpUtil = PushApplication.getInstance().getSpUtil();
    	
    	final String wsuri = mSpUtil.getServerIP();
    	final File _file = file;
        if (!SingletonHolder.websocket.isConnected()) {
        	 
	    	  try {
	    		  SingletonHolder.websocket.connect(wsuri, new WebSocketHandler() {
			        	
			            @Override
			            public void onOpen() {
			               Log.d("chat", "Status: Connected to " + wsuri);	
			               
			               Log.d("chat", "json: " + json);	
			               if (action != null && action.equals("enter")) {
							SingletonHolder.websocket.sendTextMessage(json);
						} else if (_file != null) {
							try {
								sendMessage(_file);
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}	
						} else if (_file == null) {
							 Toast.makeText(mContext, "与服务器连接正常", Toast.LENGTH_SHORT).show();	
						}
							
						isConnect = true;
			               
			       	    }
			            
			            @Override
						public void onTextMessage(String payload) {
			            	Log.d("chat", "Got echo: " + payload);
			            	
			              Gson gson = new Gson();
			              java.lang.reflect.Type type = new TypeToken<JsonMessageStruct>(){}.getType();
			              JsonMessageStruct jsonBean = gson.fromJson(payload, type);

			              if (jsonBean.type.equals("ping")) {
							SingletonHolder.websocket.sendTextMessage("{\"type\":\"pong\"}");
							return ;
						}
			                            
			              if (jsonBean.type.equals("enter")) {
							//进入聊天室
			            	  Intent intent = new Intent(mContext,ChatRoomActivity.class);
			            	  
				              Log.v("chat", "size="+jsonBean.message_info.client_list.size());
				              String userid = "";
				              for (int i = 0; i < jsonBean.message_info.client_list.size(); i++) {
				            	  //如果是管理员的话 记住管理员的信息
				            	  ClientList  list = jsonBean.message_info.client_list.get(i);
				            	  if ( list != null)  {
				            		  if (jsonBean.message_info.client_list.get(i).is_adjuster == 1) {
				            			  mSpUtil.setAdminID(jsonBean.message_info.client_list.get(i).userid);
				            			  mSpUtil.setAdminRealName(jsonBean.message_info.client_list.get(i).realname);
				            			  mSpUtil.setAdminUserName(jsonBean.message_info.client_list.get(i).username);
				            			  mSpUtil.setAdminClientID(jsonBean.message_info.client_list.get(i).client_id);
									}
									
								} else {
									mSpUtil.setAdminID(0);
									mSpUtil.setAdminRealName("");
									mSpUtil.setAdminUserName("");
									mSpUtil.setAdminClientID(0);
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
				       
				              String selfName = mSpUtil.getNick();
				              //这个条件不行得重新写
				              if (!selfName.equals("") && !selfName.equals(jsonBean.base_info.from_username)) {
				            	  ChatRoomActivity chatActivity = new ChatRoomActivity();
				            	  String content = jsonBean.message_info.content;
									chatActivity.loginContent = content;
									chatActivity.userLogin();
							} else {
								 intent.putExtra("USER_NAME",jsonBean.base_info.from_username);
							      intent.putExtra("USER_ID", jsonBean.base_info.from_client_id);
							      intent.putExtra("IS_PRIVATE_CHAT", 0);
							      intent.putExtra("IS_ADMIN", 0);
							      intent.putExtra("CHAT_ROOM_ID", jsonBean.base_info.room_id);
							      intent.putExtra("CONTENT", jsonBean.message_info.content);
							      intent.putExtra("ENTER_ROOM", "enter");
							      mContext.startActivity(intent);
							}
				             
						      
						} else if (jsonBean.type.equals("logout")){
							ChatRoomActivity chatActivity = new ChatRoomActivity();
							chatActivity.laoutContent = jsonBean.message_info.content;
							chatActivity.userLaout();
							
						} else if (jsonBean.type.equals("say") && jsonBean.base_info.is_secret == 0 && jsonBean.message_info.msg_type.equals("text") ) {
							
						    //待改
				              String username = jsonBean.base_info.from_username;
				              if (username.equals(mSpUtil.getNick())) {
								return;
							}
							
							 UploadUtil.mUserName = jsonBean.base_info.from_username;
					            UploadUtil.mUserID = String.valueOf(jsonBean.base_info.from_client_id);
					            UploadUtil.mFileType = jsonBean.message_info.msg_type;
					            UploadUtil.mVoiceLength = 0;
					            UploadUtil.agreement = 0;
					            UploadUtil.isSystemMessage = 0;
					            UploadUtil.isPrivateChat = jsonBean.message_info.is_secret;
					            UploadUtil.mRoomID = jsonBean.base_info.room_id;
					            ChatRoomActivity main = new ChatRoomActivity();
								main.receiveMessageFormServer(UploadUtil.mUserName, UploadUtil.mUserID,UploadUtil.mFileType, jsonBean.message_info.content, UploadUtil.mVoiceLength, UploadUtil.agreement,UploadUtil.isSystemMessage,UploadUtil.isPrivateChat);
			
						} else if (jsonBean.type.equals("say")&&jsonBean.message_info.msg_type.equals("image") && jsonBean.base_info.is_secret == 0) {
							
							Log.v("chat", "roomt="+ mSpUtil.getRoomID());
						    //待改
				              String username = jsonBean.base_info.from_username;
				              if (username.equals(mSpUtil.getNick())) {
								return;
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
						} else if (jsonBean.type.equals("say")&&jsonBean.message_info.msg_type.equals("audio") && jsonBean.base_info.is_secret == 0){
						    //待改
				              String username = jsonBean.base_info.from_username;
				              if (username.equals(mSpUtil.getNick())) {
								return;
							}
							
							UploadUtil.mUserName = jsonBean.base_info.from_username;
				            UploadUtil.mUserID = String.valueOf(jsonBean.base_info.from_client_id);
				            UploadUtil.mFileType = jsonBean.message_info.msg_type;
				            String time = jsonBean.message_info.content;
//				            String newStr = time.substring(0,time.indexOf("＂"));
				            String newStr = getNumbers(time);
				            UploadUtil.mVoiceLength = Integer.parseInt(newStr);
				            UploadUtil.agreement = 0;
				            UploadUtil.isSystemMessage = 0;
				            UploadUtil.isPrivateChat = jsonBean.message_info.is_secret;
				            UploadUtil.mRoomID = jsonBean.base_info.room_id;
//				            String decodeString = new String(Base64.decode((String) jsonBean.message_info.src_url, Base64.NO_WRAP));
				            UploadUtil.handleMessage(jsonBean.message_info.src_url);
						} else if (jsonBean.type.equals("say") && jsonBean.base_info.is_secret == 1 && jsonBean.base_info.to_client_id.equals(mSpUtil.getUserId()) && UploadUtil.agreement == 0){
							//悄悄话
						    //待改
				              String username = jsonBean.base_info.from_username;
				              if (username.equals(mSpUtil.getNick())) {
								return;
							}
							
							UploadUtil.mUserName = jsonBean.base_info.from_username;
				            UploadUtil.mUserID = String.valueOf(jsonBean.base_info.from_client_id);
				            UploadUtil.mFileType = jsonBean.message_info.msg_type;
				           
				            UploadUtil.mVoiceLength = 0;
				            UploadUtil.agreement = 0;
				            UploadUtil.isSystemMessage = 0;
				            UploadUtil.isPrivateChat = jsonBean.message_info.is_secret;
				            UploadUtil.mRoomID = jsonBean.base_info.room_id;
				            ChatRoomActivity main = new ChatRoomActivity();
							main.receiveMessageFormServer(UploadUtil.mUserName, UploadUtil.mUserID,UploadUtil.mFileType, jsonBean.message_info.content, UploadUtil.mVoiceLength, UploadUtil.agreement,UploadUtil.isSystemMessage,UploadUtil.isPrivateChat);
		
						} else if (jsonBean.type.equals("say")&&jsonBean.message_info.msg_type.equals("file") && jsonBean.base_info.is_secret == 0 && jsonBean.message_info.is_agreedoc ==0){
						    //待改
				              String username = jsonBean.base_info.from_username;
				              if (username.equals(mSpUtil.getNick())) {
								return;
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
						} else if (mSpUtil.getAdminID() != 0 && UploadUtil.agreement == 1 ) {
							//调解协议书
							ChatRoomActivity main = new ChatRoomActivity();
							main.receiveMessageFormServer(UploadUtil.mUserName, UploadUtil.mUserID,UploadUtil.mFileType, "http://hcjd.dev.bizcn.com/Home/AdjOl/adjagreement.html?room_id="+mSpUtil.getRoomID(), UploadUtil.mVoiceLength, UploadUtil.agreement,UploadUtil.isSystemMessage,UploadUtil.isPrivateChat);
						}else if (jsonBean.type.equals("say")&&jsonBean.message_info.msg_type.equals("file") && jsonBean.base_info.is_secret == 0 && jsonBean.message_info.is_agreedoc ==1) {
							//调解协议书
							UploadUtil.agreement = 1;
							ChatRoomActivity main = new ChatRoomActivity();
							main.receiveMessageFormServer(UploadUtil.mUserName, UploadUtil.mUserID,"file", "http://hcjd.dev.bizcn.com/Home/AdjOl/adjagreement.html?room_id="+mSpUtil.getRoomID(), UploadUtil.mVoiceLength, UploadUtil.agreement,UploadUtil.isSystemMessage,UploadUtil.isPrivateChat);
						}
			              
			            				            
			            }
			            
			            @Override
			            public void onClose(int code, String reason) {
			               Log.d("chat", "Connection lost.");
			 
			               if (isConnect) {
			            	   Toast.makeText(mContext, "您已退出聊天室", Toast.LENGTH_SHORT).show();
			            	   isConnect = false;
						}
			               try {
							handleConnection(null,null,null, mContext);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}         
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
    
  //截取数字  
    public String getNumbers(String content) {  
        Pattern pattern = Pattern.compile("\\d+");  
        Matcher matcher = pattern.matcher(content);  
        while (matcher.find()) {  
            return matcher.group(0);  
        }  
        return "";  
    }  
    
    @SuppressLint("NewApi")
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
	
	@SuppressLint("NewApi")
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
    		send.message_info.extension = ".png";
		} else if (filetype.equals(".txt") && mSpUtil.getIsPrivateChat() == 0) {
			send.message_info.msg_type = "text";
			String decodeString = new String(Base64.decode((String)data, Base64.NO_WRAP));
			send.message_info.content = decodeString;
		} else if (filetype.equals(".mp3")) {
			send.message_info.msg_type = "audio";
			send.message_info.filename = mSpUtil.getVoiceTime() + "\"";
			send.message_info.extension = ".mp3";
		} else if (filetype.equals(".doc")) {
			send.message_info.msg_type = "file";
			send.message_info.filename = filename+".doc";
			send.message_info.extension = ".doc";
		} else if (filetype.equals(".txt") && mSpUtil.getIsPrivateChat() == 1) { //悄悄话
			send.base_info.is_secret = 1;
			send.base_info.to_client_id = String.valueOf(mSpUtil.getAdminID());
			send.message_info.msg_type = "text";
			String decodeString = new String(Base64.decode((String)data, Base64.NO_WRAP));
			send.message_info.content = decodeString;
			send.base_info.to_userid = String.valueOf(mSpUtil.getAdminID());
			send.base_info.to_realname = mSpUtil.getAdminRealName();
			send.base_info.to_username = mSpUtil.getAdminUserName();
			send.base_info.to_client_id = String.valueOf(mSpUtil.getAdminClientID());
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






