package com.example.activitytracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    TextView label;
    EditText input;
    Button btn;

    Handler myHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        label = (TextView) findViewById(R.id.label);
        input = (EditText) findViewById(R.id.input);
        btn = (Button)  findViewById(R.id.btn);

        myHandler = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message message) {

                        String result = message.getData().getString("result");
                        label.setText(result);
                        return true;
                    }
                });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String arg = input.getText().toString();
                MyThread myThread = new MyThread(arg,myHandler);
                myThread.start();
            }
        });

//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                AsyncTask<String,Void,String> myAsync
//                        = new AsyncTask<String, Void, String>() {
//                    @Override
//                    protected void onPreExecute() {
//                        Toast.makeText(MainActivity.this, "Task started",
//                                Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    protected String doInBackground(String... strings) {
//
//                        try {
//                            Socket s = new Socket("172.16.1.41",8080);
//                            ObjectOutputStream oos =
//                                    new ObjectOutputStream(s.getOutputStream());
//                            ObjectInputStream ois =
//                                    new ObjectInputStream(s.getInputStream());
//                            String arg = strings[0];
//
//                            oos.writeUTF(arg);
//                            oos.flush();
//
//                            String result = ois.readUTF();
//
//                            s.close();
//                            return result;
//                        }catch (Exception e){
//                            return "Error";
//                        }
//
//                    }
//
//                    @Override
//                    protected void onPostExecute(String s) {
//                        label.setText("Result: "+s);
//                    }
//                };
//
//                myAsync.execute(input.getText().toString());
//            }
//        });



    }
}