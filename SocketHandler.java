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
    public SocketHandler(Socket s1){
        this.workerProvider = s1;



    }

    public void run(){
        try{
            ObjectOutputStream out = new ObjectOutputStream(workerProvider.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(workerProvider.getInputStream());
            out.writeObject(1);
            out.flush();


            int results = in.readInt();
            System.out.println(results);



        }
        catch(IOException e){
            e.printStackTrace();

        }


    }


}
