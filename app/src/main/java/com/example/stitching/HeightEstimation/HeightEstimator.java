package com.example.stitching.HeightEstimation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class HeightEstimator implements SensorEventListener {
    private static final float STANDARD_PRESSURE = 1013.25f; // Standard atmospheric pressure at sea level in hPa
    private static final float PRESSURE_FACTOR = 44330.0f; // Constant factor for height estimation in meters
    private SensorManager sensorManager;
    private Sensor pressureSensor;
    private float estimatedHeight;

    public HeightEstimator(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        Log.d("PressureSensor", "" + pressureSensor);
    }

    public void startEstimation() {
        sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stopEstimation() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for pressure sensor
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            float pressure = event.values[0];
            estimatedHeight = estimateHeightFromPressure(pressure);
        }
    }

    private float estimateHeightFromPressure(float pressure) {
        return ((float) Math.pow((STANDARD_PRESSURE / pressure), (1 / 5.2559)) - 1) * PRESSURE_FACTOR;
    }

    public float getHeight() {
        return estimatedHeight;
    }
}
