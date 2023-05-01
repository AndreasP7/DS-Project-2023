import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

public class WorkerHandler extends Thread{

    ObjectInputStream inWorker;
    List<Map<String,Double>> Iresults;

    public WorkerHandler( ObjectInputStream inWorker, List<Map<String,Double>> Iresults ){
        this.inWorker = inWorker;
        this.Iresults = Iresults;

    }

    @Override
    public void run() {





            try{
                Iresults.add((Map<String, Double>) inWorker.readObject());
            }catch(IOException e){
                e.printStackTrace();

            }
            catch(ClassNotFoundException e){
                throw new RuntimeException(e);
            }



    }
}
