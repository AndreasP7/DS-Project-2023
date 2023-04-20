import java.io.*;
import java.net.*;
 
public class ActionsForClients extends Thread {
ObjectInputStream in;
ObjectOutputStream out;
 
    public ActionsForClients(Socket connection) {
        try {
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    public void run() {
        try {
                Test t = (Test) in.readObject();
                t.setA(t.getB()+t.getA());
                t.setflag(true);
                out.writeObject(t);
                out.flush();
                // int a = in.readInt();
                // int b = in.readInt();
                // out.writeInt(a + b);
                // out.flush(); // sends the data
    
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);// need it in readObject 
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}


