package com.example.stitching;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.stitching.SensorClasses.HeightEstimator;
import com.example.stitching.SensorClasses.IMU;

import boofcv.android.camera2.VisualizeCamera2Activity;
import boofcv.struct.image.ImageBase;

public class LockOnActivity extends VisualizeCamera2Activity {
    private IMU imuSensor;
    private HeightEstimator heightEstimator;

    private boolean lockOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_on);

        lockOn = false;
        imuSensor = new IMU(this);
        heightEstimator = new HeightEstimator(this);


    }

    @Override
    protected void processImage(ImageBase image) {
        if (lockOn){
            // use angle to calculate the center of the image and move a point in the UI
        }
    }
}