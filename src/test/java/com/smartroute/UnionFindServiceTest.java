package com.smartroute;

import com.smartroute.algorithm.UnionFindService;
import com.smartroute.model.CityGraph;
import com.smartroute.model.TransportMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UnionFindService (Disjoint Set Union).
 * Tests connectivity detection, component counting, and path compression.
 */
class UnionFindServiceTest {

    private UnionFindService unionFind;
    private CityGraph graph;

    @BeforeEach
    void setUp() {
        unionFind = new UnionFindService();
        graph = new CityGraph();

        // Two separate components:
        // Component 1: 1 - 2 - 3
        // Component 2: 4 - 5
        graph.addNode(1, 0.0, 0.0, "A");
        graph.addNode(2, 0.0, 1.0, "B");
        graph.addNode(3, 0.0, 2.0, "C");
        graph.addNode(4, 5.0, 0.0, "D");
        graph.addNode(5, 5.0, 1.0, "E");

        graph.addBidirectionalEdge(1, 2, 1.0, TransportMode.CAR);
        graph.addBidirectionalEdge(2, 3, 1.0, TransportMode.CAR);
        graph.addBidirectionalEdge(4, 5, 1.0, TransportMode.CAR);
        // No edge between component 1 and component 2
    }

    @Test
    @DisplayName("Detects two separate connected components")
    void testTwoComponents() {
        unionFind.initialize(graph);
        assertEquals(2, unionFind.getComponentCount());
    }

    @Test
    @DisplayName("Nodes in same component are connected")
    void testSameComponent() {
        unionFind.initialize(graph);
        assertTrue(unionFind.connected(1, 2));
        assertTrue(unionFind.connected(1, 3));
        assertTrue(unionFind.connected(2, 3));
        assertTrue(unionFind.connected(4, 5));
    }

    @Test
    @DisplayName("Nodes in different components are not connected")
    void testDifferentComponents() {
        unionFind.initialize(graph);
        assertFalse(unionFind.connected(1, 4));
        assertFalse(unionFind.connected(3, 5));
    }

    @Test
    @DisplayName("Fully connected graph has one component")
    void testFullyConnected() {
        graph.addBidirectionalEdge(3, 4, 5.0, TransportMode.CAR); // bridge
        unionFind.initialize(graph);
        assertEquals(1, unionFind.getComponentCount());
        assertTrue(unionFind.connected(1, 5));
    }

    @Test
    @DisplayName("getComponent returns all nodes in same component")
    void testGetComponent() {
        unionFind.initialize(graph);
        Set<Integer> component1 = unionFind.getComponent(1);
        assertEquals(3, component1.size());
        assertTrue(component1.containsAll(Set.of(1, 2, 3)));

        Set<Integer> component2 = unionFind.getComponent(4);
        assertEquals(2, component2.size());
        assertTrue(component2.containsAll(Set.of(4, 5)));
    }

    @Test
    @DisplayName("Blocking critical bridge disconnects graph")
    void testWouldDisconnect() {
        // Make fully connected first
        graph.addBidirectionalEdge(3, 4, 5.0, TransportMode.CAR);
        unionFind.initialize(graph);

        // The 3→4 bridge is critical — blocking it disconnects the graph
        boolean disconnects = unionFind.wouldDisconnect(graph, 3, 4);
        assertTrue(disconnects, "Blocking the only bridge should disconnect the graph");
    }

    @Test
    @DisplayName("Blocking non-critical road does not disconnect graph")
    void testWouldNotDisconnect() {
        // Add redundant path 1→3
        graph.addBidirectionalEdge(1, 3, 2.0, TransportMode.CAR);
        unionFind.initialize(graph);

        // Now 1-2 is not critical (can still go 1→3)
        boolean disconnects = unionFind.wouldDisconnect(graph, 1, 2);
        assertFalse(disconnects, "Non-critical road block should not disconnect graph");
    }
}
