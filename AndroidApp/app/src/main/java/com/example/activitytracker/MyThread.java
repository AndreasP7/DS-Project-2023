package com.example.activitytracker;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MyThread extends Thread{

    Handler myHandler;

    String arg;

    public MyThread(String arg, Handler myHandler){
        this.arg = arg;
        this.myHandler = myHandler;
    }

    @Override
    public void run() {
        try {
            Socket s = new Socket("172.16.1.41",8080);
            ObjectOutputStream oos =
                    new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream ois =
                    new ObjectInputStream(s.getInputStream());

            oos.writeUTF(arg);
            oos.flush();

            String result = ois.readUTF();

            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putString("result",result);
            msg.setData(bundle);
            s.close();

            myHandler.sendMessage(msg);

        }catch (Exception e){ }
    }
}
