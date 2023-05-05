import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import java.io.*;
import java.net.*;
import java.util.*;



public class SocketHandler extends Thread{
    ObjectInputStream in1;
    ObjectOutputStream out1;
    ObjectInputStream in2;
    ObjectOutputStream out2;

    static final CustomLock lock = new CustomLock();

    List<InetAddress> workerAddr;
     HashMap<InetAddress, Socket> Workers;
     HashMap<InetAddress, ObjectOutputStream> WorkersOut;
     HashMap<InetAddress, ObjectInputStream> WorkersIn;


    int minWorkers;
    Socket userProvider;
    ServerSocket workerSocket;

    Socket workerProvider;

     int threadsReturned =0;

    int nChunks;

    public List<Map<String,Double>> Iresults = new ArrayList<Map<String,Double>>();

    List<Chunk> Chunks = new ArrayList<Chunk>();

    public SocketHandler(ServerSocket workerSocket, Socket userProvider, int nChunks, HashMap<InetAddress, Socket> Workers ,HashMap<InetAddress, ObjectOutputStream> WorkersOut,HashMap<InetAddress, ObjectInputStream> WorkersIn, List<InetAddress> workerAddr, int minWorkers){
        this.workerSocket = workerSocket;
        this.userProvider = userProvider;
        this.nChunks = nChunks;
        this.Workers = Workers;
        this.workerAddr = workerAddr;
        this.minWorkers = minWorkers;
        this.WorkersIn = WorkersIn;
        this.WorkersOut = WorkersOut;


    }

    public void run(){
        try{
            ObjectOutputStream outUser = new ObjectOutputStream(userProvider.getOutputStream());
            ObjectInputStream inUser = new ObjectInputStream(userProvider.getInputStream());


            GPX userGPX = (GPX) inUser.readObject();
            List<Map<String,String>> waypoints = parseGPX(userGPX);
            System.out.printf(String.format("GPX received from User %d", userGPX.getUid()));

            Map<String,Double> results = new HashMap<String,Double>();

            List<Map<Integer, Chunk>> mapped = this.map(waypoints, userGPX.getUid());

            int chunk = 0;
            int counter = 0;
            //new thread Round Robin

            Socket current;
            synchronized (lock) {


                while (mapped.size() > chunk) {
                    System.out.println("Chunk " + chunk);

                    while (workerAddr.size() < minWorkers) {
                        System.out.println("Waiting for " + (minWorkers - workerAddr.size()) + " workers...");
                        lock.lock();
                        workerProvider = workerSocket.accept();
                        lock.unlock();

                        if (!workerAddr.contains(workerProvider.getInetAddress())) {
                            workerAddr.add(workerProvider.getInetAddress());
                            Workers.put(workerProvider.getInetAddress(), workerProvider);
                            WorkersIn.put(workerProvider.getInetAddress(), new ObjectInputStream(workerProvider.getInputStream()));
                            WorkersOut.put(workerProvider.getInetAddress(), new ObjectOutputStream(workerProvider.getOutputStream()));


                        }


                    }
                    current = Workers.get(workerAddr.get(counter));
                    ObjectOutputStream outWorker = WorkersOut.get(workerAddr.get(counter));
                    ObjectInputStream inWorker = WorkersIn.get(workerAddr.get(counter));


                    outWorker.writeObject(mapped.get(chunk).get(userGPX.getUid()));
                    outWorker.flush();

                    System.out.printf(String.format("Chunk %d sent to Worker %s\n", chunk, current.getInetAddress().getHostAddress()));


                    Thread t = new WorkerHandler(inWorker, this);
                    t.start();


                    lock.lock();
                    workerProvider = workerSocket.accept();
                    lock.unlock();

                    if (!workerAddr.contains(workerProvider.getInetAddress())) {
                        workerAddr.add(workerProvider.getInetAddress());
                    }
                    Workers.put(workerProvider.getInetAddress(), workerProvider);
                    WorkersIn.put(workerProvider.getInetAddress(), new ObjectInputStream(workerProvider.getInputStream()));
                    WorkersOut.put(workerProvider.getInetAddress(), new ObjectOutputStream(workerProvider.getOutputStream()));


                    if (counter == workerAddr.size() - 1) {
                        counter = 0;

                    } else {
                        counter++;
                    }
                    chunk++;


                }
            }

            while(threadsReturned < mapped.size()){
                System.out.println("Waiting for results...threads returned: "+ this.threadsReturned);
                try{
                    Thread.sleep(200);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }


            }

            System.out.println(Iresults);
            userGPX.setResults(Reduce(Iresults));
            outUser.writeObject(userGPX);
            outUser.flush();
            System.out.println(lock.locked);



        }
        catch(IOException e){
            e.printStackTrace();

        }catch(ClassNotFoundException e){
            throw new RuntimeException(e);
        }catch (InterruptedException e){
            System.out.println("Error");
        }finally {
            lock.unlock();
        }


    }


    synchronized public void addResult(Map<String, Double> result){
        Iresults.add(result);
        System.out.println("Added intermediate result. Results size: "+Iresults.size());
        this.threadsReturned ++;

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
        System.out.println(Iresults.size());
        Double[] time = new Double[Iresults.size()];
        Double[] speed =new Double[Iresults.size()];
        Double[] distance = new Double[Iresults.size()];
        Double[] elevation = new Double[Iresults.size()];

        Double totalTime = 0.0;
        Double totalDistance = 0.0;
        Double totalElevation = 0.0;
        Double averageSpeed = 0.0;

        int counter =0;
        for( Map<String,Double> r: Iresults){
            time[counter] = r.get("totalTime");
            speed[counter] = r.get("averageSpeed");
            distance[counter] = r.get("totalDistance");
            elevation[counter] = r.get("totalElevation");
            counter++;
        }

        for (int i =0; i< Iresults.size();i++){

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

    static class CustomLock {
        private boolean locked = false;

        public synchronized void lock() throws InterruptedException {
            while (locked) {
                wait();
            }
            locked = true;
        }

        public synchronized void unlock() {
            locked = false;
            notifyAll();
        }
    }
}






