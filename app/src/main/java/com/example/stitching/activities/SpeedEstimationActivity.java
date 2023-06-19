package com.example.stitching.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stitching.GPS.GPSPoint;
import com.example.stitching.GPS.GPSPointFactory;
import com.example.stitching.Logging.MotionLogger;
import com.example.stitching.R;
import com.example.stitching.SensorClasses.HeightEstimator;
import com.example.stitching.SensorClasses.IMU;
import com.example.stitching.Stitching.StitchingUtils;

import boofcv.android.ConvertBitmap;
import boofcv.android.camera2.VisualizeCamera2Activity;
import boofcv.core.image.ConvertImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;
import georegression.struct.homography.Homography2D_F64;
import georegression.struct.point.Point2D_F64;
import georegression.transform.homography.HomographyPointOps_F64;

public class SpeedEstimationActivity extends VisualizeCamera2Activity {
    private static final int SKIP_FRAMES = 1000;
    private TextView speedText;
    private FrameLayout videoFeed;
    private HeightEstimator heightEstimator;
    private MotionLogger logger;
    private IMU imuSensor;
    private float azimuth, pitch, roll;

    private Homography2D_F64 transform;
    private Planar<GrayF32> previousImage;
    private double height;
    private double xFOV, yFOV;
    private GPSPoint gpsPoint;
    private long prevTime;
    private int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_estimation);

        logger = new MotionLogger(this);

        heightEstimator = new HeightEstimator(this);
        imuSensor = new IMU(this);

        videoFeed = findViewById(R.id.videoFeed);
        speedText = findViewById(R.id.speedText);
        height = 1.5;
        xFOV = 80;
        yFOV = 80;
        gpsPoint = GPSPointFactory.fromGPSCoords(32.10260582, 35.20946193, 676.35322859);
        prevTime = System.currentTimeMillis();
        counter = 0;

        setImageType(ImageType.pl(3, GrayF32.class));
        startCamera(videoFeed, null);
    }

    @Override
    protected void processImage(ImageBase image) {
        if (counter % SKIP_FRAMES == 0) {
            if (previousImage == null) {
                previousImage = (Planar<GrayF32>) image;
            }

            double time = (System.currentTimeMillis() - prevTime) / 1000.0;
            float[] orientation = imuSensor.getAngles();
            azimuth = orientation[0];
            pitch = orientation[1];
            roll = orientation[2];


            int x = (int) (((bitmap.getWidth() / 2) * (1 - pitch / 40)));
            int y = (int) (((bitmap.getHeight() / 2) * (1 - roll / 30)));

            GrayF32 grayImage = ConvertImage.average((Planar<GrayF32>) image, null);
            GrayF32 grayPreviousImage = ConvertImage.average(previousImage, null);
            Pair<Homography2D_F64, Double> stitch_result = StitchingUtils.stitch(grayPreviousImage, grayImage, GrayF32.class);
            transform = stitch_result.first;

            double xPixelDistance = calculateXDistancePerPixel();
            double yPixelDistance = calculateYDistancePerPixel();

            Point2D_F64 previousCenter = new Point2D_F64(x, y);
            Point2D_F64 transformedPoint = HomographyPointOps_F64.transform(transform, previousCenter, null);

            double xVec = (previousCenter.x - transformedPoint.x) * xPixelDistance;
            double yVec = (previousCenter.y - transformedPoint.y) * yPixelDistance;
            double distance = Math.sqrt(Math.pow(xVec, 2) + Math.pow(yVec, 2));

            Log.d("Speed", "Speed: " + (distance / time));
            String text = "Speed: " + String.format("%.3f", (distance / time)) + "m/s" + ", Time: " + time;

            speedText.setText(text);

            prevTime = System.currentTimeMillis();
            double[] rotatedValues = rotateVector(x, y, azimuth);
            gpsPoint = GPSPointFactory.fromVelocity(gpsPoint, rotatedValues[0], rotatedValues[1], 0);

            logger.setAzimuth(azimuth);
            logger.setPitch(pitch);
            logger.setRoll(roll);
            logger.setPosition(gpsPoint);
            logger.setImage((Planar<GrayF32>) image);
            logger.write();
        }
    }

    @Override
    protected void renderBitmapImage(BitmapMode mode, ImageBase image) {
        ConvertBitmap.boofToBitmap(image, bitmap, bitmapTmp);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);


        int x = (int) (((bitmap.getWidth() / 2) * (1 - pitch / 40)));
        int y = (int) (((bitmap.getHeight() / 2) * (1 - roll / 30)));
        Log.i("center", "[X: " + x + ", Y: " + y + "]");
        canvas.drawPoint(x, y, paint);
    }

    @Override
    protected void onResume() {
        super.onResume();
        heightEstimator.startEstimation();
        imuSensor.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        heightEstimator.stopEstimation();
        imuSensor.stop();
    }

    private double calculateXDistancePerPixel() {
        return (2 * Math.tan(xFOV / 2.0) * height) / previousImage.width;
    }

    private double calculateYDistancePerPixel() {
        return (2 * Math.tan(yFOV / 2.0) * height) / previousImage.height;
    }


    public static double[] rotateVector(double x, double y, double angleInDegrees) {
        double angleInRadians = Math.toRadians(angleInDegrees);
        double[] vector = new double[]{x, y};
        return rotateVector(vector, angleInRadians);
    }

    public static double[] rotateVector(double[] vector, double angle) {
        double sinTheta = Math.sin(angle);
        double cosTheta = Math.cos(angle);

        double rotatedX = vector[0] * cosTheta - vector[1] * sinTheta;
        double rotatedY = vector[0] * sinTheta + vector[1] * cosTheta;

        return new double[]{rotatedX, rotatedY};
    }
}