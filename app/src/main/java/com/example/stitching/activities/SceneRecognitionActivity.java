package com.example.stitching.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.stitching.R;

import java.io.File;

import boofcv.BoofVerbose;
import boofcv.abst.scene.SceneRecognition;
import boofcv.android.camera2.VisualizeCamera2Activity;
import boofcv.io.recognition.RecognitionIO;
import boofcv.misc.BoofMiscOps;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageType;

public class SceneRecognitionActivity extends VisualizeCamera2Activity {
    private FrameLayout surface;
    private ImageView bestMatch;
    private SceneRecognition<GrayU8> recognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene_recognition);

        surface = findViewById(R.id.liveVideoFeed);
        bestMatch = findViewById(R.id.bestMatchImage);

        // Tell it to process gray U8 images
        ImageType<GrayU8> imageType = ImageType.SB_U8;
        setImageType(imageType);

        recognizer = RecognitionIO.downloadDefaultSceneRecognition(new File("downloaded_models"), imageType);
        recognizer.setVerbose(System.out, BoofMiscOps.hashSet(BoofVerbose.RECURSIVE));

        startCamera(surface, null);
    }

    @Override
    protected void processImage(ImageBase image) {

    }
}