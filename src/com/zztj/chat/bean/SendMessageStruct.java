package com.zztj.chat.bean;

public class SendMessageStruct {

	public String type;
	public Base_Info base_info;
	public Message_Info message_info;
	
	public void setType(String type) {
		this.type = type;
	}
	
	public void setBaseInfo (Base_Info base_info) {
		this.base_info = base_info;
	}
	
	public void setMessageInfo(Message_Info message_info) {
		this.message_info = message_info;
	}
	
	public String getType() {
		return type;
	}
	
	public Base_Info getBaseInfo() {
		return base_info;
	}
	
	public Message_Info getMessageInfo() {
		return message_info;
	}
	
	public static class Base_Info {
		public long room_id;
		public long from_client_id;
		public String from_username;
		public String to_client_id;
		public String is_secret;
		
		public void setRoomID(long roomid) {
			this.room_id = roomid;
		}
		
		public void setFromClientID(long fromclientid) {
			this.from_client_id = fromclientid;
		}
		
		public void setFromUserName(String fromusername) {
			this.from_username = fromusername;
		}
		
		public void setToClientID (String to_client_id) {
			this.to_client_id = to_client_id;
		}
		
		public void setIsSecret(String is_secret) {
			this.is_secret = is_secret;
		}
		
		public long getRoomID() {
			return room_id;
		}
		
		public long getFromClientID() {
			return from_client_id;
		}
		
		public String getFromUserName() {
			return from_username;
		}
		
		public String getToClientID() {
			return to_client_id;
		}
		
		public String getIsSecret() {
			return is_secret;
		}
			
	}
	
	public static class Message_Info {
		public String msg_type;
		public String avatar;
		public String username;
		public String content;
		public String time;
		
		public void setMsgTypeInfo(String msg_type) {
			this.msg_type = msg_type;
		}
		
		public void setAvatar(String avatar) {
			this.avatar = avatar;
		}
		
		public void setUserName(String username) {
			this.username = username;
		}
		
		public void setContent(String content) {
			this.content = content;
		}
		
		public void setTime(String time) {
			this.time = time;
		}
		
		public String getMsgType() {
			return msg_type;
		}
		
		public String getAvatar() {
			return avatar;
		}
		
		public String getUserName() {
			return username;
		}
		
		public String getContent() {
			return content;
		}
		
		public String getTime() {
			return time;
		}
		
		
	}
}
