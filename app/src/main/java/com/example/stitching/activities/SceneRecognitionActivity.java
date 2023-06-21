package com.example.stitching.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.stitching.R;

import net.lingala.zip4j.ZipFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import boofcv.BoofVerbose;
import boofcv.abst.scene.SceneRecognition;
import boofcv.android.camera2.VisualizeCamera2Activity;
import boofcv.io.UtilIO;
import boofcv.io.recognition.RecognitionIO;
import boofcv.misc.BoofMiscOps;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageType;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

public class SceneRecognitionActivity extends VisualizeCamera2Activity {
    private FrameLayout surface;
    private ImageView bestMatchView;
    private SceneRecognition<GrayU8> recognizer;
    public final static String MODEL = "model";
    public final static String IMAGES = "images";

    public final static String DATA_DIRECTORY = "scene_recognition";

    // if pretraining own, should load it someplace and download it
    public final static String MODEL_ADDRESS = "http://boofcv.org/notwiki/largefiles/scene_recognition_default38_inria_holidays.zip";

    final Object lockRecognizer = new Object();

    public SceneRecognitionActivity() {
        super.bitmapMode = BitmapMode.NONE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene_recognition);



        surface = findViewById(R.id.liveVideoFeed);
        bestMatchView = findViewById(R.id.bestMatchImage);


        // Tell it to process gray U8 images
        ImageType<GrayU8> imageType = ImageType.SB_U8;
        setImageType(imageType);

        // on below line we are creating an image file and
        // specifying path for the image file on below line.
        File imgFile = new File("/storage/emulated/0/Pictures/scene_recognition/DJI_0486.JPG");


        // on below line we are checking if the image file exist or not.
        if (imgFile.exists()) {
            // on below line we are creating an image bitmap variable
            // and adding a bitmap to it from image file.
            Bitmap imgBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            // on below line we are setting bitmap to our image view.
            bestMatchView.setImageBitmap(imgBitmap);
        }

//        recognizer = RecognitionIO.downloadDefaultSceneRecognition(new File("downloaded_models"), imageType);
//        recognizer.setVerbose(System.out, BoofMiscOps.hashSet(BoofVerbose.RECURSIVE));

        startCamera(surface, null);
    }




    @Override
    protected void processImage(ImageBase image) {

    }

}