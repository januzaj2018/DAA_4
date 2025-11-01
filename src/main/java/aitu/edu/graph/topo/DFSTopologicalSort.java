package aitu.edu.graph.topo;

import aitu.edu.graph.util.Metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class DFSTopologicalSort {
    public static List<Integer> topologicalOrder(List<List<Integer>> adj, Metrics metrics) {
        int n = adj == null ? 0 : adj.size();
        boolean[] visited = new boolean[n];
        List<Integer> order = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            if (!visited[i]) dfs(i, adj, visited, order, metrics);
        }
        Collections.reverse(order);
        return order;
    }

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
        // record in post-order
        order.add(v);
    }
}
