package com.smartroute.model;

import java.util.*;

/**
 * Core city road network modelled as a Directed Weighted Graph.
 *
 * DSA: Adjacency List representation — O(V + E) space.
 * Supports:
 *   - Dynamic edge weight updates (traffic simulation)
 *   - Multi-modal edges (walk, bus, metro, car)
 *   - Road blocking for re-routing scenarios
 *   - Neighbor queries for Dijkstra / A* traversal
 *
 * Time complexities:
 *   addNode/addEdge : O(1)
 *   getNeighbors    : O(1) — returns list reference
 *   updateTraffic   : O(degree(from))
 *   getAllNodes      : O(V)
 */
public class CityGraph {

    // Adjacency list: nodeId → list of outgoing edges
    private final Map<Integer, List<Edge>> adjList;

    // Node registry: nodeId → Node object (for coordinate lookups in A*)
    private final Map<Integer, Node> nodes;

    public CityGraph() {
        this.adjList = new HashMap<>();
        this.nodes = new HashMap<>();
    }

    // ─────────────────────────────────────────────
    // Graph Construction
    // ─────────────────────────────────────────────

    public void addNode(Node node) {
        nodes.put(node.getId(), node);
        adjList.putIfAbsent(node.getId(), new ArrayList<>());
    }

    public void addNode(int id, double lat, double lon, String name) {
        addNode(new Node(id, lat, lon, name));
    }

    /**
     * Adds a directed edge from → to.
     * For undirected roads, call addEdge twice (both directions).
     */
    public void addEdge(int from, int to, double weight, TransportMode mode) {
        if (!adjList.containsKey(from)) {
            throw new IllegalArgumentException("Source node " + from + " not found in graph.");
        }
        adjList.get(from).add(new Edge(from, to, weight, mode));
    }

    /**
     * Convenience: adds a bidirectional (undirected) road edge.
     */
    public void addBidirectionalEdge(int a, int b, double weight, TransportMode mode) {
        addEdge(a, b, weight, mode);
        addEdge(b, a, weight, mode);
    }

    // ─────────────────────────────────────────────
    // Dynamic Traffic Updates
    // ─────────────────────────────────────────────

    /**
     * Applies a traffic multiplier to a specific road segment.
     * multiplier > 1 = congested, 999 = effectively blocked.
     * DSA: O(degree(from)) scan through neighbor edges.
     */
    public void updateTraffic(int from, int to, double multiplier) {
        List<Edge> edges = adjList.getOrDefault(from, Collections.emptyList());
        edges.stream()
                .filter(e -> e.getTo() == to)
                .forEach(e -> e.applyTrafficMultiplier(multiplier));
    }

    /**
     * Marks a specific road segment as completely blocked.
     * Dijkstra/A* will route around it since effectiveWeight() returns MAX/2.
     */
    public void blockRoad(int from, int to) {
        List<Edge> edges = adjList.getOrDefault(from, Collections.emptyList());
        edges.stream()
                .filter(e -> e.getTo() == to)
                .forEach(e -> e.setBlocked(true));
    }

    /**
     * Unblocks a road and resets its traffic multiplier to 1.0.
     */
    public void unblockRoad(int from, int to) {
        List<Edge> edges = adjList.getOrDefault(from, Collections.emptyList());
        edges.stream()
                .filter(e -> e.getTo() == to)
                .forEach(Edge::resetTraffic);
    }

    /**
     * Resets ALL traffic multipliers across the entire graph.
     */
    public void resetAllTraffic() {
        adjList.values().stream()
                .flatMap(List::stream)
                .forEach(Edge::resetTraffic);
    }

    // ─────────────────────────────────────────────
    // Graph Queries
    // ─────────────────────────────────────────────

    public List<Edge> getNeighbors(int nodeId) {
        return adjList.getOrDefault(nodeId, Collections.emptyList());
    }

    public Node getNode(int id) {
        return nodes.get(id);
    }

    public Collection<Node> getAllNodes() {
        return nodes.values();
    }

    public Set<Integer> getAllNodeIds() {
        return nodes.keySet();
    }

    public boolean containsNode(int id) {
        return nodes.containsKey(id);
    }

    public int getNodeCount() {
        return nodes.size();
    }

    public int getEdgeCount() {
        return adjList.values().stream().mapToInt(List::size).sum();
    }

    /**
     * Returns all edges filtered by transport mode.
     * Useful for mode-specific route planning.
     */
    public List<Edge> getNeighborsByMode(int nodeId, TransportMode mode) {
        return adjList.getOrDefault(nodeId, Collections.emptyList())
                .stream()
                .filter(e -> e.getMode() == mode)
                .toList();
    }

    @Override
    public String toString() {
        return String.format("CityGraph{nodes=%d, edges=%d}", getNodeCount(), getEdgeCount());
    }
}
