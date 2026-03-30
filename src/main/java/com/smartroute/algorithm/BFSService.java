package com.smartroute.algorithm;

import com.smartroute.dto.RouteResult;
import com.smartroute.model.CityGraph;
import com.smartroute.model.Edge;
import com.smartroute.model.TransportMode;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Breadth-First Search (BFS) for Unweighted Shortest Paths
 *
 * DSA Concepts:
 *   - Level-order traversal using a Queue (FIFO)
 *   - Guarantees minimum number of HOPS (edges traversed)
 *   - Optimal when all edge weights are equal (e.g., metro stops)
 *
 * Complexity:
 *   Time:  O(V + E)
 *   Space: O(V) for queue and visited set
 *
 * Use cases in SmartRoute:
 *   - Metro routing: minimize number of stations, not distance
 *   - Bus hop-count routing
 *   - Checking if destination is reachable (connectivity check)
 */
@Component
public class BFSService {

    /**
     * Finds path with minimum number of edges (hops) from src to dest.
     * Filters edges by transport mode.
     */
    public RouteResult findMinHopPath(CityGraph graph, int src, int dest, TransportMode mode) {
        if (!graph.containsNode(src) || !graph.containsNode(dest)) {
            return RouteResult.unreachable(src, dest, "BFS");
        }

        Map<Integer, Integer> prev = new HashMap<>();
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();

        visited.add(src);
        queue.offer(src);

        while (!queue.isEmpty()) {
            int u = queue.poll();

            if (u == dest) {
                List<Integer> path = reconstructPath(prev, src, dest);
                double dist = computeHopDistance(graph, path);
                return RouteResult.success(path, dist, "BFS_" + mode.name(), graph);
            }

            List<Edge> neighbors = (mode != null)
                    ? graph.getNeighborsByMode(u, mode)
                    : graph.getNeighbors(u);

            for (Edge edge : neighbors) {
                int v = edge.getTo();
                if (!visited.contains(v) && !edge.isBlocked()) {
                    visited.add(v);
                    prev.put(v, u);
                    queue.offer(v);
                }
            }
        }

        return RouteResult.unreachable(src, dest, "BFS");
    }

    /**
     * Simple reachability check using BFS.
     * DSA: Equivalent to connectivity check in O(V + E).
     */
    public boolean isReachable(CityGraph graph, int src, int dest) {
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(src);
        visited.add(src);

        while (!queue.isEmpty()) {
            int u = queue.poll();
            if (u == dest) return true;

            for (Edge edge : graph.getNeighbors(u)) {
                if (!visited.contains(edge.getTo()) && !edge.isBlocked()) {
                    visited.add(edge.getTo());
                    queue.offer(edge.getTo());
                }
            }
        }
        return false;
    }

    /**
     * Returns all nodes reachable from a given source.
     * Useful for connected component analysis.
     */
    public Set<Integer> getReachableNodes(CityGraph graph, int src) {
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(src);
        visited.add(src);

        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (Edge edge : graph.getNeighbors(u)) {
                if (!visited.contains(edge.getTo()) && !edge.isBlocked()) {
                    visited.add(edge.getTo());
                    queue.offer(edge.getTo());
                }
            }
        }
        return visited;
    }

    private List<Integer> reconstructPath(Map<Integer, Integer> prev, int src, int dest) {
        LinkedList<Integer> path = new LinkedList<>();
        int current = dest;
        while (current != src) {
            path.addFirst(current);
            Integer parent = prev.get(current);
            if (parent == null) return Collections.emptyList();
            current = parent;
        }
        path.addFirst(src);
        return new ArrayList<>(path);
    }

    private double computeHopDistance(CityGraph graph, List<Integer> path) {
        if (path.isEmpty()) return Double.MAX_VALUE;
        double total = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            int u = path.get(i), v = path.get(i + 1);
            total += graph.getNeighbors(u).stream()
                    .filter(e -> e.getTo() == v)
                    .mapToDouble(Edge::effectiveWeight)
                    .min().orElse(1.0);
        }
        return total;
    }
}
