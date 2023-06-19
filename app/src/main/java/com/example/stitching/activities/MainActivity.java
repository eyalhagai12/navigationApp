package com.example.stitching.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stitching.Logging.BackgroundLogging;
import com.example.stitching.R;

import boofcv.android.ConvertBitmap;


public class MainActivity extends AppCompatActivity {
    boolean backgroundStarted = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button stitchingButton = findViewById(R.id.stitchingButton);
        stitchingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, StitchingActivity.class);
                startActivity(i);
            }
        });

        Button gpsButton = findViewById(R.id.SceneRecognitionButton);
        gpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, GPSSceneRecognitionActivity.class);
                startActivity(i);
            }
        });

        Button heightButton = findViewById(R.id.heightEstimation);
        heightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, HeightActivity.class);
                startActivity(i);
            }
        });

        Button stitchingYoni = findViewById(R.id.stitchingYoniButton);
        stitchingYoni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, StitchingYoni.class);
                startActivity(i);
            }
        });

        Button lockOnButton = findViewById(R.id.lockOnActivity);
        lockOnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, LockOnActivity.class);
                startActivity(i);
            }
        });
        Button backgroundLoggingButton = findViewById(R.id.backgroudLogging);
        backgroundLoggingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent i = new Intent(MainActivity.this, LockOnActivity.class);
//                startActivity(i);
                if(!backgroundStarted) {
                    Log.i("mainActivity", "background log starting");

                    startService(new Intent(MainActivity.this, BackgroundLogging.class));
                    backgroundStarted = true;
                }else{
                    Log.i("mainActivity", "log already running");
                }
            }
        });
    }

}