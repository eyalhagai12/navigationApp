package com.example.stitching.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.stitching.R;

public class VisualizeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualize);
        Bitmap stitchedImage = ImageSingleton.getInstance().getStitchedImage();
        ImageView stitchedView = findViewById(R.id.stitchedView);
        stitchedView.setImageBitmap(stitchedImage);
    }

}