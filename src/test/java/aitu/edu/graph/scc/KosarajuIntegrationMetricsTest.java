package aitu.edu.graph.scc;

import aitu.edu.graph.util.Graph;
import aitu.edu.graph.util.GraphBuilder;
import aitu.edu.graph.util.TimerMetrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class KosarajuIntegrationMetricsTest {

    @Test
    public void testMetricsOnCycleFromFile() throws IOException {
        Graph g = GraphBuilder.fromFile("src/test/resources/cycle.json").build();
        TimerMetrics metrics = new TimerMetrics();
        KosarajuSCC.computeSCC(g, metrics);

        int n = g.nodeCount();
        int m = g.edges().size();
        assertEquals(2L * n, metrics.getDfsVisits(), "Expected total DFS visits = 2*n (dfs1 + dfs2)");
        assertEquals(2L * m, metrics.getDfsEdges(), "Expected total DFS edges = 2*m (forward + reverse)");
        assertTrue(metrics.elapsedMs() >= 0, "Elapsed ms should be non-negative");
    }

    @Test
    public void testMetricsOnDAGFromFile() throws IOException {
        Graph g = GraphBuilder.fromFile("src/test/resources/dag.json").build();
        TimerMetrics metrics = new TimerMetrics();
        KosarajuSCC.computeSCC(g, metrics);

        int n = g.nodeCount();
        int m = g.edges().size();
        assertEquals(2L * n, metrics.getDfsVisits());
        assertEquals(2L * m, metrics.getDfsEdges());
        assertTrue(metrics.elapsedMs() >= 0);
    }

    @Test
    public void testMetricsOnInlineGraph() {
        Graph g = new GraphBuilder().ensureN(4)
                .addEdge(0, 1)
                .addEdge(1, 2)
                .addEdge(2, 0)
                .addEdge(2, 3)
                .build();

        TimerMetrics metrics = new TimerMetrics();
        KosarajuSCC.computeSCC(g, metrics);
        int n = g.nodeCount();
        int m = g.edges().size();
        assertEquals(2L * n, metrics.getDfsVisits());
        assertEquals(2L * m, metrics.getDfsEdges());
        assertTrue(metrics.elapsedMs() >= 0);
    }
}

