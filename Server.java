import java.io.*;
import java.net.*;

public class Server {

    ServerSocket s;

    Socket providerSocket;

    void openServer(int port, int n) {
        try {
 
            /* Create Server Socket */
            s = new ServerSocket(port, n);
            
            System.out.println("Started Server on port"+port);
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
