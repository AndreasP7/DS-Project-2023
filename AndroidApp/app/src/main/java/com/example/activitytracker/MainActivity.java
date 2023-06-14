package com.example.activitytracker;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.github.mikephil.charting.charts.*;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.*;


public class MainActivity extends AppCompatActivity {

    TextView label;

    Button chooseBtn;
    Button sendBtn;
    Button viewBtn;

    LinearLayout chartContainer;
    private BarChart barChart;

    Handler myHandler;

    Map<String, String> gpxData = new HashMap();

    GPX gpx;

    Request request;

    CustomMap<String, GPX> results = new CustomMap<>();


    private String username;

    // variable for our bar data.
    BarData barData;

    // variable for our bar data set.
    BarDataSet barDataSet;

    // array list for storing entries.
    ArrayList barEntriesArrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");


        label = (TextView) findViewById(R.id.label);



        // Create the bar chart
        createBarChart();
        chooseBtn = (Button) findViewById(R.id.chooseFile);
        sendBtn = (Button) findViewById(R.id.sendFile);
        viewBtn = (Button) findViewById(R.id.viewResults);
        label.setText("User " + username);
        barChart = findViewById(R.id.barChart);


        if (savedInstanceState != null) {
            username = (String) savedInstanceState.get("username");
            gpx = (GPX) savedInstanceState.get("gpx");
            label.setText(savedInstanceState.getString("label"));
            sendBtn.setEnabled(savedInstanceState.getBoolean("sendBtnState"));
            viewBtn.setEnabled(savedInstanceState.getBoolean("viewBtnState"));
            results = (CustomMap<String, GPX>) savedInstanceState.get("results");
            request = (Request) savedInstanceState.get("request");
        }



        myHandler = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message message) {

                        Response response = (Response) message.getData().getSerializable("response");
                        GPX responseGPX = response.getGPX();
                        results.put(responseGPX.getFileName(), responseGPX);

                        viewBtn.setEnabled(true);
                        createAlert("GPX Result", "Results received from server for file: "+responseGPX.getFileName());

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

                MyThread myThread = new MyThread(request, myHandler);
                myThread.start();


            }
        });

        viewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                label.setText(results.get(gpx.getFileName()).toString());
            }
        });


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("results", results);
        outState.putString("username", username);
        outState.putSerializable("gpx", gpx);
        outState.putString("label", label.getText().toString());
        sendBtn = (Button) findViewById(R.id.sendFile);
        outState.putBoolean("sendBtnState", sendBtn.isEnabled());
        outState.putBoolean("viewBtnState", viewBtn.isEnabled());
        outState.putSerializable("request", request);


    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            username = (String) savedInstanceState.get("username");
            gpx = (GPX) savedInstanceState.get("gpx");
            label.setText(savedInstanceState.getString("label"));

            sendBtn = (Button) findViewById(R.id.sendFile);
            sendBtn.setEnabled(savedInstanceState.getBoolean("sendBtnState"));
            viewBtn = (Button) findViewById(R.id.viewResults);
            viewBtn.setEnabled(savedInstanceState.getBoolean("viewBtnState"));
            results = (CustomMap<String, GPX>) savedInstanceState.get("results");
            request = (Request) savedInstanceState.get("request");
        }

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


    public String getFileNameFromUri(Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (cursor != null && cursor.moveToFirst() && index > 0) {
                    fileName = cursor.getString(index);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (fileName == null) {
            fileName = uri.getPath();
            int slashIndex = fileName.lastIndexOf('/');
            if (slashIndex != -1) {
                fileName = fileName.substring(slashIndex + 1);
            }
        }
        return fileName;
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Uri uri = result.getData().getData();

                    String fileName = getFileNameFromUri(uri);

                    gpx = new GPX("", fileName, username);
                    gpx.setText(readFileContent(uri));


                    request = new Request("gpx", username, gpx);

                    label.setText(fileName);
                    sendBtn.setEnabled(true);


                }
            });


    private void createBarChart() {
        barChart = findViewById(R.id.barChart);
        getBarEntries();
        barDataSet = new BarDataSet(barEntriesArrayList, "Geeks for Geeks");

        barData = new BarData(barDataSet);

        barDataSet.setDrawValues(false);


        barData.setBarWidth(0.5f);

        barChart.setData(barData);
        // adding color to our bar data set.
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        // setting text color.
        barDataSet.setValueTextColor(Color.BLACK);

        // setting text size
        barDataSet.setValueTextSize(16f);
        barChart.setDescription("");
        barChart.animateX(1000);
        barChart.getAxisRight().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setDrawLabels(false);


        barChart.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);



    }

    private void getBarEntries() {
        // creating a new array list
        barEntriesArrayList = new ArrayList<>();

        // adding new entry to our array list with bar
        // entry and passing x and y axis value to it.
        barEntriesArrayList.add(new BarEntry(1f, 4));


        barEntriesArrayList.add(new BarEntry(2f, 2));

    }

    private void createAlert(String title, String message) {
        new AlertDialog.Builder(MainActivity.this)
                .setCancelable(true)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null).create().show();


    }

}