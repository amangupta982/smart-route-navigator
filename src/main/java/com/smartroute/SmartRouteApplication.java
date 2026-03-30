package com.smartroute;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SmartRoute Navigator — Adaptive City Graph Routing Engine
 *
 * Entry point for the Spring Boot application.
 * Implements Dijkstra, A*, Yen's K-Shortest Paths, Union-Find,
 * and dynamic traffic re-weighting on a city road graph.
 */
@SpringBootApplication
public class SmartRouteApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartRouteApplication.class, args);
    }
}
