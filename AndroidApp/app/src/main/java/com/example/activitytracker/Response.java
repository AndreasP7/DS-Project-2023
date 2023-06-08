package com.example.activitytracker;

import java.io.Serializable;
import java.util.Map;

public class Response implements Serializable {

    GPX gpx;
    String username;
    String type;
    Map<String, Double>  results;

    public Response (String type, String username, Map<String, Double>  results){
        this.type = type;
        this.username = username;
        this.results = results;
    }

    public Response (String type, String username, GPX gpx){
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

    public Map<String, Double> getResults(){
        return this.results;
    }

    public void setResults(Map<String, Double> results){
        this.results = results;
    }
}
