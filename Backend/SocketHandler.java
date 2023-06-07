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
    //SocketHandler acts as Mapper and Reducer
    Master master;

    ObjectInputStream in1;


    ObjectOutputStream out1;
    ObjectInputStream in2;
    ObjectOutputStream out2;

    static final CustomLock lock = new CustomLock(); //used to lock workerSocket.accept(), so that 2 threads don't accept the same worker socket at the same time

    List<InetAddress> workerAddr;
    CustomMap<InetAddress, Socket> Workers;
    CustomMap<InetAddress, ObjectOutputStream> WorkersOut;
    CustomMap<InetAddress, ObjectInputStream> WorkersIn;

    Map<String,Double> userResults;
    int minWorkers;
    Socket userProvider;
    ServerSocket workerSocket;

    Socket workerProvider;

     int threadsReturned =0;

    int nChunks;

    public List<Map<String,Double>> Iresults = new ArrayList<Map<String,Double>>(); //Array to store intermediate results

    List<Chunk> Chunks = new ArrayList<Chunk>();

    public SocketHandler(ServerSocket workerSocket, Socket userProvider, int nChunks, CustomMap<InetAddress, Socket> Workers ,CustomMap<InetAddress, ObjectOutputStream> WorkersOut,CustomMap<InetAddress, ObjectInputStream> WorkersIn, List<InetAddress> workerAddr, int minWorkers, Master master){
        this.workerSocket = workerSocket;
        this.userProvider = userProvider;
        this.nChunks = nChunks;
        this.Workers = Workers;
        this.workerAddr = workerAddr;
        this.minWorkers = minWorkers;
        this.WorkersIn = WorkersIn;
        this.WorkersOut = WorkersOut;
        this.master = master;


    }

    public void run(){
        try{
            ObjectOutputStream outUser = new ObjectOutputStream(userProvider.getOutputStream());
            ObjectInputStream inUser = new ObjectInputStream(userProvider.getInputStream());



            Map<String,String> userRequest = (Map<String,String>) inUser.readObject();

            if (userRequest.get("type").equals("gpx")){
                master.addUser(userRequest.get("user"));

                int userID = master.getUser(userRequest.get("user"));
                GPX userGPX = new GPX("", userID);

                userGPX.setText(userRequest.get("text"));

                List<Map<String,String>> waypoints = parseGPX(userGPX);
                System.out.printf(String.format("GPX received from User %d\n", userGPX.getUid()));

                Map<String,Double> results = new HashMap<String,Double>();

                List<Map<Integer, Chunk>> mapped = this.map(waypoints, userGPX.getUid()); //map chunks

                int chunk = 0; //counter for chunks
                int counter = 0; //counter for workers. Used to simulate Round Robin

                Socket current; //current socket in Round Robin
                synchronized (lock) {


                    while (mapped.size() > chunk) {


                        while (workerAddr.size() < minWorkers) { //connect to minimum amount of workers
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
                        current = Workers.get(workerAddr.get(counter)); //get current worker in Round Robin queue
                        ObjectOutputStream outWorker = WorkersOut.get(workerAddr.get(counter));
                        ObjectInputStream inWorker = WorkersIn.get(workerAddr.get(counter));


                        outWorker.writeObject(mapped.get(chunk).get(userGPX.getUid())); //send chunk
                        outWorker.flush();

                        System.out.printf(String.format("Chunk %d sent to Worker %s\n", chunk, current.getInetAddress().getHostAddress()));


                        Thread t = new WorkerHandler(inWorker, this);//new thread to receive results
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

                userResults = Reduce(Iresults);

                int userId = userGPX.getUid();
                userGPX.setResults (userResults);
                outUser.writeObject(userGPX);
                outUser.flush();
                this.master.addResult(userId,userResults);
            }

            if (userRequest.get("type").equals("user_average")){
                String username = userRequest.get("user");
                Map<String,Double> average = new HashMap<>();
                average.put("averageTime", 0.0);
                average.put("averageDistance", 0.0);
                average.put("averageElevation", 0.0);


                int userID = master.getUser(username);
                if (userID != -1){
                     average = master.getAverageOfUser(userID);


                }
                outUser.writeObject(average);
            }

            if (userRequest.get("type").equals("total_average")){
                Map<String,Double> average = new HashMap<>();
                average = master.getAverage();
                outUser.writeObject(average);

            }






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

    public static List<Map<String,String>> parseGPX(GPX gpxFile){ //function to parse gpx waypoint info from gpx files
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
        for( Map<String,String> w : waypoints){ //add each waypoint to a chunk, until the chunk is full
            chunk.addWp(w);
            if(chunk.getSize() == n-1 ){
                Chunks.add(chunk);

                chunk = new Chunk(id);
                chunk.addWp(w); //add last waypoint to the new chunk, so that chunk i starts from the waypoint that chunk i-1 finished

            }



            if(waypoints.size()-1 ==k && chunk.getSize() <= n-1){
                Chunks.add(chunk);
            }
            k+=1;
        }




        List<Map<Integer, Chunk>> mapped = new ArrayList<Map<Integer, Chunk>>();

        for(Chunk c : Chunks){
            Map<Integer, Chunk> pair = new HashMap<Integer, Chunk>();
            pair.put(c.getID(), c);
            mapped.add(pair);

        }

        return mapped;
    }

    public Map<String,Double> Reduce(List<Map<String,Double>> Iresults){//reduce results
        Map<String,Double> results = new HashMap<String,Double>();

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

        results.put("totalTime",totalTime);
        results.put("totalDistance",totalDistance);
        results.put("totalElevation",totalElevation);
        results.put("averageSpeed",averageSpeed);

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






