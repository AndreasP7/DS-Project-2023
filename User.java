import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User  {
    String path;
    int port;
    int uid;

    String host;
    User(String path, int port, int uid, String host){
        this.path = path;
        this.port = port;
        this.uid = uid;
        this.host = host;

    }

    public void run(){
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        Socket requestSocket = null;

        try{

            GPX gpx = new GPX(this.path, this.uid); // new gpx file
            requestSocket = new Socket(this.host, this.port);

            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());


            out.writeObject(gpx);//send gpx to server
            out.flush();

            gpx = (GPX) in.readObject();//get gpx with results from server


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

        String path = "";
        String host = "";
        int id=0;
        int port=0;
        File file = new File("config/config_user.txt");
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            host = br.readLine().split("=")[1];
            port =  Integer.parseInt(br.readLine().split("=")[1]);
            id =  Integer.parseInt(br.readLine().split("=")[1]);
            path = br.readLine().split("=")[1];

            br.close();
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(IOException e){
            throw new RuntimeException(e);
        }
        System.out.printf(String.format("Started User %d. Connected to Master-Server %s \n",id, host));
        new User(path, port, id, host).run();


    }
}
