package aitu.edu.graph.util;

public interface Metrics {
    void incDfsVisit();
    void incDfsEdge();
    void incRelaxation();
    long getDfsVisits();
    long getDfsEdges();
    long getRelaxations();
    long elapsedMs();
}
