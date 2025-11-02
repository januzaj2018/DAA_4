package aitu.edu.graph.dagsp;

import aitu.edu.graph.util.Graph;
import aitu.edu.graph.util.Metrics;
import aitu.edu.graph.topo.DFSTopologicalSort;

import java.util.*;

/**
 * Computes longest paths in a directed acyclic graph (DAG) using topological sorting and dynamic programming.
 */
public class DagLongestPath {

    /**
     * Computes the longest paths from a source node in a DAG.
     *
     * @param g      the graph to process
     * @param src    the source node
     * @param metrics optional metrics collector for performance tracking
     * @return a PathResult containing distances and predecessors for path reconstruction
     */
    public static PathResult longestPath(Graph g, int src, Metrics metrics) {
        if (g == null) throw new IllegalArgumentException("graph is null");
        int n = g.nodeCount();
        long[] dist = new long[n];
        Arrays.fill(dist, PathResult.NEG_INF);
        Map<Integer, Integer> pred = new HashMap<>();

        if (src < 0 || src >= n) return new PathResult(src, dist, pred);

        // Initialize source distance with its duration
        long srcDur = g.durationOf(src).orElse(0L);
        dist[src] = srcDur;

        // Get topological order
        List<Integer> topo = DFSTopologicalSort.topologicalOrder(g.adjacency(), metrics);
        // Relax edges in topological order to compute longest paths
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
