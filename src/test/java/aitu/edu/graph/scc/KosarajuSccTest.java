package aitu.edu.graph.scc;

import aitu.edu.graph.util.Graph;
import aitu.edu.graph.util.GraphBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class KosarajuSccTest {

    @Test
    public void testGraphConstructionAndDurations() {
        GraphBuilder gb = new GraphBuilder();
        gb.ensureN(3)
          .addEdge(0, 1)
          .addEdge(1, 2)
          .setDuration(0, 10)
          .setDuration(2, 5);

        Graph g = gb.build();
        assertEquals(3, g.nodeCount());

        // adjacency
        List<List<Integer>> adj = g.adjacency();
        // Check neighbors explicitly (avoid brittle internal adj-list sizing)
        assertEquals(Collections.singletonList(1), g.neighbors(0));
        assertEquals(Collections.singletonList(2), g.neighbors(1));
        assertEquals(Collections.emptyList(), g.neighbors(2));

        // durations
        assertTrue(g.durationOf(0).isPresent());
        assertEquals(10L, g.durationOf(0).getAsLong());
        assertFalse(g.durationOf(1).isPresent());
        assertTrue(g.durationOf(2).isPresent());
        assertEquals(5L, g.durationOf(2).getAsLong());
    }

    @Test
    public void testTinyCycleSCC() {
        GraphBuilder gb = new GraphBuilder();
        gb.addEdge(0, 1).addEdge(1, 0);
        Graph g = gb.build();

        SCCResult res = KosarajuSCC.computeSCC(g);
        List<List<Integer>> comps = res.getComponents();

        assertEquals(1, res.componentCount(), "Cycle should form a single SCC");
        List<Integer> comp = comps.get(0);
        Set<Integer> s = new HashSet<>(comp);
        assertEquals(new HashSet<>(Arrays.asList(0,1)), s);

        // condensation: no inter-component edges
        int[] compIds = res.getComponentIds();
        Set<String> condEdges = new HashSet<>();
        for (int[] e : g.edges()) {
            int cu = compIds[e[0]];
            int cv = compIds[e[1]];
            if (cu != cv) condEdges.add(cu + "->" + cv);
        }
        assertTrue(condEdges.isEmpty());
    }

    @Test
    public void testDAGSingletonsAndCondensation() {
        GraphBuilder gb = new GraphBuilder();
        gb.addEdge(0, 1).addEdge(1, 2);
        Graph g = gb.build();

        SCCResult res = KosarajuSCC.computeSCC(g);
        assertEquals(3, res.componentCount(), "Each node in DAG should be its own SCC");
        for (List<Integer> c : res.getComponents()) {
            assertEquals(1, c.size());
        }
        int[] compIds = res.getComponentIds();
        Set<String> condEdges = new HashSet<>();
        for (int[] e : g.edges()) {
            int cu = compIds[e[0]];
            int cv = compIds[e[1]];
            if (cu != cv) condEdges.add(cu + "->" + cv);
        }

        // there should be two condensation edges corresponding to the two original cross-component edges
        assertEquals(2, condEdges.size());
        for (String ed : condEdges) {
            assertFalse(ed.split("->")[0].equals(ed.split("->")[1]));
        }
    }

    @Test
    public void testGraphBuilderFromFile_cycle() throws IOException {
        String path = "src/test/resources/cycle.json";
        Graph g = GraphBuilder.fromFile(path).build();
        SCCResult res = KosarajuSCC.computeSCC(g);
        assertEquals(1, res.componentCount());
        List<Integer> comp = res.getComponents().get(0);
        Set<Integer> s = new HashSet<>(comp);
        assertEquals(new HashSet<>(Arrays.asList(0,1)), s);
    }

    @Test
    public void testGraphBuilderFromFile_dag() throws IOException {
        String path = "src/test/resources/dag.json";
        Graph g = GraphBuilder.fromFile(path).build();
        SCCResult res = KosarajuSCC.computeSCC(g);
        assertEquals(3, res.componentCount());
        for (List<Integer> c : res.getComponents()) assertEquals(1, c.size());

        // condensation edges count
        int[] compIds = res.getComponentIds();
        Set<String> condEdges = new HashSet<>();
        for (int[] e : g.edges()) {
            int cu = compIds[e[0]];
            int cv = compIds[e[1]];
            if (cu != cv) condEdges.add(cu + "->" + cv);
        }
        assertEquals(2, condEdges.size());
    }
}
