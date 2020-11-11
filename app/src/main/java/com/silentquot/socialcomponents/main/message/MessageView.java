/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.main.message;

import android.net.Uri;

import com.silentquot.socialcomponents.main.base.BaseView;
import com.silentquot.socialcomponents.model.Message;

import java.util.List;

public interface MessageView extends BaseView {

    void setName(String username);

    void setProfilePhoto(String photoUrl);

    void openMessageActivity();

    void setMessageList(List<Message> message);

    void addNewMessageList(List<Message> message);
    
    String getMessageText();
      void clearMessageText();

    int getMessageType();

    String getReceiver();

    void refreshNewItem();

    void addnewIten(Message message);

    Uri getImageUri();

    void setCacheSize(int size);
}
