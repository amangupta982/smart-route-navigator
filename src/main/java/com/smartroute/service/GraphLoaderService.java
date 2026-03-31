package com.smartroute.service;

import com.smartroute.model.*;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

/**
 * GraphLoaderService — Builds and manages the singleton CityGraph instance.
 *
 * On startup (@PostConstruct), loads a sample Bangalore-inspired city graph
 * with 20 nodes covering road, bus, and metro transport modes.
 *
 * In production: extend loadFromJson() to parse OpenStreetMap data.
 */
@Service
public class GraphLoaderService {

    private CityGraph graph;

    @PostConstruct
    public void init() {
        graph = buildSampleBangaloreGraph();
    }

    public CityGraph getGraph() {
        return graph;
    }

    /**
     * Builds a 20-node sample city graph inspired by Bangalore's layout.
     * Coordinates are approximate GPS positions.
     *
     * Nodes: Intersections, bus stops, metro stations, landmarks
     * Edges: Roads (CAR), bus routes (BUS), metro lines (METRO), walking (WALK)
     */
    private CityGraph buildSampleBangaloreGraph() {
        CityGraph g = new CityGraph();

        // ── Nodes (id, latitude, longitude, name, type) ──────────────────
        g.addNode(new Node(1,  12.9716, 77.5946, "MG Road",            NodeType.METRO_STATION));
        g.addNode(new Node(2,  12.9767, 77.5713, "Rajajinagar",        NodeType.INTERCHANGE));
        g.addNode(new Node(3,  12.9352, 77.6245, "Koramangala",        NodeType.LANDMARK));
        g.addNode(new Node(4,  12.9698, 77.7499, "Whitefield",         NodeType.LANDMARK));
        g.addNode(new Node(5,  12.9165, 77.6101, "BTM Layout",         NodeType.BUS_STOP));
        g.addNode(new Node(6,  12.9719, 77.6412, "Indiranagar",        NodeType.INTERCHANGE));
        g.addNode(new Node(7,  12.9950, 77.5938, "Hebbal",             NodeType.ROAD));
        g.addNode(new Node(8,  12.9141, 77.5600, "Banashankari",       NodeType.METRO_STATION));
        g.addNode(new Node(9,  12.9850, 77.7085, "Marathahalli",       NodeType.BUS_STOP));
        g.addNode(new Node(10, 13.0358, 77.5970, "Yelahanka",          NodeType.ROAD));
        g.addNode(new Node(11, 12.9255, 77.6828, "HSR Layout",         NodeType.LANDMARK));
        g.addNode(new Node(12, 12.9061, 77.6474, "Electronic City",    NodeType.LANDMARK));
        g.addNode(new Node(13, 12.9784, 77.6408, "Old Airport Road",   NodeType.ROAD));
        g.addNode(new Node(14, 13.0148, 77.5603, "Yeshwantpur",        NodeType.INTERCHANGE));
        g.addNode(new Node(15, 12.9542, 77.4908, "Kengeri",            NodeType.METRO_STATION));
        g.addNode(new Node(16, 12.9279, 77.5668, "Jayanagar",          NodeType.BUS_STOP));
        g.addNode(new Node(17, 12.9902, 77.6501, "CV Raman Nagar",     NodeType.ROAD));
        g.addNode(new Node(18, 13.0068, 77.5442, "Peenya",             NodeType.ROAD));
        g.addNode(new Node(19, 12.9633, 77.6059, "Richmond Circle",    NodeType.ROAD));
        g.addNode(new Node(20, 12.9581, 77.6471, "Domlur",             NodeType.ROAD));

        // ── Road edges (CAR, bidirectional) ──────────────────────────────
        g.addBidirectionalEdge(1,  2,  5.2, TransportMode.CAR);
        g.addBidirectionalEdge(1,  6,  3.8, TransportMode.CAR);
        g.addBidirectionalEdge(1,  19, 2.1, TransportMode.CAR);
        g.addBidirectionalEdge(2,  7,  4.5, TransportMode.CAR);
        g.addBidirectionalEdge(2,  14, 3.0, TransportMode.CAR);
        g.addBidirectionalEdge(2,  18, 6.1, TransportMode.CAR);
        g.addBidirectionalEdge(3,  5,  3.4, TransportMode.CAR);
        g.addBidirectionalEdge(3,  11, 5.6, TransportMode.CAR);
        g.addBidirectionalEdge(3,  20, 4.2, TransportMode.CAR);
        g.addBidirectionalEdge(4,  9,  7.1, TransportMode.CAR);
        g.addBidirectionalEdge(4,  17, 8.3, TransportMode.CAR);
        g.addBidirectionalEdge(5,  8,  4.9, TransportMode.CAR);
        g.addBidirectionalEdge(5,  16, 3.7, TransportMode.CAR);
        g.addBidirectionalEdge(6,  13, 2.5, TransportMode.CAR);
        g.addBidirectionalEdge(6,  20, 2.9, TransportMode.CAR);
        g.addBidirectionalEdge(7,  10, 6.8, TransportMode.CAR);
        g.addBidirectionalEdge(8,  15, 9.2, TransportMode.CAR);
        g.addBidirectionalEdge(8,  16, 2.3, TransportMode.CAR);
        g.addBidirectionalEdge(9,  11, 8.4, TransportMode.CAR);
        g.addBidirectionalEdge(9,  17, 4.1, TransportMode.CAR);
        g.addBidirectionalEdge(10, 14, 5.5, TransportMode.CAR);
        g.addBidirectionalEdge(11, 12, 6.2, TransportMode.CAR);
        g.addBidirectionalEdge(13, 17, 3.3, TransportMode.CAR);
        g.addBidirectionalEdge(14, 18, 4.4, TransportMode.CAR);
        g.addBidirectionalEdge(16, 19, 3.6, TransportMode.CAR);
        g.addBidirectionalEdge(19, 20, 2.0, TransportMode.CAR);

        // ── Metro edges (METRO, faster but fixed routes) ──────────────────
        // Purple Line: 15 → 8 → 1 → 2 (West to East)
        g.addBidirectionalEdge(15, 8,  4.5, TransportMode.METRO);
        g.addBidirectionalEdge(8,  1,  6.8, TransportMode.METRO);
        g.addBidirectionalEdge(1,  2,  5.2, TransportMode.METRO);

        // Green Line: 14 → 2 → 7 → 10 (South to North)
        g.addBidirectionalEdge(14, 2,  3.0, TransportMode.METRO);
        g.addBidirectionalEdge(2,  7,  4.5, TransportMode.METRO);
        g.addBidirectionalEdge(7,  10, 6.8, TransportMode.METRO);

        // ── Bus edges (BUS) ───────────────────────────────────────────────
        g.addBidirectionalEdge(5,  3,  3.4, TransportMode.BUS);
        g.addBidirectionalEdge(3,  6,  5.1, TransportMode.BUS);
        g.addBidirectionalEdge(6,  4,  11.2, TransportMode.BUS);
        g.addBidirectionalEdge(5,  12, 8.7, TransportMode.BUS);
        g.addBidirectionalEdge(16, 8,  2.3, TransportMode.BUS);
        g.addBidirectionalEdge(18, 14, 4.4, TransportMode.BUS);
        g.addBidirectionalEdge(9,  4,  7.1, TransportMode.BUS);
        g.addBidirectionalEdge(13, 9,  9.5, TransportMode.BUS);
        g.addBidirectionalEdge(11, 5,  5.6, TransportMode.BUS);
        g.addBidirectionalEdge(20, 11, 6.4, TransportMode.BUS);

        // ── Walking edges (WALK, short distances only) ────────────────────
        g.addBidirectionalEdge(1,  20, 1.2, TransportMode.WALK);
        g.addBidirectionalEdge(19, 1,  2.1, TransportMode.WALK);
        g.addBidirectionalEdge(6,  13, 0.8, TransportMode.WALK);
        g.addBidirectionalEdge(16, 5,  1.5, TransportMode.WALK);
        g.addBidirectionalEdge(8,  16, 0.9, TransportMode.WALK);
        g.addBidirectionalEdge(20, 3,  2.0, TransportMode.WALK);

        return g;
    }
}
