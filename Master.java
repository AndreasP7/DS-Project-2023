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

public class Master extends Thread{
    int workers_n;
    Master(int workers_n){

        this.workers_n = workers_n;


    }
    //Server 1- users
    //Server 2 - workers
    
    //recieve files
    //parse xml
    
    List<Map<String,String>> parseGPX(GPX gpxFile){
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
    //Initialize workers

    // Send
    public static void main(String[] args) {
        GPX file = new GPX("gpxs/route1.gpx");
        Master m = new Master(5);
        System.out.println(m.parseGPX(file));
    }
}