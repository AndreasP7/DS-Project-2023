
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TestServer {



    public static void main(String... args){

        try {
            ServerSocket server = new ServerSocket(8080);

            while(true){
                Socket s = server.accept();

                ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());

                String arg = ois.readUTF();

                oos.writeUTF(arg.toUpperCase());
                oos.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
