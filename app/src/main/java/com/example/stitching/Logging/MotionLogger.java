package com.example.stitching.Logging;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.example.stitching.GPS.CoordinateConverter;
import com.example.stitching.GPS.GPSPoint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import boofcv.android.ConvertBitmap;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.Planar;

public class MotionLogger {
    private static String TAG = "MotionLog";
    private static String LOGS_DIR = "logs";

    private Context context;
    private GPSPoint position;
    private Planar<GrayF32> image;
    private double azimuth, pitch, roll;
    private File currentLogDir, frames, orientationLogFile, positionLogFile;
    int n_frames;


    public MotionLogger(Context context) {
        this.context = context;
        n_frames = 0;

        createLogsDir();
        createCurrentLogDirectory();
        createLogFiles();
    }

    public void setAzimuth(double azimuth) {
        this.azimuth = azimuth;
    }

    public void setImage(Planar<GrayF32> image) {
        this.image = image;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    public void setRoll(double roll) {
        this.roll = roll;
    }

    public void setPosition(GPSPoint position) {
        this.position = position;
    }

    public void write() {
        String orientationText = "" + azimuth + "," + pitch + "," + roll + "\n";
        writeToFile(orientationLogFile, orientationText);

        double[] coords = CoordinateConverter.xyzToLatLonDegrees(new double[]{position.x(), position.y(), position.z()});
        String positionText = "Lat: " + coords[0] + ", Long: " + coords[1] + "\n";
        writeToFile(positionLogFile, positionText);

        savePlanarImage(image);

        n_frames++;
    }

    private void savePlanarImage(Planar<GrayF32> image) {
        // Get the external storage directory
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Create a file name
        String fileName = "image_" + n_frames + ".jpg";

        // Create the output file
        File outputFile = new File(frames, fileName);

        try {
            // Create a file output stream
            FileOutputStream fos = new FileOutputStream(outputFile);

            // Convert the Planar<GrayF32> image to Bitmap
            Bitmap bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888);
            ConvertBitmap.boofToBitmap(image, bitmap, null);

            // Compress the bitmap to the output stream as a JPEG image
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();

            // Close the file output stream
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToFile(File f, String txt) {
        try {
            FileWriter fw = new FileWriter(f.getPath(), true);
            fw.write(txt);
            fw.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createLogsDir() {
        File logsDir = new File(context.getFilesDir(), LOGS_DIR);
        if (!logsDir.exists()) {
            boolean success = logsDir.mkdirs();
            if (!success) {
                Log.e(TAG, "failed to create '" + LOGS_DIR + "' directory");
                return;
            }
            Log.i(TAG, "Log Directory Created Successfully!");
        } else {
            Log.i(TAG, "'" + LOGS_DIR + "' exists");
        }
    }

    private void createCurrentLogDirectory() {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(new Date());
        String newDirName = "log_" + timeStamp;
        String path = LOGS_DIR + File.separator + newDirName;
        currentLogDir = new File(context.getFilesDir(), path);

        if (!currentLogDir.exists()) {
            boolean success = currentLogDir.mkdirs();
            if (!success) {
                Log.e(TAG, "failed to create '" + path + "' directory");
                return;
            }
            Log.i(TAG, "Current Log Directory Created Successfully!");
        } else {
            Log.i(TAG, "'" + path + "' exists");
        }

        frames = new File(currentLogDir, "frames");
        if (!frames.exists()) {
            boolean success = frames.mkdirs();
            if (!success) {
                Log.e(TAG, "failed to create '" + frames.getPath() + "' directory");
                return;
            }
            Log.i(TAG, "Frames Directory Created Successfully!");
        } else {
            Log.i(TAG, "Frames directory exists");
        }
    }


    public void createLogFiles() {
        positionLogFile = new File(currentLogDir, "position_log.txt");
        try {
            if (!positionLogFile.createNewFile()) {
                Log.e(TAG, "Failed to create position log file");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Create the orientation log file within the new directory
        orientationLogFile = new File(currentLogDir, "orientation_log.txt");
        try {
            if (!orientationLogFile.createNewFile()) {
                Log.e(TAG, "Failed to create orientation log file");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
}
