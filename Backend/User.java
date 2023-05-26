import java.io.*;
import java.net.*;
import java.util.*;

public class User  {

    int port;
    int uid;

    String host;

    Map<String, Double> results;

    List <GPX> gpxList = new ArrayList<>();
    User( int port, int uid, String host){

        this.port = port;
        this.uid = uid;
        this.host = host;

    }


    public void addGPX(String path){
        GPX gpx = new GPX(path, this.uid); // new gpx file for the input path
        gpxList.add(gpx);

    }

    public void run(){

        if (gpxList.isEmpty()){
            System.out.println("Please add a GPX file and try again");
        }
        else{
            ObjectOutputStream out = null;
            ObjectInputStream in = null;
            Socket requestSocket = null;
            try{

                int counter =1;
                for (GPX gpx : gpxList){

                    requestSocket = new Socket(this.host, this.port);

                    out = new ObjectOutputStream(requestSocket.getOutputStream());
                    in = new ObjectInputStream(requestSocket.getInputStream());


                    out.writeObject(gpx);//send gpx to server
                    out.flush();

                    gpx = (GPX) in.readObject();//get gpx with results from server


                    results = gpx.getResults();
                    System.out.println("\n------------------------------------\n");
                    System.out.printf(String.format("Results of GPX %d :\n", counter));
                    System.out.println("Total Time:" + results.get("totalTime") );
                    System.out.println("Average Speed:" + results.get("averageSpeed") );
                    System.out.println("Total Distance:" + results.get("totalDistance") );
                    System.out.println("Total Elevation:" + results.get("totalElevation") );

                    try {
                        FileWriter myWriter = new FileWriter(String.format("results/results_user%d_gpx%d.txt",this.uid,counter));
                        myWriter.write("Total Time:" + results.get("totalTime")+"\n");
                        myWriter.write("Average Speed:" + results.get("averageSpeed")+"\n");
                        myWriter.write("Total Distance:" + results.get("totalDistance")+"\n");
                        myWriter.write("Total Elevation:" + results.get("totalElevation")+"\n");
                        myWriter.close();

                    } catch (IOException e) {
                        System.out.println("An error occurred.");
                        e.printStackTrace();
                    }

                    counter++;

                }


            }catch (UnknownHostException unknownHost) {
                System.err.println("You are trying to connect to an unknown host!");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }catch(ClassNotFoundException e){
                throw new RuntimeException(e);
            }
            finally {
                try {
                    in.close();
                    out.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }

        }

    }

    public static void main(String[] args) {

        String [] paths ;
        String host = "";
        int id=0;
        int port=0;


        try{
            Properties prop=new Properties();
            FileInputStream ip= new FileInputStream("config/config_user.properties");

            prop.load(ip);

            host = prop.getProperty("host");
            port = Integer.parseInt(prop.getProperty("port"));
            id = Integer.parseInt(prop.getProperty("id"));
            paths = prop.getProperty("paths").split("%");//each user can send multiple gpx files, each representing route

            System.out.printf(String.format("Started User %d. Connected to Master-Server %s \n",id, host));
            User u =new User(port, id, host);
            for (String path : paths){
                if (path != null){
                    System.out.println("Added gpx file in path: "+path);
                    u.addGPX(path);
                }

            }
            u.run();


        }catch(IOException e){
            e.printStackTrace();

        }







    }
}
