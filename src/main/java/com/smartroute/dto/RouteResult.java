package com.smartroute.dto;

import com.smartroute.model.CityGraph;
import com.smartroute.model.Edge;
import com.smartroute.model.Node;
import com.smartroute.model.TransportMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data Transfer Object representing a computed route result.
 * Returned by all routing algorithms and serialized to JSON by Spring Boot.
 */
public class RouteResult {

    private final List<Integer> path;          // Node IDs in order
    private final List<String> pathNames;       // Human-readable node names
    private final double totalDistance;         // km
    private final double estimatedTimeMinutes;  // minutes
    private final String algorithm;            // "DIJKSTRA" | "A_STAR" | "BFS" | etc.
    private final boolean reachable;
    private final String message;
    private final List<String> transportModes;  // modes used per segment

    private RouteResult(Builder builder) {
        this.path = builder.path;
        this.pathNames = builder.pathNames;
        this.totalDistance = builder.totalDistance;
        this.estimatedTimeMinutes = builder.estimatedTimeMinutes;
        this.algorithm = builder.algorithm;
        this.reachable = builder.reachable;
        this.message = builder.message;
        this.transportModes = builder.transportModes;
    }

    // ─── Factory methods ─────────────────────────────────────

    public static RouteResult success(List<Integer> path, double distance,
                                      String algorithm, CityGraph graph) {
        List<String> names = new ArrayList<>();
        List<String> modes = new ArrayList<>();
        double timeMinutes = 0;

        for (int i = 0; i < path.size(); i++) {
            Node node = graph.getNode(path.get(i));
            names.add(node != null ? node.getName() : "Node-" + path.get(i));

            // Collect transport modes and estimate time
            if (i < path.size() - 1) {
                int u = path.get(i), v = path.get(i + 1);
                Edge edge = graph.getNeighbors(u).stream()
                        .filter(e -> e.getTo() == v)
                        .findFirst().orElse(null);

                if (edge != null) {
                    modes.add(edge.getMode().getDisplayName());
                    timeMinutes += edge.getMode().distanceToMinutes(edge.effectiveWeight());
                } else {
                    modes.add("Unknown");
                }
            }
        }

        return new Builder()
                .path(path)
                .pathNames(names)
                .totalDistance(Math.round(distance * 100.0) / 100.0)
                .estimatedTimeMinutes(Math.round(timeMinutes * 10.0) / 10.0)
                .algorithm(algorithm)
                .reachable(true)
                .transportModes(modes)
                .message(String.format("Route found: %d stops, %.2f km, ~%.0f min",
                        path.size(), distance, timeMinutes))
                .build();
    }

    public static RouteResult unreachable(int src, int dest, String algorithm) {
        return new Builder()
                .path(Collections.emptyList())
                .pathNames(Collections.emptyList())
                .transportModes(Collections.emptyList())
                .totalDistance(Double.MAX_VALUE)
                .estimatedTimeMinutes(Double.MAX_VALUE)
                .algorithm(algorithm)
                .reachable(false)
                .message(String.format("No path found from node %d to node %d", src, dest))
                .build();
    }

    // ─── Getters ─────────────────────────────────────────────

    public List<Integer> getPath() { return path; }
    public List<String> getPathNames() { return pathNames; }
    public double getTotalDistance() { return totalDistance; }
    public double getEstimatedTimeMinutes() { return estimatedTimeMinutes; }
    public String getAlgorithm() { return algorithm; }
    public boolean isReachable() { return reachable; }
    public String getMessage() { return message; }
    public List<String> getTransportModes() { return transportModes; }
    public int getHops() { return path.isEmpty() ? 0 : path.size() - 1; }

    // ─── Builder ─────────────────────────────────────────────

    private static class Builder {
        List<Integer> path;
        List<String> pathNames;
        double totalDistance;
        double estimatedTimeMinutes;
        String algorithm;
        boolean reachable;
        String message;
        List<String> transportModes;

        Builder path(List<Integer> p) { this.path = p; return this; }
        Builder pathNames(List<String> n) { this.pathNames = n; return this; }
        Builder totalDistance(double d) { this.totalDistance = d; return this; }
        Builder estimatedTimeMinutes(double t) { this.estimatedTimeMinutes = t; return this; }
        Builder algorithm(String a) { this.algorithm = a; return this; }
        Builder reachable(boolean r) { this.reachable = r; return this; }
        Builder message(String m) { this.message = m; return this; }
        Builder transportModes(List<String> tm) { this.transportModes = tm; return this; }
        RouteResult build() { return new RouteResult(this); }
    }
}
