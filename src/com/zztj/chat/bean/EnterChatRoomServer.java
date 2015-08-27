package com.zztj.chat.bean;

import java.util.List;
import java.util.Set;

import javax.security.auth.PrivateCredentialPermission;

public class EnterChatRoomServer {
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
		
		public static class Client_List {
			public long client_id;
			public long userid;
			public String username;
			public String realname;
			public String head_view_pic;
			
			public void setUserName(String username) {
				this.username = username;
			}
			
			public void setClientID(long clientid) {
				this.client_id = clientid;
			}
			
			public void setUserID(long userid) {
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
			
			public long getClientID() {
				return client_id;
			}
			
			public long getUserID() {
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
		public long room_id;
		public long from_client_id;
		public String from_username;
		
		public void setRoomID(long roomid) {
			this.room_id = roomid;
		}
		
		public long getRoomID() {
			return room_id;
		}
		
		public void setFromClientID(long fromclientid) {
			this.from_client_id = fromclientid;
		}
		
		public long getFromClientID() {
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
