package com.example.activitytracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Button;
import android.widget.TextView;

public class PersonalAvgActivity extends AppCompatActivity {

    Handler myHandler;
    String username;
    Button btn;
    TextView text;

    Request request;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_avg);

        btn = (Button) findViewById(R.id.viewBtn);
        text = (TextView) findViewById(R.id.results);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        request = new Request("user_average", username);



        myHandler = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message message) {

                        Response response = (Response) message.getData().getSerializable("response");
                        text.setText("Received results");
                        text.setText(response.getResults().toString());
                        btn.setEnabled(true);
                        return true;
                    }
                });
        MyThread myThread = new MyThread(request,myHandler);
        myThread.start();




    }


}