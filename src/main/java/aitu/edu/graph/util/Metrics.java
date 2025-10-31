package aitu.edu.graph.util;

public interface Metrics {
    void incDfsVisit();
    void incDfsEdge();
    long getDfsVisits();
    long getDfsEdges();
    long elapsedMs();
}
