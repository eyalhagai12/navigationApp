package com.example.stitching.activities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.FrameLayout;

import com.example.stitching.R;
import com.example.stitching.SensorClasses.HeightEstimator;
import com.example.stitching.SensorClasses.IMU;

import boofcv.android.ConvertBitmap;
import boofcv.android.camera2.VisualizeCamera2Activity;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageType;

public class LockOnActivity extends VisualizeCamera2Activity {
    private static final int CHECK_INTERVAL = 100; // Interval in milliseconds
    private IMU imuSensor;
    private HeightEstimator heightEstimator;
    private float roll, pitch;
    private boolean lockOn;

    private Handler handler;
    private Runnable updateOrientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_on);
        FrameLayout surface = findViewById(R.id.lockOnImage);

        lockOn = false;
        imuSensor = new IMU(this);
        heightEstimator = new HeightEstimator(this);

        handler = new Handler();
        updateOrientation = new Runnable() {
            @Override
            public void run() {
                float[] orientation = imuSensor.getAngles();
                pitch = orientation[0];
                roll = orientation[1];
                handler.postDelayed(updateOrientation, CHECK_INTERVAL);
                Log.d("orientation", "Pitch: " + pitch + ", Roll: " + roll);
            }
        };


        setImageType(ImageType.pl(3, GrayU8.class));
        startCamera(surface, null);
    }


    @Override
    protected void processImage(ImageBase image) {
    }

    @Override
    protected void renderBitmapImage(BitmapMode mode, ImageBase image) {
        ConvertBitmap.boofToBitmap(image, bitmap, bitmapTmp);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);

        int x = (int) (((bitmap.getWidth() / 2) * (1 - pitch / 40)));
        int y = (int) (((bitmap.getHeight() / 2) * (1 - roll / 40)));
        Log.i("center", "[X: " + x + ", Y: " + y + "]");
        canvas.drawPoint(x, y, paint);
    }

    @Override
    protected void onResume() {
        super.onResume();
        imuSensor.start();
        handler.postDelayed(updateOrientation, CHECK_INTERVAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        imuSensor.stop();
        handler.removeCallbacks(updateOrientation);
    }
}