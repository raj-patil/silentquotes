package com.silentquot.Model;

public class Request  {
    public String id;
    public  String request_type;

    public Request(String id , String request_type) {
        this.id = id;
        this.request_type=request_type;
    }

    public Request() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }


}
