package com.zztj.chat.bean;

public class ClientList {

	public int client_id;
	public int userid;
	public String username;
	public String realname;
	public String head_view_pic;
	public int is_adjuster;
	
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
