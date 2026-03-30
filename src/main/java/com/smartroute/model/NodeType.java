package com.smartroute.model;

/**
 * Categorizes each node in the city graph.
 * Used for multi-modal transport routing decisions.
 */
public enum NodeType {
    ROAD,           // Regular road intersection
    BUS_STOP,       // Bus stop (accessible by walking + bus)
    METRO_STATION,  // Metro station (accessible by metro + walking)
    INTERCHANGE,    // Multi-modal interchange (bus + metro + walk)
    LANDMARK        // Named landmark / destination
}
