package com.silentquot.Model;

import com.silentquot.socialcomponents.enums.ItemType;
import com.silentquot.socialcomponents.model.LazyLoading;

import java.io.Serializable;

public class Chatlist implements Serializable, LazyLoading {
    public String id;
    public String chat_type;
    public String key;
    public  long timestamp;
    private String username;
    private String fimgurl;
    private String limgurl;
    private String lastmessage;



    public Chatlist(String id , String chat_type ,String key , long timestamp , String lastmessage, String username, String fimgurl, String limgurl) {
        this.id = id;
        this.chat_type = chat_type;
        this.key = key;
        this.timestamp=timestamp;
        this.lastmessage=lastmessage;
        this.username = username;
        this.fimgurl = fimgurl;
        this.limgurl = limgurl;
    }

    public Chatlist() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFimgurl() {
        return fimgurl;
    }

    public void setFimgurl(String fimgurl) {
        this.fimgurl = fimgurl;
    }

    public String getLimgurl() {
        return limgurl;
    }

    public void setLimgurl(String limgurl) {
        this.limgurl = limgurl;
    }


    public String getLastmessage() {
        return lastmessage;
    }

    public void setLastmessage(String lastmessage) {
        this.lastmessage = lastmessage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public  void  setChat_type(String chat_type)
    {
        this.chat_type=chat_type;
    }

    public String getChat_type()
    {
        return  chat_type;
    }

    public  String getKey() {return  key;}

    public  void setKey(String key){ this.key=key; }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long TimeStamp) {
        this.timestamp = TimeStamp;
    }


    @Override
    public ItemType getItemType() {
        return null;
    }

    @Override
    public void setItemType(ItemType itemType) {

    }
}
