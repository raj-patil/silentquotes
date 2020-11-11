package com.silentquot.Model;

public class BrodcastChat {
    private String message;
    private String msgtype;


    public BrodcastChat(String message, String msgtype) {
        this.message = message;
        this.msgtype = msgtype;
    }
    public BrodcastChat() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
    }
}
