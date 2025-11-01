package aitu.edu.graph.dagsp;

import aitu.edu.graph.util.Graph;
import aitu.edu.graph.util.GraphBuilder;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class CriticalPathTests {

    @Test
    public void testCriticalPathSimple() {
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

        PathResult cp = CriticalPathExtractor.criticalPath(g, null);

        // Expected critical path is 0 -> 2 -> 3 with total duration 1 + 5 + 3 = 9
        assertEquals(0, cp.getSource());
        assertEquals(9L, cp.distanceTo(3));
        assertEquals(Arrays.asList(0,2,3), cp.reconstructPath(3));
    }
}

