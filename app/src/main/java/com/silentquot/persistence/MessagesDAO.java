package com.silentquot.persistence;

import com.silentquot.socialcomponents.model.Message;

import java.util.List;


public interface MessagesDAO {
    public Message insert(Message message);
    public Message get(int id);
    public boolean delete(int id);
    public void deleteAllData();
    public List<Message> getAll();
    public List<Message> getAllConversation(String idConversation);
    boolean isMsgIdExist(String msgid);
    public void updateIsseen(Message message  , OnUpdateComplete onUpdateComplete);
    public void updateMessage(Message message , OnUpdateComplete onUpdateComplete);
    public  void updateStatus(Message message , OnUpdateComplete onUpdateComplete);
}
