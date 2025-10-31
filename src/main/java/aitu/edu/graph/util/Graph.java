package aitu.edu.graph.util;

import java.util.*;

/**
 * Immutable directed graph represented by adjacency lists.
 * Nodes are integers 0..n-1.
 * Optionally supports node durations (Map from node -> duration in arbitrary units).
 */
public final class Graph {
    private final int n;
    private final List<List<Integer>> adj;
    private final Map<Integer, Long> durations;

    public Graph(int n, List<List<Integer>> adj, Map<Integer, Long> durations) {
        this.n = n;
        // defensive copy
        List<List<Integer>> copy = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            List<Integer> row = (i < adj.size() && adj.get(i) != null)
                    ? Collections.unmodifiableList(new ArrayList<>(adj.get(i)))
                    : Collections.emptyList();
            copy.add(row);
        }
        this.adj = Collections.unmodifiableList(copy);

        Map<Integer, Long> dcopy = new HashMap<>();
        if (durations != null) dcopy.putAll(durations);
        this.durations = Collections.unmodifiableMap(dcopy);
    }

    public int nodeCount() {
        return n;
    }

    public List<List<Integer>> adjacency() {
        return adj;
    }

    public List<Integer> neighbors(int v) {
        return (v >= 0 && v < n) ? adj.get(v) : Collections.emptyList();
    }

    public OptionalLong durationOf(int v) {
        Long d = durations.get(v);
        return d == null ? OptionalLong.empty() : OptionalLong.of(d);
    }

    public Map<Integer, Long> durations() {
        return durations;
    }

    /**
     * Return list of edges as int[2] = {from,to}
     */
    public List<int[]> edges() {
        List<int[]> es = new ArrayList<>();
        for (int u = 0; u < n; u++) {
            for (int v : adj.get(u)) es.add(new int[]{u, v});
        }
        return es;
    }
}

