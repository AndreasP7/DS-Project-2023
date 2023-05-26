
import javax.xml.transform.Result;
import java.io.*;
import java.util.*;
import java.net.*;

public class Master extends Thread{
    
    ServerSocket userSocket;
    ServerSocket workerSocket;
    Socket userProvider;
    Socket workerProvider;

    HashMap<InetAddress, Socket> Workers = new HashMap<InetAddress, Socket>();//Worker Sockets

    List<InetAddress> workerAddr = new ArrayList<>();//Worker addresses

    HashMap<InetAddress, ObjectOutputStream> WorkersOut = new HashMap<InetAddress, ObjectOutputStream>(); //Worker in and out streams
    HashMap<InetAddress, ObjectInputStream> WorkersIn= new HashMap<InetAddress, ObjectInputStream>();

    Map<Integer, List<Map<String,Double>>> totalResults = new HashMap <Integer, List<Map<String,Double>>>(); //Keep total results, key is a user id and value is a list of each of the user's routes/gpx files

    List<Integer> userIds = new ArrayList<Integer>(); //Keep IDs of users

    int minWorkers; //minimum number of workers needed to start map reduce


    Master(int numberOfWorkers){
        this.minWorkers = numberOfWorkers;
        

    }

     synchronized void  openServer() {
        System.out.println("Opened Server");
        try {
            userSocket = new ServerSocket(4020);
            workerSocket= new ServerSocket(3000);

            int connections =0;
            while(true){


                userProvider = userSocket.accept();
                System.out.println("User Accepted");

                Thread t = new SocketHandler(workerSocket, userProvider, 20, Workers,WorkersOut,WorkersIn, workerAddr, minWorkers, this);
                t.start();
                sleep(300);
                connections ++;



            }

            
        }
        catch (IOException ioException){
            ioException.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {

                userProvider.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }


     public void addResult(int user, Map<String,Double> results){

        if (totalResults.get(user) == null) {
            totalResults.put(user, new ArrayList<Map<String,Double>>());
            userIds.add(user);

        }
        List<Map<String,Double>> r = totalResults.get(user);
        r.add(results);
        totalResults.put(user,r);

    }

    synchronized Map<String,Double> getAverageOfUser(int user){
        //return the average results of the user
        Map<String,Double> results = new HashMap<String,Double>();
        List<Map<String,Double>> userResults = totalResults.get(user); //get list of user's routes results
        int n = userResults.size();


        Double totalTime = 0.0;
        Double totalDistance = 0.0;
        Double totalElevation = 0.0;


        int counter =0;
        for( Map<String,Double> r: userResults){
            totalTime += r.get("totalTime");
            totalDistance += r.get("totalDistance");
            totalElevation += r.get("totalElevation");

        }

        results.put("averageTime", totalTime/n);
        results.put("averageDistance", totalDistance/n);
        results.put("averageElevation", totalElevation/n);

        return results;

    }


    synchronized Map<String,Double> getAverage(){
        //return the average results of all users combined
        Map<String,Double> results = new HashMap<String,Double>();
        int numberOfUsers = userIds.size();

        Double totalTime = 0.0;
        Double totalDistance = 0.0;
        Double totalElevation = 0.0;

        for (int id :userIds) {
            for (Map<String, Double> route : totalResults.get(id)) {
                totalTime += route.get("totalTime");
                totalDistance += route.get("totalDistance");
                totalElevation += route.get("totalElevation");
            }
        }

        results.put("averageTime", totalTime/numberOfUsers);
        results.put("averageDistance", totalDistance/numberOfUsers);
        results.put("averageElevation", totalElevation/numberOfUsers);

        return results;

    }

    public void printResults(){
        System.out.println(totalResults);
    }

    public static void main(String[] args) {


        //read config file
        int n = 0;

        try{
            Properties prop=new Properties();
            FileInputStream ip= new FileInputStream("config/config_master.properties");

            prop.load(ip);
            n = Integer.parseInt(prop.getProperty("number_of_workers"));

            System.out.printf(String.format("Started Master. Minimum number of Workers needed: %d \n",n));
            Master m = new Master(n);
            m.openServer();







        }catch(IOException e){
            e.printStackTrace();

        }

    }
}
