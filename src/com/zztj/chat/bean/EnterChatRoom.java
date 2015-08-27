package com.zztj.chat.bean;

import java.util.List;

import javax.security.auth.PrivateCredentialPermission;

public class EnterChatRoom {
	private String type;
	private Base_Info base_info;
	
	public static class Base_Info {
		public String session_id;
		public long room_id;
		
		public String getSessionID() {
			return session_id;
		}
		
		public void setSessionID(String sessionid) {
			this.session_id = sessionid;
		}
		
		public long getRoomID() {
			return room_id;
		}
		
		public void setRoomID(long roomid) {
			this.room_id = roomid;
		}
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
