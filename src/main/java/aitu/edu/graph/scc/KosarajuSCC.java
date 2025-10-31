package aitu.edu.graph.scc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KosarajuSCC {


    public static SCCResult computeSCC(int n, List<int[]> edges) {
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

        boolean[] visited = new boolean[n];
        List<Integer> order = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            if (!visited[i]) dfs1(i, adj, visited, order);
        }

        int[] compIds = new int[n];
        Arrays.fill(compIds, -1);
        List<List<Integer>> components = new ArrayList<>();

        for (int i = order.size() - 1; i >= 0; i--) {
            int v = order.get(i);
            if (compIds[v] == -1) {
                List<Integer> comp = new ArrayList<>();
                int cid = components.size();
                dfs2(v, rev, compIds, cid, comp);
                components.add(comp);
            }
        }

        return new SCCResult(compIds, components);
    }

    private static void dfs1(int v, List<List<Integer>> adj, boolean[] visited, List<Integer> order) {
        visited[v] = true;
        for (int to : adj.get(v)) {
            if (!visited[to]) dfs1(to, adj, visited, order);
        }
        order.add(v);
    }

    private static void dfs2(int v, List<List<Integer>> rev, int[] compIds, int cid, List<Integer> comp) {
        compIds[v] = cid;
        comp.add(v);
        for (int to : rev.get(v)) {
            if (compIds[to] == -1) dfs2(to, rev, compIds, cid, comp);
        }
    }
}
