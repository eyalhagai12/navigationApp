package com.example.stitching.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.stitching.R;

import boofcv.abst.feature.detect.interest.ConfigPointDetector;
import boofcv.abst.feature.detect.interest.PointDetectorTypes;
import boofcv.abst.sfm.d2.ImageMotion2D;
import boofcv.abst.sfm.d2.PlToGrayMotion2D;
import boofcv.abst.tracker.PointTracker;
import boofcv.alg.sfm.d2.StitchingFromMotion2D;
import boofcv.android.ConvertBitmap;
import boofcv.android.camera2.VisualizeCamera2Activity;
import boofcv.core.image.ConvertImage;
import boofcv.factory.sfm.FactoryMotion2D;
import boofcv.factory.tracker.FactoryPointTracker;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.InterleavedU8;
import boofcv.struct.image.Planar;
import georegression.struct.homography.Homography2D_F64;

public class StitchingActivity extends VisualizeCamera2Activity {
    private PointTracker<GrayF32> tracker;
    ImageMotion2D<GrayF32, Homography2D_F64> motion2D;
    ImageMotion2D<Planar<GrayF32>, Homography2D_F64> motion2DColor;
    private StitchingFromMotion2D<Planar<GrayF32>, Homography2D_F64> stitch;
    private boolean saveImage;
    private ImageSingleton imageSingleton;


    public StitchingActivity() {
        // The default behavior for selecting the camera's resolution is to
        // find the resolution which comes the closest to having this many
        // pixels.
        targetResolution = 640 * 480;
        saveImage = false;
        imageSingleton = ImageSingleton.getInstance();

        ConfigPointDetector configDetector = new ConfigPointDetector();
        // Configure the feature detector
        configDetector.type = PointDetectorTypes.SHI_TOMASI;
        configDetector.general.maxFeatures = 300;
        configDetector.general.radius = 3;
        configDetector.general.threshold = 1;

        // Use a KLT tracker
        tracker = FactoryPointTracker.klt(4, configDetector, 3, GrayF32.class, GrayF32.class);

        // This estimates the 2D image motion
        // An Affine2D_F64 model also works quite well.
        motion2D = FactoryMotion2D.createMotion2D(300, 3, 2, 50, 0.6, 0.5, false, tracker, new Homography2D_F64());

        // wrap it so it output color images while estimating motion from gray
        motion2DColor = new PlToGrayMotion2D<>(motion2D, GrayF32.class);

        // This fuses the images together
        Homography2D_F64 shrink = new Homography2D_F64(0.5, 0, 640.0 / 4, 0, 0.5, 480.0 / 4, 0, 0, 1);
        shrink = shrink.invert(null);
        stitch = FactoryMotion2D.createVideoStitch(0.5, motion2DColor, ImageType.pl(3, GrayF32.class));
        stitch.configure(640, 480, shrink);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stitching);
        FrameLayout surface = findViewById(R.id.camera_frame);
        Button takePicture = findViewById(R.id.takePicture);
        Button showResults = findViewById(R.id.showResults);

        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveImage = true;
            }
        });

        showResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(StitchingActivity.this, VisualizeActivity.class);
                Bitmap result = stitchImages();
                imageSingleton.setStitchedImage(result);
                startActivity(i);
            }
        });

        setImageType(ImageType.pl(3, GrayU8.class));

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
     * This function is invoked in its own thread and can take as long as you want.
     */
    @Override
    protected void processImage(ImageBase image) {
        if (saveImage) {
            Planar<GrayF32> imageF32 = new Planar<>(GrayF32.class, image.getWidth(), image.getHeight(), 3);
            Log.i("Size", "Width: " + image.getWidth() + ", Height: " + image.getHeight());
            InterleavedU8 imageInterU8 = new InterleavedU8(image.getWidth(), image.getHeight(), 3);
            ConvertImage.convert((Planar<GrayU8>) image, imageInterU8);
            ConvertImage.convertU8F32(imageInterU8, imageF32);
            boolean failed = false;
            try {
                stitch.process(imageF32);
            } catch (Exception e) {
                failed = true;
            }
            if (failed) {
                Toast.makeText(StitchingActivity.this, "Stitching Failed! resetting...", Toast.LENGTH_SHORT).show();
                stitch.reset();
            }

            saveImage = false;
        }
    }


    private Bitmap stitchImages() {
        // Get the stitched image
        Planar<GrayF32> stitchedImage = stitch.getStitchedImage();

        // Convert the stitched image to Bitmap
        Bitmap resultBitmap = Bitmap.createBitmap(stitchedImage.width, stitchedImage.height, Bitmap.Config.ARGB_8888);
        ConvertBitmap.boofToBitmap(stitchedImage, resultBitmap, null);

        return resultBitmap;
    }

}