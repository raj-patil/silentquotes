/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.adapters.holders;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.silentquot.R;
import com.silentquot.socialcomponents.enums.ConnectionState;
import com.silentquot.socialcomponents.managers.ConnecteManager;
import com.silentquot.socialcomponents.managers.ProfileManager;
import com.silentquot.socialcomponents.managers.listeners.OnObjectChangedListener;
import com.silentquot.socialcomponents.managers.listeners.OnObjectChangedListenerSimple;
import com.silentquot.socialcomponents.model.Profile;
import com.silentquot.socialcomponents.utils.GlideApp;
import com.silentquot.socialcomponents.utils.ImageUtil;
import com.silentquot.socialcomponents.views.ConnectButton;

public class ConnectionViewHolder extends RecyclerView.ViewHolder {
    public static final String TAG = UserViewHolder.class.getSimpleName();

    private Context context;
    private ImageView photoImageView;
    private TextView nameTextView;
    private ConnectButton acceptButton;
    private ConnectButton cancelButton;
    private ConnectButton messageButton;

    private ProfileManager profileManager;

    private Activity activity;

    public ConnectionViewHolder(View view, final ConnectionViewHolder.Callback callback, Activity activity) {
        super(view);
        this.context = view.getContext();
        this.activity = activity;
        profileManager = ProfileManager.getInstance(context);

        nameTextView = view.findViewById(R.id.nameTextView);
        photoImageView = view.findViewById(R.id.photoImageView);
        acceptButton = view.findViewById(R.id.connectButton);
        cancelButton=view.findViewById(R.id.cancelButton);
        messageButton=view.findViewById(R.id.messageButton);


        view.setOnClickListener(v -> {
            int position = getAdapterPosition();
            if (callback != null && position != RecyclerView.NO_POSITION) {
                callback.onItemClick(getAdapterPosition(), v);
            }
        });
        if (acceptButton!=null)
        acceptButton.setOnClickListener(v -> {
            if (callback != null) {
                callback.onAcceptButtonClick(getAdapterPosition(), acceptButton);
            }
        });
        if (cancelButton!=null)
        cancelButton.setOnClickListener(v -> {
            if (callback != null) {
                callback.onCancelButtonClick(getAdapterPosition(), cancelButton);
            }
        });
        if (messageButton!=null)
        messageButton.setOnClickListener(v -> {
            if (callback != null) {
                callback.onMessageButtonClick(getAdapterPosition(), messageButton);
            }
        });
    }

    public void bindData(String profileId) {
        profileManager.getProfileSingleValue(profileId, createProfileChangeListener());
    }

    public void bindData(Profile profile) {
        fillInProfileFields(profile);
    }

    private OnObjectChangedListener<Profile> createProfileChangeListener() {
        return new OnObjectChangedListenerSimple<Profile>() {
            @Override
            public void onObjectChanged(Profile obj) {
                fillInProfileFields(obj);
            }
        };
    }

    protected void fillInProfileFields(Profile profile) {

        if (profile.getUsername()!=null)
        nameTextView.setText(profile.getUsername());

        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId != null) {
            if (!currentUserId.equals(profile.getId())) {
                ConnecteManager.getInstance(context).checkConnectState(currentUserId, profile.getId(), connectionState -> {
                    acceptButton.setVisibility(View.VISIBLE);
                    acceptButton.setState(connectionState);
                    if (connectionState==ConnectionState.USER_SEND_CONNECT_REQUEST_TO_ME)
                    {
                        cancelButton.setClickable(true);
                        cancelButton.setVisibility(View.VISIBLE);
                        cancelButton.setEnabled(true);
                        cancelButton.setText(R.string.button_title_cancel);
                        cancelButton.setBackground(ContextCompat.getDrawable(context, R.drawable.follow_button_dark_bg));
                        cancelButton.setTextColor(ContextCompat.getColor(context, R.color.white));
                    }
                    if (connectionState==ConnectionState.CONNECTED_EACH_OTHER){
                        cancelButton.setVisibility(View.GONE);
                        cancelButton.setClickable(false);
                        cancelButton.setEnabled(false);

                        acceptButton.setVisibility(View.GONE);
                        acceptButton.setClickable(false);
                        acceptButton.setEnabled(false);

                        messageButton.setEnabled(true);
                        messageButton.setVisibility(View.VISIBLE);
                        messageButton.setClickable(true);
                        messageButton.setText(R.string.button_title_message);
                        messageButton.setBackground(ContextCompat.getDrawable(context, R.drawable.follow_button_light_bg));
                        messageButton.setTextColor(ContextCompat.getColor(context, R.color.primary_dark_text));
                    }

                });
            } else {
                acceptButton.setState(ConnectionState.MY_PROFILE);
            }
        } else {
            acceptButton.setState(ConnectionState.NO_ONE_CONNECTED);
        }

        if (profile.getPhotoUrl() != null) {
            ImageUtil.loadImage(GlideApp.with(activity), profile.getPhotoUrl(), photoImageView);
        }
    }

    public interface Callback {
        void onItemClick(int position, View view);
        void onAcceptButtonClick(int position , ConnectButton acceptButton);
        void onCancelButtonClick(int position , ConnectButton cancelButton);
        void onMessageButtonClick(int position , ConnectButton messageButton);
    }

}