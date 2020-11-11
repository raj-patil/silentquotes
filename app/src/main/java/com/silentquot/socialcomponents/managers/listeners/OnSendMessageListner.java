/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.managers.listeners;

import com.silentquot.socialcomponents.model.Message;

public interface OnSendMessageListner {


        void onBeforeMessageSent(Message message);

        void onMessageSentComplete(Message message);
}
