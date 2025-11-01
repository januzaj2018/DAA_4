package aitu.edu.graph.dagsp;

import aitu.edu.graph.util.Graph;
import aitu.edu.graph.util.Metrics;
import aitu.edu.graph.topo.DFSTopologicalSort;

import java.util.*;

public class DagLongestPath {

    /**
     * Compute single-source longest paths on a DAG where node weights (durations) are stored in the Graph.
     * Distance of a path is defined as the sum of node durations along the path, including the source and destination nodes.
     * If a node has no duration defined, its duration is treated as 0.
     */
    public static PathResult longestPath(Graph g, int src, Metrics metrics) {
        if (g == null) throw new IllegalArgumentException("graph is null");
        int n = g.nodeCount();
        long[] dist = new long[n];
        Arrays.fill(dist, PathResult.NEG_INF);
        Map<Integer, Integer> pred = new HashMap<>();

        if (src < 0 || src >= n) return new PathResult(src, dist, pred);

        long srcDur = g.durationOf(src).orElse(0L);
        dist[src] = srcDur;

        List<Integer> topo = DFSTopologicalSort.topologicalOrder(g.adjacency(), metrics);
        for (int u : topo) {
            if (dist[u] == PathResult.NEG_INF) continue; // unreachable
            for (int v : g.neighbors(u)) {
                long vdur = g.durationOf(v).orElse(0L);
                long cand = dist[u] + vdur;
                if (cand > dist[v]) {
                    dist[v] = cand;
                    pred.put(v, u);
                }
            }
        }

        return new PathResult(src, dist, pred);
    }
}
