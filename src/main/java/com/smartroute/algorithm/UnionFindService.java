package com.smartroute.algorithm;

import com.smartroute.model.CityGraph;
import com.smartroute.model.Edge;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Union-Find (Disjoint Set Union — DSU) Data Structure
 *
 * DSA Concepts:
 *   - Forest of trees, each tree = one connected component
 *   - Path Compression: flattens the tree on find() → amortized O(α(N)) ≈ O(1)
 *   - Union by Rank: attaches smaller tree under larger → O(log N) without compression
 *   - Combined: nearly O(1) per operation — near-constant time
 *
 * Complexity:
 *   Time per operation: O(α(N)) — Inverse Ackermann, practically constant
 *   Space: O(N) for parent[] and rank[] arrays
 *
 * Use cases in SmartRoute:
 *   - Check if two nodes are in the same connected component
 *   - Detect if road blockage splits the city into disconnected sectors
 *   - Build Minimum Spanning Tree (Kruskal's algorithm)
 *   - Count number of distinct road networks
 */
@Component
public class UnionFindService {

    private int[] parent;
    private int[] rank;
    private int componentCount;
    private Map<Integer, Integer> nodeToIndex;
    private Map<Integer, Integer> indexToNode;

    /**
     * Builds a Union-Find structure for the given city graph.
     * Initially each node is its own component.
     */
    public void initialize(CityGraph graph) {
        List<Integer> nodeIds = new ArrayList<>(graph.getAllNodeIds());
        int n = nodeIds.size();

        parent = new int[n];
        rank = new int[n];
        nodeToIndex = new HashMap<>();
        indexToNode = new HashMap<>();
        componentCount = n;

        for (int i = 0; i < n; i++) {
            parent[i] = i;   // each node is its own root
            rank[i] = 0;
            nodeToIndex.put(nodeIds.get(i), i);
            indexToNode.put(i, nodeIds.get(i));
        }

        // Union all connected nodes (based on non-blocked edges)
        for (Integer nodeId : nodeIds) {
            for (Edge edge : graph.getNeighbors(nodeId)) {
                if (!edge.isBlocked()) {
                    union(nodeId, edge.getTo());
                }
            }
        }
    }

    /**
     * Finds root of the component containing nodeId.
     * Path Compression: makes every node on the path point directly to root.
     * Amortized O(α(N)).
     */
    public int find(int nodeId) {
        Integer idx = nodeToIndex.get(nodeId);
        if (idx == null) throw new IllegalArgumentException("Node not in DSU: " + nodeId);
        return indexToNode.get(findByIndex(idx));
    }

    private int findByIndex(int i) {
        if (parent[i] != i) {
            parent[i] = findByIndex(parent[i]); // Path compression
        }
        return parent[i];
    }

    /**
     * Merges the components containing nodeA and nodeB.
     * Union by Rank: smaller rank tree goes under larger rank tree.
     */
    public void union(int nodeA, int nodeB) {
        Integer idxA = nodeToIndex.get(nodeA);
        Integer idxB = nodeToIndex.get(nodeB);
        if (idxA == null || idxB == null) return;

        int rootA = findByIndex(idxA);
        int rootB = findByIndex(idxB);

        if (rootA == rootB) return; // Already connected

        // Union by rank
        if (rank[rootA] < rank[rootB]) {
            parent[rootA] = rootB;
        } else if (rank[rootA] > rank[rootB]) {
            parent[rootB] = rootA;
        } else {
            parent[rootB] = rootA;
            rank[rootA]++;
        }
        componentCount--;
    }

    /**
     * Returns true if nodeA and nodeB are in the same connected component.
     * O(α(N)) ≈ O(1)
     */
    public boolean connected(int nodeA, int nodeB) {
        Integer idxA = nodeToIndex.get(nodeA);
        Integer idxB = nodeToIndex.get(nodeB);
        if (idxA == null || idxB == null) return false;
        return findByIndex(idxA) == findByIndex(idxB);
    }

    /**
     * Returns total number of distinct connected components.
     */
    public int getComponentCount() {
        return componentCount;
    }

    /**
     * Returns all nodes in the same component as the given node.
     */
    public Set<Integer> getComponent(int nodeId) {
        Integer rootIdx = nodeToIndex.get(nodeId);
        if (rootIdx == null) return Collections.emptySet();
        int root = findByIndex(rootIdx);

        Set<Integer> component = new HashSet<>();
        for (Map.Entry<Integer, Integer> entry : nodeToIndex.entrySet()) {
            if (findByIndex(entry.getValue()) == root) {
                component.add(entry.getKey());
            }
        }
        return component;
    }

    /**
     * Checks if blocking a road would disconnect the city.
     * Returns true if the city would become disconnected.
     */
    public boolean wouldDisconnect(CityGraph graph, int from, int to) {
        // Temporarily block the road and rebuild DSU
        graph.blockRoad(from, to);
        initialize(graph);
        boolean disconnected = componentCount > 1;
        graph.unblockRoad(from, to);
        initialize(graph);
        return disconnected;
    }
}
