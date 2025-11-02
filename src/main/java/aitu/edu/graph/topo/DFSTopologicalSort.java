package aitu.edu.graph.topo;

import aitu.edu.graph.util.Metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Performs topological sorting using depth-first search (DFS).
 */
public class DFSTopologicalSort {
    /**
     * Computes the topological order of the graph.
     *
     * @param adj     the adjacency list
     * @param metrics optional metrics collector
     * @return the list of nodes in topological order
     */
    public static List<Integer> topologicalOrder(List<List<Integer>> adj, Metrics metrics) {
        int n = adj == null ? 0 : adj.size();
        boolean[] visited = new boolean[n];
        List<Integer> order = new ArrayList<>(n);
        // Perform DFS from each unvisited node
        for (int i = 0; i < n; i++) {
            if (!visited[i]) dfs(i, adj, visited, order, metrics);
        }
        // Reverse to get topological order
        Collections.reverse(order);
        return order;
    }

    /**
     * DFS helper to visit nodes and record post-order.
     *
     * @param v       current node
     * @param adj     adjacency list
     * @param visited visited array
     * @param order   list to store post-order
     * @param metrics optional metrics
     */
    private static void dfs(int v, List<List<Integer>> adj, boolean[] visited, List<Integer> order, Metrics metrics) {
        if (metrics != null) metrics.incDfsVisit();
        visited[v] = true;
        List<Integer> nbrs = adj.get(v);
        if (nbrs != null) {
            for (int to : nbrs) {
                if (metrics != null) metrics.incDfsEdge();
                if (!visited[to]) dfs(to, adj, visited, order, metrics);
            }
        }
        // Record in post-order
        order.add(v);
    }
}
