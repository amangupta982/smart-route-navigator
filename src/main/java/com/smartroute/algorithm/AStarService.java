package com.smartroute.algorithm;

import com.smartroute.dto.RouteResult;
import com.smartroute.model.CityGraph;
import com.smartroute.model.Edge;
import com.smartroute.model.Node;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * A* (A-Star) Shortest Path Algorithm
 *
 * DSA Concepts:
 *   - Informed search: f(n) = g(n) + h(n)
 *   - g(n) = actual cost from source to n (same as Dijkstra's dist[])
 *   - h(n) = heuristic estimate from n to destination (Haversine distance)
 *   - Min-Heap ordered by f(n) = g(n) + h(n)
 *   - Admissible heuristic → A* is optimal (never overestimates)
 *
 * Why A* is faster than Dijkstra:
 *   Dijkstra explores all nodes by distance (circle expanding outward).
 *   A* prioritizes nodes closer to the GOAL — fewer nodes explored.
 *
 * Complexity:
 *   Time:  O(E log V) in practice (explores far fewer nodes than Dijkstra)
 *   Space: O(V) for gScore, fScore, prev tables
 *
 * Heuristic: Haversine formula — great-circle distance between GPS coordinates.
 *   Admissible because road distance >= straight-line distance.
 */
@Component
public class AStarService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Finds the shortest path using A* with Haversine heuristic.
     */
    public RouteResult findShortestPath(CityGraph graph, int src, int dest) {
        validateNodes(graph, src, dest);

        Node destNode = graph.getNode(dest);

        // g(n): actual shortest distance from source to each node
        Map<Integer, Double> gScore = new HashMap<>();

        // f(n) = g(n) + h(n): estimated total cost through each node
        Map<Integer, Double> fScore = new HashMap<>();

        // Parent tracking for path reconstruction
        Map<Integer, Integer> prev = new HashMap<>();

        Set<Integer> visited = new HashSet<>();

        // Min-Heap ordered by f(n)
        PriorityQueue<int[]> openSet = new PriorityQueue<>(
                Comparator.comparingDouble(a -> fScore.getOrDefault(a[0], Double.MAX_VALUE))
        );

        // Initialize source
        gScore.put(src, 0.0);
        fScore.put(src, haversine(graph.getNode(src), destNode));
        openSet.offer(new int[]{src});

        while (!openSet.isEmpty()) {
            int u = openSet.poll()[0];

            if (visited.contains(u)) continue;
            visited.add(u);

            // Goal reached — reconstruct path
            if (u == dest) {
                List<Integer> path = reconstructPath(prev, src, dest);
                double totalDist = gScore.getOrDefault(dest, Double.MAX_VALUE);
                return RouteResult.success(path, totalDist, "A_STAR", graph);
            }

            for (Edge edge : graph.getNeighbors(u)) {
                int v = edge.getTo();
                if (visited.contains(v)) continue;

                double tentativeG = gScore.getOrDefault(u, Double.MAX_VALUE) + edge.effectiveWeight();

                if (tentativeG < gScore.getOrDefault(v, Double.MAX_VALUE)) {
                    // Found a better path to v
                    gScore.put(v, tentativeG);
                    double h = haversine(graph.getNode(v), destNode);
                    fScore.put(v, tentativeG + h);
                    prev.put(v, u);
                    openSet.offer(new int[]{v});
                }
            }
        }

        return RouteResult.unreachable(src, dest, "A_STAR");
    }

    /**
     * Haversine Formula — calculates the great-circle distance between two
     * GPS coordinates. Used as the admissible heuristic h(n) in A*.
     *
     * Formula:
     *   a = sin²(Δlat/2) + cos(lat1)·cos(lat2)·sin²(Δlon/2)
     *   c = 2·atan2(√a, √(1−a))
     *   d = R·c
     *
     * @return distance in kilometers (always ≤ actual road distance → admissible)
     */
    public double haversine(Node a, Node b) {
        if (a == null || b == null) return 0.0;

        double dLat = Math.toRadians(b.getLatitude() - a.getLatitude());
        double dLon = Math.toRadians(b.getLongitude() - a.getLongitude());

        double sinLat = Math.sin(dLat / 2);
        double sinLon = Math.sin(dLon / 2);

        double x = sinLat * sinLat
                + Math.cos(Math.toRadians(a.getLatitude()))
                * Math.cos(Math.toRadians(b.getLatitude()))
                * sinLon * sinLon;

        double c = 2 * Math.atan2(Math.sqrt(x), Math.sqrt(1 - x));
        return EARTH_RADIUS_KM * c;
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

    private void validateNodes(CityGraph graph, int src, int dest) {
        if (!graph.containsNode(src))
            throw new IllegalArgumentException("Source node not found: " + src);
        if (!graph.containsNode(dest))
            throw new IllegalArgumentException("Destination node not found: " + dest);
    }
}
