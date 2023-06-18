package com.example.stitching.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.stitching.GPS.GPSPoint;
import com.example.stitching.GPS.GPSPointFactory;
import com.example.stitching.GPS.Point;
import com.example.stitching.GPS.PointAlgo;
import com.example.stitching.R;
import com.example.stitching.Stitching.StitchingUtils;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import boofcv.abst.sfm.d2.ImageMotion2D;
import boofcv.abst.tracker.PointTracker;
import boofcv.android.ConvertBitmap;
import boofcv.android.camera2.VisualizeCamera2Activity;
import boofcv.core.image.ConvertImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;
import georegression.struct.homography.Homography2D_F64;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point2D_I32;
import georegression.transform.homography.HomographyPointOps_F64;

public class StitchingYoni extends VisualizeCamera2Activity{
    private boolean takeNewImageAndProcess;
    private boolean takeFrameFromVideoAndProcess;

    ImageView stitchView;
    ImageView nextImageView;
    ImageView lastImageView;
    VideoView videoView;
    TextView distanceView;
    TextView degreeView;
    TextView degreeChangeView;
    TextView elevationView;
    Planar<GrayF32> lastImage;
    Planar<GrayF32> nextVideoFrame;
    double degree = 0;
    double degreeChange;
    GPSPoint startingPoint;
    List<GPSPoint> gpsPoints;
    double xDistancePerPixel;
    double yDistancePerPixel;
    // TODO understand from camera params and height
    double xDistance = 500;
    double yDistance = 500;
    final double distanceThreshold = 1.5;



    public StitchingYoni() {
        // The default behavior for selecting the camera's resolution is to
        // find the resolution which comes the closest to having this many
        // pixels.
        targetResolution = 640 * 480;
        takeNewImageAndProcess = false;
        takeFrameFromVideoAndProcess = false;

        // initialize points for GPS
        startingPoint = GPSPointFactory.fromGPSCoords(32.09237848, 35.17513055, 564.05338779);
        gpsPoints = new LinkedList<>();
        gpsPoints.add(startingPoint);

    }

    /*
    initialize ui and some other things
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stitching_yoni);
        FrameLayout surface = findViewById(R.id.camera_frame);
        Button takePicture = findViewById(R.id.take_pic);
        stitchView = findViewById(R.id.stitch_image);
        nextImageView = findViewById(R.id.next_image);
        lastImageView = findViewById(R.id.last_image);
        videoView = findViewById(R.id.video_view);

        // text for debug

        distanceView = findViewById(R.id.distance_text);
        degreeView = findViewById(R.id.degree_text);;
         degreeChangeView = findViewById(R.id.degree_change_text);;
         elevationView = findViewById(R.id.elevation_text);;

        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(StitchingYoni.this, "Taking and processing new image", Toast.LENGTH_SHORT).show();
                takeNewImageAndProcess = true;

            }
        });


        // read video and send frames as if they are camera frames


//        int sampleEveryMillisec = 4000;
////        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//


//        String url = "http://www.youtube.com/watch?v=1FJHYqE0RDg";
//        VideoView videoView = findViewById(R.id.video_view);
//
//        Uri uri = Uri.parse(videoUrl);
//
//        // sets the resource from the
//        // videoUrl to the videoView
//        videoView.setVideoURI(uri);
//
//
//        // Start playing the video
//        videoView.start();
//
//        try {
//            // Set the data source to the video file
//            retriever.setDataSource(videoPath);
//
//            // Get the duration of the video in milliseconds
//            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//            long videoDuration = Long.parseLong(duration);
//
//            // Read frames from the video at regular intervals
//            for (long time = 0; time < videoDuration; time += sampleEveryMillisec) {
//                // Get the frame at the specified time
//                // Use time * 1000 for microsecond precision
//                Bitmap frame = retriever.getFrameAtTime(time * sampleEveryMillisec, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
//
//                nextVideoFrame = bitmapToPlanarGrayF32(frame);
//                takeFrameFromVideoAndProcess = true;
//            }
//        } catch (Exception e) {
////            throw new RuntimeException(e);
//            e.printStackTrace();
//            Log.e("TAG", "Error log message");
//            Toast.makeText(this, "Error loading video", Toast.LENGTH_SHORT).show();
//        } finally {
//            // Release the MediaMetadataRetriever
//            try {
//                retriever.release();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }

        // set image tpye for imageProcess function
        setImageType(ImageType.pl(3, GrayF32.class));


        startCamera(surface, null);
    }



//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String[] permissions, int[] grantResults) {
//        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission granted
//            } else {
//                // Permission denied
//            }
//        }
//    }

    /**
     * This is where you specify custom camera settings. See {@link boofcv.android.camera2.SimpleCamera2Activity}'s
     * JavaDoc for more funcitons which you can override.
     *
     * @param captureRequestBuilder Used to configure the camera.
     */
    @Override
    protected void configureCamera(CameraDevice device, CameraCharacteristics characteristics, CaptureRequest.Builder captureRequestBuilder) {
        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
    }

    /**
     * Main logic. Takes a new image every time takeNewImageAndProcess is true.
     * then transform is computed, images are stitched, etc.
     * lastImage stores previous image. Updated each time.
     * (This function is invoked in its own thread and can take as long as you want.)
     *
     */
    @Override
    protected void processImage(ImageBase nextImageGeneric) { // Type of image is specified in OnCreate: setImageType(ImageType.pl(3, GrayU8.class));
        if (takeNewImageAndProcess) {

            Planar<GrayF32> nextImage = (Planar<GrayF32>)nextImageGeneric;
            stitchImagesAndDisplayInfo(nextImage);

            takeNewImageAndProcess = false;
        }

//        if (takeFrameFromVideoAndProcess) {
//
//            // next video frame set in video loop every x seconds
//            stitchImagesAndDisplayInfo(nextVideoFrame);
//
//            takeFrameFromVideoAndProcess = false;
//        }

    }

    private void stitchImagesAndDisplayInfo(Planar<GrayF32> nextImage){
        // initialize first image and some related parameters
        if(lastImage == null) {
            lastImage = nextImage.clone();// important to copy!
            // compute distance (in meters) per pixel
            xDistancePerPixel = xDistance / lastImage.getWidth();
            yDistancePerPixel = yDistance / lastImage.getHeight();
            // display first image
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmapImage = Bitmap.createBitmap(lastImage.width, lastImage.height, Bitmap.Config.ARGB_8888);
                    ConvertBitmap.boofToBitmap(lastImage, bitmapImage, bitmapTmp);
                    lastImageView.setImageBitmap(rotate90DegCW(bitmapImage));

//                    Toast.makeText(StitchingYoni.this, "lastimage is null", Toast.LENGTH_SHORT).show();
                }
            });
            takeNewImageAndProcess = false;
            return;
        }

        // draw next and last images
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmapImage = Bitmap.createBitmap(lastImage.width, lastImage.height, Bitmap.Config.ARGB_8888);
                ConvertBitmap.boofToBitmap(lastImage, bitmapImage, null);
                // move last one square
                lastImageView.setImageBitmap(rotate90DegCW(bitmapImage));
                Bitmap bitmapImage2 = Bitmap.createBitmap(lastImage.width, lastImage.height, Bitmap.Config.ARGB_8888);
                bitmapImage2 = Bitmap.createBitmap(nextImage.width, nextImage.height, Bitmap.Config.ARGB_8888);
                ConvertBitmap.boofToBitmap(nextImage, bitmapImage2, null);
                nextImageView.setImageBitmap(rotate90DegCW(bitmapImage2));

            }
        });

        // Convert Planar<GrayF32> to GrayF32
        GrayF32 grayLast = new GrayF32(lastImage.width, lastImage.height);
        ConvertImage.average(lastImage, grayLast);
        GrayF32 grayNew = new GrayF32(nextImage.width, nextImage.height);
        ConvertImage.average(nextImage , grayNew);

        // compute transform between next and last
        Homography2D_F64 transform = StitchingUtils.stitch(grayLast, grayNew, GrayF32.class);


        // use the homography to transform the center of the previous image
        // take top and center points
        Point2D_F64 startImageCenter = new Point2D_F64(lastImage.getWidth() / 2.0, lastImage.getHeight() / 2.0);
        Point2D_F64 startImageTop = new Point2D_F64(lastImage.getWidth() / 2.0, 0.0);

        // compute the transform on those points
        Point2D_F64 transformedCenterPoint = new Point2D_F64();
        Point2D_F64 transformedTopPoint = new Point2D_F64();
        HomographyPointOps_F64.transform(transform, startImageCenter, transformedCenterPoint);
        HomographyPointOps_F64.transform(transform, startImageTop, transformedTopPoint);

        // compute degree from two lines in the space of the stitch
        degreeChange = calculateDegree(
                startImageTop.x - startImageCenter.x,
                startImageTop.y - startImageCenter.y,
                transformedTopPoint.x - transformedCenterPoint.x,
                transformedTopPoint.y - transformedCenterPoint.y);
        degree += degreeChange;


        // compute distances in meters and get new point from result
        double xDistanceMeters = (startImageCenter.x - transformedCenterPoint.x) * xDistancePerPixel;
        double yDistanceMeters = (startImageCenter.y - transformedCenterPoint.y) * yDistancePerPixel;
        double[] rotatedVector = rotateVector(xDistanceMeters, yDistanceMeters, degree);
        GPSPoint lastPoint = gpsPoints.get(gpsPoints.size() - 1);
        GPSPoint newPoint = GPSPointFactory.fromVelocity(lastPoint, rotatedVector[0], rotatedVector[1], 0);
        double distance = PointAlgo.distance(lastPoint, newPoint);

        // add point when enough distance tavelled
        if (distance > distanceThreshold) {
            gpsPoints.add(newPoint);
//                    System.out.println("Moving! (Distance: " + distance + ")");
        }

        // rotation is only for displaying! can make it all the time if needed
        Bitmap stitchedImage = StitchingUtils.computeStitchedImage(lastImage, nextImage , transform);
        Bitmap rotatedImage  = rotate90DegCW(stitchedImage);

        // display stitch and update text elements with computed info
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stitchView.setImageBitmap(rotatedImage);

                distanceView.setText("total distance"+ distance);
                degreeView .setText("degree"+ degree);
                degreeChangeView.setText("degreeChange"+ degreeChange);
                elevationView .setText("todo implement");


            }
        });
        // important to copy!
        lastImage = nextImage.clone();
    }



    // useful for displaying images in imageview
    private Bitmap rotate90DegCW(Bitmap image){
        // Rotate the Bitmap by 90 degrees clockwise (don't know why need to do it)
        Matrix matrix = new Matrix();
        matrix.postRotate(90); // Specify the rotation angle

        return Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
    }





    private static Point2D_I32 renderPoint(int x0, int y0, Homography2D_F64 fromBtoWork) {
        Point2D_F64 result = new Point2D_F64();
        HomographyPointOps_F64.transform(fromBtoWork, new Point2D_F64(x0, y0), result);
        return new Point2D_I32((int) result.x, (int) result.y);
    }

    public static double calculateDegree(double x1, double y1, double x2, double y2) {
        double angle1 = Math.atan2(y1, x1);
        double angle2 = Math.atan2(y2, x2);
        double radians = angle2 - angle1;

        // Convert the angle to the range of -pi to pi
        if (radians > Math.PI) {
            radians -= 2 * Math.PI;
        } else if (radians < -Math.PI) {
            radians += 2 * Math.PI;
        }

        // Convert radians to degrees

        return Math.toDegrees(radians);
    }

    public static double[] rotateVector(double x, double y, double degrees) {
        // Convert the angle from degrees to radians
        double radians = Math.toRadians(degrees);

        // Calculate the cosine and sine of the angle
        double cosTheta = Math.cos(radians);
        double sinTheta = Math.sin(radians);

        // Perform the rotation using the rotation matrix
        double newX = x * cosTheta - y * sinTheta;
        double newY = x * sinTheta + y * cosTheta;

        // Return the new rotated vector as an array
        return new double[]{newX, newY};
    }

    /**
     * converts image (and also copies).
     * @param image
     * @return
     */
    public static Planar<GrayF32> bitmapToPlanarGrayF32(Bitmap image){
        // Convert the Bitmap to Planar<GrayF32>
        Planar<GrayF32> boofcvImage = new Planar<>(GrayF32.class, image.getWidth(), image.getHeight(), 3);
        ConvertBitmap.bitmapToBoof(image, boofcvImage, null);
        return boofcvImage;
    }







//    private Bitmap getStitchedBitmap() {
//        // Get the stitched image
//        Planar<GrayF32> stitchedImage = stitch.getStitchedImage();
//
//        // Convert the stitched image to Bitmap
//        Bitmap resultBitmap = Bitmap.createBitmap(stitchedImage.width, stitchedImage.height, Bitmap.Config.ARGB_8888);
//        ConvertBitmap.boofToBitmap(stitchedImage, resultBitmap, null);
//
//        return resultBitmap;
//    }

}