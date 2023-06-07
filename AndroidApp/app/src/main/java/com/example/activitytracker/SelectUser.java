package com.example.activitytracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SelectUser extends AppCompatActivity {

    TextView label;
    EditText input;
    Button logInBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user);

        label = (TextView) findViewById(R.id.welcome_label);
        input = (EditText) findViewById(R.id.input);
        logInBtn = (Button) findViewById (R.id.logIn);

        logInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SelectUser.this, SelectAction.class);
                intent.putExtra("username",input.getText().toString());
                startActivity(intent);

            }
        });
    }


}