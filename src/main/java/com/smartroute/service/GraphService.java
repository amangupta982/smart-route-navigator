package com.smartroute.service;

import com.smartroute.algorithm.UnionFindService;
import com.smartroute.dto.GraphInfoResponse;
import com.smartroute.model.CityGraph;
import com.smartroute.model.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * GraphService — Provides graph metadata, analytics, and connectivity queries.
 *
 * Wraps CityGraph and UnionFindService to answer questions like:
 *   - How many connected components are in the city?
 *   - Are nodes A and B in the same sector?
 *   - Would blocking this road disconnect the city?
 *   - What does the full graph look like?
 */
@Service
public class GraphService {

    @Autowired private GraphLoaderService graphLoader;
    @Autowired private UnionFindService unionFind;

    /**
     * Returns complete graph info including nodes, edge count, and connectivity.
     */
    public GraphInfoResponse getGraphInfo() {
        CityGraph graph = graphLoader.getGraph();
        unionFind.initialize(graph);

        List<GraphInfoResponse.NodeInfo> nodeInfos = graph.getAllNodes().stream()
                .map(node -> new GraphInfoResponse.NodeInfo(
                        node.getId(),
                        node.getName(),
                        node.getType().name(),
                        node.getLatitude(),
                        node.getLongitude(),
                        graph.getNeighbors(node.getId()).size()
                ))
                .sorted((a, b) -> Integer.compare(a.id, b.id))
                .collect(Collectors.toList());

        int components = unionFind.getComponentCount();
        return new GraphInfoResponse(
                graph.getNodeCount(),
                graph.getEdgeCount(),
                components,
                components == 1,
                nodeInfos
        );
    }

    /**
     * Checks if two nodes are in the same connected component.
     */
    public boolean areConnected(int nodeA, int nodeB) {
        CityGraph graph = graphLoader.getGraph();
        unionFind.initialize(graph);
        return unionFind.connected(nodeA, nodeB);
    }

    /**
     * Checks if blocking a road would disconnect any part of the city.
     */
    public boolean wouldBlockDisconnectCity(int from, int to) {
        return unionFind.wouldDisconnect(graphLoader.getGraph(), from, to);
    }

    /**
     * Returns all node IDs in the same connected component as the given node.
     */
    public java.util.Set<Integer> getComponentOf(int nodeId) {
        CityGraph graph = graphLoader.getGraph();
        unionFind.initialize(graph);
        return unionFind.getComponent(nodeId);
    }
}
