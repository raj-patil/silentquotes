package com.silentquot.socialcomponents.model;

public class Connection {

    private  String id;

    public Connection() {
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    private String requestType;

    public String getId() {
        return id;
    }

    public void setId(String id ) {
        this.id = id;

    }


    public Connection(String id, String requestType) {
        this.id = id;
        this.requestType =requestType;
    }
}
