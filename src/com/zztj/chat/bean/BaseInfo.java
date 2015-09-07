package com.zztj.chat.bean;

public class BaseInfo {
	public int room_id;
	public int from_client_id;
	public String from_username;
	public String session_id;
	public String to_client_id;
	public int is_secret;
	public String room_name;
	public String to_username;
	public String to_userid;
	public String to_realname;
	
	public void setToClientID(String to_client_id) {
		this.to_client_id = to_client_id;
	}
	
	public String getToClientID() {
		return to_client_id;
	}
	
	public void setIsSecret(int is_secret) {
		this.is_secret = is_secret;
	}
	
	public int getIsSecret() {
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
