import java.io.IOException;
import java.io.ObjectInputStream;

import java.util.Map;

public class WorkerHandler extends Thread{

    ObjectInputStream inWorker;
    SocketHandler socketHandler;

    public WorkerHandler( ObjectInputStream inWorker, SocketHandler socketHandler){
        this.inWorker = inWorker;
        this.socketHandler = socketHandler;

    }

    @Override
    synchronized public void run() {

            try{
                this.socketHandler.addResult((Map<String, Double>) inWorker.readObject());
                System.out.println("Received Results");

            }catch(IOException e){
                e.printStackTrace();

            }
            catch(ClassNotFoundException e){
                throw new RuntimeException(e);
            }



    }
}
