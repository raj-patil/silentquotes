/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.managers;

import android.content.Context;
import android.net.Uri;

import com.google.firebase.database.ValueEventListener;
import com.silentquot.Model.Chatlist;
import com.silentquot.socialcomponents.main.interactors.ChatInteractor;
import com.silentquot.socialcomponents.managers.listeners.OnChatListChangeListner;
import com.silentquot.socialcomponents.managers.listeners.OnMessageListChangesListner;
import com.silentquot.socialcomponents.managers.listeners.OnObjectChangedListener;
import com.silentquot.socialcomponents.managers.listeners.OnSendMessageListner;
import com.silentquot.socialcomponents.model.Message;
import com.silentquot.socialcomponents.utils.LogUtil;


public class ChatManager   extends FirebaseListenersManager {

    private static ChatManager instance;
    private final ChatInteractor chatInteractor;
    private Context context;


    public static ChatManager getInstance(Context context) {
    if (instance == null) {
        instance = new ChatManager(context);
    }

    return instance;
}

    private ChatManager(Context context) {
        this.context = context;
       chatInteractor = ChatInteractor.getInstance(context);
    }
    private  static ValueEventListener chatEventListener = null ;

    public  void getAllMessages(Context activityContext ,Chatlist chatitem, OnMessageListChangesListner onMessageListChangesListner) {
//        if (chatEventListener ==null) {
//             chatEventListener = chatInteractor.getAllMessages(chatitem, onMessageListChangesListner);
//        //    addListenerToMap(activityContext, chatEventListener);
//        }
//        else {
            closeListeners(activityContext);
            chatInteractor.removeChatListner();
            chatEventListener = chatInteractor.getAllMessages(chatitem, onMessageListChangesListner);
             addListenerToMap(activityContext, chatEventListener);
      //  }
    }


    public void getAllChats(Context activityContext,String currentUserId, final OnChatListChangeListner onChatListChangeListner) {
        chatInteractor.getAllChats(currentUserId , onChatListChangeListner);
        LogUtil.logDebug("TAG", "Offline  - ChatManager, getAllChat  ");

    }


    public void readAllChats( Context activityContext,String currentUserId, OnChatListChangeListner onChatListChangeListner) {

            ValueEventListener valueEventListener = chatInteractor.getNewChats(currentUserId, onChatListChangeListner);
            addListenerToMap(activityContext, valueEventListener);

    }

    public  void getSigngleChatItem(String currentuserId  ,String chatId , OnObjectChangedListener<Chatlist> onObjectChangedListener)
    {
        chatInteractor.getSingleChatItem( currentuserId, chatId , onObjectChangedListener);
    }

public  void getSingleChatItemOffline(String currentuserId  ,String chatId , OnObjectChangedListener<Chatlist> onObjectChangedListener)
{
    chatInteractor.getSingleChatItemOffline(currentuserId , chatId , onObjectChangedListener);
}
    public void sendTextMessage(String sender, String receiver, String messageText, String messageType, OnSendMessageListner onSendMessageListner) {
    chatInteractor.sendTextMessage(sender  , receiver , messageText , messageType ,"newmsg", onSendMessageListner);

    }

    public void sendImageMessage(Message message, Uri imageUri, OnSendMessageListner onSendMessageListner) {

    chatInteractor.sendImageMessage(message , imageUri , onSendMessageListner);

    }



}
