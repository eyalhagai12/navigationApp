package com.example.stitching.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.example.stitching.SensorClasses.HeightEstimator;
import com.example.stitching.R;
import com.example.stitching.SensorClasses.IMU;

public class HeightActivity extends AppCompatActivity {
    private static final int CHECK_INTERVAL = 500; // Interval in milliseconds
    private HeightEstimator heightEstimator;
    private IMU imuSensor;
    private Handler handler;
    private Runnable updateHeight;

    private TextView heightText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_height);
        heightText = findViewById(R.id.heightText);

        imuSensor = new IMU(this);
        heightEstimator = new HeightEstimator(this);
        handler = new Handler();
        updateHeight = new Runnable() {
            @Override
            public void run() {
                float[] orientation = imuSensor.getAngles();
                heightText.setText("Height: " + heightEstimator.getHeight() + ", Azimuth: " + orientation[0]);
                handler.postDelayed(updateHeight, CHECK_INTERVAL);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        heightEstimator.startEstimation();
        imuSensor.start();
        handler.postDelayed(updateHeight, CHECK_INTERVAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        heightEstimator.stopEstimation();
        imuSensor.stop();
        handler.removeCallbacks(updateHeight);
    }
}