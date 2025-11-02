package aitu.edu.graph.dagsp;

import aitu.edu.graph.util.Graph;
import aitu.edu.graph.util.GraphBuilder;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class CriticalPathAdditionalTests {

    @Test
    public void testCriticalPathReconstructionFormat() {
        // Linear chain 0->1->2->3 durations 1,2,3,4 total 10
        GraphBuilder gb = new GraphBuilder();
        gb.ensureN(4)
          .addEdge(0,1).addEdge(1,2).addEdge(2,3)
          .setDuration(0,1).setDuration(1,2).setDuration(2,3).setDuration(3,4);
        Graph g = gb.build();

        PathResult cp = CriticalPathExtractor.criticalPath(g, null);
        assertEquals(0, cp.getSource());
        assertEquals(10L, cp.distanceTo(3));
        assertEquals(Arrays.asList(0,1,2,3), cp.reconstructPath(3));
    }
}

