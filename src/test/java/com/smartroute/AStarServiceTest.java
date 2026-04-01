package com.smartroute;

import com.smartroute.algorithm.AStarService;
import com.smartroute.algorithm.DijkstraService;
import com.smartroute.dto.RouteResult;
import com.smartroute.model.CityGraph;
import com.smartroute.model.Node;
import com.smartroute.model.TransportMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AStarService.
 * Verifies A* optimality and that it agrees with Dijkstra.
 */
class AStarServiceTest {

    private AStarService aStar;
    private DijkstraService dijkstra;
    private CityGraph graph;

    @BeforeEach
    void setUp() {
        aStar = new AStarService();
        dijkstra = new DijkstraService();
        graph = new CityGraph();

        // Grid-like graph with GPS coordinates
        // 1(0,0) - 2(0,1) - 3(0,2)
        //   |         |         |
        // 4(1,0) - 5(1,1) - 6(1,2)
        graph.addNode(1, 0.0, 0.0, "TL");
        graph.addNode(2, 0.0, 0.5, "TC");
        graph.addNode(3, 0.0, 1.0, "TR");
        graph.addNode(4, 0.5, 0.0, "BL");
        graph.addNode(5, 0.5, 0.5, "BC");
        graph.addNode(6, 0.5, 1.0, "BR");

        graph.addBidirectionalEdge(1, 2, 2.0, TransportMode.CAR);
        graph.addBidirectionalEdge(2, 3, 2.0, TransportMode.CAR);
        graph.addBidirectionalEdge(4, 5, 2.0, TransportMode.CAR);
        graph.addBidirectionalEdge(5, 6, 2.0, TransportMode.CAR);
        graph.addBidirectionalEdge(1, 4, 3.0, TransportMode.CAR);
        graph.addBidirectionalEdge(2, 5, 3.0, TransportMode.CAR);
        graph.addBidirectionalEdge(3, 6, 3.0, TransportMode.CAR);
    }

    @Test
    @DisplayName("A* finds the same distance as Dijkstra")
    void testAStarMatchesDijkstra() {
        RouteResult aStarResult = aStar.findShortestPath(graph, 1, 6);
        RouteResult dijkResult  = dijkstra.findShortestPath(graph, 1, 6);

        assertTrue(aStarResult.isReachable());
        assertEquals(dijkResult.getTotalDistance(), aStarResult.getTotalDistance(), 0.01,
                "A* and Dijkstra must return same optimal distance");
    }

    @Test
    @DisplayName("A* returns reachable result for connected nodes")
    void testAStarReachable() {
        RouteResult result = aStar.findShortestPath(graph, 1, 3);
        assertTrue(result.isReachable());
        assertEquals("A_STAR", result.getAlgorithm());
    }

    @Test
    @DisplayName("A* returns unreachable for disconnected nodes")
    void testAStarUnreachable() {
        graph.addNode(99, 99.0, 99.0, "Far Away");
        RouteResult result = aStar.findShortestPath(graph, 1, 99);
        assertFalse(result.isReachable());
    }

    @Test
    @DisplayName("Haversine distance is zero for same coordinates")
    void testHaversineSamePoint() {
        Node a = new Node(1, 12.9716, 77.5946, "Test");
        double dist = aStar.haversine(a, a);
        assertEquals(0.0, dist, 0.001);
    }

    @Test
    @DisplayName("Haversine distance between MG Road and Koramangala is ~5km")
    void testHaversineKnownDistance() {
        // Approximate real Bangalore coordinates
        Node mgRoad      = new Node(1, 12.9716, 77.5946, "MG Road");
        Node koramangala = new Node(2, 12.9352, 77.6245, "Koramangala");

        double dist = aStar.haversine(mgRoad, koramangala);
        // Real road distance ~5-6 km, straight-line (haversine) ~4.5 km
        assertTrue(dist > 3.0 && dist < 8.0,
                "Haversine distance should be between 3 and 8 km, was: " + dist);
    }

    @Test
    @DisplayName("A* handles same source and destination")
    void testSameSourceDest() {
        RouteResult result = aStar.findShortestPath(graph, 1, 1);
        assertTrue(result.isReachable());
        assertEquals(0.0, result.getTotalDistance(), 0.001);
    }
}
