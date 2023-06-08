package com.example.activitytracker;

import java.io.Serializable;

public class Request implements Serializable {

    String type;
    String username;
    GPX gpx;

    public Request (String type, String username){
        this.type = type;
        this.username = username;
    }

    public Request (String type, String username, GPX gpx){
        this.type = type;
        this.username = username;
        this.gpx = gpx;
    }

    public String getType(){
        return this.type;
    }

    public void setType(String type){
        this.type = type;
    }

    public GPX getGPX(){
        return this.gpx;
    }

    public void setGPX(GPX gpx){
        this.gpx = gpx;
    }

    public String getUsername(){
        return this.username;
    }

    public void setUsername(String username){
        this.username = username;
    }



}
