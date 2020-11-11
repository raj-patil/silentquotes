/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.silentquot.R;
import com.silentquot.socialcomponents.adapters.holders.MessageViewHolder;
import com.silentquot.socialcomponents.model.Message;
import com.silentquot.socialcomponents.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    public static  final int MSG_TYPE_LEFT_TEXT = 0;
    public static  final int MSG_TYPE_LEFT_IMG = 1;
    public static  final int MSG_TYPE_RIGHT_TEXT= 2;
    public static  final int MSG_TYPE_RIGHT_IMG = 3;
    private final Activity activity;

    private MessageViewHolder.CallBack callBack;

    private List<Message> messagelist =  new ArrayList<>();


    public MessageAdapter(Activity activity) {
        this.activity = activity;
        setHasStableIds(true);
    }


    public void setCallBack(MessageViewHolder.CallBack callBack) {
        this.callBack = callBack;
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MSG_TYPE_RIGHT_TEXT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_right, parent, false);
            return new MessageViewHolder(view , callBack, activity);
        }else  if (viewType == MSG_TYPE_RIGHT_IMG) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_img_right, parent, false);
            return new MessageViewHolder(view , callBack ,activity);
        }
        else if (viewType==MSG_TYPE_LEFT_IMG)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_img_left, parent, false);
            return new MessageViewHolder(view,callBack,activity);
        }
        else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_left, parent, false);
            return new MessageViewHolder(view,callBack,activity);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        int TYPE = getItemViewType(position);
        if (TYPE==MSG_TYPE_LEFT_TEXT || TYPE == MSG_TYPE_RIGHT_TEXT) {
            ((MessageViewHolder)holder).bindMessage(getItemByPosition(position));
        }
        else
        {
            String status=getItemByPosition(position).getStatus();
            if (status!=null &&status.equals("sending"))
            {
                LogUtil.logDebug("TAG", "bindlocalimage " + status);
                ((MessageViewHolder)holder).bindLocalImageMessage(getItemByPosition(position));
            }
            else {

                if (TYPE == MSG_TYPE_RIGHT_IMG) {
                    ((MessageViewHolder)holder).bindSenderImageMessage(getItemByPosition(position));
                }
                else
                {
                    ((MessageViewHolder)holder).bindReceiverImageMessage(getItemByPosition(position));
                }
            }
        }
  //      if (getItemViewType(position)!=MSG_TYPE_RIGHT)
    }



   public void  setMessageList(List<Message> messageList)
   {
     this.messagelist.clear();
       this.messagelist=messageList;
       notifyDataSetChanged();

   }



   public  void addNewMessages(List<Message> messageList)
   {
       if (this.messagelist!=null) {
       this.messagelist.addAll(messageList);
           notifyDataSetChanged();
   }
   else {
       this.messagelist=messageList;
           notifyDataSetChanged();
   }
   }

    @Override
    public long getItemId(int position) {
        return position;
    }



    public void updateNewMesage(Message message) {
        List<Message> messageList = messagelist;

        if (messageList != null) {
            int messagePosition = messageList.indexOf(message);

            if (messagePosition >= 0 && messagePosition < messageList.size()) {
                messageList.set(messagePosition, message);
            } else {
                messageList.add(message);
//            int lastPosition = messageList.size() - 1;

//            messageList.add(lastPosition, message);
            }
            notifyDataSetChanged();
        }
        else {
            messagelist=new ArrayList<>();
            messagelist.add(message);
            notifyDataSetChanged();
        }
    }
    public void updateItem() {
        notifyItemChanged(messagelist.size()+1);
        notifyDataSetChanged();
    }
    @Override
    public int getItemViewType(int position) {
        Message Message = getItemByPosition(position);
       String currentuserid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (Message.getSender().equals(currentuserid) && Message.getMsgtype().equals("Text") ){
            return MSG_TYPE_RIGHT_TEXT;
        }else  if (Message.getSender().equals(currentuserid) && Message.getMsgtype().equals("Image"))
        {
            return  MSG_TYPE_RIGHT_IMG;
        }else if (!Message.getSender().equals(currentuserid) && Message.getMsgtype().equals("Image"))
        {
            return MSG_TYPE_LEFT_IMG;
        }
        else {
            return MSG_TYPE_LEFT_TEXT;
        }
    }

    public Message getItemByPosition(int position) {
        return messagelist.get(position);
    }

    @Override
    public int getItemCount() {
        if (messagelist!=null) {

            return messagelist.size();
        }
        else
        {
            return 0;
        }
    }

    public void addSedingMessage(Message sendingMessage) {
        this.messagelist.add(sendingMessage);
        notifyDataSetChanged();
    }



}
