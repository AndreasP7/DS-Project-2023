import java.io.*;
import java.net.*;
import java.util.*;

public class Worker extends Thread{
    int port;
    //connect to server on socket
    Worker(int port){
        this.port = port;
    }

    public void run(){
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        Socket requestSocket = null;

        try{
            String host = "localhost";
            requestSocket = new Socket(host, this.port);

            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());
            List<Map<String,String>> chunk = (List<Map<String,String>> ) in.readObject();
            
            chunk = (List<Map<String,String>>) in.readObject();

            // proccess chunk
            
            Map<String,Integer> results = new HashMap<String, Integer>();
            results.put("Result 1",1);
            out.writeObject(results);

            //send results

            out.flush();
        }catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }catch (ClassNotFoundException e){
            throw new RuntimeException(e);
        }finally {
            try {
                in.close();
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

    }
        

    public static void main(String[] args) {
        new Worker(4019);
    }
}
