package aitu.edu.graph.dagsp;

import aitu.edu.graph.util.PathReconstructor;

import java.util.*;

/**
 * Represents the result of a path-finding algorithm, including distances, predecessors, and path reconstruction.
 */
public class PathResult {
    public static final long INF = Long.MAX_VALUE / 4;
    public static final long NEG_INF = Long.MIN_VALUE / 4;

    private final int src;
    private final long[] dist;
    private final Map<Integer, Integer> pred;

    /**
     * Constructs a PathResult with the given source, distances, and predecessors.
     *
     * @param src  the source node
     * @param dist the array of distances from the source
     * @param pred the map of predecessors for path reconstruction
     */
    public PathResult(int src, long[] dist, Map<Integer, Integer> pred) {
        this.src = src;
        this.dist = dist;
        this.pred = pred == null ? Collections.emptyMap() : Collections.unmodifiableMap(new HashMap<>(pred));
    }

    /**
     * Returns the source node.
     *
     * @return the source node
     */
    public int getSource() {
        return src;
    }

    /**
     * Returns a copy of the distances array.
     *
     * @return the distances array
     */
    public long[] distances() {
        return dist.clone();
    }

    /**
     * Returns the distance to a specific node.
     *
     * @param v the node
     * @return the distance to the node, or INF if out of bounds
     */
    public long distanceTo(int v) {
        if (v < 0 || v >= dist.length) return INF;
        return dist[v];
    }

    /**
     * Returns the map of predecessors.
     *
     * @return the predecessors map
     */
    public Map<Integer, Integer> predecessors() {
        return pred;
    }

    /**
     * Reconstructs the path from the source to the destination node.
     *
     * @param dst the destination node
     * @return the list of nodes in the path, or empty list if no path
     */
    public List<Integer> reconstructPath(int dst) {
        return PathReconstructor.reconstruct(pred, src, dst);
    }
}
