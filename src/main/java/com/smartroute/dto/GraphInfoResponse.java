package com.smartroute.dto;

import java.util.List;
import java.util.Map;

/**
 * Response body for /api/graph/info — returns current graph state.
 */
public class GraphInfoResponse {

    private int totalNodes;
    private int totalEdges;
    private int connectedComponents;
    private boolean isFullyConnected;
    private List<NodeInfo> nodes;

    public GraphInfoResponse(int totalNodes, int totalEdges, int connectedComponents,
                              boolean isFullyConnected, List<NodeInfo> nodes) {
        this.totalNodes = totalNodes;
        this.totalEdges = totalEdges;
        this.connectedComponents = connectedComponents;
        this.isFullyConnected = isFullyConnected;
        this.nodes = nodes;
    }

    public int getTotalNodes() { return totalNodes; }
    public int getTotalEdges() { return totalEdges; }
    public int getConnectedComponents() { return connectedComponents; }
    public boolean isFullyConnected() { return isFullyConnected; }
    public List<NodeInfo> getNodes() { return nodes; }

    public static class NodeInfo {
        public final int id;
        public final String name;
        public final String type;
        public final double latitude;
        public final double longitude;
        public final int degree;

        public NodeInfo(int id, String name, String type, double lat, double lon, int degree) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.latitude = lat;
            this.longitude = lon;
            this.degree = degree;
        }
    }
}
