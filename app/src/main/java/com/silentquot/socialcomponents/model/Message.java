/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.model;

import android.net.Uri;

import com.silentquot.socialcomponents.enums.ItemType;

import java.io.Serializable;

public class Message  implements Serializable, LazyLoading  {
    public String msgid;
    private String sender;
    private String receiver;
    private String message;
    private boolean isseen;
    String msgtype;
    String chat_key;
    String status;
    Uri localImageUri;
    private  String thubmnail;

    public String getThubmnail() {
        return thubmnail;
    }

    public void setThubmnail(String thubmnail) {
        this.thubmnail = thubmnail;
    }

    public Uri getLocalImageUri() {
        return localImageUri;
    }

    public void setLocalImageUri(Uri localImageUri) {
        this.localImageUri = localImageUri;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private  int id;
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }




    public Message(String sender, String receiver, String message, boolean isseen , String msgtype) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isseen = isseen;
        this.msgtype=msgtype;
    }

    public Message(String sender, String receiver, String message, boolean isseen , String chat_key , String msgtype) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isseen = isseen;
        this.msgtype=msgtype;
        this.chat_key=chat_key;
    }

    public Message() {
    }

    public String getMsgid() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public String getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
    }

    public String getChat_key() {
        return chat_key;
    }

    public void setChat_key(String chat_key) {
        this.chat_key = chat_key;
    }

    @Override
    public ItemType getItemType() {
        return null;
    }

    @Override
    public void setItemType(ItemType itemType) {

    }
}
