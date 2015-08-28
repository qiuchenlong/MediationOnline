package com.pzf.liaotian.common.util;

import com.pzf.liaotian.bean.MessageItem;

import android.R.bool;
import android.content.Context;
import android.content.SharedPreferences;

public class SharePreferenceUtil {
    public static final String MESSAGE_NOTIFY_KEY = "message_notify";
    public static final String MESSAGE_SOUND_KEY = "message_sound";
    public static final String SHOW_HEAD_KEY = "show_head";
    public static final String PULLREFRESH_SOUND_KEY = "pullrefresh_sound";
    
    
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    public SharePreferenceUtil(Context context, String file) {
        sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        editor = sp.edit();
    }
  
    //聊天室id
    public void setRoomID(int  roomid) {
    	editor.putInt("roomid", roomid);
    	editor.commit();
    }
    
    public int getRoomID() {
    	return sp.getInt("roomid", 0);
    }
    
    
    //调解咨询
    public void setIsConsult(Boolean  isConsult) {
    	editor.putBoolean("isconsult", isConsult);
    	editor.commit();
    }
    
    public Boolean getIsConsult() {
    	return sp.getBoolean("isconsult", false);
    }
    
    //协议书
    public void setIsAgreement(int  isAgreement) {
    	editor.putInt("isagreement", isAgreement);
    	editor.commit();
    }
    
    public int getIsAgreement() {
    	return sp.getInt("isagreement", 0);
    }
    
    //系统消息
    public void setIsSystemMessage(int  isSystemMessage) {
    	editor.putInt("issystemmessage", isSystemMessage);
    	editor.commit();
    }
    
    public int getIsSystemMessage() {
    	return sp.getInt("issystemmessage", MessageItem.NOT_SYSTEM_MESSAGE);
    }
    
    
  //时间
    public void setSystemTime(Long  currentTime) {
    	editor.putLong("currenttime", currentTime);
    	editor.commit();
    }
    
    public long getsystemtime() {
    	return sp.getLong("currenttime", 0);
    }
    //语音权限
    public void setRecordPermission(Boolean permission) {
    	editor.putBoolean("recordpermission", permission);
    	editor.commit();
    }
    
    public Boolean getRecordPermission() {
    	return sp.getBoolean("recordpermission", false);
    }
    
    //服务器ip
    public void setServerIP(String serverip) {
    	editor.putString("serverip", serverip);
    	editor.commit();
    }
    
    public String getServerIP() {
    	return sp.getString("serverip", "");
    }
    
    //管理员
    public void setIsAdmin(int isadmin) {
    	editor.putInt("isadmin", isadmin);
    	editor.commit();
    }
    
    public int getIsAdmin() {
    	return sp.getInt("isadmin", 0);
    }
    
    //悄悄话
    public void setIsPrivateChat(int isprivatechat) {
    	editor.putInt("isprivatechat", isprivatechat);
    	editor.commit();
    }
    
    public int getIsPrivateChat() {
    	return sp.getInt("isprivatechat", 0);
    }
    
    // appid
    public void setAppId(String appid) {
        // TODO Auto-generated method stub
        editor.putString("appid", appid);
        editor.commit();
    }

    public String getAppId() {
        return sp.getString("appid", "");
    }

    //voicetime
    public void setVoiceTime(int voiceTime) {
        editor.putInt("voiceTime", voiceTime);
        editor.commit();
    }

    public int getVoiceTime() {
        return sp.getInt("voiceTime", 0);
    }
    
    // user_id
    public void setUserId(String userId) {
        editor.putString("userId", userId);
        editor.commit();
    }

    public String getUserId() {
        return sp.getString("userId", "");
    }
    

    // channel_id
    public void setChannelId(String ChannelId) {
        editor.putString("ChannelId", ChannelId);
        editor.commit();
    }

    public String getChannelId() {
        return sp.getString("ChannelId", "");
    }

    // nick
    public void setNick(String nick) {
        editor.putString("nick", nick);
        editor.commit();
    }

    public String getNick() {
        return sp.getString("nick", "");
    }

    // 头像图标
    public int getHeadIcon() {
        return sp.getInt("headIcon", 0);
    }

    public void setHeadIcon(int icon) {
        editor.putInt("headIcon", icon);
        editor.commit();
    }

    // 设置Tag
    public void setTag(String tag) {
        editor.putString("tag", tag);
        editor.commit();
    }

    public String getTag() {
        return sp.getString("tag", "");
    }
    
 // 设置Tag
    public void setIsCome(Boolean iscome) {
        editor.putBoolean("iscome", iscome);
        editor.commit();
    }

    public Boolean getIsCome() {
        return sp.getBoolean("iscome", false);
    }

    // 是否通知
    public boolean getMsgNotify() {
        return sp.getBoolean(MESSAGE_NOTIFY_KEY, true);
    }

    public void setMsgNotify(boolean isChecked) {
        editor.putBoolean(MESSAGE_NOTIFY_KEY, isChecked);
        editor.commit();
    }

    // 新消息是否有声音
    public boolean getMsgSound() {
        return sp.getBoolean(MESSAGE_SOUND_KEY, true);
    }

    public void setMsgSound(boolean isChecked) {
        editor.putBoolean(MESSAGE_SOUND_KEY, isChecked);
        editor.commit();
    }

    // 刷新是否有声音
    public boolean getPullRefreshSound() {
        return sp.getBoolean(PULLREFRESH_SOUND_KEY, true);
    }

    public void setPullRefreshSound(boolean isChecked) {
        editor.putBoolean(PULLREFRESH_SOUND_KEY, isChecked);
        editor.commit();
    }

    // 是否显示自己头像
    public boolean getShowHead() {
        return sp.getBoolean(SHOW_HEAD_KEY, true);
    }

    public void setShowHead(boolean isChecked) {
        editor.putBoolean(SHOW_HEAD_KEY, isChecked);
        editor.commit();
    }

    // 表情翻页效果
    public int getFaceEffect() {
        return sp.getInt("face_effects", 7);
    }

    public void setFaceEffect(int effect) {
        if (effect < 0 || effect > 11)
            effect = 3;
        editor.putInt("face_effects", effect);
        editor.commit();
    }
}
