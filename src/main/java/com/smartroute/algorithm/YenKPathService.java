package com.smartroute.algorithm;

import com.smartroute.dto.RouteResult;
import com.smartroute.model.CityGraph;
import com.smartroute.model.Edge;
import com.smartroute.model.TransportMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Yen's K-Shortest Paths Algorithm
 *
 * DSA Concepts:
 *   - Combines Dijkstra with path "deviation" technique
 *   - Uses a candidate list (min-heap) of potential K-th shortest paths
 *   - Each iteration deviates from the previous best path at a "spur node"
 *   - Handles path uniqueness by temporarily removing edges/nodes
 *
 * Complexity:
 *   Time:  O(K · V · (E + V log V))
 *   Space: O(K · V) for storing K paths
 *
 * Used for: returning top-3 alternative routes to the user.
 *
 * Algorithm Steps:
 *   1. Find shortest path P1 using Dijkstra
 *   2. For k = 2..K:
 *      a. For each spur node in P(k-1):
 *         - Remove edges that lead to already-found paths from spur node
 *         - Remove nodes in root path (to avoid cycles)
 *         - Run Dijkstra from spur node to dest
 *         - Combine root path + spur path → candidate
 *      b. Add best candidate to result list
 */
@Component
public class YenKPathService {

    @Autowired
    private DijkstraService dijkstra;

    /**
     * Returns up to K shortest paths between src and dest.
     *
     * @param graph the city graph
     * @param src   source node ID
     * @param dest  destination node ID
     * @param k     number of paths to find (typically 3)
     * @return list of RouteResult, sorted by total distance ascending
     */
    public List<RouteResult> findKShortestPaths(CityGraph graph, int src, int dest, int k) {
        // A: confirmed K shortest paths
        List<List<Integer>> A = new ArrayList<>();

        // B: candidates min-heap ordered by path cost
        PriorityQueue<PathCandidate> B = new PriorityQueue<>(
                Comparator.comparingDouble(pc -> pc.cost)
        );

        // Step 1: Find the first shortest path
        RouteResult firstRoute = dijkstra.findShortestPath(graph, src, dest);
        if (!firstRoute.isReachable()) {
            return Collections.emptyList();
        }
        A.add(firstRoute.getPath());

        // Step 2: Find paths k = 2..K
        for (int i = 1; i < k; i++) {
            List<Integer> prevPath = A.get(i - 1);

            // Try each node in previous path as the spur node
            for (int spurIdx = 0; spurIdx < prevPath.size() - 1; spurIdx++) {
                int spurNode = prevPath.get(spurIdx);
                List<Integer> rootPath = prevPath.subList(0, spurIdx + 1);

                // Track temporarily removed edges and nodes
                List<EdgeRemoval> removedEdges = new ArrayList<>();
                List<Integer> removedNodes = new ArrayList<>();

                // Remove edges from spur node that are shared with existing found paths
                for (List<Integer> path : A) {
                    if (path.size() > spurIdx && path.subList(0, spurIdx + 1).equals(rootPath)) {
                        if (spurIdx + 1 < path.size()) {
                            int nextNode = path.get(spurIdx + 1);
                            // Mark this edge as blocked temporarily
                            blockEdgeTemporarily(graph, spurNode, nextNode, removedEdges);
                        }
                    }
                }

                // Remove all nodes in root path (except spur node) to prevent cycles
                for (int j = 0; j < rootPath.size() - 1; j++) {
                    int rootNode = rootPath.get(j);
                    removeNodeTemporarily(graph, rootNode, removedEdges);
                    removedNodes.add(rootNode);
                }

                // Find spur path from spur node to destination
                RouteResult spurRoute = dijkstra.findShortestPath(graph, spurNode, dest);

                if (spurRoute.isReachable()) {
                    // Combine root path + spur path (avoid duplicating spur node)
                    List<Integer> totalPath = new ArrayList<>(rootPath);
                    List<Integer> spurPath = spurRoute.getPath();
                    totalPath.addAll(spurPath.subList(1, spurPath.size()));

                    double rootCost = computePathCost(graph, rootPath);
                    double totalCost = rootCost + spurRoute.getTotalDistance();

                    PathCandidate candidate = new PathCandidate(totalPath, totalCost);

                    // Add only if not already a candidate
                    if (!containsPath(B, totalPath) && !A.contains(totalPath)) {
                        B.offer(candidate);
                    }
                }

                // Restore temporarily removed edges
                restoreEdges(removedEdges);
            }

            if (B.isEmpty()) break; // No more paths available

            // Move best candidate from B to A
            PathCandidate best = B.poll();
            A.add(best.path);
        }

        // Convert paths to RouteResult objects
        List<RouteResult> results = new ArrayList<>();
        for (List<Integer> path : A) {
            double cost = computePathCost(graph, path);
            results.add(RouteResult.success(path, cost, "YEN_K_SHORTEST", graph));
        }
        return results;
    }

    private double computePathCost(CityGraph graph, List<Integer> path) {
        double total = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            int u = path.get(i), v = path.get(i + 1);
            total += graph.getNeighbors(u).stream()
                    .filter(e -> e.getTo() == v)
                    .mapToDouble(Edge::effectiveWeight)
                    .min()
                    .orElse(Double.MAX_VALUE);
        }
        return total;
    }

    private void blockEdgeTemporarily(CityGraph graph, int from, int to, List<EdgeRemoval> removed) {
        graph.getNeighbors(from).stream()
                .filter(e -> e.getTo() == to && !e.isBlocked())
                .forEach(e -> {
                    e.setBlocked(true);
                    removed.add(new EdgeRemoval(e, false));
                });
    }

    private void removeNodeTemporarily(CityGraph graph, int nodeId, List<EdgeRemoval> removed) {
        // Block all outgoing edges from this node
        graph.getNeighbors(nodeId).stream()
                .filter(e -> !e.isBlocked())
                .forEach(e -> {
                    e.setBlocked(true);
                    removed.add(new EdgeRemoval(e, false));
                });
    }

    private void restoreEdges(List<EdgeRemoval> removed) {
        removed.forEach(er -> er.edge.setBlocked(er.wasBlocked));
    }

    private boolean containsPath(PriorityQueue<PathCandidate> queue, List<Integer> path) {
        return queue.stream().anyMatch(pc -> pc.path.equals(path));
    }

    // ─── Inner classes ──────────────────────────────

    private static class PathCandidate {
        final List<Integer> path;
        final double cost;

        PathCandidate(List<Integer> path, double cost) {
            this.path = new ArrayList<>(path);
            this.cost = cost;
        }
    }

    private static class EdgeRemoval {
        final Edge edge;
        final boolean wasBlocked;

        EdgeRemoval(Edge edge, boolean wasBlocked) {
            this.edge = edge;
            this.wasBlocked = wasBlocked;
        }
    }
}
