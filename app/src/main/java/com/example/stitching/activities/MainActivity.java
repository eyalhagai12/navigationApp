package com.example.stitching.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.stitching.Logging.BackgroundLogging;
import com.example.stitching.R;

import boofcv.android.ConvertBitmap;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class MainActivity extends AppCompatActivity {
    boolean backgroundStarted = false;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkPermission()) {
            Toast.makeText(MainActivity.this,"WE Have Permission",Toast.LENGTH_SHORT).show();   // WE have a permission just start your work.
        } else {
            requestPermission(); // Request Permission
        }

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult( ActivityResult result ) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (Environment.isExternalStorageManager())
                            Toast.makeText(MainActivity.this,"We Have Permission",Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(MainActivity.this, "You Denied the permission", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "You Denied the permission", Toast.LENGTH_SHORT).show();
                    }
                }
            });


        Button stitchingButton = findViewById(R.id.stitchingButton);
        stitchingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, StitchingActivity.class);
                startActivity(i);
            }
        });

        Button sceneButton = findViewById(R.id.SceneRecognitionButton);
        sceneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, SceneRecognitionActivity.class);
                startActivity(i);
            }
        });

        Button heightButton = findViewById(R.id.heightEstimation);
        heightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, HeightActivity.class);
                startActivity(i);
            }
        });

        Button stitchingYoni = findViewById(R.id.stitchingYoniButton);
        stitchingYoni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, StitchingYoni.class);
                startActivity(i);
            }
        });

        Button lockOnButton = findViewById(R.id.lockOnActivity);
        lockOnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, LockOnActivity.class);
                startActivity(i);
            }
        });
        Button backgroundLoggingButton = findViewById(R.id.backgroudLogging);
        backgroundLoggingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent i = new Intent(MainActivity.this, LockOnActivity.class);
//                startActivity(i);
                if (!backgroundStarted) {
                    Log.i("mainActivity", "background log starting");

                    startService(new Intent(MainActivity.this, BackgroundLogging.class));
                    backgroundStarted = true;
                } else {
                    Log.i("mainActivity", "log already running");
                }
            }
        });


        Button motionEstimation = findViewById(R.id.motionEstimation);
        motionEstimation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, SpeedEstimationActivity.class);
                startActivity(i);
            }
        });


        Button sceneRecognition = findViewById(R.id.sceneRecognition);
        sceneRecognition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, SceneRecognitionActivity.class);
                startActivity(i);
            }
        });
    }
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int readCheck = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
            int writeCheck = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
            return readCheck == PackageManager.PERMISSION_GRANTED && writeCheck == PackageManager.PERMISSION_GRANTED;
        }
    }
    private String[] permissions = {READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE};
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Permission")
                    .setMessage("Please give the Storage permission")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which ) {
                            try {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                intent.addCategory("android.intent.category.DEFAULT");
                                intent.setData(Uri.parse(String.format("package:%s", new Object[]{getApplicationContext().getPackageName()})));
                                activityResultLauncher.launch(intent);
                            } catch (Exception e) {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                activityResultLauncher.launch(intent);
                            }
                        }
                    })
                    .setCancelable(false)
                    .show();

        } else {

            ActivityCompat.requestPermissions(MainActivity.this, permissions, 30);
        }
    }

}