package com.smartroute.controller;

import com.smartroute.dto.GraphInfoResponse;
import com.smartroute.service.GraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/**
 * GraphController — Provides graph metadata and connectivity queries.
 *
 * Endpoints:
 *   GET /api/graph/info                  → Full graph details (nodes, edges, components)
 *   GET /api/graph/connected?a=1&b=12    → Check if two nodes are connected
 *   GET /api/graph/component?node=5      → All nodes in same component as given node
 *   GET /api/graph/would-disconnect      → Check if blocking a road disconnects city
 */
@RestController
@RequestMapping("/api/graph")
@CrossOrigin(origins = "*")
public class GraphController {

    @Autowired
    private GraphService graphService;

    /**
     * GET /api/graph/info
     * Returns full graph information: node list, edge count, connectivity status.
     */
    @GetMapping("/info")
    public ResponseEntity<GraphInfoResponse> getGraphInfo() {
        return ResponseEntity.ok(graphService.getGraphInfo());
    }

    /**
     * GET /api/graph/connected?a=1&b=12
     * Uses Union-Find to check if two nodes are in the same connected component.
     * O(α(N)) ≈ O(1) per query.
     */
    @GetMapping("/connected")
    public ResponseEntity<?> checkConnectivity(
            @RequestParam int a,
            @RequestParam int b) {
        boolean connected = graphService.areConnected(a, b);
        return ResponseEntity.ok(Map.of(
                "nodeA", a,
                "nodeB", b,
                "connected", connected,
                "message", connected
                        ? "Nodes " + a + " and " + b + " are in the same connected component."
                        : "Nodes " + a + " and " + b + " are in DIFFERENT components — no path exists."
        ));
    }

    /**
     * GET /api/graph/component?node=5
     * Returns all nodes reachable from the given node (its connected component).
     */
    @GetMapping("/component")
    public ResponseEntity<?> getComponent(@RequestParam int node) {
        Set<Integer> component = graphService.getComponentOf(node);
        return ResponseEntity.ok(Map.of(
                "node", node,
                "componentSize", component.size(),
                "nodeIds", component
        ));
    }

    /**
     * GET /api/graph/would-disconnect?from=1&to=6
     * Checks if removing the road from→to would split the city into disconnected parts.
     * Useful for traffic management decisions.
     */
    @GetMapping("/would-disconnect")
    public ResponseEntity<?> wouldDisconnect(
            @RequestParam int from,
            @RequestParam int to) {
        boolean disconnects = graphService.wouldBlockDisconnectCity(from, to);
        return ResponseEntity.ok(Map.of(
                "from", from,
                "to", to,
                "wouldDisconnect", disconnects,
                "warning", disconnects
                        ? "CRITICAL: Blocking this road disconnects parts of the city!"
                        : "Safe to block: alternative routes maintain full connectivity."
        ));
    }
}
