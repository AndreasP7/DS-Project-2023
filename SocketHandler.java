import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketHandler extends Thread{
    ObjectInputStream in1;
    ObjectOutputStream out1;
    ObjectInputStream in2;
    ObjectOutputStream out2;
    Socket socket1;
    Socket socket2;
    public SocketHandler(Socket s1, Socket s2){
        this.socket1 = s1;
        this.socket2 = s2;


    }

    public void run(){
        try{
            out1 = new ObjectOutputStream(socket1.getOutputStream());
            in1 = new ObjectInputStream(socket1.getInputStream());
            out2 = new ObjectOutputStream(socket2.getOutputStream());
            in2 = new ObjectInputStream(socket2.getInputStream());

            int data = (Integer) in1.readObject();
            out2.writeObject(data);
            out2.flush();
            int results = (Integer) in2.readObject();
            out1.writeObject(results);



        }
        catch(IOException e){
            e.printStackTrace();

        }catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }


    }


}
