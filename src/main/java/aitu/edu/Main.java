package aitu.edu;

import aitu.edu.graph.scc.KosarajuSCC;
import aitu.edu.graph.scc.SCCResult;
import aitu.edu.graph.util.Graph;
import aitu.edu.graph.util.GraphBuilder;
import aitu.edu.graph.util.TimerMetrics;
import aitu.edu.graph.util.Metrics;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String path = args.length > 0 ? args[0] : "data/tasks.json";

        Graph g = GraphBuilder.fromFile(path).build();
        Metrics metrics = new TimerMetrics();

        SCCResult scc = KosarajuSCC.computeSCC(g, metrics);

        // Output only the list of SCCs as JSON array of arrays
        List<List<Integer>> comps = scc.getComponents();
        ObjectMapper om = new ObjectMapper();
        String out = om.writeValueAsString(comps);
        // Output metrics
        String metricsJson = om.writerWithDefaultPrettyPrinter().writeValueAsString(metrics);
        System.out.println(metricsJson);
        System.out.println(out);
    }
}