package com.example.trider.smartbarui;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.util.Arrays;


public class TestKeyBoard extends ActionBarActivity {

    String pinString = "";
    private XYPlot plot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_key_board);


        // initialize our XYPlot reference:
        plot = (XYPlot) findViewById(R.id.mySimpleXYPlot);

        // Create a couple arrays of y-values to plot:
        Number[] series1Numbers = {1, 8, 5, 2, 7, 4, 5};
        Number[] series2Numbers = {4, 6, 3, 8, 2, 10, 6};

        // Turn the above arrays into XYSeries':
        XYSeries series1 = new SimpleXYSeries(
                Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
                "Series1");                             // Set the display title of the series

        // same as above
        XYSeries series2 = new SimpleXYSeries(Arrays.asList(series2Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series2");

        // Create a formatter to use for drawing a series using LineAndPointRenderer
        // and configure it from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.setPointLabelFormatter(new PointLabelFormatter());
        series1Format.configure(getApplicationContext(),
                R.xml.line_point_formatter_with_plf1);

        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);

        // same as above:
        LineAndPointFormatter series2Format = new LineAndPointFormatter();
        series2Format.setPointLabelFormatter(new PointLabelFormatter());
        series2Format.configure(getApplicationContext(),
                R.xml.line_point_formatter_with_plf2);
        plot.addSeries(series2, series2Format);

        // reduce the number of range labels
        plot.setTicksPerRangeLabel(3);
        plot.getGraphWidget().setDomainLabelOrientation(-45);

    }


    public void EnterPin(View view){
        switch(view.getId()){
            case R.id.keyOne:
                pinString+="1";
                break;
            case R.id.keyTwo:
                pinString+="2";
                break;
            case R.id.keyThree:
                pinString+="3";
                break;
            case R.id.keyFour:
                pinString+="4";
                break;
            case R.id.keyFive:
                pinString+="5";
                break;
            case R.id.keySix:
                pinString+="6";
                break;
            case R.id.keySeven:
                pinString+="7";
                break;
            case R.id.keyEight:
                pinString+="8";
                break;
            case R.id.keyNine:
                pinString+="9";
                break;
            case R.id.keyZero:
                pinString+="0";
                break;
            case R.id.keyBack:
                if(pinString.length() == 0){return;}
                pinString = pinString.substring(0,pinString.length()-1);
                break;
            case R.id.keyEnter:
                if(pinString.length() < 11){
                    Toast.makeText(getApplicationContext(),"Hey not long enough",Toast.LENGTH_SHORT).show();
                }else if(pinString.length()> 11){
                    Toast.makeText(getApplicationContext(),"Hey too long ",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Hey... Ok",Toast.LENGTH_SHORT).show();
                }
                break;
        }
        TextView tView = (TextView) findViewById(R.id.enterField);
        tView.setText(pinString);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test_key_board, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
