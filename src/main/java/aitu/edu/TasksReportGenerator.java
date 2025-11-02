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

/**
 * Generates reports for graph processing tasks, including SCC, topological sort, shortest and longest paths.
 */
public class TasksReportGenerator {

    /**
     * Generates a report from the input JSON file and writes the results to the output JSON file.
     *
     * @param inputPath  the path to the input JSON file containing graph data
     * @param outputPath the path to the output JSON file where the report will be written
     * @throws IOException if there is an issue reading the input file or writing the output file
     */
    public static void generateReport(String inputPath, String outputPath) throws IOException {
        // Warm up the JVM with a dummy computation
        warmupJVM();

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

    /**
     * Warms up the JVM by performing dummy computations on a small graph.
     */
    private static void warmupJVM() {
        System.out.println("[report] warming up JVM...");
        // Create a small dummy graph
        GraphBuilder gb = new GraphBuilder();
        gb.addEdge(0, 1);
        gb.addEdge(1, 2);
        gb.addEdge(0, 2);
        gb.setDuration(0, 1);
        gb.setDuration(1, 2);
        gb.setDuration(2, 3);
        Graph dummyGraph = gb.build();

        // Run dummy computations
        TimerMetrics metrics = new TimerMetrics();
        SCCResult scc = KosarajuSCC.computeSCC(dummyGraph, metrics);
        List<Integer> topo = DFSTopologicalSort.topologicalOrder(dummyGraph.adjacency(), metrics);
        PathResult sp = DagShortestPath.shortestPath(dummyGraph, 0, metrics);
        PathResult lp = CriticalPathExtractor.criticalPath(dummyGraph, metrics);

        System.out.println("[report] JVM warmup complete.");
    }

    /**
     * Processes a single graph node and returns the report object.
     *
     * @param gnode the JSON node representing the graph
     * @param om    the ObjectMapper for creating JSON nodes
     * @return the ObjectNode containing the processed report data
     */
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
        long totalNs = 0;

        // --- Kosaraju SCC ---
        System.out.println("[report] computing SCC for graph id=" + graphId);
        TimerMetrics sccMetrics = new TimerMetrics();
        long sccStart = System.nanoTime();
        SCCResult scc = KosarajuSCC.computeSCC(g, sccMetrics);
        long sccEnd = System.nanoTime();
        long sccOps = sccMetrics.getDfsVisits() + sccMetrics.getDfsEdges() + sccMetrics.getRelaxations();
        long sccNs = sccEnd - sccStart;
        System.out.println("[report] scc done id=" + graphId + " comps=" + scc.componentCount() + " ops=" + sccOps + " ns=" + sccNs);

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
        sccNode.put("execution_time_ns", sccNs);
        out.set("kosaraju_scc", sccNode);

        totalOps += sccOps;
        totalNs += sccNs;

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
        long topoNs = topoEnd - topoStart;
        System.out.println("[report] topo done id=" + graphId + " orderLen=" + topoOrder.size() + " ops=" + topoOps + " ns=" + topoNs);

        ObjectNode topoNode = om.createObjectNode();
        ArrayNode topoArr = om.createArrayNode();
        for (int v : topoOrder) topoArr.add(v);
        topoNode.set("topological_order", topoArr);
        topoNode.put("operations_count", topoOps);
        topoNode.put("execution_time_ns", topoNs);
        out.set("topological_sort", topoNode);

        totalOps += topoOps;
        totalNs += topoNs;

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
        long spNs = spEnd - spStart;
        System.out.println("[report] shortest paths done id=" + graphId + " ops=" + spOps + " ns=" + spNs);

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
        spNode.put("execution_time_ns", spNs);
        out.set("shortest_path", spNode);

        totalOps += spOps;
        totalNs += spNs;

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
        long lpNs = lpEnd - lpStart;
        System.out.println("[report] critical path done id=" + graphId + " ops=" + lpOps + " ns=" + lpNs);

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
        lpNode.put("execution_time_ns", lpNs);
        out.set("longest_path", lpNode);

        totalOps += lpOps;
        totalNs += lpNs;

        out.put("total_operations_count", totalOps);
        out.put("total_execution_time_ns", totalNs);

        System.out.println("[report] finished graph id=" + graphId + " totalOps=" + totalOps + " totalNs=" + totalNs);
        return out;
    }
}
