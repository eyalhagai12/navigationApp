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
import android.widget.Toast;
import android.widget.VideoView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

public class StitchingYoni extends VisualizeCamera2Activity{
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 1001;
    private PointTracker<GrayF32> tracker;
    ImageMotion2D<GrayF32, Homography2D_F64> motion2D;
    ImageMotion2D<Planar<GrayF32>, Homography2D_F64> motion2DColor;
//    private StitchingFromMotion2D<Planar<GrayF32>, Homography2D_F64> stitch;
    private boolean takeNewImageAndProcess;
    private boolean takeFrameFromVideoAndProcess;
    private ImageSingleton imageSingleton;

    ImageView stitchView;
    ImageView nextImageView;
    ImageView lastImageView;
    VideoView videoView;
//    Planar<GrayF32> lastImage;
//    private final Class<GrayF32> imageClass = GrayF32.class;
    Planar<GrayF32> lastImage;
    Planar<GrayF32> nextVideoFrame;
//    Planar<GrayF32> nextImage;
    boolean flagwithNext = false;



    public StitchingYoni() {
        // The default behavior for selecting the camera's resolution is to
        // find the resolution which comes the closest to having this many
        // pixels.
        targetResolution = 640 * 480;
        takeNewImageAndProcess = false;
        takeFrameFromVideoAndProcess = false;


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
//        Button showResults = findViewById(R.id.showResults);

        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(StitchingYoni.this, "Taking and processing new image", Toast.LENGTH_SHORT).show();
                takeNewImageAndProcess = true;

            }
        });

//        File privateDir = getFilesDir();
//        Log.d("AppStorage", "Private Storage Directory: " + privateDir.getAbsolutePath());

        String videoUrl = "Paste Your Video URL Here";


        // read video and send frames as if they are camera frames

        // Replace "video_path" with the actual path of your video file
//        String videoPath = "res/drawable/drone_foot_trim.mp4";


        int sampleEveryMillisec = 4000;
//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();



        String url = "http://www.youtube.com/watch?v=1FJHYqE0RDg";
        videoUrl = getUrlVideoRTSP(url);
        VideoView videoView = findViewById(R.id.video_view);

        Uri uri = Uri.parse(videoUrl);

        // sets the resource from the
        // videoUrl to the videoView
        videoView.setVideoURI(uri);


        // Start playing the video
        videoView.start();
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

    public static String getUrlVideoRTSP(String urlYoutube) {
        try {
            String gdy = "http://gdata.youtube.com/feeds/base/videos/-/justinbieber?orderby=published&alt=rss&client=ytapi-youtube-rss-redirect&v=2";
            DocumentBuilder documentBuilder = DocumentBuilderFactory
                    .newInstance().newDocumentBuilder();
            String id = extractYoutubeId(urlYoutube);
            URL url = new URL(gdy + id);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            Document doc = documentBuilder.parse(connection.getInputStream());
            Element el = doc.getDocumentElement();
            NodeList list = el.getElementsByTagName("media:content");// /media:content
            String cursor = urlYoutube;
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                if (node != null) {
                    NamedNodeMap nodeMap = node.getAttributes();
                    HashMap<String, String> maps = new HashMap<String, String>();
                    for (int j = 0; j < nodeMap.getLength(); j++) {
                        Attr att = (Attr) nodeMap.item(j);
                        maps.put(att.getName(), att.getValue());
                    }
                    if (maps.containsKey("yt:format")) {
                        String f = maps.get("yt:format");
                        if (maps.containsKey("url")) {
                            cursor = maps.get("url");
                        }
                        if (f.equals("1"))
                            return cursor;
                    }
                }
            }
            return cursor;
        } catch (Exception ex) {
            Log.e("Get Url Video RTSP Exception======>>", ex.toString());
        }
        return urlYoutube;
    }
    private static String extractYoutubeId(String url) {
        return url;
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
        if (takeFrameFromVideoAndProcess) {

            // next video frame set in video loop every x seconds
            stitchImagesAndDisplayInfo(nextVideoFrame);

            takeFrameFromVideoAndProcess = false;
        }

    }

    private void stitchImagesAndDisplayInfo(Planar<GrayF32> nextImage){
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
    }



    // useful for displaying images in imageview
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