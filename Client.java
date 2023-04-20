import java.io.*;
import java.net.*;

import javax.management.RuntimeErrorException;
 
public class Client extends Thread {
    int a, b;
 
    Client(int a, int b) {
        this.a = a;
        this.b = b;
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
            
            /* Write the two integers */
            Test t = new Test(a,b);
            out.writeObject(t);
            out.flush();

            // out.writeInt(a);
            // out.flush();
            // out.writeInt(b);
            // out.flush();
            Test t2 = (Test) in.readObject();
            //System.out.println("Server>" + in.readInt());
            System.out.println("System>" + t2.getA() + " " + t2.getB());
            /* Print the received result from server */
 
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
        new Client(10, 5).start();
        new Client(20, 5).start();
        new Client(30, 5).start();
        new Client(40, 5).start();
        new Client(50, 5).start();
        new Client(60, 5).start();
        new Client(70, 5).start();
        new Client(80, 5).start();
        new Client(90, 5).start();
        new Client(100, 5).start();
    }
}