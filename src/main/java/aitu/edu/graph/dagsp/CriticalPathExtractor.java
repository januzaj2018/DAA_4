package aitu.edu.graph.dagsp;

import aitu.edu.graph.util.Graph;
import aitu.edu.graph.util.Metrics;
import aitu.edu.graph.topo.DFSTopologicalSort;

import java.util.*;

/**
 * Extracts the critical (longest) path in a directed acyclic graph (DAG) using topological sorting and dynamic programming.
 */
public class CriticalPathExtractor {

    /**
     * Computes the critical path (longest path) from the source node in a DAG.
     *
     * @param g      the graph to process
     * @param metrics optional metrics collector for performance tracking
     * @return a PathResult containing distances and predecessors for path reconstruction
     */
    public static PathResult criticalPath(Graph g, Metrics metrics) {
        if (g == null) throw new IllegalArgumentException("graph is null");
        int n = g.nodeCount();
        long[] dist = new long[n];
        Arrays.fill(dist, PathResult.NEG_INF);
        Map<Integer, Integer> pred = new HashMap<>();

        // Initialize distances with node durations
        for (int v = 0; v < n; v++) {
            dist[v] = g.durationOf(v).orElse(0L);
        }

        // Get topological order
        List<Integer> topo = DFSTopologicalSort.topologicalOrder(g.adjacency(), metrics);
        // Relax edges in topological order to compute longest paths
        for (int u : topo) {
            if (dist[u] == PathResult.NEG_INF) continue; // unreachable in strange graphs
            for (int v : g.neighbors(u)) {
                if (metrics != null) metrics.incRelaxation();
                long vdur = g.durationOf(v).orElse(0L);
                long cand = dist[u] + vdur;
                if (cand > dist[v]) {
                    dist[v] = cand;
                    pred.put(v, u);
                }
            }
        }

        // Find the sink with the maximum distance
        long best = PathResult.NEG_INF;
        int sink = -1;
        for (int i = 0; i < n; i++) {
            if (dist[i] > best) {
                best = dist[i];
                sink = i;
            }
        }

        if (sink == -1) {
            return new PathResult(0, dist, pred);
        }

        // Reconstruct the path by following predecessors
        int src = sink;
        while (pred.containsKey(src)) {
            src = pred.get(src);
        }

        return new PathResult(src, dist, pred);
    }
}
