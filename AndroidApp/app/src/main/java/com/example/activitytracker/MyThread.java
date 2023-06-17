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

    Request request;

    public MyThread(Request request, Handler myHandler){
        this.request = request;
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

            oos.writeObject(request);

            oos.flush();

            Response serverResponse = (Response) ois.readObject();

            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putSerializable("response",serverResponse);
            msg.setData(bundle);
            s.close();

            myHandler.sendMessage(msg);

        }catch (Exception e){ }
    }
}