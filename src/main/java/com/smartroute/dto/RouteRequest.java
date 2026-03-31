package com.smartroute.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Incoming request body for the /api/route endpoint.
 *
 * Example JSON:
 * {
 *   "source": 1,
 *   "destination": 10,
 *   "algorithm": "A_STAR",
 *   "k": 3
 * }
 */
public class RouteRequest {

    @NotNull(message = "Source node ID is required")
    @Min(value = 0, message = "Node ID must be non-negative")
    private Integer source;

    @NotNull(message = "Destination node ID is required")
    @Min(value = 0, message = "Node ID must be non-negative")
    private Integer destination;

    // "DIJKSTRA" | "A_STAR" | "BFS" | "K_SHORTEST" (default: DIJKSTRA)
    private String algorithm = "DIJKSTRA";

    // Number of shortest paths for K_SHORTEST algorithm (default: 3)
    private int k = 3;

    // Optional: filter routes by transport mode (null = all modes)
    private String preferredMode;

    // Getters and Setters
    public Integer getSource() { return source; }
    public void setSource(Integer source) { this.source = source; }

    public Integer getDestination() { return destination; }
    public void setDestination(Integer destination) { this.destination = destination; }

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

    public int getK() { return k; }
    public void setK(int k) { this.k = k; }

    public String getPreferredMode() { return preferredMode; }
    public void setPreferredMode(String preferredMode) { this.preferredMode = preferredMode; }
}
