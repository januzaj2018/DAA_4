package aitu.edu.graph.topo;

import aitu.edu.graph.scc.SCCResult;
import aitu.edu.graph.util.Graph;

import java.util.*;

public class TaskOrderDeriver {


    public static List<List<Integer>> buildCondensation(Graph g, SCCResult scc) {
        int compCount = scc.componentCount();
        List<Set<Integer>> temp = new ArrayList<>(compCount);
        for (int i = 0; i < compCount; i++) temp.add(new LinkedHashSet<>());

        int[] compIds = scc.getComponentIds();
        for (int[] e : g.edges()) {
            int u = e[0];
            int v = e[1];
            if (u < 0 || v < 0 || u >= compIds.length || v >= compIds.length) continue;
            int cu = compIds[u];
            int cv = compIds[v];
            if (cu != cv) temp.get(cu).add(cv);
        }

        List<List<Integer>> adj = new ArrayList<>(compCount);
        for (int i = 0; i < compCount; i++) {
            List<Integer> nbrs = new ArrayList<>(temp.get(i));
            adj.add(Collections.unmodifiableList(nbrs));
        }
        return Collections.unmodifiableList(adj);
    }

    public static List<Integer> deriveTaskOrderFromComponentOrder(List<Integer> componentOrder, SCCResult scc) {
        List<List<Integer>> comps = scc.getComponents();
        List<Integer> out = new ArrayList<>();
        for (int cid : componentOrder) {
            if (cid < 0 || cid >= comps.size()) continue; // ignore invalid
            List<Integer> comp = comps.get(cid);
            out.addAll(comp);
        }
        return out;
    }
}
