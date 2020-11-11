/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.main.imageDetail;

import android.content.Context;
import android.net.Uri;

import com.silentquot.socialcomponents.main.pickImageBase.PickImagePresenter;
import com.silentquot.socialcomponents.main.pickImageBase.PickImageView;
import com.silentquot.socialcomponents.managers.ChatManager;
import com.silentquot.socialcomponents.managers.listeners.OnSendMessageListner;
import com.silentquot.socialcomponents.model.Message;
import com.silentquot.socialcomponents.utils.LogUtil;

public class SendImagePresenter<V extends SendImageView & PickImageView> extends PickImagePresenter<V> {

    ChatManager chatManager ;

    public SendImagePresenter(Context context) {
        super(context);
        chatManager =ChatManager.getInstance(context);
    }

    public void onImageSendClick() {
        LogUtil.logDebug(TAG, "OnSendImageCall, ");
        ifViewAttached(view -> {
            sendImage(view.getImageUri());


            LogUtil.logDebug(TAG, "OnSendImageCallWithImageUri");
        });

    }

    // sender , receiver , messagetext , isseen , message type , chatkey
    private void sendImage(Uri imageUri) {
        ifViewAttached(view -> {
            if (imageUri!=null)
            {
                LogUtil.logDebug(TAG, "ImageUri" + imageUri);
                Message message = new Message();
                message.setMsgtype("Image");
                message.setSender(getCurrentUserId());
                message.setReceiver(view.getReceiver());
                chatManager.sendImageMessage(message, imageUri, new OnSendMessageListner() {
                    @Override
                    public void onBeforeMessageSent(Message message) {

                    }

                    @Override
                    public void onMessageSentComplete(Message message) {

                    }
                });

            }
        });


    }
}
