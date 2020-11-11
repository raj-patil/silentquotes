package com.silentquot.persistence;

import com.silentquot.Model.Chatlist;

import java.util.List;

/**
 * Created by Eslem on 07/10/2014.
 */
public interface ConversationDAO {
    public Chatlist insert(Chatlist conversation);
    public Chatlist get(String id);
  //  public Chatlist getFromUser(String idUser);
    public boolean delete(int id);
    public List<Chatlist> getAll();
    public void deleteAllData();
    public void update(Chatlist conversation);
    public String getFimgurl(String chatId);
    public void updateLimg(String LimgPath, String fimgUrl , String chatid);
}
