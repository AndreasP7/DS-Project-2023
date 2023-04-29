import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SocketHandler extends Thread{
    ObjectInputStream in1;
    ObjectOutputStream out1;
    ObjectInputStream in2;
    ObjectOutputStream out2;
    Socket userProvider;
    ServerSocket workerSocket;

    Socket workerProvider;

    int nChunks;

    List<Chunk> Chunks = new ArrayList<Chunk>();

    public SocketHandler(ServerSocket workerSocket, Socket userProvider, int nChunks){
        this.workerSocket = workerSocket;
        this.userProvider = userProvider;
        this.nChunks = nChunks;

    }

    public void run(){
        try{
            ObjectOutputStream outUser = new ObjectOutputStream(userProvider.getOutputStream());
            ObjectInputStream inUser = new ObjectInputStream(userProvider.getInputStream());


            GPX userGPX = (GPX) inUser.readObject();
            List<Map<String,String>> waypoints = parseGPX(userGPX);
            Map<String,Float> results = new HashMap<String,Float>();
            Chunk chunk;
            int size = waypoints.size();
            int k = 0;
            int n = size / nChunks;
            int mod = size % nChunks;

            boolean f = (mod ==0);

            while( size >= n){
                chunk = new Chunk(userGPX.getUid());
                for (int i =k; i <= k*n ; i++){
                    chunk.addWp(waypoints.get(i));
                    size -= 1;
                    k += n;
                }
                Chunks.add(chunk);
            }

            if (!f){
                chunk = new Chunk(userGPX.getUid());
                for (Map <String, String> waypoint : waypoints){
                    chunk.addWp(waypoint);
                }
            }

            while(true){
                workerProvider = workerSocket.accept();
                ObjectOutputStream outWorker = new ObjectOutputStream(workerProvider.getOutputStream());
                ObjectInputStream inWorker = new ObjectInputStream(workerProvider.getInputStream());

                //map Chunks
                outWorker.writeObject(Chunks.get(0));
                outWorker.flush();

                //get results



            }

        }
        catch(IOException e){
            e.printStackTrace();

        }catch(ClassNotFoundException e){
            throw new RuntimeException(e);
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
        catch (ParserConfigurationException e) {
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

    public static List<Map<Integer, Chunk>> map( List<Chunk> Chunks){
        List<Map<Integer, Chunk>> mapped = new ArrayList<Map<Integer, Chunk>>();

        for(Chunk chunk : Chunks){
            Map<Integer, Chunk> pair = new HashMap<Integer, Chunk>();
            pair.put(chunk.getID(), chunk);
            mapped.add(pair);

        }

        return mapped;
    }




}
