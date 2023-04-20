import java.io.*;
import java.net.*;

import javax.management.RuntimeErrorException;
 
public class Client extends Thread {
    int port;
    String path;   
 
    Client(String path, int port) {
        this.port = port;
        this.path = path;
       
    }
 
    public void run() {
        ObjectOutputStream out= null ;
        ObjectInputStream in = null ;
        Socket requestSocket= null ;

    
        try {
            String host = "localhost";
            /* Create socket for contacting the server on port 4321*/
            requestSocket = new Socket(host,port);
            
            /* Create the streams to send and receive data from server */
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());

            //send GPX file to server
            GPX gpxFile = new GPX(path);
            out.writeObject(gpxFile);
            
            
          
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                in.close(); out.close();
                requestSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
    
    public static void main(String [] args) {
        //start Clients
    }
}