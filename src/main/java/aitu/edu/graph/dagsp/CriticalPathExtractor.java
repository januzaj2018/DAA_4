package aitu.edu.graph.dagsp;

import aitu.edu.graph.util.Graph;
import aitu.edu.graph.util.Metrics;
import aitu.edu.graph.topo.DFSTopologicalSort;

import java.util.*;

public class CriticalPathExtractor {

    /**
     * Extract the global critical path (longest-duration path) in the DAG.
     * Returns a PathResult that can be used to inspect distances and reconstruct the critical path to the sink node.
     */
    public static PathResult criticalPath(Graph g, Metrics metrics) {
        if (g == null) throw new IllegalArgumentException("graph is null");
        int n = g.nodeCount();
        long[] dist = new long[n];
        Arrays.fill(dist, PathResult.NEG_INF);
        Map<Integer, Integer> pred = new HashMap<>();

        // initialize each node as a potential start with its own duration
        for (int v = 0; v < n; v++) {
            dist[v] = g.durationOf(v).orElse(0L);
        }

        List<Integer> topo = DFSTopologicalSort.topologicalOrder(g.adjacency(), metrics);
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

        // find global max sink
        long best = PathResult.NEG_INF;
        int sink = -1;
        for (int i = 0; i < n; i++) {
            if (dist[i] > best) {
                best = dist[i];
                sink = i;
            }
        }

        if (sink == -1) {
            // empty graph
            return new PathResult(0, dist, pred);
        }

        // find the source (start) of the critical path by walking predecessors backwards
        int src = sink;
        while (pred.containsKey(src)) {
            src = pred.get(src);
        }

        return new PathResult(src, dist, pred);
    }
}
