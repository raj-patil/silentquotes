/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.model;

import java.util.ArrayList;
import java.util.List;

public class MessageListResult {

    boolean isMoreDataAvailable;
    List<Message> message = new ArrayList<>();
    long lastItemCreatedDate;

    public boolean isMoreDataAvailable() {
        return isMoreDataAvailable;
    }

    public void setMoreDataAvailable(boolean moreDataAvailable) {
        isMoreDataAvailable = moreDataAvailable;
    }

    public List<Message> getMessage() {
        return message;
    }

    public void setMessage(List<Message> message) {
        this.message = message;
    }

    public long getLastItemCreatedDate() {
        return lastItemCreatedDate;
    }

    public void setLastItemCreatedDate(long lastItemCreatedDate) {
        this.lastItemCreatedDate = lastItemCreatedDate;
    }
    


    
}

