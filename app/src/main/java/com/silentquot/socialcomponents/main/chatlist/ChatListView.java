/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.main.chatlist;

import com.silentquot.Model.Chatlist;
import com.silentquot.socialcomponents.main.base.BaseFragmentView;

import java.util.List;

public interface ChatListView extends BaseFragmentView {
    void setChatList(List<Chatlist> list);
    void openMessageActivity(Chatlist chatItem);
}
