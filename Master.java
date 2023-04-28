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

            while (true){
                workerProvider = workerSocket.accept();
                System.out.println("Worker connected");

                Thread t = new SocketHandler(workerProvider);
                t.start();
            }






            /*while (true){
               userProvider = userSocket.accept();
               System.out.println("User Accepted");


               
               Thread t = new SocketHandler(userProvider, workerSocket);
               t.start();

           }*/
                
            
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
    
    public static List<Map<String,String>> parseGPX(GPX gpxFile){
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        List<Map<String,String>> waypoints = new ArrayList<Map<String,String>>();
        try {
            File inputFile = gpxFile.file;
            
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            NodeList list = doc.getElementsByTagName("wpt");
            
            for (int temp = 0; temp < list.getLength(); temp++) {
                Map<String, String> hm = new HashMap<String, String>();
                Node node = list.item(temp);
  
                if (node.getNodeType() == Node.ELEMENT_NODE) {
  
                    Element element = (Element) node;
  
                    
                    String lat = element.getAttribute("lat");
                    String lon = element.getAttribute("lon");
  
                    
                    String ele = element.getElementsByTagName("ele").item(0).getTextContent();
                    String time = element.getElementsByTagName("time").item(0).getTextContent();
                    hm.put("ele", ele);
                    hm.put("lat", lat);
                    hm.put("lon", lon);
                    hm.put("time", time);
                }
                waypoints.add(hm);
            }      
        }
        catch (ParserConfigurationException  e) {
            e.printStackTrace();
        }
        catch (SAXException e) {
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return waypoints;
    
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

    public Map<String,Float> Reduce(List<Map<String,String>> Iresults){
        Map<String,Float> results = new HashMap<String,Float>();
        
        return results;

    }



    public static void main(String[] args) {
        
        new Master(2).openServer();
    }
}
