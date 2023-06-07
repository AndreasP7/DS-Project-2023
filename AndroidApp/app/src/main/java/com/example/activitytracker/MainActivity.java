package com.example.activitytracker;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    TextView label;



    Button chooseBtn ;
    Button sendBtn;
    Button viewBtn;

    Handler myHandler;

    Map<String,String> gpxData = new HashMap();



    private String username;

    private static final int PERMISSION_REQUEST_CODE = 1;
    private ListView fileListView;
    private List<String> fileList;

    GPX currentGpx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");



        label = (TextView) findViewById(R.id.label);


        chooseBtn = (Button) findViewById(R.id.chooseFile) ;
        sendBtn = (Button) findViewById(R.id.sendFile);
        viewBtn = (Button) findViewById(R.id.viewResults);



        label.setText("User " +username);


        myHandler = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message message) {

                        String result = (String) message.getData().getSerializable("results");
                        label.setText(result);
                        return true;
                    }
                });



        chooseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilePicker(Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()));

            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyThread myThread = new MyThread(gpxData,myHandler);
                myThread.start();
            }
        });



    }

    private void openFilePicker(Uri pickerInitialUri) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

        someActivityResultLauncher.launch(intent);

    }
    private String readFileContent(Uri fileUri) {
        StringBuilder content = new StringBuilder();
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                    content.append("\n");
                }
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }



    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Uri uri = result.getData().getData();


                    gpxData.put("text",readFileContent(uri) );
                    gpxData.put("user", username);

                    chooseBtn.setText( "File Chosen");
                    sendBtn.setEnabled(true);



                }
            });


}