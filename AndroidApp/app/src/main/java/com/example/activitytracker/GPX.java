
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

    String fileName;

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

    public GPX(String path, String fileName , String username){
        this.path = path;
        this.uid = uid;
        this.fileName = fileName;
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

    public void setFileName(String name){
        this.fileName = name;
    }

    public String getFileName(){
        return this.fileName;

    }


    @Override
    public String toString() {
        String gpxText = "";
        Double time = results.get("totalTime");
        Double speed = results.get("averageSpeed");
        Double distance = results.get("totalDistance");
        Double elevation = results.get("totalElevation");
        String timeString = "";

        int seconds = time.intValue();
        int hours = seconds/3600;
        int minutes = (seconds%3600)/60;
        seconds = seconds%60;




        gpxText = String.format("File Name: %s\n" +
                "Total Time: %d h %d m %d s\n"+
                "Average Speed: %.2f km/h\n"+
                "Total Distance: %.2f km\n"+
                "Total Elevation: %.2f m\n"

                ,this.fileName,
                hours,
                minutes,
                seconds,
                speed,
                distance,
                elevation);


        return gpxText;
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

