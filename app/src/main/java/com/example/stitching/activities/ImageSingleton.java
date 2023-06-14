package com.example.stitching.activities;

import android.graphics.Bitmap;

public class ImageSingleton {
    private static ImageSingleton singleton;
    private Bitmap stitchedImage;

    private ImageSingleton() {
        stitchedImage = null;
    }

    public static ImageSingleton getInstance() {
        if (singleton == null) {
            singleton = new ImageSingleton();
        }

        return singleton;
    }

    public Bitmap getStitchedImage() {
        return stitchedImage;
    }

    public void setStitchedImage(Bitmap stitchedImage) {
        this.stitchedImage = stitchedImage;
    }
}
