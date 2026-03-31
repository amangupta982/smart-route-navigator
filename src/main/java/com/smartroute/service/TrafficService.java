package com.smartroute.service;

import com.smartroute.dto.TrafficUpdateRequest;
import com.smartroute.model.CityGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * TrafficService — Manages Dynamic Edge Weight Updates
 *
 * Handles:
 *   - Applying traffic congestion multipliers to road segments
 *   - Blocking specific roads (road closure simulation)
 *   - Resetting traffic to clear conditions
 *   - Tracking current traffic state for reporting
 *
 * DSA: Modifies edge weights in O(degree) time.
 * The routing algorithms automatically pick up new weights on next call.
 */
@Service
public class TrafficService {

    @Autowired private GraphLoaderService graphLoader;

    // In-memory traffic log: "from→to" → multiplier
    private final Map<String, Double> trafficLog = new HashMap<>();
    private final Map<String, Boolean> blockLog = new HashMap<>();

    /**
     * Applies a traffic update to a specific road segment.
     */
    public String applyTrafficUpdate(TrafficUpdateRequest request) {
        CityGraph graph = graphLoader.getGraph();
        int from = request.getFrom();
        int to = request.getTo();

        if (!graph.containsNode(from) || !graph.containsNode(to)) {
            throw new IllegalArgumentException(
                    "Invalid road: node " + from + " or " + to + " not found.");
        }

        String key = from + "→" + to;

        if (request.isBlocked()) {
            graph.blockRoad(from, to);
            blockLog.put(key, true);
            trafficLog.remove(key);
            return String.format("Road %d→%d is now BLOCKED. Re-routing activated.", from, to);
        } else {
            graph.updateTraffic(from, to, request.getMultiplier());
            trafficLog.put(key, request.getMultiplier());
            blockLog.remove(key);
            return String.format("Traffic on road %d→%d updated: %.1fx multiplier applied.",
                    from, to, request.getMultiplier());
        }
    }

    /**
     * Unblocks a specific road and resets its traffic multiplier.
     */
    public String unblockRoad(int from, int to) {
        CityGraph graph = graphLoader.getGraph();
        graph.unblockRoad(from, to);

        String key = from + "→" + to;
        blockLog.remove(key);
        trafficLog.remove(key);

        return String.format("Road %d→%d has been UNBLOCKED.", from, to);
    }

    /**
     * Resets ALL roads to normal traffic conditions.
     */
    public String resetAllTraffic() {
        graphLoader.getGraph().resetAllTraffic();
        trafficLog.clear();
        blockLog.clear();
        return "All traffic conditions reset to normal.";
    }

    /**
     * Returns a summary of currently active traffic updates.
     */
    public Map<String, Object> getTrafficStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("blockedRoads", blockLog);
        status.put("congestedRoads", trafficLog);
        status.put("totalAffectedSegments", trafficLog.size() + blockLog.size());
        return status;
    }
}
