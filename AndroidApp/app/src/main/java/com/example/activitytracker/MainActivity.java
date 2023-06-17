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
import androidx.core.content.ContextCompat;

import android.Manifest;
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
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
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
import com.google.gson.Gson;


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
    CustomMap<String, Double> community = new CustomMap<>();

    public static final String SHARED_PREFS = "sharedPrefs";

    private String username;


    public static String CHANNEL_1_ID = "channel1";
    private NotificationManagerCompat notificationManager;

    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        community.put("averageTime", 0.0);
        community.put("averageDistance", 0.0);
        community.put("averageElevation", 0.0);

        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");


        label = (TextView) findViewById(R.id.label);

        createNotificationChannels();
        notificationManager = NotificationManagerCompat.from(this);

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

        loadData();

        myHandler = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message message) {

                        Response response = (Response) message.getData().getSerializable("response");
                        if (response.getType().equals("gpx")) {
                            GPX responseGPX = response.getGPX();
                            results.put(responseGPX.getFileName(), responseGPX);

                            if (community.get("averageTime") == 0.0) {
                                community.put("averageTime", responseGPX.getResults().get("totalTime"));
                                community.put("averageElevation", responseGPX.getResults().get("totalElevation"));
                                community.put("averageDistance", responseGPX.getResults().get("totalDistance"));
                            }
                            viewBtn.setEnabled(true);
                            createAlert("GPX Result", "Results received from server for file: " + responseGPX.getFileName());
                            sendChannel1Notification(MainActivity.this, responseGPX.getFileName());
                            writeFile(responseGPX.toString(),responseGPX.getFileName()+"_results.txt");
                        }
                        if (response.getType().equals("total_average")) {
                            community = new CustomMap(response.getResults());
                        }

                        //saveData();

                        return true;
                    }
                });


        chooseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilePicker(Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()));
                saveData();
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                request = new Request("gpx", username, gpx);
                MyThread myThread = new MyThread(request, myHandler);
                myThread.start();
                saveData();

            }
        });

        viewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                label.setText(results.get(gpx.getFileName()).toString());
                CustomMap<String, Double> map = new CustomMap(results.get(gpx.getFileName()).getResults());


                builder.buildRadar("route", map, community);
                chart.setVisibility(View.VISIBLE);
                saveData();
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

        saveData();

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        sharedPreferences.edit().clear();

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

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This is Channel 1");


            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);

        }


    }

    public void sendChannel1Notification(Context context, String filename) {
        Intent activityIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, activityIntent, PendingIntent.FLAG_IMMUTABLE);


        Notification notification = new NotificationCompat.Builder(context, CHANNEL_1_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setColor(Color.BLUE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setContentText("Results received for file: "+filename)
                .setContentTitle("Results")
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission( MainActivity.this,android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            String [] permissions = {android.Manifest.permission.POST_NOTIFICATIONS};
            ActivityCompat.requestPermissions(this,permissions,1);

        }
        notificationManager.notify(1, notification);
    }
    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("username", username);
        editor.putBoolean("viewBtnState", viewBtn.isEnabled());
        editor.putBoolean("sendBtnState", sendBtn.isEnabled());
        editor.putString("results",gson.toJson(results));
        editor.putString("gpx",gson.toJson(gpx));
        editor.putString("community",gson.toJson(community));




        editor.apply();

        //Toast.makeText(this, "Results saved", Toast.LENGTH_SHORT).show();
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        if (sharedPreferences.contains("username")){
            username = sharedPreferences.getString("username", null);
        }
        if(sharedPreferences.contains("viewBtnState")){
            viewBtn.setEnabled(sharedPreferences.getBoolean("viewBtnState", false));
        }
        if(sharedPreferences.contains("sendBtnState")){
            sendBtn.setEnabled(sharedPreferences.getBoolean("sendBtnState", false));
        }
        if(sharedPreferences.contains("community")){
            community = gson.fromJson(sharedPreferences.getString("community", null),CustomMap.class);
        }
        if(sharedPreferences.contains("results")){
            results = gson.fromJson(sharedPreferences.getString("results",null),CustomMap.class);
        }
        if(sharedPreferences.contains("gpx")){
            gpx = gson.fromJson(sharedPreferences.getString("gpx",null), GPX.class);
        }

    }

    public boolean isExternalStorageWritable(){
        if( Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            Log.i("State","External Storage is writable");
            return true;
        }
        return false;

    }

    public void writeFile(String text, String filename){

        if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            String [] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this,permissions,1);
        }

        if (isExternalStorageWritable()){
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),filename);
            try{
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(text.getBytes());
                fos.close();
                Toast.makeText(this,"File saved", Toast.LENGTH_SHORT).show();
            }
            catch(IOException e){
                e.printStackTrace();

            }


        }
        else{
            Toast.makeText(this,"Error saving file", Toast.LENGTH_SHORT).show();
        }


    }

    public boolean checkPermission(String permission){
        int check = ContextCompat.checkSelfPermission(this,permission);
        return (check == PackageManager.PERMISSION_GRANTED);

    }


}