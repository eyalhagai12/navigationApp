package com.example.stitching.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stitching.R;


public class MainActivity extends AppCompatActivity {
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
    }

}