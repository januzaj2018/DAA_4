package aitu.edu.graph.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class GraphBuilder {
    private int n = -1;
    private final List<List<Integer>> adj = new ArrayList<>();
    private final Map<Integer, Long> durations = new HashMap<>();

    public GraphBuilder() {}

    public static GraphBuilder fromFile(String path) throws IOException {
        ObjectMapper om = new ObjectMapper();
        JsonNode root = om.readTree(new File(path));
        GraphBuilder gb = new GraphBuilder();

        String weightModel = null;
        if (root.has("weight_model")) weightModel = root.get("weight_model").asText(null);

        // nodes array
        if (root.has("nodes") && root.get("nodes").isArray()) {
            List<Integer> nodes = new ArrayList<>();
            for (JsonNode n : root.get("nodes")) nodes.add(n.asInt());
            int max = nodes.stream().mapToInt(Integer::intValue).max().orElse(-1);
            gb.ensureN(max + 1);
        }

        // If explicit durations provided, use them
        if (root.has("durations") && root.get("durations").isObject()) {
            Iterator<String> it = root.get("durations").fieldNames();
            while (it.hasNext()) {
                String key = it.next();
                try {
                    int node = Integer.parseInt(key);
                    long val = root.get("durations").get(key).asLong();
                    gb.setDuration(node, val);
                } catch (NumberFormatException ex) {
                    // ignore non-integer keys
                }
            }
        }

        // edges can be arrays ([u,v]) or objects ({"u":..,"v":..,"w":..})
        Map<Integer, Long> inferredNodeDur = new HashMap<>(); // temp store when inferring durations from edge weights
        if (root.has("edges") && root.get("edges").isArray()) {
            for (JsonNode e : root.get("edges")) {
                if (e.isArray() && e.size() >= 2) {
                    int u = e.get(0).asInt();
                    int v = e.get(1).asInt();
                    gb.addEdge(u, v);
                } else if (e.isObject() && e.has("u") && e.has("v")) {
                    int u = e.get("u").asInt();
                    int v = e.get("v").asInt();
                    gb.addEdge(u, v);
                    if (e.has("w")) {
                        long w = e.get("w").asLong();
                        // If weight model is node and no explicit durations given, infer by using max outgoing w per node
                        if ("node".equalsIgnoreCase(weightModel)) {
                            long prev = inferredNodeDur.getOrDefault(u, Long.MIN_VALUE);
                            inferredNodeDur.put(u, Math.max(prev, w));
                        }
                    }
                }
            }
        }

        // If weight_model==node and durations were not explicitly provided, use inferred durations
        if ("node".equalsIgnoreCase(weightModel) && !inferredNodeDur.isEmpty()) {
            for (Map.Entry<Integer, Long> ent : inferredNodeDur.entrySet()) {
                int node = ent.getKey();
                long val = ent.getValue();
                // only set if not already present from explicit durations
                if (!gb.durations.containsKey(node)) gb.setDuration(node, val);
            }
        }

        // If top-level n provided and builder hasn't been sized, ensure appropriate size
        if (root.has("n") && root.get("n").isInt()) {
            int want = root.get("n").asInt();
            gb.ensureN(want);
        }

        return gb;
    }

    public GraphBuilder ensureN(int n) {
        if (this.n >= n) return this;
        for (int i = this.n == -1 ? 0 : this.n; i < n; i++) adj.add(new ArrayList<>());
        this.n = n;
        return this;
    }

    public GraphBuilder addEdge(int u, int v) {
        int max = Math.max(u, v);
        if (n <= max) ensureN(max + 1);
        adj.get(u).add(v);
        return this;
    }

    public GraphBuilder setDuration(int node, long duration) {
        if (n <= node) ensureN(node + 1);
        durations.put(node, duration);
        return this;
    }

    public Graph build() {
        int finalN = Math.max(0, n == -1 ? 0 : n);
        return new Graph(finalN, adj, durations);
    }
}
