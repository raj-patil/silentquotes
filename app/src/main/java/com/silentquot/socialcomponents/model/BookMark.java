package com.silentquot.socialcomponents.model;

import java.util.Calendar;

public class BookMark {
    private String id;
    private String authorId;
    private long createdDate;
        private String postType;

    public BookMark() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
    }

    public BookMark(String authorId , String postType) {
        this.authorId = authorId;
        this.createdDate = Calendar.getInstance().getTimeInMillis();
        this.postType=postType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public String getPostType() {
        return postType;
    }

    public void setPostType(String postType) {
        this.postType = postType;
    }
}
