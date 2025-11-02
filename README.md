# README.md

## Table of Contents

- [Introduction](#introduction)
- [Theory](#theory)
- [Datasets](#datasets)
- [Results](#results)
    - [SCC Results](#scc-results)
    - [Topological Sort Results](#topological-sort-results)
    - [DAG Shortest Path Results](#dag-shortest-path-results)
    - [Summary Results](#summary-results)
- [Analysis](#analysis)
    - [SCC Analysis](#scc-analysis)
    - [Topological Sort Analysis](#topological-sort-analysis)
    - [DAG-SP Analysis](#dag-sp-analysis)
- [Comparison: Theory vs. Practice](#comparison-theory-vs-practice)
- [Conclusions](#conclusions)

## Introduction

I've implemented three key graph algorithms: Kosaraju's algorithm for finding strongly connected components (SCCs), topological sort for directed acyclic graphs (DAGs), and single-source shortest paths in DAGs. These are based on standard techniques from graph theory, and I ran experiments on various graph structures to see how they perform in practice compared to their theoretical bounds.

The implementations are in Java, using adjacency lists for graph representation since most graphs I tested are sparse or moderately dense. I focused on measuring operations count (like DFS visits or relaxations) and execution time in nanoseconds to get a sense of efficiency. Note that the code also includes a longest path variant for DAGs, which is symmetric to shortest paths but maximizes sums; however, the provided data focuses on shortest paths.

Reference: Cormen, T. H., Leiserson, C. E., Rivest, R. L., & Stein, C. (2009). Introduction to Algorithms (3rd ed.), Chapter 22.

## Theory

### Topological Sort

Topological sort orders the vertices of a DAG such that for every directed edge (u, v), u comes before v in the ordering. It's useful for scheduling tasks with dependencies.

The algorithm uses depth-first search (DFS): perform DFS on the graph, and as each vertex finishes (all its descendants are visited), insert it at the front of a list. The resulting list is the topological order in reverse finishing times.

Time complexity: Θ(V + E), since it's basically DFS.

In practice, this works well for DAGs, but fails if there's a cycle (back edge detected).

### Strongly Connected Components (Kosaraju's Algorithm)

SCCs are maximal sets of vertices where every pair is mutually reachable. Kosaraju's algorithm finds them in directed graphs.

It runs two DFS passes:
1. DFS on the original graph G to compute finishing times.
2. Transpose the graph (reverse edges) to get G^T.
3. DFS on G^T, processing vertices in decreasing order of finishing times from step 1. Each DFS tree is an SCC.

Time complexity: Θ(V + E), from two DFS calls plus transpose.

This leverages the property that SCCs form a DAG when contracted, and the transpose preserves SCCs but reverses the meta-graph order.

### Single-Source Shortest Paths in DAGs

For DAGs, we can compute shortest paths from a source s in linear time, even with negative weights (no cycles anyway).

Algorithm:
1. Topologically sort the vertices.
2. Initialize: s.d = 0, others ∞.
3. Relax edges in topological order: for each vertex u in order, relax all outgoing edges (v.d = min(v.d, u.d + w(u,v))).

Time complexity: Θ(V + E).

This is efficient because topo order ensures we process predecessors before dependents, so one pass suffices.

Note: The implementation uses node durations as weights, summing them along paths (including source and destination). This is adaptable to edge weights by shifting.

A longest path variant is also implemented, which maximizes path sums similarly, useful for critical path analysis in scheduling.

### Task Order Derivation

For graphs with cycles, after finding SCCs, we can condense the graph into a DAG of components and apply topological sort to derive a partial order respecting dependencies across components.

## Datasets

I generated 18 graphs with varying sizes, densities, and structures to test the algorithms. Density is "sparse" (E ≈ V) or "dense" (E closer to V^2/2 for directed). Variants:
- pure_dag: Acyclic graph, no cycles.
- one_cycle: Has one cycle, so cyclic.
- two_cycles: Has two disjoint cycles.
- multiple_sccs: Multiple SCCs, possibly with cycles inside.

All are directed graphs. Source is a random vertex. I used node weights (durations) instead of edge weights because in scenarios like task graphs or pipelines, the cost is often on nodes (e.g., processing time per task), and edges just show dependencies. Path cost sums node durations. This fits well with scheduling applications, and it's easy to adapt standard algorithms by treating node weights as edge weights incoming to the node.

In task scheduling graphs (like those for project management, build pipelines, or dependency resolution), the "cost" or time is typically inherent to the tasks themselves (nodes), representing execution duration, while edges merely indicate precedence or dependencies without their own weights. Using node durations models this real-world scenario more intuitively: the total path cost becomes the sum of task times in a sequence, directly computing things like minimum completion time or critical paths. This avoids artificially assigning weights to edges, simplifies graph construction for such applications, and is algorithmically equivalent (e.g., by conceptually shifting a node's duration to its incoming edges during relaxation). Standard edge-weighted approaches work better for networks like roads or data flows where costs are on connections, but node weights fit dependency-focused domains like PERT/CPM or workflow orchestration.

Brief descriptions:

1. 6 vertices, 7 edges, sparse, pure_dag, source 0: Small DAG.
2. 8 vertices, 17 edges, sparse, one_cycle, source 4: Small with one cycle.
3. 10 vertices, 28 edges, sparse, two_cycles, source 1: Medium with two cycles.
4. 6 vertices, 12 edges, dense, pure_dag, source 0: Small dense DAG.
5. 8 vertices, 28 edges, dense, one_cycle, source 2: Dense with one cycle.
6. 10 vertices, 42 edges, dense, two_cycles, source 4: Medium dense with two cycles.
7. 14 vertices, 36 edges, sparse, one_cycle, source 0: Larger sparse with one cycle.
8. 16 vertices, 38 edges, sparse, two_cycles, source 5: Sparse with two cycles.
9. 20 vertices, 43 edges, sparse, multiple_sccs, source 2: Sparse with multiple SCCs.
10. 14 vertices, 94 edges, dense, one_cycle, source 4: Dense with one cycle.
11. 16 vertices, 113 edges, dense, two_cycles, source 6: Dense with two cycles.
12. 20 vertices, 115 edges, dense, multiple_sccs, source 2: Dense with multiple SCCs.
13. 30 vertices, 147 edges, sparse, pure_dag, source 0: Larger sparse DAG.
14. 40 vertices, 109 edges, sparse, multiple_sccs, source 12: Sparse with multiple SCCs.
15. 50 vertices, 461 edges, sparse, two_cycles, source 14: Large sparse with two cycles.
16. 30 vertices, 322 edges, dense, pure_dag, source 0: Dense DAG.
17. 40 vertices, 397 edges, dense, multiple_sccs, source 5: Dense with multiple SCCs.
18. 50 vertices, 1307 edges, dense, two_cycles, source 25: Large dense with two cycles.

## Results

### SCC Results

| graph_id | vertices | edges | density | variant | source | weight_model | num_sccs | operations_count | execution_time_ns |
|----------|----------|-------|---------|---------|--------|--------------|----------|------------------|-------------------|
| 1 | 6 | 7 | sparse | pure_dag | 0 | node | 6 | 26 | 18700 |
| 2 | 8 | 17 | sparse | one_cycle | 4 | node | 2 | 50 | 15700 |
| 3 | 10 | 28 | sparse | two_cycles | 1 | node | 1 | 76 | 19700 |
| 4 | 6 | 12 | dense | pure_dag | 0 | node | 6 | 36 | 16100 |
| 5 | 8 | 28 | dense | one_cycle | 2 | node | 1 | 72 | 18300 |
| 6 | 10 | 42 | dense | two_cycles | 4 | node | 1 | 104 | 22800 |
| 7 | 14 | 36 | sparse | one_cycle | 0 | node | 2 | 100 | 29000 |
| 8 | 16 | 38 | sparse | two_cycles | 5 | node | 2 | 108 | 52900 |
| 9 | 20 | 43 | sparse | multiple_sccs | 2 | node | 3 | 126 | 37400 |
| 10 | 14 | 94 | dense | one_cycle | 4 | node | 1 | 216 | 27800 |
| 11 | 16 | 113 | dense | two_cycles | 6 | node | 1 | 258 | 32400 |
| 12 | 20 | 115 | dense | multiple_sccs | 2 | node | 3 | 270 | 31700 |
| 13 | 30 | 147 | sparse | pure_dag | 0 | node | 30 | 354 | 105500 |
| 14 | 40 | 109 | sparse | multiple_sccs | 12 | node | 2 | 298 | 144100 |
| 15 | 50 | 461 | sparse | two_cycles | 14 | node | 1 | 1022 | 100600 |
| 16 | 30 | 322 | dense | pure_dag | 0 | node | 30 | 704 | 218700 |
| 17 | 40 | 397 | dense | multiple_sccs | 5 | node | 4 | 874 | 163000 |
| 18 | 50 | 1307 | dense | two_cycles | 25 | node | 1 | 2714 | 599000 |

### Topological Sort Results

| graph_id | vertices | edges | density | variant | source | weight_model | operations_count | execution_time_ns |
|----------|----------|-------|---------|---------|--------|--------------|------------------|-------------------|
| 1 | 6 | 7 | sparse | pure_dag | 0 | node | 13 | 7400 |
| 2 | 8 | 17 | sparse | one_cycle | 4 | node | 25 | 6600 |
| 3 | 10 | 28 | sparse | two_cycles | 1 | node | 38 | 6800 |
| 4 | 6 | 12 | dense | pure_dag | 0 | node | 18 | 7600 |
| 5 | 8 | 28 | dense | one_cycle | 2 | node | 36 | 6100 |
| 6 | 10 | 42 | dense | two_cycles | 4 | node | 52 | 7800 |
| 7 | 14 | 36 | sparse | one_cycle | 0 | node | 50 | 10800 |
| 8 | 16 | 38 | sparse | two_cycles | 5 | node | 54 | 9600 |
| 9 | 20 | 43 | sparse | multiple_sccs | 2 | node | 63 | 9000 |
| 10 | 14 | 94 | dense | one_cycle | 4 | node | 108 | 13900 |
| 11 | 16 | 113 | dense | two_cycles | 6 | node | 129 | 8800 |
| 12 | 20 | 115 | dense | multiple_sccs | 2 | node | 135 | 19600 |
| 13 | 30 | 147 | sparse | pure_dag | 0 | node | 177 | 35300 |
| 14 | 40 | 109 | sparse | multiple_sccs | 12 | node | 149 | 33600 |
| 15 | 50 | 461 | sparse | two_cycles | 14 | node | 511 | 65800 |
| 16 | 30 | 322 | dense | pure_dag | 0 | node | 352 | 52200 |
| 17 | 40 | 397 | dense | multiple_sccs | 5 | node | 437 | 53700 |
| 18 | 50 | 1307 | dense | two_cycles | 25 | node | 1357 | 167200 |

### DAG Shortest Path Results

| graph_id | vertices | edges | density | variant | source | weight_model | operations_count | execution_time_ns |
|----------|----------|-------|---------|---------|--------|--------------|------------------|-------------------|
| 1 | 6 | 7 | sparse | pure_dag | 0 | node | 20 | 24300 |
| 2 | 8 | 17 | sparse | one_cycle | 4 | node | 0 | 300 |
| 3 | 10 | 28 | sparse | two_cycles | 1 | node | 0 | 200 |
| 4 | 6 | 12 | dense | pure_dag | 0 | node | 30 | 29100 |
| 5 | 8 | 28 | dense | one_cycle | 2 | node | 0 | 200 |
| 6 | 10 | 42 | dense | two_cycles | 4 | node | 0 | 200 |
| 7 | 14 | 36 | sparse | one_cycle | 0 | node | 0 | 300 |
| 8 | 16 | 38 | sparse | two_cycles | 5 | node | 0 | 200 |
| 9 | 20 | 43 | sparse | multiple_sccs | 2 | node | 0 | 200 |
| 10 | 14 | 94 | dense | one_cycle | 4 | node | 0 | 200 |
| 11 | 16 | 113 | dense | two_cycles | 6 | node | 0 | 200 |
| 12 | 20 | 115 | dense | multiple_sccs | 2 | node | 0 | 200 |
| 13 | 30 | 147 | sparse | pure_dag | 0 | node | 324 | 118700 |
| 14 | 40 | 109 | sparse | multiple_sccs | 12 | node | 0 | 200 |
| 15 | 50 | 461 | sparse | two_cycles | 14 | node | 0 | 200 |
| 16 | 30 | 322 | dense | pure_dag | 0 | node | 674 | 384700 |
| 17 | 40 | 397 | dense | multiple_sccs | 5 | node | 0 | 200 |
| 18 | 50 | 1307 | dense | two_cycles | 25 | node | 0 | 200 |

### DAG Longest Path Results

Note: DAG longest-path (critical path) results were added. The `critical_path` column contains a bracketed list of node IDs like "[0,1,2,3]" and `node_durations` lists the corresponding per-node durations; these values are written to `data/longest_path_results.csv` and are included in the summary totals.

|graph_id|vertices|edges|density|variant      |source|weight_model|critical_path_length|critical_path                                             |node_durations                                |operations_count|execution_time_ns|
|--------|--------|-----|-------|-------------|------|------------|--------------------|----------------------------------------------------------|----------------------------------------------|----------------|-----------------|
|1       |6       |7    |sparse |pure_dag     |0     |node        |8                   |[0,1,2]                                                   |[2,1,5]                                       |20              |37500            |
|2       |8       |17   |sparse |one_cycle    |4     |node        |0                   |[]                                                        |[]                                            |0               |200              |
|3       |10      |28   |sparse |two_cycles   |1     |node        |0                   |[]                                                        |[]                                            |0               |200              |
|4       |6       |12   |dense  |pure_dag     |0     |node        |41                  |[0,1,2,3,4]                                               |[7,7,10,8,9]                                  |30              |24100            |
|5       |8       |28   |dense  |one_cycle    |2     |node        |0                   |[]                                                        |[]                                            |0               |200              |
|6       |10      |42   |dense  |two_cycles   |4     |node        |0                   |[]                                                        |[]                                            |0               |200              |
|7       |14      |36   |sparse |one_cycle    |0     |node        |0                   |[]                                                        |[]                                            |0               |200              |
|8       |16      |38   |sparse |two_cycles   |5     |node        |0                   |[]                                                        |[]                                            |0               |200              |
|9       |20      |43   |sparse |multiple_sccs|2     |node        |0                   |[]                                                        |[]                                            |0               |200              |
|10      |14      |94   |dense  |one_cycle    |4     |node        |0                   |[]                                                        |[]                                            |0               |100              |
|11      |16      |113  |dense  |two_cycles   |6     |node        |0                   |[]                                                        |[]                                            |0               |100              |
|12      |20      |115  |dense  |multiple_sccs|2     |node        |0                   |[]                                                        |[]                                            |0               |100              |
|13      |30      |147  |sparse |pure_dag     |0     |node        |87                  |[0,2,3,4,6,8,10,12,13,14,17,18,20,23,26,28]               |[9,9,2,5,3,8,3,2,4,8,7,1,9,7,4,6]             |324             |113600           |
|14      |40      |109  |sparse |multiple_sccs|12    |node        |0                   |[]                                                        |[]                                            |0               |200              |
|15      |50      |461  |sparse |two_cycles   |14    |node        |0                   |[]                                                        |[]                                            |0               |200              |
|16      |30      |322  |dense  |pure_dag     |0     |node        |92                  |[0,2,3,4,5,6,7,8,9,11,12,13,15,16,17,19,20,21,22,24,27,28]|[4,8,2,3,2,1,1,4,3,7,6,2,10,5,2,2,4,8,3,6,8,1]|674             |199900           |
|17      |40      |397  |dense  |multiple_sccs|5     |node        |0                   |[]                                                        |[]                                            |0               |200              |
|18      |50      |1307 |dense  |two_cycles   |25    |node        |0                   |[]                                                        |[]                                            |0               |100              |


### Summary Results

|graph_id|vertices|edges|density|variant      |source|weight_model|total_operations_count|total_execution_time_ns|
|--------|--------|-----|-------|-------------|------|------------|----------------------|-----------------------|
|1       |6       |7    |sparse |pure_dag     |0     |node        |59                    |50400                  |
|2       |8       |17   |sparse |one_cycle    |4     |node        |75                    |22600                  |
|3       |10      |28   |sparse |two_cycles   |1     |node        |114                   |26700                  |
|4       |6       |12   |dense  |pure_dag     |0     |node        |84                    |52800                  |
|5       |8       |28   |dense  |one_cycle    |2     |node        |108                   |24600                  |
|6       |10      |42   |dense  |two_cycles   |4     |node        |156                   |30800                  |
|7       |14      |36   |sparse |one_cycle    |0     |node        |150                   |40100                  |
|8       |16      |38   |sparse |two_cycles   |5     |node        |162                   |62700                  |
|9       |20      |43   |sparse |multiple_sccs|2     |node        |189                   |46600                  |
|10      |14      |94   |dense  |one_cycle    |4     |node        |324                   |41900                  |
|11      |16      |113  |dense  |two_cycles   |6     |node        |387                   |41400                  |
|12      |20      |115  |dense  |multiple_sccs|2     |node        |405                   |51500                  |
|13      |30      |147  |sparse |pure_dag     |0     |node        |855                   |259500                 |
|14      |40      |109  |sparse |multiple_sccs|12    |node        |447                   |177900                 |
|15      |50      |461  |sparse |two_cycles   |14    |node        |1533                  |166600                 |
|16      |30      |322  |dense  |pure_dag     |0     |node        |1730                  |655600                 |
|17      |40      |397  |dense  |multiple_sccs|5     |node        |1311                  |216900                 |
|18      |50      |1307 |dense  |two_cycles   |25    |node        |4071                  |766400                 |


## Analysis

### SCC Analysis

Bottlenecks: The two DFS traversals and graph transpose. Transpose is O(V+E), but DFS scales with graph size.

Effect of structure:
- Density: Dense graphs (e.g., 18: 2714 ops, 599000 ns) take more time than sparse (15: 1022 ops, 100600 ns), due to more edges in adjacency lists.
- SCC sizes: More SCCs (13: 30 SCCs, 105500 ns) vs few/large (18: 1 SCC, higher time) – large SCCs mean deeper DFS trees.
- Cycles: Variants with cycles (one_cycle, two_cycles) often have fewer SCCs if cycles merge components, but operations roughly linear in E.

Theory vs practice: Θ(V+E) holds; times grow with V+E, but dense ones show cache effects or list traversal overhead.

### Topological Sort Analysis

Bottlenecks: Single DFS, finishing time list building.

Effect of structure:
- Density: Dense higher ops/time (18: 1357 ops, 167200 ns vs 15: 511 ops, 65800 ns).
- Cycles: Runs on cyclic graphs but doesn't guarantee order; used anyway for comparison. Multiple_sccs or cycles don't drastically change, as it's still DFS.
- In pure_dag, smooth; in cyclic, back edges detected but algorithm proceeds.

Theory vs practice: Ops ≈ 2E + V (visits), times low, matches linear.

### DAG-SP Analysis

Bottlenecks: Topological sort + one-pass relaxation. Relaxation is O(E).

Effect of structure:
- Only meaningful for DAGs; on cyclic graphs, ops=0 because the code (in TasksReportGenerator.java) checks the graph's metadata for "is_dag" before running DagShortestPath.shortestPath. If false, it skips, leaving operations_count at 0 and no paths computed.
- Density: When run (e.g., graph 1: sparse pure_dag, 20 ops; graph 16: dense pure_dag, 674 ops), dense DAGs incur higher relaxation costs due to more edges.
- SCCs/cycles: Effect is binary—runs only if acyclic. For graphs labeled pure_dag but with cycles (e.g., some multiple_sccs or cyclic variants), skipped. Cycles prevent topo order validity, aligning with theory.
- In data, graphs 1,4,13,16 ran fully (e.g., graph 16: 674 ops, 384700 ns); others 0 ops, minimal time (~200-300 ns, likely init overhead).

Theory vs practice: Θ(V + E) when executed; practice shows skips for non-DAGs, preventing errors. Times reflect linear efficiency when run, with dense cases (e.g., graph 16) showing higher times due to E dominance.

## Comparison: Theory vs. Practice

Theoretical Comparison (Based on Cormen et al., 2009)

The theoretical efficiency of the algorithms depends on the number of vertices (V) and edges (E), as well as the data structures used (adjacency lists here).

Kosaraju's SCC Algorithm

    Dominant cost: Two DFS traversals + transpose → Θ(V + E)
    DFS visits each vertex and edge once per pass.
    Total runtime: Θ(V + E)

Topological Sort (DFS-based)

    Dominant cost: Single DFS traversal → Θ(V + E)
    Records finishing times during post-order.
    Total runtime: Θ(V + E)

DAG Single-Source Shortest Paths

    Dominant cost: Topological sort + relaxation in topo order → Θ(V + E) for sort + O(E) for relaxations.
    Total runtime: Θ(V + E)
    (Longest paths variant: Same complexity, but maximizes instead of minimizes.)

Practical Comparison (Based on Test Data)

The experimental data aligns well with the theory, showing linear scaling, but highlights overheads from density and structure.

Performance on Sparse Graphs (E ≈ V)

    All algorithms have similar theoretical complexity: Θ(V + E) ≈ Θ(V).
    Practical results:
        Topo sort is fastest overall (e.g., avg ~10-20k ns for V=10-20), as it's a single DFS.
        SCC takes ~2x longer (e.g., graph 9: 37400 ns vs topo 9000 ns), due to two DFS + transpose.
        DAG-SP varies; low ops/times when run (e.g., graph 1: 24300 ns), but many 0 ops due to cycle detection skips.
    Insight: Sparse structures minimize edge traversals, keeping times low; cycles reduce SCC count but don't inflate times beyond linear.

Performance on Dense Graphs (E ≫ V)

    Theoretical remains Θ(V + E), but E dominates.
    Practical data confirms: Dense graphs show higher ops/times.
        SCC: Graph 18 (E=1307): 2714 ops, 599000 ns vs sparse graph 15 (E=461): 1022 ops, 100600 ns → ~6x time for ~3x edges.
        Topo: Graph 18: 1357 ops, 167200 ns → scales with E.
        DAG-SP: Higher when applicable (e.g., graph 16, E=322: 674 ops, 384700 ns).
    Largest graph (ID #18, V=50):
        SCC: 599000 ns
        Topo: 167200 ns → ~3.5x faster than SCC, close to expected (half the DFS work).
        Summary total: Not provided, but trends consistent.

    Conclusion: The Θ(E) edge processing bottleneck becomes dominant as density increases. For cyclic graphs, SCC helps identify condensable DAGs for further processing (via TaskOrderDeriver), but DAG-SP skips or fails, aligning with theory. Node weights add negligible overhead. In practice, Java's list iterations introduce minor cache misses in dense cases, but overall linearity holds.

## Conclusions

Use topological sort when you have a DAG and need dependency ordering (e.g., build systems).

Use Kosaraju's for general directed graphs to find SCCs, great for condensing graphs or detecting cycles.

Use DAG-SP for shortest paths in acyclic graphs; handles negative weights, faster than Bellman-Ford. For longest paths, use the variant for critical paths.

Practical recommendations: For large sparse graphs, adjacency lists are key. Pre-check for cycles if needed for topo/DAG-SP. Node weights work well for task models; convert to edge weights if standard libs expect that. Test on your data – density hurts more in practice due to memory access. If cycles present, condense via SCC then topo for partial orders. If code needed, the provided Java implementations cover these.

## Run & Build

Quick instructions to build, test and run the project on Windows (PowerShell). Maven is used for build and tests. The examples below assume you run them in the repository root (where `pom.xml` is located).

1) Run unit tests

```powershell
mvn test
```

2) Build the JAR (produces `target/untitled1-1.0-SNAPSHOT.jar`)

```powershell
mvn package
```

3) Run the report generator over all `data/input_*.json` files

This will run `Main` which iterates `data/input_*.json` and writes `data/report_*.json` outputs.
Use the built classes + dependency jars on the classpath:

```powershell
java -cp "target/untitled1-1.0-SNAPSHOT.jar;target/dependency/*" aitu.edu.Main
```

Alternatively, use Maven to run it directly (handles classpath automatically):

```powershell
mvn exec:java -Dexec.mainClass="aitu.edu.Main"
```

If you prefer to run a specific generator directly (no Main):

- Generate reports from a single input file using `TasksReportGenerator` (example)

```powershell
java -cp "target/untitled1-1.0-SNAPSHOT.jar;target/dependency/*" aitu.edu.TasksReportGenerator <input.json> <output.json>
```

(If you get a "NoClassDefFoundError" for dependencies, ensure you have run `mvn package` and a `target/dependency` directory containing the runtime jars is present. Alternatively run via your IDE which handles the classpath.)

4) Generate CSV summary files from existing `report_*.json` files

After `report_*.json` files are present in `data/`, run the CSV generator:

```powershell
java -cp "target/untitled1-1.0-SNAPSHOT.jar;target/dependency/*" aitu.edu.CsvGenerator
```

This writes the following CSV files into `data/`:
- `scc_results.csv`
- `topo_results.csv`
- `shortest_path_results.csv`
- `longest_path_results.csv`
- `summary_results.csv`


## Why DAG shortest-path operations_count can be 0 and no paths are reported

The DAG shortest-path and longest-path (critical path) computations are executed only when a graph is marked as a DAG in its metadata. The report generator checks `metadata.is_dag` in the input JSON and skips DAG-specific algorithms for non-DAG graphs. When skipped, the operations_count is 0 and no paths are reported. See `TasksReportGenerator.processGraph(...)` for the exact behavior.
