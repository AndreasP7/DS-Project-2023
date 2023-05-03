import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.*;
import java.util.*;



public class SocketHandler extends Thread{
    ObjectInputStream in1;
    ObjectOutputStream out1;
    ObjectInputStream in2;
    ObjectOutputStream out2;


    List<InetAddress> workerAddr;
    HashMap<InetAddress, Socket> Workers;

    int minWorkers;
    Socket userProvider;
    ServerSocket workerSocket;

    Socket workerProvider;

    int nChunks;

    List<Map<String,Double>> Iresults = new ArrayList<Map<String,Double>>();

    List<Chunk> Chunks = new ArrayList<Chunk>();

    public SocketHandler(ServerSocket workerSocket, Socket userProvider, int nChunks, HashMap<InetAddress, Socket> Workers , List<InetAddress> workerAddr, int minWorkers){
        this.workerSocket = workerSocket;
        this.userProvider = userProvider;
        this.nChunks = nChunks;
        this.Workers = Workers;
        this.workerAddr = workerAddr;
        this.minWorkers = minWorkers;

    }

    synchronized public void run(){
        try{
            ObjectOutputStream outUser = new ObjectOutputStream(userProvider.getOutputStream());
            ObjectInputStream inUser = new ObjectInputStream(userProvider.getInputStream());


            GPX userGPX = (GPX) inUser.readObject();
            List<Map<String,String>> waypoints = parseGPX(userGPX);

            Map<String,Double> results = new HashMap<String,Double>();

            List<Map<Integer, Chunk>> mapped = this.map(waypoints, userGPX.getUid());

            int chunk = 0;
            int counter = 0;
            //new thread Round Robin

            Socket current;
            while(mapped.size() > Iresults.size()){

                while(workerAddr.size()<minWorkers){
                    System.out.println("Waiting for "+(minWorkers - workerAddr.size())+" workers...");
                    workerProvider = workerSocket.accept();
                    if (!workerAddr.contains(workerProvider.getInetAddress())){
                        workerAddr.add(workerProvider.getInetAddress());
                        Workers.put(workerProvider.getInetAddress(),workerProvider);

                    }



                }
                current = Workers.get(workerAddr.get(counter));
                ObjectOutputStream outWorker = new ObjectOutputStream(current.getOutputStream());
                ObjectInputStream inWorker = new ObjectInputStream(current.getInputStream());

                outWorker.writeObject(mapped.get(counter).get(userGPX.getUid()));
                outWorker.flush();

                Thread t = new WorkerHandler(inWorker, Iresults);
                t.start();



                workerProvider = workerSocket.accept();
                while(workerProvider.getInetAddress() != current.getInetAddress()){
                    workerProvider = workerSocket.accept();
                    workerAddr.add(workerProvider.getInetAddress());
                    Workers.put(workerProvider.getInetAddress(),workerProvider);



                }







                if (counter == workerAddr.size()){
                    counter = 0;

                }
                else{
                    counter++;
                }
            }

            userGPX.setResults(Reduce(Iresults));
            outUser.writeObject(userGPX);
            outUser.flush();

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



            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(gpxFile.getText()));
            Document doc = dBuilder.parse(is);
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

    public List<Map<Integer, Chunk>> map( List<Map<String,String>> waypoints, int id){

        Chunk chunk;
        int size = waypoints.size();
        int n = 5;


        chunk = new Chunk(id);
        int k =0;
        for( Map<String,String> w : waypoints){
            chunk.addWp(w);
            if(chunk.getSize() == n-1 ){
                Chunks.add(chunk);

                chunk = new Chunk(id);
                chunk.addWp(w);

            }



            if(waypoints.size()-1 ==k && chunk.getSize() <= n-1){
                Chunks.add(chunk);
            }
            k+=1;
        }


        System.out.println(Chunks.size());

        List<Map<Integer, Chunk>> mapped = new ArrayList<Map<Integer, Chunk>>();

        for(Chunk c : Chunks){
            Map<Integer, Chunk> pair = new HashMap<Integer, Chunk>();
            pair.put(c.getID(), c);
            mapped.add(pair);

        }

        return mapped;
    }

    public Map<String,Double> Reduce(List<Map<String,Double>> Iresults){
        Map<String,Double> results = new HashMap<String,Double>();

        Double time[] = new Double[Iresults.size()];
        Double speed[] =new Double[Iresults.size()];
        Double distance[] = new Double[Iresults.size()];
        Double elevation[] = new Double[Iresults.size()];

        Double totalTime = 0.0;
        Double totalDistance = 0.0;
        Double totalElevation = 0.0;
        Double averageSpeed = 0.0;

        for (int i =0; i<= Iresults.size();i++){
            totalTime += time[i];
            totalDistance += distance[i];
            totalElevation += elevation[i];
            averageSpeed += speed[i]/Iresults.size();
        }

        results.put("Total Time",totalTime);
        results.put("Total Distance",totalDistance);
        results.put("Total Elevation",totalElevation);
        results.put("Average Speed",averageSpeed);

        return results;

    }





}
