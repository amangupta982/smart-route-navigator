package com.smartroute.model;

/**
 * Represents a directed road segment (an edge in the city graph).
 *
 * DSA Role: Graph edge. Weight encodes travel cost (distance, time,
 * or combined cost). Mutable weight supports dynamic traffic updates.
 *
 * Weight semantics:
 *   - Base weight = road distance in km
 *   - Effective weight = base weight * trafficMultiplier
 *   - blocked = true → effectiveWeight() returns Double.MAX_VALUE / 2
 */
public class Edge {

    private final int from;
    private final int to;
    private double baseWeight;          // original road distance/time
    private double trafficMultiplier;   // 1.0 = clear, 2.0 = slow, 999 = blocked
    private final TransportMode mode;
    private boolean blocked;

    public Edge(int from, int to, double baseWeight, TransportMode mode) {
        this.from = from;
        this.to = to;
        this.baseWeight = baseWeight;
        this.trafficMultiplier = 1.0;
        this.mode = mode;
        this.blocked = false;
    }

    /**
     * Returns the effective routing weight, accounting for traffic.
     * If blocked, returns a huge number so Dijkstra/A* naturally avoids this edge.
     */
    public double effectiveWeight() {
        if (blocked) return Double.MAX_VALUE / 2;
        return baseWeight * trafficMultiplier;
    }

    public void applyTrafficMultiplier(double multiplier) {
        this.trafficMultiplier = multiplier;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public void resetTraffic() {
        this.trafficMultiplier = 1.0;
        this.blocked = false;
    }

    public int getFrom() { return from; }
    public int getTo() { return to; }
    public double getBaseWeight() { return baseWeight; }
    public double getTrafficMultiplier() { return trafficMultiplier; }
    public TransportMode getMode() { return mode; }
    public boolean isBlocked() { return blocked; }

    @Override
    public String toString() {
        return String.format("Edge{%d→%d, weight=%.2f, mode=%s, blocked=%b}",
                from, to, effectiveWeight(), mode, blocked);
    }
}
