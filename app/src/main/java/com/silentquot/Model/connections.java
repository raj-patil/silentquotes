package com.silentquot.Model;

public class connections {
    private String id;
    private String date;

    public  connections()
    {

    }

    public connections(String id, String date) {
        this.id = id;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }



}
