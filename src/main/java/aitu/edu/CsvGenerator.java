package aitu.edu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvGenerator {

    public static void main(String[] args) throws IOException {
        String dataDir = "data/";
        List<String> reportFiles = getReportJsonFiles(dataDir);

        ObjectMapper om = new ObjectMapper();

        List<Map<String, Object>> sccData = new ArrayList<>();
        List<Map<String, Object>> topoData = new ArrayList<>();
        List<Map<String, Object>> spData = new ArrayList<>();
        List<Map<String, Object>> longestData = new ArrayList<>();

        for (String fileName : reportFiles) {
            String filePath = dataDir + fileName;
            JsonNode root = om.readTree(new File(filePath));
            if (!root.isArray()) continue;
            for (JsonNode report : root) {
                if (!report.isObject()) continue;

                int graphId = report.get("graph_id").asInt();
                JsonNode inputStats = report.get("input_stats");
                int vertices = inputStats.get("vertices").asInt();
                int edges = inputStats.get("edges").asInt();
                String density = inputStats.get("density").asText();
                String variant = inputStats.get("variant").asText();
                int source = inputStats.get("source").asInt();
                String weightModel = inputStats.get("weight_model").asText();

                // SCC
                JsonNode scc = report.get("kosaraju_scc");
                if (scc != null) {
                    int numSccs = scc.get("num_sccs").asInt();
                    long ops = scc.get("operations_count").asLong();
                    long time = scc.get("execution_time_ns").asLong();
                    sccData.add(Map.of(
                            "graph_id", graphId,
                            "vertices", vertices,
                            "edges", edges,
                            "density", density,
                            "variant", variant,
                            "source", source,
                            "weight_model", weightModel,
                            "num_sccs", numSccs,
                            "operations_count", ops,
                            "execution_time_ns", time
                    ));
                }

                // Topo
                JsonNode topo = report.get("topological_sort");
                if (topo != null) {
                    long ops = topo.get("operations_count").asLong();
                    long time = topo.get("execution_time_ns").asLong();
                    topoData.add(Map.of(
                            "graph_id", graphId,
                            "vertices", vertices,
                            "edges", edges,
                            "density", density,
                            "variant", variant,
                            "source", source,
                            "weight_model", weightModel,
                            "operations_count", ops,
                            "execution_time_ns", time
                    ));
                }

                // Shortest Path
                JsonNode sp = report.get("shortest_path");
                if (sp != null) {
                    long ops = sp.get("operations_count").asLong();
                    long time = sp.get("execution_time_ns").asLong();
                    // produce a bracketed, sorted list of reachable node ids like "[0,1,2]"
                    JsonNode pathsNode = sp.get("paths");
                    String pathsList;
                    if (pathsNode == null || pathsNode.isMissingNode() || pathsNode.isEmpty()) {
                        pathsList = "[]";
                    } else {
                        java.util.Set<Integer> reachable = new java.util.TreeSet<>();
                        Iterator<String> fns = pathsNode.fieldNames();
                        while (fns.hasNext()) {
                            String fn = fns.next();
                            JsonNode info = pathsNode.get(fn);
                            if (info == null) continue;
                            JsonNode pathArr = info.get("path");
                            if (pathArr != null && pathArr.isArray()) {
                                for (JsonNode n : pathArr) reachable.add(n.asInt());
                            }
                        }
                        if (reachable.isEmpty()) {
                            pathsList = "[]";
                        } else {
                            StringBuilder sb = new StringBuilder();
                            sb.append("[");
                            boolean first = true;
                            for (int node : reachable) {
                                if (!first) sb.append(",");
                                sb.append(node);
                                first = false;
                            }
                            sb.append("]");
                            pathsList = sb.toString();
                        }
                    }
                     // use LinkedHashMap to preserve column order and avoid Map.of size limits
                     java.util.Map<String, Object> spMap = new java.util.LinkedHashMap<>();
                     spMap.put("graph_id", graphId);
                     spMap.put("vertices", vertices);
                     spMap.put("edges", edges);
                     spMap.put("density", density);
                     spMap.put("variant", variant);
                     spMap.put("source", source);
                     spMap.put("weight_model", weightModel);
                     spMap.put("operations_count", ops);
                     spMap.put("execution_time_ns", time);
                     spMap.put("paths", pathsList);
                     spData.add(spMap);
                 }

                // Longest Path
                JsonNode longest = report.get("longest_path");
                if (longest != null) {
                    long critLen = longest.get("critical_path_length").asLong();
                    String critPath = longest.get("critical_path") != null ? longest.get("critical_path").toString() : "[]";
                    String nodeDurations = longest.get("node_durations") != null ? longest.get("node_durations").toString() : "[]";
                    long ops = longest.get("operations_count").asLong();
                    long time = longest.get("execution_time_ns").asLong();
                    // Map.of supports up to 10 entries; longestData needs more, so use a LinkedHashMap to preserve column order
                    java.util.Map<String, Object> longestMap = new java.util.LinkedHashMap<>();
                    longestMap.put("graph_id", graphId);
                    longestMap.put("vertices", vertices);
                    longestMap.put("edges", edges);
                    longestMap.put("density", density);
                    longestMap.put("variant", variant);
                    longestMap.put("source", source);
                    longestMap.put("weight_model", weightModel);
                    longestMap.put("critical_path_length", critLen);
                    longestMap.put("critical_path", critPath);
                    longestMap.put("node_durations", nodeDurations);
                    longestMap.put("operations_count", ops);
                    longestMap.put("execution_time_ns", time);
                    longestData.add(longestMap);
                }
            }
        }

        // Write CSVs
        sccData.sort(java.util.Comparator.comparingInt(a -> (Integer) a.get("graph_id")));
        topoData.sort(java.util.Comparator.comparingInt(a -> (Integer) a.get("graph_id")));
        spData.sort(java.util.Comparator.comparingInt(a -> (Integer) a.get("graph_id")));
        longestData.sort(java.util.Comparator.comparingInt(a -> (Integer) a.get("graph_id")));

        // Create summary data (require all four pieces present for a graph)
        List<Map<String, Object>> summaryData = new ArrayList<>();
        for (Map<String, Object> scc : sccData) {
            int gid = (Integer) scc.get("graph_id");
            Map<String, Object> topo = topoData.stream().filter(t -> (Integer)t.get("graph_id") == gid).findFirst().orElse(null);
            Map<String, Object> sp = spData.stream().filter(s -> (Integer)s.get("graph_id") == gid).findFirst().orElse(null);
            Map<String, Object> longest = longestData.stream().filter(l -> (Integer)l.get("graph_id") == gid).findFirst().orElse(null);
            if (topo != null && sp != null && longest != null) {
                long totalOps = (Long)scc.get("operations_count") + (Long)topo.get("operations_count") + (Long)sp.get("operations_count") + (Long)longest.get("operations_count");
                long totalTime = (Long)scc.get("execution_time_ns") + (Long)topo.get("execution_time_ns") + (Long)sp.get("execution_time_ns") + (Long)longest.get("execution_time_ns");
                summaryData.add(Map.of(
                        "graph_id", gid,
                        "vertices", scc.get("vertices"),
                        "edges", scc.get("edges"),
                        "density", scc.get("density"),
                        "variant", scc.get("variant"),
                        "source", scc.get("source"),
                        "weight_model", scc.get("weight_model"),
                        "total_operations_count", totalOps,
                        "total_execution_time_ns", totalTime
                ));
            }
        }
        summaryData.sort(java.util.Comparator.comparingInt(a -> (Integer) a.get("graph_id")));

        writeCsv("data/scc_results.csv", sccData, List.of("graph_id","vertices","edges","density","variant","source","weight_model","num_sccs","operations_count","execution_time_ns"));
        writeCsv("data/topo_results.csv", topoData, List.of("graph_id","vertices","edges","density","variant","source","weight_model","operations_count","execution_time_ns"));
        writeCsv("data/shortest_path_results.csv", spData, List.of("graph_id","vertices","edges","density","variant","source","weight_model","operations_count","execution_time_ns","paths"));
        writeCsv("data/longest_path_results.csv", longestData, List.of("graph_id","vertices","edges","density","variant","source","weight_model","critical_path_length","critical_path","node_durations","operations_count","execution_time_ns"));
        writeCsv("data/summary_results.csv", summaryData, List.of("graph_id","vertices","edges","density","variant","source","weight_model","total_operations_count","total_execution_time_ns"));

        System.out.println("CSV files generated.");
    }

    private static List<String> getReportJsonFiles(String directory) throws IOException {
        Path dirPath = Paths.get(directory);
        if (!Files.isDirectory(dirPath)) {
            System.err.println("Provided path is not a directory: " + directory);
            return List.of();
        }
        try (Stream<Path> paths = Files.list(dirPath)) {
            return paths.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.startsWith("report_") && name.endsWith(".json"))
                    .collect(Collectors.toList());
        }
    }

    private static void writeCsv(String filePath, List<Map<String, Object>> data, List<String> columnOrder) throws IOException {
        if (data.isEmpty()) return;
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write header
            writer.write(String.join(",", columnOrder));
            writer.write("\n");

            // Write rows
            for (Map<String, Object> row : data) {
                List<String> values = new ArrayList<>();
                for (String key : columnOrder) {
                    Object raw = row.get(key);
                    String s = raw == null ? "" : String.valueOf(raw);
                    // If the value contains comma, newline or quote, escape it per CSV rules by
                    // wrapping in double quotes and doubling any internal double quotes.
                    if (s.contains(",") || s.contains("\n") || s.contains("\r") || s.contains("\"")) {
                        s = s.replace("\"", "\"\"");
                        s = "\"" + s + "\"";
                    }
                    values.add(s);
                }
                writer.write(String.join(",", values));
                writer.write("\n");
            }
        }
    }
}
