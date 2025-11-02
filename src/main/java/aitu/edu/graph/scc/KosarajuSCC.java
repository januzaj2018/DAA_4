package aitu.edu.graph.scc;

import aitu.edu.graph.util.Graph;
import aitu.edu.graph.util.Metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implements Kosaraju's algorithm to find strongly connected components (SCCs) in a directed graph.
 */
public class KosarajuSCC {

    /**
     * Computes SCCs from a list of edges and number of nodes.
     *
     * @param n     the number of nodes
     * @param edges the list of edges as int[2] arrays
     * @return the SCCResult containing component assignments and lists
     */
    public static SCCResult computeSCC(int n, List<int[]> edges) {
        return computeSCC(n, edges, null);
    }

    /**
     * Computes SCCs from a list of edges and number of nodes, with optional metrics.
     *
     * @param n       the number of nodes
     * @param edges   the list of edges as int[2] arrays
     * @param metrics optional metrics collector
     * @return the SCCResult containing component assignments and lists
     */
    public static SCCResult computeSCC(int n, List<int[]> edges, Metrics metrics) {
        // Build adjacency list and reverse adjacency list
        List<List<Integer>> adj = new ArrayList<>(n);
        List<List<Integer>> rev = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
            rev.add(new ArrayList<>());
        }
        for (int[] e : edges) {
            int u = e[0];
            int v = e[1];
            if (u < 0 || u >= n || v < 0 || v >= n) continue; // ignore invalid
            adj.get(u).add(v);
            rev.get(v).add(u);
        }

        // First DFS to get finishing order
        boolean[] visited = new boolean[n];
        List<Integer> order = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            if (!visited[i]) dfs1(i, adj, visited, order, metrics);
        }

        // Second DFS on reverse graph in reverse finishing order
        int[] compIds = new int[n];
        Arrays.fill(compIds, -1);
        List<List<Integer>> components = new ArrayList<>();

        for (int i = order.size() - 1; i >= 0; i--) {
            int v = order.get(i);
            if (compIds[v] == -1) {
                List<Integer> comp = new ArrayList<>();
                int cid = components.size();
                dfs2(v, rev, compIds, cid, comp, metrics);
                components.add(comp);
            }
        }

        return new SCCResult(compIds, components);
    }


    /**
     * Computes SCCs from a Graph object.
     *
     * @param g the graph
     * @return the SCCResult containing component assignments and lists
     */
    public static SCCResult computeSCC(Graph g) {
        return computeSCC(g, null);
    }

    /**
     * Computes SCCs from a Graph object, with optional metrics.
     *
     * @param g       the graph
     * @param metrics optional metrics collector
     * @return the SCCResult containing component assignments and lists
     */
    public static SCCResult computeSCC(Graph g, Metrics metrics) {
        int n = g.nodeCount();
        List<List<Integer>> adj = g.adjacency();
        // Build reverse adjacency list
        List<List<Integer>> rev = new ArrayList<>(n);
        for (int i = 0; i < n; i++) rev.add(new ArrayList<>());
        for (int u = 0; u < n; u++) {
            for (int v : adj.get(u)) {
                rev.get(v).add(u);
            }
        }

        // First DFS to get finishing order
        boolean[] visited = new boolean[n];
        List<Integer> order = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            if (!visited[i]) dfs1(i, adj, visited, order, metrics);
        }

        // Second DFS on reverse graph in reverse finishing order
        int[] compIds = new int[n];
        Arrays.fill(compIds, -1);
        List<List<Integer>> components = new ArrayList<>();

        for (int i = order.size() - 1; i >= 0; i--) {
            int v = order.get(i);
            if (compIds[v] == -1) {
                List<Integer> comp = new ArrayList<>();
                int cid = components.size();
                dfs2(v, rev, compIds, cid, comp, metrics);
                components.add(comp);
            }
        }

        return new SCCResult(compIds, components);
    }

    /**
     * First DFS pass to compute finishing times.
     *
     * @param v       current node
     * @param adj     adjacency list
     * @param visited visited array
     * @param order   list to store finishing order
     * @param metrics optional metrics
     */
    private static void dfs1(int v, List<List<Integer>> adj, boolean[] visited, List<Integer> order, Metrics metrics) {
        if (metrics != null) metrics.incDfsVisit();
        visited[v] = true;
        for (int to : adj.get(v)) {
            if (metrics != null) metrics.incDfsEdge();
            if (!visited[to]) dfs1(to, adj, visited, order, metrics);
        }
        order.add(v);
    }

    /**
     * Second DFS pass on reverse graph to assign components.
     *
     * @param v       current node
     * @param rev     reverse adjacency list
     * @param compIds component ID array
     * @param cid     current component ID
     * @param comp    list to store component nodes
     * @param metrics optional metrics
     */
    private static void dfs2(int v, List<List<Integer>> rev, int[] compIds, int cid, List<Integer> comp, Metrics metrics) {
        if (metrics != null) metrics.incDfsVisit();
        compIds[v] = cid;
        comp.add(v);
        for (int to : rev.get(v)) {
            if (metrics != null) metrics.incDfsEdge();
            if (compIds[to] == -1) dfs2(to, rev, compIds, cid, comp, metrics);
        }
    }
}