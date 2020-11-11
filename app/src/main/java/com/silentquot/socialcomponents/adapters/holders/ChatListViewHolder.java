/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.adapters.holders;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.silentquot.Model.Brodcast;
import com.silentquot.R;
import com.silentquot.socialcomponents.managers.ProfileManager;
import com.silentquot.socialcomponents.managers.listeners.OnObjectChangedListener;
import com.silentquot.socialcomponents.managers.listeners.OnObjectChangedListenerSimple;
import com.silentquot.socialcomponents.model.Profile;
import com.silentquot.socialcomponents.utils.GlideApp;
import com.silentquot.socialcomponents.utils.ImageUtil;

public class ChatListViewHolder extends RecyclerView.ViewHolder{


    private final Activity activity;
    private final ProfileManager profileManager;
    private Context context;
    private ImageView photoImageView;
    private TextView nameTextView;

    public ChatListViewHolder(View view, final ChatListViewHolder.Callback callback, Activity activity) {
        super(view);
        this.context = view.getContext();
        this.activity = activity;
        profileManager = ProfileManager.getInstance(context);
        nameTextView = view.findViewById(R.id.nameTextView);
        photoImageView = view.findViewById(R.id.photoImageView);

        view.setOnClickListener(v -> {
            int position = getAdapterPosition();
            if (callback != null && position != RecyclerView.NO_POSITION) {
                callback.onItemClick(getAdapterPosition(), v);
            }
        });


    }

    public void bindData(String profileId) {
        profileManager.getProfileSingleValue(profileId, chatItemChangeListener());
    }


    private OnObjectChangedListener<Brodcast> brodCastItemChangelistner() {
        return new OnObjectChangedListenerSimple<Brodcast>() {
            @Override
            public void onObjectChanged(Brodcast obj) {
                fillChatItemInUiBrodCastField(obj);
            }
        };
    }



    private OnObjectChangedListener<Profile> chatItemChangeListener() {
        return new OnObjectChangedListenerSimple<Profile>() {
            @Override
            public void onObjectChanged(Profile obj) {
                fillChatItemInUiField(obj);
            }
        };
    }

    private void fillChatItemInUiBrodCastField(Brodcast brodcast) {
        if (brodcast.getId()!=null)
            nameTextView.setText(brodcast.getId());

        if (brodcast.getImageURL() != null) {
            ImageUtil.loadImage(GlideApp.with(activity), brodcast.getImageURL(), photoImageView);
        }

    }

    public  void fillChatItemInUiField(Profile profile)
    {
        if (profile.getUsername()!=null)
            nameTextView.setText(profile.getUsername());

        if (profile.getPhotoUrl() != null) {
            ImageUtil.loadImage(GlideApp.with(activity), profile.getPhotoUrl(), photoImageView);
        }
    }

    public void bindBrodcastData(String id) {
        profileManager.getBrodCastSingleValue(id, brodCastItemChangelistner() );

    }


    public interface Callback {
        void onItemClick(int position, View view);
    }
}
