package aitu.edu.graph.util;

import java.util.*;

/**
 * Represents an immutable directed graph with nodes and edges, including node durations.
 */
public final class Graph {
    private final int n;
    private final List<List<Integer>> adj;
    private final Map<Integer, Long> durations;

    /**
     * Constructs a Graph with the given number of nodes, adjacency list, and durations.
     *
     * @param n         the number of nodes
     * @param adj       the adjacency list
     * @param durations the map of node durations
     */
    public Graph(int n, List<List<Integer>> adj, Map<Integer, Long> durations) {
        this.n = n;
        // Defensive copy of adjacency list
        List<List<Integer>> copy = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            List<Integer> row = (i < adj.size() && adj.get(i) != null)
                    ? Collections.unmodifiableList(new ArrayList<>(adj.get(i)))
                    : Collections.emptyList();
            copy.add(row);
        }
        this.adj = Collections.unmodifiableList(copy);

        // Defensive copy of durations
        Map<Integer, Long> dcopy = new HashMap<>();
        if (durations != null) dcopy.putAll(durations);
        this.durations = Collections.unmodifiableMap(dcopy);
    }

    /**
     * Returns the number of nodes in the graph.
     *
     * @return the node count
     */
    public int nodeCount() {
        return n;
    }

    /**
     * Returns the adjacency list of the graph.
     *
     * @return the adjacency list
     */
    public List<List<Integer>> adjacency() {
        return adj;
    }

    /**
     * Returns the neighbors of a node.
     *
     * @param v the node
     * @return the list of neighbors
     */
    public List<Integer> neighbors(int v) {
        return (v >= 0 && v < n) ? adj.get(v) : Collections.emptyList();
    }

    /**
     * Returns the duration of a node.
     *
     * @param v the node
     * @return the duration, or empty if not set
     */
    public OptionalLong durationOf(int v) {
        Long d = durations.get(v);
        return d == null ? OptionalLong.empty() : OptionalLong.of(d);
    }

    /**
     * Returns the map of all durations.
     *
     * @return the durations map
     */
    public Map<Integer, Long> durations() {
        return durations;
    }

    /**
     * Returns a list of all edges as int[2] arrays.
     *
     * @return the list of edges
     */
    public List<int[]> edges() {
        List<int[]> es = new ArrayList<>();
        for (int u = 0; u < n; u++) {
            for (int v : adj.get(u)) es.add(new int[]{u, v});
        }
        return es;
    }
}
