import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;


public class Worker {
    int port;
    int wid;
    String host;

    ArrayList<Socket> Sockets = new ArrayList<Socket>();
    Worker(int port, int wid, String host){
        this.port = port;
        this.wid = wid;
        this.host = host;

    }

    public void  run() {

        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        Socket requestSocket = null;

        try {
            while (true) {
                requestSocket = new Socket(this.host, this.port);
                out = new ObjectOutputStream(requestSocket.getOutputStream());
                in = new ObjectInputStream(requestSocket.getInputStream());

                System.out.println("Waiting for Request");
                Chunk Request = ( Chunk) in.readObject();
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

        String host = "";
        int id=0;
        File file = new File("config/config_worker.txt");
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                host = br.readLine().split("=")[1];
                id =  Integer.parseInt(br.readLine().split("=")[1]);
                br.close();
            }catch(FileNotFoundException e){
                e.printStackTrace();
            }catch(IOException e){
                throw new RuntimeException(e);
            }
        System.out.printf(String.format("Started Worker %d. Connected to Master-Server %s \n",id, host));


        new Worker(3000, id,host).run();
       

        
        
        
    }
}
