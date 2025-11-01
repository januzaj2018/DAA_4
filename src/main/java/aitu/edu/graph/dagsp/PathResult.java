package aitu.edu.graph.dagsp;

import aitu.edu.graph.util.PathReconstructor;

import java.util.*;

public class PathResult {
    public static final long INF = Long.MAX_VALUE / 4;
    public static final long NEG_INF = Long.MIN_VALUE / 4;

    private final int src;
    private final long[] dist;
    private final Map<Integer, Integer> pred;

    public PathResult(int src, long[] dist, Map<Integer, Integer> pred) {
        this.src = src;
        this.dist = dist;
        this.pred = pred == null ? Collections.emptyMap() : Collections.unmodifiableMap(new HashMap<>(pred));
    }

    public int getSource() {
        return src;
    }

    public long[] distances() {
        return dist.clone();
    }

    public long distanceTo(int v) {
        if (v < 0 || v >= dist.length) return INF;
        return dist[v];
    }

    public Map<Integer, Integer> predecessors() {
        return pred;
    }

    /**
     * Reconstruct path from source to dst. Returns empty list if unreachable.
     */
    public List<Integer> reconstructPath(int dst) {
        return PathReconstructor.reconstruct(pred, src, dst);
    }
}
