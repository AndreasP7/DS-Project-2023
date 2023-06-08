package com.example.activitytracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SelectAction extends AppCompatActivity {

    private String username;

    private TextView label;

    private Button gpxBtn;
    private Button UserAvgBtn;
    private Button AvgBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_action);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        label = (TextView) findViewById(R.id.label) ;
        gpxBtn = (Button) findViewById(R.id.sendGPXBtn);
        UserAvgBtn = (Button) findViewById(R.id.viewUserAverageBtn);
        AvgBtn = (Button) findViewById(R.id.viewAverageBtn);

        gpxBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectAction.this, MainActivity.class);
                intent.putExtra("username",username);
                startActivity(intent);

            }
        });

        UserAvgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectAction.this,PersonalAvgActivity.class);
                intent.putExtra("username",username);
                startActivity(intent);
            }
        });


    }
}