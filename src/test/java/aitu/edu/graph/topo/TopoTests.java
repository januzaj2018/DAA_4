package aitu.edu.graph.topo;

import aitu.edu.graph.scc.KosarajuSCC;
import aitu.edu.graph.scc.SCCResult;
import aitu.edu.graph.util.Graph;
import aitu.edu.graph.util.GraphBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TopoTests {

    @Test
    public void testTopologicalOrderSimple() {
        List<List<Integer>> adj = Arrays.asList(
                Collections.singletonList(1),
                Collections.singletonList(2),
                Collections.emptyList()
        );
        List<Integer> topo = DFSTopologicalSort.topologicalOrder(adj, null);
        assertEquals(Arrays.asList(0,1,2), topo);
    }

    @Test
    public void testTopologicalOrderMultipleComponents_isValidTopo() {
        List<List<Integer>> adj = Arrays.asList(
                Collections.singletonList(1),
                Collections.emptyList(),
                Collections.singletonList(3),
                Collections.emptyList()
        );
        List<Integer> topo = DFSTopologicalSort.topologicalOrder(adj, null);
        // verify topological property: for every edge u->v index(u) < index(v)
        Map<Integer,Integer> pos = new HashMap<>();
        for (int i = 0; i < topo.size(); i++) pos.put(topo.get(i), i);
        for (int u = 0; u < adj.size(); u++) {
            for (int v : adj.get(u)) {
                assertTrue(pos.get(u) < pos.get(v), "edge " + u + "->" + v + " violated topo order");
            }
        }
    }

    @Test
    public void testEmptyGraphReturnsEmptyOrder() {
        List<List<Integer>> adj = Collections.emptyList();
        List<Integer> topo = DFSTopologicalSort.topologicalOrder(adj, null);
        assertTrue(topo.isEmpty());
    }

    @Test
    public void testBuildCondensationAndDeriveTaskOrder() {
        // Graph: 0<->1 (SCC A), 0->2 (edge from A to B)
        Graph g = new GraphBuilder().addEdge(0,1).addEdge(1,0).addEdge(0,2).ensureN(3).build();
        SCCResult scc = KosarajuSCC.computeSCC(g);
        // build condensation
        List<List<Integer>> cond = TaskOrderDeriver.buildCondensation(g, scc);
        // cond should be a DAG of size = component count
        int compCount = scc.componentCount();
        assertEquals(compCount, cond.size());

        // get component order via DFS-based topo sort
        List<Integer> compOrder = DFSTopologicalSort.topologicalOrder(cond, null);
        // derive task order by flattening components in component order
        List<Integer> taskOrder = TaskOrderDeriver.deriveTaskOrderFromComponentOrder(compOrder, scc);

        // taskOrder should contain all original nodes exactly once
        Set<Integer> unique = new HashSet<>(taskOrder);
        assertEquals(g.nodeCount(), taskOrder.size());
        assertEquals(g.nodeCount(), unique.size());
        for (int i = 0; i < g.nodeCount(); i++) assertTrue(unique.contains(i));

        // For every original edge u->v, either comp(u)==comp(v) or position(u) < position(v)
        Map<Integer,Integer> pos = new HashMap<>();
        for (int i = 0; i < taskOrder.size(); i++) pos.put(taskOrder.get(i), i);
        int[] compIds = scc.getComponentIds();
        for (int[] e : g.edges()) {
            int u = e[0], v = e[1];
            if (compIds[u] == compIds[v]) continue;
            assertTrue(pos.get(u) < pos.get(v), "Edge " + u + "->" + v + " violates component order");
        }
    }

    @Test
    public void testDeriveIgnoresInvalidComponentIds() throws IOException {
        Graph g = GraphBuilder.fromFile("src/test/resources/dag.json").build();
        SCCResult scc = KosarajuSCC.computeSCC(g);
        List<Integer> compOrder = new ArrayList<>();
        compOrder.add(-1); // invalid
        for (int i = 0; i < scc.componentCount(); i++) compOrder.add(i);
        compOrder.add(scc.componentCount()); // invalid
        List<Integer> taskOrder = TaskOrderDeriver.deriveTaskOrderFromComponentOrder(compOrder, scc);
        // should equal flattening of valid component sequence [0..componentCount-1]
        List<Integer> expected = TaskOrderDeriver.deriveTaskOrderFromComponentOrder(compOrder.subList(1, 1 + scc.componentCount()), scc);
        assertEquals(expected, taskOrder);
    }
}

