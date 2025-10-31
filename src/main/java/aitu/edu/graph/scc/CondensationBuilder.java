package aitu.edu.graph.scc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CondensationBuilder {

    public static Condensation build(int n, List<int[]> edges, SCCResult scc) {
        int[] compIds = scc.getComponentIds();
        int k = scc.componentCount();
        List<Set<Integer>> sets = new ArrayList<>(k);
        for (int i = 0; i < k; i++) sets.add(new HashSet<>());

        for (int[] e : edges) {
            int u = e[0], v = e[1];
            if (u < 0 || u >= n || v < 0 || v >= n) continue;
            int cu = compIds[u], cv = compIds[v];
            if (cu != cv) sets.get(cu).add(cv);
        }

        List<List<Integer>> adj = new ArrayList<>(k);
        List<int[]> dagEdges = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            List<Integer> outs = new ArrayList<>(sets.get(i));
            outs.sort(Integer::compareTo);
            adj.add(outs);
            for (int to : outs) dagEdges.add(new int[]{i, to});
        }

        return new Condensation(k, adj, dagEdges);
    }

    public static class Condensation {
        private final int componentCount;
        private final List<List<Integer>> adj;
        private final List<int[]> edges;

        public Condensation(int componentCount, List<List<Integer>> adj, List<int[]> edges) {
            this.componentCount = componentCount;
            this.adj = adj;
            this.edges = edges;
        }

        public int getComponentCount() {
            return componentCount;
        }

        public List<List<Integer>> getAdjacency() {
            return adj;
        }

        public List<int[]> getEdges() {
            return edges;
        }
    }
}
