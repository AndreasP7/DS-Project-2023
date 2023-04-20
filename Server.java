import java.io.*;
import java.net.*;

public class Server {

    ServerSocket s;

    Socket providerSocket;

    void openServer(int n) {
        try {
 
            /* Create Server Socket */
            s = new ServerSocket(4020, n);//the same port as before, 10 connections
 
            while (true) {
                /* Accept the connection */
                providerSocket = s.accept();
            
                /* Handle the request */
                Thread t = new ActionsForClients(providerSocket);
                t.start();
            }
 
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                providerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
    
}
