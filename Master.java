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
    ArrayList<Socket> Workers = new ArrayList<Socket>();
    int numberOfWorkers;

    Master(int numberOfWorkers){
        this.numberOfWorkers = numberOfWorkers;
        

    }

     void openServer() {
        System.out.println("Opened Server");
        try {
            userSocket = new ServerSocket(4020);
            workerSocket= new ServerSocket(3000);

            while(true){
                userProvider = userSocket.accept();
                System.out.println("User Accepted");

                Thread t = new SocketHandler(workerSocket, userProvider, 20);
                t.start();

            }
            
        }
        catch (IOException ioException){
            ioException.printStackTrace();
        }
        finally {
            try {
                workerProvider.close();
                //userProvider.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
    

    //map

    public Map<String,Float> Map( List<Map<String,String>> gpx_map, int n){
        
        Map<String,Float> results = new HashMap<String,Float>();
        int size = gpx_map.size();
        int k = 0;
        while( size >= n){
            List<Map<String,String>> chunk = new ArrayList<Map<String,String>>();
            for (int i =k; i <= k*n ; i++){
                chunk.add(gpx_map.get(i));
                size -= 1;
                k += n; 
            }
            //send chunk to worker
                   
        }
            
        return results;

    }





    public static void main(String[] args) {
        
        new Master(2).openServer();
    }
}
