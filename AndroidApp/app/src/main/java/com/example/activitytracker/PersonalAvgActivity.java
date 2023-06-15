package com.example.activitytracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PersonalAvgActivity extends AppCompatActivity {

    Handler myHandler;
    String username;
    Button btn;
    TextView text;

    Request request;

    Response response;
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

                         response = (Response) message.getData().getSerializable("response");
                        text.setText("Received results");
                        text.setVisibility(View.VISIBLE);

                        btn.setEnabled(true);
                        return true;
                    }
                });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text.setText(response.getResults().toString());
            }
        });
        MyThread myThread = new MyThread(request,myHandler);
        myThread.start();




    }


}