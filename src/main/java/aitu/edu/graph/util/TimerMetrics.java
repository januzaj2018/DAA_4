package aitu.edu.graph.util;

/**
 * Simple implementation of Metrics using System.nanoTime().
 */
public class TimerMetrics implements Metrics {
    private long dfsVisits = 0;
    private long dfsEdges = 0;
    private final long startNs;

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
    public synchronized long getDfsVisits() {
        return dfsVisits;
    }

    @Override
    public synchronized long getDfsEdges() {
        return dfsEdges;
    }

    @Override
    public long elapsedMs() {
        return (System.nanoTime() - startNs) / 1_000_000;
    }
}
