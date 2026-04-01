package com.smartroute.controller;

import com.smartroute.dto.RouteRequest;
import com.smartroute.dto.RouteResult;
import com.smartroute.service.RouterFacade;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * RouteController — Handles all route-finding API requests.
 *
 * Endpoints:
 *   POST /api/route             → Find single shortest route
 *   POST /api/route/alternatives → Find top-K alternative routes
 *   GET  /api/route/algorithms  → List available algorithms
 *
 * All algorithms operate on the shared CityGraph maintained by GraphLoaderService.
 */
@RestController
@RequestMapping("/api/route")
@CrossOrigin(origins = "*")
public class RouteController {

    @Autowired
    private RouterFacade router;

    /**
     * POST /api/route
     *
     * Finds the optimal route from source to destination.
     * Algorithm is selected via the "algorithm" field in the request body.
     *
     * Request body example:
     * {
     *   "source": 1,
     *   "destination": 12,
     *   "algorithm": "A_STAR"
     * }
     *
     * Response: RouteResult with path, distance, time estimate, and transport modes.
     */
    @PostMapping
    public ResponseEntity<?> findRoute(@Valid @RequestBody RouteRequest request) {
        try {
            RouteResult result = router.findRoute(request);

            if (!result.isReachable()) {
                return ResponseEntity.status(404).body(Map.of(
                        "error", result.getMessage(),
                        "source", request.getSource(),
                        "destination", request.getDestination()
                ));
            }

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/route/alternatives
     *
     * Returns top-K alternative routes using Yen's K-Shortest Paths.
     *
     * Request body example:
     * {
     *   "source": 1,
     *   "destination": 12,
     *   "k": 3
     * }
     */
    @PostMapping("/alternatives")
    public ResponseEntity<?> findAlternativeRoutes(@Valid @RequestBody RouteRequest request) {
        try {
            List<RouteResult> results = router.findKRoutes(request);

            if (results.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                        "error", "No routes found",
                        "source", request.getSource(),
                        "destination", request.getDestination()
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "source", request.getSource(),
                    "destination", request.getDestination(),
                    "k", results.size(),
                    "routes", results
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/route/algorithms
     * Returns a list of supported routing algorithms with descriptions.
     */
    @GetMapping("/algorithms")
    public ResponseEntity<?> listAlgorithms() {
        return ResponseEntity.ok(List.of(
                Map.of("name", "DIJKSTRA",
                        "description", "Guaranteed shortest path. O((V+E)logV). Best for correctness.",
                        "complexity", "O((V+E) log V)"),
                Map.of("name", "A_STAR",
                        "description", "Heuristic search with Haversine. Faster than Dijkstra on large maps.",
                        "complexity", "O(E log V) in practice"),
                Map.of("name", "BFS",
                        "description", "Minimum hop count. Optimal for unweighted metro/bus routing.",
                        "complexity", "O(V + E)"),
                Map.of("name", "K_SHORTEST",
                        "description", "Yen's algorithm returns top-K alternative paths.",
                        "complexity", "O(K * V * (E + V log V))")
        ));
    }
}
