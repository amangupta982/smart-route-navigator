package com.smartroute.controller;

import com.smartroute.dto.TrafficUpdateRequest;
import com.smartroute.service.TrafficService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * TrafficController — Manages real-time traffic updates on the city graph.
 *
 * Endpoints:
 *   POST   /api/traffic/update      → Apply congestion / block a road
 *   DELETE /api/traffic/unblock     → Remove block from a road
 *   DELETE /api/traffic/reset       → Reset all traffic to normal
 *   GET    /api/traffic/status      → View current traffic conditions
 *
 * After updating traffic, calling /api/route will automatically use
 * the updated edge weights — no restart needed.
 */
@RestController
@RequestMapping("/api/traffic")
@CrossOrigin(origins = "*")
public class TrafficController {

    @Autowired
    private TrafficService trafficService;

    /**
     * POST /api/traffic/update
     *
     * Apply traffic congestion or block a road entirely.
     *
     * Example – slow traffic:
     * { "from": 1, "to": 6, "multiplier": 3.0, "blocked": false }
     *
     * Example – block road:
     * { "from": 1, "to": 6, "blocked": true }
     */
    @PostMapping("/update")
    public ResponseEntity<?> updateTraffic(@Valid @RequestBody TrafficUpdateRequest request) {
        try {
            String message = trafficService.applyTrafficUpdate(request);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", message,
                    "from", request.getFrom(),
                    "to", request.getTo()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/traffic/unblock?from=1&to=6
     * Removes a road block and resets traffic to normal.
     */
    @DeleteMapping("/unblock")
    public ResponseEntity<?> unblockRoad(
            @RequestParam int from,
            @RequestParam int to) {
        try {
            String message = trafficService.unblockRoad(from, to);
            return ResponseEntity.ok(Map.of("status", "success", "message", message));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/traffic/reset
     * Resets all roads to normal (clear traffic) conditions.
     */
    @DeleteMapping("/reset")
    public ResponseEntity<?> resetAllTraffic() {
        String message = trafficService.resetAllTraffic();
        return ResponseEntity.ok(Map.of("status", "success", "message", message));
    }

    /**
     * GET /api/traffic/status
     * Returns all currently blocked or congested roads.
     */
    @GetMapping("/status")
    public ResponseEntity<?> getTrafficStatus() {
        return ResponseEntity.ok(trafficService.getTrafficStatus());
    }
}
