package Test;
import java.io.*;
import java.net.*;

public class Client extends Thread{
    public  Client(){

    }

    public void run() {
        ObjectOutputStream out= null ;
        ObjectInputStream in = null ;
        Socket requestSocket= null ;


        try {
            String host = "localhost";
            /* Create socket for contacting the server on port 4321*/
            requestSocket = new Socket(host, 4321);

            /* Create the streams to send and receive data from server */
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());

            String text = "ready";
            out.writeObject(text);
            out.flush();

            int result = (int) in.readObject();
            System.out.println(result);



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

    public static void main(String[] args) {
        Thread t = new Client();
        t.start();
    }
}
