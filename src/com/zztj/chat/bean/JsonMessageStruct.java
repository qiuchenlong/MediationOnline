package com.zztj.chat.bean;

import java.util.List;

import com.pzf.liaotian.PublicChatActivity;
import com.zztj.chat.bean.EnterChatRoomServer.Base_Info;
import com.zztj.chat.bean.EnterChatRoomServer.Message_Info;
import com.zztj.chat.bean.EnterChatRoomServer.Message_Info.Client_List;

public class JsonMessageStruct {

	public String type;
	public Base_Info base_info;
	public Message_Info message_info;
	
	public static class Message_Info {
		
		public List<Client_List> client_List;
		public String msg_type;
		public String avatar;
		public String username;
		public String content;
		public String time;
		public String is_secret;
		public String filename;
		public String extension;
		public String src_url;
		
		public static class Client_List {
			public int client_id;
			public int userid;
			public String username;
			public String realname;
			public String head_view_pic;
			
			public void setUserName(String username) {
				this.username = username;
			}
			
			public void setClientID(int clientid) {
				this.client_id = clientid;
			}
			
			public void setUserID(int userid) {
				this.userid = userid;
			}
			
			public void setRealName(String realname) {
				this.realname = realname;
			}
			
			public void setHeadViewPic(String headviewpic) {
				this.head_view_pic = headviewpic;
			}
			
			
			public String getUserName() {
				return username;
			}
			
			public int getClientID() {
				return client_id;
			}
			
			public int getUserID() {
				return userid;
			}
			
			public String getRealName() {
				return realname;
			}
			
			public String getHeadViewPic() {
				return head_view_pic;
			}
		}
	}
	
	
	public static class Base_Info {
		public int room_id;
		public int from_client_id;
		public String from_username;
		public String session_id;
		public String to_client_id;
		public String is_secret;
		
		public void setToClientID(String to_client_id) {
			this.to_client_id = to_client_id;
		}
		
		public String getToClientID() {
			return to_client_id;
		}
		
		public void setIsSecret(String is_secret) {
			this.is_secret = is_secret;
		}
		
		public String getIsSecret() {
			return is_secret;
		}
		
		public void setSessionID(String session_id) {
			this.session_id = session_id;
		}
		
		public String getSessionID() {
			return session_id;
		}
		
		public void setRoomID(int roomid) {
			this.room_id = roomid;
		}
		
		public int getRoomID() {
			return room_id;
		}
		
		public void setFromClientID(int fromclientid) {
			this.from_client_id = fromclientid;
		}
		
		public int getFromClientID() {
			return from_client_id;
		}
		
		public void setFromUserName(String username) {
			this.from_username = username;
		}
		
		public String getFromUserName() {
			return from_username;
		}
	}
	
	public Message_Info getMessageInfo() {
		return message_info;
	}
	
	public void setMessageInfo(Message_Info messageinfo) {
		this.message_info = messageinfo;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public Base_Info getBaseInfo() {
		return base_info;
	}
	
	public void setBaseInfo(Base_Info baseinfo) {
		this.base_info = baseinfo;
	}
}
