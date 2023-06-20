package com.example.activitytracker;


import android.graphics.Color;
import android.graphics.Paint;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChartBuilder {


    BarChart barChart;
    RadarChart radarChart;
    String type;
    CustomMap<String,Double> user;
    CustomMap<String,Double> community;




    public ChartBuilder(RadarChart radarChart){
        this.radarChart = radarChart;
    }

    public ChartBuilder(BarChart barChart){this.barChart = barChart;}


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

    public void buildBar(String type, CustomMap<String,Double> user, CustomMap<String,Double> community){

        this.type = type;
        this.user = user;
        this.community = community;


        barChart.setExtraTopOffset(-30f);
        barChart.setExtraBottomOffset(10f);
        barChart.setExtraLeftOffset(70f);
        barChart.setExtraRightOffset(70f);

        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);




        // scaling can now only be done on x- and y-axis separately
        barChart.setPinchZoom(false);

        barChart.setDrawGridBackground(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextColor(Color.LTGRAY);
        xAxis.setTextSize(13f);
        xAxis.setLabelCount(3);
        xAxis.setCenterAxisLabels(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new MyAxisValueFormatter());




        YAxis left = barChart.getAxisLeft();
        left.setDrawLabels(false);
        left.setSpaceTop(25f);
        left.setSpaceBottom(25f);
        left.setDrawAxisLine(true);
        left.setDrawGridLines(false);
        left.setDrawZeroLine(true); // draw a zero line
        left.setZeroLineColor(Color.WHITE);
        left.setZeroLineWidth(0.7f);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);


        Description description = barChart.getDescription();
        description.setEnabled(true);
        description.setTextSize(12f);
        description.setTextColor(Color.WHITE);
        description.setText("Difference% from global average for each metric");
        description.setPosition(barChart.getWidth()-20f , 100f);

        barChart.setDescription(description);



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



        // THIS IS THE ORIGINAL DATA YOU WANT TO PLOT
        final List<Data> data = new ArrayList<>();
        data.add(new Data(0f, userTime, "12-29"));
        data.add(new Data(1f,   userDistance, "12-30"));
        data.add(new Data(2f, userElevation, "12-31"));





        setData(data);
    }

    private class MyValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return String.valueOf( value);
        }
    }

    private class MyAxisValueFormatter extends ValueFormatter {
        private final String[] labels = {"Time", "Distance", "Elevation"};

        @Override
        public String getFormattedValue(float value) {
            int index = (int) value;
            if (index >= 0 && index < labels.length) {
                return labels[index];
            }
            return "";
        }
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

    private void setData(List<Data> dataList) {

        ArrayList<BarEntry> values = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();



        int purple = Color.rgb(95,12,170);
        int orange = Color.rgb(236,117,87);


        for (int i = 0; i < dataList.size(); i++) {

            Data d = dataList.get(i);
            BarEntry entry = new BarEntry(d.xValue, d.yValue);
            values.add(entry);

            // specific colors
            if (d.yValue >= 0)
                colors.add(orange);
            else
                colors.add(purple);
        }

        BarDataSet set;

        if (barChart.getData() != null &&
                barChart.getData().getDataSetCount() > 0) {
            set = (BarDataSet) barChart.getData().getDataSetByIndex(0);
            set.setValues(values);
            barChart.getData().notifyDataChanged();
            barChart.notifyDataSetChanged();
        } else {
            set = new BarDataSet(values, "Values");
            set.setColors(colors);
            set.setValueTextColors(colors);

            BarData data = new BarData(set);

            data.setValueFormatter(new MyValueFormatter());
            data.setValueTextSize(13f);


            data.setBarWidth(0.8f);

            barChart.setData(data);
            barChart.invalidate();
        }
    }


    private class Data {

        final String xAxisValue;
        final float yValue;
        final float xValue;

        Data(float xValue, float yValue, String xAxisValue) {
            this.xAxisValue = xAxisValue;
            this.yValue = yValue;
            this.xValue = xValue;
        }
    }




}

