package Test;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class Server {
    public static void main(String args[]) {
        new Server().openServer();
    }

    /* Define the socket that receives requests */
    ServerSocket s;
    ObjectInputStream in;
    ObjectOutputStream out;

    HashMap<InetAddress, Socket> Clients = new HashMap<InetAddress, Socket>();
    Set<InetAddress> clientAddress = new HashSet<>();

    /* Define the socket that is used to handle the connection */
    Socket providerSocket;
    void openServer() {
        try {

            /* Create Server Socket */
            s = new ServerSocket(4321, 10);//the same port as before, 10 connections
            System.out.println("Server open");

            while (true) {
                /* Accept the connection */
                providerSocket = s.accept();

                while(clientAddress.contains(providerSocket.getInetAddress())){
                    providerSocket = s.accept();
                }


                out = new ObjectOutputStream(providerSocket.getOutputStream());
                in = new ObjectInputStream(providerSocket.getInputStream());

                clientAddress.add(providerSocket.getInetAddress());
                System.out.println(clientAddress);
                String s =( String) in.readObject();
                out.writeObject(1);
                out.flush();

                System.out.println(1);








            }

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }  catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }finally {
            try {
                providerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
