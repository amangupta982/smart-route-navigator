package com.smartroute;

import com.smartroute.algorithm.DijkstraService;
import com.smartroute.dto.RouteResult;
import com.smartroute.model.CityGraph;
import com.smartroute.model.TransportMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DijkstraService.
 * Tests correctness of shortest paths on various graph configurations.
 */
class DijkstraServiceTest {

    private DijkstraService dijkstra;
    private CityGraph graph;

    @BeforeEach
    void setUp() {
        dijkstra = new DijkstraService();
        graph = new CityGraph();

        // Build a simple 5-node test graph:
        //   1 --2-- 2 --3-- 3
        //   |               |
        //   4       5 --1-- 3
        //   |
        //   4 --6-- 5
        //
        // Shortest path 1→5 = 1→2→3→5 (cost=6) not 1→4→5 (cost=10)

        graph.addNode(1, 0.0, 0.0, "A");
        graph.addNode(2, 0.0, 1.0, "B");
        graph.addNode(3, 0.0, 2.0, "C");
        graph.addNode(4, 1.0, 0.0, "D");
        graph.addNode(5, 0.0, 3.0, "E");

        graph.addEdge(1, 2, 2.0, TransportMode.CAR);
        graph.addEdge(2, 3, 3.0, TransportMode.CAR);
        graph.addEdge(3, 5, 1.0, TransportMode.CAR);
        graph.addEdge(1, 4, 4.0, TransportMode.CAR);
        graph.addEdge(4, 5, 6.0, TransportMode.CAR);
        graph.addEdge(2, 5, 10.0, TransportMode.CAR);  // longer direct path
    }

    @Test
    @DisplayName("Dijkstra finds correct shortest path")
    void testShortestPath() {
        RouteResult result = dijkstra.findShortestPath(graph, 1, 5);

        assertTrue(result.isReachable());
        assertEquals(6.0, result.getTotalDistance(), 0.01);
        assertEquals(java.util.List.of(1, 2, 3, 5), result.getPath());
        assertEquals("DIJKSTRA", result.getAlgorithm());
    }

    @Test
    @DisplayName("Dijkstra returns correct path length")
    void testPathLength() {
        RouteResult result = dijkstra.findShortestPath(graph, 1, 5);
        assertEquals(4, result.getPath().size()); // 4 nodes = 3 hops
        assertEquals(3, result.getHops());
    }

    @Test
    @DisplayName("Dijkstra handles same source and destination")
    void testSameSourceDest() {
        RouteResult result = dijkstra.findShortestPath(graph, 1, 1);
        assertTrue(result.isReachable());
        assertEquals(0.0, result.getTotalDistance(), 0.001);
        assertEquals(java.util.List.of(1), result.getPath());
    }

    @Test
    @DisplayName("Dijkstra returns unreachable when no path exists")
    void testNoPath() {
        // Add isolated node
        graph.addNode(99, 10.0, 10.0, "Isolated");

        RouteResult result = dijkstra.findShortestPath(graph, 1, 99);
        assertFalse(result.isReachable());
        assertTrue(result.getPath().isEmpty());
    }

    @Test
    @DisplayName("Dijkstra avoids blocked edges")
    void testBlockedEdge() {
        graph.blockRoad(2, 3); // block 2→3

        RouteResult result = dijkstra.findShortestPath(graph, 1, 5);
        assertTrue(result.isReachable());
        // Should now route via 1→4→5 (cost=10) or 1→2→5 (cost=12)
        assertFalse(result.getPath().contains(3)); // should not pass through 3
    }

    @Test
    @DisplayName("Dijkstra uses updated traffic weights")
    void testTrafficWeight() {
        double originalDist = dijkstra.findShortestPath(graph, 1, 5).getTotalDistance();

        // Make road 2→3 very congested (5x multiplier)
        graph.updateTraffic(2, 3, 5.0);

        double newDist = dijkstra.findShortestPath(graph, 1, 5).getTotalDistance();
        assertTrue(newDist > originalDist, "Route should be longer after traffic congestion");
    }

    @Test
    @DisplayName("Dijkstra throws exception for invalid source node")
    void testInvalidSourceNode() {
        assertThrows(IllegalArgumentException.class,
                () -> dijkstra.findShortestPath(graph, 999, 5));
    }

    @Test
    @DisplayName("Dijkstra throws exception for invalid destination node")
    void testInvalidDestNode() {
        assertThrows(IllegalArgumentException.class,
                () -> dijkstra.findShortestPath(graph, 1, 999));
    }
}
