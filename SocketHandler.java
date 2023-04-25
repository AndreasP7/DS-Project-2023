import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

public class SocketHandler extends Thread{
    ObjectInputStream in1;
    ObjectOutputStream out1;
    ObjectInputStream in2;
    ObjectOutputStream out2;
    Socket userProvider;
    Socket workerProvider;
    ServerSocket workerSocket;
    public SocketHandler(Socket s1, ServerSocket s2){
        this.userProvider = s1;
        this.workerSocket = s2;


    }

    public void run(){
        try{
            out1 = new ObjectOutputStream(userProvider.getOutputStream());
            in1 = new ObjectInputStream(userProvider.getInputStream());
            
            int data = (Integer) in1.readObject();
            
            try{
                while(true){
                    this.workerProvider = workerSocket.accept();
                    System.out.println("Worker connected");


                }
            }catch(IOException e){
                

            }



        }
        catch(IOException e){
            e.printStackTrace();

        }catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }


    }


}
