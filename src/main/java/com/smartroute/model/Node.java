package com.smartroute.model;

/**
 * Represents a city intersection or landmark (a vertex in the city graph).
 *
 * DSA Role: Graph vertex. Stores geographic coordinates for the
 * Haversine heuristic used in A* algorithm.
 */
public class Node {

    private final int id;
    private final double latitude;
    private final double longitude;
    private final String name;
    private final NodeType type;

    public Node(int id, double latitude, double longitude, String name, NodeType type) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.type = type;
    }

    // Convenience constructor with default type ROAD
    public Node(int id, double latitude, double longitude, String name) {
        this(id, latitude, longitude, name, NodeType.ROAD);
    }

    public int getId() { return id; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getName() { return name; }
    public NodeType getType() { return type; }

    @Override
    public String toString() {
        return String.format("Node{id=%d, name='%s', type=%s}", id, name, type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        return this.id == ((Node) o).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
