
import java.io.*;
import java.util.*;
import java.net.*;

public class Master extends Thread{
    
    ServerSocket userSocket;
    ServerSocket workerSocket;
    Socket userProvider;
    Socket workerProvider;//Worker Sockets

    HashMap<InetAddress, Socket> Workers = new HashMap<InetAddress, Socket>();//Worker addresses

    List<InetAddress> workerAddr = new ArrayList<>();

    HashMap<InetAddress, ObjectOutputStream> WorkersOut = new HashMap<InetAddress, ObjectOutputStream>(); //Worker in and out streams
    HashMap<InetAddress, ObjectInputStream> WorkersIn= new HashMap<InetAddress, ObjectInputStream>();

    int minWorkers; //minimum number of workers needed to start map reduce


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

                Thread t = new SocketHandler(workerSocket, userProvider, 20, Workers,WorkersOut,WorkersIn, workerAddr, minWorkers );
                t.start();
                sleep(300);

            }
            
        }
        catch (IOException ioException){
            ioException.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                workerProvider.close();
                userProvider.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
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
            new Master(n).openServer();



        }catch(IOException e){
            e.printStackTrace();

        }

    }
}
