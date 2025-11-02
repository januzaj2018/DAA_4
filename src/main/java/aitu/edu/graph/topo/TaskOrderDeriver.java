package aitu.edu.graph.topo;

import aitu.edu.graph.scc.SCCResult;
import aitu.edu.graph.util.Graph;

import java.util.*;

/**
 * Derives task orders from component orders and builds condensation graphs.
 */
public class TaskOrderDeriver {


    /**
     * Builds the condensation adjacency list from a graph and its SCC result.
     *
     * @param g   the graph
     * @param scc the SCC result
     * @return the condensation adjacency list
     */
    public static List<List<Integer>> buildCondensation(Graph g, SCCResult scc) {
        int compCount = scc.componentCount();
        List<Set<Integer>> temp = new ArrayList<>(compCount);
        for (int i = 0; i < compCount; i++) temp.add(new LinkedHashSet<>());

        // Collect edges between components
        int[] compIds = scc.getComponentIds();
        for (int[] e : g.edges()) {
            int u = e[0];
            int v = e[1];
            if (u < 0 || v < 0 || u >= compIds.length || v >= compIds.length) continue;
            int cu = compIds[u];
            int cv = compIds[v];
            if (cu != cv) temp.get(cu).add(cv);
        }

        // Convert sets to lists
        List<List<Integer>> adj = new ArrayList<>(compCount);
        for (int i = 0; i < compCount; i++) {
            List<Integer> nbrs = new ArrayList<>(temp.get(i));
            adj.add(Collections.unmodifiableList(nbrs));
        }
        return Collections.unmodifiableList(adj);
    }

    /**
     * Derives the task order from a component order.
     *
     * @param componentOrder the order of components
     * @param scc            the SCC result
     * @return the list of tasks in order
     */
    public static List<Integer> deriveTaskOrderFromComponentOrder(List<Integer> componentOrder, SCCResult scc) {
        List<List<Integer>> comps = scc.getComponents();
        List<Integer> out = new ArrayList<>();
        // Append all nodes from each component in order
        for (int cid : componentOrder) {
            if (cid < 0 || cid >= comps.size()) continue; // ignore invalid
            List<Integer> comp = comps.get(cid);
            out.addAll(comp);
        }
        return out;
    }
}
