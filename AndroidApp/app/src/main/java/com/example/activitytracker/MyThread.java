package com.example.activitytracker;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MyThread extends Thread{

    Handler myHandler;

    GPX gpx;

    public MyThread(GPX gpx, Handler myHandler){
        this.gpx = gpx;
        this.myHandler = myHandler;
    }

    @Override
    public void run() {
        try {
            Socket s = new Socket("192.168.1.22",4020);
            ObjectOutputStream oos =
                    new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream ois =
                    new ObjectInputStream(s.getInputStream());

            oos.writeObject(gpx);

            oos.flush();

            GPX result = (GPX)ois.readObject();

            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putSerializable("results",result);
            msg.setData(bundle);
            s.close();

            myHandler.sendMessage(msg);

        }catch (Exception e){ }
    }
}