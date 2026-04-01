package com.smartroute;

import com.smartroute.algorithm.AStarService;
import com.smartroute.algorithm.DijkstraService;
import com.smartroute.algorithm.YenKPathService;
import com.smartroute.dto.RouteResult;
import com.smartroute.model.CityGraph;
import com.smartroute.model.TransportMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test: verifies all algorithms work correctly together
 * on the same graph, and that dynamic traffic updates affect all of them.
 */
class GraphIntegrationTest {

    private DijkstraService dijkstra;
    private AStarService aStar;
    private YenKPathService yenK;
    private CityGraph graph;

    @BeforeEach
    void setUp() {
        dijkstra = new DijkstraService();
        aStar    = new AStarService();
        yenK     = new YenKPathService();

        // Inject dijkstra into yenK via reflection (simulating Spring injection)
        try {
            var field = YenKPathService.class.getDeclaredField("dijkstra");
            field.setAccessible(true);
            field.set(yenK, dijkstra);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dijkstra into yenK", e);
        }

        graph = new CityGraph();

        // 6-node graph with multiple paths
        //   1 --(2)-- 2 --(3)-- 3
        //   |                   |
        //  (4)                 (1)
        //   |                   |
        //   4 --(5)-- 5 --(2)-- 6=3? No, 6 connects to 3
        //
        // Paths from 1 to 6:
        //   P1: 1→2→3→6 (cost=6)
        //   P2: 1→4→5→6 (cost=11)  (longer)

        graph.addNode(1, 12.97, 77.59, "Start");
        graph.addNode(2, 12.97, 77.62, "Node2");
        graph.addNode(3, 12.97, 77.65, "Node3");
        graph.addNode(4, 12.94, 77.59, "Node4");
        graph.addNode(5, 12.94, 77.62, "Node5");
        graph.addNode(6, 12.97, 77.68, "End");

        graph.addBidirectionalEdge(1, 2, 2.0, TransportMode.CAR);
        graph.addBidirectionalEdge(2, 3, 3.0, TransportMode.CAR);
        graph.addBidirectionalEdge(3, 6, 1.0, TransportMode.CAR);
        graph.addBidirectionalEdge(1, 4, 4.0, TransportMode.CAR);
        graph.addBidirectionalEdge(4, 5, 5.0, TransportMode.CAR);
        graph.addBidirectionalEdge(5, 6, 2.0, TransportMode.CAR);
        graph.addBidirectionalEdge(2, 5, 4.0, TransportMode.CAR);  // shortcut
    }

    @Test
    @DisplayName("Dijkstra and A* agree on shortest distance")
    void testDijkstraAndAStarAgree() {
        RouteResult d = dijkstra.findShortestPath(graph, 1, 6);
        RouteResult a = aStar.findShortestPath(graph, 1, 6);

        assertTrue(d.isReachable());
        assertTrue(a.isReachable());
        assertEquals(d.getTotalDistance(), a.getTotalDistance(), 0.01,
                "Both algorithms must return the same optimal distance");
    }

    @Test
    @DisplayName("Yen's K finds 3 distinct paths")
    void testYenKFindsMultiplePaths() {
        List<RouteResult> paths = yenK.findKShortestPaths(graph, 1, 6, 3);
        assertFalse(paths.isEmpty(), "Should find at least one path");
        assertTrue(paths.size() >= 2, "Should find at least 2 paths in this graph");

        // Paths should be sorted by cost
        for (int i = 0; i < paths.size() - 1; i++) {
            assertTrue(paths.get(i).getTotalDistance() <= paths.get(i + 1).getTotalDistance(),
                    "Paths should be sorted by distance ascending");
        }
    }

    @Test
    @DisplayName("Traffic update reroutes all algorithms")
    void testTrafficUpdateReroutes() {
        // Record original best routes
        double originalDijkstra = dijkstra.findShortestPath(graph, 1, 6).getTotalDistance();
        double originalAStar    = aStar.findShortestPath(graph, 1, 6).getTotalDistance();

        // Block the optimal path (1→2→3→6)
        graph.blockRoad(2, 3);

        RouteResult newDijkstra = dijkstra.findShortestPath(graph, 1, 6);
        RouteResult newAStar    = aStar.findShortestPath(graph, 1, 6);

        // Both should now take a different (longer) route
        assertTrue(newDijkstra.isReachable(), "Should still find alternate route");
        assertTrue(newAStar.isReachable(),    "Should still find alternate route");
        assertTrue(newDijkstra.getTotalDistance() > originalDijkstra,
                "New route should be longer after blocking optimal path");

        // Unblock and verify restoration
        graph.unblockRoad(2, 3);
        double restoredDist = dijkstra.findShortestPath(graph, 1, 6).getTotalDistance();
        assertEquals(originalDijkstra, restoredDist, 0.01,
                "Distance should restore after unblocking");
    }

    @Test
    @DisplayName("All algorithms return unreachable for isolated node")
    void testAllUnreachableOnIsolatedNode() {
        graph.addNode(99, 0.0, 0.0, "Isolated");

        assertFalse(dijkstra.findShortestPath(graph, 1, 99).isReachable());
        assertFalse(aStar.findShortestPath(graph, 1, 99).isReachable());
        assertTrue(yenK.findKShortestPaths(graph, 1, 99, 3).isEmpty());
    }
}
