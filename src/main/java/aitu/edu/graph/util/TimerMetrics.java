package aitu.edu.graph.util;


/**
 * Implementation of Metrics that tracks DFS visits, edges, relaxations, and elapsed time.
 */
public class TimerMetrics implements Metrics {
    private long dfsVisits = 0;
    private long dfsEdges = 0;
    private long relaxations = 0;
    private final long startNs;

    /**
     * Constructs a TimerMetrics instance, starting the timer.
     */
    public TimerMetrics() {
        this.startNs = System.nanoTime();
    }

    @Override
    public synchronized void incDfsVisit() {
        dfsVisits++;
    }

    @Override
    public synchronized void incDfsEdge() {
        dfsEdges++;
    }

    @Override
    public synchronized void incRelaxation() {
        relaxations++;
    }

    @Override
    public synchronized long getDfsVisits() {
        return dfsVisits;
    }

    @Override
    public synchronized long getDfsEdges() {
        return dfsEdges;
    }

    @Override
    public synchronized long getRelaxations() {
        return relaxations;
    }

    @Override
    public long elapsedMs() {
        return (System.nanoTime() - startNs) / 1_000_000;
    }
}
