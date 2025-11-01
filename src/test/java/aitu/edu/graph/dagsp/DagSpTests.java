package aitu.edu.graph.dagsp;

import aitu.edu.graph.util.Graph;
import aitu.edu.graph.util.GraphBuilder;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class DagSpTests {

    @Test
    public void testShortestAndLongestPathSimple() {
        // Graph:
        // 0 -> 1 -> 3
        //  \        ^
        //   -> 2 -> /
        GraphBuilder gb = new GraphBuilder();
        gb.ensureN(4)
          .addEdge(0,1).addEdge(1,3)
          .addEdge(0,2).addEdge(2,3)
          .setDuration(0, 1)
          .setDuration(1, 2)
          .setDuration(2, 5)
          .setDuration(3, 3);
        Graph g = gb.build();

        PathResult sp = DagShortestPath.shortestPath(g, 0, null);
        assertEquals(6L, sp.distanceTo(3)); // 1 + 2 + 3
        assertEquals(Arrays.asList(0,1,3), sp.reconstructPath(3));

        PathResult lp = DagLongestPath.longestPath(g, 0, null);
        assertEquals(9L, lp.distanceTo(3)); // 1 + 5 + 3
        assertEquals(Arrays.asList(0,2,3), lp.reconstructPath(3));

        // unreachable node test
        gb = new GraphBuilder();
        gb.ensureN(3).setDuration(0, 10).setDuration(1, 20); // node 2 isolated
        g = gb.build();
        sp = DagShortestPath.shortestPath(g, 0, null);
        assertEquals(PathResult.INF, sp.distanceTo(2));
        lp = DagLongestPath.longestPath(g, 0, null);
        assertEquals(PathResult.NEG_INF, lp.distanceTo(2));
    }
}

