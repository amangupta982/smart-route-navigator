package com.smartroute.model;

/**
 * Transport modes supported by the multi-modal routing engine.
 * Each mode has different speed characteristics used for weight calculation.
 */
public enum TransportMode {
    WALK(5.0, "Walking"),       // ~5 km/h
    BUS(25.0, "Bus"),           // ~25 km/h avg in city traffic
    METRO(60.0, "Metro"),       // ~60 km/h
    CAR(40.0, "Car"),           // ~40 km/h in city
    BIKE(15.0, "Bicycle");      // ~15 km/h

    private final double avgSpeedKmh;
    private final String displayName;

    TransportMode(double avgSpeedKmh, String displayName) {
        this.avgSpeedKmh = avgSpeedKmh;
        this.displayName = displayName;
    }

    /**
     * Converts distance (km) to travel time (minutes) for this mode.
     */
    public double distanceToMinutes(double distanceKm) {
        return (distanceKm / avgSpeedKmh) * 60.0;
    }

    public double getAvgSpeedKmh() { return avgSpeedKmh; }
    public String getDisplayName() { return displayName; }
}
