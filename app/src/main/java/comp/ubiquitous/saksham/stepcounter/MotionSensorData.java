package comp.ubiquitous.saksham.stepcounter;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by saksham on 9/9/17.
 */

public class MotionSensorData extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private static final String TAG = "StepCounter";
    private final int SIZE = 3;

    final float[] gravity = new float[SIZE];
    final float[] linearAcceleration = new float[SIZE];

    protected ArrayList<List<Float>> mainData = new ArrayList<>();
    protected List<Float> combinedData = new LinkedList<>();
    // this is the raw data

    protected List<Float> changeData = new LinkedList<>();

    private TextView stepsDisplay;
    private LineChart chartX, chartY, chartZ;
    private ArrayList<LineChart> charts = new ArrayList<>(3);

    public MotionSensorData() {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        setContentView(R.layout.activity_accel);
        chartX = (LineChart) findViewById(R.id.chart_x);
        chartY = (LineChart) findViewById(R.id.chart_y);
        chartZ = (LineChart) findViewById(R.id.chart_z);
        charts.add(chartX);
        charts.add(chartY);
        charts.add(chartZ);
        stepsDisplay = (TextView) findViewById(R.id.stepsDisplay);

        for (int i = 0; i < SIZE; i++) {
            mainData.add(new LinkedList<Float>());
        }
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        Log.d(TAG, "Registered the listener...");
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        Log.d(TAG, "Unregistered the listener...");
    }


    private int randomAdd = 0;

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // REading the accelorometer data
            final float alpha = 0.8f;

            // Isolate the force of gravity with the low-pass filter.
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            // Remove the gravity contribution with the high-pass filter.
            linearAcceleration[0] = event.values[0] - gravity[0];
            linearAcceleration[1] = event.values[1] - gravity[1];
            linearAcceleration[2] = event.values[2] - gravity[2];


            if (randomAdd % 10 == 0) {
                for (int i = 0; i < 3; i++) {
                    List<Float> someData = mainData.get(i);
                    if (someData.size() > 15)
                        someData.remove(0);
                    someData.add(linearAcceleration[i]);
                    mainData.set(i, someData);
                }
                normalizeData(mainData);
                updateCharts(mainData);
                randomAdd /= 10;
            }
            randomAdd++;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.i(TAG, "onAccuracyChanged");
    }


    public void updateCharts(ArrayList<List<Float>> mainData) {
        for (int i = 0; i < mainData.size(); i++) {
            try {
                List<Float> data = mainData.get(i);
                if (i == 2){
                    data = changeData;
                }
                List<Entry> entries = new ArrayList<Entry>();
                // turn your data into Entry objects
                for (int j = 0; j < data.size(); j++)
                    entries.add(new Entry(j, data.get(j)));

                Description description = new Description();
                description.setText("Accelorometer Data");

                LineDataSet dataSet;
                if (i == 0) {
                    dataSet = new LineDataSet(entries, "X-Axis Data");
                    dataSet.setColor(Color.RED);
                } else if (i == 1) {
                    dataSet = new LineDataSet(entries, "Y-Axis Data");
                    dataSet.setColor(Color.BLUE);
                } else {
                    dataSet = new LineDataSet(entries, "Combined Data");
                    dataSet.setColor(Color.GREEN);
                    charts.get(2).getAxisLeft().setAxisMaximum(9.8f);
                    charts.get(2).getAxisLeft().setAxisMinimum(-9.8f);
                    charts.get(2).getAxisRight().setAxisMaximum(9.8f);
                    charts.get(2).getAxisRight().setAxisMinimum(-9.8f);
                }
                dataSet.setValueTextColor(Color.BLACK);
                LineData lineData = new LineData(dataSet);
                charts.get(i).setData(lineData);
                //   chartX.getAxisLeft().setAxisMaximum(9.80f);
                charts.get(i).setDescription(description);
                charts.get(i).invalidate(); // refresh

            } catch (NegativeArraySizeException ne) {
                Log.e(TAG, "Failing to add data to chart");
            }
        }
    }


    private double minRawDataThreshold = 0.4;
    private boolean walkingCurrently = false;
    private int walkCounter = 0;
    private int steps = 0;
    private int windowSize = 16;

    private void normalizeData(ArrayList<List<Float>> allData) {
//        Log.i(TAG, "Entering calculate steps method");
        Log.i(TAG, allData.toString());
        int arraySize = allData.get(0).size();
        // aggregate the data. Find the max value
        float base = getMaxAbsVal(
                        allData.get(0).get(arraySize - 1),
                        allData.get(1).get(arraySize - 1),
                        allData.get(2).get(arraySize - 1));
        if (Math.abs(base) < minRawDataThreshold)
            base = 0;

        combinedData.add(base);
        changeData.add(base);

        if (combinedData.size() > windowSize)
            combinedData.remove(0);
        if (changeData.size() > windowSize)
            changeData.remove(0);

//        // Subtract the median for the window
        float median = getMedian(combinedData);
        for (int i=0; i<combinedData.size(); i++){
            changeData.set(i, combinedData.get(i) - median);
        }

//        Log.e(TAG, combinedData.toString());
        if (!walkingCurrently) {
            walkingCurrently = isWalking();
        } else{
            walkCounter++;
            // aprox 10 secs
            if (walkCounter == 2*windowSize){
                walkCounter = 0;
                walkingCurrently = false;
            }
        }
        if (walkingCurrently) {
            // Do peak detection
            Log.e(TAG, "Walking currently and the data is "+changeData.get(changeData.size()-1));
            if (changeData.get(changeData.size()-1) > 0.70)
                steps++;
            stepsDisplay.setText("Step Count:"+steps);
        }
    }

    private double minWalkThreshold = 0.15, maxWalkThreshold = 6.4;
    public boolean isWalking(){
        // Differentiate
//        if (changeData.size() > 0) {
//            float prev = combinedData.get(combinedData.size()-1);
//            if (prev < 0)
//                base += prev;
//            else
//                base -= prev;
//        }
        int underControl = 0;
        for (int i=windowSize/2; i<combinedData.size(); i++){
            float diff = changeData.get(i) - changeData.get(i-1);
            if(diff > minWalkThreshold && diff < maxWalkThreshold)
                underControl++;
        }
//        Log.e(TAG, "Is this undercontrol "+underControl);
        return (underControl >= 3);
    }


    private float getMedian(List<Float> ls){
        List<Float> l2 = new ArrayList<>(ls);
        int size = l2.size();
        if (size > 0){
            if (size % 2 == 1)
                return l2.get(size/2);
            else
                return (l2.get((size-1)/2) + l2.get(size/2))/2;
        }
        return 0;
    }


    private float getMaxAbsVal(float f1, float f2, float f3){
        float max = f2;
        if (Math.abs(f1) > Math.abs(f2)) max = f1;
        if (Math.abs(f3) > Math.abs(max)) max = f3;

        return max;
    }

}
