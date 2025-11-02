package aitu.edu.graph.util;

/**
 * Interface for collecting performance metrics during graph algorithms.
 */
public interface Metrics {
    /**
     * Increments the DFS visit count.
     */
    void incDfsVisit();
    /**
     * Increments the DFS edge count.
     */
    void incDfsEdge();
    /**
     * Increments the relaxation count.
     */
    void incRelaxation();
    /**
     * Returns the total DFS visits.
     *
     * @return the DFS visit count
     */
    long getDfsVisits();
    /**
     * Returns the total DFS edges processed.
     *
     * @return the DFS edge count
     */
    long getDfsEdges();
    /**
     * Returns the total relaxations.
     *
     * @return the relaxation count
     */
    long getRelaxations();
    /**
     * Returns the elapsed time in milliseconds.
     *
     * @return the elapsed time
     */
    long elapsedMs();
}
