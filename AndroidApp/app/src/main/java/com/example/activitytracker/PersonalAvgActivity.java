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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;

public class PersonalAvgActivity extends AppCompatActivity {

    Handler myHandler;
    String username;
    String server;

    TextView text;
    TextView mess;
    TextView total;

    BarChart chart;

    Request request;

    Response response;

    CustomMap<String,Double> community;
    CustomMap<String,Double> user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_avg);

        mess = (TextView) findViewById(R.id.message);
        text = (TextView) findViewById(R.id.results);
        total = (TextView) findViewById(R.id.totalResults);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        server = intent.getStringExtra("server");





        chart = (BarChart) findViewById(R.id.chart);

        ChartBuilder builder = new ChartBuilder(chart);




        myHandler = new Handler(Looper.getMainLooper(),
                new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message message) {
                        response = (Response) message.getData().getSerializable("response");
                        if(response.getType().equals("user_average")){

                            text.setVisibility(View.VISIBLE);
                            user = new CustomMap<>(response.getResults());

                            String resultText = "";
                            Double time = user.get("averageTime");
                            Double distance = user.get("averageDistance");
                            Double elevation = user.get("averageElevation");

                            int seconds = time.intValue();
                            int hours = seconds/3600;
                            int minutes = (seconds%3600)/60;
                            seconds = seconds%60;




                            resultText = String.format("Average Time: %d h %d m %d s\n"+
                                            "Average Distance: %.2f km\n"+
                                            "Average Elevation: %.2f m\n",

                                    hours,
                                    minutes,
                                    seconds,
                                    distance,
                                    elevation);

                            text.setText(resultText);
                            resultText = "";
                            time = user.get("totalTime");
                            distance = user.get("totalDistance");
                            elevation = user.get("totalElevation");

                            seconds = time.intValue();
                            hours = seconds/3600;
                            minutes = (seconds%3600)/60;
                            seconds = seconds%60;




                            resultText = String.format("Total Time: %d h %d m %d s\n"+
                                            "Total Distance: %.2f km\n"+
                                            "Total Elevation: %.2f m\n",

                                    hours,
                                    minutes,
                                    seconds,
                                    distance,
                                    elevation);

                            total.setText(resultText);
                            total.setVisibility(View.VISIBLE);
                            mess.setVisibility(View.INVISIBLE);

                        }
                        else{
                            community = new CustomMap<>(response.getResults());
                            builder.buildBar("user",user,community);
                            chart.setVisibility(View.VISIBLE);
                        }




                        return true;
                    }
                });



        request = new Request("user_average", username);
        MyThread myThread = new MyThread(request,myHandler, server);
        myThread.start();

        request = new Request("total_average", username);
        MyThread myThread2 = new MyThread(request,myHandler, server);
        myThread2.start();


        text.setText("Waiting");



    }


}