import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

public class WorkerThread extends Thread{
    int wid;


    ObjectOutputStream out = null;
    ObjectInputStream in = null;
    WorkerThread( int wid, Socket socket, ObjectOutputStream out){

        this.wid = wid;
        this.out = out;




    }
    public WorkerThread() {
        super();
    }

    public void run(){

        try{
            String host = "localhost";


            this.out.writeInt(2);
            this.out.flush();


        }catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
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