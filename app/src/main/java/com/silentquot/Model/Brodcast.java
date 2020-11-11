package com.silentquot.Model;

public class Brodcast {

    private String id;
    private String imageURL;
    private String admin;

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }




    public Brodcast(String id , String imageURL) {
        this.id = id;
        this.imageURL = imageURL;


    }

    public Brodcast() {
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
