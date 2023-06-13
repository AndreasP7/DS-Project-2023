package com.example.activitytracker;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    TextView label;

    Button chooseBtn;
    Button sendBtn;
    Button viewBtn;

    LinearLayout chartContainer;

    Handler myHandler;

    Map<String, String> gpxData = new HashMap();

    GPX gpx;

    Request request;

    CustomMap<String, GPX> results = new CustomMap<>();


    private String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");


        label = (TextView) findViewById(R.id.label);

        chartContainer = findViewById(R.id.chartContainer);

        // Create the bar chart
        createBarChart();
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
                        gpx = response.getGPX();
                        results.put(gpx.getFileName(), gpx);

                        viewBtn.setEnabled(true);


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
                createNotification();

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
        int userResult = 10; // User result
        int totalResults = 50; // Total results

        // Calculate the ratio of user result to total results
        float userRatio = (float) userResult / totalResults;
        float totalRatio = 1.0f;

        int containerWidth = chartContainer.getWidth();

        // Calculate the width of each bar based on the ratios
        int userBarWidth = (int) (containerWidth * userRatio);
        int totalBarWidth = (int) (containerWidth * totalRatio);

        // Create a LinearLayout for the user result bar
        LinearLayout.LayoutParams userParams = new LinearLayout.LayoutParams(
                userBarWidth,
                LinearLayout.LayoutParams.MATCH_PARENT,
                0
        );
        View userBar = new View(this);
        userBar.setLayoutParams(userParams);
        userBar.setBackgroundColor(Color.BLUE);

        // Create a LinearLayout for the total results bar
        LinearLayout.LayoutParams totalParams = new LinearLayout.LayoutParams(
                totalBarWidth,
                LinearLayout.LayoutParams.MATCH_PARENT,
                0
        );
        View totalBar = new View(this);
        totalBar.setLayoutParams(totalParams);
        totalBar.setBackgroundColor(Color.GREEN);

        // Add the bars to the chart container
        chartContainer.addView(userBar);
        chartContainer.addView(totalBar);
    }

    private void createNotification() {
        String id = "my_channel_id_01";
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = manager.getNotificationChannel(id);
            if (channel == null) {
                channel = new NotificationChannel(id, "Channel Title", NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("[Channel description]");
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{100, 1000, 200, 340});
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                manager.createNotificationChannel(channel);
            }
        }

        Intent notificationIntent = new Intent(MainActivity.this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity
                    (this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity
                    (this, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, id)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("title")
                .setContentText("Your text description")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[]{100, 1000, 200, 340})
                .setAutoCancel(false)
                .setTicker("Notification");

        NotificationManagerCompat m = NotificationManagerCompat.from(MainActivity.this);

        builder.setContentIntent(pendingIntent);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            m.notify(1, builder.build());
        }


    }
}