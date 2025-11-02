package aitu.edu.graph.dagsp;

import aitu.edu.graph.util.Graph;
import aitu.edu.graph.util.GraphBuilder;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class DagShortestPathCorrectnessTests {

    @Test
    public void testSimpleShortestPath() {
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

        // Distances: 0->0:1, 1: 1+2=3, 2:1+5=6, 3: via 1 is 6, via 2 is 9 -> min 6
        assertEquals(1L, sp.distanceTo(0));
        assertEquals(3L, sp.distanceTo(1));
        assertEquals(6L, sp.distanceTo(2));
        assertEquals(6L, sp.distanceTo(3));

        // path to 3 via 1: [0,1,3]
        assertEquals(Arrays.asList(0,1,3), sp.reconstructPath(3));
    }

    @Test
    public void testAlternativeSources() {
        GraphBuilder gb = new GraphBuilder();
        gb.ensureN(5)
          .addEdge(0,1).addEdge(1,2).addEdge(2,3).addEdge(3,4)
          .setDuration(0,1).setDuration(1,1).setDuration(2,1).setDuration(3,1).setDuration(4,1);
        Graph g = gb.build();

        PathResult sp = DagShortestPath.shortestPath(g, 2, null);
        assertEquals(1L, sp.distanceTo(2));
        assertEquals(2L, sp.distanceTo(3));
        assertEquals(Arrays.asList(2,3,4), sp.reconstructPath(4));
    }
}

