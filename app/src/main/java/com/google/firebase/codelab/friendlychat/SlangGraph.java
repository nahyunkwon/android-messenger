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
    protected void onCreate(Bundle savedInstanceState) { //create a new activity to show graph of using swearwords
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slang_graph);

        showGraph(); //call showGraph() method when the activity is created
    }

    public static com.jjoe64.graphview.GraphView graph_view;

    public void showGraph() { //a method that shows the graph of using swearwords of users

        Timestamp today = new Timestamp(System.currentTimeMillis()); //make a Timestamp instance of current time

        String key = today.toString(); //change it into String

        StringTokenizer str = new StringTokenizer(key, " "); //get YYYY-MM-DD part by using StringTokenizer with " "

        key = str.nextToken();

        str = new StringTokenizer(key, "-"); //get DD part by using StringTokenizer with "-" and for loop
        for(int i =0; i<3; i++){
            key = str.nextToken();
        }

        int k_value = Integer.parseInt(key); //change it into integer value

        int[] array_x = new int[7]; //make an array of x-aixs of the graph
        for(int i=0; i<7; i++){ //put the values of the date of the last 7 days in the array
            array_x[i] = k_value-i;
        }


        int[] array_y = new int[7]; //make an array of y-aixs of the graph

        for(int i=0; i<7; i++){ //put the values of the number of using bad language
            if(!MainActivity.week.containsKey(array_x[i])){
                array_y[i] = 0;
                break;
            }
            array_y[i] = MainActivity.week.get(array_x[i]).size();
        }

        //make a linear graph about the number of using swearwords in the last 7 days using arrays
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
