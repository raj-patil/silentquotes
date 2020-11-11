package com.silentquot.dommain;

import java.util.List;


public class Conversation {
    private int id;
    private String idUser;
    private String lastMessage;
    private String userName;
    private List<Message> messagesList;

    public Conversation(int id, String idUser){
        this.id=id;
        this.idUser=idUser;
    }

    public Conversation(String idUser){
        this.idUser=idUser;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public List<Message> getMessagesList() {
        return messagesList;
    }

    public void setMessagesList(List<Message> messagesList) {
        this.messagesList = messagesList;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String toString(){
        return "Conversation id:"+this.id+" with user:"+this.idUser+" "+this.getUserName() ;
    }
}
