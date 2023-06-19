package com.example.stitching.Logging;

import android.app.Application;
import android.content.Intent;

/**
 * delet e this
 */
public class ApplicationForLogging  extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        startService(new Intent(this, BackgroundLogging.class));
    }
}
