package com.smartroute.service;

import com.smartroute.algorithm.*;
import com.smartroute.dto.RouteRequest;
import com.smartroute.dto.RouteResult;
import com.smartroute.model.CityGraph;
import com.smartroute.model.TransportMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RouterFacade — Strategy Pattern for Algorithm Selection
 *
 * Acts as the single entry point for all routing requests.
 * Selects the appropriate algorithm based on request parameters:
 *
 *   DIJKSTRA    → guaranteed shortest path, slower on large graphs
 *   A_STAR      → faster goal-directed search with Haversine heuristic
 *   BFS         → unweighted hop-count minimization (metro stops)
 *   K_SHORTEST  → Yen's K-shortest paths for alternatives
 *
 * Design Pattern: Facade + Strategy
 *   Facade: simplifies complex subsystem (algorithms) behind one interface
 *   Strategy: switches algorithm at runtime based on request
 */
@Service
public class RouterFacade {

    @Autowired private DijkstraService dijkstra;
    @Autowired private AStarService aStar;
    @Autowired private BFSService bfs;
    @Autowired private YenKPathService yenK;
    @Autowired private GraphLoaderService graphLoader;

    /**
     * Routes a request to the appropriate algorithm and returns the result.
     */
    public RouteResult findRoute(RouteRequest request) {
        CityGraph graph = graphLoader.getGraph();

        return switch (request.getAlgorithm().toUpperCase()) {
            case "DIJKSTRA" -> dijkstra.findShortestPath(graph, request.getSource(), request.getDestination());
            case "A_STAR"   -> aStar.findShortestPath(graph, request.getSource(), request.getDestination());
            case "BFS"      -> {
                TransportMode mode = parseMode(request.getPreferredMode());
                yield bfs.findMinHopPath(graph, request.getSource(), request.getDestination(), mode);
            }
            case "K_SHORTEST" -> {
                // Returns first result of K-shortest (best path)
                List<RouteResult> paths = yenK.findKShortestPaths(
                        graph, request.getSource(), request.getDestination(), request.getK());
                yield paths.isEmpty() ? RouteResult.unreachable(
                        request.getSource(), request.getDestination(), "K_SHORTEST") : paths.get(0);
            }
            default -> throw new IllegalArgumentException(
                    "Unknown algorithm: " + request.getAlgorithm()
                    + ". Valid options: DIJKSTRA, A_STAR, BFS, K_SHORTEST");
        };
    }

    /**
     * Returns top K shortest paths for alternate route suggestions.
     */
    public List<RouteResult> findKRoutes(RouteRequest request) {
        CityGraph graph = graphLoader.getGraph();
        return yenK.findKShortestPaths(
                graph, request.getSource(), request.getDestination(), request.getK());
    }

    private TransportMode parseMode(String mode) {
        if (mode == null || mode.isBlank()) return null;
        try {
            return TransportMode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
