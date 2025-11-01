package aitu.edu;

import aitu.edu.graph.dagsp.CriticalPathExtractor;
import aitu.edu.graph.dagsp.DagShortestPath;
import aitu.edu.graph.dagsp.DagLongestPath;
import aitu.edu.graph.dagsp.PathResult;
import aitu.edu.graph.scc.SCCResult;
import aitu.edu.graph.scc.KosarajuSCC;
import aitu.edu.graph.scc.CondensationBuilder;
import aitu.edu.graph.topo.DFSTopologicalSort;
import aitu.edu.graph.topo.TaskOrderDeriver;
import aitu.edu.graph.util.Graph;
import aitu.edu.graph.util.GraphBuilder;
import aitu.edu.graph.util.Metrics;
import aitu.edu.graph.util.TimerMetrics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TasksReportGenerator {

    public static void generateReport(String inputPath, String outputPath) throws IOException {
        ObjectMapper om = new ObjectMapper();
        JsonNode root = om.readTree(new File(inputPath));

        ArrayNode outArr = om.createArrayNode();

        if (!root.has("graphs") || !root.get("graphs").isArray()) {
            // Single graph object - reuse existing GraphBuilder.fromJson
            JsonNode gnode = root;
            outArr.add(processGraph(gnode, om));
        } else {
            int idx = 0;
            for (JsonNode gnode : root.get("graphs")) {
                System.out.println("[report] processing graph index=" + idx + " id=" + (gnode.has("id") ? gnode.get("id").asText() : "?") );
                try {
                    outArr.add(processGraph(gnode, om));
                } catch (Throwable t) {
                    System.err.println("[report] error processing graph index=" + idx + " - " + t);
                    t.printStackTrace(System.err);
                    // create a minimal failure entry
                    ObjectNode fail = om.createObjectNode();
                    fail.put("graph_id", gnode.has("id") ? gnode.get("id").asInt(-1) : -1);
                    fail.put("error", t.toString());
                    outArr.add(fail);
                }
                idx++;
            }
        }

        // write output
        om.writerWithDefaultPrettyPrinter().writeValue(new File(outputPath), outArr);
        System.out.println("[report] written output to " + outputPath);
    }

    private static ObjectNode processGraph(JsonNode gnode, ObjectMapper om) {
        ObjectNode out = om.createObjectNode();

        int graphId = gnode.has("id") ? gnode.get("id").asInt() : -1;
        out.put("graph_id", graphId);

        System.out.println("[report] building graph id=" + graphId);
        // Build Graph
        Graph g = GraphBuilder.fromJson(gnode).build();
        System.out.println("[report] built graph id=" + graphId + " nodes=" + g.nodeCount() + " edges=" + g.edges().size());

        // input_stats
        ObjectNode input = om.createObjectNode();
        input.put("vertices", g.nodeCount());
        input.put("edges", g.edges().size());
        input.put("density", gnode.has("density") ? gnode.get("density").asText() : "");
        input.put("variant", gnode.has("variant") ? gnode.get("variant").asText() : "");
        input.put("source", gnode.has("source") ? gnode.get("source").asInt() : -1);
        input.put("weight_model", gnode.has("weight_model") ? gnode.get("weight_model").asText() : "");
        out.set("input_stats", input);

        long totalOps = 0;
        double totalMs = 0.0;

        // --- Kosaraju SCC ---
        System.out.println("[report] computing SCC for graph id=" + graphId);
        TimerMetrics sccMetrics = new TimerMetrics();
        long sccStart = System.nanoTime();
        SCCResult scc = KosarajuSCC.computeSCC(g, sccMetrics);
        long sccEnd = System.nanoTime();
        long sccOps = sccMetrics.getDfsVisits() + sccMetrics.getDfsEdges() + sccMetrics.getRelaxations();
        double sccMs = (sccEnd - sccStart) / 1_000_000.0;
        System.out.println("[report] scc done id=" + graphId + " comps=" + scc.componentCount() + " ops=" + sccOps + " ms=" + sccMs);

        ObjectNode sccNode = om.createObjectNode();
        sccNode.put("num_sccs", scc.componentCount());
        ArrayNode comps = om.createArrayNode();
        for (List<Integer> comp : scc.getComponents()) {
            ArrayNode arr = om.createArrayNode();
            for (int v : comp) arr.add(v);
            comps.add(arr);
        }
        sccNode.set("sccs", comps);
        sccNode.put("operations_count", sccOps);
        sccNode.put("execution_time_ms", sccMs);
        out.set("kosaraju_scc", sccNode);

        totalOps += sccOps;
        totalMs += sccMs;

        // condensation_graph (use TaskOrderDeriver to build condensation adjacency)
        System.out.println("[report] building condensation for graph id=" + graphId);
        List<List<Integer>> condAdj = TaskOrderDeriver.buildCondensation(g, scc);
        int condV = condAdj == null ? 0 : condAdj.size();
        int condE = 0;
        if (condAdj != null) for (List<Integer> l : condAdj) condE += (l == null ? 0 : l.size());
        ObjectNode condNode = om.createObjectNode();
        condNode.put("vertices", condV);
        condNode.put("edges", condE);
        out.set("condensation_graph", condNode);
        System.out.println("[report] condensation done id=" + graphId + " v=" + condV + " e=" + condE);

        // topological sort on original graph
        System.out.println("[report] computing topological order for graph id=" + graphId);
        TimerMetrics topoMetrics = new TimerMetrics();
        long topoStart = System.nanoTime();
        List<Integer> topoOrder = DFSTopologicalSort.topologicalOrder(g.adjacency(), topoMetrics);
        long topoEnd = System.nanoTime();
        long topoOps = topoMetrics.getDfsVisits() + topoMetrics.getDfsEdges() + topoMetrics.getRelaxations();
        double topoMs = (topoEnd - topoStart) / 1_000_000.0;
        System.out.println("[report] topo done id=" + graphId + " orderLen=" + topoOrder.size() + " ops=" + topoOps + " ms=" + topoMs);

        ObjectNode topoNode = om.createObjectNode();
        ArrayNode topoArr = om.createArrayNode();
        for (int v : topoOrder) topoArr.add(v);
        topoNode.set("topological_order", topoArr);
        topoNode.put("operations_count", topoOps);
        topoNode.put("execution_time_ms", topoMs);
        out.set("topological_sort", topoNode);

        totalOps += topoOps;
        totalMs += topoMs;

        // shortest paths (from source)
        int source = gnode.has("source") ? gnode.get("source").asInt(-1) : -1;
        boolean isDag = gnode.has("metadata") && gnode.get("metadata").has("is_dag") && gnode.get("metadata").get("is_dag").asBoolean();
        System.out.println("[report] computing shortest paths for graph id=" + graphId + " src=" + source + " isDag=" + isDag);
        TimerMetrics spMetrics = new TimerMetrics();
        long spStart = System.nanoTime();
        PathResult sp = null;
        if (isDag) {
            sp = DagShortestPath.shortestPath(g, source, spMetrics);
        }
        long spEnd = System.nanoTime();
        long spOps = spMetrics.getDfsVisits() + spMetrics.getDfsEdges() + spMetrics.getRelaxations();
        double spMs = (spEnd - spStart) / 1_000_000.0;
        System.out.println("[report] shortest paths done id=" + graphId + " ops=" + spOps + " ms=" + spMs);

        ObjectNode spNode = om.createObjectNode();
        spNode.put("source", source);
        spNode.put("destination", "all_reachable");
        ObjectNode pathsNode = om.createObjectNode();
        if (sp != null) {
            long[] dists = sp.distances();
            for (int v = 0; v < dists.length; v++) {
                if (v == source) continue;
                if (dists[v] == PathResult.INF) continue;
                List<Integer> path = sp.reconstructPath(v);
                ArrayNode pathArr = om.createArrayNode();
                ArrayNode durArr = om.createArrayNode();
                for (int node : path) {
                    pathArr.add(node);
                    long dv = g.durationOf(node).isPresent() ? g.durationOf(node).getAsLong() : 0L;
                    durArr.add(dv);
                }
                ObjectNode info = om.createObjectNode();
                info.set("path", pathArr);
                info.set("node_durations", durArr);
                info.put("path_length", dists[v]);
                pathsNode.set(String.valueOf(v), info);
            }
        }
        spNode.set("paths", pathsNode);
        spNode.put("operations_count", spOps);
        spNode.put("execution_time_ms", spMs);
        out.set("shortest_path", spNode);

        totalOps += spOps;
        totalMs += spMs;

        // longest / critical path
        System.out.println("[report] computing critical (longest) path for graph id=" + graphId + " isDag=" + isDag);
        TimerMetrics lpMetrics = new TimerMetrics();
        long lpStart = System.nanoTime();
        PathResult lp = null;
        if (isDag) {
            lp = CriticalPathExtractor.criticalPath(g, lpMetrics);
        }
        long lpEnd = System.nanoTime();
        long lpOps = lpMetrics.getDfsVisits() + lpMetrics.getDfsEdges() + lpMetrics.getRelaxations();
        double lpMs = (lpEnd - lpStart) / 1_000_000.0;
        System.out.println("[report] critical path done id=" + graphId + " ops=" + lpOps + " ms=" + lpMs);

        ObjectNode lpNode = om.createObjectNode();
        if (lp != null) {
            // find sink = argmax distance
            long[] lpd = lp.distances();
            long best = PathResult.NEG_INF;
            int sink = -1;
            for (int i = 0; i < lpd.length; i++) {
                if (lpd[i] > best) {
                    best = lpd[i];
                    sink = i;
                }
            }
            List<Integer> criticalPath = lp.reconstructPath(sink);
            ArrayNode cpArr = om.createArrayNode();
            ArrayNode cpDur = om.createArrayNode();
            for (int node : criticalPath) {
                cpArr.add(node);
                long dv = g.durationOf(node).isPresent() ? g.durationOf(node).getAsLong() : 0L;
                cpDur.add(dv);
            }

            lpNode.put("critical_path_length", best == PathResult.NEG_INF ? 0 : best);
            lpNode.set("critical_path", cpArr);
            lpNode.set("node_durations", cpDur);
        } else {
            lpNode.put("critical_path_length", 0);
            lpNode.set("critical_path", om.createArrayNode());
            lpNode.set("node_durations", om.createArrayNode());
        }
        lpNode.put("operations_count", lpOps);
        lpNode.put("execution_time_ms", lpMs);
        out.set("longest_path", lpNode);

        totalOps += lpOps;
        totalMs += lpMs;

        out.put("total_operations_count", totalOps);
        out.put("total_execution_time_ms", totalMs);

        System.out.println("[report] finished graph id=" + graphId + " totalOps=" + totalOps + " totalMs=" + totalMs);
        return out;
    }
}

