package com.example.stitching.GPS;

public class GPSPointFactory {
    public static GPSPoint fromGPSCoords(double lat, double lon, double alt) {
        double[] coords = CoordinateConverter.getXYZfromLatLonDegrees(lat, lon, alt);
        return new GPSPoint(coords[0], coords[1], coords[2]);
    }

    public static GPSPoint fromVelocity(GPSPoint point, double xVelocity, double yVelocity, double zVelocity) {
        double x = point.x() + xVelocity;
        double y = point.y() + yVelocity;
        double z = point.z() + zVelocity;

        return new GPSPoint(x, y, z);
    }
}
