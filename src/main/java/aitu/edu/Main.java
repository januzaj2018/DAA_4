package aitu.edu;

import aitu.edu.graph.scc.CondensationBuilder;
import aitu.edu.graph.scc.KosarajuSCC;
import aitu.edu.graph.scc.SCCResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String path = args.length > 0 ? args[0] : "data/tasks.json";
        System.out.println("Reading graph from: " + path);

        GraphInput input = readGraph(path);
        int n = input.n;
        List<int[]> edges = input.edges;
        List<String> names = input.names;

        SCCResult scc = KosarajuSCC.computeSCC(n, edges);

        // Print components and sizes
        System.out.println("Strongly connected components (count = " + scc.componentCount() + "):");
        List<List<Integer>> comps = scc.getComponents();
        for (int i = 0; i < comps.size(); i++) {
            List<Integer> comp = comps.get(i);
            System.out.print("Component " + i + " (size=" + comp.size() + "): ");
            List<String> outNames = new ArrayList<>();
            for (int v : comp) {
                outNames.add(v < names.size() ? names.get(v) : String.valueOf(v));
            }
            System.out.println(outNames);
        }

        // Build condensation
        CondensationBuilder.Condensation cond = CondensationBuilder.build(n, edges, scc);
        System.out.println("\nCondensation DAG: components = " + cond.getComponentCount());
        System.out.println("Adjacency lists:");
        List<List<Integer>> adj = cond.getAdjacency();
        for (int i = 0; i < adj.size(); i++) {
            List<Integer> outs = adj.get(i);
            System.out.print("C" + i + " -> ");
            System.out.println(outs);
        }
        System.out.println("Edge list (component->component):");
        for (int[] e : cond.getEdges()) System.out.println(e[0] + " -> " + e[1]);
    }

    private static class GraphInput {
        int n;
        List<int[]> edges;
        List<String> names;

        GraphInput(int n, List<int[]> edges, List<String> names) {
            this.n = n;
            this.edges = edges;
            this.names = names;
        }
    }

    private static GraphInput readGraph(String path) throws IOException {
        ObjectMapper om = new ObjectMapper();
        JsonNode root = om.readTree(new File(path));

        // Two supported formats:
        // 1) { "n": 5, "edges": [[0,1],[1,2]] }
        // 2) { "vertices": ["A","B"], "edges": [["A","B"], ["B","C"]] }

        List<int[]> edges = new ArrayList<>();
        List<String> names = new ArrayList<>();
        if (root.has("n")) {
            int n = root.get("n").asInt();
            if (root.has("edges") && root.get("edges").isArray()) {
                for (JsonNode e : root.get("edges")) {
                    if (e.isArray() && e.size() >= 2) {
                        int u = e.get(0).asInt();
                        int v = e.get(1).asInt();
                        edges.add(new int[]{u, v});
                    }
                }
            }
            for (int i = 0; i < n; i++) names.add(String.valueOf(i));
            return new GraphInput(n, edges, names);
        } else if (root.has("vertices") && root.get("vertices").isArray()) {
            Map<String, Integer> idx = new LinkedHashMap<>();
            int i = 0;
            for (JsonNode vn : root.get("vertices")) {
                String name = vn.asText();
                idx.put(name, i++);
                names.add(name);
            }
            int n = names.size();
            if (root.has("edges") && root.get("edges").isArray()) {
                for (JsonNode e : root.get("edges")) {
                    if (e.isArray() && e.size() >= 2) {
                        String a = e.get(0).asText();
                        String b = e.get(1).asText();
                        Integer u = idx.get(a);
                        Integer v = idx.get(b);
                        if (u != null && v != null) edges.add(new int[]{u, v});
                    }
                }
            }
            return new GraphInput(n, edges, names);
        } else {
            // Try to infer: if root is an array of edges of integer pairs
            if (root.isObject() && root.has("edges") && root.get("edges").isArray()) {
                int max = -1;
                for (JsonNode e : root.get("edges")) {
                    if (e.isArray() && e.size() >= 2) {
                        int u = e.get(0).asInt();
                        int v = e.get(1).asInt();
                        edges.add(new int[]{u, v});
                        max = Math.max(max, Math.max(u, v));
                    }
                }
                int n = max + 1;
                for (int i = 0; i < n; i++) names.add(String.valueOf(i));
                return new GraphInput(n, edges, names);
            }
        }

        throw new IOException("Unrecognized graph format in " + path);
    }
}