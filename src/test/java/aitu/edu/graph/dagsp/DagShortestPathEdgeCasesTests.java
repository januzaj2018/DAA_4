package aitu.edu.graph.dagsp;

import aitu.edu.graph.util.Graph;
import aitu.edu.graph.util.GraphBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DagShortestPathEdgeCasesTests {

    @Test
    public void testEmptyGraph() {
        GraphBuilder gb = new GraphBuilder();
        Graph g = gb.build(); // 0 nodes
        // asking for src=0 is out of bounds -> should return distances array with INF and no exception
        PathResult res = DagShortestPath.shortestPath(g, 0, null);
        assertEquals(0, res.getSource());
        // nodeCount is 0, distance array length should be 0
        assertEquals(0, res.distances().length);
    }

    @Test
    public void testSingleNode() {
        GraphBuilder gb = new GraphBuilder();
        gb.ensureN(1).setDuration(0, 7);
        Graph g = gb.build();
        PathResult res = DagShortestPath.shortestPath(g, 0, null);
        assertEquals(0, res.getSource());
        assertEquals(1, res.distances().length);
        assertEquals(7L, res.distanceTo(0));
        assertEquals(java.util.Arrays.asList(0), res.reconstructPath(0));
    }

    @Test
    public void testDisconnectedNodes() {
        GraphBuilder gb = new GraphBuilder();
        gb.ensureN(3)
          .setDuration(0, 1)
          .setDuration(1, 2)
          .setDuration(2, 3);
        // no edges
        Graph g = gb.build();
        PathResult res = DagShortestPath.shortestPath(g, 0, null);
        assertEquals(1, res.reconstructPath(0).size());
        assertEquals(java.util.Collections.singletonList(0), res.reconstructPath(0));
        // other nodes unreachable
        assertEquals(PathResult.INF, res.distanceTo(1));
        assertEquals(PathResult.INF, res.distanceTo(2));
    }

    @Test
    public void testCycleGraphHandled() {
        // If the graph contains a cycle, topological sort should detect or produce an order, but
        // shortestPath is designed for DAGs. We ensure it doesn't crash and returns sensible results
        // for nodes reachable before the algorithm processes cycles.
        GraphBuilder gb = new GraphBuilder();
        gb.ensureN(3)
          .addEdge(0,1).addEdge(1,2).addEdge(2,0)
          .setDuration(0,1).setDuration(1,1).setDuration(2,1);
        Graph g = gb.build();
        // call should not throw
        PathResult res = DagShortestPath.shortestPath(g, 0, null);
        // source distance should be its duration
        assertEquals(1L, res.distanceTo(0));
    }
}

