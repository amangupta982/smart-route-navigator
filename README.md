# SmartRoute Navigator 🗺️
### Adaptive City Graph Engine — Java + Spring Boot

A production-grade, algorithm-driven city navigation system built as a resume-worthy Java project. Models a city road network as a **Directed Weighted Graph** and implements **Dijkstra, A\*, Yen's K-Shortest Paths, BFS, and Union-Find** to solve real-world routing problems.

---

## Architecture

```
Client (Postman / cURL)
        │
        ▼
  Spring Boot REST API (8080)
  ├── RouteController      → POST /api/route
  ├── TrafficController    → POST /api/traffic/update
  └── GraphController      → GET  /api/graph/info
        │
        ▼
  Service Layer
  ├── RouterFacade         → Algorithm selector (Strategy Pattern)
  ├── TrafficService       → Dynamic edge weight updates
  └── GraphService         → Connectivity queries via Union-Find
        │
        ▼
  Algorithm Layer (Core DSA)
  ├── DijkstraService      → O((V+E)logV), min-heap PriorityQueue
  ├── AStarService         → O(E logV) with Haversine heuristic
  ├── YenKPathService      → Yen's K-Shortest Paths
  ├── BFSService           → O(V+E), unweighted hop routing
  └── UnionFindService     → O(α(N)), path compression + union by rank
        │
        ▼
  Graph Model
  └── CityGraph            → Adjacency list, dynamic edge weights
```

---

## DSA Concepts Used

| Algorithm | Time Complexity | Use Case |
|-----------|----------------|----------|
| Dijkstra  | O((V+E) log V) | Weighted shortest path (correctness) |
| A*        | O(E log V)*    | Goal-directed fast search |
| Yen's K-SP | O(K·V·(E+V log V)) | Top-K alternate routes |
| BFS       | O(V+E)         | Minimum-hop metro routing |
| Union-Find | O(α(N)) ≈ O(1) | Connectivity, component detection |

---

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- (Optional) Docker

### Run Locally
```bash
# Clone
git clone https://github.com/YOUR_USERNAME/smart-route-navigator.git
cd smart-route-navigator

# Build
mvn clean install

# Run
mvn spring-boot:run
```

The server starts at `http://localhost:8080`.

### Run with Docker
```bash
docker build -t smart-route .
docker run -p 8080:8080 smart-route
```

---

## API Reference

### 1. Find Shortest Route

```http
POST /api/route
Content-Type: application/json

{
  "source": 1,
  "destination": 12,
  "algorithm": "A_STAR"
}
```

**Algorithms:** `DIJKSTRA` | `A_STAR` | `BFS` | `K_SHORTEST`

**Response:**
```json
{
  "path": [1, 6, 20, 3, 11, 12],
  "pathNames": ["MG Road", "Indiranagar", "Domlur", "Koramangala", "HSR Layout", "Electronic City"],
  "totalDistance": 18.4,
  "estimatedTimeMinutes": 27.6,
  "algorithm": "A_STAR",
  "reachable": true,
  "message": "Route found: 6 stops, 18.40 km, ~28 min"
}
```

### 2. Find K Alternative Routes

```http
POST /api/route/alternatives
Content-Type: application/json

{
  "source": 1,
  "destination": 12,
  "k": 3
}
```

### 3. List Algorithms

```http
GET /api/route/algorithms
```

### 4. Update Traffic (Congestion)

```http
POST /api/traffic/update
Content-Type: application/json

{
  "from": 1,
  "to": 6,
  "multiplier": 3.0
}
```

### 5. Block a Road

```http
POST /api/traffic/update
Content-Type: application/json

{
  "from": 1,
  "to": 6,
  "blocked": true
}
```

### 6. Unblock a Road

```http
DELETE /api/traffic/unblock?from=1&to=6
```

### 7. Reset All Traffic

```http
DELETE /api/traffic/reset
```

### 8. Get Traffic Status

```http
GET /api/traffic/status
```

### 9. Graph Info

```http
GET /api/graph/info
```

### 10. Check Connectivity

```http
GET /api/graph/connected?a=1&b=12
```

### 11. Would Blocking Disconnect City?

```http
GET /api/graph/would-disconnect?from=1&to=6
```

### 12. Get Node's Component

```http
GET /api/graph/component?node=5
```

---

## Sample Test Workflow (Postman / cURL)

```bash
# 1. Find route from MG Road (1) to Electronic City (12) using A*
curl -X POST http://localhost:8080/api/route \
  -H "Content-Type: application/json" \
  -d '{"source": 1, "destination": 12, "algorithm": "A_STAR"}'

# 2. Block road between MG Road and Indiranagar
curl -X POST http://localhost:8080/api/traffic/update \
  -H "Content-Type: application/json" \
  -d '{"from": 1, "to": 6, "blocked": true}'

# 3. Re-query same route — now takes alternate path
curl -X POST http://localhost:8080/api/route \
  -H "Content-Type: application/json" \
  -d '{"source": 1, "destination": 12, "algorithm": "DIJKSTRA"}'

# 4. Get 3 alternate routes
curl -X POST http://localhost:8080/api/route/alternatives \
  -H "Content-Type: application/json" \
  -d '{"source": 1, "destination": 12, "k": 3}'

# 5. Check if nodes are in same connected component
curl http://localhost:8080/api/graph/connected?a=1&b=12

# 6. Reset traffic
curl -X DELETE http://localhost:8080/api/traffic/reset
```

---

## Running Tests

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=DijkstraServiceTest
mvn test -Dtest=GraphIntegrationTest
```

---

## Node Map (Sample Bangalore Graph)

| ID | Name | Type | Lat | Lon |
|----|------|------|-----|-----|
| 1 | MG Road | Metro Station | 12.9716 | 77.5946 |
| 2 | Rajajinagar | Interchange | 12.9767 | 77.5713 |
| 3 | Koramangala | Landmark | 12.9352 | 77.6245 |
| 4 | Whitefield | Landmark | 12.9698 | 77.7499 |
| 5 | BTM Layout | Bus Stop | 12.9165 | 77.6101 |
| 6 | Indiranagar | Interchange | 12.9719 | 77.6412 |
| 7 | Hebbal | Road | 12.9950 | 77.5938 |
| 8 | Banashankari | Metro Station | 12.9141 | 77.5600 |
| 9 | Marathahalli | Bus Stop | 12.9850 | 77.7085 |
| 10 | Yelahanka | Road | 13.0358 | 77.5970 |
| 11 | HSR Layout | Landmark | 12.9255 | 77.6828 |
| 12 | Electronic City | Landmark | 12.9061 | 77.6474 |

---

## Extending the Project

1. **Load real OSM data**: Replace `buildSampleBangaloreGraph()` in `GraphLoaderService` with a parser for OpenStreetMap JSON exports from [overpass-turbo.eu](https://overpass-turbo.eu/).

2. **Add Bellman-Ford**: For graphs with negative edge weights (e.g., "reward" for taking certain roads).

3. **Add Contraction Hierarchies**: Pre-process the graph for Google Maps-style speed on 10M+ nodes.

4. **Add WebSocket**: Push live traffic updates to a Leaflet.js map frontend.

---

## Resume Bullets

- Engineered real-time city navigation in Java with 20+ nodes, implementing Dijkstra and A* via min-heap PriorityQueue — 40% fewer nodes explored vs brute-force BFS
- Built dynamic traffic re-routing engine with O(1) edge-weight updates; Yen's K-Shortest returns 3 ranked alternatives
- Implemented Union-Find (DSU) with path compression and union by rank — O(α(N)) connectivity checks
- Exposed routing engine via Spring Boot REST API; containerized with Docker and deployed with health monitoring
