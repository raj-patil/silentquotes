/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.silentquot.Model.Chatlist;
import com.silentquot.R;
import com.silentquot.socialcomponents.adapters.holders.ChatListViewHolder;
import com.silentquot.socialcomponents.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListViewHolder>{

    private  int USER = 1;
    private  int BRODCAST = 2;

    private List<Chatlist> itemsList = new ArrayList<>();

    private ChatListViewHolder.Callback callback;
    private Activity activity;

    public ChatListAdapter(Activity activity) {
        this.activity = activity;
    }

    public void setCallback(ChatListViewHolder.Callback callback) {
        this.callback = callback;
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }

    @NonNull
    @Override
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ChatListViewHolder(inflater.inflate(R.layout.chatlist_item_list_view, parent, false),
                callback, activity);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListViewHolder holder, int position) {

        int CHAT_TYPE = getItemViewType(position);

        if (CHAT_TYPE== USER) {
            LogUtil.logDebug("TAG", "OnBindChatList, onbind - " + getItemByPosition(position).getId());
            holder.bindData(getItemByPosition(position).getId());
        }
        else {
           holder.bindBrodcastData(getItemByPosition(position).getId());
        }
    }

    public void setList(List<Chatlist> list) {
        itemsList.clear();

        if (list!=null)
        itemsList.addAll(list);
        notifyDataSetChanged();
    }

    public int getItemViewType(int position) {
      String chatlist = getItemByPosition(position).getChat_type();
      if (chatlist.equals("brodcast"))
      {
          return BRODCAST;
      }else
      {
          return USER;
      }

    }

    public Chatlist getItemByPosition(int position) {
        return itemsList.get(position);
    }


}
