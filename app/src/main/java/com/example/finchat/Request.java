package com.example.finchat;

public class Request {

    String from_user_name;
    String request_type;

    public Request(String from_user_name, String request_type) {
        this.from_user_name = from_user_name;
        this.request_type = request_type;
    }

    public Request(){

    }

    public String getFrom_user_name() {
        return from_user_name;
    }

    public void setFrom_user_name(String from_user_name) {
        this.from_user_name = from_user_name;
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }
}
