package aitu.edu;

import aitu.edu.graph.scc.KosarajuSCC;
import aitu.edu.graph.scc.SCCResult;
import aitu.edu.graph.util.Graph;
import aitu.edu.graph.util.GraphBuilder;
import aitu.edu.graph.util.TimerMetrics;
import aitu.edu.graph.util.Metrics;
import aitu.edu.graph.topo.DFSTopologicalSort;
import aitu.edu.graph.topo.TaskOrderDeriver;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String path = args.length > 0 ? args[0] : "data/tasks.json";

        Graph g = GraphBuilder.fromFile(path).build();
        Metrics sccMetrics = new TimerMetrics();

        SCCResult scc = KosarajuSCC.computeSCC(g, sccMetrics);

        ObjectMapper om = new ObjectMapper();

        // --- Topological sort on condensation ---
        Metrics topoMetrics = new TimerMetrics();
        List<List<Integer>> condensation = TaskOrderDeriver.buildCondensation(g, scc);
        List<Integer> componentOrder = DFSTopologicalSort.topologicalOrder(condensation, topoMetrics);
        List<Integer> taskOrder = TaskOrderDeriver.deriveTaskOrderFromComponentOrder(componentOrder, scc);

        // Print outputs: component order (order of components in condensation) and derived task order
        String topoComponentOrderJson = om.writeValueAsString(componentOrder);
        String topoTaskOrderJson = om.writeValueAsString(taskOrder);
        System.out.println(topoComponentOrderJson);
        System.out.println(topoTaskOrderJson);
    }
}