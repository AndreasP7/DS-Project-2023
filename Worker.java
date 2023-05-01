import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;


public class Worker {
    int port;
    int wid;

    ArrayList<Socket> Sockets = new ArrayList<Socket>();
    Worker(int port, int wid){
        this.port = port;
        this.wid = wid;

    }

    public void  run() {

        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        Socket requestSocket = null;

        try {


            String host = "localhost";

            while (true) {
                requestSocket = new Socket(host, this.port);
                out = new ObjectOutputStream(requestSocket.getOutputStream());
                in = new ObjectInputStream(requestSocket.getInputStream());

                System.out.println("Waiting for Request");
                Map<Integer, Chunk> Request = (Map<Integer, Chunk>) in.readObject();
                System.out.println("Request received");

                Thread t = new WorkerThread(wid, Request, out);
                t.start();
            }





        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

        }

    }


    public static void main(String[] args) {
        Scanner keyboard = new Scanner(System.in);


        //System.out.println("Enter ID");
        //int wid = keyboard.nextInt();

        new Worker(3000, 0).run();
       

        
        
        
    }
}
