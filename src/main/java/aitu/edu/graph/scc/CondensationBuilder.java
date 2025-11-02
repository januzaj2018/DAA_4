package aitu.edu.graph.scc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Builds the condensation graph from a graph and its SCC result.
 */
public class CondensationBuilder {

    /**
     * Builds the condensation graph from edges, node count, and SCC result.
     *
     * @param n     the number of nodes
     * @param edges the list of edges
     * @param scc   the SCC result
     * @return the Condensation object
     */
    public static Condensation build(int n, List<int[]> edges, SCCResult scc) {
        int[] compIds = scc.getComponentIds();
        int k = scc.componentCount();
        List<Set<Integer>> sets = new ArrayList<>(k);
        for (int i = 0; i < k; i++) sets.add(new HashSet<>());

        // Collect inter-component edges
        for (int[] e : edges) {
            int u = e[0], v = e[1];
            if (u < 0 || u >= n || v < 0 || v >= n) continue;
            int cu = compIds[u], cv = compIds[v];
            if (cu != cv) sets.get(cu).add(cv);
        }

        // Build adjacency list for condensation
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

    /**
     * Represents the condensation graph.
     */
    public static class Condensation {
        private final int componentCount;
        private final List<List<Integer>> adj;
        private final List<int[]> edges;

        /**
         * Constructs a Condensation.
         *
         * @param componentCount the number of components
         * @param adj            the adjacency list
         * @param edges          the list of edges
         */
        public Condensation(int componentCount, List<List<Integer>> adj, List<int[]> edges) {
            this.componentCount = componentCount;
            this.adj = adj;
            this.edges = edges;
        }

        /**
         * Returns the number of components.
         *
         * @return the component count
         */
        public int getComponentCount() {
            return componentCount;
        }

        /**
         * Returns the adjacency list.
         *
         * @return the adjacency list
         */
        public List<List<Integer>> getAdjacency() {
            return adj;
        }

        /**
         * Returns the list of edges.
         *
         * @return the edges list
         */
        public List<int[]> getEdges() {
            return edges;
        }
    }
}
