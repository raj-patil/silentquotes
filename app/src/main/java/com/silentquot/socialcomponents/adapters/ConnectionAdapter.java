/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.silentquot.R;
import com.silentquot.socialcomponents.adapters.holders.ConnectionViewHolder;
import com.silentquot.socialcomponents.managers.ProfileManager;
import com.silentquot.socialcomponents.managers.listeners.OnObjectChangedListenerSimple;
import com.silentquot.socialcomponents.model.Connection;
import com.silentquot.socialcomponents.model.Profile;

import java.util.ArrayList;
import java.util.List;

public class ConnectionAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = SearchUsersAdapter.class.getSimpleName();

    private List<Connection> itemsList = new ArrayList<>();

    private ConnectionViewHolder.Callback callback;
    private Activity activity;

    public ConnectionAdapter(Activity activity) {
        this.activity = activity;
    }

    public void setCallback(ConnectionViewHolder.Callback callback) {
        this.callback = callback;
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ConnectionViewHolder(inflater.inflate(R.layout.connection_item_list_view, parent, false),
                callback, activity);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ProfileManager.getInstance(activity.getApplicationContext()).getProfileSingleValue(itemsList.get(position).getId(), new OnObjectChangedListenerSimple<Profile>() {
            @Override
            public void onObjectChanged(Profile obj) {
                ((ConnectionViewHolder) holder).bindData(obj);
            }
        });

    }

    public void setList(List<Connection> list) {
        itemsList.clear();
        itemsList.addAll(list);
        notifyDataSetChanged();
    }

    public void updateItem(int position) {

        Connection profile = getItemByPosition(position);
        ProfileManager.getInstance(activity.getApplicationContext()).getProfileSingleValue(profile.getId(), new OnObjectChangedListenerSimple<Profile>() {
            @Override
            public void onObjectChanged(Profile updatedProfile) {
                itemsList.set(position , profile);
                notifyItemChanged(position);
            }
        });
    }

    public Connection getItemByPosition(int position) {
        return itemsList.get(position);
    }
}
