package aitu.edu.graph.dagsp;

import aitu.edu.graph.util.Graph;
import aitu.edu.graph.util.Metrics;
import aitu.edu.graph.topo.DFSTopologicalSort;

import java.util.*;

public class DagShortestPath {

    /**
     * Compute single-source shortest paths on a DAG where node weights (durations) are stored in the Graph.
     * Distance of a path is defined as the sum of node durations along the path, including the source and destination nodes.
     * If a node has no duration defined, its duration is treated as 0.
     */
    public static PathResult shortestPath(Graph g, int src, Metrics metrics) {
        if (g == null) throw new IllegalArgumentException("graph is null");
        int n = g.nodeCount();
        long[] dist = new long[n];
        Arrays.fill(dist, PathResult.INF);
        Map<Integer, Integer> pred = new HashMap<>();

        if (src < 0 || src >= n) return new PathResult(src, dist, pred);

        // initialize source distance as its own duration (or 0 if absent)
        long srcDur = g.durationOf(src).orElse(0L);
        dist[src] = srcDur;

        List<Integer> topo = DFSTopologicalSort.topologicalOrder(g.adjacency(), metrics);
        // relax edges in topological order
        for (int u : topo) {
            if (dist[u] == PathResult.INF) continue; // unreachable
            List<Integer> nbrs = g.neighbors(u);
            for (int v : nbrs) {
                long vdur = g.durationOf(v).orElse(0L);
                long cand = dist[u] + vdur;
                if (cand < dist[v]) {
                    dist[v] = cand;
                    pred.put(v, u);
                }
            }
        }

        return new PathResult(src, dist, pred);
    }
}
