import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.w3c.dom.Node;
import java.io.*;
import java.util.*;
import java.net.*;

public class Master extends Thread{
    
    ServerSocket userSocket;
    ServerSocket workerSocket;
    Socket userProvider;
    Socket workerProvider;
    HashMap<InetAddress, Socket> Workers = new HashMap<InetAddress, Socket>();
    Set<InetAddress> workerAddr = new HashSet<>();
    int numberOfWorkers;

    Master(int numberOfWorkers){
        this.numberOfWorkers = numberOfWorkers;
        

    }

     void  openServer() {
        System.out.println("Opened Server");
        try {
            userSocket = new ServerSocket(4020);
            workerSocket= new ServerSocket(3000);

            while(true){
                userProvider = userSocket.accept();
                System.out.println("User Accepted");

                Thread t = new SocketHandler(workerSocket, userProvider, 20, Workers, workerAddr);
                t.start();

            }
            
        }
        catch (IOException ioException){
            ioException.printStackTrace();
        }
        finally {
            try {
                workerProvider.close();
                userProvider.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }



    public static void main(String[] args) {
        
        new Master(2).openServer();
    }
}
