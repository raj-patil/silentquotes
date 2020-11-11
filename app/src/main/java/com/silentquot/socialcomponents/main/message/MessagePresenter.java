/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.main.message;

import android.content.Context;
import android.net.Uri;

import com.silentquot.Model.Chatlist;
import com.silentquot.persistence.MessageDAOImplSQLiteOpenHelper;
import com.silentquot.persistence.MessagesDAO;
import com.silentquot.persistence.OnUpdateComplete;
import com.silentquot.socialcomponents.main.base.BasePresenter;
import com.silentquot.socialcomponents.managers.ChatManager;
import com.silentquot.socialcomponents.managers.ProfileManager;
import com.silentquot.socialcomponents.managers.listeners.OnMessageListChangesListner;
import com.silentquot.socialcomponents.managers.listeners.OnObjectChangedListenerSimple;
import com.silentquot.socialcomponents.managers.listeners.OnSendMessageListner;
import com.silentquot.socialcomponents.model.Message;
import com.silentquot.socialcomponents.model.MessageListResult;
import com.silentquot.socialcomponents.model.Profile;
import com.silentquot.socialcomponents.utils.LogUtil;

import java.util.List;

public class MessagePresenter extends BasePresenter<MessageView> {

    ChatManager chatManager;
    ProfileManager profileManager;

    public MessagePresenter(Context context) {
        super(context);
        chatManager = ChatManager.getInstance(context);
        profileManager = ProfileManager.getInstance(context);
    }


    public void loaduserprofile(String userid) {
        profileManager.getProfileSingleValue(userid, new OnObjectChangedListenerSimple<Profile>() {
            @Override
            public void onObjectChanged(Profile obj) {
              Profile  profile = obj;
                ifViewAttached(view -> {
                    if (profile != null) {
                        view.setName(profile.getUsername());

                        if (profile.getPhotoUrl() != null) {
                            view.setProfilePhoto(profile.getPhotoUrl());
                        }
                    }


                    view.hideProgress();
                  //  view.setNameError(null);
                });
            }
        });

    }

    public void loadmessages(Chatlist chatitem) {

        if (hasInternetConnection()) {
            MessagesDAO messagesDAO;
            messagesDAO = new MessageDAOImplSQLiteOpenHelper(context);

            if (chatitem.getKey()!=null) {

                ifViewAttached(view -> {
                    List<Message> persistanceMessageList = messagesDAO.getAllConversation(chatitem.getKey());
                    view.setMessageList(persistanceMessageList);
                    //  view.addNewMessageList(persistanceMessageList);
                    if (persistanceMessageList!=null)
                    view.setCacheSize(persistanceMessageList.size());
                    view.refreshNewItem();
                });

                LogUtil.logDebug(TAG, "chatitem is exist " + chatitem.getKey());
                chatManager.getAllMessages(context, chatitem, new OnMessageListChangesListner() {
                    @Override
                    public void onListChanged(MessageListResult result) {
                        ifViewAttached(view -> {
                         List<Message> persistanceMessageList = messagesDAO.getAllConversation(chatitem.getKey());
                          view.setMessageList(persistanceMessageList);
                          //  view.addNewMessageList(persistanceMessageList);
                            if (persistanceMessageList!=null)
                            view.setCacheSize(persistanceMessageList.size());
                            view.refreshNewItem();

                        });
                    }
                    @Override
                    public void onCanceled(String message) {

                    }
                });
            }
            else
            {
                ifViewAttached(view -> {
                    view.setMessageList(null);
                });
            }
        }else
        {
            MessagesDAO messagesDAO;
            messagesDAO = new MessageDAOImplSQLiteOpenHelper(context);
            List<Message> persistanceMessageList = messagesDAO.getAllConversation(chatitem.getKey());
            ifViewAttached(view -> {
                view.setMessageList(persistanceMessageList);
               view.refreshNewItem();
            });
        }
    }

    public void onSendButtonClick() {
        LogUtil.logDebug(TAG, "onsendButtonClick");
        ifViewAttached(view -> {
        int MSG_TYPE =  view.getMessageType();

        if (MSG_TYPE == MessageActivity.TEXT_MESSAGE)
        {

            sendTextMessage();
            LogUtil.logDebug(TAG, "textMessage - send text message called");
        }


        });
    }

    private void sendTextMessage() {
        ifViewAttached(view -> {
            String MessageText = view.getMessageText();
            String Receiver = view.getReceiver();
            String Sender = getCurrentUserId();
            String MessageType= "Text";

            view.clearMessageText();

            chatManager.sendTextMessage(Sender, Receiver, MessageText, MessageType, new OnSendMessageListner() {

                private MessageDAOImplSQLiteOpenHelper messagesDAO= new MessageDAOImplSQLiteOpenHelper(context);

                @Override
                public void onBeforeMessageSent(Message message) {
                    message.setStatus("sending");
                    messagesDAO.insert(message);
                    view.addnewIten(message);
                    view.refreshNewItem();
                }

                @Override
                public void onMessageSentComplete(Message message) {

                    messagesDAO.updateMessage(message, sucuss -> messagesDAO.updateStatus(message, sucuss1 -> {
                        List<Message> persistanceMessageList = messagesDAO.getAllConversation(message.getChat_key());
                        view.setMessageList(persistanceMessageList);
                        view.refreshNewItem();
                    }));

                }
            });
        });

    }



    public void onImageSendClick() {
        LogUtil.logDebug(TAG, "OnSendImageCall, ");
        ifViewAttached(view -> {
            sendImage(view.getImageUri());


            LogUtil.logDebug(TAG, "OnSendImageCallWithImageUri");
        });

    }

    // sender , receiver , messagetext , isseen , message type , chatkey
    private void sendImage(Uri imageUri) {
        ifViewAttached(view -> {
            if (imageUri!=null)
            {
                LogUtil.logDebug(TAG, "ImageUri" + imageUri);
                Message message = new Message();
                message.setMsgtype("Image");
                message.setSender(getCurrentUserId());
                message.setReceiver(view.getReceiver());
                chatManager.sendImageMessage(message, imageUri, new OnSendMessageListner() {
                    private MessageDAOImplSQLiteOpenHelper messagesDAO= new MessageDAOImplSQLiteOpenHelper(context);
                    @Override
                    public void onBeforeMessageSent(Message message) {
                       // message.setMessage(String.valueOf(imageUri));
                        message.setStatus("sending");
                        messagesDAO.insert(message);
                          view.addnewIten(message);
                            view.refreshNewItem();

                    }
                    @Override
                    public void onMessageSentComplete(Message message) {
                         // view.addnewIten(message);
                                messagesDAO.updateMessage(message, new OnUpdateComplete() {
                                    @Override
                                    public void onComplete(boolean sucuss) {
                                        message.setStatus("send");
                                        LogUtil.logDebug(TAG, "Image Mesg Send Complete : "+message.getMessage());
                                        messagesDAO.updateStatus(message, new OnUpdateComplete() {
                                            @Override
                                            public void onComplete(boolean sucuss) {
                                                List<Message> persistanceMessageList = messagesDAO.getAllConversation(message.getChat_key());
                                                view.setMessageList(persistanceMessageList);
                                                view.refreshNewItem();
                                            }
                                        });
                                    }
                                });
                    }
                });

            }
        });
    }
}
