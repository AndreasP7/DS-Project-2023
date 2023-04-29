import java.io.*;
import java.net.*;
public class User extends Thread {
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
            String host = "localhost";
            requestSocket = new Socket(host, this.port);

            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());
            GPX gpx = new GPX(this.path, this.uid);
            out.writeObject(gpx);
            out.flush();

            //gpx = (GPX) in.readObject();

            System.out.println("Result " + gpx.getResults());

            out.writeObject(this.path);
            out.flush();
            

        }catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }finally {
            try {
                in.close();
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new User("gpxs/route1.gpx", 4020, 0);

    }
}
