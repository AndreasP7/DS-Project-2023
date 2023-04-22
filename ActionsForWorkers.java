import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;


public class ActionsForWorkers extends Thread {
    
    ObjectInputStream in;
    ObjectOutputStream out;

    List<Map<String,String>> chunk ;
    public ActionsForWorkers(List<Map<String,String>> chunk, Socket connection) {
        this.chunk = chunk ;

        try {
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){

        try{
            out.writeObject(chunk);
            System.out.println("Action");
            out.flush();
        }catch (IOException e){
            e.printStackTrace();
        }
            
       
    }
    
}
