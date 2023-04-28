import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

public class WorkerThread extends Thread{
    int wid;


    ObjectOutputStream out = null;
    ObjectInputStream in = null;
    WorkerThread( int wid, Socket socket){

        this.wid = wid;

        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){



        try{
            String host = "localhost";
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