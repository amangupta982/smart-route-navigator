package com.smartroute.algorithm;

import com.smartroute.dto.RouteResult;
import com.smartroute.model.CityGraph;
import com.smartroute.model.Edge;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Dijkstra's Shortest Path Algorithm
 *
 * DSA Concepts:
 *   - Greedy algorithm using a Min-Heap (PriorityQueue)
 *   - Dynamic Programming: dist[] table stores optimal subproblem solutions
 *   - Path reconstruction via prev[] parent array
 *
 * Complexity:
 *   Time:  O((V + E) log V) with binary min-heap
 *   Space: O(V) for dist[], prev[], and heap
 *
 * Works correctly only with NON-NEGATIVE edge weights.
 * For negative weights → use Bellman-Ford instead.
 */
@Component
public class DijkstraService {

    /**
     * Finds the shortest path from src to dest in the given graph.
     *
     * @param graph  the city graph
     * @param src    source node ID
     * @param dest   destination node ID
     * @return RouteResult containing path, distance, and metadata
     */
    public RouteResult findShortestPath(CityGraph graph, int src, int dest) {
        validateNodes(graph, src, dest);

        // DP table: dist[v] = shortest known distance from src to v
        Map<Integer, Double> dist = new HashMap<>();

        // Parent table: prev[v] = node we came from to reach v on shortest path
        Map<Integer, Integer> prev = new HashMap<>();

        // Visited set: once a node is finalized, skip it
        Set<Integer> visited = new HashSet<>();

        // Min-Heap: [distance, nodeId] — ordered by distance (index 0)
        // This is the key data structure: O(log V) poll/offer
        PriorityQueue<double[]> minHeap = new PriorityQueue<>(
                Comparator.comparingDouble(a -> a[0])
        );

        // Initialize: distance to source = 0, all others = ∞
        for (int nodeId : graph.getAllNodeIds()) {
            dist.put(nodeId, Double.MAX_VALUE);
        }
        dist.put(src, 0.0);
        minHeap.offer(new double[]{0.0, src});

        while (!minHeap.isEmpty()) {
            double[] curr = minHeap.poll();
            double currentDist = curr[0];
            int u = (int) curr[1];

            // Skip if already finalized (stale entry in heap)
            if (visited.contains(u)) continue;
            visited.add(u);

            // Early termination: we've reached the destination
            if (u == dest) break;

            // Relaxation: for each neighbor v of u
            for (Edge edge : graph.getNeighbors(u)) {
                int v = edge.getTo();
                if (visited.contains(v)) continue;

                double newDist = currentDist + edge.effectiveWeight();

                // Relaxation step: if new path is shorter, update
                if (newDist < dist.getOrDefault(v, Double.MAX_VALUE)) {
                    dist.put(v, newDist);
                    prev.put(v, u);
                    minHeap.offer(new double[]{newDist, v});
                }
            }
        }

        // Check if destination is reachable
        double totalDist = dist.getOrDefault(dest, Double.MAX_VALUE);
        if (totalDist == Double.MAX_VALUE) {
            return RouteResult.unreachable(src, dest, "DIJKSTRA");
        }

        // Reconstruct path by following prev[] from dest back to src
        List<Integer> path = reconstructPath(prev, src, dest);
        return RouteResult.success(path, totalDist, "DIJKSTRA", graph);
    }

    /**
     * Reconstructs the path from destination back to source using the prev[] map.
     * DSA: O(V) backtracking through parent pointers.
     */
    private List<Integer> reconstructPath(Map<Integer, Integer> prev, int src, int dest) {
        LinkedList<Integer> path = new LinkedList<>();
        int current = dest;

        while (current != src) {
            path.addFirst(current);
            Integer parent = prev.get(current);
            if (parent == null) return Collections.emptyList(); // disconnected
            current = parent;
        }
        path.addFirst(src);
        return new ArrayList<>(path);
    }

    /**
     * Returns just the distance (useful for Yen's K-Shortest algorithm).
     */
    public double computeDistance(CityGraph graph, int src, int dest) {
        return findShortestPath(graph, src, dest).getTotalDistance();
    }

    private void validateNodes(CityGraph graph, int src, int dest) {
        if (!graph.containsNode(src)) {
            throw new IllegalArgumentException("Source node not found: " + src);
        }
        if (!graph.containsNode(dest)) {
            throw new IllegalArgumentException("Destination node not found: " + dest);
        }
    }
}
