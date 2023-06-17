package com.example.activitytracker;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ColorRes;
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

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;





public class MainActivity extends AppCompatActivity {

    TextView label;

    Button chooseBtn;
    Button sendBtn;
    Button viewBtn;

    private RadarChart chart;



    Handler myHandler;

    Map<String, String> gpxData = new HashMap();

    GPX gpx;

    Request request;

    CustomMap<String, GPX> results = new CustomMap<>();
    CustomMap<String,Double> community = new CustomMap<>();


    private String username;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        community.put("averageTime",0.0);
        community.put("averageDistance",0.0);
        community.put("averageElevation",0.0);

        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");


        label = (TextView) findViewById(R.id.label);



        // Create the bar chart
        chart = findViewById(R.id.chart);

        ChartBuilder builder = new ChartBuilder(chart);





        chooseBtn = (Button) findViewById(R.id.chooseFile);
        sendBtn = (Button) findViewById(R.id.sendFile);
        viewBtn = (Button) findViewById(R.id.viewResults);
        label.setText("User " + username);



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
                        if(response.getType().equals("gpx")){
                            GPX responseGPX = response.getGPX();
                            results.put(responseGPX.getFileName(), responseGPX);

                            if (community.get("averageTime") == 0.0){
                                community.put("averageTime",responseGPX.getResults().get("totalTime"));
                                community.put("averageElevation",responseGPX.getResults().get("totalElevation"));
                                community.put("averageDistance",responseGPX.getResults().get("totalDistance"));
                            }
                            viewBtn.setEnabled(true);
                            createAlert("GPX Result", "Results received from server for file: "+responseGPX.getFileName());

                        }
                        if(response.getType().equals("total_average")){
                            community = new CustomMap(response.getResults());
                        }


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
                CustomMap<String,Double> map = new CustomMap(results.get(gpx.getFileName()).getResults());


                builder.buildRadar( "route",map,community);
                chart.setVisibility(View.VISIBLE);

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




    private void createAlert(String title, String message) {
        new AlertDialog.Builder(MainActivity.this)
                .setCancelable(true)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null).create().show();


    }

    private void setData(String type, CustomMap<String,Double> user, CustomMap<String,Double> community) {

        ArrayList<RadarEntry> entries1 = new ArrayList<>();
        ArrayList<RadarEntry> entries2 = new ArrayList<>();

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        if(type.equals("route")){
            entries1.add(new RadarEntry(user.get("totalTime").floatValue()));
            entries1.add(new RadarEntry(user.get("totalDistance").floatValue()));
            entries1.add(new RadarEntry(user.get("totalElevation").floatValue()));
        }else{
            entries1.add(new RadarEntry(user.get("averageTime").floatValue()));
            entries1.add(new RadarEntry(user.get("averageDistance").floatValue()));
            entries1.add(new RadarEntry(user.get("averageElevation").floatValue()));
        }


        entries2.add(new RadarEntry(community.get("averageTime").floatValue()));
        entries2.add(new RadarEntry(community.get("averageDistance").floatValue()));
        entries2.add(new RadarEntry(community.get("averageElevation").floatValue()));

        RadarDataSet set1 = new RadarDataSet(entries1, "You");
        set1.setColor(Color.rgb(103, 110, 129));
        set1.setFillColor(Color.rgb(103, 110, 129));
        set1.setDrawFilled(true);
        set1.setFillAlpha(180);
        set1.setLineWidth(2f);
        set1.setDrawHighlightCircleEnabled(true);
        set1.setDrawHighlightIndicators(false);

        RadarDataSet set2 = new RadarDataSet(entries2, "Community Average");
        set2.setColor(Color.rgb(121, 162, 175));
        set2.setFillColor(Color.rgb(121, 162, 175));
        set2.setDrawFilled(true);
        set2.setFillAlpha(180);
        set2.setLineWidth(2f);
        set2.setDrawHighlightCircleEnabled(true);
        set2.setDrawHighlightIndicators(false);

        ArrayList<IRadarDataSet> sets = new ArrayList<>();
        sets.add(set1);
        sets.add(set2);

        RadarData data = new RadarData(sets);

        data.setValueTextSize(8f);
        data.setDrawValues(false);
        data.setValueTextColor(Color.BLACK);

        chart.setData(data);
        chart.invalidate();
    }
    private void setDefaultData(){
        float mul = 80;
        float min = 20;
        int cnt = 5;

        ArrayList<RadarEntry> entries1 = new ArrayList<>();
        ArrayList<RadarEntry> entries2 = new ArrayList<>();

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        for (int i = 0; i < cnt; i++) {
            float val1 = (float) (Math.random() * mul) + min;
            entries1.add(new RadarEntry(val1));

            float val2 = (float) (Math.random() * mul) + min;
            entries2.add(new RadarEntry(val2));
        }

        RadarDataSet set1 = new RadarDataSet(entries1, "Last Week");
        set1.setColor(Color.rgb(103, 110, 129));
        set1.setFillColor(Color.rgb(103, 110, 129));
        set1.setDrawFilled(true);
        set1.setFillAlpha(180);
        set1.setLineWidth(2f);
        set1.setDrawHighlightCircleEnabled(true);
        set1.setDrawHighlightIndicators(false);

        RadarDataSet set2 = new RadarDataSet(entries2, "This Week");
        set2.setColor(Color.rgb(121, 162, 175));
        set2.setFillColor(Color.rgb(121, 162, 175));
        set2.setDrawFilled(true);
        set2.setFillAlpha(180);
        set2.setLineWidth(2f);
        set2.setDrawHighlightCircleEnabled(true);
        set2.setDrawHighlightIndicators(false);

        ArrayList<IRadarDataSet> sets = new ArrayList<>();
        sets.add(set1);
        sets.add(set2);

        RadarData data = new RadarData(sets);

        data.setValueTextSize(8f);
        data.setDrawValues(false);
        data.setValueTextColor(Color.WHITE);

        chart.setData(data);
        chart.invalidate();
    }



}