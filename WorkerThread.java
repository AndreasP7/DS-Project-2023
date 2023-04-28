import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

public class WorkerThread extends Thread{
    int wid;
    Socket socket;
    WorkerThread( int wid, Socket socket){
        this.socket = socket;
        this.wid = wid;
    }

    public void run(){
        ObjectOutputStream out = null;
        ObjectInputStream in = null;


        try{
            String host = "localhost";

            out = new ObjectOutputStream(this.socket.getOutputStream());


            int results = 2;

            out.writeObject(results);
            out.flush();



        }catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        finally {
            try {

                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

    }
    
}