import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User  {
    String path;
    int port;
    int uid;
    User(String path, int port, int uid){
        this.path = path;
        this.port = port;

    }

    public void run(){
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        Socket requestSocket = null;

        try{

            GPX gpx = new GPX(this.path, this.uid);


            String host = "localhost";
            requestSocket = new Socket(host, this.port);

            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());


            out.writeObject(gpx);
            out.flush();

            gpx = (GPX) in.readObject();
            ;

            System.out.println("Results: ");
            System.out.println("Total Time:" + gpx.getResults().get("totalTime") );
            System.out.println("Average Speed:" + gpx.getResults().get("averageSpeed") );
            System.out.println("Total Distance:" + gpx.getResults().get("totalDistance") );
            System.out.println("Total Elevation:" + gpx.getResults().get("totalElevation") );





            

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

    public static void main(String[] args) {
        new User("gpxs/route2.gpx", 4020, 0).run();


    }
}
