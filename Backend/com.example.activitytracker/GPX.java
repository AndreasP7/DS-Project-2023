
package com.example.activitytracker;
import java.io.*;
import java.util.Map;


public class GPX implements Serializable {
    String path;
    File file;
    Map<String, Double> results;

    int uid;

    String username;

    String text;

    public GPX(String path , int uid){
        this.path = path;
        this.uid = uid;
        if( !path.equals("")){
            this.file = new File(path);
            this.text = this.ReadFile();

        }
    }

    public GPX(String path , String username){
        this.path = path;
        this.username = username;
        if( !path.equals("")){
            this.file = new File(path);
            this.text = this.ReadFile();

        }
    }

    public void setResults(Map<String, Double> r){
        this.results = r;
    }
    public Map<String, Double> getResults(){
        return this.results;
    }
    public String getText(){return this.text;}

    public void setText(String text){this.text = text;}

    public int getUid(){
        return this.uid;
    }

    public void setUid(int uid){ this.uid = uid;}

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

