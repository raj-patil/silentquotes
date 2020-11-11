/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.main.chatlist;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.silentquot.Model.Chatlist;
import com.silentquot.R;
import com.silentquot.socialcomponents.adapters.ChatListAdapter;
import com.silentquot.socialcomponents.adapters.holders.ChatListViewHolder;
import com.silentquot.socialcomponents.main.base.BaseFragment;
import com.silentquot.socialcomponents.main.message.MessageActivity;
import com.silentquot.socialcomponents.managers.ChatManager;
import com.silentquot.socialcomponents.utils.LogUtil;

import java.util.List;

public class ChatListFragment  extends BaseFragment<ChatListView, ChatListPresenter>
        implements ChatListView {


    private RecyclerView recyclerView;
    private ChatListAdapter chatListAdapter;
    private int selectedItemPosition = RecyclerView.NO_POSITION;
    private String currentUserId;


    public ChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public ChatListPresenter createPresenter() {
        if (presenter == null) {
            return new ChatListPresenter(getActivity());
        }
        return presenter;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        //progressBar = view.findViewById(R.id.progressBar);
        recyclerView = view.findViewById(R.id.recycler_view);
        // emptyListMessageTextView = view.findViewById(R.id.emptyListMessageTextView);
        //emptyListMessageTextView.setText(getResources().getString(R.string.empty_user_search_message));
        currentUserId = FirebaseAuth.getInstance().getUid();
        initRecyclerView();
        return view;
    }

    private void initRecyclerView() {

        chatListAdapter = new ChatListAdapter(getActivity());
        chatListAdapter.setCallback(new ChatListViewHolder.Callback() {


            @Override
            public void onItemClick(int position, View view) {
                Chatlist chatitem = chatListAdapter.getItemByPosition(position);
                presenter.onChatItemClick(chatitem.getId());
                Toast.makeText(getContext() ,"ClickOn Item" , Toast.LENGTH_SHORT).show();


            }
        });


        recyclerView.setAdapter(chatListAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
                ((LinearLayoutManager) recyclerView.getLayoutManager()).getOrientation()));

        loadChats1();
    }



    public void loadChats1() {

        ChatManager chatManager=ChatManager.getInstance(getContext());
        if (!presenter.checkInternetConnection()) {
            chatManager.getAllChats(getContext(),currentUserId, chatList ->
            {
                setChatList(chatList);
                LogUtil.logDebug("TAG", "Offline  - chatlistset, ReadAllChats - setAll Chat " + chatList);
            });
        }else {
            chatManager.readAllChats(getContext() ,currentUserId , chatList -> {

                    setChatList(chatList);
                    LogUtil.logDebug("TAG", "chatlistset, ReadAllChats - setAll Chat " + chatList);
            });
        }
    }

    @Override
    public void setChatList(List<Chatlist> list) {
        chatListAdapter.setList(list);
    }

    @Override
    public void openMessageActivity(Chatlist chatItem) {
        Intent intent = new Intent(getContext(), MessageActivity.class);
        intent.putExtra(MessageActivity.CHAT_EXTRA_KEY, chatItem);
        startActivity(intent);
    }
}
