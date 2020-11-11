package com.silentquot.Model;
import java.util.Date;

public class Post  extends com.silentquot.Fragments.PostId {
    private String Description;
    private String UserId;
    private String PostType;
    private Date TimeStamp;
    private String ImageUrl;
    private  String Type;
    private String CollabPostType;
    private  String CollabImage;
    private String CollabText;
    private String Id;
    public Post() {

    }

    public Post(String CollabImage , String CollabPostType , String CollabText , String description, String imageUrl, String postType, Date timeStamp, String Type, String userId    ) {
        ImageUrl = imageUrl;
        Description = description;
        UserId = userId;
        PostType = postType;
        TimeStamp = timeStamp;
        this.CollabImage=CollabImage;
        this.CollabPostType=CollabPostType;
        this.CollabText=CollabText;
        this.Type=Type;


    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String UserId) {
        this.UserId = UserId;
    }

    public String getPostType() {
        return PostType;
    }

    public void setPostType(String postType) {
        PostType = postType;
    }

    public Date getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(Date TimeStamp) {
        this.TimeStamp = TimeStamp;
    }


    public String getType() {
        return Type;
    }

    public void setType(String Type) {
        this.Type = Type;
    }

    public String getCollabPostType() {
        return CollabPostType;
    }

    public void setCollabPostType(String collabPostType) {
        CollabPostType = collabPostType;
    }

    public String getCollabImage() {
        return CollabImage;
    }

    public void setCollabImage(String collabImage) {
        CollabImage = collabImage;
    }

    public String getCollabtext() {
        return CollabText;
    }

    public void setCollabtext(String Collabtext) {
        this.CollabText = Collabtext;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }
}
