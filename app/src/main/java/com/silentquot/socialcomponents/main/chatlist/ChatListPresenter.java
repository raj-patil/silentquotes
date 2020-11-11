/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.main.chatlist;

import android.app.Activity;

import com.google.firebase.auth.FirebaseAuth;
import com.silentquot.Model.Chatlist;
import com.silentquot.socialcomponents.main.base.BasePresenter;
import com.silentquot.socialcomponents.managers.ChatManager;
import com.silentquot.socialcomponents.managers.ProfileManager;
import com.silentquot.socialcomponents.managers.listeners.OnObjectChangedListener;
import com.silentquot.socialcomponents.utils.LogUtil;

import java.util.List;

public class ChatListPresenter  extends BasePresenter<ChatListView> {

    private final ChatManager chatManager;
    private String currentUserId;
    private Activity activity;
    private ProfileManager profileManager;

    public ChatListPresenter(Activity activity) {
        super(activity);

        this.activity = activity;
        chatManager = ChatManager.getInstance(context);
        currentUserId = FirebaseAuth.getInstance().getUid();
        profileManager = ProfileManager.getInstance(context.getApplicationContext());

    }

//
//    public void loadChats() {
//
//        if (!checkInternetConnection()) {
//            chatManager.getAllChats(context,currentUserId, chatList ->
//            {
////                ifViewAttached(view -> {
//                    setChatList(chatList);
//                    LogUtil.logDebug(TAG, "Offline  - chatlistset, ReadAllChats - setAll Chat " + chatList);
////                });
//            });
//        }else {
//            chatManager.readAllChats(context ,currentUserId , chatList -> {
//
//                    ifViewAttached(view -> {
//                        view.setChatList(chatList);
//                        LogUtil.logDebug(TAG, "chatlistset, ReadAllChats - setAll Chat " + chatList);
//                    });
//            });
//        }
//    }

    private void setChatList(List<Chatlist> chatList) {
        ifViewAttached(view -> {

            view.setChatList(chatList);
            LogUtil.logDebug(TAG, "2-Offline  - chatlistset, ReadAllChats - setAll Chat " + chatList);


        });
    }

    public void onChatItemClick(String chatid) {


        chatManager.getSigngleChatItem(getCurrentUserId(), chatid, new OnObjectChangedListener<Chatlist>() {


            @Override
            public void onObjectChanged(Chatlist obj) {
                ifViewAttached(view -> {
                if (obj!=null)
                    view.openMessageActivity(obj);
                });
            }

            @Override
            public void onError(String errorText) {

            }
        });

    }
}
