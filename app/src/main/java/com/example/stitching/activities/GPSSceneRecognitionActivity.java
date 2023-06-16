package com.example.stitching.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.stitching.GPS.CoordinateConverter;
import com.example.stitching.GPS.GPSPoint;
import com.example.stitching.GPS.GPSPointFactory;
import com.example.stitching.R;

public class GPSSceneRecognitionActivity extends AppCompatActivity {
    private static final double X_DISTANCE_METERS = 6620.86;
    private static final double Y_DISTANCE_METERS = 2716.41;

    private Bitmap mapImage;
    private GPSPoint centerPoint;
    private double xDistancePerPixel, yDistancePerPixel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpsscene_recognition);

        centerPoint = GPSPointFactory.fromGPSCoords(32.10246376, 35.19198764, 604.75964759);
        ImageView surface = findViewById(R.id.mapView);
        mapImage = BitmapFactory.decodeResource(getResources(), R.drawable.ariel);

        xDistancePerPixel = X_DISTANCE_METERS / mapImage.getWidth();
        yDistancePerPixel = Y_DISTANCE_METERS / mapImage.getHeight();

        Bitmap mapCopy = mapImage.copy(Bitmap.Config.ARGB_8888, true);
        Canvas pen = new Canvas(mapCopy);
        // Define the square dimensions and position
        int left = 500;   // Left coordinate of the square
        int top = 200;    // Top coordinate of the square
        int size = 200;   // Size of the square (width and height are the same)

        int x_center = mapImage.getWidth() / 2;
        int y_center = mapImage.getHeight() / 2;

        double xChange = (left - x_center) * xDistancePerPixel;
        double yChange = (top - y_center) * yDistancePerPixel;

        GPSPoint destination = GPSPointFactory.fromVelocity(centerPoint, xChange, yChange, 0);
        double[] coords = CoordinateConverter.xyzToLatLonDegrees(new double[]{destination.x(), destination.y(), destination.z()});

        // Draw to the screen
        Paint paint = new Paint();
        paint.setColor(Color.RED);  // Set the color of the square
        paint.setStyle(Paint.Style.STROKE);  // Set the style to stroke (outline)
        paint.setStrokeWidth(10);

        pen.drawLine(x_center, y_center, left, top, paint);

        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(30);
        pen.drawPoint(left, top, paint);
        pen.drawPoint(x_center, y_center, paint);
        surface.setImageBitmap(mapCopy);

        Log.i("values", "X Distance: " + X_DISTANCE_METERS + ", Y Distance: " + Y_DISTANCE_METERS);
        Log.i("values", "X Distance per Pixel: " + xDistancePerPixel + ", Y Distance per Pixel: " + yDistancePerPixel);
    }
}