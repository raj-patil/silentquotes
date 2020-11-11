package com.silentquot.Model;

public class Chat {




    public String msgid;
    private String sender;
    private String receiver;
    private String message;
    private boolean isseen;
    String msgtype;
    String chat_key;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private  int id;


    public String getMsgid() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }

    public String getChat_key() {
        return chat_key;
    }

    public void setChat_key(String chat_key) {
        this.chat_key = chat_key;
    }



    public Chat(String sender, String receiver, String message, boolean isseen , String msgtype) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isseen = isseen;
        this.msgtype=msgtype;
    }

    public Chat(String sender, String receiver, String message, boolean isseen , String chat_key , String msgtype) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isseen = isseen;
        this.msgtype=msgtype;
        this.chat_key=chat_key;
    }

    public Chat() {
    }

    public String getMsgtype(){return  msgtype;}
    public  void setMsgtype(String msgtype){ this.msgtype=msgtype; }

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
}
