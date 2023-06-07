package com.example.activitytracker;


import android.net.Uri;

import java.io.*;
import java.net.URI;
import java.util.Map;

public class GPX implements Serializable {
    String path;
    File file;
    Map<String, Double> results;

    int uid;

    String text;

    GPX(String path, int uid){
        this.path = path;
        this.file = new File(path);
        this.uid = uid;
        this.text = this.ReadFile();


    }
    void setResults(Map<String, Double> r){
        this.results = r;
    }
    Map<String, Double> getResults(){
        return this.results;
    }
    public String getText(){return this.text;}

    public void setText(String text){this.text = text;}

    int getUid(){
        return this.uid;
    }

    String ReadFile(){
        String text = "";
        try{

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            while(line!=null){
                text += line;
                line = br.readLine();
            }

            br.close();


        }

        catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        catch(IOException e){
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return text;

    }




}
