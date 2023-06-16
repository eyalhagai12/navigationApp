package com.example.stitching.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.example.stitching.HeightEstimation.HeightEstimator;
import com.example.stitching.R;

public class HeightActivity extends AppCompatActivity {
    private static final int CHECK_INTERVAL = 500; // Interval in milliseconds
    private HeightEstimator heightEstimator;
    private Handler handler;
    private Runnable updateHeight;

    private TextView heightText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_height);
        heightText = findViewById(R.id.heightText);

        heightEstimator = new HeightEstimator(this);
        handler = new Handler();
        updateHeight = new Runnable() {
            @Override
            public void run() {
                Log.d("Height", "Height: " + heightEstimator.getHeight());
                heightText.setText("Height: " + heightEstimator.getHeight());
                handler.postDelayed(updateHeight, CHECK_INTERVAL);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        heightEstimator.startEstimation();
        handler.postDelayed(updateHeight, CHECK_INTERVAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        heightEstimator.stopEstimation();
        handler.removeCallbacks(updateHeight);
    }
}