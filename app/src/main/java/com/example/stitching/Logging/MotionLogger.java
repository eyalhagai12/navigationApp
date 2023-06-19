package com.example.stitching.Logging;

import com.example.stitching.GPS.GPSPoint;

import boofcv.struct.image.GrayF32;
import boofcv.struct.image.Planar;

public class MotionLogger {
    private GPSPoint position;
    private Planar<GrayF32> image;
    double azimuth, pitch, roll;

    public MotionLogger() {
        // create log dir

    }

}
