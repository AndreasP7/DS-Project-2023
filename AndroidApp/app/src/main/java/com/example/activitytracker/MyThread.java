package com.example.activitytracker;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class MyThread extends Thread{

    Handler myHandler;

    Map<String,String> gpxData;

    public MyThread(Map<String,String> gpxData, Handler myHandler){
        this.gpxData = gpxData;
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

            oos.writeObject(gpxData);

            oos.flush();

            //GPX result = (GPX)ois.readObject();
            String result = "1";

            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putSerializable("results",result);
            msg.setData(bundle);
            s.close();

            myHandler.sendMessage(msg);

        }catch (Exception e){ }
    }
}