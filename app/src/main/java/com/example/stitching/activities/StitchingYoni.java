package com.example.stitching.activities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.stitching.R;
import com.example.stitching.Stitching.StitchingUtils;

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

public class StitchingYoni extends VisualizeCamera2Activity {
    private PointTracker<GrayF32> tracker;
    ImageMotion2D<GrayF32, Homography2D_F64> motion2D;
    ImageMotion2D<Planar<GrayF32>, Homography2D_F64> motion2DColor;
//    private StitchingFromMotion2D<Planar<GrayF32>, Homography2D_F64> stitch;
    private boolean takeNewImageAndProcess;
    private ImageSingleton imageSingleton;

    ImageView stitchView;
    ImageView nextImageView;
    ImageView lastImageView;
//    Planar<GrayF32> lastImage;
//    private final Class<GrayF32> imageClass = GrayF32.class;
    Planar<GrayF32> lastImage;
//    Planar<GrayF32> nextImage;
    boolean flagwithNext = false;



    public StitchingYoni() {
        // The default behavior for selecting the camera's resolution is to
        // find the resolution which comes the closest to having this many
        // pixels.
        targetResolution = 640 * 480;
        takeNewImageAndProcess = false;


//        imageSingleton = ImageSingleton.getInstance();
//
//        ConfigPointDetector configDetector = new ConfigPointDetector();
//        // Configure the feature detector
//        configDetector.type = PointDetectorTypes.SHI_TOMASI;
//        configDetector.general.maxFeatures = 300;
//        configDetector.general.radius = 3;
//        configDetector.general.threshold = 1;

//        // Use a KLT tracker
//        tracker = FactoryPointTracker.klt(4, configDetector, 3, GrayF32.class, GrayF32.class);
//
//        // This estimates the 2D image motion
//        // An Affine2D_F64 model also works quite well.
//        motion2D = FactoryMotion2D.createMotion2D(300, 3, 2, 50, 0.6, 0.5, false, tracker, new Homography2D_F64());
//
//        // wrap it so it output color images while estimating motion from gray
//        motion2DColor = new PlToGrayMotion2D<>(motion2D, GrayF32.class);
//
//
//        // This fuses the images together
//        Homography2D_F64 shrink = new Homography2D_F64(0.5, 0, 640.0 / 4, 0, 0.5, 480.0 / 4, 0, 0, 1);
//        shrink = shrink.invert(null);
//
//
//        stitch = FactoryMotion2D.createVideoStitch(0.5, motion2DColor, ImageType.pl(3, GrayF32.class));
//        stitch.configure(640, 480, shrink);


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
//        Button showResults = findViewById(R.id.showResults);

        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(StitchingYoni.this, "Taking and processing new image", Toast.LENGTH_SHORT).show();
                takeNewImageAndProcess = true;

            }
        });

//        showResults.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent i = new Intent(StitchingYoni.this, VisualizeActivity.class);
//                Bitmap result = getStitchedBitmap();
//                imageSingleton.setStitchedImage(result);
//                startActivity(i);
//            }
//        });

        setImageType(ImageType.pl(3, GrayF32.class));

//        setImageType(ImageType.pl(3, GrayU8.class));

        startCamera(surface, null);
    }

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
            // initialize first image
            if(lastImage == null) {
                lastImage = copyBoof(nextImage);// important to copy!
                // display first image
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmapImage = Bitmap.createBitmap(lastImage.width, lastImage.height, Bitmap.Config.ARGB_8888);
                        ConvertBitmap.boofToBitmap(lastImage, bitmapImage, bitmapTmp);
                        lastImageView.setImageBitmap(rotate90DegCW(bitmapImage));

                    Toast.makeText(StitchingYoni.this, "lastimage is null", Toast.LENGTH_SHORT).show();
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


            Bitmap stitchedImage = StitchingUtils.computeStitchedImage(lastImage, nextImage , transform);

            Bitmap rotatedImage  = rotate90DegCW(stitchedImage);


            // display stitch
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stitchView.setImageBitmap(rotatedImage);
                }
            });
            // important to copy!
            lastImage = copyBoof(nextImage);


            takeNewImageAndProcess = false;
        }
    }
    private Bitmap rotate90DegCW(Bitmap image){
        // Rotate the Bitmap by 90 degrees clockwise (don't know why need to do it)
        Matrix matrix = new Matrix();
        matrix.postRotate(90); // Specify the rotation angle

        return Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
    }



    // optimize maybe
    public static Planar<GrayF32> copyBoof(Planar<GrayF32> image){
        // make a copy of the image
        Bitmap bitmapImage = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888);
        ConvertBitmap.boofToBitmap(image, bitmapImage, null);

        // Convert the Bitmap to Planar<GrayF32>
        Planar<GrayF32> boofcvImage = new Planar<>(GrayF32.class, image.getWidth(), image.getHeight(), 3);
        ConvertBitmap.bitmapToBoof(bitmapImage, boofcvImage, null);
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