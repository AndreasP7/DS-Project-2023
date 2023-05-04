import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

public class WorkerHandler extends Thread{

    ObjectInputStream inWorker;
    SocketHandler socketHandler;

    public WorkerHandler( ObjectInputStream inWorker, SocketHandler socketHandler){
        this.inWorker = inWorker;
        this.socketHandler = socketHandler;

    }

    @Override
    public void run() {

            try{
                socketHandler.addResult((Map<String, Double>) inWorker.readObject());
                System.out.println("Received Results");

            }catch(IOException e){
                e.printStackTrace();

            }
            catch(ClassNotFoundException e){
                throw new RuntimeException(e);
            }



    }
}
