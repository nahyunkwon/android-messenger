package com.google.firebase.codelab.friendlychat;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.sql.Timestamp;
import java.util.StringTokenizer;

public class SlangGraph extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slang_graph);

        showGraph();
    }

    public static com.jjoe64.graphview.GraphView graph_view;

    public void showGraph() {

        Timestamp today = new Timestamp(System.currentTimeMillis());

        String key = today.toString();

        StringTokenizer str = new StringTokenizer(key, " ");

        key = str.nextToken();

        str = new StringTokenizer(key, "-");
        for(int i =0; i<3; i++){
            key = str.nextToken();
        }

        int k_value = Integer.parseInt(key);

        int[] array_x = new int[7];
        for(int i=0; i<7; i++){
            array_x[i] = k_value-i;
        }


        int[] array_y = new int[7];

        for(int i=0; i<7; i++){
            if(!MainActivity.week.containsKey(array_x[i])){
                array_y[i] = 0;
                break;
            }
            array_y[i] = MainActivity.week.get(array_x[i]).size();
        }


        int numx = array_x[6];
        int numy = array_y[6];

        graph_view = (com.jjoe64.graphview.GraphView) findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(array_x[6], array_y[6]),
                new DataPoint(array_x[5], array_y[5]),
                new DataPoint(array_x[4], array_y[4]),
                new DataPoint(array_x[3], array_y[3]),
                new DataPoint(array_x[2], array_y[2]),
                new DataPoint(array_x[1], array_y[1]),
                new DataPoint(array_x[0], array_y[0])
        });
        graph_view.addSeries(series);

        //styling series
        series.setTitle("slang Count 1");
        series.setColor(Color.GREEN);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(10);
        series.setThickness(8);
    }
}
