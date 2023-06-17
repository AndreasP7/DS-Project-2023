package com.example.activitytracker;


import android.graphics.Color;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;

import java.util.ArrayList;
import java.util.Map;

public class ChartBuilder {

    RadarChart radarChart;
    String type;
    CustomMap<String,Double> user;
    CustomMap<String,Double> community;




    public ChartBuilder(RadarChart radarChart){
        this.radarChart = radarChart;
    }


    public void buildRadar(String type, CustomMap<String,Double> user, CustomMap<String,Double> community){
        radarChart.getDescription().setEnabled(false);
        this.type = type;
        this.user = user;
        this.community = community;
        radarChart.setWebLineWidth(1f);
        radarChart.setWebColor(Color.LTGRAY);
        radarChart.setWebLineWidthInner(1f);
        radarChart.setWebColorInner(Color.LTGRAY);
        radarChart.setWebAlpha(100);



        setDataPerc();



        radarChart.animateXY(1400, 1400, Easing.EaseInOutQuad);

        XAxis xAxis = radarChart.getXAxis();
        String [] columnNames = {"Time", "Distance", "Elevation"};
        xAxis.setTextSize(12f);
        xAxis.setYOffset(200f);
        xAxis.setXOffset(0f);

        xAxis.setValueFormatter(new IndexAxisValueFormatter(columnNames));


        xAxis.setTextColor(Color.BLACK);

        YAxis yAxis = radarChart.getYAxis();

        yAxis.setLabelCount(5, false);
        yAxis.setTextSize(9f);
        yAxis.setAxisMinimum(-100);
        yAxis.setAxisMaximum(100);
        yAxis.setDrawLabels(false);
        String[] yAxisLabels = {"-100", "-50", "0", "50", "100"};
        CustomYAxisValueFormatter yAxisValueFormatter = new CustomYAxisValueFormatter(yAxisLabels);
        yAxis.setValueFormatter(new IndexAxisValueFormatter(yAxisLabels));
        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);



        Legend l = radarChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        l.setXEntrySpace(7f);
        l.setYEntrySpace(5f);
        l.setTextColor(Color.WHITE);


    }

    public RadarChart getRadarChart(){
        return radarChart;
    }



    private void setData() {




        ArrayList<RadarEntry> entries1 = new ArrayList<>();
        ArrayList<RadarEntry> entries2 = new ArrayList<>();


        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        if(this.type.equals("route")){
            entries1.add(new RadarEntry(user.get("totalTime").floatValue()/60));
            entries1.add(new RadarEntry(user.get("totalDistance").floatValue()));
            entries1.add(new RadarEntry(user.get("totalElevation").floatValue()));
        }else{
            entries1.add(new RadarEntry(user.get("averageTime").floatValue()));
            entries1.add(new RadarEntry(user.get("averageDistance").floatValue()));
            entries1.add(new RadarEntry(user.get("averageElevation").floatValue()));
        }



        entries2.add(new RadarEntry(community.get("averageTime").floatValue()/60));
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

        radarChart.setData(data);
        radarChart.invalidate();
    }
    private void setDataPerc() {




        ArrayList<RadarEntry> entries1 = new ArrayList<>();

        float userTime = 0;
        float userDistance= 0;
        float userElevation= 0;

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        if(this.type.equals("route")){
            userTime = user.get("totalTime").floatValue();
            userDistance = user.get("totalDistance").floatValue();
             userElevation= user.get("totalElevation").floatValue();
        }else{
             userTime = user.get("averageTime").floatValue();
             userDistance = user.get("averageDistance").floatValue();
             userElevation= user.get("averageElevation").floatValue();
        }

        float communityTime = community.get("averageTime").floatValue();
        float communityDistance = community.get("averageDistance").floatValue();
        float communityElevation = community.get("averageElevation").floatValue();



        userTime = ((userTime - communityTime)/communityTime) * 100;
        userDistance = ((userDistance - communityDistance)/communityDistance) * 100;
        userElevation = ((userElevation - communityElevation)/communityElevation)*100;
        entries1.add(new RadarEntry(userTime));
        entries1.add(new RadarEntry(userDistance));
        entries1.add(new RadarEntry(userElevation));

        YAxis yAxis = radarChart.getYAxis();
        yAxis.setAxisMaximum(200);

        RadarDataSet set1 = new RadarDataSet(entries1, "You");
        set1.setColor(Color.rgb(103, 110, 129));
        set1.setFillColor(Color.rgb(103, 110, 129));
        set1.setDrawFilled(true);
        set1.setFillAlpha(180);
        set1.setLineWidth(2f);
        set1.setDrawHighlightCircleEnabled(true);
        set1.setDrawHighlightIndicators(false);



        ArrayList<IRadarDataSet> sets = new ArrayList<>();
        sets.add(set1);

        RadarData data = new RadarData(sets);

        data.setValueTextSize(8f);
        data.setDrawValues(false);
        data.setValueTextColor(Color.BLACK);

        radarChart.setData(data);
        radarChart.invalidate();
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

        radarChart.setData(data);
        radarChart.invalidate();
    }
    public class CustomYAxisValueFormatter extends ValueFormatter {
        private String[] labels;

        public CustomYAxisValueFormatter(String[] labels) {
            this.labels = labels;
        }

        @Override
        public String getFormattedValue(float value) {
            int index = (int) value;
            if (index >= 0 && index < labels.length) {
                return labels[index];
            } else {
                return "";
            }
        }
    }

}

