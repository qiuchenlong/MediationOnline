package com.pzf.liaotian.adapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.bither.util.ImageShower;

import android.annotation.SuppressLint;
import android.content.ClipData.Item;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pzf.liaotian.MainViewActivity;
import com.pzf.liaotian.WebViewActivity;
import com.pzf.liaotian.PublicChatActivity;
import com.pzf.liaotian.R;
import com.pzf.liaotian.album.takephoto.BubbleImageHelper;
import com.pzf.liaotian.album.takephoto.MessageBitmapCache;
import com.pzf.liaotian.app.PushApplication;
import com.pzf.liaotian.bean.MessageItem;
import com.pzf.liaotian.common.util.SharePreferenceUtil;
import com.pzf.liaotian.common.util.SoundUtil;
import com.pzf.liaotian.common.util.TimeUtil;
import com.pzf.liaotian.view.GifTextView;

/**
 * @desc发送消息的adapter
 * @author pangzf
 * @blog:http://blog.csdn.net/pangzaifei/article/details/43023625
 * @github:https://github.com/pangzaifei/zfIMDemo
 * @qq:1660380990
 * @email:pzfpang451@163.com
 */
@SuppressLint("NewApi")
public class MessageAdapter extends BaseAdapter {

    public static final Pattern EMOTION_URL = Pattern.compile("\\[(\\S+?)\\]");
    public static final int MESSAGE_TYPE_INVALID = -1;
    public static final int MESSAGE_TYPE_MINE_TEXT = 0x00;
    public static final int MESSAGE_TYPE_MINE_IMAGE = 0x01;
    public static final int MESSAGE_TYPE_MINE_AUDIO = 0x02;
    public static final int MESSAGE_TYPE_OTHER_TEXT = 0x03;
    public static final int MESSAGE_TYPE_OTHER_IMAGE = 0x04;
    public static final int MESSAGE_TYPE_OTHER_AUDIO = 0x05;
    public static final int MESSAGE_TYPE_TIME_TITLE = 0x07;
    public static final int MESSAGE_TYPE_HISTORY_DIVIDER = 0x08;
    public static final int MESSAGE_TYPE_MINE_FILE = 0x10;
    public static final int MESSAGE_TYPE_OTHER_FILE = 0x11;
    private static final int VIEW_TYPE_COUNT = 9;

    private Context mContext;
    private LayoutInflater mInflater;
    private List<MessageItem> mMsgList;
    private SharePreferenceUtil mSpUtil;

    private long mPreDate;

    private SoundUtil mSoundUtil;

    //点击放大缩小图片
    Bitmap bp=null;  
    ImageView imageview;  
    float scaleWidth;  
    float scaleHeight;  
    int h;  
    boolean num=true; 
    TextView noticeTextView;
    LinearLayout linearLayout;

    public MessageAdapter(Context context, List<MessageItem> msgList) {
        mContext = context;
        mMsgList = msgList;
        mInflater = LayoutInflater.from(context);
        mSpUtil = PushApplication.getInstance().getSpUtil();
        mSoundUtil = SoundUtil.getInstance();
    }

    public void removeHeadMsg() {
        if (mMsgList.size() - 10 > 10) {
            for (int i = 0; i < 10; i++) {
                mMsgList.remove(i);
            }
            notifyDataSetChanged();
        }
    }

    public void setmMsgList(List<MessageItem> msgList) {
        mMsgList = msgList;
        notifyDataSetChanged();
    }

    public void upDateMsg(MessageItem msg) {
        mMsgList.add(msg);
        notifyDataSetChanged();
    }

    public void upDateMsgByList(List<MessageItem> list) {
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                mMsgList.add(list.get(i));
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mMsgList.size();
    }

    @Override
    public Object getItem(int position) {
        return mMsgList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // final MessageItem item = mMsgList.get(position);
        // boolean isComMsg = item.isComMeg();
        // ViewHolder holder;
        // if (convertView == null
        // || convertView.getTag(R.drawable.ic_launcher + position) == null) {
        // holder = new ViewHolder();
        // if (isComMsg) {
        // convertView = mInflater.inflate(R.layout.chat_item_left, null);
        // } else {
        // convertView = mInflater.inflate(R.layout.chat_item_right, null);
        // }
        // holder.head = (ImageView) convertView.findViewById(R.id.icon);
        // holder.time = (TextView) convertView.findViewById(R.id.datetime);
        // holder.msg = (GifTextView) convertView.findViewById(R.id.textView2);
        // holder.rlMessage = (RelativeLayout) convertView
        // .findViewById(R.id.relativeLayout1);
        // holder.ivphoto = (ImageView) convertView
        // .findViewById(R.id.iv_chart_item_photo);
        // holder.progressBar = (ProgressBar) convertView
        // .findViewById(R.id.progressBar1);
        // holder.voiceTime = (TextView) convertView
        // .findViewById(R.id.tv_voice_time);
        // holder.flPickLayout = (FrameLayout) convertView
        // .findViewById(R.id.message_layout);
        // convertView.setTag(R.drawable.ic_launcher + position);
        // } else {
        // holder = (ViewHolder) convertView.getTag(R.drawable.ic_launcher
        // + position);
        // }
        // holder.time.setText(TimeUtil.getChatTime(item.getDate()));
        //
        // // if (Math.abs(mPreDate - item.getDate()) < 60000) {
        // // holder.time.setVisibility(View.GONE);
        // // } else {
        // // mPreDate = item.getDate();
        // // holder.time.setVisibility(View.VISIBLE);
        // // }
        //
        // holder.time.setVisibility(View.VISIBLE);
        //
        // holder.head.setBackgroundResource(PushApplication.heads[item
        // .getHeadImg()]);
        // if (!isComMsg && !mSpUtil.getShowHead()) {
        // holder.head.setVisibility(View.GONE);
        // }
        // showTextOrVoiceOrImage(item, holder);
        // holder.msg.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // if (item.getMsgType() == MessageItem.MESSAGE_TYPE_RECORD) {
        // mSoundUtil.playRecorder(mContext, item.getMessage());
        // }
        // }
        // });
        //
        // holder.progressBar.setVisibility(View.GONE);
        // holder.progressBar.setProgress(50);
        // return convertView;

        // ===============


        int type = getItemViewType(position);
        MessageHolderBase holder = null;
        if (null == convertView && null != mInflater) {
            holder = new MessageHolderBase();



            switch (type) {
                case MESSAGE_TYPE_MINE_TEXT: {
                    convertView = mInflater.inflate(
                            R.layout.zf_chat_mine_text_message_item, parent, false);
                    holder = new TextMessageHolder();
                    convertView.setTag(holder);
                    fillTextMessageHolder((TextMessageHolder) holder,
                            convertView);
                    break;
                }
                case MESSAGE_TYPE_MINE_IMAGE: {
                    convertView = mInflater.inflate(
                            R.layout.zf_chat_mine_image_message_item, parent, false);
                    holder = new ImageMessageHolder();
                    convertView.setTag(holder);
                    // fillTextMessageHolder(holder, convertView);
                    fillImageMessageHolder((ImageMessageHolder) holder,
                            convertView);
                    break;
                }
                case MESSAGE_TYPE_MINE_AUDIO: {
                    convertView = mInflater.inflate(
                            R.layout.zf_chat_mine_audio_message_item, parent, false);
                    holder = new AudioMessageHolder();
                    convertView.setTag(holder);
                    fillAudioMessageHolder((AudioMessageHolder) holder,
                            convertView);
                    break;
                }
                case MESSAGE_TYPE_OTHER_TEXT: {
                    convertView = mInflater.inflate(
                            R.layout.zf_chat_other_text_message_item, parent, false);
                    holder = new TextMessageHolder();
                    convertView.setTag(holder);
                    fillTextMessageHolder((TextMessageHolder) holder,
                            convertView);
                    break;
                }
                case MESSAGE_TYPE_OTHER_IMAGE: {
                    convertView = mInflater
                            .inflate(R.layout.zf_chat_other_image_message_item,
                                    parent, false);
                    holder = new ImageMessageHolder();
                    convertView.setTag(holder);
                    fillImageMessageHolder((ImageMessageHolder) holder,
                            convertView);
                    break;
                }
                case MESSAGE_TYPE_OTHER_AUDIO: {
                    convertView = mInflater
                            .inflate(R.layout.zf_chat_other_audio_message_item,
                                    parent, false);
                    holder = new AudioMessageHolder();
                    convertView.setTag(holder);
                    fillAudioMessageHolder((AudioMessageHolder) holder,
                            convertView);
                    break;
                }
                default:

                    break;
            }
        } else {
            holder = (MessageHolderBase) convertView.getTag();
        }

        final MessageItem mItem = mMsgList.get(position);
        if (mItem != null) {
            int msgType = mItem.getMsgType();
            if (msgType == MessageItem.MESSAGE_TYPE_TEXT) {
                handleTextMessage((TextMessageHolder) holder, mItem, parent);

            } else if (msgType == MessageItem.MESSAGE_TYPE_IMG) {
                handleImageMessage((ImageMessageHolder) holder, mItem, parent);

            } else if (msgType == MessageItem.MESSAGE_TYPE_RECORD) {
                handleAudioMessage((AudioMessageHolder) holder, mItem, parent);

            }else if (msgType == MessageItem.MESSAGE_TYPE_FILE) {
                handleTextMessage((TextMessageHolder) holder, mItem, parent);
            }
        }

        return convertView;
    }



    private void handleTextMessage(final TextMessageHolder holder,
            final MessageItem mItem, final View parent) {
        handleBaseMessage(holder, mItem);

        if (mItem.getDate() == 0) {
        	//显示公告
			holder.time.setVisibility(View.VISIBLE);
			holder.name.setVisibility(View.GONE);
			holder.time.setText("    公告：请双方当事人尊重法律，尊重对方，就事论事，避免人身攻击和论及其他无关事物，谢谢！");
			holder.time.setTextColor(Color.rgb(254, 20, 99));
			holder.time.setTextSize(14);
			holder.time.setPadding(12, 5, 12, 5);
			holder.time.bringToFront();
			holder.time.setBackgroundColor(Color.rgb(242, 242, 242));
			holder.flLayout.setVisibility(View.GONE);
		} else if (mItem.getMsgType() == MessageItem.MESSAGE_TYPE_FILE && mItem.isComMeg() == true && mItem.getMessage().contains(".doc")){
        	//收到文件
			holder.time.setVisibility(View.VISIBLE);
			holder.name.setVisibility(View.GONE);
			holder.time.setText("接收文件保存于："+mItem.getMessage());
			holder.time.setPadding(12, 5, 12, 5);
			holder.time.setTextSize(14);
			holder.time.bringToFront();
			holder.flLayout.setVisibility(View.GONE);
	
        }else if (mItem.getMsgType() == MessageItem.MESSAGE_TYPE_FILE && mItem.isComMeg() == false && mItem.getMessage().contains(".doc")) {
        	//发送文件
        	holder.time.setVisibility(View.VISIBLE);
			holder.name.setVisibility(View.GONE);
			holder.time.setText("已发送文件："+mItem.getMessage());
			holder.time.setPadding(12, 5, 12, 5);
			holder.time.setTextSize(14);
			holder.time.bringToFront();
			holder.flLayout.setVisibility(View.GONE);
           
        }
        else if(mItem.getMsgType() == MessageItem.MESSAGE_TYPE_FILE && mSpUtil.getIsAdmin() == 1 && mItem.getAgreement() == 1) {
            //如果收到的是调解协议书
        	holder.rlMessage.setBackgroundResource(R.drawable.balloon3_l);
            holder.msg.insertGif(convertNormalStringToSpannableString("调解协议书-待确认"));
        
//        } else if (mItem.getMsgType() == MessageItem.MESSAGE_TYPE_TEXT && mItem.getIsNew() == 1 && mItem.isComMeg() == true) {
//			//对方收到的text
//        	holder.rlMessage.setBackgroundResource(R.drawable.balloon1_l);
//        	// 文字
//            holder.msg.insertGif(convertNormalStringToSpannableString(mItem
//                    .getMessage() + " "));
		}else if (mItem.getMsgType() == MessageItem.MESSAGE_TYPE_TEXT && mItem.getIsSystemMessage() == MessageItem.SYSTEM_MESSAGE && mItem.getMessage().contains(mItem.getName()+",进入聊天室")) {
			//刚进入聊天室 发送提醒
			holder.time.setVisibility(View.VISIBLE);
			holder.name.setVisibility(View.GONE);
			holder.time.setText(mItem.getName()+",进入聊天室");
			holder.time.setPadding(12, 12, 12, 12);
			
			holder.time.bringToFront();
			holder.time.setTextSize(14);
			holder.time.setTextColor(Color.WHITE);
			holder.time.setBackgroundResource(R.drawable.chat_time_block);
			holder.flLayout.setVisibility(View.GONE);
		}else {
             // 文字
            holder.msg.insertGif(convertNormalStringToSpannableString(mItem
                    .getMessage() + " "));
            holder.flLayout.setVisibility(View.VISIBLE);
        }
        
      

        holder.msg.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                int filetype = mItem.getMsgType();
                //如果是协议书，点击后会跳转到相应的url
                if (mItem.getMsgType() == MessageItem.MESSAGE_TYPE_FILE && mSpUtil.getIsAdmin() == 1 && mItem.getAgreement() == 1) {
                
                    Intent intent =new Intent(mContext,WebViewActivity.class);  
                    intent.putExtra("URL_PATH", mItem.getMessage());
                    mContext.startActivity(intent);
                } 
                 Log.v("chat", "text message=" + filetype);

            }
        });

    }
    

    /**
     * @Description 处理图片消息
     * @param holder
     * @param info
     * @param position
     * @param isMine
     * @param parent
     */
    private void handleImageMessage(final ImageMessageHolder holder,
            final MessageItem mItem, final View parent) {
        handleBaseMessage(holder, mItem);
//        if (!mSpUtil.getIsCome()) {
//          holder.nameTitle.setText(mSpUtil.getNick());
//        }

        // 图片文件
        if (mItem.getMessage() != null) {
            // Bitmap bitmap = BitmapFactory.decodeFile(item.getMessage());
            Bitmap bitmap = MessageBitmapCache.getInstance().get(
                    mItem.getMessage());

            if (!mItem.isComMeg()) {
                bitmap = BubbleImageHelper.getInstance(mContext)
                        .getBubbleImageBitmap(bitmap,
                                R.drawable.zf_mine_image_big_default_bk);
            } else {
                bitmap = BubbleImageHelper.getInstance(mContext)
                        .getBubbleImageBitmap(bitmap,
                                R.drawable.zf_other_image_big_default_bk);
            }

            if (bitmap != null) {
                holder.ivphoto.setLayoutParams(new FrameLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                holder.ivphoto.setImageBitmap(bitmap);
            }
            // if (isMine) {
            // isMine = false;
            // showTextOrVoiceOrImage(item, holder);
            // }


            holder.flPickLayout.setVisibility(View.VISIBLE);
        } else {
            holder.flPickLayout.setVisibility(View.GONE);
        }
        holder.rlMessage.setVisibility(View.GONE);

       holder.ivphoto.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View arg0) {
            Intent intent = new Intent(mContext,ImageShower.class);
            intent.putExtra("photo_path", mItem.getMessage());
            mContext.startActivity(intent);

        }
    });

    }




    /**
     * @Description 处理语音消息
     * @param holder
     * @param info
     * @param isMine
     * @param parent
     * @param position
     */
    private void handleAudioMessage(final AudioMessageHolder holder,
            final MessageItem mItem, final View parent) {
        handleBaseMessage(holder, mItem);
        // 语音
        holder.msg.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                R.drawable.chatto_voice_playing, 0);
//        holder.ivphoto.setCompoundDrawablesWithIntrinsicBounds(0, 0,
//              R.drawable.chatto_voice_playing, 0);
        holder.voiceTime.setText(TimeUtil.getVoiceRecorderTime(mItem
                .getVoiceTime())+"\"");
        final String filePathString = mItem.getMessage();

        holder.msg.setOnClickListener(new OnClickListener() {
            //TODO
            @Override
            public void onClick(View v) {
                if (mItem.getMsgType() == MessageItem.MESSAGE_TYPE_RECORD) {
                    Log.v("com.pzf.liaotian", mItem.getMessage());
                    Log.v("com.pzf.liaotian", "is=="+mItem.isComMeg());
                    // 播放语音
                    mSoundUtil.playRecorder(mContext, filePathString,mItem.isComMeg());

                    //点击从服务器下载
                }
            }
        });
    }


    private void handleBaseMessage(MessageHolderBase holder,
            final MessageItem mItem) {  
    	mSpUtil.setIsSystemMessage(mItem.getIsSystemMessage());
    	mSpUtil.setIsAgreement(mItem.getAgreement());
    	
        //判断是不是悄悄话
        if (mItem.getIsPrivateChat() == 1) {
            holder.privateChat.setVisibility(View.VISIBLE);
        } else {
            holder.privateChat.setVisibility(View.GONE);
        }

//        if (mItem.getMsgType() == MessageItem.MESSAGE_TYPE_FILE && mItem.getMessage().contains("已发送文件：")) {
//            holder.rlMessage.setBackgroundResource(R.drawable.balloon1_r);
//        } else if (mItem.getMsgType() == MessageItem.MESSAGE_TYPE_TEXT){
//            holder.rlMessage.setBackgroundResource(R.drawable.balloon3_r);
//        }



        holder.time.setText(TimeUtil.getChatTime(mItem.getDate()));

        if (mItem.getIsHideTime() == 1) {
			holder.time.setVisibility(View.GONE);
		
		} else {
			holder.time.setVisibility(View.VISIBLE);
		}
        
        holder.name.setText(mItem.getName());
        holder.name.setVisibility(View.VISIBLE);
        holder.name.bringToFront();
        holder.head.setBackgroundResource(PushApplication.heads[mItem
                .getHeadImg()]);
        holder.head.setEnabled(false);
        holder.progressBar.setVisibility(View.GONE);
        holder.progressBar.setProgress(50);

    }

    private void fillBaseMessageholder(MessageHolderBase holder,
            View convertView) {

        holder.privateChat = (TextView)convertView.findViewById(R.id.private_chat_textview);
        holder.head = (ImageView) convertView.findViewById(R.id.icon);

        holder.time = (TextView) convertView.findViewById(R.id.datetime);
        
        
        
        // holder.msg = (GifTextView) convertView.findViewById(R.id.textView2);
        holder.rlMessage = (RelativeLayout) convertView
                .findViewById(R.id.relativeLayout1);

//        if (mSpUtil.getIsAdmin() == 1) {
//            holder.rlMessage.setBackgroundColor(Color)
//        }
        // holder.ivphoto = (ImageView) convertView
        // .findViewById(R.id.iv_chart_item_photo);
        holder.progressBar = (ProgressBar) convertView
                .findViewById(R.id.progressBar1);
        // holder.voiceTime = (TextView) convertView
        // .findViewById(R.id.tv_voice_time);
        holder.name = (TextView) convertView.findViewById(R.id.nametitle);
        holder.name.setVisibility(View.VISIBLE);
        
        holder.flPickLayout = (FrameLayout) convertView
                .findViewById(R.id.message_layout);
        holder.flLayout = (LinearLayout) convertView.findViewById(R.id.linearLayout1);

    }

    private void fillTextMessageHolder(TextMessageHolder holder,
            View convertView) {
        fillBaseMessageholder(holder, convertView);
        holder.msg = (GifTextView) convertView.findViewById(R.id.textView2);
//        if (mSpUtil.getIsAdmin() == 1) {
//            holder.msg.setBackgroundColor(Color.parseColor("#FF99CC"));
//        }
    }

    private void fillImageMessageHolder(ImageMessageHolder holder,
            View convertView) {
        fillBaseMessageholder(holder, convertView);
        holder.ivphoto = (ImageView) convertView
                .findViewById(R.id.iv_chart_item_photo);
    }

    private void fillAudioMessageHolder(AudioMessageHolder holder,
            View convertView) {
        fillBaseMessageholder(holder, convertView);
        holder.voiceTime = (TextView) convertView
                .findViewById(R.id.tv_voice_time);
        holder.ivphoto = (ImageView) convertView
                .findViewById(R.id.iv_chart_item_photo);
        holder.msg = (GifTextView) convertView.findViewById(R.id.textView2);
//        if (mSpUtil.getIsAdmin() == 1) {
//            holder.msg.setBackgroundColor(Color.parseColor("#FF99CC"));
//        }
    }

    private static class MessageHolderBase {
        TextView privateChat;
        TextView name;
        ImageView head;
        TextView time;
        ImageView imageView;
        ProgressBar progressBar;
        RelativeLayout rlMessage;
        FrameLayout flPickLayout;
        LinearLayout flLayout;
    }

    private static class TextMessageHolder extends MessageHolderBase {
        /**
         * 文字消息体
         */
        GifTextView msg;
    }

    private static class ImageMessageHolder extends MessageHolderBase {

        /**
         * 图片消息体
         */
        ImageView ivphoto;
    }

    private static class AudioMessageHolder extends MessageHolderBase {
        ImageView ivphoto;
        /**
         * 语音秒数
         */
        TextView voiceTime;
        GifTextView msg;
    }


    /**
     * 另外一种方法解析表情将[表情]换成fxxx
     * 
     * @param message
     *            传入的需要处理的String
     * @return
     */
    private String convertNormalStringToSpannableString(String message) {
        String hackTxt;
        if (message.startsWith("[") && message.endsWith("]")) {
            hackTxt = message + " ";
        } else {
            hackTxt = message;
        }

        Matcher localMatcher = EMOTION_URL.matcher(hackTxt);
        while (localMatcher.find()) {
            String str2 = localMatcher.group(0);
            if (PushApplication.getInstance().getFaceMap().containsKey(str2)) {
                String faceName = mContext.getResources().getString(
                        PushApplication.getInstance().getFaceMap().get(str2));
                CharSequence name = options(faceName);
                message = message.replace(str2, name);
            }

        }
        return message;
    }

    /**
     * 取名字f010
     * 
     * @param faceName
     */
    private CharSequence options(String faceName) {
        int start = faceName.lastIndexOf("/");
        CharSequence c = faceName.subSequence(start + 1, faceName.length() - 4);
        return c;
    }

    static class ViewHolder {

        TextView privateChat;
        TextView name;
        ImageView head;
        TextView time;
        GifTextView msg;
        ImageView imageView;
        ProgressBar progressBar;
        TextView voiceTime;
        ImageView ivphoto;
        RelativeLayout rlMessage;
        FrameLayout flPickLayout;
        LinearLayout flLayout;
    }

    @Override
    public int getItemViewType(int position) {
        // logger.d("chat#getItemViewType -> position:%d", position);
        try {
            if (position >= mMsgList.size()) {
                return MESSAGE_TYPE_INVALID;
            }

            MessageItem item = mMsgList.get(position);
            if (item != null) {
                boolean comMeg = item.isComMeg();
                int type = item.getMsgType();
                if (comMeg) {
                    // 接受的消息
                    switch (type) {
                        case MessageItem.MESSAGE_TYPE_TEXT: {
                            return MESSAGE_TYPE_OTHER_TEXT;
                        }

                        case MessageItem.MESSAGE_TYPE_IMG: {
                            return MESSAGE_TYPE_OTHER_IMAGE;
                        }

                        case MessageItem.MESSAGE_TYPE_RECORD: {
                            return MESSAGE_TYPE_OTHER_AUDIO;
                        }
                        case MessageItem.MESSAGE_TYPE_FILE: {
                            return MESSAGE_TYPE_OTHER_TEXT;
                        }

                        default:
                            break;
                    }
                } else {
                    // 发送的消息
                    switch (type) {
                        case MessageItem.MESSAGE_TYPE_TEXT: {
                            return MESSAGE_TYPE_MINE_TEXT;

                        }

                        case MessageItem.MESSAGE_TYPE_IMG: {
                            return MESSAGE_TYPE_MINE_IMAGE;

                        }

                        case MessageItem.MESSAGE_TYPE_RECORD: {
                            return MESSAGE_TYPE_MINE_AUDIO;
                        }

                        case MessageItem.MESSAGE_TYPE_FILE: {
                            return MESSAGE_TYPE_MINE_TEXT;
                        }

                        default:
                            break;
                    }
                }
            }
            return MESSAGE_TYPE_INVALID;
        } catch (Exception e) {
            Log.e("fff", e.getMessage());
            return MESSAGE_TYPE_INVALID;
        }
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

}