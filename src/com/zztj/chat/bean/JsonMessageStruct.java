package com.zztj.chat.bean;

import java.util.List;

import com.pzf.liaotian.PublicChatActivity;
import com.zztj.chat.bean.EnterChatRoomServer.Base_Info;
import com.zztj.chat.bean.EnterChatRoomServer.Message_Info;
import com.zztj.chat.bean.EnterChatRoomServer.Message_Info.Client_List;

public class JsonMessageStruct {

	public static JsonMessageStruct jsonMessage;
	
	public String type;
	public BaseInfo base_info;
	public MessageInfo message_info;
	
	public void init() {
		this.base_info = new BaseInfo();
		this.message_info = new MessageInfo();
	}
}
