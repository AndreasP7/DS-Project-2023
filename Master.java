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
    List<InetAddress> workerAddr = new ArrayList<>();
    int minWorkers;

    Master(int numberOfWorkers){
        this.minWorkers = numberOfWorkers;
        

    }

     void  openServer() {
        System.out.println("Opened Server");
        try {
            userSocket = new ServerSocket(4020);
            workerSocket= new ServerSocket(3000);

            while(true){
                userProvider = userSocket.accept();
                System.out.println("User Accepted");

                Thread t = new SocketHandler(workerSocket, userProvider, 20, Workers, workerAddr, minWorkers = 2);
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

        int n = 0;

        File file = new File("config/config_master.txt");
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            n =  Integer.parseInt(br.readLine().split("=")[1]);
            br.close();
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(IOException e){
            throw new RuntimeException(e);
        }
        System.out.printf(String.format("Started Master. Minimum number of Workers needed: %d \n",n));
        new Master(2).openServer();
    }
}
