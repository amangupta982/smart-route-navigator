package com.smartroute.util;

import com.smartroute.model.Node;

/**
 * HaversineUtil — Geographic distance calculation utility.
 *
 * The Haversine formula computes the great-circle distance between
 * two points on a sphere (Earth) given their GPS coordinates.
 *
 * Used as the admissible heuristic h(n) in A* algorithm:
 *   - Always ≤ actual road distance (never overestimates)
 *   - Therefore A* with Haversine is both complete and optimal
 *
 * Formula:
 *   a = sin²(Δφ/2) + cos φ1 ⋅ cos φ2 ⋅ sin²(Δλ/2)
 *   c = 2 ⋅ atan2( √a, √(1−a) )
 *   d = R ⋅ c   (where R = Earth's radius ≈ 6371 km)
 */
public class HaversineUtil {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private HaversineUtil() {}  // utility class — no instantiation

    /**
     * Calculates the straight-line (great-circle) distance in km
     * between two GPS coordinate pairs.
     */
    public static double distanceKm(double lat1, double lon1,
                                    double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Overload accepting Node objects directly.
     */
    public static double distanceKm(Node a, Node b) {
        if (a == null || b == null) return 0.0;
        return distanceKm(a.getLatitude(), a.getLongitude(),
                b.getLatitude(), b.getLongitude());
    }

    /**
     * Converts km to approximate travel time in minutes for a given speed.
     */
    public static double toMinutes(double distanceKm, double speedKmh) {
        return (distanceKm / speedKmh) * 60.0;
    }
}
